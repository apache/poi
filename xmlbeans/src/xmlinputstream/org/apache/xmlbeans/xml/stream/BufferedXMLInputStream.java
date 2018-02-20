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

import java.io.IOException;
/**
 * Extends the XMLInputStream to allow marking and reseting of the stream.
 *
 * @since XMLInputStream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.CharacterData
 * @see org.apache.xmlbeans.xml.stream.ProcessingInstruction
 * @see org.apache.xmlbeans.xml.stream.StartElement
 * @see org.apache.xmlbeans.xml.stream.EndElement
 * @see org.apache.xmlbeans.xml.stream.CharacterData
 * @see org.apache.xmlbeans.xml.stream.XMLName
 * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
 */
public interface BufferedXMLInputStream extends XMLInputStream {
  /**
   * Sets the marks a point to return to in the stream,
   * throws an exception if the stream does not support mark.
   * This is only supported in BufferedStreams
   */
  public void mark() throws XMLStreamException;
  /**
   * Resets the stream to the previous mark.
   * throws an exception if the stream does not support mark;
   */
  public void reset() throws XMLStreamException;
}





