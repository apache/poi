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

/**
 * This class is used to attach arbitrary information to an XML
 * document.  It also defines several well-known types of
 * information that can be attached or found on a document.
 * <p>
 * For example, suppose you wanted to associate a filename
 * with the document containing an xml object "xobj".  This
 * could be done via the following code:
 * <p>
 * tokenSource.documentProperties().set(XmlDocumentProperties.NAME, "MyFilename.xml");
 * <p>
 * To fetch the filename later, given an xobj2 anywhere in the
 * same document, you can write:
 * <p>
 * filename = (String)tokenSource.documentProperties().get(XmlDocumentProperties.NAME);
 */
public abstract class XmlDocumentProperties
{
    /**
     * Sets the name of the XML document file.  Typically a URL, but may
     * be any String.
     * @param sourceName the name to set
     * @see XmlOptions#setDocumentSourceName
    */ 
    public void   setSourceName ( String sourceName ) { put( SOURCE_NAME, sourceName ); }
    /**
     * Returns the name of the XML document file.  Typically a URL, but may
     * be any String.
     * @see XmlOptions#setDocumentSourceName
     */ 
    public String getSourceName ( ) { return (String) get( SOURCE_NAME ); }
    
    /**
     * Sets the encoding to use for the XML document.  Should be a valid
     * XML encoding string.
     * @param encoding the ISO encoding name
     * @see XmlOptions#setCharacterEncoding
     */ 
    public void   setEncoding ( String encoding ) { put( ENCODING, encoding ); }
    /**
     * Returns the encoding used for the XML document, as an ISO encoding name.
     * @see XmlOptions#setCharacterEncoding
     */ 
    public String getEncoding ( ) { return (String) get( ENCODING ); }
    
    /**
     * Sets the XML version string to use in the &lt&#63;xml&#63;&gt; declaration.
     * (The XML specification is quite stable at "1.0".)
     * @param version the XML version string
     */ 
    public void   setVersion ( String version ) { put( VERSION, version ); }
    /**
     * Returns the XML version string used in the &lt&#63;xml&#63;&gt; declaration.
     */ 
    public String getVersion ( ) { return (String) get( VERSION ); }
    
    /**
     * Sets the standalone property.
     * @param standalone whether standalone is true or not
     */ 
    public void setStandalone ( boolean standalone ) { put( STANDALONE, standalone ? "true" : null ); }
    /**
     * Returns the standalone property
     */
    public boolean getStandalone ( ) { return get( STANDALONE ) != null; }

    /**
     * Sets the DOCTYPE name use in the &lt&#33;DOCTYPE&gt; declaration.
     * @param doctypename the doctypename
     */ 
    public void   setDoctypeName ( String doctypename ) { put( DOCTYPE_NAME, doctypename ); }
    /**
     * Returns the DOCTYPE name used in the &lt&#33;DOCTYPE&gt; declaration.
     */ 
    public String getDoctypeName ( ) { return (String) get( DOCTYPE_NAME ); }
    
    /**
     * Sets the DOCTYPE public ID to use in the &lt&#33;DOCTYPE&gt; declaration.
     * @param publicid the public ID
     */ 
    public void   setDoctypePublicId ( String publicid ) { put( DOCTYPE_PUBLIC_ID, publicid ); }
    /**
     * Returns the DOCTYPE public ID used in the &lt&#33;DOCTYPE&gt; declaration.
     */ 
    public String getDoctypePublicId ( ) { return (String) get( DOCTYPE_PUBLIC_ID ); }
    
    /**
     * Sets the DOCTYPE system ID to use in the &lt&#33;DOCTYPE&gt; declaration.
     * @param systemid the system ID
     */ 
    public void   setDoctypeSystemId ( String systemid ) { put( DOCTYPE_SYSTEM_ID, systemid ); }
    /**
     * Returns the DOCTYPE system ID used in the &lt&#33;DOCTYPE&gt; declaration.
     */
    public String getDoctypeSystemId ( ) { return (String) get( DOCTYPE_SYSTEM_ID ); }

    /**
     * Sets the message digest used to summarize the document.
     * @param digest the bytes of the digest
     * 
     * @see XmlOptions#setLoadMessageDigest
     */ 
    public void   setMessageDigest( byte[] digest ) { put( MESSAGE_DIGEST, digest ); }
    /**
     * Returns the message digest used to summarize the document.
     * 
     * @see XmlOptions#setLoadMessageDigest
     */ 
    public byte[] getMessageDigest( ) { return (byte[]) get( MESSAGE_DIGEST ); }
    
    /**
     * Used to store the original name (a String) for
     * the source from which the XML document was loaded.
     * This name, if present, is used to identify the
     * document when reporting validation or comilation errors.
     *
     * XmlObject.Factory.parse(File) and SchemaTypeLoader.loadInstance(File)
     * both automatically set this value to the filename.
     */
    public static final Object SOURCE_NAME = new Object();
    
    /**
     * Document encoding
     */
    public static final Object ENCODING = new Object();
    
    /**
     * Document version
     */
    public static final Object VERSION = new Object();
    
    /**
     * Document standlone
     */
    public static final Object STANDALONE = new Object();
    
    /**
     * Doc type name
     */
    public static final Object DOCTYPE_NAME = new Object();
    
    /**
     * Doc type public id
     */
    public static final Object DOCTYPE_PUBLIC_ID = new Object();
    
    /**
     * Doc type system id
     */
    public static final Object DOCTYPE_SYSTEM_ID = new Object();
    
    /**
     * SHA message digest
     */
    public static final Object MESSAGE_DIGEST = new Object();

    /**
     * Attaches a value to the root of the document containing
     * the given token source.
     *
     * @param key   The key: there can be one value for each key.
     * @param value The value to attach to the document.
     */
    public abstract Object put ( Object key, Object value );
    
    /**
     * Returns a value previously attached to a document using set.
     *
     * @param key   The key: this is the key that was previously
     *              passed to set to store the value.
     * @return      The saved value, or null if none is found.
     */
    public abstract Object get ( Object key );

    /**
     * Removes a value previously attached to a document using set.
     *
     * @param key   The key: this is the key that was previously
     *              passed to set to store the value.
     */
    public abstract Object remove ( Object key );
}

