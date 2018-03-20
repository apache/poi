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

package org.apache.xmlbeans.xml.stream;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.xmlbeans.xml.stream.utils.NestedThrowable;

/**
 * The base exception for unexpected input during XML handling
 *
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 */

public class XMLStreamException 
  extends IOException 
  implements NestedThrowable 
{
  protected Throwable th;

  public XMLStreamException() {}

  public XMLStreamException(String msg) { 
    super(msg); 
  }

  public XMLStreamException(Throwable th) {
    this.th = th;
    
  }

  public XMLStreamException(String msg, Throwable th) {
    super(msg);
    this.th = th;
  }

  /**
   * Gets the nested exception.
   *
   * @return                 Nested exception
   */
  public Throwable getNestedException() {
    return getNested();
  }

  //try to do someting useful
  public String getMessage() {
    String msg = super.getMessage();

    if (msg == null && th != null) {
      return th.getMessage();
    } else {
      return msg;
    }
  }


  // =================================================================
  // NestedThrowable implementation.

  /**
   * Gets the nested Throwable.
   *
   * @return                 Nested exception
   */
  public Throwable getNested() {
    return th;
  }

  public String superToString() {
    return super.toString();
  }

  public void superPrintStackTrace(PrintStream ps) {
    super.printStackTrace(ps);
  }

  public void superPrintStackTrace(PrintWriter pw) {
    super.printStackTrace(pw);
  }

  // End NestedThrowable implementation.
  // =================================================================

  /**
   * Prints the exception message and its nested exception message.
   *
   * @return                 String representation of the exception
   */
  public String toString() {
    return NestedThrowable.Util.toString(this);
  }

  /**
   * Prints the stack trace associated with this exception and
   * its nested exception.
   *
   * @param s                 PrintStream
   */
  public void printStackTrace(PrintStream s) { 
    NestedThrowable.Util.printStackTrace(this, s);
  }

  /**
   * Prints the stack trace associated with this exception and
   * its nested exception.
   *
   * @param s                 PrintStream
   */
  public void printStackTrace(PrintWriter w) { 
    NestedThrowable.Util.printStackTrace(this, w);
  }

  /**
   * Prints the stack trace associated with this exception and
   * its nested exception to System.err.
   *
   * @param s                 PrintStream
   */
  public void printStackTrace() {
    printStackTrace(System.err);
  }
}


