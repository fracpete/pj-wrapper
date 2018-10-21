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
 * MethodDescriptor.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.pjwrapper.core;

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
  public SignatureDescriptor signature;

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
