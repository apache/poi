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

package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.impl.common.PrefixResolver;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.impl.common.QNameHelper;

import javax.xml.namespace.QName;
import org.apache.xmlbeans.impl.values.NamespaceContext;
   
public class JavaQNameHolder extends XmlObjectBase
{
    public JavaQNameHolder() {}

    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_QNAME; }

    private QName _value;

    protected int get_wscanon_rule()
        { return SchemaType.WS_PRESERVE; }
    
    // an ergonomic prefixer so that you can say stringValue() on a free-floating QName.
    private static final NamespaceManager PRETTY_PREFIXER = new PrettyNamespaceManager();
    
    private static class PrettyNamespaceManager implements NamespaceManager
    {
        public String find_prefix_for_nsuri(String nsuri, String suggested_prefix)
        {
            return QNameHelper.suggestPrefix(nsuri);
        }
        public String getNamespaceForPrefix(String prefix)
        {
            throw new RuntimeException( "Should not be called" );
        }
    }

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------
    public String compute_text(NamespaceManager nsm)
    {
        if (nsm == null)
        {
            // we used to: throw new IllegalStateException("Cannot create QName prefix outside of a document");
            // but it's not nice to throw on stringValue()
            nsm = PRETTY_PREFIXER;
        }

// TODO - what I really need to do here is that if there is no
// namespace for this qname, then instead of finding the prefix for the
// uri, I should make a call to set the default namespace for the
// immediate context for this qname to be "".  
   
        String namespace = _value.getNamespaceURI();
        String localPart = _value.getLocalPart();

        if (namespace == null || namespace.length() == 0)
            return localPart;

        String prefix = nsm.find_prefix_for_nsuri( namespace, null );

        assert prefix != null;

        return "".equals(prefix) ? localPart : prefix + ":" + localPart;
    }

    public static QName validateLexical(
        String v, ValidationContext context, PrefixResolver resolver)
    {
        QName name;

        try
        {
            name = parse(v, resolver);
        }
        catch ( XmlValueOutOfRangeException e )
        {
            context.invalid(e.getMessage());
            name = null;
        }

        return name;
    }

    private static QName parse(String v, PrefixResolver resolver)
    {
        String prefix, localname;
        int start;
        int end;
        for (end = v.length(); end > 0; end -= 1)
            if (!XMLChar.isSpace(v.charAt(end-1)))
                break;
        for (start = 0; start < end; start += 1)
            if (!XMLChar.isSpace(v.charAt(start)))
                break;

        int firstcolon = v.indexOf(':', start);
        if (firstcolon >= 0)
        {
            prefix = v.substring(start, firstcolon);
            localname = v.substring(firstcolon + 1, end);
        }
        else
        {
            prefix = "";
            localname = v.substring(start, end);
        }

        if ( prefix.length()>0 && !XMLChar.isValidNCName(prefix) )
            throw new XmlValueOutOfRangeException(XmlErrorCodes.QNAME, new Object[] { "Prefix not a valid NCName in '" + v + "'" });

        if ( !XMLChar.isValidNCName(localname) )
            throw new XmlValueOutOfRangeException(XmlErrorCodes.QNAME, new Object[] { "Localname not a valid NCName in '" + v + "'" });

        String uri =
            resolver == null ? null : resolver.getNamespaceForPrefix(prefix);
        
        if (uri == null)
        {
            if (prefix.length() > 0)
                throw new XmlValueOutOfRangeException(XmlErrorCodes.QNAME, new Object[] { "Can't resolve prefix '" + prefix + "'"});
                        
            uri = "";
        }

        if ( prefix!=null && prefix.length()>0 )
            return new QName(uri, localname, prefix );
        else
            return new QName( uri, localname );
    }
    
    protected void set_text(String s)
    {
        PrefixResolver resolver = NamespaceContext.getCurrent();

        if (resolver == null && has_store())
            resolver = get_store();
        
        _value = parse(s, resolver);
    }

    // BUGBUG - having prefix here may not work
    protected void set_QName(QName name)
    {
        assert name != null;
        
        // Sync force of creation of namesapce mapping ..
        
        if (has_store())
            get_store().find_prefix_for_nsuri( name.getNamespaceURI(), null );
        
        _value = name;
    }

    protected void set_xmlanysimple(XmlAnySimpleType value)
    {
        _value = parse(value.getStringValue(), NamespaceContext.getCurrent());
    }

    protected void set_nil() { _value = null; }

    // setters, getters (setter already handled via set_text)

    public QName getQNameValue()
    {
        check_dated();
        return _value;
    }

    // comparators
    protected boolean equal_to(XmlObject obj)
    {
        return _value.equals(((XmlObjectBase)obj).qNameValue());
    }

    protected int value_hash_code()
    {
        return _value.hashCode();
    }
}
