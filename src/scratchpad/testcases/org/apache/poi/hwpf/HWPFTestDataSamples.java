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
package org.apache.poi.hwpf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

public class HWPFTestDataSamples {

    private static final POILogger logger = POILogFactory
            .getLogger( HWPFTestDataSamples.class );

    public static HWPFDocument openSampleFile(String sampleFileName) {
        try {
            InputStream is = POIDataSamples.getDocumentInstance().openResourceAsStream(sampleFileName);
            return new HWPFDocument(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static HWPFDocument openSampleFileFromArchive( String sampleFileName )
    {
        final long start = System.currentTimeMillis();
        try
        {
            ZipInputStream is = new ZipInputStream( POIDataSamples
                    .getDocumentInstance()
                    .openResourceAsStream( sampleFileName ) );
            try
            {
                is.getNextEntry();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try
                {
                    IOUtils.copy( is, baos );
                }
                finally
                {
                    baos.close();
                }

                final long endUnzip = System.currentTimeMillis();
                byte[] byteArray = baos.toByteArray();

                logger.log( POILogger.DEBUG, "Unzipped in ",
                        Long.valueOf( endUnzip - start ), " ms -- ",
                        Long.valueOf( byteArray.length ), " byte(s)" );

                ByteArrayInputStream bais = new ByteArrayInputStream( byteArray );
                HWPFDocument doc = new HWPFDocument( bais );
                final long endParse = System.currentTimeMillis();

                logger.log( POILogger.DEBUG, "Parsed in ",
                        Long.valueOf( endParse - start ), " ms" );

                return doc;
            }
            finally
            {
                is.close();
            }
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static HWPFOldDocument openOldSampleFile(String sampleFileName) {
       try {
           InputStream is = POIDataSamples.getDocumentInstance().openResourceAsStream(sampleFileName);
           return new HWPFOldDocument(new POIFSFileSystem(is));
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
   }
    /**
     * Writes a spreadsheet to a <tt>ByteArrayOutputStream</tt> and reads it back
     * from a <tt>ByteArrayInputStream</tt>.<p/>
     * Useful for verifying that the serialisation round trip
     */
    public static HWPFDocument writeOutAndReadBack(HWPFDocument original) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            original.write(baos);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            return new HWPFDocument(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
