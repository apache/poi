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
 * This is the base element interface for handling markup events.
 *
 * @since Weblogic XML Input Stream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.CharacterData
 * @see org.apache.xmlbeans.xml.stream.ProcessingInstruction
 * @see org.apache.xmlbeans.xml.stream.StartElement
 * @see org.apache.xmlbeans.xml.stream.EndElement
 * @see org.apache.xmlbeans.xml.stream.CharacterData
 * @see org.apache.xmlbeans.xml.stream.XMLName
 * @see org.apache.xmlbeans.xml.stream.StartDocument
 */

public interface XMLEvent {
  /**
   * A constant which identifies an XMLEvent
   * @see org.apache.xmlbeans.xml.stream.XMLEvent
   */
  public static final int XML_EVENT=0x00000001;
  /**
   * A constant which identifies a StartElement
   * @see org.apache.xmlbeans.xml.stream.StartElement
   */
  public static final int START_ELEMENT=0x00000002;
  /**
   * A constant which identifies an EndElement
   * @see org.apache.xmlbeans.xml.stream.EndElement
   */
  public static final int END_ELEMENT=0x00000004;
  /**
   * A constant which identifies a ProcessingInstruction
   * @see org.apache.xmlbeans.xml.stream.ProcessingInstruction
   */
  public static final int PROCESSING_INSTRUCTION=0x00000008;
  /**
   * A constant which identifies a CharacterData Event
   * @see org.apache.xmlbeans.xml.stream.CharacterData
   */
  public static final int CHARACTER_DATA=0x00000010;
  /**
   * A constant which identifies a Comment
   * @see org.apache.xmlbeans.xml.stream.Comment
   */
  public static final int COMMENT=0x00000020;
  /**
   * A constant which identifies a Space
   * @see org.apache.xmlbeans.xml.stream.Space
   */
  public static final int SPACE=0x00000040;
  /**
   * A constant which identifies a NullElement
   */
  public static final int NULL_ELEMENT=0x00000080;
  /**
   * A constant which identifies a StartDocument
   * @see org.apache.xmlbeans.xml.stream.StartDocument
   */
  public static final int START_DOCUMENT=0x00000100;
  /**
   * A constant which identifies an EndDocument
   * @see org.apache.xmlbeans.xml.stream.EndDocument
   */
  public static final int END_DOCUMENT=0x00000200;
  /**
   * A constant which identifies a StartPrefixMapping
   * @see org.apache.xmlbeans.xml.stream.StartPrefixMapping
   */
  public static final int START_PREFIX_MAPPING=0x00000400;
  /**
   * A constant which identifies a EndPrefixMapping
   * @see org.apache.xmlbeans.xml.stream.EndPrefixMapping
   */
  public static final int END_PREFIX_MAPPING=0x00000800;
  /**
   * A constant which identifies a ChangePrefixMapping
   * @see org.apache.xmlbeans.xml.stream.ChangePrefixMapping
   */
  public static final int CHANGE_PREFIX_MAPPING=0x00001000;
  /**
   * A constant which identifies an EntityReference 
   * @see org.apache.xmlbeans.xml.stream.EntityReference
   */
  public static final int ENTITY_REFERENCE=0x00002000;
  /**
   * Get the event type of the current element,
   * returns an integer so that switch statements
   * can be written on the result
   */
  public int getType();
  /**
   * Get the event type of the current element,
   * returns an integer so that switch statements
   * can be written on the result
   */
  public XMLName getSchemaType();
  /**
   * Get the string value of the type name
   */
  public String getTypeAsString();
  /**
   * Get the XMLName of the current element
   * @see org.apache.xmlbeans.xml.stream.XMLName
   */
  public XMLName getName();

  /**
   * Check if this Element has a name
   */
  public boolean hasName();

  /**
   * Return the location of this Element
   */
  public Location getLocation();

  /**
   * Method access to the elements type
   */
  public boolean isStartElement();
  public boolean isEndElement();
  public boolean isEntityReference();
  public boolean isStartPrefixMapping();
  public boolean isEndPrefixMapping();
  public boolean isChangePrefixMapping();
  public boolean isProcessingInstruction();
  public boolean isCharacterData();
  public boolean isSpace();
  public boolean isNull();
  public boolean isStartDocument();
  public boolean isEndDocument();
}
