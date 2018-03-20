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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

public class ReaderInputStream extends PushedInputStream
{
    private Reader reader;
    private Writer writer;
    private char[] buf;
    public static int defaultBufferSize = 2048;

    public ReaderInputStream(Reader reader, String encoding) throws UnsupportedEncodingException
    {
        this(reader, encoding, defaultBufferSize);
    }

    public ReaderInputStream(Reader reader, String encoding, int bufferSize) throws UnsupportedEncodingException
    {
        if (bufferSize <= 0)
            throw new IllegalArgumentException("Buffer size <= 0");

        this.reader = reader;
        this.writer = new OutputStreamWriter(getOutputStream(), encoding);
        buf = new char[bufferSize];
    }

    public void fill(int requestedBytes) throws IOException
    {
        do
        {
            int chars = reader.read(buf);
            if (chars < 0)
                return;

            writer.write(buf, 0, chars);
            writer.flush();
        }
        while (available() <= 0); // loop for safety, in case encoding didn't produce any bytes yet
    }
}
