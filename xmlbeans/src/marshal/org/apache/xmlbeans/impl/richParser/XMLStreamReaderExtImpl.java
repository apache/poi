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

package org.apache.xmlbeans.impl.richParser;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.impl.common.XMLChar;
import org.apache.xmlbeans.impl.util.XsTypeConverter;
import org.apache.xmlbeans.impl.util.Base64;
import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.impl.common.InvalidLexicalValueException;
import org.apache.xmlbeans.impl.common.XmlWhitespace;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 17, 2003
 */
public class XMLStreamReaderExtImpl
    implements XMLStreamReaderExt
{
    private final XMLStreamReader _xmlStream;
    private final CharSeqTrimWS _charSeq;
    private String _defaultValue;

    public XMLStreamReaderExtImpl(XMLStreamReader xmlStream)
    {
        if (xmlStream==null)
            throw new IllegalArgumentException();

        _xmlStream = xmlStream;
        _charSeq = new CharSeqTrimWS(this);
    }

    public XMLStreamReader getUnderlyingXmlStream()
    {
        return _xmlStream;
    }

    // XMLStreamReaderExt methods
    public String getStringValue()
        throws XMLStreamException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_PRESERVE);
        return _charSeq.toString();
    }

    public String getStringValue(int wsStyle)
        throws XMLStreamException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_PRESERVE);
        //REVIEW zieg 2004-01-11 - we should write a collapse method
        //that takes a CharSequence to void creating this extra String object
        return XmlWhitespace.collapse(_charSeq.toString(), wsStyle);
    }

    public boolean getBooleanValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexBoolean(_charSeq);
        }
        catch(InvalidLexicalValueException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public byte getByteValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexByte(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public short getShortValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexShort(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public int getIntValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexInt(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public long getLongValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexLong(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public BigInteger getBigIntegerValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexInteger(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public BigDecimal getBigDecimalValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexDecimal(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public float getFloatValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexFloat(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public double getDoubleValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexDouble(_charSeq);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public InputStream getHexBinaryValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        String text = _charSeq.toString();
        byte[] buf = HexBin.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid hexBinary value", _charSeq.getLocation());
    }

    public InputStream getBase64Value()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        String text = _charSeq.toString();
        byte[] buf = Base64.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid base64Binary value", _charSeq.getLocation());
    }

    public XmlCalendar getCalendarValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return new GDateBuilder(_charSeq).getCalendar();
        }
        catch( IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public Date getDateValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return new GDateBuilder(_charSeq).getDate();
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public GDate getGDateValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexGDate(_charSeq);
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public GDuration getGDurationValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return new GDuration(_charSeq);
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public QName getQNameValue()
        throws XMLStreamException, InvalidLexicalValueException
    {
        _charSeq.reload(CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexQName(_charSeq, _xmlStream.getNamespaceContext());
        }
        catch(InvalidLexicalValueException e)
        {
            throw new InvalidLexicalValueException(e.getMessage(), _charSeq.getLocation());
        }
    }

    public String getAttributeStringValue(int index) throws XMLStreamException
    {
        return _xmlStream.getAttributeValue(index);
    }

    public String getAttributeStringValue(int index, int wsStyle) throws XMLStreamException
    {
        return XmlWhitespace.collapse(_xmlStream.getAttributeValue(index), wsStyle);
    }

    public boolean getAttributeBooleanValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexBoolean(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(InvalidLexicalValueException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public byte getAttributeByteValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexByte(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public short getAttributeShortValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexShort(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public int getAttributeIntValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexInt(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public long getAttributeLongValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexLong(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public BigInteger getAttributeBigIntegerValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexInteger(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public BigDecimal getAttributeBigDecimalValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexDecimal(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public float getAttributeFloatValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexFloat(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public double getAttributeDoubleValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexDouble(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public InputStream getAttributeHexBinaryValue(int index) throws XMLStreamException
    {
        String text = _charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM).toString();
        byte[] buf = HexBin.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid hexBinary value", _charSeq.getLocation());
    }

    public InputStream getAttributeBase64Value(int index) throws XMLStreamException
    {
        String text = _charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM).toString();
        byte[] buf = Base64.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid base64Binary value", _charSeq.getLocation());
    }

    public XmlCalendar getAttributeCalendarValue(int index) throws XMLStreamException
    {
        try
        {
            return new GDateBuilder(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM)).
                getCalendar();
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public Date getAttributeDateValue(int index) throws XMLStreamException
    {
        try
        {
            return new GDateBuilder(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM))
                .getDate();
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public GDate getAttributeGDateValue(int index) throws XMLStreamException
    {
        try
        {
            return new GDate(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public GDuration getAttributeGDurationValue(int index) throws XMLStreamException
    {
        try
        {
            return new GDuration(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public QName getAttributeQNameValue(int index) throws XMLStreamException
    {
        try
        {
            return XsTypeConverter.lexQName(_charSeq.reloadAtt(index, CharSeqTrimWS.XMLWHITESPACE_TRIM),
                _xmlStream.getNamespaceContext());
        }
        catch(InvalidLexicalValueException e)
        {
            throw new InvalidLexicalValueException(e.getMessage(), _charSeq.getLocation());
        }
    }

    public String getAttributeStringValue(String uri, String local) throws XMLStreamException
    {
        return _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_PRESERVE).toString();
    }

    public String getAttributeStringValue(String uri, String local, int wsStyle) throws XMLStreamException
    {
        return XmlWhitespace.collapse(_xmlStream.getAttributeValue(uri, local), wsStyle);
    }

    public boolean getAttributeBooleanValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexBoolean(cs);
        }
        catch(InvalidLexicalValueException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public byte getAttributeByteValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexByte(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public short getAttributeShortValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexShort(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public int getAttributeIntValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexInt(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public long getAttributeLongValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexLong(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public BigInteger getAttributeBigIntegerValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexInteger(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public BigDecimal getAttributeBigDecimalValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexDecimal(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public float getAttributeFloatValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexFloat(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public double getAttributeDoubleValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexDouble(cs);
        }
        catch(NumberFormatException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public InputStream getAttributeHexBinaryValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        String text = cs.toString();
        byte[] buf = HexBin.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid hexBinary value", _charSeq.getLocation());
    }

    public InputStream getAttributeBase64Value(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        String text = cs.toString();
        byte[] buf = Base64.decode(text.getBytes());
        if (buf!=null)
            return new ByteArrayInputStream(buf);
        else
            throw new InvalidLexicalValueException("invalid base64Binary value", _charSeq.getLocation());
    }

    public XmlCalendar getAttributeCalendarValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return new GDateBuilder(cs).getCalendar();
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public Date getAttributeDateValue(String uri, String local) throws XMLStreamException
    {
        try
        {
            CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
            return new GDateBuilder(cs).getDate();
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public GDate getAttributeGDateValue(String uri, String local) throws XMLStreamException
    {
        try
        {
            CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
            return new GDate(cs);
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public GDuration getAttributeGDurationValue(String uri, String local) throws XMLStreamException
    {
        try
        {
            return new GDuration(_charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM));
        }
        catch(IllegalArgumentException e)
        {
            throw new InvalidLexicalValueException(e, _charSeq.getLocation());
        }
    }

    public QName getAttributeQNameValue(String uri, String local) throws XMLStreamException
    {
        CharSequence cs = _charSeq.reloadAtt(uri, local, CharSeqTrimWS.XMLWHITESPACE_TRIM);
        try
        {
            return XsTypeConverter.lexQName(cs, _xmlStream.getNamespaceContext());
        }
        catch (InvalidLexicalValueException e)
        {
            throw new InvalidLexicalValueException(e.getMessage(), _charSeq.getLocation());
        }
    }

    public void setDefaultValue(String defaultValue) throws XMLStreamException
    {
        _defaultValue = defaultValue;
    }

    /**
     * Only trims the XML whaitspace at edges, it should not be used for WS collapse
     * Used for int, short, byte
     */
    private static class CharSeqTrimWS
        implements CharSequence
    {
        final static int XMLWHITESPACE_PRESERVE = 1;
        final static int XMLWHITESPACE_TRIM = 2;

        private static int INITIAL_SIZE = 100;
        private char[] _buf = new char[INITIAL_SIZE];
        private int _start, _length = 0;
        private int _nonWSStart = 0;
        private int _nonWSEnd = 0;
        private String _toStringValue;
        private XMLStreamReaderExtImpl _xmlSteam;
        //private boolean _supportForGetTextCharacters = true;
        private final ExtLocation _location;
        private boolean _hasText;

        CharSeqTrimWS(XMLStreamReaderExtImpl xmlSteam)
        {
            _xmlSteam = xmlSteam;
            _location = new ExtLocation();
        }

        void reload(int style)
            throws XMLStreamException
        {
            _toStringValue = null;
            _location.reset();
            _hasText = false;

            fillBuffer();

            if (style==XMLWHITESPACE_PRESERVE)
            {
                _nonWSStart = 0;
                _nonWSEnd = _length;

                // takes defaultValue only if there wasn't any text
                if (!_hasText && _xmlSteam._defaultValue!=null)
                {
                    _length = 0;
                    fillBufferFromString(_xmlSteam._defaultValue);
                }
            }
            else if (style==XMLWHITESPACE_TRIM)
            {
                for (_nonWSStart=0; _nonWSStart<_length; _nonWSStart++)
                    if (!XMLChar.isSpace(_buf[_nonWSStart]))
                        break;
                for (_nonWSEnd=_length; _nonWSEnd>_nonWSStart; _nonWSEnd--)
                    if (!XMLChar.isSpace(_buf[_nonWSEnd-1]))
                        break;

                // takes defaultValue if length after triming is 0
                if (length()==0 && _xmlSteam._defaultValue!=null)
                {
                    _length = 0;
                    fillBufferFromString(_xmlSteam._defaultValue);

                    //apply whispace rule on the default value
                    for (_nonWSStart=0; _nonWSStart<_length; _nonWSStart++)
                        if (!XMLChar.isSpace(_buf[_nonWSStart]))
                            break;
                    for (_nonWSEnd=_length; _nonWSEnd>_nonWSStart; _nonWSEnd--)
                        if (!XMLChar.isSpace(_buf[_nonWSEnd-1]))
                            break;
                }
            }
            _xmlSteam._defaultValue = null;
        }

        private void fillBuffer()
            throws XMLStreamException
        {
            _length = 0;

            if (_xmlSteam.getEventType() == XMLStreamReader.START_DOCUMENT)
                _xmlSteam.next();
            if (_xmlSteam.isStartElement())
                _xmlSteam.next();

            int depth = 0;
            String error = null;
            int eventType = _xmlSteam.getEventType();

            loop:
            while(true)
            {
                switch(eventType)
                {
                case XMLStreamReader.CDATA:
                case XMLStreamReader.CHARACTERS:
                case XMLStreamReader.SPACE:
                    _location.set(_xmlSteam.getLocation());

                    if (depth==0)
                        addTextToBuffer();

                    break;

                case XMLStreamReader.ATTRIBUTE:
                case XMLStreamReader.COMMENT:
                case XMLStreamReader.DTD:
                case XMLStreamReader.ENTITY_DECLARATION:
                case XMLStreamReader.NAMESPACE:
                case XMLStreamReader.NOTATION_DECLARATION:
                case XMLStreamReader.PROCESSING_INSTRUCTION:
                case XMLStreamReader.START_DOCUMENT:
                    // ignore
                    break;

                case XMLStreamReader.END_DOCUMENT:
                    _location.set(_xmlSteam.getLocation());

                    break loop;

                case XMLStreamReader.END_ELEMENT:
                    _location.set(_xmlSteam.getLocation());
                    depth--;
                    if (depth<0)
                        break loop;
                    break;

                case XMLStreamReader.ENTITY_REFERENCE:
                    _location.set(_xmlSteam.getLocation());

                    addEntityToBuffer();
                    break;

                case XMLStreamReader.START_ELEMENT:
                    depth++;
                    error = "Unexpected element '" + _xmlSteam.getName() + "' in text content.";
                    _location.set(_xmlSteam.getLocation());

                    break;
                }
                eventType = _xmlSteam.next();
            }
            if (error!=null)
                throw new XMLStreamException(error);
        }

        private void ensureBufferLength(int lengthToAdd)
        {
            if (_length + lengthToAdd>_buf.length)
            {
                char[] newBuf = new char[_length + lengthToAdd];
                if (_length>0)
                    System.arraycopy(_buf, 0, newBuf, 0, _length);
                _buf = newBuf;
            }
        }

        private void fillBufferFromString(CharSequence value)
        {
            int textLength = value.length();
            ensureBufferLength(textLength);

            for (int i=0; i<textLength; i++)
            {
                _buf[i] = value.charAt(i);
            }
            _length = textLength;
        }

        private void addTextToBuffer()
        {
            _hasText = true;
            int textLength = _xmlSteam.getTextLength();
            ensureBufferLength(textLength);

            /*
            Commented out as part of the receipt of the more up to date
            jsr173_1.0_ri.jar. getTextCharacters(int, char[], int, int)
            used to throw UnsupportedOperationException always. Now it no longer
            does, but getTextCharacters(int, char[], int, int) does not return what
            we expect. So reverting to always calling getTextCharacters() until
            we can work out whether it's us that's wrong or them.

            if (_supportForGetTextCharacters)
                try
                {
                    _length = _xmlSteam.getTextCharacters(0, _buf, _length, textLength);
                }
                catch(Exception e)
                {
                    _supportForGetTextCharacters = false;
                }
            */

            // if(!_supportForGetTextCharacters)
            //{
            System.arraycopy(_xmlSteam.getTextCharacters(), _xmlSteam.getTextStart(), _buf, _length, textLength);
            _length = _length + textLength;
            //}
        }

        private void addEntityToBuffer()
        {
            String text = _xmlSteam.getText();

            int textLength = text.length();
            ensureBufferLength(textLength);

            text.getChars(0, text.length(), _buf, _length);
            _length = _length + text.length();
        }

        CharSequence reloadAtt(int index, int style)
            throws XMLStreamException
        {
            _location.reset();
            _location.set(_xmlSteam.getLocation());
            String value = _xmlSteam.getAttributeValue(index);

            if (value==null && _xmlSteam._defaultValue!=null)
                value = _xmlSteam._defaultValue;

            _xmlSteam._defaultValue = null;

            int length = value.length();

            if (style==XMLWHITESPACE_PRESERVE)
            {
                return value;
            }
            else if (style==XMLWHITESPACE_TRIM)
            {
                int nonWSStart, nonWSEnd;
                for (nonWSStart=0; nonWSStart<length; nonWSStart++)
                    if (!XMLChar.isSpace(value.charAt(nonWSStart)))
                        break;
                for (nonWSEnd=length; nonWSEnd>nonWSStart; nonWSEnd--)
                    if (!XMLChar.isSpace(value.charAt(nonWSEnd-1)))
                        break;
                if (nonWSStart==0 && nonWSEnd==length)
                    return value;
                else
                    return value.subSequence(nonWSStart, nonWSEnd);
            }

            throw new IllegalStateException("unknown style");
        }

        CharSequence reloadAtt(String uri, String local, int style)
            throws XMLStreamException
        {
            _location.reset();
            _location.set(_xmlSteam.getLocation());
            String value = _xmlSteam.getAttributeValue(uri, local);

            if (value==null && _xmlSteam._defaultValue!=null)
                value = _xmlSteam._defaultValue;

            _xmlSteam._defaultValue = null;

            int length = value.length();

            if (style==XMLWHITESPACE_PRESERVE)
            {
                return value;
            }
            else if (style==XMLWHITESPACE_TRIM)
            {
                for (_nonWSStart=0; _nonWSStart<length; _nonWSStart++)
                    if (!XMLChar.isSpace(value.charAt(_nonWSStart)))
                        break;
                for (_nonWSEnd=length; _nonWSEnd>_nonWSStart; _nonWSEnd--)
                    if (!XMLChar.isSpace(value.charAt(_nonWSEnd-1)))
                        break;
                if (_nonWSStart==0 && _nonWSEnd==length)
                    return value;
                else
                    return value.subSequence(_nonWSStart, _nonWSEnd);
            }
            throw new IllegalStateException("unknown style");
        }

        Location getLocation()
        {
            ExtLocation loc = new ExtLocation();
            loc.set(_location);
            return loc;
        }

        public int length()
        {
            return _nonWSEnd - _nonWSStart;
        }

        public char charAt(int index)
        {
            // for each char, this has to be fast, using assert instead of if throw
            assert (index<_nonWSEnd-_nonWSStart && -1<index) :
                "Index " + index + " must be >-1 and <" + (_nonWSEnd - _nonWSStart);

            return _buf[_nonWSStart + index];
        }

        public CharSequence subSequence(int start, int end)
        {
            return new String(_buf, _nonWSStart + start, end - start);
        }

        public String toString()
        {
            if (_toStringValue!=null)
                return _toStringValue;

            _toStringValue = new String(_buf, _nonWSStart, _nonWSEnd - _nonWSStart);
            return _toStringValue;
        }

        private static class ExtLocation implements Location
        {
            private int _line;
            private int _col;
            private int _off;
            private String _pid;
            private String _sid;
            private boolean _isSet;

            ExtLocation()
            {
                _isSet = false;
            }

            public int getLineNumber()
            {
                if (_isSet)
                    return _line;
                else
                    throw new IllegalStateException();
            }

            public int getColumnNumber()
            {
                if (_isSet)
                    return _col;
                else
                    throw new IllegalStateException();
            }

            public int getCharacterOffset()
            {
                if (_isSet)
                    return _off;
                else
                    throw new IllegalStateException();
            }

            public String getPublicId()
            {
                if (_isSet)
                    return _pid;
                else
                    throw new IllegalStateException();
            }

            public String getSystemId()
            {
                if (_isSet)
                    return _sid;
                else
                    throw new IllegalStateException();
            }

            void set(Location loc)
            {
                if (_isSet)
                    return;

                _isSet = true;
                _line = loc.getLineNumber();
                _col = loc.getColumnNumber();
                _off = loc.getCharacterOffset();
                _pid = loc.getPublicId();
                _sid = loc.getSystemId();
            }

            void reset()
            {
                _isSet = false;
            }
        }
    }

    // XMLStreamReader methods
    public Object getProperty(String s)
        throws IllegalArgumentException
    {
        return _xmlStream.getProperty(s);
    }

    public int next()
        throws XMLStreamException
    {
        return _xmlStream.next();
    }

    public void require(int i, String s, String s1)
        throws XMLStreamException
    {
        _xmlStream.require(i, s, s1);
    }

    public String getElementText() throws XMLStreamException
    {
        return _xmlStream.getElementText();
    }

    public int nextTag() throws XMLStreamException
    {
        return _xmlStream.nextTag();
    }

    public boolean hasNext() throws XMLStreamException
    {
        return _xmlStream.hasNext();
    }

    public void close() throws XMLStreamException
    {
        _xmlStream.close();
    }

    public String getNamespaceURI(String s)
    {
        return _xmlStream.getNamespaceURI(s);
    }

    public boolean isStartElement()
    {
        return _xmlStream.isStartElement();
    }

    public boolean isEndElement()
    {
        return _xmlStream.isEndElement();
    }

    public boolean isCharacters()
    {
        return _xmlStream.isCharacters();
    }

    public boolean isWhiteSpace()
    {
        return _xmlStream.isWhiteSpace();
    }

    public String getAttributeValue(String s, String s1)
    {
        return _xmlStream.getAttributeValue(s, s1);
    }

    public int getAttributeCount()
    {
        return _xmlStream.getAttributeCount();
    }

    public QName getAttributeName(int i)
    {
        return _xmlStream.getAttributeName(i);
    }

    public String getAttributeNamespace(int i)
    {
        return _xmlStream.getAttributeNamespace(i);
    }

    public String getAttributeLocalName(int i)
    {
        return _xmlStream.getAttributeLocalName(i);
    }

    public String getAttributePrefix(int i)
    {
        return _xmlStream.getAttributePrefix(i);
    }

    public String getAttributeType(int i)
    {
        return _xmlStream.getAttributeType(i);
    }

    public String getAttributeValue(int i)
    {
        return _xmlStream.getAttributeValue(i);
    }

    public boolean isAttributeSpecified(int i)
    {
        return _xmlStream.isAttributeSpecified(i);
    }

    public int getNamespaceCount()
    {
        return _xmlStream.getNamespaceCount();
    }

    public String getNamespacePrefix(int i)
    {
        return _xmlStream.getNamespacePrefix(i);
    }

    public String getNamespaceURI(int i)
    {
        return _xmlStream.getNamespaceURI(i);
    }

    public NamespaceContext getNamespaceContext()
    {
        return _xmlStream.getNamespaceContext();
    }

    public int getEventType()
    {
        return _xmlStream.getEventType();
    }

    public String getText()
    {
        return _xmlStream.getText();
    }

    public char[] getTextCharacters()
    {
        return _xmlStream.getTextCharacters();
    }

    public int getTextCharacters(int i, char[] chars, int i1, int i2)
        throws XMLStreamException
    {
        return _xmlStream.getTextCharacters(i, chars, i1, i2);
    }

    public int getTextStart()
    {
        return _xmlStream.getTextStart();
    }

    public int getTextLength()
    {
        return _xmlStream.getTextLength();
    }

    public String getEncoding()
    {
        return _xmlStream.getEncoding();
    }

    public boolean hasText()
    {
        return _xmlStream.hasText();
    }

    public Location getLocation()
    {
        return _xmlStream.getLocation();
    }

    public QName getName()
    {
        return _xmlStream.getName();
    }

    public String getLocalName()
    {
        return _xmlStream.getLocalName();
    }

    public boolean hasName()
    {
        return _xmlStream.hasName();
    }

    public String getNamespaceURI()
    {
        return _xmlStream.getNamespaceURI();
    }

    public String getPrefix()
    {
        return _xmlStream.getPrefix();
    }

    public String getVersion()
    {
        return _xmlStream.getVersion();
    }

    public boolean isStandalone()
    {
        return _xmlStream.isStandalone();
    }

    public boolean standaloneSet()
    {
        return _xmlStream.standaloneSet();
    }

    public String getCharacterEncodingScheme()
    {
        return _xmlStream.getCharacterEncodingScheme();
    }

    public String getPITarget()
    {
        return _xmlStream.getPITarget();
    }

    public String getPIData()
    {
        return _xmlStream.getPIData();
    }
}
