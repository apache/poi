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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.validator.ValidatingXMLInputStream;

import org.apache.xmlbeans.impl.store.Locale;

import org.apache.xmlbeans.SchemaAttributeGroup;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaModelGroup;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlSaxHandler;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlFactoryHook;
import org.apache.xmlbeans.XmlBeans;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.InputStream;
import java.io.Reader;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Node;
import org.w3c.dom.DOMImplementation;

import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;

public abstract class SchemaTypeLoaderBase implements SchemaTypeLoader
{
    private static final String USER_AGENT = "XMLBeans/" + XmlBeans.getVersion() + " (" + XmlBeans.getTitle() + ")";

    private static final Method _pathCompiler = getMethod( "org.apache.xmlbeans.impl.store.Path", "compilePath", new Class[] { String.class, XmlOptions.class } );
    private static final Method _queryCompiler = getMethod( "org.apache.xmlbeans.impl.store.Query", "compileQuery", new Class[] { String.class, XmlOptions.class } );

    private static Method getMethod ( String className, String methodName, Class[] args )
    {
        try
        {
            return
                Class.forName( className ).
                    getDeclaredMethod( methodName, args );
        }
        catch (Exception e)
        {
            throw new IllegalStateException(
                "Cannot find " + className + "." + methodName +
                    ".  verify that xmlstore " +
                        "(from xbean.jar) is on classpath" );
        }
    }

    private static Object invokeMethod ( Method method, Object[] args )
    {
        try
        {
            return method.invoke( method, args );
        }
        catch ( InvocationTargetException e )
        {
            Throwable t = e.getCause();
            IllegalStateException ise = new IllegalStateException( t.getMessage() );
            ise.initCause( t ); // need initCause() to support Java1.4
            throw ise;
        }
        catch ( Exception e )
        {
            IllegalStateException ise = new IllegalStateException( e.getMessage() );
            ise.initCause( e );
            throw ise;
        }
    }

    private static String doCompilePath ( String pathExpr, XmlOptions options )
    {
        return (String) invokeMethod( _pathCompiler, new Object[] { pathExpr, options } );
    }

    private static String doCompileQuery ( String queryExpr, XmlOptions options )
    {
        return (String) invokeMethod( _queryCompiler, new Object[] { queryExpr, options } );
    }

    public SchemaType findType(QName name)
    {
        SchemaType.Ref ref = findTypeRef(name);
        if (ref == null)
            return null;
        SchemaType result = ref.get();
        assert(result != null);
        return result;
    }

    public SchemaType findDocumentType(QName name)
    {
        SchemaType.Ref ref = findDocumentTypeRef(name);
        if (ref == null)
            return null;
        SchemaType result = ref.get();
        assert(result != null);
        return result;
    }

    public SchemaType findAttributeType(QName name)
    {
        SchemaType.Ref ref = findAttributeTypeRef(name);
        if (ref == null)
            return null;
        SchemaType result = ref.get();
        assert(result != null);
        return result;
    }

    public SchemaModelGroup findModelGroup(QName name)
    {
        SchemaModelGroup.Ref ref = findModelGroupRef(name);
        if (ref == null)
            return null;
        SchemaModelGroup result = ref.get();
        assert(result != null);
        return result;
    }

    public SchemaAttributeGroup findAttributeGroup(QName name)
    {
        SchemaAttributeGroup.Ref ref = findAttributeGroupRef(name);
        if (ref == null)
            return null;
        SchemaAttributeGroup result = ref.get();
        assert(result != null);
        return result;
    }

    public SchemaGlobalElement findElement(QName name)
    {
        SchemaGlobalElement.Ref ref = findElementRef(name);
        if (ref == null)
            return null;
        SchemaGlobalElement result = ref.get();
        assert(result != null);
        return result;
    }

    public SchemaGlobalAttribute findAttribute(QName name)
    {
        SchemaGlobalAttribute.Ref ref = findAttributeRef(name);
        if (ref == null)
            return null;
        SchemaGlobalAttribute result = ref.get();
        assert(result != null);
        return result;
    }

    //
    //
    //

    public XmlObject newInstance ( SchemaType type, XmlOptions options )
    {
        XmlFactoryHook hook = XmlFactoryHook.ThreadContext.getHook();
        
        if (hook != null)
            return hook.newInstance( this, type, options );

        return Locale.newInstance( this, type, options );
    }

    public XmlObject parse ( String xmlText, SchemaType type, XmlOptions options ) throws XmlException
    {
        XmlFactoryHook hook = XmlFactoryHook.ThreadContext.getHook();
        
        if (hook != null)
            return hook.parse( this, xmlText, type, options );

        return Locale.parseToXmlObject( this, xmlText, type, options );
    }

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XmlObject parse ( XMLInputStream xis, SchemaType type, XmlOptions options ) throws XmlException, XMLStreamException
    {
        XmlFactoryHook hook = XmlFactoryHook.ThreadContext.getHook();
        
        if (hook != null)
            return hook.parse( this, xis, type, options );
        
        return Locale.parseToXmlObject( this, xis, type, options );
    }

    public XmlObject parse ( XMLStreamReader xsr, SchemaType type, XmlOptions options ) throws XmlException
    {
        XmlFactoryHook hook = XmlFactoryHook.ThreadContext.getHook();
        
        if (hook != null)
            return hook.parse( this, xsr, type, options );

        return Locale.parseToXmlObject( this, xsr, type, options );
    }
    
    public XmlObject parse ( File file, SchemaType type, XmlOptions options ) throws XmlException, IOException
    {
        if (options == null)
        {
            options = new XmlOptions();
            options.put( XmlOptions.DOCUMENT_SOURCE_NAME, file.toURI().normalize().toString() );
        }

        else if (! options.hasOption(XmlOptions.DOCUMENT_SOURCE_NAME))
        {
            options = new XmlOptions( options );
            options.put( XmlOptions.DOCUMENT_SOURCE_NAME, file.toURI().normalize().toString() );
        }

        InputStream fis = new FileInputStream( file );

        try
        {
            return parse( fis, type, options );
        }
        finally
        {
            fis.close();
        }
    }

    public XmlObject parse ( URL url, SchemaType type, XmlOptions options ) throws XmlException, IOException
    {
        if (options == null)
        {
            options = new XmlOptions();
            options.put( XmlOptions.DOCUMENT_SOURCE_NAME, url.toString() );
        }

        else if (! options.hasOption(XmlOptions.DOCUMENT_SOURCE_NAME))
        {
            options = new XmlOptions( options );
            options.put( XmlOptions.DOCUMENT_SOURCE_NAME, url.toString() );
        }

        URLConnection conn = null;
        InputStream stream = null;
        download: try
        {

            boolean redirected = false;
            int count = 0;

            do {
                conn = url.openConnection();
                conn.addRequestProperty("User-Agent", USER_AGENT);
                conn.addRequestProperty("Accept", "application/xml, text/xml, */*");
                if (conn instanceof HttpURLConnection)
                {
                    HttpURLConnection httpcon = (HttpURLConnection)conn;
                    int code = httpcon.getResponseCode();
                    redirected = (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP);
                    if (redirected && count > 5)
                        redirected = false;

                    if (redirected)
                    {
                        String newLocation = httpcon.getHeaderField("Location");
                        if (newLocation == null)
                            redirected = false;
                        else
                        {
                            url = new URL(newLocation);
                            count ++;
                        }
                    }
                }
            } while (redirected);

            stream = conn.getInputStream();
            return parse( stream, type, options );
        }
        finally
        {
            if (stream != null)
                stream.close();
        }
    }

    public XmlObject parse ( InputStream jiois, SchemaType type, XmlOptions options ) throws XmlException, IOException
    {
        XmlFactoryHook hook = XmlFactoryHook.ThreadContext.getHook();
        
        DigestInputStream digestStream = null;
        
        setupDigest:
        if (options != null && options.hasOption( XmlOptions.LOAD_MESSAGE_DIGEST ))
        {
            MessageDigest sha;
            
            try
            {
                sha = MessageDigest.getInstance("SHA");
            }
            catch (NoSuchAlgorithmException e)
            {
                break setupDigest;
            }

            digestStream = new DigestInputStream( jiois, sha );
            jiois = digestStream;
        }

        if (hook != null)
            return hook.parse( this, jiois, type, options );

        XmlObject result = Locale.parseToXmlObject( this, jiois, type, options );

        if (digestStream != null)
            result.documentProperties().setMessageDigest( digestStream.getMessageDigest().digest() );

        return result;
    }

    public XmlObject parse ( Reader jior, SchemaType type, XmlOptions options ) throws XmlException, IOException
    {
        XmlFactoryHook hook = XmlFactoryHook.ThreadContext.getHook();
        
        if (hook != null)
            return hook.parse( this, jior, type, options );

        return Locale.parseToXmlObject( this, jior, type, options );
    }

    public XmlObject parse ( Node node, SchemaType type, XmlOptions options ) throws XmlException
    {
        XmlFactoryHook hook = XmlFactoryHook.ThreadContext.getHook();
        
        if (hook != null)
            return hook.parse( this, node, type, options );

        return Locale.parseToXmlObject( this, node, type, options );
    }

    public XmlSaxHandler newXmlSaxHandler ( SchemaType type, XmlOptions options )
    {
        XmlFactoryHook hook = XmlFactoryHook.ThreadContext.getHook();
        
        if (hook != null)
            return hook.newXmlSaxHandler( this, type, options );

        return Locale.newSaxHandler( this, type, options );
    }

    public DOMImplementation newDomImplementation ( XmlOptions options )
    {
        return Locale.newDomImplementation( this, options );
    }

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XMLInputStream newValidatingXMLInputStream ( XMLInputStream xis, SchemaType type, XmlOptions options ) throws XmlException, XMLStreamException
    {
        return new ValidatingXMLInputStream( xis, this, type, options );
    }

    //
    //
    //

    public String compilePath ( String pathExpr )
    {
        return compilePath( pathExpr, null );
    }

    public String compilePath ( String pathExpr, XmlOptions options )
    {
        return doCompilePath( pathExpr, options );
    }

    public String compileQuery ( String queryExpr )
    {
        return compileQuery( queryExpr, null );
    }

    public String compileQuery ( String queryExpr, XmlOptions options )
    {
        return doCompileQuery( queryExpr, options );
    }

    /**
     * Utility function to load a type from a signature.
     *
     * A signature is the string you get from type.toString().
     */
    public SchemaType typeForSignature(String signature)
    {
        int end = signature.indexOf('@');
        String uri;

        if (end < 0)
        {
            uri = "";
            end = signature.length();
        }
        else
        {
            uri = signature.substring(end + 1);
        }

        List parts = new ArrayList();

        for (int index = 0; index < end; )
        {
            int nextc = signature.indexOf(':', index);
            int nextd = signature.indexOf('|', index);
            int next = (nextc < 0 ? nextd : nextd < 0 ? nextc : nextc < nextd ? nextc : nextd);
            if (next < 0 || next > end)
                next = end;
            String part = signature.substring(index, next);
            parts.add(part);
            index = next + 1;
        }

        SchemaType curType = null;

        outer: for (int i = parts.size() - 1; i >= 0; i -= 1)
        {
            String part = (String)parts.get(i);
            if (part.length() < 1)
                throw new IllegalArgumentException();
            int offset = (part.length() >= 2 && part.charAt(1) == '=') ? 2 : 1;
            cases: switch (part.charAt(0))
            {
                case 'T':
                    if (curType != null)
                        throw new IllegalArgumentException();
                    curType = findType(QNameHelper.forLNS(part.substring(offset), uri));
                    if (curType == null)
                        return null;
                    break;

                case 'D':
                    if (curType != null)
                        throw new IllegalArgumentException();
                    curType = findDocumentType(QNameHelper.forLNS(part.substring(offset), uri));
                    if (curType == null)
                        return null;
                    break;

                case 'C': // deprecated
                case 'R': // current
                    if (curType != null)
                        throw new IllegalArgumentException();
                    curType = findAttributeType(QNameHelper.forLNS(part.substring(offset), uri));
                    if (curType == null)
                        return null;
                    break;

                case 'E':
                case 'U': // distinguish qualified/unqualified TBD
                    if (curType != null)
                    {
                        if (curType.getContentType() < SchemaType.ELEMENT_CONTENT)
                            return null;
                        SchemaType[] subTypes = curType.getAnonymousTypes();
                        String localName = part.substring(offset);
                        for (int j = 0; j < subTypes.length; j++)
                        {
                            SchemaField field = subTypes[j].getContainerField();
                            if (field != null && !field.isAttribute() && field.getName().getLocalPart().equals(localName))
                            {
                                curType = subTypes[j];
                                break cases;
                            }
                        }
                        return null;
                    }
                    else
                    {
                        SchemaGlobalElement elt = findElement(QNameHelper.forLNS(part.substring(offset), uri));
                        if (elt == null)
                            return null;
                        curType = elt.getType();
                    }
                    break;

                case 'A':
                case 'Q': // distinguish qualified/unqualified TBD
                    if (curType != null)
                    {
                        if (curType.isSimpleType())
                            return null;
                        SchemaType[] subTypes = curType.getAnonymousTypes();
                        String localName = part.substring(offset);
                        for (int j = 0; j < subTypes.length; j++)
                        {
                            SchemaField field = subTypes[j].getContainerField();
                            if (field != null && field.isAttribute() && field.getName().getLocalPart().equals(localName))
                            {
                                curType = subTypes[j];
                                break cases;
                            }
                        }
                        return null;
                    }
                    else
                    {
                        SchemaGlobalAttribute attr = findAttribute(QNameHelper.forLNS(part.substring(offset), uri));
                        if (attr == null)
                            return null;
                        curType = attr.getType();
                    }
                    break;

                case 'B':
                    if (curType == null)
                    {
                        throw new IllegalArgumentException();
                    }
                    else
                    {
                        if (curType.getSimpleVariety() != SchemaType.ATOMIC)
                            return null;
                        SchemaType[] subTypes = curType.getAnonymousTypes();
                        if (subTypes.length != 1)
                            return null;
                        curType = subTypes[0];
                    }
                    break;

                case 'I':
                    if (curType == null)
                    {
                        throw new IllegalArgumentException();
                    }
                    else
                    {
                        if (curType.getSimpleVariety() != SchemaType.LIST)
                            return null;
                        SchemaType[] subTypes = curType.getAnonymousTypes();
                        if (subTypes.length != 1)
                            return null;
                        curType = subTypes[0];
                    }
                    break;

                case 'M':
                    if (curType == null)
                    {
                        throw new IllegalArgumentException();
                    }
                    else
                    {
                        int index;
                        try
                        {
                            index = Integer.parseInt(part.substring(offset));
                        }
                        catch (Exception e)
                        {
                            throw new IllegalArgumentException();
                        }

                        if (curType.getSimpleVariety() != SchemaType.UNION)
                            return null;
                        SchemaType[] subTypes = curType.getAnonymousTypes();
                        if (subTypes.length <= index)
                            return null;
                        curType = subTypes[index];
                    }
                    break;

                default:
                    throw new IllegalArgumentException();
            }
        }
        return curType;
    }
}
