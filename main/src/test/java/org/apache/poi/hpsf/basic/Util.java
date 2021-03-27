
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.util.IOUtils;



/**
 * <p>Static utility methods needed by the HPSF test cases.</p>
 */
final class Util {

    private Util() {
    }

    /**
     * <p>Reads a set of files from a POI filesystem and returns them
     * as an array of {@link POIFile} instances. This method loads all
     * files into memory and thus does not cope well with large POI
     * filessystems.</p>
     *
     * @param poiFs The name of the POI filesystem as seen by the
     * operating system. (This is the "filename".)
     *
     * @param poiFiles The names of the POI files to be read.
     *
     * @return The POI files. The elements are ordered in the same way
     * as the files in the POI filesystem.
     *
     * @exception FileNotFoundException if the file containing the POI
     * filesystem does not exist
     *
     * @exception IOException if an I/O exception occurs
     */
    static List<POIFile> readPOIFiles(final File poiFs, final String... poiFiles) throws IOException {
        final List<POIFile> files = new ArrayList<>();
        POIFSReader r = new POIFSReader();
        POIFSReaderListener pfl = event -> assertDoesNotThrow(() -> {
            final POIFile f = new POIFile();
            f.setName(event.getName());
            f.setPath(event.getPath());
            final InputStream in = event.getStream();
            f.setBytes(IOUtils.toByteArray(in));
            in.close();
            files.add(f);
        });
        if (poiFiles.length == 0) {
            /* Register the listener for all POI files. */
            r.registerListener(pfl);
        } else {
            for (String poiFile : poiFiles) {
                r.registerListener(pfl, poiFile);
            }
        }

        /* Read the POI filesystem. */
        r.read(poiFs);

        return files;
    }



    /**
     * <p>Read all files from a POI filesystem which are property set streams
     * and returns them as an array of {@link org.apache.poi.hpsf.PropertySet}
     * instances.</p>
     *
     * @param poiFs The name of the POI filesystem as seen by the
     * operating system. (This is the "filename".)
     *
     * @return The property sets. The elements are ordered in the same way
     * as the files in the POI filesystem.
     *
     * @exception IOException if an I/O exception occurs
     */
    static List<POIFile> readPropertySets(final File poiFs) throws IOException {
        final List<POIFile> files = new ArrayList<>(7);
        final POIFSReader r = new POIFSReader();
        final POIFSReaderListener pfl = event -> assertDoesNotThrow(() -> {
            final POIFile f = new POIFile();
            f.setName(event.getName());
            f.setPath(event.getPath());
            final InputStream in = event.getStream();
            if (PropertySet.isPropertySetStream(in)) {
                f.setBytes(IOUtils.toByteArray(in));
                files.add(f);
            }
        });

        /* Register the listener for all POI files. */
        r.registerListener(pfl);

        /* Read the POI filesystem. */
        r.read(poiFs);

        return files;
    }
}
