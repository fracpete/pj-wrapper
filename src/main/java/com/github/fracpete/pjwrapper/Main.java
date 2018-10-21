/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Main.java
 * Copyright (C) 2018 FracPete
 */

package com.github.fracpete.pjwrapper;

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generates Python 3 wrapper code for a Java class.
 *
 * @author FracPete (fracpete at gmail dot com)
 */
public class Main {

  /**
   * Container for the parsed javap output.
   */
  public class MethodDescriptor {

    /** the name of the method. */
    public String name;

    /** the signature. */
    public String signature;

    /**
     * Returns a string representation of the container.
     *
     * @return		the description
     */
    public String toString() {
      return name + ": " + signature;
    }
  }

  /** the java home directory to use. */
  protected File m_JavaHome;

  /** the javap binary. */
  protected File m_Javap;

  /** the classnames to process. */
  protected List<String> m_Classes;

  /** the classpath to use. */
  protected String m_ClassPath;

  /** the Python output file. */
  protected File m_OutputFile;

  public Main() {
    m_JavaHome   = (System.getenv("JAVA_HOME") != null ? new File(System.getenv("JAVA_HOME")) : null);
    m_Classes    = new ArrayList<>();
    m_ClassPath  = null;
    m_OutputFile = null;
  }

  /**
   * Sets the java home directory.
   *
   * @param value	the directory
   */
  public void setJavaHome(File value) {
    m_JavaHome = value;
  }

  /**
   * Returns the java home directory.
   *
   * @return		the directory
   */
  public File getJavaHome() {
    return m_JavaHome;
  }

  /**
   * Sets the classpath to use.
   *
   * @param value	the classpath
   */
  public void setClassPath(String value) {
    m_ClassPath = value;
  }

  /**
   * Returns the classpath to use.
   *
   * @return		the classpath, null if not set
   */
  public String getClassPath() {
    return m_ClassPath;
  }

  /**
   * Sets the class names to process.
   *
   * @param value	the class names
   */
  public void setClasses(List<String> value) {
    m_Classes = value;
  }

  /**
   * Returns the class names to process.
   *
   * @return		the class names
   */
  public List<String> getClassesFile() {
    return m_Classes;
  }

  /**
   * Sets the Python output file.
   *
   * @param value	the file
   */
  public void setOutputFile(File value) {
    m_OutputFile = value;
  }

  /**
   * Returns the Python output file.
   *
   * @return		the file, null if not set
   */
  public File getOutputFile() {
    return m_OutputFile;
  }

  /**
   * Performs some checks.
   *
   * @return		null if successful, otherwise error message
   */
  protected String check() {
    if (!m_JavaHome.exists())
      return "Java home directory does not exist: " + m_JavaHome;
    if (!m_JavaHome.isDirectory())
      return "Java home does not point to a directory: " + m_JavaHome;
    if (System.getProperty("os.name").toLowerCase().contains("windows"))
      m_Javap = new File(m_JavaHome.getAbsolutePath() + File.separator + "bin" + File.separator + "javap.exe");
    else
      m_Javap = new File(m_JavaHome.getAbsolutePath() + File.separator + "bin" + File.separator + "javap");
    if (!m_Javap.exists())
      return "javap binary does not exist: " + m_Javap;

    if (m_Classes.size() == 0)
      return "No classnames provided!";

    return null;
  }

  /**
   * Executes javap and parses the output.
   *
   * @param classname	the class to process
   * @return		the parsed output, null if failed to process
   */
  protected List<MethodDescriptor> getSignatures(String classname) {
    List<MethodDescriptor>	result;
    String[]			cmd;
    ProcessBuilder 		builder;
    CollectingProcessOutput 	output;
    List<String>		lines;
    int				i;
    MethodDescriptor		method;
    String			tmp;

    cmd = new String[]{
      m_Javap.getAbsolutePath(),
      "-s",
      "-public",
      "-cp",
      getClassPath(),
      classname};
    builder = new ProcessBuilder();
    builder.command(cmd);
    output = new CollectingProcessOutput();

    try {
      output.monitor(builder);
    }
    catch (Exception e) {
      System.err.println("Failed to execute command: " + builder.command());
      e.printStackTrace();
      return null;
    }

    result = new ArrayList<>();
    lines  = new ArrayList<>(Arrays.asList(output.getStdOut().split("\n")));

    // clean up
    i = 0;
    while (i < lines.size()) {
      if (lines.get(i).contains("Compiled from")) {
        lines.remove(i);
        continue;
      }
      if (lines.get(i).contains("{") || lines.get(i).contains("}")) {
        lines.remove(i);
        continue;
      }
      if (lines.get(i).trim().isEmpty()) {
        lines.remove(i);
        continue;
      }
      i++;
    }

    // create descriptors
    for (i = 0; i < lines.size(); i += 2) {
      method = new MethodDescriptor();

      tmp = lines.get(i);
      tmp = tmp.replace("public ", "");
      tmp = tmp.replace("final ", "");
      tmp = tmp.replace("static ", "");
      tmp = tmp.replaceAll("\\(.*", "").trim();
      if (!tmp.contains(" "))
        method.name = tmp;  // constructor
      else
        method.name = tmp.split(" ")[1];

      tmp = lines.get(i+1);
      tmp = tmp.replace("descriptor: ", "");
      method.signature = tmp.trim();
      result.add(method);
    }

    return result;
  }

  /**
   * Generates the code.
   *
   * @return		null if successful, otherwise error message
   */
  public String execute() {
    String			result;
    StringBuilder		output;
    List<MethodDescriptor>	methods;

    result = check();

    if (result == null) {
      output = new StringBuilder();

      for (String classname: m_Classes) {
	System.out.println("Processing: " + classname);
	methods = getSignatures(classname);
	if (methods == null)
	  continue;
	System.out.println(methods);
      }
    }
    // TODO

    return result;
  }

  /**
   * Parses the command-line options.
   *
   * @param options	the options
   * @return		true if successful
   */
  public boolean setOptions(String[] options) {
    ArgumentParser parser;
    Namespace ns;

    parser = ArgumentParsers.newArgumentParser(getClass().getName());
    parser.addArgument("--java-home")
      .type(Arguments.fileType().verifyExists().verifyIsDirectory())
      .setDefault((System.getenv("JAVA_HOME") != null ? new File(System.getenv("JAVA_HOME")) : null))
      .dest("javahome")
      .required(false)
      .help("The java home directory of the JDK that includes the javap binary, default is taken from JAVA_HOME environment variable.");
    parser.addArgument("--class-path")
      .dest("classpath")
      .required(true)
      .help("The CLASSPATH to use.");
    parser.addArgument("--class")
      .setDefault(new ArrayList<String>())
      .dest("classes")
      .action(Arguments.append())
      .required(true)
      .help("The classname of the class to generate a wrapper for, can be supplied multiple times.");
    parser.addArgument("--output")
      .type(Arguments.fileType())
      .setDefault(new File("."))
      .required(true)
      .dest("output")
      .help("The Python output file.");

    try {
      ns = parser.parseArgs(options);
    }
    catch (ArgumentParserException e) {
      parser.handleError(e);
      return false;
    }

    setJavaHome(ns.get("javahome"));
    setClassPath(ns.getString("classpath"));
    setClasses(ns.getList("classes"));
    setOutputFile(ns.get("output"));

    return true;
  }

  /**
   * Executes the tool from command-line.
   *
   * @param args	the command-line arguments, use -h/--help for help output
   * @throws Exception	if parsing fails
   */
  public static void main(String[] args) throws Exception {
    Main 	main;
    String	error;

    main = new Main();
    if (main.setOptions(args)) {
      error = main.execute();
      if (error != null) {
	System.err.println(error);
	System.exit(2);
      }
    }
    else {
      System.exit(1);
    }
  }
}