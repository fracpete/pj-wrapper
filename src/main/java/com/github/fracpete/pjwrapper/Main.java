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

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates Python 3 wrapper code for a Java class.
 *
 * @author FracPete (fracpete at gmail dot com)
 */
public class Main {

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
   * Generates the code.
   *
   * @return		null if successful, otherwise error message
   */
  public String execute() {
    String result;

    result = check();

    // TODO

    return result;
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
