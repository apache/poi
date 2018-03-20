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

package org.apache.xmlbeans;

import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;

import org.w3c.dom.Node;
import org.w3c.dom.DOMImplementation;

import java.io.InputStream;
import java.io.File;
import java.io.Reader;
import java.io.IOException;

/**
 * Corresponds to the XML Schema
 * <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#key-urType">xs:anyType</a>,
 * the base type for all XML Beans.
 * <p>
 * Since all XML Schema types are translated into corresponding XML Bean
 * classes, and all Schema type derivation corresponds to Java class
 * inheritance, the fact that all Schema types derive from xs:anyType means
 * that all XML Bean classes derive from XmlObject.
 * <p>
 * On this base class you will find a number of common facilities that
 * all XML Bean classes provide:
 * <p>
 * <ul>
 * <li>Every XML Bean class has an inner Factory class for creating and parsing
 *     instances, including XmlObject. Use {@link XmlObject.Factory} itself
 *     to produce untyped XML trees or XML trees that implement specific
 *     subtypes of XmlObject depending on a recognized root document element.
 *     If you depend on the automatic type inference, you will want to understand
 *     the type inference issues described below.
 * <li>To write out an accurate XML file for the XML tree under this
 *     XmlObject, use one of the {@link #save} methods,
 *     or {@link #newInputStream} or {@link #newReader}.
 *     Use {@link #toString} to produce a pretty-printed representation of the
 *     XML subtree under this XML Object.  If you save interior portions of
 *     an XML document, you will want to understand the inner contents
 *     versus outer container issues described below.
 * <li>It is also simple to copy an XmlObject instance to or from a standard
 *     DOM tree or SAX stream.  Use {@link XmlObject.Factory#parse(Node)},
 *     for example, to load from DOM; use {@link XmlObject.Factory#newXmlSaxHandler}
 *     to load from SAX; use {@link #newDomNode()} to save to DOM; and use 
 *     {@link #save(org.xml.sax.ContentHandler, org.xml.sax.ext.LexicalHandler)}
 *     to save to SAX.
 * <li>Use {@link #validate} to validate the subtree of XML under this
 *     XML Object.  If you wish to get information about the location
 *     and reason for validation errors, see {@link XmlOptions#setErrorListener},
 *     and use {@link #validate(XmlOptions)}.
 * <li>Use {@link #newCursor} to access the full XML infoset, for example,
 *     if you need to determine interleaved element order or manipulate
 *     annotations, comments, or mixed content.  You can get an element name with
 *     a cursor by calling {@link XmlCursor#getName() cursor.getName()} when the 
 *     cursor is positioned at an element's START token. See {@link XmlCursor}.
 * <li>Use {@link #selectPath} to find other XmlObjects in the subtree underneath
 *     this XmlObject using relative XPaths.  (In selectPath, "." indicates
 *     the current element or attribute.)
 * </ul>
 * <p>
 * Type inference.  When using {@link XmlObject.Factory} to parse XML documents,
 * the actual document type is not {@link XmlObject#type} itself, but a subtype
 * based on the contents of the parsed document.  If the parsed document
 * contains a recognized root document element, then the actual type of the
 * loaded instance will be the matching Document type.  For example:
 * <pre>
 * XmlObject xobj = XmlObject.Factory.parse(myDocument);
 * if (xobj instanceof MyOrderDocument) // starts w/ &lt;my-order&gt;
 * {
 *     MyOrderDocument mydoc = (MyOrderDocument)xobj;
 *     if (!xobj.validate())
 *         System.out.println("Not a valid my-order document");
 * }
 * else
 * {
 *     System.out.println("Not a my-order document");
 * }
 * </pre>
 * Every XML Bean class has its own inner Factory class,
 * so if you actually know exactly which XML Bean document type
 * you want to load as in the example above, you should use the
 * the specific XML Bean Factory class instead. For example:
 * <pre>
 * MyOrderDocument mydoc = MyOrderDocument.Factory.parse(myDocument);
 * </pre>
 * The code above will throw an exception if the parsed document
 * does not begin with the proper (my-order) element.
 * <p>
 * Inner versus outer.  An XmlObject represents the
 * <em>contents</em> of an element or attribute, <em>not</em> the element
 * or attribute itself.  So when you validate or save an XmlObject, you
 * are validating or saving its contents, not its container.  For example,
 * if the XmlObject represents the contents of an element which happens
 * to itself be in the wrong order relative to its siblings, validate will
 * not complain about the misplacement of the element itself.  On the other hand, if
 * elements <em>within</em> the XmlObject are in the wrong order, validate
 * will complain.  Similarly, when saving the contents of an interior
 * XmlObject, it is the contents of an element, not the element itself,
 * which is saved by default.
 * <p>
 * Reading and writing fragments. When reading or writing the contents of a
 * whole XML document, the standard XML reprentation for a document is used.
 * However, there is no standard concrete XML representation for "just the
 * contents" of an interior element or attribute. So when one is needed,
 * the tag &lt;xml-fragment&gt; is used to wrap the contents.  This tag is used
 * can also be used to load just the contents for an XmlObject document fragment
 * of arbitrary type. If you wish to save out the XmlObject's container element
 * along with its contents, use {@link XmlOptions#setSaveOuter}.
 * <p>
 * Implementing XmlObject.  The XMLBeans library does not support arbitrary
 * implementations of XmlObject - in almost all cases, you should only use
 * the implementations of XmlObject provided by the XMLBeans compiler itself.
 * If you need to implement XmlObject yourself, you should subclass
 * FilterXmlObject in order to delegate to another underlying XmlObject
 * implementation. This technique will allow you to use your code unchanged
 * with future versions of XMLBeans that add additional methods on XmlObject. 
 */ 
public interface XmlObject extends XmlTokenSource
{
    /** The constant {@link SchemaType} object representing this schema type. */
    public static final SchemaType type = XmlBeans.getBuiltinTypeSystem().typeForHandle("_BI_anyType");

    /**
     * The schema type for this instance. This is a permanent,
     * unchanging property of the instance.
     */
    SchemaType schemaType();

    /**
     * Returns true if the contents of this object are valid
     * accoring to schemaType().
     * <p>
     * Does a deep validation of the entire subtree under the
     * object, but does not validate the parents or siblings
     * of the object if the object is in the interior of an xml
     * tree.
     */
    boolean validate();
    
    /**
     * <p>Just like validate(), but with options.</p>
     * <p>If you wish to collect error messages and locations while validating,
     * use the {@link XmlOptions#setErrorListener} method. With that method,
     * you can specify an object in which to store messages related to validation.
     * The following is a simple example.</p>
     * 
     * <pre>
     * // Create an XmlOptions instance and set the error listener.
     * XmlOptions validateOptions = new XmlOptions();
     * ArrayList errorList = new ArrayList();
     * validateOptions.setErrorListener(errorList);
     * 
     * // Validate the XML.
     * boolean isValid = newEmp.validate(validateOptions);
     * 
     * // If the XML isn't valid, loop through the listener's contents,
     * // printing contained messages.
     * if (!isValid)
     * {
     *      for (int i = 0; i < errorList.size(); i++)
     *      {
     *          XmlError error = (XmlError)errorList.get(i);
     *          
     *          System.out.println("\n");
     *          System.out.println("Message: " + error.getMessage() + "\n");
     *          System.out.println("Location of invalid XML: " + 
     *              error.getCursorLocation().xmlText() + "\n");
     *      }
     * }
     * </pre>
     *
     * @param options An object that implements the {@link java.util.Collection
     * Collection} interface.
     */
    boolean validate(XmlOptions options);

    /**
     * Selects a path.  Path can be a string or precompiled path String.
     * <p>
     * The path must be a relative path, where "." represents the
     * element or attribute containg this XmlObject, and it must select
     * only other elements or attributes.  If a non-element or non-attribute
     * is selected, an unchecked exception is thrown.
     * <p>
     * The array that is returned contains all the selected
     * XmlObjects, within the same document, listed in document
     * order.  The actual array type of the result is inferred
     * from the closest common base type of selected results.
     * <p>
     * Here is an example of usage.  Suppose we have a global
     * element definition for "owner" whose type is "person":
     * <pre>
     *   &lt;schema targetNamespace="http://openuri.org/sample">
     *      &lt;element name="owner" type="person"/>
     *      &lt;complexType name="person">
     *         [...]
     *      &lt;/complexType>
     *   &lt;/schema>
     * </pre>
     * and suppose "owner" tags can be scattered throughout the
     * document.  Then we can write the following code to find
     * them all:
     * <pre>
     * import org.openuri.sample.Person;
     * import org.apache.xmlbeans.*;
     * [...]
     *   XmlObject xobj = XmlObject.Factory.parse(myFile);
     *   Person[] results;
     *   results = (Person[])xobj.selectPath(
     *      "declare namespace s='http://www.openuri.org/sample' " +
     *      ".//s:owner");
     * </pre>
     * Notice the way in which namespace declarations are done in XPath 2.0.
     * Since XPath can only navigate within an XML document - it cannot
     * construct new XML - the resulting XmlObjects all reside in
     * the same XML document as this XmlObject itself.
     */
    XmlObject[] selectPath ( String path );

    /**
     * Selects a path, applying options.
     * 
     * @see #selectPath(String)
     */
    XmlObject[] selectPath ( String path, XmlOptions options );
    

    /**
     * Executes a query.  Query can be a string or precompiled query String.
     * <p>
     * An XQuery is very similar to an XPath, except that it also permits
     * construction of new XML.  As a result, the XmlObjects that are
     * returned from execQuery are in newly created documents, separate
     * from the XmlObject on which the query is executed.
     * <p>
     * Syntax and usage is otherwise similar to selectPath.
     * <p>
     * @see #selectPath(String)
     */
    XmlObject[] execQuery ( String query );

    /**
     * Executes a query with options.
     * 
     * Use the <em>options</em> parameter to specify the following:</p>
     * 
     * <table>
     * <tr><th>To specify this</th><th>Use this method</th></tr>
     * <tr>
     *  <td>The document type for the root element.</td>
     *  <td>{@link XmlOptions#setDocumentType}</td>
     * </tr>
     * <tr>
     *  <td>To replace the document element with the specified QName when constructing the
     *  resulting document.</td>
     *  <td>{@link XmlOptions#setLoadReplaceDocumentElement}</td>
     * </tr>
     * <tr>
     *  <td>To strip all insignificant whitespace when constructing a document.</td>
     *  <td>{@link XmlOptions#setLoadStripWhitespace}</td>
     * </tr>
     * <tr>
     *  <td>To strip all comments when constructing a document.</td>
     *  <td>{@link XmlOptions#setLoadStripComments}</td>
     * </tr>
     * <tr>
     *  <td>To strip all processing instructions when constructing a document.</td>
     *  <td>{@link XmlOptions#setLoadStripProcinsts}</td>
     * </tr>
     * <tr>
     *  <td>A map of namespace URI substitutions to use when constructing a document.</td>
     *  <td>{@link XmlOptions#setLoadSubstituteNamespaces}</td>
     * </tr>
     * <tr>
     *  <td>Additional namespace mappings to be added when constructing a document.</td>
     *  <td>{@link XmlOptions#setLoadAdditionalNamespaces}</td>
     * </tr>
     * <tr>
     *  <td>To trim the underlying XML text buffer immediately after constructing 
     *  a document, resulting in a smaller memory footprint.</td>
     *  <td>{@link XmlOptions#setLoadTrimTextBuffer}</td>
     * </tr>
     * <tr>
     *  <td>Whether value facets should be checked as they are set.</td>
     *  <td>{@link XmlOptions#setValidateOnSet}</td>
     * </tr>
     * </table> 
     * 
     * @param query The XQuery expression.
     * @param options Options as described.
     * 
     * @see #execQuery(String)
     */
    XmlObject[] execQuery ( String query, XmlOptions options );
    

    /**
     * Changes the schema type associated with this data and
     * returns a new XmlObject instance whose schemaType is the
     * new type.
     * <p>
     * Returns the new XmlObject if the type change was successful,
     * the old XmlObject if no changes could be made. <p/>
     * Certain type changes may be prohibited in the interior of an xml
     * tree due to schema type system constraints (that is, due
     * to a parent container within which the newly specified
     * type is not permissible), but there are no constraints
     * at the roottype changes are never
     * prohibited at the root of an xml tree.
     * <p>
     * If the type change is allowed, then the new XmlObject should
     * be used rather than the old one. The old XmlObject instance and
     * any other XmlObject instances in the subtree are permanently
     * invalidated and should not be used. (They will return
     * XmlValueDisconnectedException if you try to use them.)
     *
     * If a type change is done on the interior of an Xml
     * tree, then xsi:type attributes are updated as needed.
     */
    XmlObject changeType(SchemaType newType);

    /**
     * Changes the schema type associated with this data using substitution
     * groups and returns an XmlObject instance whose schemaType is the
     * new type and container name is the new name.
     * <p>
     * Returns the new XmlObject if the substitution was successful,
     * the old XmlObject if no changes could be made. <p/>
     * In order for the operation to succeed, several conditions must hold:
     * <ul><li> the container of this type must be an element </li>
     * <li> a global element with the name <code>newName</code> must exist
     * and must be in the substition group of the containing element </li>
     * <li> the <code>newType</code> type must be consistent with the declared
     * type of the new element </li></ul>
     * <p>
     * If the type change is allowed, then the new XmlObject should
     * be used rather than the old one. The old XmlObject instance and
     * any other XmlObject instances in the subtree are permanently
     * invalidated and should not be used. (They will return
     * XmlValueDisconnectedException if you try to use them.)
     * If necessary, xsi:type attributes are updated.
     */
    XmlObject substitute(QName newName, SchemaType newType);

    /**
     * True if the value is nil. Note that in order to be nil,
     * the value must be in an element, and the element containing
     * the value must be marked as nillable in the schema.
     */
    boolean isNil();

    /**
     * Sets the value to nil. The element containing the value must
     * be marked as nillable in the schema.
     */
    void setNil();


    /**
     * Returns an XML string for this XML object.
     * <p>
     * The string is pretty-printed.  If you want a non-pretty-printed
     * string, or if you want to control options precisely, use the
     * xmlText() methods.
     * <p>
     * Note that when producing XML any object other than very root of the
     * document, then you are guaranteed to be looking at only a fragment
     * of XML, i.e., just the contents of an element or attribute, and
     * and we will produce a string that starts with an <code>&lt;xml-fragment&gt;</code> tag.
     * The XmlOptions.setSaveOuter() option on xmlText can be used to produce
     * the actual element name above the object if you wish.
     */
    String toString();

    /**
     * True if the value is an immutable value. Immutable values do not
     * have a position in a tree; rather, they are stand-alone simple type
     * values. If the object is immutable, the equals() methods tests for
     * value equality, and the object can be used as the key for a hash.
     */
    boolean isImmutable();

    /**
     * Set the value/type of this XmlObject to be a copy of the source
     * XmlObject.  Because the type of the source may be different than this
     * target, this XmlObject may become defunct.  In this case the new
     * XmlObject is returned.  If no type change happens, the same this will be
     * returned.
     */
    XmlObject set(XmlObject srcObj);

    /**
     * Returns a deep copy of this XmlObject.  The returned object has the
     * same type as the current object, and has all the content of
     * the XML document underneath the current object.  Note that
     * any parts of the XML document above or outside this XmlObject are
     * not copied.
     *
     * Note: The result object will be in the same synchronization domain as the source,
     * and additional synchronization is required for concurent access.
     * To use a different synchronization domain use setCopyUseNewSynchronizationDomain
     * option with copy(XmlOptions) method.
     * @see #copy(XmlOptions)
     * @see org.apache.xmlbeans.XmlOptions#setCopyUseNewSynchronizationDomain(boolean)
     */
    XmlObject copy();

    /**
     * Returns a deep copy of this XmlObject.  The returned object has the
     * same type as the current object, and has all the content of
     * the XML document underneath the current object.  Note that
     * any parts of the XML document above or outside this XmlObject are
     * not copied.
     *
     * Note: The result object will be in the same synchronization domain as the source,
     * and additional synchronization is required for concurent access.
     * To use a different synchronization domain use setCopyUseNewSynchronizationDomain
     * option when creating the original XmlObject.
     * @see org.apache.xmlbeans.XmlOptions#setCopyUseNewSynchronizationDomain(boolean)
     */
    XmlObject copy(XmlOptions options);

    /**
     * True if the xml values are equal. Two different objects
     * (which are distinguished by equals(obj) == false) may of
     * course have equal values (valueEquals(obj) == true).
     * <p>
     * Usually this method can be treated as an ordinary equvalence
     * relation, but actually it is not is not transitive.
     * Here is a precise specification:
     * <p>
     * There are two categories of XML object: objects with a known
     * instance type, and objects whose only known type is one of the
     * ur-types (either AnyType or AnySimpleType). The first category
     * is compared in terms of logical value spaces, and the second
     * category is compared lexically.
     * <p>
     * Within each of these two categories, valueEquals is a well-behaved
     * equivalence relation. However, when comparing an object of known
     * type with an object with ur-type, the comparison is done by attempting
     * to convert the lexical form of the ur-typed object into the other
     * type, and then comparing the results. Ur-typed objects are therefore
     * treated as lexical wildcards and may be equal to objects in different
     * value spaces, even though the objects in different value spaces are
     * not equal to each other.
     * <p>
     * For example, the anySimpleType value "1" will compare as an
     * equalValue to the string "1", the float value "1.0", the double
     * value "1.0", the decimal "1", and the GYear "1", even though
     * all these objects will compare unequal to each other since they
     * lie in different value spaces.
     * Note: as of XMLBeans 2.2.1 only implemented for simple type values.
     */
    boolean valueEquals(XmlObject obj);

    int valueHashCode();

    /**
     * Impelements the Comparable interface by comparing two simple
     * xml values based on their standard XML schema ordering.
     * Throws a ClassCastException if no standard ordering applies,
     * or if the two values are incomparable within a partial order.
     */
    int compareTo(Object obj);

    /**
     * This comparison method is similar to compareTo, but rather
     * than throwing a ClassCastException when two values are incomparable,
     * it returns the number 2. The result codes are -1 if this object
     * is less than obj, 1 if this object is greater than obj, zero if
     * the objects are equal, and 2 if the objects are incomparable.
     */
    int compareValue(XmlObject obj);

    /** LESS_THAN is -1. See {@link #compareValue}. */
    static final int LESS_THAN = -1;

    /** EQUAL is 0. See {@link #compareValue}. */
    static final int EQUAL = 0;

    /** GREATER_THAN is 1. See {@link #compareValue}. */
    static final int GREATER_THAN = 1;

    /** NOT_EQUAL is 2. See {@link #compareValue}. */
    static final int NOT_EQUAL = 2;

    /**
     * Selects the contents of the children elements with the given name.
     * @param elementName The name of the elements to be selected.
     * @return Returns the contents of the selected elements.
     */
    XmlObject[] selectChildren(QName elementName);

    /**
     * Selects the contents of the children elements with the given name.
     * @param elementUri The URI of the elements to be selected.
     * @param elementLocalName The local name of the elements to be selected.
     * @return Returns the contents of the selected elements.
     */
    XmlObject[] selectChildren(String elementUri, String elementLocalName);

    /**
     * Selects the contents of the children elements that are contained in the elementNameSet.
     * @param elementNameSet Set of element names to be selected.
     * @return Returns the contents of the selected elements.
     * @see SchemaType#qnameSetForWildcardElements()
     * @see QNameSetBuilder for creating sets of qnames
     */
    XmlObject[] selectChildren(QNameSet elementNameSet);

    /**
     * Selects the content of the attribute with the given name.
     * @param attributeName The name of the attribute to be selected.
     * @return Returns the contents of the selected attribute.
     */
    XmlObject selectAttribute(QName attributeName);

    /**
     * Selects the content of the attribute with the given name.
     * @param attributeUri The URI of the attribute to be selected.
     * @param attributeLocalName The local name of the attribute to be selected.
     * @return Returns the content of the selected attribute.
     */
    XmlObject selectAttribute(String attributeUri, String attributeLocalName);

    /**
     * Selects the contents of the attributes that are contained in the elementNameSet.
     * @param attributeNameSet Set of attribute names to be selected.
     * @return Returns the contents of the selected attributes.
     * @see SchemaType#qnameSetForWildcardAttributes()
     * @see QNameSetBuilder for creating sets of qnames
     */
    XmlObject[] selectAttributes(QNameSet attributeNameSet);

    /**
     * Static factory class for creating new instances.  Note that if
     * a type can be inferred from the XML being loaded (for example,
     * by recognizing the document element QName), then the instance
     * returned by a factory will have the inferred type.  Otherwise
     * the Factory will returned an untyped document.
     */
    public static final class Factory
    {
        /**
         * Creates a new, completely empty instance.
         */ 
        public static XmlObject newInstance ( ) {
          return XmlBeans.getContextTypeLoader().newInstance( null, null ); }
        
        /**
         * <p>Creates a new, completely empty instance, specifying options
         * for the root element's document type and/or whether to validate
         * value facets as they are set.</p>
         * 
         * Use the <em>options</em> parameter to specify the following:</p>
         * 
         * <table>
         * <tr><th>To specify this</th><th>Use this method</th></tr>
         * <tr>
         *  <td>The document type for the root element.</td>
         *  <td>{@link XmlOptions#setDocumentType}</td>
         * </tr>
         * <tr>
         *  <td>Whether value facets should be checked as they are set.</td>
         *  <td>{@link XmlOptions#setValidateOnSet}</td>
         * </tr>
         * </table> 
         * 
         * @param options Options specifying root document type and/or value facet 
         * checking.
         * @return A new, empty instance of XmlObject.</li>
         */ 
        public static XmlObject newInstance ( XmlOptions options ) {
          return XmlBeans.getContextTypeLoader().newInstance( null, options ); }
        
        /**
         * Creates a new immutable value.
         */ 
        /** Creates an immutable {@link XmlObject} value */
        public static XmlObject newValue ( Object obj ) {
          return type.newValue( obj ); }
        
        /**
         * Parses the given {@link String} as XML.
         */ 
        public static XmlObject parse ( String xmlAsString ) throws XmlException {
          return XmlBeans.getContextTypeLoader().parse( xmlAsString, null, null ); }
        
        /**
         * Parses the given {@link String} as XML.
         * 
         * Use the <em>options</em> parameter to specify the following:</p>
         * 
         * <table>
         * <tr><th>To specify this</th><th>Use this method</th></tr>
         * <tr>
         *  <td>The document type for the root element.</td>
         *  <td>{@link XmlOptions#setDocumentType}</td>
         * </tr>
         * <tr>
         *  <td>To place line number annotations in the store when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadLineNumbers}</td>
         * </tr>
         * <tr>
         *  <td>To replace the document element with the specified QName when parsing.</td>
         *  <td>{@link XmlOptions#setLoadReplaceDocumentElement}</td>
         * </tr>
         * <tr>
         *  <td>To strip all insignificant whitespace when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadStripWhitespace}</td>
         * </tr>
         * <tr>
         *  <td>To strip all comments when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadStripComments}</td>
         * </tr>
         * <tr>
         *  <td>To strip all processing instructions when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadStripProcinsts}</td>
         * </tr>
         * <tr>
         *  <td>A map of namespace URI substitutions to use when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadSubstituteNamespaces}</td>
         * </tr>
         * <tr>
         *  <td>Additional namespace mappings to be added when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadAdditionalNamespaces}</td>
         * </tr>
         * <tr>
         *  <td>To trim the underlying XML text buffer immediately after parsing 
         *  a document, resulting in a smaller memory footprint.</td>
         *  <td>{@link XmlOptions#setLoadTrimTextBuffer}</td>
         * </tr>
         * </table> 
         * 
         * @param xmlAsString The string to parse.
         * @param options Options as specified.
         * @return A new instance containing the specified XML.
         */ 
        public static XmlObject parse ( String xmlAsString, XmlOptions options ) throws XmlException {
          return XmlBeans.getContextTypeLoader().parse( xmlAsString, null, options ); }
        
        /**
         * Parses the given {@link File} as XML.
         */ 
        public static XmlObject parse ( File file ) throws XmlException, IOException {
          return XmlBeans.getContextTypeLoader().parse( file, null, null ); }

        /**
         * Parses the given {@link File} as XML.
         */ 
        public static XmlObject parse ( File file, XmlOptions options ) throws XmlException, IOException {
          return XmlBeans.getContextTypeLoader().parse( file, null, options ); }
        
        /**
         * Downloads the given {@link java.net.URL} as XML.
         */ 
        public static XmlObject parse(java.net.URL u) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return XmlBeans.getContextTypeLoader().parse( u, null, null ); }

        /**
         * Downloads the given {@link java.net.URL} as XML.
         */ 
        public static XmlObject parse(java.net.URL u, org.apache.xmlbeans.XmlOptions options) throws org.apache.xmlbeans.XmlException, java.io.IOException {
          return XmlBeans.getContextTypeLoader().parse( u, null, options ); }

        /**
         * Decodes and parses the given {@link InputStream} as XML.
         */ 
        public static XmlObject parse ( InputStream is ) throws XmlException, IOException {
          return XmlBeans.getContextTypeLoader().parse( is, null, null ); }
        
        /**
         * Decodes and parses the given {@link XMLStreamReader} as XML.
         */ 
        public static XmlObject parse ( XMLStreamReader xsr ) throws XmlException {
          return XmlBeans.getContextTypeLoader().parse( xsr, null, null ); }
        
        /**
         * Decodes and parses the given {@link InputStream} as XML.
         * 
         * Use the <em>options</em> parameter to specify the following:</p>
         * 
         * <table>
         * <tr><th>To specify this</th><th>Use this method</th></tr>
         * <tr>
         *  <td>The character encoding to use when parsing or writing a document.</td>
         *  <td>{@link XmlOptions#setCharacterEncoding}</td>
         * </tr>
         * <tr>
         *  <td>The document type for the root element.</td>
         *  <td>{@link XmlOptions#setDocumentType}</td>
         * </tr>
         * <tr>
         *  <td>Place line number annotations in the store when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadLineNumbers}</td>
         * </tr>
         * <tr>
         *  <td>Replace the document element with the specified QName when parsing.</td>
         *  <td>{@link XmlOptions#setLoadReplaceDocumentElement}</td>
         * </tr>
         * <tr>
         *  <td>Strip all insignificant whitespace when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadStripWhitespace}</td>
         * </tr>
         * <tr>
         *  <td>Strip all comments when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadStripComments}</td>
         * </tr>
         * <tr>
         *  <td>Strip all processing instructions when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadStripProcinsts}</td>
         * </tr>
         * <tr>
         *  <td>Set a map of namespace URI substitutions to use when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadSubstituteNamespaces}</td>
         * </tr>
         * <tr>
         *  <td>Set additional namespace mappings to be added when parsing a document.</td>
         *  <td>{@link XmlOptions#setLoadAdditionalNamespaces}</td>
         * </tr>
         * <tr>
         *  <td>Trim the underlying XML text buffer immediately after parsing 
         *  a document, resulting in a smaller memory footprint.</td>
         *  <td>{@link XmlOptions#setLoadTrimTextBuffer}</td>
         * </tr>
         * </table> 
         */ 
        public static XmlObject parse ( InputStream is, XmlOptions options ) throws XmlException, IOException {
          return XmlBeans.getContextTypeLoader().parse( is, null, options ); }
        
        /**
         * Parses the given {@link XMLStreamReader} as XML.
         */ 
        public static XmlObject parse ( XMLStreamReader xsr, XmlOptions options ) throws XmlException {
          return XmlBeans.getContextTypeLoader().parse( xsr, null, options ); }
        
        /**
         * Parses the given {@link Reader} as XML.
         */ 
        public static XmlObject parse ( Reader r ) throws XmlException, IOException {
          return XmlBeans.getContextTypeLoader().parse( r, null, null ); }
        
        /**
         * Parses the given {@link Reader} as XML.
         */ 
        public static XmlObject parse ( Reader r, XmlOptions options ) throws XmlException, IOException {
          return XmlBeans.getContextTypeLoader().parse( r, null, options ); }
        
        /**
         * Converts the given DOM {@link Node} into an XmlObject.
         */ 
        public static XmlObject parse ( Node node ) throws XmlException {
          return XmlBeans.getContextTypeLoader().parse( node, null, null ); }
        
        /**
         * Converts the given DOM {@link Node} into an XmlObject.
         */ 
        public static XmlObject parse ( Node node, XmlOptions options ) throws XmlException {
          return XmlBeans.getContextTypeLoader().parse( node, null, options ); }
        
        /**
         * Loads the given {@link XMLInputStream} into an XmlObject.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlObject parse ( XMLInputStream xis ) throws XmlException, XMLStreamException {
          return XmlBeans.getContextTypeLoader().parse( xis, null, null ); }
        
        /**
         * Loads the given {@link XMLInputStream} into an XmlObject.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XmlObject parse ( XMLInputStream xis, XmlOptions options ) throws XmlException, XMLStreamException {
          return XmlBeans.getContextTypeLoader().parse( xis, null, options ); }

        /**
         * Returns an {@link XmlSaxHandler} that can load an XmlObject from SAX events.
         */ 
        public static XmlSaxHandler newXmlSaxHandler ( ) {
          return XmlBeans.getContextTypeLoader().newXmlSaxHandler( null, null ); }
            
        /**
         * Returns an {@link XmlSaxHandler} that can load an XmlObject from SAX events.
         */ 
        public static XmlSaxHandler newXmlSaxHandler ( XmlOptions options ) {
          return XmlBeans.getContextTypeLoader().newXmlSaxHandler( null, options ); }
            
        /**
         * Creates a new DOMImplementation object
         */ 
        public static DOMImplementation newDomImplementation ( ) {
          return XmlBeans.getContextTypeLoader().newDomImplementation( null ); }
        
        /**
         * Creates a new DOMImplementation object, taking options
         */ 
        public static DOMImplementation newDomImplementation ( XmlOptions options ) {
          return XmlBeans.getContextTypeLoader().newDomImplementation( options ); }
        
        /**
         * Returns a new validating {@link XMLInputStream} that throws exceptions when the input is not valid.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XMLInputStream newValidatingXMLInputStream ( XMLInputStream xis ) throws XmlException, XMLStreamException {
          return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, null, null ); }
        
        /**
         * Returns a new validating {@link XMLInputStream} that throws exceptions 
         * when the input is not valid, specifying options
         * for the root element's document type and/or the collection object to use
         * as an error listener while validating.</p>
         * 
         * <p>Use the <em>options</em> parameter to specify the following:</p>
         * 
         * <ul>
         * <li>A collection instance that should be used as an error listener during
         * compilation, as described in {@link XmlOptions#setErrorListener}.</li>
         * <li>The document type for the root element, as described in 
         * {@link XmlOptions#setDocumentType(SchemaType)}.</li>
         * </ul>
         * 
         * @param xis The basis for the new XMLInputStream.
         * @param options Options specifying root document type and/or an error listener.
         * @return A new validating XMLInputStream.
         * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
         */
        public static XMLInputStream newValidatingXMLInputStream ( XMLInputStream xis, XmlOptions options ) throws XmlException, XMLStreamException {
          return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream( xis, null, options ); }
        
        /**
         * Instances cannot be created.
         */ 
        private Factory() { }
    }
}
