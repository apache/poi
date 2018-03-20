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

import java.util.Map;
/**
 * The StartElement interface provides access to information about
 * start elements
 *
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.AttributeIterator
 */

public interface StartElement extends XMLEvent {
  /**
   * Returns an AttributeIterator of non-namespace declared attributes
   */
  public AttributeIterator getAttributes();
  /**
   * Returns an AttributeIterator of namespaces declared in this element
   */
  public AttributeIterator getNamespaces();
  /**
   * Returns the union of declared attributes and namespaces
   */
  public AttributeIterator getAttributesAndNamespaces();
  /**
   * Returns the attribute referred to by this name
   */
  public Attribute getAttributeByName(XMLName name);
  /**
   * Gets the value that the prefix is bound to in the
   * context of this element.  Returns null if 
   * the prefix is not bound in this context
   */
  public String getNamespaceUri(String prefix);
  /**
   * Gets a java.util.Map from prefixes to URIs in scope for this
   * element.
   */
  public Map getNamespaceMap();
}
