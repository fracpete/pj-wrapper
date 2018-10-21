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
 * Generator.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.pjwrapper.core;

/**
 * Generates Python code from a parsed class descriptor.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Generator {

  /** whether to generate python-weka-wrapper code. */
  protected boolean m_PWW;

  /**
   * Initializes the generator.
   *
   * @param pww		whether to generate python-weka-wrapper code
   */
  public Generator(boolean pww) {
    m_PWW = pww;
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
  public String generate(ClassDescriptor cls, StringBuilder code) {
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
}
