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
 * This event signals that a prefix mapping has begun scope
 *
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.EndPrefixMapping
 * @see org.apache.xmlbeans.xml.stream.ChangePrefixMapping
 */

public interface StartPrefixMapping extends XMLEvent {
  /* 
   * Returns the uri of the prefix that is in scope for the following 
   * element
   * @return String value of the uri
   */
  public String getNamespaceUri();
  /* 
   * Returns the prefix that is now in scope
   * @return String value of the prefix
   */
  public String getPrefix();
}
