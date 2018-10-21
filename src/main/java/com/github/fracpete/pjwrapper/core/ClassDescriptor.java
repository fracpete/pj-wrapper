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
 * ClassDescriptor.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.pjwrapper.core;

import java.util.ArrayList;
import java.util.List;

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
