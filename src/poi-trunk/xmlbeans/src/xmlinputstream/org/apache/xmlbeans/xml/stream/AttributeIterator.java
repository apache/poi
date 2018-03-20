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

/**
 * This interface specifies methods for iterating over attributes.
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.Attribute
*/

public interface AttributeIterator {
  /**
   * Get the next Attribute on the stream
   * @see org.apache.xmlbeans.xml.stream.Attribute
   */
  public Attribute next();

  /**
   * Check if there are any attributes on the stream
   */
  public boolean hasNext();

  /**
   * Return the next element on the stream without shifting it
   */
  public Attribute peek();


  /**
   * Skip the next element on the stream
   */
  public void skip();
}

