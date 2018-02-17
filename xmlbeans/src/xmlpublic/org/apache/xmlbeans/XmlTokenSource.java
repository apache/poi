/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at\\
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans;

import org.apache.xmlbeans.xml.stream.XMLInputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;

/**
 * Represents a holder of XML that can return an {@link XmlCursor}
 * or copy itself to various media such as
 * {@link Writer Writers} or {@link File Files}.
 * Both {@link XmlObject}
 * (and thus all XML Beans) and {@link XmlCursor} are
 * XmlTokenSource implementations.
 * 
 * @see XmlObject
 * @see XmlCursor
 */ 
public interface XmlTokenSource
{
    /**
     * Returns the synchronization object for the document.  If concurrent
     * multithreaded access to a document is required, the access should should
     * be protected by synchronizing on this monitor() object.  There is one
     * monitor per XML document tree.
     */
    Object monitor();
    
    /**
     * Returns the XmlDocumentProperties object for the document this token
     * source is associated with.
     */
    XmlDocumentProperties documentProperties();

    /**
     * Returns a new XML cursor.
     *
     * A cursor provides random access to all the tokens in the XML
     * data, plus the ability to extract strongly-typed XmlObjects
     * for the data. If the data is not read-only, the XML cursor
     * also allows modifications to the data.
     *
     * Using a cursor for the first time typically forces the XML
     * document into memory.
     */
    XmlCursor newCursor();

    /**
     * Returns a new XmlInputStream.
     *
     * The stream starts at the current begin-tag or begin-document
     * position and ends at the matching end-tag or end-document.
     *
     * This is a fail-fast stream, so if the underlying data is changed
     * while the stream is being read, the stream throws a
     * ConcurrentModificationException.
     *
     * Throws an IllegalStateException if the XmlTokenSource is not
     * positioned at begin-tag or begin-document (e.g., if it is at
     * an attribute).
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    XMLInputStream newXMLInputStream();
    
    /**
     * Returns a new XMLStreamReader.
     *
     * The stream starts at the current begin-tag or begin-document
     * position and ends at the matching end-tag or end-document.
     *
     * This is a fail-fast stream, so if the underlying data is changed
     * while the stream is being read, the stream throws a
     * ConcurrentModificationException.
     */
    XMLStreamReader newXMLStreamReader();
    
    /**
     * Returns standard XML text.
     * <p>
     * The text returned represents the document contents starting at
     * the current begin-tag or begin-document and ending at the matching
     * end-tag or end-document. This is same content as newReader, but
     * it is returned as a single string.
     * <p>
     * Throws an IllegalStateException if the XmlTokenSource is not
     * positioned at begin-tag or begin-document (e.g., if it is at
     * an attribute).
     * <p>
     * Note that this method does not produce XML with the XML declaration, 
     * including the encoding information. To save the XML declaration with 
     * the XML, see {@link #save(OutputStream)} or {@link #save(OutputStream, XmlOptions)}.
     */
    String xmlText();

    /**
     * Returns a new stream containing standard XML text, encoded
     * according to the given encoding.
     *
     * The byte stream contains contents starting at the current
     * begin-tag or begin-document and ending at the matching
     * end-tag or end-document.  The specified encoding is used
     * and also emitted in a PI at the beginning of the stream.
     *
     * This is a fail-fast stream, so if the underlying data is changed
     * while the stream is being read, the stream throws a
     * ConcurrentModificationException.
     *
     * Throws an IllegalStateException if the XmlTokenSource is not
     * positioned at begin-tag or begin-document (e.g., if it is at
     * an attribute).
     */
    InputStream newInputStream();

    /**
     * Returns a new character reader containing XML text.
     *
     * The contents of the reader represents the document contents
     * starting at the current begin-tag or begin-document and ending at
     * the matching end-tag or end-document.  No encoding annotation
     * will be made in the text itself.
     *
     * This is a fail-fast reader, so if the underlying data is changed
     * while the reader is being read, the reader throws a
     * ConcurrentModificationException.
     *
     * Throws an IllegalStateException if the XmlTokenSource is not
     * positioned at begin-tag or begin-document (e.g., if it is at
     * an attribute).
     */
    Reader newReader();
    
    /**
     * Returns a W3C DOM Node containing the XML
     * represented by this source.  This is a copy of the XML, it is
     * not a live with the underlying store of this token source.
     * If this is the document node, then a Document is returned, else
     * a DocumentFragment is returned.
     */
    Node newDomNode();

    /**
     * Returns a W3C DOM Node containing the XML represented by this source.
     * This is a live DOM node, not a copy.  Any changes made through this node
     * are immediately reflected in the document associated with this token
     * source.  Depending on the kind of token this XmlTokenSource represents,
     * an appropriate node will be returned.
     */
    Node getDomNode();

    /**
     * Writes the XML represented by this source to the given SAX content and
     * lexical handlers.
     * Note that this method does not save the XML declaration, including the encoding information. 
     * To save the XML declaration with the XML, see {@link #save(OutputStream)}, 
     * {@link #save(OutputStream, XmlOptions)}, {@link #save(File)} or {@link #save(File, XmlOptions)}.
     */
    void save ( ContentHandler ch, LexicalHandler lh ) throws SAXException;
    
    /**
     * Writes the XML represented by this source to the given File.
     * This method will save the XML declaration, including encoding information,
     * with the XML.
     */
    void save ( File file ) throws IOException;
    
    /**
     * Writes the XML represented by this source to the given output stream.
     * This method will save the XML declaration, including encoding information,
     * with the XML.
     */
    void save ( OutputStream os ) throws IOException;

    /**
     * Writes the XML represented by this source to the given output.
     * Note that this method does not save the XML declaration, including the encoding information. 
     * To save the XML declaration with the XML, see {@link #save(OutputStream)}, 
     * {@link #save(OutputStream, XmlOptions)}, {@link #save(File)} or {@link #save(File, XmlOptions)}.
     */
    void save ( Writer w ) throws IOException;

    /**
     * <p>Just like newXMLInputStream() but with any of a number of options. Use the 
     * <em>options</em> parameter to specify the following:</p>
     * 
     * <table>
     * <tr><th>To specify this</th><th>Use this method</th></tr>
     * <tr>
     *  <td>The character encoding to use when converting the character
     *  data in the XML to bytess.</td>
     *  <td>{@link XmlOptions#setCharacterEncoding}</td>
     * </tr>
     * <tr>
     *  <td>Prefix-to-namespace mappings that should be assumed
     *  when saving this XML. This is useful when the resulting
     *  XML will be part of a larger XML document, ensuring that this 
     *  inner document will take advantage of namespaces defined in 
     *  the outer document.</td>
     *  <td>{@link XmlOptions#setSaveImplicitNamespaces}</td>
     * </tr>
     * <tr>
     *  <td>Suggested namespace prefixes to use when saving. Used only
     *  when a namespace attribute needs to be synthesized.</td>
     *  <td>{@link XmlOptions#setSaveSuggestedPrefixes}</td>
     * </tr>
     * <tr>
     *  <td>That namespace attributes should occur first in elements when
     * the XML is saved. By default, they occur last.</td>
     *  <td>{@link XmlOptions#setSaveNamespacesFirst}</td>
     * </tr>
     * <tr>
     *  <td>The XML should be pretty printed when saved. Note that this 
     *  should only be used for debugging.</td>
     *  <td>{@link XmlOptions#setSavePrettyPrint}</td>
     * </tr>
     * <tr>
     *  <td>The number of spaces to use when indenting for pretty printing. 
     *  The default is 2.</td>
     *  <td>{@link XmlOptions#setSavePrettyPrintIndent}</td>
     * </tr>
     * <tr>
     *  <td>The additional number of spaces indented from the left
     *  for pretty printed XML.</td>
     *  <td>{@link XmlOptions#setSavePrettyPrintOffset}</td>
     * </tr>
     * <tr>
     *  <td>To minimize the number of namespace attributes generated for the 
     *  saved XML. Note that this can reduce performance significantly.</td>
     *  <td>{@link XmlOptions#setSaveAggresiveNamespaces}</td>
     * </tr>
     * <tr>
     *  <td>To reduce the size of the saved document
     *  by allowing the use of the default namespace. Note that this can 
     *  potentially change the semantic meaning of the XML if unprefixed QNames are 
     *  present as the value of an attribute or element.</td>
     *  <td>{@link XmlOptions#setUseDefaultNamespace}</td>
     * </tr>
     * <tr>
     *  <td>To filter out processing instructions with the specified target name.</td>
     *  <td>{@link XmlOptions#setSaveFilterProcinst}</td>
     * </tr>
     * <tr>
     *  <td>Change the QName of the synthesized root element when saving. This 
     *  replaces "xml-fragment" with "fragment" in the namespace 
     *  http://www.openuri.org/fragment</td>
     *  <td>{@link XmlOptions#setSaveUseOpenFrag}</td>
     * </tr>
     * <tr>
     *  <td>Saving should begin on the element's contents.</td>
     *  <td>{@link XmlOptions#setSaveInner}</td>
     * </tr>
     * <tr>
     *  <td>Saving should begin on the element, rather than its contents.</td>
     *  <td>{@link XmlOptions#setSaveOuter}</td>
     * </tr>
     * <tr>
     *  <td>To rename the document element, or to specify the document element
     *  for this XML.</td>
     *  <td>{@link XmlOptions#setSaveSyntheticDocumentElement}</td>
     * </tr>
     * </table>
     * 
     * @see XmlOptions
     * 
     * @param options Any of the described options.
     * @return A new validating XMLInputStream.
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    XMLInputStream newXMLInputStream(XmlOptions options);

    /**
     * Just like newXMLInputStream() but with options.
     * Options map may be null.
     * @see XmlOptions
     */
    XMLStreamReader newXMLStreamReader(XmlOptions options);
    
    /**
     * Just like xmlText() but with options.
     * Options map may be null.
     * <p>
     * Note that this method does not produce XML with the XML declaration, 
     * including the encoding information. To save the XML declaration with 
     * the XML, see {@link #save(OutputStream)} or {@link #save(OutputStream, XmlOptions)}.
     *
     * @see XmlOptions
     */
    String xmlText(XmlOptions options);
    
    /**
     *
     * Just like newInputStream(String encoding) but with options.
     * Options map may be null.
     * @see XmlOptions
     */
    InputStream newInputStream(XmlOptions options);
    
    /**
     * Just like newReader() but with options.
     * Options map may be null.
     * @see XmlOptions
     */
    Reader newReader(XmlOptions options);
    
    /**
     * Just like newDomNode() but with options.
     * Options map may be null.
     * @see XmlOptions
     */

    Node newDomNode(XmlOptions options);
    
    /**
     * Writes the XML represented by this source to the given SAX content and
     * lexical handlers.
     * Note that this method does not save the XML declaration, including the encoding information. 
     * To save the XML declaration with the XML, see {@link #save(OutputStream)}, 
     * {@link #save(OutputStream, XmlOptions)}, {@link #save(File)} or {@link #save(File, XmlOptions)}.
     */
    void save ( ContentHandler ch, LexicalHandler lh, XmlOptions options ) throws SAXException;
    
    /**
     * Writes the XML represented by this source to the given File.
     * This method will save the XML declaration, including encoding information,
     * with the XML.
     */
    void save ( File file, XmlOptions options ) throws IOException;
    
    /**
     * Writes the XML represented by this source to the given output stream.
     * This method will save the XML declaration, including encoding information,
     * with the XML.
     */
    void save ( OutputStream os, XmlOptions options ) throws IOException;

    /**
     * Writes the XML represented by this source to the given output.
     * Note that this method does not save the XML declaration, including the encoding information. 
     * To save the XML declaration with the XML, see {@link #save(OutputStream)}, 
     * {@link #save(OutputStream, XmlOptions)}, {@link #save(File)} or {@link #save(File, XmlOptions)}.
     */
    void save ( Writer w, XmlOptions options ) throws IOException;

    /**
     * Prints to stdout the state of the document in which this token source is positioned.
     * This is very implementation specific and may change at any time.  Dump can be useful
     * for debugging purposes.  It is very different from the save methods which produce
     * XML text which only approximates the actual state of the document.
     */

    void dump ( );
}
