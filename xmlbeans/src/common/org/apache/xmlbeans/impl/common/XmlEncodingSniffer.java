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

package org.apache.xmlbeans.impl.common;

import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

public class XmlEncodingSniffer
{
    private String      _xmlencoding;
    private String      _javaencoding;
    private InputStream _stream;
    private Reader      _reader;

    /**
     * Sniffs the given XML stream for encoding information.
     *
     * After a sniffer is constructed, it can return either a stream
     * (which is a buffered stream wrapper of the original) or a reader
     * (which applies the proper encoding).
     *
     * @param stream           The stream to sniff
     * @param encodingOverride The XML (IANA) name for the overriding encoding
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public XmlEncodingSniffer(InputStream stream, String encodingOverride)
        throws IOException, UnsupportedEncodingException
    {
        _stream = stream;
        
        if (encodingOverride != null)
            _xmlencoding = EncodingMap.getJava2IANAMapping(encodingOverride);

        if (_xmlencoding == null)
            _xmlencoding = encodingOverride;

        if (_xmlencoding == null)
        {
            SniffedXmlInputStream sniffed = new SniffedXmlInputStream(_stream);
            _xmlencoding = sniffed.getXmlEncoding();
            assert(_xmlencoding != null);
            _stream = sniffed;
        }

        _javaencoding = EncodingMap.getIANA2JavaMapping(_xmlencoding);
        
        // we allow you to use Java's encoding names in XML even though you're
        // not supposed to.
        
        if (_javaencoding == null)
            _javaencoding = _xmlencoding;
    }

    /**
     * Sniffs the given XML stream for encoding information.
     *
     * After a sniffer is constructed, it can return either a reader
     * (which is a buffered stream wrapper of the original) or a stream
     * (which applies the proper encoding).
     *
     * @param reader           The reader to sniff
     * @param encodingDefault  The Java name for the default encoding to apply, UTF-8 if null.
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public XmlEncodingSniffer(Reader reader, String encodingDefault)
            throws IOException, UnsupportedEncodingException
    {
        if (encodingDefault == null)
            encodingDefault = "UTF-8";
        
        SniffedXmlReader sniffedReader = new SniffedXmlReader(reader);
        _reader = sniffedReader;
        _xmlencoding = sniffedReader.getXmlEncoding();

        if (_xmlencoding == null)
        {
            _xmlencoding = EncodingMap.getJava2IANAMapping(encodingDefault);
            if (_xmlencoding != null)
                _javaencoding = encodingDefault;
            else
                _xmlencoding = encodingDefault;
        }

        if (_xmlencoding == null)
            _xmlencoding = "UTF-8";
        
        // we allow you to use Java's encoding names in XML even though you're
        // not supposed to.
        
        _javaencoding = EncodingMap.getIANA2JavaMapping(_xmlencoding);
        
        if (_javaencoding == null)
            _javaencoding = _xmlencoding;
    }

    public String getXmlEncoding()
    {
        return _xmlencoding;
    }

    public String getJavaEncoding()
    {
        return _javaencoding;
    }

    public InputStream getStream()
            throws UnsupportedEncodingException
    {
        if (_stream != null)
        {
            InputStream is = _stream;
            _stream = null;
            return is;
        }

        if (_reader != null)
        {
            InputStream is = new ReaderInputStream( _reader, _javaencoding );
            _reader = null;
            return is;
        }

        return null;
    }


    public Reader getReader ( )
        throws UnsupportedEncodingException
    {
        if (_reader != null)
        {
            Reader reader = _reader;
            _reader = null;
            return reader;
        }

        if (_stream != null)
        {
            Reader reader = new InputStreamReader( _stream, _javaencoding );
            _stream = null;
            return reader;
        }

        return null;
    }
}
