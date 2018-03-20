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
 * An interface for the start document event
 *
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.XMLEvent
*/
public interface StartDocument extends XMLEvent {
  /* 
   * Returns the system id of the stream
   * @return the system id, defaults to ""
   */
  public String getSystemId();
  /* 
   * Returns the encoding style of this XML stream
   * @return the character encoding, defaults to "UTF-8"
   */
  public String getCharacterEncodingScheme();
  /* 
   * Returns if this XML is standalone
   * @return the version of XML, defaults to "yes"
   */
  public boolean isStandalone();
  /* 
   * Returns the version of XML of this XML stream
   * @return the version of XML, defaults to "1.0"
   */
  public String getVersion();
}
