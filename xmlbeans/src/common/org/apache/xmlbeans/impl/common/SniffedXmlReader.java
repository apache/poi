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

import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import java.nio.charset.Charset;

public class SniffedXmlReader extends BufferedReader
{
    // We don't sniff more than 192 bytes.
    public static int MAX_SNIFFED_CHARS = 192;

    public SniffedXmlReader(Reader reader) throws IOException
    {
        super(reader);
        _encoding = sniffForXmlDecl();
    }

    private int readAsMuchAsPossible(char[] buf, int startAt, int len) throws IOException
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

    // BUGBUG in JDK: Charset.forName is not threadsafe, so we'll prime it
    // with the common charsets.

    private static Charset dummy1 = Charset.forName("UTF-8");
    private static Charset dummy2 = Charset.forName("UTF-16");
    private static Charset dummy3 = Charset.forName("UTF-16BE");
    private static Charset dummy4 = Charset.forName("UTF-16LE");
    private static Charset dummy5 = Charset.forName("ISO-8859-1");
    private static Charset dummy6 = Charset.forName("US-ASCII");
    private static Charset dummy7 = Charset.forName("Cp1252");


    private String sniffForXmlDecl() throws IOException
    {
        mark(MAX_SNIFFED_CHARS);
        try
        {
            char[] buf = new char[MAX_SNIFFED_CHARS];
            int limit = readAsMuchAsPossible(buf, 0, MAX_SNIFFED_CHARS);
            return SniffedXmlInputStream.extractXmlDeclEncoding(buf, 0, limit);
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
}
