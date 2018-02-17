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

package org.apache.xmlbeans.impl.common;

import org.apache.xmlbeans.xml.stream.XMLName;

public class XmlNameImpl implements XMLName {

  private String namespaceUri=null;
  private String localName=null;
  private String prefix=null;
  private int hash = 0;

  public XmlNameImpl () {}
  public XmlNameImpl (String localName) {
    this.localName = localName;
  }
  public XmlNameImpl (String namespaceUri,String localName) {
    setNamespaceUri(namespaceUri);
    this.localName = localName;
  }
  public XmlNameImpl (String namespaceUri,
               String localName,
               String prefix) {
    setNamespaceUri(namespaceUri);
    this.localName = localName;
    this.prefix = prefix;
  }

  public String getNamespaceUri() { return namespaceUri; }
  public String getLocalName() { return localName; }
  public String getPrefix() { return prefix; }

  public void setNamespaceUri(String namespaceUri) {
    hash = 0;
    if (namespaceUri != null && namespaceUri.equals("")) return;
    this.namespaceUri = namespaceUri; 
  }
  public void setLocalName(String localName) { 
    this.localName = localName; 
    hash = 0;
  }
  public void setPrefix(String prefix) { this.prefix = prefix; }

  public String getQualifiedName() {
    if (prefix != null && prefix.length() > 0)
      return prefix + ":" + localName;
    else
      return localName;
  }
  public String toString() {
    if (getNamespaceUri() != null)
      return "['"+getNamespaceUri()+"']:"+getQualifiedName();
    else 
      return getQualifiedName();
  }

  public final int hashCode() {
    int tmp_hash = hash;
    if (tmp_hash == 0) {
      tmp_hash = 17;
      if (namespaceUri != null) {
	tmp_hash = 37*tmp_hash + namespaceUri.hashCode();
      }
      if (localName != null) {
	tmp_hash = 37*tmp_hash + localName.hashCode();
      }
      hash = tmp_hash;
    }
    return tmp_hash;
  }

  public final boolean equals(Object obj) {
    if (obj == this) return true;

    if (obj instanceof XMLName) {
      final XMLName name= (XMLName) obj;

      final String lname = localName;
      if (!(lname==null ? name.getLocalName()==null : 
	    lname.equals(name.getLocalName())))
	return false;

      final String uri = namespaceUri;
      return (uri==null ? name.getNamespaceUri()==null :
	      uri.equals(name.getNamespaceUri()));
	
    }
    return false;
  }



}
