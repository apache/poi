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

import javax.xml.stream.XMLStreamReader;

/**
 * debugging utilities for XmlStreamReader
 */
public final class XmlStreamUtils
{
  public static String printEvent(XMLStreamReader xmlr)
  {
    StringBuffer b = new StringBuffer();
    b.append("EVENT:[" + xmlr.getLocation().getLineNumber() + "][" +
             xmlr.getLocation().getColumnNumber() + "] ");
    b.append(getName(xmlr.getEventType()));
    b.append(" [");
    switch (xmlr.getEventType()) {
      case XMLStreamReader.START_ELEMENT:
        b.append("<");
        printName(xmlr, b);
        for (int i = 0; i < xmlr.getNamespaceCount(); i++) {
          b.append(" ");
          String n = xmlr.getNamespacePrefix(i);
          if ("xmlns".equals(n)) {
            b.append("xmlns=\"" + xmlr.getNamespaceURI(i) + "\"");
          } else {
            b.append("xmlns:" + n);
            b.append("=\"");
            b.append(xmlr.getNamespaceURI(i));
            b.append("\"");
          }
        }

        for (int i = 0; i < xmlr.getAttributeCount(); i++) {
          b.append(" ");
          printName(xmlr.getAttributePrefix(i),
                    xmlr.getAttributeNamespace(i),
                    xmlr.getAttributeLocalName(i),
                    b);
          b.append("=\"");
          b.append(xmlr.getAttributeValue(i));
          b.append("\"");
        }

        b.append(">");
        break;
      case XMLStreamReader.END_ELEMENT:
        b.append("</");
        printName(xmlr, b);
        for (int i = 0; i < xmlr.getNamespaceCount(); i++) {
          b.append(" ");
          String n = xmlr.getNamespacePrefix(i);
          if ("xmlns".equals(n)) {
            b.append("xmlns=\"" + xmlr.getNamespaceURI(i) + "\"");
          } else {
            b.append("xmlns:" + n);
            b.append("=\"");
            b.append(xmlr.getNamespaceURI(i));
            b.append("\"");
          }
        }
        b.append(">");
        break;
      case XMLStreamReader.SPACE:
      case XMLStreamReader.CHARACTERS:
        //b.append(xmlr.getText());
        int start = xmlr.getTextStart();
        int length = xmlr.getTextLength();
        b.append(new String(xmlr.getTextCharacters(),
                            start,
                            length));
        break;
      case XMLStreamReader.PROCESSING_INSTRUCTION:
        String target = xmlr.getPITarget();
        if (target == null) target = "";
        String data = xmlr.getPIData();
        if (data == null) data = "";
        b.append("<?");
        b.append(target + " " + data);
        b.append("?>");
        break;
      case XMLStreamReader.CDATA:
        b.append("<![CDATA[");
        if (xmlr.hasText())
          b.append(xmlr.getText());
        b.append("]]>");
        break;

      case XMLStreamReader.COMMENT:
        b.append("<!--");
        if (xmlr.hasText())
          b.append(xmlr.getText());
        b.append("-->");
        break;
      case XMLStreamReader.ENTITY_REFERENCE:
        b.append(xmlr.getLocalName() + "=");
        if (xmlr.hasText())
          b.append("[" + xmlr.getText() + "]");
        break;
      case XMLStreamReader.START_DOCUMENT:
        b.append("<?xml");
        b.append(" version='" + xmlr.getVersion() + "'");
        b.append(" encoding='" + xmlr.getCharacterEncodingScheme() + "'");
        if (xmlr.isStandalone())
          b.append(" standalone='yes'");
        else
          b.append(" standalone='no'");
        b.append("?>");
        break;

    }
    b.append("]");
    return b.toString();
  }


  private static void printName(String prefix,
                                String uri,
                                String localName,
                                StringBuffer b)
  {
    if (uri != null && !("".equals(uri))) b.append("['" + uri + "']:");
    if (prefix != null && !("".equals(prefix))) b.append(prefix + ":");
    if (localName != null) b.append(localName);
  }

  private static void printName(XMLStreamReader xmlr, StringBuffer b)
  {
    if (xmlr.hasName()) {
      String prefix = xmlr.getPrefix();
      String uri = xmlr.getNamespaceURI();
      String localName = xmlr.getLocalName();
      printName(prefix, uri, localName, b);
    }
  }

  public static String getName(int eventType)
  {
    switch (eventType) {
      case XMLStreamReader.START_ELEMENT:
        return "START_ELEMENT";
      case XMLStreamReader.END_ELEMENT:
        return "END_ELEMENT";
      case XMLStreamReader.PROCESSING_INSTRUCTION:
        return "PROCESSING_INSTRUCTION";
      case XMLStreamReader.CHARACTERS:
        return "CHARACTERS";
      case XMLStreamReader.SPACE:
        return "SPACE";
      case XMLStreamReader.COMMENT:
        return "COMMENT";
      case XMLStreamReader.START_DOCUMENT:
        return "START_DOCUMENT";
      case XMLStreamReader.END_DOCUMENT:
        return "END_DOCUMENT";
      case XMLStreamReader.ENTITY_REFERENCE:
        return "ENTITY_REFERENCE";
      case XMLStreamReader.ATTRIBUTE:
        return "ATTRIBUTE";
      case XMLStreamReader.DTD:
        return "DTD";
      case XMLStreamReader.CDATA:
        return "CDATA";
      case XMLStreamReader.NAMESPACE:
        return "NAMESPACE";
    }
    return "UNKNOWN_EVENT_TYPE";
  }

  public static int getType(String val)
  {
    if (val.equals("START_ELEMENT"))
      return XMLStreamReader.START_ELEMENT;
    if (val.equals("SPACE"))
      return XMLStreamReader.SPACE;
    if (val.equals("END_ELEMENT"))
      return XMLStreamReader.END_ELEMENT;
    if (val.equals("PROCESSING_INSTRUCTION"))
      return XMLStreamReader.PROCESSING_INSTRUCTION;
    if (val.equals("CHARACTERS"))
      return XMLStreamReader.CHARACTERS;
    if (val.equals("COMMENT"))
      return XMLStreamReader.COMMENT;
    if (val.equals("START_DOCUMENT"))
      return XMLStreamReader.START_DOCUMENT;
    if (val.equals("END_DOCUMENT"))
      return XMLStreamReader.END_DOCUMENT;
    if (val.equals("ATTRIBUTE"))
      return XMLStreamReader.ATTRIBUTE;
    if (val.equals("DTD"))
      return XMLStreamReader.DTD;
    if (val.equals("CDATA"))
      return XMLStreamReader.CDATA;
    if (val.equals("NAMESPACE"))
      return XMLStreamReader.NAMESPACE;
    return -1;
  }


}
