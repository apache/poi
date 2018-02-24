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
 * An interface that contains information about an attribute
 *
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.XMLName
 * @see org.apache.xmlbeans.xml.stream.Attribute
 */
public interface Attribute {
  /**
   * Get the XMLName of the current attribute.
   * @see org.apache.xmlbeans.xml.stream.XMLName
   */
  public XMLName getName();

  /**
   * Get the attribute value of the current attribute.
   */
  public String getValue();

  /**
   * Get the attribute type of the current attribute
   */
  public String getType();

  /**
   * Get the attribute type of the current attribute
   */
  public XMLName getSchemaType();
}
