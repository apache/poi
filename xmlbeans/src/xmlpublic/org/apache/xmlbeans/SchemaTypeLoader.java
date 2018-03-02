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

import javax.xml.namespace.QName;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;
import org.w3c.dom.DOMImplementation;

/**
 * Represents a searchable set of XML Schema component definitions.
 * <p>
 * SchemaTypeLoader is somewhat analogous to {@link java.lang.ClassLoader},
 * because it is responsible for finding {@link SchemaComponent} definitions
 * by name, yet it is not responsible for being able to enumerate all the
 * component definitons available. (If you wish to enumerate component
 * definitions, see {@link SchemaTypeSystem}.) There are some ways in which
 * SchemaTypeSystems are dissimilar from ClassLoaders, however.
 * Since XML Schema has a number of instance-oriented typing mechanisms
 * (such as wildcards) that do not exist in Java, a SchemaTypeLoader is
 * not associated with a type; instead, a SchemaTypeLoader is associated
 * with each XML instance.
 * <p>
 * Every XML instance is loaded within the context of a SchemaTypeLoader;
 * the SchemaTypeLoader for an instance is used to resolve all type definitions
 * within the instance and for applying type-sensitive methods such as
 * {@link XmlObject#validate}.
 * <p>
 * Normally the SchemaTypeLoader being used for all instances is the
 * context type loader (that is, the SchemaTypeLoader returned from
 * {@link XmlBeans#getContextTypeLoader()}).  The context type loader
 * consults the thread's context ClassLoader (see {@link Thread#getContextClassLoader()})
 * to find schema type defintions that are available on the classpath.
 * The net result is that you can use schema types simply by putting
 * their compiled schema JARs on your classpath.
 * If you wish to load instances using a different SchemaTypeLoader, then you must
 * call {@link #parse} methods on the SchemaTypeLoader instance explicitly
 * rather than using the normal convenient Factory methods.
 * <p>
 * A SchemaTypeLoader can be obtained by dynamically loading XSD files
 * using {@link XmlBeans#loadXsd}, or by assembling other SchemaTypeLoaders
 * or SchemaTypeSystems on a path using {@link XmlBeans#typeLoaderUnion}.
 * 
 * @see XmlBeans#loadXsd
 * @see XmlBeans#getContextTypeLoader
 * @see XmlBeans#typeLoaderUnion
 * @see SchemaTypeSystem
 */ 
public interface SchemaTypeLoader
{
    /** Returns the type with the given name, or null if none. */
    public SchemaType findType(QName name);

    /** Returns the document type rooted at the given element name, or null if none. */
    public SchemaType findDocumentType(QName name);
    
    /** Returns the attribute type containing the given attribute name, or null if none. */
    public SchemaType findAttributeType(QName name);

    /** Returns the global element defintion with the given name, or null if none. */
    public SchemaGlobalElement findElement(QName name);

    /** Returns the global attribute defintion with the given name, or null if none. */
    public SchemaGlobalAttribute findAttribute(QName name);
    
    /** Returns the model group defintion with the given name, or null if none. */
    public SchemaModelGroup findModelGroup(QName name);
    
    /** Returns the attribute group defintion with the given name, or null if none. */
    public SchemaAttributeGroup findAttributeGroup(QName name);

    /** True if the typeloader contains any definitions in the given namespace. */
    public boolean isNamespaceDefined(String namespace);

    /** Used for on-demand loading. */
    public SchemaType.Ref findTypeRef(QName name);

    /** Used for on-demand loading. */
    public SchemaType.Ref findDocumentTypeRef(QName name);
    
    /** Used for on-demand loading. */
    public SchemaType.Ref findAttributeTypeRef(QName name);

    /** Used for on-demand loading. */
    public SchemaGlobalElement.Ref findElementRef(QName name);

    /** Used for on-demand loading. */
    public SchemaGlobalAttribute.Ref findAttributeRef(QName name);
    
    /** Used for on-demand loading. */
    public SchemaModelGroup.Ref findModelGroupRef(QName name);
    
    /** Used for on-demand loading. */
    public SchemaAttributeGroup.Ref findAttributeGroupRef(QName name);

    /** Used for on-demand loading. */
    public SchemaIdentityConstraint.Ref findIdentityConstraintRef(QName name);

    /** Finds a type for a given signature string */
    public SchemaType typeForSignature(String signature);

    /** Finds a type for a given fully-qualified XML Bean classname */
    public SchemaType typeForClassname(String classname);

    /** Loads original XSD source as a stream.  See {@link SchemaType#getSourceName}. */
    public InputStream getSourceAsStream(String sourceName);
    
    /** Compiles an XPath */
    public String compilePath(String pathExpr, XmlOptions options) throws XmlException;

    /** Compiles an XQuery */
    public String compileQuery(String queryExpr, XmlOptions options) throws XmlException;

    /** Creates an instance of the given type. */
    public XmlObject newInstance ( SchemaType type, XmlOptions options );
    /** Parses an instance of the given type. */
    public XmlObject parse ( String xmlText, SchemaType type, XmlOptions options ) throws XmlException;
    /** Parses an instance of the given type. */
    public XmlObject parse ( File file, SchemaType type, XmlOptions options ) throws XmlException, IOException;
    /** Parses an instance of the given type. */
    public XmlObject parse ( URL file, SchemaType type, XmlOptions options ) throws XmlException, IOException;
    /** Parses an instance of the given type. */
    public XmlObject parse ( InputStream jiois, SchemaType type, XmlOptions options ) throws XmlException, IOException;
    /** Parses an instance of the given type. */
    public XmlObject parse ( XMLStreamReader xsr, SchemaType type, XmlOptions options ) throws XmlException;
    /** Parses an instance of the given type. */
    public XmlObject parse ( Reader jior, SchemaType type, XmlOptions options ) throws XmlException, IOException;
    /** Parses an instance of the given type. */
    public XmlObject parse ( Node node, SchemaType type, XmlOptions options ) throws XmlException;
    /** Parses an instance of the given type.
     * @deprecated Deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XmlObject parse ( XMLInputStream xis, SchemaType type, XmlOptions options ) throws XmlException, XMLStreamException;
    /** Returns an XmlSaxHandler that can parse an instance of the given type. */
    public XmlSaxHandler newXmlSaxHandler ( SchemaType type, XmlOptions options );
    /** Returns a DOMImplementation. */
    public DOMImplementation newDomImplementation ( XmlOptions options );
    /** Returns a validating XMLInputStream that will throw an exception if the XML is not valid
     * @deprecated Deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XMLInputStream newValidatingXMLInputStream ( XMLInputStream xis, SchemaType type, XmlOptions options ) throws XmlException, XMLStreamException;
}
