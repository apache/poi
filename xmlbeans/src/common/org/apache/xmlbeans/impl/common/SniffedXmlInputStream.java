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

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class SniffedXmlInputStream extends BufferedInputStream
{
    // We don't sniff more than 192 bytes.
    public static int MAX_SNIFFED_BYTES = 192;

    public SniffedXmlInputStream(InputStream stream) throws IOException
    {
        super(stream);

        // read byte order marks and detect EBCDIC etc
        _encoding = sniffFourBytes();

        if (_encoding != null && _encoding.equals("IBM037"))
        {
            // First four bytes suggest EBCDIC with <?xm at start
            String encoding = sniffForXmlDecl(_encoding);
            if (encoding != null)
                _encoding = encoding;
        }

        if (_encoding == null)
        {
            // Haven't yet determined encoding: sniff for <?xml encoding="..."?>
            // assuming we can read it as UTF-8.
            _encoding = sniffForXmlDecl("UTF-8");
        }

        if (_encoding == null)
        {
            // The XML spec says these two things:

            // (1) "In the absence of external character encoding information
            // (such as MIME headers), parsed entities which are stored in an
            // encoding other than UTF-8 or UTF-16 must begin with a text
            // declaration (see 4.3.1 The Text Declaration) containing an
            // encoding declaration:"

            // (2) "In the absence of information provided by an external
            // transport protocol (e.g. HTTP or MIME), it is an error
            // for an entity including an encoding declaration to be
            // presented to the XML processor in an encoding other than
            // that named in the declaration, or for an entity which begins
            // with neither a Byte Order Mark nor an encoding declaration
            // to use an encoding other than UTF-8."

            // Since we're using a sniffed stream, we do not have external
            // character encoding information.

            // Since we're here, we also don't have a recognized byte order
            // mark or an explicit encoding declaration that can be read in
            // either ASCII or EBDIC style.

            // Therefore, we must use UTF-8.

            _encoding = "UTF-8";
        }
    }

    private int readAsMuchAsPossible(byte[] buf, int startAt, int len) throws IOException
    {
        int total = 0;
        while (total < len)
        {
            int count = read(buf, startAt + total, len - total);
            if (count < 0)
                break;
            total += count;
        }
        return total;
    }

    private String sniffFourBytes() throws IOException
    {
        mark(4);
        int skip = 0;
        try
        {
            byte[] buf = new byte[4];
            if (readAsMuchAsPossible(buf, 0, 4) < 4)
                return null;
            long result = 0xFF000000 & (buf[0] << 24) | 0x00FF0000 & (buf[1] << 16) | 0x0000FF00 & (buf[2] << 8) | 0x000000FF & buf[3];

            if (result == 0x0000FEFF)
                return "UCS-4";
            else if (result == 0xFFFE0000)
                return "UCS-4";
            else if (result == 0x0000003C)
                return "UCS-4BE";
            else if (result == 0x3C000000)
                return "UCS-4LE";
            else if (result == 0x003C003F)
                return "UTF-16BE";
            else if (result == 0x3C003F00)
                return "UTF-16LE";
            else if (result == 0x3C3F786D)
                return null; // looks like US-ASCII with <?xml: sniff
            else if (result == 0x4C6FA794)
                return "IBM037"; // Sniff for ebdic codepage
            else if ((result & 0xFFFF0000) == 0xFEFF0000)
                return "UTF-16";
            else if ((result & 0xFFFF0000) == 0xFFFE0000)
                return "UTF-16";
            else if ((result & 0xFFFFFF00) == 0xEFBBBF00)
                return "UTF-8";
            else return null;
        }
        finally
        {
            reset();
        }
    }

    // BUGBUG in JDK: Charset.forName is not threadsafe, so we'll prime it
    // with the common charsets.

    private static Charset dummy1 = Charset.forName("UTF-8");
    private static Charset dummy2 = Charset.forName("UTF-16");
    private static Charset dummy3 = Charset.forName("UTF-16BE");
    private static Charset dummy4 = Charset.forName("UTF-16LE");
    private static Charset dummy5 = Charset.forName("ISO-8859-1");
    private static Charset dummy6 = Charset.forName("US-ASCII");
    private static Charset dummy7 = Charset.forName("Cp1252");


    private String sniffForXmlDecl(String encoding) throws IOException
    {
        mark(MAX_SNIFFED_BYTES);
        try
        {
            byte[] bytebuf = new byte[MAX_SNIFFED_BYTES];
            int bytelimit = readAsMuchAsPossible(bytebuf, 0, MAX_SNIFFED_BYTES);

            // BUGBUG in JDK: Charset.forName is not threadsafe.
            Charset charset = Charset.forName(encoding);
            Reader reader = new InputStreamReader(new ByteArrayInputStream(bytebuf, 0, bytelimit), charset);
            char[] buf = new char[bytelimit];
            int limit = 0;
            while (limit < bytelimit)
            {
                int count = reader.read(buf, limit, bytelimit - limit);
                if (count < 0)
                    break;
                limit += count;
            }

            return extractXmlDeclEncoding(buf, 0, limit);
        }
        finally
        {
            reset();
        }
    }

    private String _encoding;

    public String getXmlEncoding()
    {
        return _encoding;
    }

    /* package */ static String extractXmlDeclEncoding(char[] buf, int offset, int size)
    {
        int limit = offset + size;
        int xmlpi = firstIndexOf("<?xml", buf, offset, limit);
        if (xmlpi >= 0)
        {
            int i = xmlpi + 5;
            ScannedAttribute attr = new ScannedAttribute();
            while (i < limit)
            {
                i = scanAttribute(buf, i, limit, attr);
                if (i < 0)
                    return null;
                if (attr.name.equals("encoding"))
                    return attr.value;
            }
        }
        return null;
    }

    private static int firstIndexOf(String s, char[] buf, int startAt, int limit)
    {
        assert(s.length() > 0);
        char[] lookFor = s.toCharArray();

        char firstchar = lookFor[0];
        searching: for (limit -= lookFor.length; startAt < limit; startAt++)
        {
            if (buf[startAt] == firstchar)
            {
                for (int i = 1; i < lookFor.length; i++)
                {
                    if (buf[startAt + i] != lookFor[i])
                    {
                        continue searching;
                    }
                }
                return startAt;
            }
        }

        return -1;
    }

    private static int nextNonmatchingByte(char[] lookFor, char[] buf, int startAt, int limit)
    {
        searching: for (; startAt < limit; startAt++)
        {
            int thischar = buf[startAt];
            for (int i = 0; i < lookFor.length; i++)
                if (thischar == lookFor[i])
                    continue searching;
            return startAt;
        }
        return -1;
    }

    private static int nextMatchingByte(char[] lookFor, char[] buf, int startAt, int limit)
    {
        searching: for (; startAt < limit; startAt++)
        {
            int thischar = buf[startAt];
            for (int i = 0; i < lookFor.length; i++)
                if (thischar == lookFor[i])
                    return startAt;
        }
        return -1;
    }

    private static int nextMatchingByte(char lookFor, char[] buf, int startAt, int limit)
    {
        searching: for (; startAt < limit; startAt++)
        {
            if (buf[startAt] == lookFor)
                return startAt;
        }
        return -1;
    }
    private static char[] WHITESPACE = new char[] { ' ', '\r', '\t', '\n' };
    private static char[] NOTNAME = new char[] { '=', ' ', '\r', '\t', '\n', '?', '>', '<', '\'', '\"' };

    private static class ScannedAttribute
    {
        public String name;
        public String value;
    }

    private static int scanAttribute(char[] buf, int startAt, int limit, ScannedAttribute attr)
    {
        int nameStart = nextNonmatchingByte(WHITESPACE, buf, startAt, limit);
        if (nameStart < 0)
            return -1;
        int nameEnd = nextMatchingByte(NOTNAME, buf, nameStart, limit);
        if (nameEnd < 0)
            return -1;
        int equals = nextNonmatchingByte(WHITESPACE, buf, nameEnd, limit);
        if (equals < 0)
            return -1;
        if (buf[equals] != '=')
            return -1;
        int valQuote = nextNonmatchingByte(WHITESPACE, buf, equals + 1, limit);
        if (buf[valQuote] != '\'' && buf[valQuote] != '\"')
            return -1;
        int valEndquote = nextMatchingByte(buf[valQuote], buf, valQuote + 1, limit);
        if (valEndquote < 0)
            return -1;
        attr.name = new String(buf, nameStart, nameEnd - nameStart);
        attr.value = new String(buf, valQuote + 1, valEndquote - valQuote - 1);
        return valEndquote + 1;
    }
}
