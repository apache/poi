/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.poifs.poibrowser;

import java.io.*;
import org.apache.poi.poifs.filesystem.*;

/**
 * <p>Describes the most important (whatever that is) features of a
 * {@link POIFSDocument}.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class DocumentDescriptor
{
    String name;
    POIFSDocumentPath path;
    DocumentInputStream stream;

    int size;
    byte[] bytes;


    /**
     * <p>Creates a {@link DocumentDescriptor}.</p>
     *
     * @param name The stream's name.
     *
     * @param path The stream's path in the POI filesystem hierarchy.
     *
     * @param stream The stream.
     *
     * @param nrOfBytes The maximum number of bytes to display in a
     * dump starting at the beginning of the stream.
     */
    public DocumentDescriptor(final String name,
                              final POIFSDocumentPath path,
                              final DocumentInputStream stream,
                              final int nrOfBytes)
    {
        this.name = name;
        this.path = path;
        this.stream = stream;
        try
        {
            size = stream.available();
            if (stream.markSupported())
            {
                stream.mark(nrOfBytes);
                final byte[] b = new byte[nrOfBytes];
                final int read = stream.read(b, 0, Math.min(size, b.length));
                bytes = new byte[read];
                System.arraycopy(b, 0, bytes, 0, read);
                stream.reset();
            }
        }
        catch (IOException ex)
        {
            System.out.println(ex);
        }
    }

}
