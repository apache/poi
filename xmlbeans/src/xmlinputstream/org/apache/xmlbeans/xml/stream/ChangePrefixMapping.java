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
 * This event signals that a prefix mapping has changed from
 * OldNamespaceName to the NewNamespaceName
 *
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.StartPrefixMapping
 * @see org.apache.xmlbeans.xml.stream.EndPrefixMapping
 */
public interface ChangePrefixMapping extends XMLEvent {
  /*
   * returns the uri that the prefix was bound to
   * @return String value of the uri
   */
  public String getOldNamespaceUri();
  /*
   * returns the new uri that the prefix is bound to
   * @return String value of the uri
   */
  public String getNewNamespaceUri();
  /*
   * returns the prefix that is bound
   * @return String value of the prefix
   */
  public String getPrefix();
}
