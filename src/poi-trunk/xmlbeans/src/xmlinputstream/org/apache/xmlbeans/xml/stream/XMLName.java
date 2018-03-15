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
 * Encapsulates information about the Qualified Name of an XML event.
 *
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.StartElement
 * @see org.apache.xmlbeans.xml.stream.XMLEvent
 */

public interface XMLName {
  /*
   * returns the uri of the prefix of this name
   * 
   * @return returns the uri, 
   * empty string if the default namespace is bound to empty string 
   * or null if the name does not have a namespace
   */
  public String getNamespaceUri();
  /*
   * returns the localname of the xml name
   * 
   * @return the localname, cannot be null
   */
  public String getLocalName();
  /*
   * returns the prefix of the xml name
   * 
   * @return the prefix of the xml name or null
   */
  public String getPrefix();

  /*
   * returns the prefix of the xml name + ':' + the local name
   * if the prefix is not null
   * returns the local name otherwise
   * 
   * @return the prefix plus the local name
   */
  public String getQualifiedName();
}
