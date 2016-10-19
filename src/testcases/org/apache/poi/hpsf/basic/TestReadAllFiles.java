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

package org.apache.poi.hpsf.basic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.PropertySetFactory;

/**
 * <p>Tests some HPSF functionality by reading all property sets from all files
 * in the "data" directory. If you want to ensure HPSF can deal with a certain
 * OLE2 file, just add it to the "data" directory and run this test case.</p>
 */
public class TestReadAllFiles extends TestCase {
    private static String[] excludes = new String[] {};

    /**
     * <p>This test methods reads all property set streams from all POI
     * filesystems in the "data" directory.</p>
     * 
     * @throws IOException 
     * @throws HPSFException
     */
    public void testReadAllFiles() throws IOException, HPSFException
    {
        POIDataSamples _samples = POIDataSamples.getHPSFInstance();
        final File dataDir = _samples.getFile("");
        final File[] fileList = dataDir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(final File f)
                {
                    // exclude files that we know will fail
                    return f.isFile() && checkExclude(f);
                }
            });

        for (final File f : fileList) {
            /* Read the POI filesystem's property set streams: */
            final POIFile[] psf1 = Util.readPropertySets(f);

            for (int j = 0; j < psf1.length; j++)
            {
                final InputStream in =
                    new ByteArrayInputStream(psf1[j].getBytes());
                try {
                    PropertySetFactory.create(in);
                } catch (Exception e) {
                    throw new IOException("While handling file: " + f + " at " + j, e);
                }
            }
        }
    }

    /**
     * Returns true if the file should be checked, false if it should be excluded.
     *
     * @param f
     * @return
     */
    public static boolean checkExclude(File f) {
        for(String exclude : excludes) {
            if(f.getAbsolutePath().endsWith(exclude)) {
                return false;
            }
        }
        
        return true;
    }
}
