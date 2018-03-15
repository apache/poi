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

package org.apache.xmlbeans.impl.validator;

import org.apache.xmlbeans.impl.common.XMLNameHelper;
import org.apache.xmlbeans.impl.common.GenericXmlInputStream;
import org.apache.xmlbeans.impl.common.ValidatorListener.Event;
import org.apache.xmlbeans.impl.common.ValidatorListener;
import org.apache.xmlbeans.impl.common.XmlWhitespace;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XMLStreamValidationException;
import java.util.Map;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Collections;

import org.apache.xmlbeans.xml.stream.Attribute;
import org.apache.xmlbeans.xml.stream.AttributeIterator;
import org.apache.xmlbeans.xml.stream.CharacterData;
import org.apache.xmlbeans.xml.stream.StartElement;
import org.apache.xmlbeans.xml.stream.XMLEvent;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLName;
import org.apache.xmlbeans.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;

/**
 * @deprecated XMLInputStream was deprecated by XMLStreamReader from STaX - jsr173 API.
 */
public final class ValidatingXMLInputStream
    extends GenericXmlInputStream implements Event
{
    public ValidatingXMLInputStream (
        XMLInputStream xis,
        SchemaTypeLoader typeLoader, SchemaType sType, XmlOptions options )
            throws XMLStreamException
    {
        _source = xis;

        // Figure out the root type

        options = XmlOptions.maskNull( options );
        
        SchemaType type = (SchemaType) options.get( XmlOptions.DOCUMENT_TYPE );

        if (type == null)
            type = sType;

        if (type == null)
        {
            type = BuiltinSchemaTypeSystem.ST_ANY_TYPE;

            xis = xis.getSubStream();

            if (xis.skip( XMLEvent.START_ELEMENT ))
            {
                SchemaType docType =
                    typeLoader.findDocumentType(
                        XMLNameHelper.getQName( xis.next().getName() ) );

                if (docType != null)
                    type = docType;
            }

            xis.close();
        }

        // Create the validator

        _validator =
            new Validator(
                type, null, typeLoader, options, new ExceptionXmlErrorListener() );

        nextEvent( ValidatorListener.BEGIN );
    }

    // TODO - this is the quick and dirty impl of streaming validation,
    // may objects are created (like strings) which can be optimized
    
    protected XMLEvent nextEvent ( ) throws XMLStreamException
    {
        XMLEvent e = _source.next();

        if (e == null)
        {
            if (!_finished)
            {
                flushText();
                nextEvent( ValidatorListener.END );
                _finished = true;
            }
        }
        else
        {
            switch ( e.getType() )
            {
            case XMLEvent.CHARACTER_DATA :
            case XMLEvent.SPACE :
            {
                CharacterData cd = (CharacterData) e;

                if (cd.hasContent())
                    _text.append( cd.getContent() );

                break;
            }
            case XMLEvent.START_ELEMENT :
            {
                StartElement se = (StartElement) e;
                
                flushText();

                // Used for prefix to namespace mapping
                _startElement = se;

                // Prepare the xsi:* values
                
                AttributeIterator attrs = se.getAttributes();

                while ( attrs.hasNext() )
                {
                    Attribute attr = attrs.next();

                    XMLName attrName = attr.getName();

                    if ("http://www.w3.org/2001/XMLSchema-instance".equals(
                            attrName.getNamespaceUri() ))
                    {
                        String local = attrName.getLocalName();

                        if (local.equals( "type" ))
                            _xsiType = attr.getValue();
                        else if (local.equals( "nil" ))
                            _xsiNil = attr.getValue();
                        else if (local.equals( "schemaLocation" ))
                            _xsiLoc = attr.getValue();
                        else if (local.equals( "noNamespaceSchemaLocation" ))
                            _xsiNoLoc = attr.getValue();
                    }
                }

                // Emit the START

                // TODO - should delay the aquisition of the name
                _name = e.getName();

                nextEvent( ValidatorListener.BEGIN );
                
                // Emit the attrs
                
                attrs = se.getAttributes();

                while ( attrs.hasNext() )
                {
                    Attribute attr = attrs.next();

                    XMLName attrName = attr.getName();

                    if ("http://www.w3.org/2001/XMLSchema-instance".equals(
                            attrName.getNamespaceUri() ))
                    {
                        String local = attrName.getLocalName();

                        if (local.equals( "type" ))
                            continue;
                        else if (local.equals( "nil" ))
                            continue;
                        else if (local.equals( "schemaLocation" ))
                            continue;
                        else if (local.equals( "noNamespaceSchemaLocation" ))
                            continue;
                    }

                    // TODO - God, this is lame :-)

                    _text.append( attr.getValue() );
                    _name = attr.getName();
                    
                    nextEvent( ValidatorListener.ATTR );
                }

                clearText();

                _startElement = null;

                break;
            }

            case XMLEvent.END_ELEMENT :
            {
                flushText();
                
                nextEvent( ValidatorListener.END );

                break;
            }
            }
        }

        return e;
    }

    private void clearText ( )
    {
        _text.delete( 0, _text.length() );
    }
    
    private void flushText ( ) throws XMLStreamException
    {
        if (_text.length() > 0)
        {
            nextEvent( ValidatorListener.TEXT );
            clearText();
        }
    }
    
    public String getNamespaceForPrefix ( String prefix )
    {
        if (_startElement == null)
            return null;

        Map map = _startElement.getNamespaceMap();

        if (map == null)
            return null;

        return (String) map.get( prefix );
    }

    public XmlCursor getLocationAsCursor ( )
    {
        return null;
    }

    public javax.xml.stream.Location getLocation()
    {
        try
        {
            final org.apache.xmlbeans.xml.stream.Location xeLoc = _source.peek().getLocation();

            if (xeLoc==null)
                return null;

            javax.xml.stream.Location loc = new javax.xml.stream.Location()
            {
                public int getLineNumber()
                { return xeLoc.getLineNumber(); }

                public int getColumnNumber()
                { return xeLoc.getColumnNumber(); }

                public int getCharacterOffset()
                { return -1;}

                public String getPublicId()
                { return xeLoc.getPublicId(); }

                public String getSystemId()
                { return xeLoc.getSystemId(); }
            };

            return loc;
        }
        catch (XMLStreamException e)
        {
            return null;
        }
    }

    public String getXsiType ( )
    {
        return _xsiType;
    }
    
    public String getXsiNil ( )
    {
        return _xsiNil;
    }

    public String getXsiLoc ( )
    {
        return _xsiLoc;
    }

    public String getXsiNoLoc ( )
    {
        return _xsiNoLoc;
    }

    public QName getName ( )
    {
        return XMLNameHelper.getQName( _name );
    }

    public String getText ( )
    {
        return _text.toString();
    }

    public String getText ( int wsr )
    {
        return XmlWhitespace.collapse( _text.toString(), wsr );
    }

    public boolean textIsWhitespace ( )
    {
        for ( int i = 0 ; i < _text.length() ; i++ )
        {
            switch ( _text.charAt( i ) )
            {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    break;

                default :
                    return false;
            }
        }
        
        return true;
    }
    
    private final class ExceptionXmlErrorListener extends AbstractCollection
    {
        public boolean add(Object o)
        {
            assert ValidatingXMLInputStream.this._exception == null;
            
            ValidatingXMLInputStream.this._exception = 
                new XMLStreamValidationException( (XmlError)o );

            return false;
        }

        public Iterator iterator()
        {
            return Collections.EMPTY_LIST.iterator();
        }

        public int size()
        {
            return 0;
        }
    }

    private void nextEvent ( int kind )
        throws XMLStreamException
    {
        assert _exception == null;
        
        _validator.nextEvent( kind, this );

        if (_exception != null)
            throw _exception;
    }
    
    private XMLStreamValidationException _exception;

    private XMLInputStream _source;
    private Validator      _validator;
    private StringBuffer   _text = new StringBuffer();
    private boolean        _finished;
    private String         _xsiType;
    private String         _xsiNil;
    private String         _xsiLoc;
    private String         _xsiNoLoc;
    private XMLName        _name;
    private StartElement   _startElement;
}