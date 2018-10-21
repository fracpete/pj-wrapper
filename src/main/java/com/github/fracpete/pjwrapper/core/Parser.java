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
 * Parser.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.pjwrapper.core;

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * For parsing classes.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Parser {

  /** the javap executable. */
  protected File m_Javap;

  /** the classpath. */
  protected String m_Classpath;

  /** the skip pattern. */
  protected Pattern m_SkipPattern;

  /**
   * Initializes the parser.
   *
   * @param javap	the javap executable
   * @param classpath	the classpath to use
   * @param skipPattern	the pattern for skipping methods
   */
  public Parser(File javap, String classpath, Pattern skipPattern) {
    m_Javap       = javap;
    m_Classpath   = classpath;
    m_SkipPattern = skipPattern;
  }

  /**
   * Parses the class.
   *
   * @param classname	the classname
   * @return		the parsed descriptor, or null if failed
   */
  public ClassDescriptor parse(String classname) {
    ClassDescriptor		result;
    String[]			cmd;
    ProcessBuilder 		builder;
    CollectingProcessOutput output;
    List<String> lines;
    int				i;
    SignatureDescriptor signature;
    MethodDescriptor		method;
    String			tmp;
    StringBuilder 		params;
    PropertyDescriptor property;
    int				n;

    cmd = new String[]{
      m_Javap.getAbsolutePath(),
      "-s",
      "-public",
      "-cp",
      m_Classpath,
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
      signature = new SignatureDescriptor();
      signature.raw = tmp.trim();
      // arguments
      params = new StringBuilder(signature.raw.substring(1, signature.raw.indexOf(')')));
      n = 0;
      while (params.length() > 0) {
        if (params.charAt(n) == '[') {
          n++;
          continue;
        }
        switch (params.charAt(n)) {
          case 'L':  // classname
	    params.deleteCharAt(n);
            signature.argTypes.add(params.substring(0, params.indexOf(";")));
            params.delete(0, params.indexOf(";") + 1);
            n = 0;
            break;
          default:
            signature.argTypes.add(params.substring(0, 1));
            params.deleteCharAt(0);
            n = 0;
            break;
        }
      }
      // return type
      signature.returnType = signature.raw.replaceAll(".*\\)", "");
      if (signature.returnType.equals("V")) {
	signature.returnType = null;
      }
      else if (signature.returnType.contains(";")) {
	tmp = signature.returnType;
        if (signature.returnType.indexOf('L') > 0)
	  signature.returnType = tmp.substring(0, tmp.indexOf('L')) + tmp.substring(tmp.indexOf('L') + 1, tmp.length() - 1);
	else
	  signature.returnType = tmp.substring(tmp.indexOf('L') + 1, tmp.length() - 1);
      }
      method.signature = signature;

      if ((m_SkipPattern != null) && m_SkipPattern.matcher(method.name).matches())
        continue;
      result.methods.add(method);
    }

    // determine properties
    for (MethodDescriptor m: result.methods) {
      if (!m.name.startsWith("set") || !m.signature.raw.endsWith("V"))
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
}
