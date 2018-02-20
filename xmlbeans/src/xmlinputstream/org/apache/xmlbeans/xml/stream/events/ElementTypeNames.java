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

package org.apache.xmlbeans.xml.stream.events;

import org.apache.xmlbeans.xml.stream.XMLEvent;

public class ElementTypeNames {
  public static String getName(int val) { 
    switch(val) {
    case XMLEvent.XML_EVENT: return ("XML_EVENT");
    case XMLEvent.START_ELEMENT: return ("START_ELEMENT");
    case XMLEvent.END_ELEMENT: return ("END_ELEMENT");
    case XMLEvent.PROCESSING_INSTRUCTION: return ("PROCESSING_INSTRUCTION");
    case XMLEvent.CHARACTER_DATA: return ("CHARACTER_DATA");
    case XMLEvent.COMMENT: return ("COMMENT");
    case XMLEvent.SPACE: return ("SPACE");
    case XMLEvent.NULL_ELEMENT: return ("NULL_ELEMENT");
    case XMLEvent.START_DOCUMENT: return ("START_DOCUMENT");
    case XMLEvent.END_DOCUMENT: return ("END_DOCUMENT");
    case XMLEvent.START_PREFIX_MAPPING: return ("START_PREFIX_MAPPING");
    case XMLEvent.CHANGE_PREFIX_MAPPING: return ("CHANGE_PREFIX_MAPPING");
    case XMLEvent.END_PREFIX_MAPPING: return ("END_PREFIX_MAPPING");
    case XMLEvent.ENTITY_REFERENCE: return ("ENTITY_REFERENCE");
    default: return "";
    }
  }
  public static int getType(String val) {
    if (val.equals("XML_EVENT")) 
      return XMLEvent.XML_EVENT;
    if (val.equals ("START_ELEMENT")) return 
        XMLEvent.START_ELEMENT; 
    if (val.equals ("END_ELEMENT")) return XMLEvent.END_ELEMENT;
    if (val.equals ("PROCESSING_INSTRUCTION"))
      return XMLEvent.PROCESSING_INSTRUCTION; 
    if (val.equals ("CHARACTER_DATA"))
      return XMLEvent.CHARACTER_DATA; 
    if (val.equals ("COMMENT"))
      return XMLEvent.COMMENT; 
    if (val.equals ("SPACE"))
      return XMLEvent.SPACE; 
    if (val.equals ("NULL_ELEMENT"))
      return XMLEvent.NULL_ELEMENT; 
    if (val.equals ("START_DOCUMENT"))
      return XMLEvent.START_DOCUMENT; 
    if (val.equals ("END_DOCUMENT"))
      return XMLEvent.END_DOCUMENT; 
    if (val.equals ("START_PREFIX_MAPPING"))
      return XMLEvent.START_PREFIX_MAPPING; 
    if (val.equals ("CHANGE_PREFIX_MAPPING"))
      return XMLEvent.CHANGE_PREFIX_MAPPING;
    if (val.equals ("ENTITY_REFERENCE"))
      return XMLEvent.ENTITY_REFERENCE; 
    if (val.equals ("END_PREFIX_MAPPING"))
      return XMLEvent.END_PREFIX_MAPPING;

    return XMLEvent.NULL_ELEMENT;
  }
}
