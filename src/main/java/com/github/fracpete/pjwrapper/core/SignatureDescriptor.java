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
 * SignatureDescriptor.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package com.github.fracpete.pjwrapper.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for signature details.
 */
public class SignatureDescriptor {

  /** the raw signature. */
  public String raw;

  /** the argument types. */
  public List<String> argTypes = new ArrayList<>();

  /** the return type. */
  public String returnType;

  /**
   * Returns just the raw signature.
   *
   * @return        the signature
   */
  public String toString() {
    return "(" + argTypes + ")" + (returnType == null ? "V" : returnType);
  }
}
