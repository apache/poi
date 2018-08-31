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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.CustomProperty;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.hpsf.MarkUnsupportedException;
import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests some HPSF functionality by reading all property sets from all files
 * in the "data" directory. If you want to ensure HPSF can deal with a certain
 * OLE2 file, just add it to the "data" directory and run this test case.
 */
@RunWith(Parameterized.class)
public class TestReadAllFiles {
    private static final POIDataSamples _samples = POIDataSamples.getHPSFInstance();
    
    @Parameters(name="{index}: {0} using {1}")
    public static Iterable<Object[]> files() {
        final List<Object[]> files = new ArrayList<>();
        
        _samples.getFile("").listFiles(f -> {
            if (f.getName().startsWith("Test")) {
                files.add(new Object[]{ f });
            }
            return false;
        });
        
        return files;
    }

    @Parameter(value=0)
    public File file;

    /**
     * This test methods reads all property set streams from all POI
     * filesystems in the "data" directory.
     */
    @Test
    public void read() throws IOException, NoPropertySetStreamException, MarkUnsupportedException {
        /* Read the POI filesystem's property set streams: */
        for (POIFile pf : Util.readPropertySets(file)) {
            try (InputStream in = new ByteArrayInputStream(pf.getBytes())) {
                PropertySetFactory.create(in);
            }
        }
    }
    
    
    /**
     * This test method does a write and read back test with all POI
     * filesystems in the "data" directory by performing the following
     * actions for each file:<p>
     *
     * <ul>
     * <li>Read its property set streams.
     * <li>Create a new POI filesystem containing the origin file's property set streams.
     * <li>Read the property set streams from the POI filesystem just created.
     * <li>Compare each property set stream with the corresponding one from
     * the origin file and check whether they are equal.
     * </ul>
     */
    @Test
    public void recreate() throws IOException, HPSFException {
        /* Read the POI filesystem's property set streams: */
        Map<String,PropertySet> psMap = new HashMap<>();
        
        /* Create a new POI filesystem containing the origin file's
         * property set streams: */
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final POIFSFileSystem poiFs = new POIFSFileSystem();
        for (POIFile poifile : Util.readPropertySets(file)) {
            final InputStream in = new ByteArrayInputStream(poifile.getBytes());
            final PropertySet psIn = PropertySetFactory.create(in);
            psMap.put(poifile.getName(), psIn);
            bos.reset();
            psIn.write(bos);
            poiFs.createDocument(new ByteArrayInputStream(bos.toByteArray()), poifile.getName());
        }

        /* Read the property set streams from the POI filesystem just
         * created. */
        for (Map.Entry<String,PropertySet> me : psMap.entrySet()) {
            final PropertySet ps1 = me.getValue();
            final PropertySet ps2 = PropertySetFactory.create(poiFs.getRoot(), me.getKey());
            assertNotNull(ps2);
            
            /* Compare the property set stream with the corresponding one
             * from the origin file and check whether they are equal. */
            
            // Because of missing 0-paddings in the original input files, the bytes might differ.
            // This fixes the comparison
            String ps1str = ps1.toString().replace(" 00", "   ").replace(".", " ").replaceAll("(?m)( +$|(size|offset): [0-9]+)","");
            String ps2str = ps2.toString().replace(" 00", "   ").replace(".", " ").replaceAll("(?m)( +$|(size|offset): [0-9]+)","");
            
            assertEquals("Equality for file " + file.getName(), ps1str, ps2str);
        }
        poiFs.close();
    }
    
    /**
     * This test method checks whether DocumentSummary information streams
     * can be read. This is done by opening all "Test*" files in the 'poifs' directrory
     * pointed to by the "POI.testdata.path" system property, trying to extract
     * the document summary information stream in the root directory and calling
     * its get... methods.
     */
    @Test
    public void readDocumentSummaryInformation() throws Exception {
        /* Read a test document <em>doc</em> into a POI filesystem. */
        try (POIFSFileSystem poifs = new POIFSFileSystem(file, true)) {
            final DirectoryEntry dir = poifs.getRoot();
            /*
             * If there is a document summry information stream, read it from
             * the POI filesystem.
             */
            if (dir.hasEntry(DocumentSummaryInformation.DEFAULT_STREAM_NAME)) {
                final DocumentSummaryInformation dsi = TestWriteWellKnown.getDocumentSummaryInformation(poifs);

                /* Execute the get... methods. */
                dsi.getByteCount();
                dsi.getByteOrder();
                dsi.getCategory();
                dsi.getCompany();
                dsi.getCustomProperties();
                // FIXME dsi.getDocparts();
                // FIXME dsi.getHeadingPair();
                dsi.getHiddenCount();
                dsi.getLineCount();
                dsi.getLinksDirty();
                dsi.getManager();
                dsi.getMMClipCount();
                dsi.getNoteCount();
                dsi.getParCount();
                dsi.getPresentationFormat();
                dsi.getScale();
                dsi.getSlideCount();
            }
        }
    }
    
    /**
     * Tests the simplified custom properties by reading them from the
     * available test files.
     *
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void readCustomPropertiesFromFiles() throws Exception {
        /* Read a test document <em>doc</em> into a POI filesystem. */
        try (POIFSFileSystem poifs = new POIFSFileSystem(file)) {
            /*
             * If there is a document summry information stream, read it from
             * the POI filesystem, else create a new one.
             */
            DocumentSummaryInformation dsi = TestWriteWellKnown.getDocumentSummaryInformation(poifs);
            if (dsi == null) {
                dsi = PropertySetFactory.newDocumentSummaryInformation();
            }
            final CustomProperties cps = dsi.getCustomProperties();

            if (cps == null) {
                /* The document does not have custom properties. */
                return;
            }

            for (CustomProperty cp : cps.properties()) {
                assertNotNull(cp.getName());
                assertNotNull(cp.getValue());
            }
        }
    }

}
