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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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

    /** whether it is the constructor. */
    public boolean isConstructor;

    /** whether the method is static. */
    public boolean isStatic;

    /** the signature. */
    public String signature;

    /** whether it has arguments. */
    public boolean hasArgs;

    /** whether it has a return value. */
    public boolean hasReturn;

    /** whether the method is part of a property get/set pair. */
    public boolean isProperty;

    /**
     * Returns a string representation of the container.
     *
     * @return		the description
     */
    public String toString() {
      return name + ": " + signature + (isConstructor ? " (constructor)" : "") + (isStatic ? " [static]" : "");
    }
  }

  /**
   * Container for the parsed javap output.
   */
  public class PropertyDescriptor {

    /** the name of the proeprty. */
    public String name;

    /** the read method. */
    public MethodDescriptor read;

    /** the write method. */
    public MethodDescriptor write;

    /**
     * Returns a short description of the property.
     *
     * @return		the description
     */
    public String toString() {
      return name + ": read(" + read.name + "), write(" + write.name + ")";
    }
  }

  /**
   * Container for parsed javap ouput.
   */
  public class ClassDescriptor {

    /** the name of the class. */
    public String name;

    /** the methods. */
    public List<MethodDescriptor> methods = new ArrayList<>();

    /** the properties. */
    public List<PropertyDescriptor> properties = new ArrayList<>();

    /**
     * Returns a description of the class.
     *
     * @return    the description
     */
    public String toString() {
      StringBuilder result;

      result = new StringBuilder();
      result.append(name).append("\n");
      if (methods.size() > 0) {
        result.append("  Methods:\n");
	for (MethodDescriptor method : methods)
	  result.append("    ").append(method.toString()).append("\n");
      }
      if (properties.size() > 0) {
	result.append("  Properties:\n");
	for (PropertyDescriptor property : properties)
	  result.append("    ").append(property.toString()).append("\n");
      }

      return result.toString();
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

  /** whether to append the file. */
  protected boolean m_Append;

  /** the regular expression for skipping method names. */
  protected String m_Skip;

  /** the pattern for matching method names to skip. */
  protected Pattern m_SkipPattern;

  /** whether to output some debugging information. */
  protected boolean m_Debug;

  /** whether to output python-weka-wrapper code. */
  protected boolean m_PWW;

  /**
   * Initalizes the object.
   */
  public Main() {
    m_JavaHome    = (System.getenv("JAVA_HOME") != null ? new File(System.getenv("JAVA_HOME")) : null);
    m_Classes     = new ArrayList<>();
    m_ClassPath   = null;
    m_OutputFile  = null;
    m_Append      = false;
    m_Skip        = null;
    m_SkipPattern = null;
    m_Debug       = false;
    m_PWW         = false;
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
   * Sets whether to append the output file.
   *
   * @param value	true if to append
   */
  public void setAppend(boolean value) {
    m_Append = value;
  }

  /**
   * Returns whether to append the output file
   *
   * @return		true if to append
   */
  public boolean getAppend() {
    return m_Append;
  }

  /**
   * Sets the regular expression for methods to skip.
   *
   * @param value	the expression
   */
  public void setSkip(String value) {
    m_Skip = value;
  }

  /**
   * Returns the regular expression for methods to skip.
   *
   * @return		the expression
   */
  public String getSkip() {
    return m_Skip;
  }

  /**
   * Sets whether to turn debug information on.
   *
   * @param value	true for debug info
   */
  public void setDebug(boolean value) {
    m_Debug = value;
  }

  /**
   * Returns whether debug information is on.
   *
   * @return		true if debug is on
   */
  public boolean getDebug() {
    return m_Debug;
  }

  /**
   * Sets whether to output python-weka-wrapper code.
   *
   * @param value	true for pww code
   */
  public void setPWW(boolean value) {
    m_PWW = value;
  }

  /**
   * Returns whether to output python-weka-wrapper code.
   *
   * @return		true for pww code
   */
  public boolean getPWW() {
    return m_PWW;
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

    m_SkipPattern = null;
    if (m_Skip != null) {
      try {
	m_SkipPattern = Pattern.compile(m_Skip);
      }
      catch (Exception e) {
	return "Invalid regular expression for skipping method names: " + m_Skip;
      }
    }

    return null;
  }

  /**
   * Executes javap and parses the output.
   *
   * @param classname	the class to process
   * @return		the parsed output, null if failed to process
   */
  protected ClassDescriptor parseClass(String classname) {
    ClassDescriptor		result;
    String[]			cmd;
    ProcessBuilder 		builder;
    CollectingProcessOutput 	output;
    List<String>		lines;
    int				i;
    MethodDescriptor		method;
    String			tmp;
    PropertyDescriptor		property;

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

    result = new ClassDescriptor();
    result.name = classname;
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

      // name
      tmp = lines.get(i);
      method.isStatic = tmp.contains(" static ");
      tmp = tmp.replace("public ", "");
      tmp = tmp.replace("final ", "");
      tmp = tmp.replace("static ", "");
      tmp = tmp.replaceAll("\\(.*", "").trim();
      if (!tmp.contains(" ")) {
        method.name        = tmp;
        method.isConstructor = true;
      }
      else {
        method.name = tmp.split(" ")[1];
      }

      // signature
      tmp = lines.get(i+1);
      tmp = tmp.replace("descriptor: ", "");
      method.signature = tmp.trim();
      method.hasArgs   = !method.signature.contains("()");
      method.hasReturn = !method.signature.endsWith("V");

      if ((m_SkipPattern != null) && m_SkipPattern.matcher(method.name).matches())
        continue;
      result.methods.add(method);
    }

    // determine properties
    for (MethodDescriptor m: result.methods) {
      if (!m.name.startsWith("set") || !m.signature.endsWith("V"))
        continue;
      tmp = "g" + m.name.substring(1);
      for (MethodDescriptor m2: result.methods) {
        if (m2.name.equals(tmp)) {
          property = new PropertyDescriptor();
          property.name  = m.name.substring(3, 4).toLowerCase() + m.name.substring(4);
          property.write = m;
          property.read  = m2;
          m.isProperty   = true;
          m2.isProperty  = true;
          result.properties.add(property);
	}
      }
    }

    return result;
  }

  /**
   * Turns a Java camel case into a Python lower_underscore name.
   *
   * @param javaname	the name to convert
   * @return		the converted name
   */
  protected String pythonName(String javaname) {
    StringBuilder	result;
    char		c;
    int			i;

    result = new StringBuilder();

    for (i = 0; i < javaname.length(); i++) {
      c = javaname.charAt(i);
      if ((c >= 'A') && (c <= 'Z')) {
        result.append("_");
        c = Character.toLowerCase(c);
      }
      result.append(c);
    }

    return result.toString();
  }

  /**
   * Generates code for the parsed class.
   *
   * @param cls		the class to generate code for
   * @param code	for storing the code
   * @return		null if successful, otherwise error message
   */
  protected String generateCode(ClassDescriptor cls, StringBuilder code) {
    // imports
    if (m_PWW && (code.length() == 0)) {
      code.append("from weka.core.classes import JavaObject\n");
      code.append("\n");
      code.append("\n");
    }

    // class
    code.append("class ").append(cls.name.replaceAll(".*\\.", ""));
    if (m_PWW)
      code.append("(JavaObject):\n");
    else
      code.append("(Object):\n");

    // comments
    code.append("    \"\"\"\n");
    code.append("    classname: " + cls.name + "\n");
    code.append("    classpath: " + m_ClassPath + "\n");
    code.append("    \"\"\"\n");
    code.append("    \n");

    // constructor
    if (m_PWW) {
      code.append("    def __init__(self, jobject):\n");
      code.append("        super(JavaObject, jobject)\n");
      code.append("        \n");
    }
    else {
      code.append("    def __init__(self, jobject):\n");
      code.append("        self.jobject = jobject\n");
      code.append("        \n");
    }

    // iterate methods
    for (MethodDescriptor method: cls.methods) {
      if (method.isConstructor || method.isProperty || method.isStatic)
        continue;

      code.append("    def ").append(pythonName(method.name)).append("(self):\n");  // TODO parameters
      code.append("        \"\"\"\n");
      code.append("        method: ").append(method.name).append(method.signature).append("\n");
      code.append("        \"\"\"\n");
      code.append("        pass\n");  // TODO set
      code.append("        \n");
    }

    // iterate properties
    for (PropertyDescriptor property: cls.properties) {
      code.append("    @property\n");
      code.append("    def ").append(pythonName(property.name)).append("(self):\n");
      code.append("        \"\"\"\n");
      code.append("        method: ").append(property.read.name).append(property.read.signature).append("\n");
      code.append("        \"\"\"\n");
      code.append("        return None\n");  // TODO return
      code.append("        \n");

      code.append("    @").append(pythonName(property.name)).append(".setter\n");
      code.append("    def ").append(pythonName(property.name)).append("(self, value):\n");
      code.append("        \"\"\"\n");
      code.append("        method: ").append(property.write.name).append(property.write.signature).append("\n");
      code.append("        \"\"\"\n");
      code.append("        pass\n");  // TODO set
      code.append("        \n");
    }

    return null;
  }

  /**
   * Outputs the generated code.
   *
   * @param code	the code to output
   * @return		null if successful, otherwise error message
   */
  protected String outputCode(StringBuilder code) {
    String		msg;
    BufferedWriter 	bwriter;
    FileWriter		fwriter;

    if (m_OutputFile == null) {
      System.out.println(code.toString());
      return null;
    }
    else {
      if (getDebug())
        System.err.println("Writing to '" + m_OutputFile + " (append=" + m_Append + ")");
      fwriter = null;
      bwriter = null;
      try {
        fwriter = new FileWriter(m_OutputFile, m_Append);
        bwriter = new BufferedWriter(fwriter);
        bwriter.write(code.toString());
        bwriter.newLine();
        bwriter.flush();
        fwriter.flush();
	return null;
      }
      catch (Exception e) {
        msg = "Failed to write to '" + m_OutputFile + "' (append=" + m_Append + ")!";
        System.err.println(msg);
        e.printStackTrace();
        return msg + "\n" + e;
      }
      finally {
        if (bwriter != null) {
	  try {
	    bwriter.close();
	  }
	  catch (Exception e) {
	    // ignored
	  }
	}
        if (fwriter != null) {
          try {
            fwriter.close();
	  }
	  catch (Exception e) {
            // ignored
	  }
	}
      }
    }
  }

  /**
   * Generates the code.
   *
   * @return		null if successful, otherwise error message
   */
  public String execute() {
    String		result;
    StringBuilder 	code;
    ClassDescriptor 	cls;

    result = check();

    if (result == null) {
      code = new StringBuilder();

      for (String classname : m_Classes) {
	if (getDebug())
	  System.err.println("Processing: " + classname);
	cls = parseClass(classname);
	if (cls == null)
	  continue;
	if (getDebug())
	  System.err.println(cls);

	result = generateCode(cls, code);
	if (result != null)
	  break;
      }

      if (result == null)
	result = outputCode(code);
    }

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
      .required(false)
      .dest("output")
      .help("The Python file to write to; outputs to stdout if not specified.");
    parser.addArgument("--append")
      .dest("append")
      .required(false)
      .action(Arguments.storeTrue())
      .help("If to append to the output file.");
    parser.addArgument("--skip")
      .dest("skip")
      .required(false)
      .help("The regular expression for method names to skip.");
    parser.addArgument("--debug")
      .dest("debug")
      .required(false)
      .action(Arguments.storeTrue())
      .help("For outputting some debugging information.");
    parser.addArgument("--pww")
      .dest("pww")
      .required(false)
      .action(Arguments.storeTrue())
      .help("For outputting python-weka-wrapper code.");

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
    setAppend(ns.get("append"));
    setSkip(ns.get("skip"));
    setDebug(ns.get("debug"));
    setPWW(ns.get("pww"));

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
