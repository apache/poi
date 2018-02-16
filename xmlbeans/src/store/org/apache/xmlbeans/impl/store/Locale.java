/*   Copyright 2004-2017 The Apache Software Foundation
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

package org.apache.xmlbeans.impl.store;

import org.apache.xmlbeans.XmlErrorCodes;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.DTDHandler;

import java.util.HashMap;
import java.util.Map;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;
import java.lang.ref.PhantomReference;
import java.lang.ref.SoftReference;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import org.apache.xmlbeans.xml.stream.Attribute;
import org.apache.xmlbeans.xml.stream.AttributeIterator;
import org.apache.xmlbeans.xml.stream.CharacterData;
import org.apache.xmlbeans.xml.stream.ProcessingInstruction;
import org.apache.xmlbeans.xml.stream.Space;
import org.apache.xmlbeans.xml.stream.StartDocument;
import org.apache.xmlbeans.xml.stream.StartElement;
import org.apache.xmlbeans.xml.stream.XMLEvent;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLName;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.impl.common.XMLNameHelper;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.XmlLocale;
import org.apache.xmlbeans.impl.common.ResolverUtil;
import org.apache.xmlbeans.impl.common.SystemCache;

import org.apache.xmlbeans.impl.store.Saaj.SaajCallback;

import org.apache.xmlbeans.impl.store.DomImpl.Dom;
import org.apache.xmlbeans.impl.store.DomImpl.TextNode;
import org.apache.xmlbeans.impl.store.DomImpl.CdataNode;
import org.apache.xmlbeans.impl.store.DomImpl.SaajTextNode;
import org.apache.xmlbeans.impl.store.DomImpl.SaajCdataNode;

import org.apache.xmlbeans.impl.store.Cur.Locations;

import org.apache.xmlbeans.CDataBookmark;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlLineNumber;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.XmlBookmark;
import org.apache.xmlbeans.XmlSaxHandler;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlTokenSource;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.QNameCache;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.XmlDocumentProperties;

import org.apache.xmlbeans.impl.values.TypeStore;
import org.apache.xmlbeans.impl.values.TypeStoreUser;
import org.apache.xmlbeans.impl.values.TypeStoreUserFactory;

public final class Locale
    implements DOMImplementation, SaajCallback, XmlLocale
{
    static final int ROOT = Cur.ROOT;
    static final int ELEM = Cur.ELEM;
    static final int ATTR = Cur.ATTR;
    static final int COMMENT = Cur.COMMENT;
    static final int PROCINST = Cur.PROCINST;
    static final int TEXT = Cur.TEXT;

    static final int WS_UNSPECIFIED = TypeStore.WS_UNSPECIFIED;
    static final int WS_PRESERVE = TypeStore.WS_PRESERVE;
    static final int WS_REPLACE = TypeStore.WS_REPLACE;
    static final int WS_COLLAPSE = TypeStore.WS_COLLAPSE;

    static final String _xsi = "http://www.w3.org/2001/XMLSchema-instance";
    static final String _schema = "http://www.w3.org/2001/XMLSchema";
    static final String _openFragUri = "http://www.openuri.org/fragment";
    static final String _xml1998Uri = "http://www.w3.org/XML/1998/namespace";
    static final String _xmlnsUri = "http://www.w3.org/2000/xmlns/";

    static final QName _xsiNil = new QName(_xsi, "nil", "xsi");
    static final QName _xsiType = new QName(_xsi, "type", "xsi");
    static final QName _xsiLoc = new QName(_xsi, "schemaLocation", "xsi");
    static final QName _xsiNoLoc = new QName(_xsi, "noNamespaceSchemaLocation",
        "xsi");
    static final QName _openuriFragment = new QName(_openFragUri, "fragment",
        "frag");
    static final QName _xmlFragment = new QName("xml-fragment");

    private Locale(SchemaTypeLoader stl, XmlOptions options)
    {
        options = XmlOptions.maskNull(options);

        //
        //
        //

        // TODO - add option for no=sync, or make it all thread safe
        //
        // Also - have a thread local setting for thread safety?  .. Perhaps something
        // in the type loader which defines whether ot not sync is on????
        
        _noSync = options.hasOption(XmlOptions.UNSYNCHRONIZED);

        _tempFrames = new Cur[_numTempFramesLeft = 8];

        // BUGBUG - this cannot be thread local ....
        // BUGBUG - this cannot be thread local ....
        // BUGBUG - this cannot be thread local .... uhh what, again?
        // 
        // Lazy create this (loading up a locale should use the thread locale one)
        // same goes for the qname factory .. use thread local for hte most part when loading
        
        _qnameFactory = new DefaultQNameFactory(); //new LocalDocumentQNameFactory();

        _locations = new Locations(this);

        _schemaTypeLoader = stl;

        _validateOnSet = options.hasOption(XmlOptions.VALIDATE_ON_SET);
        
        //
        // Check for Saaj implementation request
        //
        
        Object saajObj = options.get(Saaj.SAAJ_IMPL);

        if (saajObj != null)
        {
            if (!(saajObj instanceof Saaj))
                throw new IllegalStateException(
                    "Saaj impl not correct type: " + saajObj);

            _saaj = (Saaj) saajObj;

            _saaj.setCallback(this);
        }
    }

    //
    //
    //

    public static final String USE_SAME_LOCALE = "USE_SAME_LOCALE";
    /**
     * This option is checked in XmlObjectBase._copy(XmlOptions), the locale is used as the synchronization domain.
     * useNewLocale = true: copy will use a new locale, false: copy will use the same locale as the source
     * @deprecated Replace usages with CopyUseNewSynchronizationDomain option
     * @see org.apache.xmlbeans.XmlOptions#setCopyUseNewSynchronizationDomain(boolean)
     */
    public static final String COPY_USE_NEW_LOCALE             = "COPY_USE_NEW_LOCALE";

    static Locale getLocale(SchemaTypeLoader stl, XmlOptions options)
    {
        if (stl == null)
            stl = XmlBeans.getContextTypeLoader();

        options = XmlOptions.maskNull(options);

        Locale l = null;

        if (options.hasOption(USE_SAME_LOCALE))
        {
            Object source = options.get(USE_SAME_LOCALE);

            if (source instanceof Locale)
                l = (Locale) source;
            else if (source instanceof XmlTokenSource)
                l = (Locale) ((XmlTokenSource) source).monitor();
            else
                throw new IllegalArgumentException(
                    "Source locale not understood: " + source);

            if (l._schemaTypeLoader != stl)
                throw new IllegalArgumentException(
                    "Source locale does not support same schema type loader");

            if (l._saaj != null && l._saaj != options.get(Saaj.SAAJ_IMPL))
                throw new IllegalArgumentException(
                    "Source locale does not support same saaj");

            if (l._validateOnSet &&
                !options.hasOption(XmlOptions.VALIDATE_ON_SET))
                throw new IllegalArgumentException(
                    "Source locale does not support same validate on set");

            // TODO - other things to check?
        }
        else
            l = new Locale(stl, options);

        return l;
    }

    //
    //
    //
    
    static void associateSourceName(Cur c, XmlOptions options)
    {
        String sourceName = (String) XmlOptions.safeGet(options,
            XmlOptions.DOCUMENT_SOURCE_NAME);

        if (sourceName != null)
            getDocProps(c, true).setSourceName(sourceName);
    }

    //
    //
    //
    
    static void autoTypeDocument(Cur c, SchemaType requestedType,
        XmlOptions options)
        throws XmlException
    {
        assert c.isRoot();

        // The type in the options overrides all sniffing

        options = XmlOptions.maskNull(options);

        SchemaType optionType = (SchemaType) options.get(
            XmlOptions.DOCUMENT_TYPE);

        if (optionType != null)
        {
            c.setType(optionType);
            return;
        }

        SchemaType type = null;

        // An xsi:type can be used to pick a type out of the loader, or used to refine
        // a type with a name.

        if (requestedType == null || requestedType.getName() != null)
        {
            QName xsiTypeName = c.getXsiTypeName();

            SchemaType xsiSchemaType =
                xsiTypeName == null ?
                null : c._locale._schemaTypeLoader.findType(xsiTypeName);

            if (requestedType == null ||
                requestedType.isAssignableFrom(xsiSchemaType))
                type = xsiSchemaType;
        }

        // Look for a document element to establish type
        
        if (type == null &&
            (requestedType == null || requestedType.isDocumentType()))
        {
            assert c.isRoot();

            c.push();

            QName docElemName =
                !c.hasAttrs() && Locale.toFirstChildElement(c) &&
                !Locale.toNextSiblingElement(c)
                ? c.getName() : null;

            c.pop();

            if (docElemName != null)
            {
                type =
                    c._locale._schemaTypeLoader.findDocumentType(docElemName);

                if (type != null && requestedType != null)
                {
                    QName requesteddocElemNameName = requestedType.getDocumentElementName();

                    if (!requesteddocElemNameName.equals(docElemName) &&
                        !requestedType.isValidSubstitution(docElemName))
                    {
                        throw
                            new XmlException("Element " +
                            QNameHelper.pretty(docElemName) +
                            " is not a valid " +
                            QNameHelper.pretty(requesteddocElemNameName) +
                            " document or a valid substitution.");
                    }
                }
            }
        }

        if (type == null && requestedType == null)
        {
            c.push();

            type =
                Locale.toFirstNormalAttr(c) && !Locale.toNextNormalAttr(c)
                ?
                c._locale._schemaTypeLoader.findAttributeType(c.getName()) :
                null;

            c.pop();
        }

        if (type == null)
            type = requestedType;

        if (type == null)
            type = XmlBeans.NO_TYPE;

        c.setType(type);

        if (requestedType != null)
        {
            if (type.isDocumentType())
                verifyDocumentType(c, type.getDocumentElementName());
            else if (type.isAttributeType())
                verifyAttributeType(c, type.getAttributeTypeAttributeName());
        }
    }

    private static boolean namespacesSame(QName n1, QName n2)
    {
        if (n1 == n2)
            return true;

        if (n1 == null || n2 == null)
            return false;

        if (n1.getNamespaceURI() == n2.getNamespaceURI())
            return true;

        if (n1.getNamespaceURI() == null || n2.getNamespaceURI() == null)
            return false;

        return n1.getNamespaceURI().equals(n2.getNamespaceURI());
    }

    private static void addNamespace(StringBuffer sb, QName name)
    {
        if (name.getNamespaceURI() == null)
            sb.append("<no namespace>");
        else
        {
            sb.append("\"");
            sb.append(name.getNamespaceURI());
            sb.append("\"");
        }
    }

    private static void verifyDocumentType(Cur c, QName docElemName)
        throws XmlException
    {
        assert c.isRoot();

        c.push();

        try
        {
            StringBuffer sb = null;

            if (!Locale.toFirstChildElement(c) ||
                Locale.toNextSiblingElement(c))
            {
                sb = new StringBuffer();

                sb.append("The document is not a ");
                sb.append(QNameHelper.pretty(docElemName));
                sb.append(
                    c.isRoot() ?
                    ": no document element" : ": multiple document elements");
            }
            else
            {
                QName name = c.getName();

                if (!name.equals(docElemName))
                {
                    sb = new StringBuffer();

                    sb.append("The document is not a ");
                    sb.append(QNameHelper.pretty(docElemName));

                    if (docElemName.getLocalPart().equals(name.getLocalPart()))
                    {
                        sb.append(": document element namespace mismatch ");
                        sb.append("expected ");
                        addNamespace(sb, docElemName);
                        sb.append(" got ");
                        addNamespace(sb, name);
                    }
                    else if (namespacesSame(docElemName, name))
                    {
                        sb.append(": document element local name mismatch ");
                        sb.append("expected " + docElemName.getLocalPart());
                        sb.append(" got " + name.getLocalPart());
                    }
                    else
                    {
                        sb.append(": document element mismatch ");
                        sb.append("got ");
                        sb.append(QNameHelper.pretty(name));
                    }
                }
            }

            if (sb != null)
            {
                XmlError err = XmlError.forCursor(sb.toString(),
                    new Cursor(c));
                throw new XmlException(err.toString(), null, err);
            }
        }
        finally
        {
            c.pop();
        }
    }

    private static void verifyAttributeType(Cur c, QName attrName)
        throws XmlException
    {
        assert c.isRoot();

        c.push();

        try
        {
            StringBuffer sb = null;

            if (!Locale.toFirstNormalAttr(c) || Locale.toNextNormalAttr(c))
            {
                sb = new StringBuffer();

                sb.append("The document is not a ");
                sb.append(QNameHelper.pretty(attrName));
                sb.append(
                    c.isRoot() ? ": no attributes" : ": multiple attributes");
            }
            else
            {
                QName name = c.getName();

                if (!name.equals(attrName))
                {
                    sb = new StringBuffer();

                    sb.append("The document is not a ");
                    sb.append(QNameHelper.pretty(attrName));

                    if (attrName.getLocalPart().equals(name.getLocalPart()))
                    {
                        sb.append(": attribute namespace mismatch ");
                        sb.append("expected ");
                        addNamespace(sb, attrName);
                        sb.append(" got ");
                        addNamespace(sb, name);
                    }
                    else if (namespacesSame(attrName, name))
                    {
                        sb.append(": attribute local name mismatch ");
                        sb.append("expected " + attrName.getLocalPart());
                        sb.append(" got " + name.getLocalPart());
                    }
                    else
                    {
                        sb.append(": attribute element mismatch ");
                        sb.append("got ");
                        sb.append(QNameHelper.pretty(name));
                    }
                }
            }

            if (sb != null)
            {
                XmlError err = XmlError.forCursor(sb.toString(),
                    new Cursor(c));
                throw new XmlException(err.toString(), null, err);
            }
        }
        finally
        {
            c.pop();
        }
    }

    static boolean isFragmentQName(QName name)
    {
        return name.equals(Locale._openuriFragment) ||
            name.equals(Locale._xmlFragment);
    }

    static boolean isFragment(Cur start, Cur end)
    {
        assert !end.isAttr();

        start.push();
        end.push();

        int numDocElems = 0;
        boolean isFrag = false;

        while (!start.isSamePos(end))
        {
            int k = start.kind();

            if (k == ATTR)
                break;

            if (k == TEXT && !isWhiteSpace(start.getCharsAsString(-1)))
            {
                isFrag = true;
                break;
            }

            if (k == ELEM && ++numDocElems > 1)
            {
                isFrag = true;
                break;
            }

            // Move to next token

            assert k != ATTR;

            if (k != TEXT)
                start.toEnd();

            start.next();
        }

        start.pop();
        end.pop();

        return isFrag || numDocElems != 1;
    }
    
    //
    //
    //
    
    public static XmlObject newInstance(SchemaTypeLoader stl, SchemaType type,
        XmlOptions options)
    {
        Locale l = getLocale(stl, options);

        if (l.noSync())
        {
            l.enter();
            try
            {
                return l.newInstance(type, options);
            }
            finally
            {
                l.exit();
            }
        }
        else
            synchronized (l)
            {
                l.enter();
                try
                {
                    return l.newInstance(type, options);
                }
                finally
                {
                    l.exit();
                }
            }
    }

    private XmlObject newInstance(SchemaType type, XmlOptions options)
    {
        options = XmlOptions.maskNull(options);

        Cur c = tempCur();


        SchemaType sType = (SchemaType) options.get(XmlOptions.DOCUMENT_TYPE);

        if (sType == null)
            sType = type == null ? XmlObject.type : type;
        if (sType.isDocumentType())
            c.createDomDocumentRoot();
        else
             c.createRoot();
        c.setType(sType);
        
        XmlObject x = (XmlObject) c.getUser();

        c.release();

        return x;
    }

    //
    //
    //

    public static DOMImplementation newDomImplementation(SchemaTypeLoader stl,
        XmlOptions options)
    {
        return (DOMImplementation) getLocale(stl, options);
    }

    //
    //
    //

    public static XmlObject parseToXmlObject(SchemaTypeLoader stl,
        String xmlText, SchemaType type, XmlOptions options)
        throws XmlException
    {
        Locale l = getLocale(stl, options);

        if (l.noSync())
        {
            l.enter();
            try
            {
                return l.parseToXmlObject(xmlText, type, options);
            }
            finally
            {
                l.exit();
            }
        }
        else
            synchronized (l)
            {
                l.enter();
                try
                {
                    return l.parseToXmlObject(xmlText, type, options);
                }
                finally
                {
                    l.exit();
                }
            }
    }

    private XmlObject parseToXmlObject(String xmlText, SchemaType type,
        XmlOptions options)
        throws XmlException
    {
        Cur c = parse(xmlText, type, options);

        XmlObject x = (XmlObject) c.getUser();

        c.release();

        return x;
    }

    Cur parse(String s, SchemaType type, XmlOptions options)
        throws XmlException
    {
        Reader r = new StringReader(s);

        try
        {
            Cur c = getSaxLoader(options).load(this, new InputSource(r),
                options);

            autoTypeDocument(c, type, options);

            return c;
        }
        catch (IOException e)
        {
            assert false: "StringReader should not throw IOException";

            throw new XmlException(e.getMessage(), e);
        }
        finally
        {
            try
            {
                r.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    //
    //
    //

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public static XmlObject parseToXmlObject(SchemaTypeLoader stl,
        XMLInputStream xis, SchemaType type, XmlOptions options)
        throws XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException
    {
        Locale l = getLocale(stl, options);

        if (l.noSync())
        {
            l.enter();
            try
            {
                return l.parseToXmlObject(xis, type, options);
            }
            finally
            {
                l.exit();
            }
        }
        else
            synchronized (l)
            {
                l.enter();
                try
                {
                    return l.parseToXmlObject(xis, type, options);
                }
                finally
                {
                    l.exit();
                }
            }
    }

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    public XmlObject parseToXmlObject(XMLInputStream xis, SchemaType type,
        XmlOptions options)
        throws XmlException, org.apache.xmlbeans.xml.stream.XMLStreamException
    {
        Cur c;

        try
        {
            c = loadXMLInputStream(xis, options);
        }
        catch (org.apache.xmlbeans.xml.stream.XMLStreamException e)
        {
            throw new XmlException(e.getMessage(), e);
        }

        autoTypeDocument(c, type, options);

        XmlObject x = (XmlObject) c.getUser();

        c.release();

        return x;
    }

    //
    //
    //

    public static XmlObject parseToXmlObject(SchemaTypeLoader stl,
        XMLStreamReader xsr, SchemaType type, XmlOptions options)
        throws XmlException
    {
        Locale l = getLocale(stl, options);

        if (l.noSync())
        {
            l.enter();
            try
            {
                return l.parseToXmlObject(xsr, type, options);
            }
            finally
            {
                l.exit();
            }
        }
        else
            synchronized (l)
            {
                l.enter();
                try
                {
                    return l.parseToXmlObject(xsr, type, options);
                }
                finally
                {
                    l.exit();
                }
            }
    }

    public XmlObject parseToXmlObject(XMLStreamReader xsr, SchemaType type,
        XmlOptions options)
        throws XmlException
    {
        Cur c;

        try
        {
            c = loadXMLStreamReader(xsr, options);
        }
        catch (XMLStreamException e)
        {
            throw new XmlException(e.getMessage(), e);
        }

        autoTypeDocument(c, type, options);

        XmlObject x = (XmlObject) c.getUser();

        c.release();

        return x;
    }

    private static void lineNumber(XMLEvent xe, LoadContext context)
    {
        org.apache.xmlbeans.xml.stream.Location loc = xe.getLocation();

        if (loc != null)
            context.lineNumber(loc.getLineNumber(), loc.getColumnNumber(), -1);
    }

    private static void lineNumber(XMLStreamReader xsr, LoadContext context)
    {
        javax.xml.stream.Location loc = xsr.getLocation();

        if (loc != null)
        {
            context.lineNumber(loc.getLineNumber(), loc.getColumnNumber(),
                loc.getCharacterOffset());
        }
    }

    private void doAttributes(XMLStreamReader xsr, LoadContext context)
    {
        int n = xsr.getAttributeCount();

        for (int a = 0; a < n; a++)
        {
            context.attr(xsr.getAttributeLocalName(a),
                xsr.getAttributeNamespace(a),
                xsr.getAttributePrefix(a),
                xsr.getAttributeValue(a));
        }
    }

    private void doNamespaces(XMLStreamReader xsr, LoadContext context)
    {
        int n = xsr.getNamespaceCount();

        for (int a = 0; a < n; a++)
        {
            String prefix = xsr.getNamespacePrefix(a);

            if (prefix == null || prefix.length() == 0)
                context.attr("xmlns", _xmlnsUri, null,
                    xsr.getNamespaceURI(a));
            else
                context.attr(prefix, _xmlnsUri, "xmlns",
                    xsr.getNamespaceURI(a));
        }

    }

    /**
     * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
     */
    private Cur loadXMLInputStream(XMLInputStream xis, XmlOptions options)
        throws org.apache.xmlbeans.xml.stream.XMLStreamException
    {
        options = XmlOptions.maskNull(options);

        boolean lineNums = options.hasOption(XmlOptions.LOAD_LINE_NUMBERS);

        XMLEvent x = xis.peek();

        if (x != null && x.getType() == XMLEvent.START_ELEMENT)
        {
            Map nsMap = ((StartElement) x).getNamespaceMap();

            if (nsMap != null && nsMap.size() > 0)
            {
                Map namespaces = new HashMap();

                namespaces.putAll(nsMap);

                options = new XmlOptions(options);

                options.put(XmlOptions.LOAD_ADDITIONAL_NAMESPACES, namespaces);
            }
        }

        String systemId = null;
        String encoding = null;
        String version = null;
        boolean standAlone = true;

        LoadContext context = new Cur.CurLoadContext(this, options);

        events:
        for (XMLEvent xe = xis.next(); xe != null; xe = xis.next())
        {
            switch (xe.getType())
            {
            case XMLEvent.START_DOCUMENT:
                StartDocument doc = (StartDocument) xe;

                systemId = doc.getSystemId();
                encoding = doc.getCharacterEncodingScheme();
                version = doc.getVersion();
                standAlone = doc.isStandalone();
                standAlone = doc.isStandalone();

                if (lineNums)
                    lineNumber(xe, context);

                break;

            case XMLEvent.END_DOCUMENT:
                if (lineNums)
                    lineNumber(xe, context);

                break events;

            case XMLEvent.NULL_ELEMENT:
                if (!xis.hasNext())
                    break events;
                break;

            case XMLEvent.START_ELEMENT:
                context.startElement(XMLNameHelper.getQName(xe.getName()));

                if (lineNums)
                    lineNumber(xe, context);

                for (AttributeIterator ai = ((StartElement) xe).getAttributes();
                     ai.hasNext();)
                {
                    Attribute attr = ai.next();

                    context.attr(XMLNameHelper.getQName(attr.getName()),
                        attr.getValue());
                }

                for (AttributeIterator ai = ((StartElement) xe).getNamespaces()
                    ; ai.hasNext();)
                {
                    Attribute attr = ai.next();

                    XMLName name = attr.getName();
                    String local = name.getLocalName();

                    if (name.getPrefix() == null && local.equals("xmlns"))
                        local = "";

                    context.xmlns(local, attr.getValue());
                }

                break;

            case XMLEvent.END_ELEMENT:
                context.endElement();

                if (lineNums)
                    lineNumber(xe, context);

                break;

            case XMLEvent.SPACE:
                if (((Space) xe).ignorable())
                    break;

                // Fall through

            case XMLEvent.CHARACTER_DATA:
                CharacterData cd = (CharacterData) xe;

                if (cd.hasContent())
                {
                    context.text(cd.getContent());

                    if (lineNums)
                        lineNumber(xe, context);
                }

                break;

            case XMLEvent.COMMENT:
                org.apache.xmlbeans.xml.stream.Comment comment =
                    (org.apache.xmlbeans.xml.stream.Comment) xe;

                if (comment.hasContent())
                {
                    context.comment(comment.getContent());

                    if (lineNums)
                        lineNumber(xe, context);
                }

                break;

            case XMLEvent.PROCESSING_INSTRUCTION:
                ProcessingInstruction procInstr = (ProcessingInstruction) xe;

                context.procInst(procInstr.getTarget(), procInstr.getData());

                if (lineNums)
                    lineNumber(xe, context);

                break;

                // These are ignored
            case XMLEvent.ENTITY_REFERENCE:
            case XMLEvent.START_PREFIX_MAPPING:
            case XMLEvent.END_PREFIX_MAPPING:
            case XMLEvent.CHANGE_PREFIX_MAPPING:
            case XMLEvent.XML_EVENT:
                break;

            default :
                throw new RuntimeException(
                    "Unhandled xml event type: " + xe.getTypeAsString());
            }
        }

        Cur c = context.finish();

        associateSourceName(c, options);

        XmlDocumentProperties props = getDocProps(c, true);

        props.setDoctypeSystemId(systemId);
        props.setEncoding(encoding);
        props.setVersion(version);
        props.setStandalone(standAlone);

        return c;
    }

    private Cur loadXMLStreamReader(XMLStreamReader xsr, XmlOptions options)
        throws XMLStreamException
    {
        options = XmlOptions.maskNull(options);

        boolean lineNums = options.hasOption(XmlOptions.LOAD_LINE_NUMBERS);

        String encoding = null, version = null;
        boolean standAlone = false;

        LoadContext context = new Cur.CurLoadContext(this, options);
        int depth = 0;

        events:
        for (int eventType = xsr.getEventType(); ; eventType = xsr.next())
        {
            switch (eventType)
            {
            case XMLStreamReader.START_DOCUMENT:
                {
                    depth++;

                    encoding = xsr.getCharacterEncodingScheme();
                    version = xsr.getVersion();
                    standAlone = xsr.isStandalone();

                    if (lineNums)
                        lineNumber(xsr, context);

                    break;
                }

            case XMLStreamReader.END_DOCUMENT:
                {
                    depth--;

                    if (lineNums)
                        lineNumber(xsr, context);

                    break events;
                }

            case XMLStreamReader.START_ELEMENT:
                {
                    depth++;
                    context.startElement(xsr.getName());

                    if (lineNums)
                        lineNumber(xsr, context);

                    doAttributes(xsr, context);
                    doNamespaces(xsr, context);

                    break;
                }

            case XMLStreamReader.END_ELEMENT:
                {
                    depth--;
                    context.endElement();

                    if (lineNums)
                        lineNumber(xsr, context);

                    break;
                }

            case XMLStreamReader.CHARACTERS:
            case XMLStreamReader.CDATA:
                {
                    context.text(xsr.getTextCharacters(), xsr.getTextStart(),
                        xsr.getTextLength());

                    if (lineNums)
                        lineNumber(xsr, context);

                    break;
                }

            case XMLStreamReader.COMMENT:
                {
                    String comment = xsr.getText();

                    context.comment(comment);

                    if (lineNums)
                        lineNumber(xsr, context);

                    break;
                }

            case XMLStreamReader.PROCESSING_INSTRUCTION:
                {
                    context.procInst(xsr.getPITarget(), xsr.getPIData());

                    if (lineNums)
                        lineNumber(xsr, context);

                    break;
                }

            case XMLStreamReader.ATTRIBUTE:
                {
                    doAttributes(xsr, context);
                    break;
                }

            case XMLStreamReader.NAMESPACE:
                {
                    doNamespaces(xsr, context);
                    break;
                }

            case XMLStreamReader.ENTITY_REFERENCE:
                {
                    context.text(xsr.getText());
                    break;
                }

            case XMLStreamReader.SPACE:
            case XMLStreamReader.DTD:
                break;

            default :
                throw new RuntimeException(
                    "Unhandled xml event type: " + eventType);
            }

            if (!xsr.hasNext() || depth <= 0)
                break;
        }

        Cur c = context.finish();

        associateSourceName(c, options);

        XmlDocumentProperties props = getDocProps(c, true);

        props.setEncoding(encoding);
        props.setVersion(version);
        props.setStandalone(standAlone);

        return c;
    }

    //
    //
    //

    public static XmlObject parseToXmlObject(SchemaTypeLoader stl,
        InputStream is, SchemaType type, XmlOptions options)
        throws XmlException, IOException
    {
        Locale l = getLocale(stl, options);

        if (l.noSync())
        {
            l.enter();
            try
            {
                return l.parseToXmlObject(is, type, options);
            }
            finally
            {
                l.exit();
            }
        }
        else
            synchronized (l)
            {
                l.enter();
                try
                {
                    return l.parseToXmlObject(is, type, options);
                }
                finally
                {
                    l.exit();
                }
            }
    }

    private XmlObject parseToXmlObject(InputStream is, SchemaType type,
        XmlOptions options)
        throws XmlException, IOException
    {
        Cur c = getSaxLoader(options).load(this, new InputSource(is),
            options);

        autoTypeDocument(c, type, options);

        XmlObject x = (XmlObject) c.getUser();

        c.release();

        return x;
    }

    //
    //
    //

    public static XmlObject parseToXmlObject(SchemaTypeLoader stl,
        Reader reader, SchemaType type, XmlOptions options)
        throws XmlException, IOException
    {
        Locale l = getLocale(stl, options);

        if (l.noSync())
        {
            l.enter();
            try
            {
                return l.parseToXmlObject(reader, type, options);
            }
            finally
            {
                l.exit();
            }
        }
        else
            synchronized (l)
            {
                l.enter();
                try
                {
                    return l.parseToXmlObject(reader, type, options);
                }
                finally
                {
                    l.exit();
                }
            }
    }

    private XmlObject parseToXmlObject(Reader reader, SchemaType type,
        XmlOptions options)
        throws XmlException, IOException
    {
        Cur c = getSaxLoader(options).load(this, new InputSource(reader),
            options);

        autoTypeDocument(c, type, options);

        XmlObject x = (XmlObject) c.getUser();

        c.release();

        return x;
    }

    //
    //
    //

    public static XmlObject parseToXmlObject(SchemaTypeLoader stl, Node node,
        SchemaType type, XmlOptions options)
        throws XmlException
    {
        Locale l = getLocale(stl, options);

        if (l.noSync())
        {
            l.enter();
            try
            {
                return l.parseToXmlObject(node, type, options);
            }
            finally
            {
                l.exit();
            }
        }
        else
            synchronized (l)
            {
                l.enter();
                try
                {
                    return l.parseToXmlObject(node, type, options);
                }
                finally
                {
                    l.exit();
                }
            }
    }

    public XmlObject parseToXmlObject(Node node, SchemaType type,
        XmlOptions options)
        throws XmlException
    {
        LoadContext context = new Cur.CurLoadContext(this, options);

        loadNode(node, context);

        Cur c = context.finish();

        associateSourceName(c, options);

        autoTypeDocument(c, type, options);

        XmlObject x = (XmlObject) c.getUser();

        c.release();

        return x;
    }

    private void loadNodeChildren(Node n, LoadContext context)
    {
        for (Node c = n.getFirstChild(); c != null; c = c.getNextSibling())
            loadNode(c, context);
    }

    void loadNode(Node n, LoadContext context)
    {
        switch (n.getNodeType())
        {
        case Node.DOCUMENT_NODE:
        case Node.DOCUMENT_FRAGMENT_NODE:
        case Node.ENTITY_REFERENCE_NODE:
            {
                loadNodeChildren(n, context);

                break;
            }
        case Node.ELEMENT_NODE:
            {
                context.startElement(
                    makeQualifiedQName(n.getNamespaceURI(), n.getNodeName()));

                NamedNodeMap attrs = n.getAttributes();

                for (int i = 0; i < attrs.getLength(); i++)
                {
                    Node a = attrs.item(i);

                    String attrName = a.getNodeName();
                    String attrValue = a.getNodeValue();

                    if (attrName.toLowerCase().startsWith("xmlns"))
                    {
                        if (attrName.length() == 5)
                            context.xmlns(null, attrValue);
                        else
                            context.xmlns(attrName.substring(6), attrValue);
                    }
                    else
                        context.attr(
                            makeQualifiedQName(a.getNamespaceURI(), attrName),
                            attrValue);
                }

                loadNodeChildren(n, context);

                context.endElement();

                break;
            }
        case Node.TEXT_NODE:
        case Node.CDATA_SECTION_NODE:
            {
                context.text(n.getNodeValue());
                break;
            }
        case Node.COMMENT_NODE:
            {
                context.comment(n.getNodeValue());
                break;
            }
        case Node.PROCESSING_INSTRUCTION_NODE:
            {
                context.procInst(n.getNodeName(), n.getNodeValue());
                break;
            }
        case Node.DOCUMENT_TYPE_NODE:
        case Node.ENTITY_NODE:
        case Node.NOTATION_NODE:
        case Node.ATTRIBUTE_NODE:
            {
                throw new RuntimeException("Unexpected node");
            }
        }
    }

    //
    //
    //

    private class XmlSaxHandlerImpl
        extends SaxHandler
        implements XmlSaxHandler
    {
        XmlSaxHandlerImpl(Locale l, SchemaType type, XmlOptions options)
        {
            super(null);

            _options = options;
            _type = type;

            // Because SAX loading is not atomic with respect to XmlBeans, I can't use the default
            // thread local CharUtil.  Instruct the SaxHandler (and the LoadContext, eventually)
            // to use the Locale specific CharUtil.

            XmlOptions saxHandlerOptions = new XmlOptions(options);
            saxHandlerOptions.put(Cur.LOAD_USE_LOCALE_CHAR_UTIL);

            initSaxHandler(l, saxHandlerOptions);
        }

        public ContentHandler getContentHandler()
        {
            return _context == null ? null : this;
        }

        public LexicalHandler getLexicalHandler()
        {
            return _context == null ? null : this;
        }

        public void bookmarkLastEvent(XmlBookmark mark)
        {
            _context.bookmarkLastNonAttr(mark);
        }

        public void bookmarkLastAttr(QName attrName, XmlBookmark mark)
        {
            _context.bookmarkLastAttr(attrName, mark);
        }

        public XmlObject getObject()
            throws XmlException
        {
            if (_context == null)
                return null;

            _locale.enter();

            try
            {
                Cur c = _context.finish();

                autoTypeDocument(c, _type, _options);

                XmlObject x = (XmlObject) c.getUser();

                c.release();

                _context = null;

                return x;
            }
            finally
            {
                _locale.exit();
            }
        }

        private SchemaType _type;
        private XmlOptions _options;
    }

    public static XmlSaxHandler newSaxHandler(SchemaTypeLoader stl,
        SchemaType type, XmlOptions options)
    {
        Locale l = getLocale(stl, options);

        if (l.noSync())
        {
            l.enter();
            try
            {
                return l.newSaxHandler(type, options);
            }
            finally
            {
                l.exit();
            }
        }
        else
            synchronized (l)
            {
                l.enter();
                try
                {
                    return l.newSaxHandler(type, options);
                }
                finally
                {
                    l.exit();
                }
            }
    }

    public XmlSaxHandler newSaxHandler(SchemaType type, XmlOptions options)
    {
        return new XmlSaxHandlerImpl(this, type, options);
    }

    // TODO (ericvas ) - have a qname factory here so that the same factory may be
    // used by the parser.  This factory would probably come from my
    // high speed parser.  Otherwise, use a thread local on

    QName makeQName(String uri, String localPart)
    {
        assert localPart != null && localPart.length() > 0;
        // TODO - make sure name is a well formed name?

        return _qnameFactory.getQName(uri, localPart);
    }

    QName makeQNameNoCheck(String uri, String localPart)
    {
        return _qnameFactory.getQName(uri, localPart);
    }

    QName makeQName(String uri, String local, String prefix)
    {
        return _qnameFactory.getQName(uri, local, prefix == null ? "" : prefix);
    }

    QName makeQualifiedQName(String uri, String qname)
    {
        if (qname == null)
            qname = "";

        int i = qname.indexOf(':');

        return i < 0
            ?
            _qnameFactory.getQName(uri, qname)
            :
            _qnameFactory.getQName(uri, qname.substring(i + 1),
                qname.substring(0, i));
    }

    static private class DocProps
        extends XmlDocumentProperties
    {
        private HashMap _map = new HashMap();

        public Object put(Object key, Object value)
        {
            return _map.put(key, value);
        }

        public Object get(Object key)
        {
            return _map.get(key);
        }

        public Object remove(Object key)
        {
            return _map.remove(key);
        }
    }

    static XmlDocumentProperties getDocProps(Cur c, boolean ensure)
    {
        c.push();

        while (c.toParent())
            ;

        DocProps props = (DocProps) c.getBookmark(DocProps.class);

        if (props == null && ensure)
            c.setBookmark(DocProps.class, props = new DocProps());

        c.pop();

        return props;
    }

    interface ChangeListener
    {
        void notifyChange();

        void setNextChangeListener(ChangeListener listener);

        ChangeListener getNextChangeListener();
    }

    void registerForChange(ChangeListener listener)
    {
        if (listener.getNextChangeListener() == null)
        {
            if (_changeListeners == null)
                listener.setNextChangeListener(listener);
            else
                listener.setNextChangeListener(_changeListeners);

            _changeListeners = listener;
        }
    }

    void notifyChange()
    {
        // First, notify the registered listeners ...

        while (_changeListeners != null)
        {
            _changeListeners.notifyChange();

            if (_changeListeners.getNextChangeListener() == _changeListeners)
                _changeListeners.setNextChangeListener(null);

            ChangeListener next = _changeListeners.getNextChangeListener();

            _changeListeners.setNextChangeListener(null);

            _changeListeners = next;
        }

        // Then, prepare for the change in a locale specific way.  Need to create real Curs for
        // 'virtual' Curs in Locations

        _locations.notifyChange();
    }

    //
    // Cursor helpers
    //

    static String getTextValue(Cur c)
    {
        assert c.isNode();

        if (!c.hasChildren())
            return c.getValueAsString();

        StringBuffer sb = new StringBuffer();

        c.push();

        for (c.next(); !c.isAtEndOfLastPush(); c.next())
            if (c.isText())
            {
                if ( (c._xobj.isComment() || c._xobj.isProcinst() ) && c._pos<c._xobj._cchValue )
                    continue;
                CharUtil.getString(sb, c.getChars(-1), c._offSrc, c._cchSrc);
            }

        c.pop();

        return sb.toString();
    }

    static int getTextValue(Cur c, int wsr, char[] chars, int off, int maxCch)
    {
        // TODO - hack impl for now ... improve

        assert c.isNode();

        String s = c._xobj.getValueAsString(wsr);

        int n = s.length();

        if (n > maxCch)
            n = maxCch;

        if (n <= 0)
            return 0;

        s.getChars(0, n, chars, off);

        return n;
    }

    static String applyWhiteSpaceRule(String s, int wsr)
    {
        int l = s == null ? 0 : s.length();

        if (l == 0 || wsr == WS_PRESERVE)
            return s;

        char ch;

        if (wsr == WS_REPLACE)
        {
            for (int i = 0; i < l; i++)
                if ((ch = s.charAt(i)) == '\n' || ch == '\r' || ch == '\t')
                    return processWhiteSpaceRule(s, wsr);
        }
        else if (wsr == Locale.WS_COLLAPSE)
        {
            if (CharUtil.isWhiteSpace(s.charAt(0)) ||
                CharUtil.isWhiteSpace(s.charAt(l - 1)))
                return processWhiteSpaceRule(s, wsr);

            boolean lastWasWhite = false;

            for (int i = 1; i < l; i++)
            {
                boolean isWhite = CharUtil.isWhiteSpace(s.charAt(i));

                if (isWhite && lastWasWhite)
                    return processWhiteSpaceRule(s, wsr);

                lastWasWhite = isWhite;
            }
        }

        return s;
    }

    static String processWhiteSpaceRule(String s, int wsr)
    {
        ScrubBuffer sb = getScrubBuffer(wsr);

        sb.scrub(s, 0, s.length());

        return sb.getResultAsString();
    }

    static final class ScrubBuffer
    {
        ScrubBuffer()
        {
            _sb = new StringBuffer();
        }

        void init(int wsr)
        {
            _sb.delete(0, _sb.length());

            _wsr = wsr;
            _state = START_STATE;
        }

        void scrub(Object src, int off, int cch)
        {
            if (cch == 0)
                return;

            if (_wsr == Locale.WS_PRESERVE)
            {
                CharUtil.getString(_sb, src, off, cch);
                return;
            }

            char[] chars;

            if (src instanceof char[])
                chars = (char[]) src;
            else
            {
                if (cch <= _srcBuf.length)
                    chars = _srcBuf;
                else if (cch <= 16384)
                    chars = _srcBuf = new char[16384];
                else
                    chars = new char[cch];

                CharUtil.getChars(chars, 0, src, off, cch);
                off = 0;
            }

            int start = 0;

            for (int i = 0; i < cch; i++)
            {
                char ch = chars[off + i];

                if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t')
                {
                    _sb.append(chars, off + start, i - start);

                    start = i + 1;

                    if (_wsr == Locale.WS_REPLACE)
                        _sb.append(' ');
                    else if (_state == NOSPACE_STATE)
                        _state = SPACE_SEEN_STATE;
                }
                else
                {
                    if (_state == SPACE_SEEN_STATE)
                        _sb.append(' ');

                    _state = NOSPACE_STATE;
                }
            }

            _sb.append(chars, off + start, cch - start);
        }

        String getResultAsString()
        {
            return _sb.toString();
        }

        private static final int START_STATE = 0;
        private static final int SPACE_SEEN_STATE = 1;
        private static final int NOSPACE_STATE = 2;

        private int _state;

        private int _wsr;

        private char[] _srcBuf = new char[1024];
        private StringBuffer _sb;
    }

    private static ThreadLocal tl_scrubBuffer =
        new ThreadLocal()
        {
            protected Object initialValue()
            {
                return new SoftReference(new ScrubBuffer());
            }
        };

    static ScrubBuffer getScrubBuffer(int wsr)
    {
        SoftReference softRef = (SoftReference) tl_scrubBuffer.get();
        ScrubBuffer scrubBuffer = (ScrubBuffer) (softRef).get();
        if (scrubBuffer == null)
        {
            scrubBuffer = new ScrubBuffer();
            tl_scrubBuffer.set(new SoftReference(scrubBuffer));
        }

        scrubBuffer.init(wsr);
        return scrubBuffer;
    }

    static boolean pushToContainer(Cur c)
    {
        c.push();

        for (; ;)
        {
            switch (c.kind())
            {
            case ROOT:
            case ELEM:
                return true;
            case -ROOT:
            case -ELEM:
                c.pop();
                return false;
            case COMMENT:
            case PROCINST:
                c.skip();
                break;
            default                      :
                c.nextWithAttrs();
                break;
            }
        }
    }

    static boolean toFirstNormalAttr(Cur c)
    {
        c.push();

        if (c.toFirstAttr())
        {
            do
            {
                if (!c.isXmlns())
                {
                    c.popButStay();
                    return true;
                }
            }
            while (c.toNextAttr());
        }

        c.pop();

        return false;
    }

    static boolean toPrevNormalAttr(Cur c)
    {
        if (c.isAttr())
        {
            c.push();

            for (; ;)
            {
                assert c.isAttr();

                // See if I can move backward.  If I'm at the first attr, prev must return
                // false and not move.

                if (!c.prev())
                    break;

                // Skip past the text value or attr begin

                c.prev();

                // I might have skipped over text above

                if (!c.isAttr())
                    c.prev();

                if (c.isNormalAttr())
                {
                    c.popButStay();
                    return true;
                }
            }

            c.pop();
        }

        return false;
    }

    static boolean toNextNormalAttr(Cur c)
    {
        c.push();

        while (c.toNextAttr())
        {
            if (!c.isXmlns())
            {
                c.popButStay();
                return true;
            }
        }

        c.pop();

        return false;
    }

    Xobj findNthChildElem(Xobj parent, QName name, QNameSet set, int n)
    {
        // only one of (set or name) is not null
        // or both are null for a wildcard
        assert (name == null || set == null);
        assert n >= 0;

        if (parent == null)
            return null;

        int da = _nthCache_A.distance(parent, name, set, n);
        int db = _nthCache_B.distance(parent, name, set, n);

        Xobj x =
            da <= db
            ? _nthCache_A.fetch(parent, name, set, n)
            : _nthCache_B.fetch(parent, name, set, n);

        if (da == db)
        {
            nthCache temp = _nthCache_A;
            _nthCache_A = _nthCache_B;
            _nthCache_B = temp;
        }

        return x;
    }

    int count(Xobj parent, QName name, QNameSet set)
    {
        int n = 0;

        for (Xobj x = findNthChildElem(parent, name, set, 0);
             x != null; x = x._nextSibling)
        {
            if (x.isElem())
            {
                if (set == null)
                {
                    if (x._name.equals(name))
                        n++;
                }
                else if (set.contains(x._name))
                    n++;
            }
        }

        return n;
    }

    static boolean toChild(Cur c, QName name, int n)
    {
        if (n >= 0 && pushToContainer(c))
        {
            Xobj x = c._locale.findNthChildElem(c._xobj, name, null, n);

            c.pop();

            if (x != null)
            {
                c.moveTo(x);
                return true;
            }
        }

        return false;
    }

    static boolean toFirstChildElement(Cur c)
    {
//        if (!pushToContainer(c))
//            return false;
//
//        if (!c.toFirstChild() || (!c.isElem() && !toNextSiblingElement(c)))
//        {
//            c.pop();
//            return false;
//        }
//
//        c.popButStay();
//
//        return true;

        Xobj originalXobj = c._xobj;
        int  originalPos  = c._pos;

        loop:
        for (; ;)
        {
            switch (c.kind())
            {
            case ROOT:
            case ELEM:
                break loop;
            case -ROOT:
            case -ELEM:
                c.moveTo(originalXobj, originalPos);
                return false;
            case COMMENT:
            case PROCINST:
                c.skip();
                break;
            default:
                c.nextWithAttrs();
                break;
            }
        }

        if (!c.toFirstChild() || (!c.isElem() && !toNextSiblingElement(c)))
        {
            c.moveTo(originalXobj, originalPos);
            return false;
        }

        return true;
    }

    static boolean toLastChildElement(Cur c)
    {
        if (!pushToContainer(c))
            return false;

        if (!c.toLastChild() || (!c.isElem() && !toPrevSiblingElement(c)))
        {
            c.pop();
            return false;
        }

        c.popButStay();

        return true;
    }

    static boolean toPrevSiblingElement(Cur cur)
    {
        if (!cur.hasParent())
            return false;

        Cur c = cur.tempCur();

        boolean moved = false;

        int k = c.kind();

        if (k != ATTR)
        {
            for (; ;)
            {
                if (!c.prev())
                    break;

                k = c.kind();

                if (k == ROOT || k == ELEM)
                    break;

                if (c.kind() == -ELEM)
                {
                    c.toParent();

                    cur.moveToCur(c);
                    moved = true;

                    break;
                }
            }
        }

        c.release();

        return moved;
    }

    static boolean toNextSiblingElement(Cur c)
    {
        if (!c.hasParent())
            return false;

        c.push();

        int k = c.kind();

        if (k == ATTR)
        {
            c.toParent();
            c.next();
        }
        else if (k == ELEM)
            c.skip();

        while ((k = c.kind()) >= 0)
        {
            if (k == ELEM)
            {
                c.popButStay();
                return true;
            }

            if (k > 0)
                c.toEnd();

            c.next();
        }

        c.pop();

        return false;
    }

    static boolean toNextSiblingElement(Cur c, Xobj parent)
    {
        Xobj originalXobj = c._xobj;
        int originalPos = c._pos;

        int k = c.kind();

        if (k == ATTR)
        {
            c.moveTo(parent);
            c.next();
        }
        else if (k == ELEM)
            c.skip();

        while ((k = c.kind()) >= 0)
        {
            if (k == ELEM)
            {
                return true;
            }

            if (k > 0)
                c.toEnd();

            c.next();
        }

        c.moveTo(originalXobj, originalPos);

        return false;
    }

    static void applyNamespaces(Cur c, Map namespaces)
    {
        assert c.isContainer();

        java.util.Iterator i = namespaces.keySet().iterator();

        while (i.hasNext())
        {
            String prefix = (String) i.next();

            // Usually, this is the predefined xml namespace
            if (!prefix.toLowerCase().startsWith("xml"))
            {
                if (c.namespaceForPrefix(prefix, false) == null)
                {
                    c.push();

                    c.next();
                    c.createAttr(c._locale.createXmlns(prefix));
                    c.next();

                    c.insertString((String) namespaces.get(prefix));

                    c.pop();
                }
            }
        }
    }

    static Map getAllNamespaces(Cur c, Map filleMe)
    {
        assert c.isNode();

        c.push();

        if (!c.isContainer())
            c.toParent();

        assert c.isContainer();

        do
        {
            QName cName = c.getName();

            while (c.toNextAttr())
            {
                if (c.isXmlns())
                {
                    String prefix = c.getXmlnsPrefix();
                    String uri = c.getXmlnsUri();

                    if (filleMe == null)
                        filleMe = new HashMap();

                    if (!filleMe.containsKey(prefix))
                        filleMe.put(prefix, uri);
                }
            }

            if (!c.isContainer())
                c.toParentRaw();
        }
        while (c.toParentRaw());

        c.pop();

        return filleMe;
    }

    class nthCache
    {
        private boolean namesSame(QName pattern, QName name)
        {
            return pattern == null || pattern.equals(name);
        }

        private boolean setsSame(QNameSet patternSet, QNameSet set)
        {
            // value equality is probably too expensive. Since the use case
            // involves QNameSets that are generated by the compiler, we
            // can use identity comparison.

            return patternSet != null && patternSet == set;
        }

        private boolean nameHit(QName namePattern, QNameSet setPattern,
            QName name)
        {
            return
                setPattern == null
                ? namesSame(namePattern, name)
                : setPattern.contains(name);
        }

        private boolean cacheSame(QName namePattern, QNameSet setPattern)
        {
            return
                setPattern == null
                ? namesSame(namePattern, _name)
                : setsSame(setPattern, _set);
        }

        int distance(Xobj parent, QName name, QNameSet set, int n)
        {
            assert n >= 0;

            if (_version != Locale.this.version())
                return Integer.MAX_VALUE - 1;

            if (parent != _parent || !cacheSame(name, set))
                return Integer.MAX_VALUE;

            return n > _n ? n - _n : _n - n;
        }

        Xobj fetch(Xobj parent, QName name, QNameSet set, int n)
        {
            assert n >= 0;

            if (_version != Locale.this.version() || _parent != parent ||
                !cacheSame(name, set) || n == 0)
            {
                _version = Locale.this.version();
                _parent = parent;
                _name = name;
                _child = null;
                _n = -1;

                loop:
                for (Xobj x = parent._firstChild;
                     x != null; x = x._nextSibling)
                {
                    if (x.isElem() && nameHit(name, set, x._name))
                    {
                        _child = x;
                        _n = 0;

                        break loop;
                    }
                }
            }

            if (_n < 0)
                return null;

            if (n > _n)
            {
                while (n > _n)
                {
                    for (Xobj x = _child._nextSibling; ; x = x._nextSibling)
                    {
                        if (x == null)
                            return null;

                        if (x.isElem() && nameHit(name, set, x._name))
                        {
                            _child = x;
                            _n++;

                            break;
                        }
                    }
                }
            }
            else if (n < _n)
            {
                while (n < _n)
                {
                    for (Xobj x = _child._prevSibling; ; x = x._prevSibling)
                    {
                        if (x == null)
                            return null;

                        if (x.isElem() && nameHit(name, set, x._name))
                        {
                            _child = x;
                            _n--;

                            break;
                        }
                    }
                }
            }

            return _child;
        }

        private long _version;
        private Xobj _parent;
        private QName _name;
        private QNameSet _set;
        private Xobj _child;
        private int _n;
    }

    //
    //
    //
    
    Dom findDomNthChild ( Dom parent, int n )
    {
        assert n >= 0;
        
        if (parent == null)
            return null;
        
        int da = _domNthCache_A.distance(parent, n);
        int db = _domNthCache_B.distance(parent, n);
        
       
        // the "better" cache should never walk more than 1/2 len
        Dom x = null;
        boolean bInvalidate = (db - _domNthCache_B._len / 2 > 0) &&
            (db - _domNthCache_B._len / 2 - domNthCache.BLITZ_BOUNDARY > 0);
        boolean aInvalidate = (da - _domNthCache_A._len / 2 > 0) &&
            (da - _domNthCache_A._len / 2 - domNthCache.BLITZ_BOUNDARY > 0);
        if (da <= db)
            if (!aInvalidate)
                x = _domNthCache_A.fetch(parent, n);
            else
            {
                _domNthCache_B._version = -1;//blitz the cache
                x = _domNthCache_B.fetch(parent, n);
            }
        else if (!bInvalidate)
            x = _domNthCache_B.fetch(parent, n);
        else
        {
            _domNthCache_A._version = -1;//blitz the cache
            x = _domNthCache_A.fetch(parent, n);
        }

        if (da == db)
        {
            domNthCache temp = _domNthCache_A;
            _domNthCache_A = _domNthCache_B;
            _domNthCache_B = temp;
        }
        
        return x;
    }
    
    int domLength ( Dom parent )
    {
        if (parent == null)
            return 0;
        
        int da = _domNthCache_A.distance( parent, 0 );
        int db = _domNthCache_B.distance( parent, 0 );
        
        int len =
            da <= db
            ? _domNthCache_A.length( parent )
            : _domNthCache_B.length( parent );
        
        if (da == db)
        {
            domNthCache temp = _domNthCache_A;
            _domNthCache_A = _domNthCache_B;
            _domNthCache_B = temp;
        }
        
        return len;
    }
    
    void invalidateDomCaches ( Dom d )
    {
        if (_domNthCache_A._parent == d)
            _domNthCache_A._version = -1;
        if (_domNthCache_B._parent == d)
            _domNthCache_B._version = -1;
    }
    
    boolean isDomCached ( Dom d )
    {
        return _domNthCache_A._parent == d || _domNthCache_B._parent == d;
    }
    
    class domNthCache
    {
        
        int distance ( Dom parent, int n )
        {
            assert n >= 0;
            
            if (_version != Locale.this.version())
                return Integer.MAX_VALUE - 1;
            
            if (parent != _parent)
                return Integer.MAX_VALUE;
            
            return n > _n ? n - _n : _n - n;
        }
        
        int length ( Dom parent )
        {
            if (_version != Locale.this.version() || _parent != parent)
            {
                _parent = parent;
                _version = Locale.this.version();
                _child = null;
                _n = -1;
                _len = -1;
            }
            
            if (_len == -1)
            {
                Dom x = null;
                
                if (_child != null && _n != -1)
                {
                    x = _child;
                    _len = _n;
                }
                else
                {
                    x = DomImpl.firstChild(_parent);
                    _len = 0;
                    
                    // cache the 0th child
                    _child = x;
                    _n = 0;
                }
                
                for (; x != null; x = DomImpl.nextSibling(x) )
                {
                    _len++;
                }
            }
            
            
            return _len;
        }
        
        Dom fetch ( Dom parent, int n )
        {
            assert n >= 0;
            
            if (_version != Locale.this.version() || _parent != parent)
            {
                _parent = parent;
                _version = Locale.this.version();
                _child = null;
                _n = -1;
                _len = -1;
                
                for (Dom x = DomImpl.firstChild(_parent); x != null; x = DomImpl.nextSibling(x) )
                {
                    _n++;
                    if (_child == null && n == _n )
                    {
                        _child = x;
                        break;
                    }
                }
                
                return _child;
            }
            
            if (_n < 0)
                return null;
            
            if (n > _n)
            {
                while ( n > _n )
                {
                    for (Dom x = DomImpl.nextSibling(_child); ; x = DomImpl.nextSibling(x) )
                    {
                        if (x == null)
                            return null;
                        
                        _child = x;
                        _n++;
                        
                        break;
                    }
                }
            }
            else if (n < _n)
            {
                while ( n < _n )
                {
                    for (Dom x = DomImpl.prevSibling(_child); ; x = DomImpl.prevSibling(x) )
                    {
                        if (x == null)
                            return null;
                        
                        _child = x;
                        _n--;
                        
                        break;
                    }
                }
            }
            
            return _child;
        }
        
        public static final int BLITZ_BOUNDARY = 40; //walk small lists
	 private long  _version;
        private Dom   _parent;
        private Dom   _child;
        private int   _n;
        private int   _len;
    }
    
    //
    // 
    //

    CharUtil getCharUtil()
    {
        if (_charUtil == null)
            _charUtil = new CharUtil(1024);

        return _charUtil;
    }

    long version()
    {
        return _versionAll;
    }

    Cur weakCur(Object o)
    {
        assert o != null && !(o instanceof Ref);

        Cur c = getCur();

        assert c._tempFrame == -1;
        assert c._ref == null;

        c._ref = new Ref(c, o);

        return c;
    }

    final ReferenceQueue refQueue()
    {
        if (_refQueue == null)
            _refQueue = new ReferenceQueue();

        return _refQueue;
    }

    final static class Ref
        extends PhantomReference
    {
        Ref(Cur c, Object obj)
        {
            super(obj, c._locale.refQueue());

            _cur = c;
        }

        Cur _cur;
    }

    Cur tempCur()
    {
        return tempCur(null);
    }

    Cur tempCur(String id)
    {
        Cur c = getCur();

        assert c._tempFrame == -1;

        assert _numTempFramesLeft < _tempFrames.length : "Temp frame not pushed";

        int frame = _tempFrames.length - _numTempFramesLeft - 1;

        assert frame >= 0 && frame < _tempFrames.length;

        Cur next = _tempFrames[frame];

        c._nextTemp = next;
        assert c._prevTemp == null;

        if (next != null)
        {
            assert next._prevTemp == null;
            next._prevTemp = c;
        }

        _tempFrames[frame] = c;
        c._tempFrame = frame;

        c._id = id;

        return c;
    }

    Cur getCur()
    {
        assert _curPool == null || _curPoolCount > 0;

        Cur c;

        if (_curPool == null)
            c = new Cur(this);
        else
        {
            _curPool = _curPool.listRemove(c = _curPool);
            _curPoolCount--;
        }

        assert c._state == Cur.POOLED;
        assert c._prev == null && c._next == null;
        assert c._xobj == null && c._pos == Cur.NO_POS;
        assert c._ref == null;

        _registered = c.listInsert(_registered);
        c._state = Cur.REGISTERED;

        return c;
    }

    void embedCurs()
    {
        for (Cur c; (c = _registered) != null;)
        {
            assert c._xobj != null;

            _registered = c.listRemove(_registered);
            c._xobj._embedded = c.listInsert(c._xobj._embedded);
            c._state = Cur.EMBEDDED;
        }
    }

    TextNode createTextNode()
    {
        return _saaj == null ? new TextNode(this) : new SaajTextNode(this);
    }

    CdataNode createCdataNode()
    {
        return _saaj == null ?
            new CdataNode(this) : new SaajCdataNode(this);
    }

    boolean entered()
    {
        return _tempFrames.length - _numTempFramesLeft > 0;
    }

    public void enter(Locale otherLocale)
    {
        enter();

        if (otherLocale != this)
            otherLocale.enter();
    }

    public void enter()
    {
        assert _numTempFramesLeft >= 0;

        if (--_numTempFramesLeft <= 0)
        {
            Cur[] newTempFrames = new Cur[_tempFrames.length * 2];
            //move this assignment down so if array allocation fails, error is not masked
            _numTempFramesLeft = _tempFrames.length;
            System.arraycopy(_tempFrames, 0, newTempFrames, 0,
                _tempFrames.length);
            _tempFrames = newTempFrames;
        }

        if (++_entryCount > 1000)
        {
            pollQueue();
            _entryCount = 0;
        }
    }

    private void pollQueue()
    {
        if (_refQueue != null)
        {
            for (; ;)
            {
                Ref ref = (Ref) _refQueue.poll();

                if (ref == null)
                    break;

                if (ref._cur != null)
                    ref._cur.release();
            }
        }
    }

    public void exit(Locale otherLocale)
    {
        exit();

        if (otherLocale != this)
            otherLocale.exit();
    }

    public void exit()
    {
        // assert _numTempFramesLeft >= 0;
        //asserts computed frame fits between 0 and _tempFrames.length
        assert _numTempFramesLeft >= 0 &&
            (_numTempFramesLeft <= _tempFrames.length - 1):
            " Temp frames mismanaged. Impossible stack frame. Unsynchronized: " +
            noSync();

        int frame = _tempFrames.length - ++_numTempFramesLeft;

        while (_tempFrames[frame] != null)
            _tempFrames[frame].release();
    }

    //
    //
    //

    public boolean noSync()
    {
        return _noSync;
    }

    public boolean sync()
    {
        return !_noSync;
    }

    static final boolean isWhiteSpace(String s)
    {
        int l = s.length();

        while (l-- > 0)
            if (!CharUtil.isWhiteSpace(s.charAt(l)))
                return false;

        return true;
    }

    static final boolean isWhiteSpace(StringBuffer sb)
    {
        int l = sb.length();

        while (l-- > 0)
            if (!CharUtil.isWhiteSpace(sb.charAt(l)))
                return false;

        return true;
    }

    static boolean beginsWithXml(String name)
    {
        if (name.length() < 3)
            return false;

        char ch;

        if (((ch = name.charAt(0)) == 'x' || ch == 'X') &&
            ((ch = name.charAt(1)) == 'm' || ch == 'M') &&
            ((ch = name.charAt(2)) == 'l' || ch == 'L'))
        {
            return true;
        }

        return false;
    }

    static boolean isXmlns(QName name)
    {
        String prefix = name.getPrefix();

        if (prefix.equals("xmlns"))
            return true;

        return prefix.length() == 0 && name.getLocalPart().equals("xmlns");
    }

    QName createXmlns(String prefix)
    {
        if (prefix == null)
            prefix = "";

        return
            prefix.length() == 0
            ? makeQName(_xmlnsUri, "xmlns", "")
            : makeQName(_xmlnsUri, prefix, "xmlns");
    }

    static String xmlnsPrefix(QName name)
    {
        return name.getPrefix().equals("xmlns") ? name.getLocalPart() : "";
    }

    //
    // Loading/parsing
    //

    static abstract class LoadContext
    {
        protected abstract void startDTD(String name, String publicId,
            String systemId);

        protected abstract void endDTD();

        protected abstract void startElement(QName name);

        protected abstract void endElement();

        protected abstract void attr(QName name, String value);

        protected abstract void attr(String local, String uri, String prefix,
            String value);

        protected abstract void xmlns(String prefix, String uri);

        protected abstract void comment(char[] buff, int off, int cch);

        protected abstract void comment(String comment);

        protected abstract void procInst(String target, String value);

        protected abstract void text(char[] buff, int off, int cch);

        protected abstract void text(String s);

        protected abstract Cur finish();

        protected abstract void abort();

        protected abstract void bookmark(XmlBookmark bm);

        protected abstract void bookmarkLastNonAttr(XmlBookmark bm);

        protected abstract void bookmarkLastAttr(QName attrName,
            XmlBookmark bm);

        protected abstract void lineNumber(int line, int column, int offset);

        protected void addIdAttr(String eName, String aName){
            if ( _idAttrs == null )
                _idAttrs = new java.util.Hashtable();
            _idAttrs.put(aName,eName);
        }

        protected boolean isAttrOfTypeId(QName aqn, QName eqn){
            if (_idAttrs == null)
                return false;
            String pre = aqn.getPrefix();
            String lName = aqn.getLocalPart();
            String urnName = "".equals(pre)?lName:pre + ":" + lName;
            String eName = (String) _idAttrs.get(urnName);
            if (eName == null ) return false;
            //get the name of the parent elt
            pre = eqn.getPrefix();
            lName = eqn.getLocalPart();
            lName = eqn.getLocalPart();
            urnName = "".equals(pre)?lName:pre + ":" + lName;
            return eName.equals(urnName);
        }
        private java.util.Hashtable _idAttrs;
    }

    private static class DefaultEntityResolver
        implements EntityResolver
    {
        public InputSource resolveEntity(String publicId, String systemId)
        {
            return new InputSource(new StringReader(""));
        }
    }

    private static SaxLoader getSaxLoader(XmlOptions options) throws XmlException
    {
        options = XmlOptions.maskNull(options);

        EntityResolver er = null;

        if (!options.hasOption(XmlOptions.LOAD_USE_DEFAULT_RESOLVER))
        {
            er = (EntityResolver) options.get(XmlOptions.ENTITY_RESOLVER);

            if (er == null)
                er = ResolverUtil.getGlobalEntityResolver();

            if (er == null)
                er = new DefaultEntityResolver();
        }

        XMLReader xr = (XMLReader) options.get(
            XmlOptions.LOAD_USE_XMLREADER);

        if (xr == null) {
            try {
                xr = SAXHelper.newXMLReader();
            } catch(Exception e) {
                throw new XmlException("Problem creating XMLReader", e);
            } 
        }

        SaxLoader sl = new XmlReaderSaxLoader(xr);

        // I've noticed that most XMLReaders don't like a null EntityResolver...

        if (er != null)
            xr.setEntityResolver(er);

        return sl;
    }

    private static class XmlReaderSaxLoader
        extends SaxLoader
    {
        XmlReaderSaxLoader(XMLReader xr)
        {
            super(xr, null);
        }
    }

    private static abstract class SaxHandler
        implements ContentHandler, LexicalHandler , DeclHandler, DTDHandler
    {
        SaxHandler(Locator startLocator)
        {
            _startLocator = startLocator;
        }

        SaxHandler()
        {
            this(null);
        }

        void initSaxHandler(Locale l, XmlOptions options)
        {
            _locale = l;

            options = XmlOptions.maskNull(options);

            _context = new Cur.CurLoadContext(_locale, options);

            _wantLineNumbers =
                _startLocator != null &&
                options.hasOption(XmlOptions.LOAD_LINE_NUMBERS);
            _wantLineNumbersAtEndElt =
                _startLocator != null &&
                options.hasOption(XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT);
            _wantCdataBookmarks =
                _startLocator != null &&
                options.hasOption(XmlOptions.LOAD_SAVE_CDATA_BOOKMARKS);

            if (options.hasOption(XmlOptions.LOAD_ENTITY_BYTES_LIMIT))
                _entityBytesLimit = ((Integer)(options.get(XmlOptions.LOAD_ENTITY_BYTES_LIMIT))).intValue();
        }

        public void startDocument()
            throws SAXException
        {
            // Do nothing ... start of document is implicit
        }

        public void endDocument()
            throws SAXException
        {
            // Do nothing ... end of document is implicit
        }

        public void startElement(String uri, String local, String qName,
            Attributes atts)
            throws SAXException
        {
            if (local.length() == 0)
                local = qName;

            // Out current parser does not error when a
            // namespace is used and not defined.  Check for these here

            if (qName.indexOf(':') >= 0 && uri.length() == 0)
            {
                XmlError err =
                    XmlError.forMessage("Use of undefined namespace prefix: " +
                    qName.substring(0, qName.indexOf(':')));

                throw new XmlRuntimeException(err.toString(), null, err);
            }

            _context.startElement(_locale.makeQualifiedQName(uri, qName));

            if (_wantLineNumbers)
            {
                _context.bookmark(
                    new XmlLineNumber(_startLocator.getLineNumber(),
                        _startLocator.getColumnNumber() - 1, -1));
            }

            for (int i = 0, len = atts.getLength(); i < len; i++)
            {
                String aqn = atts.getQName(i);

                if (aqn.equals("xmlns"))
                {
                    _context.xmlns("", atts.getValue(i));
                }
                else if (aqn.startsWith("xmlns:"))
                {
                    String prefix = aqn.substring(6);

                    if (prefix.length() == 0)
                    {
                        XmlError err =
                            XmlError.forMessage("Prefix not specified",
                                XmlError.SEVERITY_ERROR);

                        throw new XmlRuntimeException(err.toString(), null,
                            err);
                    }

                    String attrUri = atts.getValue(i);

                    if (attrUri.length() == 0)
                    {
                        XmlError err =
                            XmlError.forMessage(
                                "Prefix can't be mapped to no namespace: " +
                            prefix,
                                XmlError.SEVERITY_ERROR);

                        throw new XmlRuntimeException(err.toString(), null,
                            err);
                    }

                    _context.xmlns(prefix, attrUri);
                }
                else
                {
                    int colon = aqn.indexOf(':');

                    if (colon < 0)
                        _context.attr(aqn, atts.getURI(i), null,
                            atts.getValue(i));
                    else
                    {
                        _context.attr(aqn.substring(colon + 1), atts.getURI(i), aqn.substring(
                            0, colon),
                            atts.getValue(i));
                    }
                }
            }
        }

        public void endElement(String namespaceURI, String localName,
            String qName)
            throws SAXException
        {
            _context.endElement();
            if (_wantLineNumbersAtEndElt)
            {
                _context.bookmark(
                    new XmlLineNumber(_startLocator.getLineNumber(),
                        _startLocator.getColumnNumber() - 1, -1));
            }
        }

        public void characters(char ch[], int start, int length)
            throws SAXException
        {
            _context.text(ch, start, length);

            if (_wantCdataBookmarks && _insideCDATA)
                _context.bookmarkLastNonAttr(CDataBookmark.CDATA_BOOKMARK);

            if (_insideEntity!=0)
            {
                if ((_entityBytes += length) > _entityBytesLimit)
                {
                    XmlError err = XmlError.forMessage(XmlErrorCodes.EXCEPTION_EXCEEDED_ENTITY_BYTES,
                            new Integer[]{ new Integer(_entityBytesLimit) });

                    throw new SAXException(err.getMessage());
                }
            }
        }

        public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException
        {
        }

        public void comment(char ch[], int start, int length)
            throws SAXException
        {
            _context.comment(ch, start, length);
        }

        public void processingInstruction(String target, String data)
            throws SAXException
        {
            _context.procInst(target, data);
        }

        public void startDTD(String name, String publicId, String systemId)
            throws SAXException
        {
            _context.startDTD(name, publicId, systemId);
        }

        public void endDTD()
            throws SAXException
        {
            _context.endDTD();
        }

        public void startPrefixMapping(String prefix, String uri)
            throws SAXException
        {
            if (beginsWithXml(prefix) &&
                !("xml".equals(prefix) && _xml1998Uri.equals(uri)))
            {
                XmlError err =
                    XmlError.forMessage(
                        "Prefix can't begin with XML: " + prefix,
                        XmlError.SEVERITY_ERROR);

                throw new XmlRuntimeException(err.toString(), null, err);
            }
        }

        public void endPrefixMapping(String prefix)
            throws SAXException
        {
        }

        public void skippedEntity(String name)
            throws SAXException
        {
//            throw new RuntimeException( "Not impl: skippedEntity" );
        }

        public void startCDATA()
            throws SAXException
        {
            _insideCDATA = true;
        }

        public void endCDATA()
            throws SAXException
        {
            _insideCDATA = false;
        }

        public void startEntity(String name)
            throws SAXException
        {
            _insideEntity++;
        }

        public void endEntity(String name)
            throws SAXException
        {
            _insideEntity--;
            assert _insideEntity>=0;

            if (_insideEntity==0)
            {
                _entityBytes=0;
            }
        }

        public void setDocumentLocator(Locator locator)
        {
            // TODO - for non-Piccolo use cases, use a locator to get line numbers
        }
        //DeclHandler
        public void attributeDecl(String eName, String aName, String type, String valueDefault, String value){
             if (type.equals("ID")){
                 _context.addIdAttr(eName,aName);
             }
        }
        public void elementDecl(String name, String model){
         }
        public void externalEntityDecl(String name, String publicId, String systemId){
         }
        public void internalEntityDecl(String name, String value){
         }

        //DTDHandler
        public void notationDecl(String name, String publicId, String systemId){
        }
        public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName){
        }
        protected Locale _locale;

        protected LoadContext _context;

        private boolean _wantLineNumbers;
        private boolean _wantLineNumbersAtEndElt;
        private boolean _wantCdataBookmarks;
        private Locator _startLocator;
        private boolean _insideCDATA = false;
        private int _entityBytesLimit = 10240;
        private int _entityBytes = 0;
        private int _insideEntity = 0;
    }

    private static abstract class SaxLoader
        extends SaxHandler
        implements ErrorHandler
    {
        SaxLoader(XMLReader xr, Locator startLocator)
        {
            super(startLocator);

            _xr = xr;

            try
            {
                _xr.setFeature(
                    "http://xml.org/sax/features/namespace-prefixes", true);
                _xr.setFeature("http://xml.org/sax/features/namespaces", true);
                _xr.setFeature("http://xml.org/sax/features/validation", false);
                _xr.setProperty(
                    "http://xml.org/sax/properties/lexical-handler", this);
                _xr.setContentHandler(this);
                _xr.setProperty("http://xml.org/sax/properties/declaration-handler", this);
                _xr.setDTDHandler(this);
                _xr.setErrorHandler(this);
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        void setEntityResolver(EntityResolver er)
        {
            _xr.setEntityResolver(er);
        }

        void postLoad(Cur c)
        {
            // fix garbage collection of Locale -> Xobj -> STL
            _locale = null;
            _context = null;
        }

        public Cur load(Locale l, InputSource is, XmlOptions options)
            throws XmlException, IOException
        {
            is.setSystemId("file://");

            initSaxHandler(l, options);

            try
            {
                _xr.parse(is);

                Cur c = _context.finish();

                associateSourceName(c, options);

                postLoad(c);

                return c;
            }
            catch (XmlRuntimeException e)
            {
                _context.abort();

                throw new XmlException(e);
            }
            catch (SAXParseException e)
            {
                _context.abort();

                XmlError err =
                    XmlError.forLocation(e.getMessage(),
                        (String) XmlOptions.safeGet(options,
                            XmlOptions.DOCUMENT_SOURCE_NAME),
                        e.getLineNumber(), e.getColumnNumber(), -1);

                throw new XmlException(err.toString(), e, err);
            }
            catch (SAXException e)
            {
                _context.abort();

                XmlError err = XmlError.forMessage(e.getMessage());

                throw new XmlException(err.toString(), e, err);
            }
            catch (RuntimeException e)
            {
                _context.abort();

                throw e;
            }
        }

        public void fatalError(SAXParseException e)
            throws SAXException
        {
            throw e;
        }

        public void error(SAXParseException e)
            throws SAXException
        {
            throw e;
        }

        public void warning(SAXParseException e)
            throws SAXException
        {
            throw e;
        }

        private XMLReader _xr;
    }

    private Dom load(InputSource is, XmlOptions options)
        throws XmlException, IOException
    {
        return getSaxLoader(options).load(this, is, options).getDom();
    }

    public Dom load(Reader r)
        throws XmlException, IOException
    {
        return load(r, null);
    }

    public Dom load(Reader r, XmlOptions options)
        throws XmlException, IOException
    {
        return load(new InputSource(r), options);
    }

    public Dom load(InputStream in)
        throws XmlException, IOException
    {
        return load(in, null);
    }

    public Dom load(InputStream in, XmlOptions options)
        throws XmlException, IOException
    {
        return load(new InputSource(in), options);
    }

    public Dom load(String s)
        throws XmlException
    {
        return load(s, null);
    }

    public Dom load(String s, XmlOptions options)
        throws XmlException
    {
        Reader r = new StringReader(s);

        try
        {
            return load(r, options);
        }
        catch (IOException e)
        {
            assert false: "StringReader should not throw IOException";

            throw new XmlException(e.getMessage(), e);
        }
        finally
        {
            try
            {
                r.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    //
    // DOMImplementation methods
    //

    public Document createDocument(String uri, String qname,
        DocumentType doctype)
    {
        return DomImpl._domImplementation_createDocument(this, uri, qname,
            doctype);
    }

    public DocumentType createDocumentType(String qname, String publicId,
        String systemId)
    {
        throw new RuntimeException("Not implemented");
//        return DomImpl._domImplementation_createDocumentType( this, qname, publicId, systemId );
    }

    public boolean hasFeature(String feature, String version)
    {
        return DomImpl._domImplementation_hasFeature(this, feature, version);
    }

    public Object getFeature(String feature, String version)
    {
        throw new RuntimeException("DOM Level 3 Not implemented");
    }

    //
    // Dom methods
    //

    private static Dom checkNode(Node n)
    {
        if (n == null)
            throw new IllegalArgumentException("Node is null");

        if (!(n instanceof Dom))
            throw new IllegalArgumentException("Node is not an XmlBeans node");

        return (Dom) n;
    }

    public static XmlCursor nodeToCursor(Node n)
    {
        return DomImpl._getXmlCursor(checkNode(n));
    }

    public static XmlObject nodeToXmlObject(Node n)
    {
        return DomImpl._getXmlObject(checkNode(n));
    }

    public static XMLStreamReader nodeToXmlStream(Node n)
    {
        return DomImpl._getXmlStreamReader(checkNode(n));
    }

    public static Node streamToNode(XMLStreamReader xs)
    {
        return Jsr173.nodeFromStream(xs);
    }

    //
    // SaajCallback methods
    //

    public void setSaajData(Node n, Object o)
    {
        assert n instanceof Dom;

        DomImpl.saajCallback_setSaajData((Dom) n, o);
    }

    public Object getSaajData(Node n)
    {
        assert n instanceof Dom;

        return DomImpl.saajCallback_getSaajData((Dom) n);
    }

    public Element createSoapElement(QName name, QName parentName)
    {
        assert _ownerDoc != null;

        return DomImpl.saajCallback_createSoapElement(_ownerDoc, name,
            parentName);
    }

    public Element importSoapElement(Document doc, Element elem, boolean deep,
        QName parentName)
    {
        assert doc instanceof Dom;

        return DomImpl.saajCallback_importSoapElement((Dom) doc, elem, deep,
            parentName);
    }


    private static final class DefaultQNameFactory
        implements QNameFactory
    {
        private QNameCache _cache = XmlBeans.getQNameCache();

        public QName getQName(String uri, String local)
        {
            return _cache.getName(uri, local, "");
        }

        public QName getQName(String uri, String local, String prefix)
        {
            return _cache.getName(uri, local, prefix);
        }

        public QName getQName(char[] uriSrc, int uriPos, int uriCch,
            char[] localSrc, int localPos, int localCch)
        {
            return
                _cache.getName(new String(uriSrc, uriPos, uriCch),
                    new String(localSrc, localPos, localCch),
                    "");
        }

        public QName getQName(char[] uriSrc, int uriPos, int uriCch,
            char[] localSrc, int localPos, int localCch,
            char[] prefixSrc, int prefixPos, int prefixCch)
        {
            return
                _cache.getName(new String(uriSrc, uriPos, uriCch),
                    new String(localSrc, localPos, localCch),
                    new String(prefixSrc, prefixPos, prefixCch));
        }
    }


    private static final class LocalDocumentQNameFactory
        implements QNameFactory
    {
        private QNameCache _cache = new QNameCache( 32 );

        public QName getQName(String uri, String local)
        {
            return _cache.getName(uri, local, "");
        }

        public QName getQName(String uri, String local, String prefix)
        {
            return _cache.getName(uri, local, prefix);
        }

        public QName getQName(char[] uriSrc, int uriPos, int uriCch,
            char[] localSrc, int localPos, int localCch)
        {
            return
                _cache.getName(new String(uriSrc, uriPos, uriCch),
                    new String(localSrc, localPos, localCch),
                    "");
        }

        public QName getQName(char[] uriSrc, int uriPos, int uriCch,
            char[] localSrc, int localPos, int localCch,
            char[] prefixSrc, int prefixPos, int prefixCch)
        {
            return
                _cache.getName(new String(uriSrc, uriPos, uriCch),
                    new String(localSrc, localPos, localCch),
                    new String(prefixSrc, prefixPos, prefixCch));
        }
    }

    //
    //
    //

    boolean _noSync;

    SchemaTypeLoader _schemaTypeLoader;

    private ReferenceQueue _refQueue;
    private int _entryCount;

    int _numTempFramesLeft;
    Cur[] _tempFrames;

    Cur _curPool;
    int _curPoolCount;

    Cur _registered;

    ChangeListener _changeListeners;

    long _versionAll;
    long _versionSansText;

    Locations _locations;

    private CharUtil _charUtil;

    int _offSrc;
    int _cchSrc;

    Saaj _saaj;

    Dom _ownerDoc;

    QNameFactory _qnameFactory;

    boolean _validateOnSet;

    int _posTemp;

    nthCache _nthCache_A = new nthCache();
    nthCache _nthCache_B = new nthCache();

    domNthCache _domNthCache_A = new domNthCache();
    domNthCache _domNthCache_B = new domNthCache();
}
