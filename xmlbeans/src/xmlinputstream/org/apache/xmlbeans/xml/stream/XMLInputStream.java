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
 *
 * This is the top level interface for iterating over XML Events
 * in an XML document.
 *
 * <p> Difference from SAX </p>
 * <p> An event stream can be thought of encapsulating SAX
 * events. It applies an iterator/pull metaphor to the parser 
 * allowing procedural, stream-based, handling of input XML rather than
 * having to write chained event handlers to handle complex XML
 * documents.
 * <p> Difference from DOM </p>
 * <p>The pull metaphor allows single-pass, stream-based  parsing of the document rather
 * than tree based manipulation.</p>
 *
 * @since XMLInputStream 1.0
 * @version 1.0
 * @see org.apache.xmlbeans.xml.stream.XMLEvent
 * @see org.apache.xmlbeans.xml.stream.CharacterData
 * @see org.apache.xmlbeans.xml.stream.ProcessingInstruction
 * @see org.apache.xmlbeans.xml.stream.StartElement
 * @see org.apache.xmlbeans.xml.stream.EndElement
 * @see org.apache.xmlbeans.xml.stream.CharacterData
 * @see org.apache.xmlbeans.xml.stream.XMLName
 * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
 */

// REVIEW pdapkus@bea.com 2002-Sep-13 -- while I like the convenience
// of many of these methods, it strikes me that many of these methods
// could implemented as static methods in a utility class.  the down
// side to having them in this base interface is that it makes the
// contract for implementers unnecessarily steap and results in
// duplicated code in methods that can't extend one of the common base
// classes.  

public interface XMLInputStream {
  /**
   * Get the next XMLEvent on the stream
   * @see org.apache.xmlbeans.xml.stream.XMLEvent
   */
  public XMLEvent next() throws XMLStreamException;
  /**
   * Check if there are more events to pull of the stream
   * @see org.apache.xmlbeans.xml.stream.XMLEvent
   */
  public boolean hasNext() throws XMLStreamException;
  /**
   * Skip the next stream event
   */
  public void skip() throws XMLStreamException;
  /**
   * Skips the entire next start tag / end tag pair.
   */
  public void skipElement() throws XMLStreamException;
  /**
   * Check the next XMLEvent without reading it from the stream.
   * Returns null if the stream is at EOF or has no more XMLEvents.
   * @see org.apache.xmlbeans.xml.stream.XMLEvent
   */
  public XMLEvent peek() throws XMLStreamException;
  /**
   * Position the stream at the next XMLEvent of this type.  The method
   * returns true if the stream contains another XMLEvent of this type
   * and false otherwise.
   * @param eventType An integer code that indicates the element type.
   * @see org.apache.xmlbeans.xml.stream.XMLEvent
   */
  public boolean skip(int eventType) throws XMLStreamException;
  /**
   * Position the stream at the next element of this name.  The method
   * returns true if the stream contains another element with this name
   * and false otherwise.  Skip is a forward operator only.  It does
   * not look backward in the stream.
   * @param name An object that defines an XML name.
   * If the XMLName.getNameSpaceName() method on the XMLName argument returns
   * null the XMLName will match just the local name.  Prefixes are
   * not checked for equality.
   * @see org.apache.xmlbeans.xml.stream.XMLName
   */
  public boolean skip(XMLName name) throws XMLStreamException;
  /**
   * Position the stream at the next element of this name and this type.
   * The method returns true if the stream contains another element 
   * with this name of this type and false otherwise.  
   * @param name An object that defines an XML name.
   * If the XMLName.getNameSpaceName() method on the XMLName argument returns
   * null the XMLName will match just the local name.  Prefixes are
   * not checked for equality.
   * @param eventType An integer code that indicates the element type.
   * @see org.apache.xmlbeans.xml.stream.XMLEvent
   * @see org.apache.xmlbeans.xml.stream.XMLName
   */
  public boolean skip(XMLName name, int eventType) throws XMLStreamException;
  /**
   * getSubStream() returns a stream which points to the entire next element in the
   * current stream.  For example: take a document that has a root node A, where the children
   * of A are B, C, and D. If the stream is pointing to the start element of A, getSubStream() will return 
   * A, B, C and D including the start element of A and the end element of A.  The position of the parent
   * stream is not changed and the events read by the substream are written back to its parent.   
   */
  public XMLInputStream getSubStream() throws XMLStreamException;
  /**
   * Closes this input stream and releases any system resources associated with the stream.
   */
  public void close() throws XMLStreamException;

  /**
   * Returns the reference resolver that was set for this stream,
   * returns null if no ReferenceResolver has been set.
   * @see org.apache.xmlbeans.xml.stream.ReferenceResolver
   */
  public ReferenceResolver getReferenceResolver();
  /**
   * Provides a way to set the ReferenceResolver of the stream,
   * this is mostly needed for handle references to other parts of the
   * document.
   * @see org.apache.xmlbeans.xml.stream.ReferenceResolver
   */
  public void setReferenceResolver(ReferenceResolver resolver);
}





