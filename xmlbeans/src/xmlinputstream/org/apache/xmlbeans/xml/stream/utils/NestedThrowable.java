/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.xml.stream.utils;

/**
 * The interface implemented by NestedException, NestedError, and
 * NestedRuntimeException largely so Util can provide a standard
 * implementation of toString() and printStackTrace()
 *
 * @deprecated use JDK 1.4 style nested throwables where possible.
 *
 */

import java.io.PrintWriter;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

public interface NestedThrowable {

  /** Get the nested Throwable. */
  Throwable getNested();

  /** Call super.toString(). [Kludge but necessary.] */
  String superToString();

  /** Call super.printStackTrace(). [Kludge but necessary.] */
  void superPrintStackTrace(PrintStream ps);

  /** Call super.printStackTrace(). [Kludge but necessary.] */
  void superPrintStackTrace(PrintWriter po);

  static class Util {

    private static String EOL = System.getProperty("line.separator");

    /**
     * Prints the exception message and its nested exception message.
     *
     * @return                 String representation of the exception
     */
    public static String toString(NestedThrowable nt) {
      Throwable nested = nt.getNested();
      if (nested == null) {
        return nt.superToString();
      } else {
        return nt.superToString() + " - with nested exception:" + 
          EOL + "[" + nestedToString(nested) + "]";
      }
    }

    private static String nestedToString(Throwable nested) {
      if (nested instanceof InvocationTargetException) {
        InvocationTargetException ite = (InvocationTargetException) nested;
        return nested.toString() + " - with target exception:" + 
          EOL + "[" + ite.getTargetException().toString() +
          "]";
      }
      return nested.toString();
    }

    /**
     * Prints the stack trace associated with this exception and
     * its nested exception.
     *
     * @param s                 PrintStream
     */
    public static void printStackTrace(NestedThrowable nt, PrintStream s) { 
      Throwable nested = nt.getNested();
      if (nested != null) {
        nested.printStackTrace(s);
        s.println("--------------- nested within: ------------------");
      }
      nt.superPrintStackTrace(s);
    }

    /**
     * Prints the stack trace associated with this exception and
     * its nested exception.
     *
     * @param w                 PrintWriter
     */
    public static void printStackTrace(NestedThrowable nt, PrintWriter w) { 
      Throwable nested = nt.getNested();
      if (nested != null) {
        nested.printStackTrace(w);
        w.println("--------------- nested within: ------------------");
      }
      nt.superPrintStackTrace(w);
    }
  }

}
