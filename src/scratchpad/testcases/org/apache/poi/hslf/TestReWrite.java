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

package org.apache.poi.hslf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.TempFile;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that HSLFSlideShow writes the powerpoint bit of data back out
 *  correctly. Currently, that means being the same as what it read in
 *
 * @author Nick Burch (nick at torchbox dot com)
 */
public final class TestReWrite {
    // HSLFSlideShow primed on the test data
    private HSLFSlideShowImpl hssA;
    private HSLFSlideShowImpl hssB;
    private HSLFSlideShowImpl hssC;
    // POIFS primed on the test data
    private POIFSFileSystem pfsA;
    private POIFSFileSystem pfsB;
    private POIFSFileSystem pfsC;

    @Before
    public void setUp() throws Exception {

        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();
        
        pfsA = new POIFSFileSystem(slTests.openResourceAsStream("basic_test_ppt_file.ppt"));
        hssA = new HSLFSlideShowImpl(pfsA);

        pfsB = new POIFSFileSystem(slTests.openResourceAsStream("ParagraphStylesShorterThanCharStyles.ppt"));
        hssB = new HSLFSlideShowImpl(pfsB);

        pfsC = new POIFSFileSystem(slTests.openResourceAsStream("WithMacros.ppt"));
        hssC = new HSLFSlideShowImpl(pfsC);
    }

    @Test
    public void testWritesOutTheSame() throws Exception {
        assertWritesOutTheSame(hssA, pfsA);
        assertWritesOutTheSame(hssB, pfsB);
    }
    
    public void assertWritesOutTheSame(HSLFSlideShowImpl hss, POIFSFileSystem pfs) throws Exception {
        // Write out to a byte array, and to a temp file
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        hss.write(baos);
        
        final File file = TempFile.createTempFile("TestHSLF", ".ppt");
        final File file2 = TempFile.createTempFile("TestHSLF", ".ppt");
        hss.write(file);
        hss.write(file2);
        

        // Build an input stream of it, and read back as a POIFS from the stream
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        POIFSFileSystem npfS = new POIFSFileSystem(bais);
        
        // And the same on the temp file
        POIFSFileSystem npfF = new POIFSFileSystem(file);
        
        // And another where we do an in-place write
        POIFSFileSystem npfRF = new POIFSFileSystem(file2, false);
        HSLFSlideShowImpl hssRF = new HSLFSlideShowImpl(npfRF);
        hssRF.write();
        hssRF.close();
        npfRF = new POIFSFileSystem(file2);
        
        // Check all of them in turn
        for (POIFSFileSystem npf : new POIFSFileSystem[] { npfS, npfF, npfRF }) {
            // Check that the "PowerPoint Document" sections have the same size
            DocumentEntry oProps = (DocumentEntry)pfs.getRoot().getEntry(HSLFSlideShow.POWERPOINT_DOCUMENT);
            DocumentEntry nProps = (DocumentEntry)npf.getRoot().getEntry(HSLFSlideShow.POWERPOINT_DOCUMENT);
            assertEquals(oProps.getSize(),nProps.getSize());
    
            // Check that they contain the same data
            byte[] _oData = new byte[oProps.getSize()];
            byte[] _nData = new byte[nProps.getSize()];
            pfs.createDocumentInputStream(HSLFSlideShow.POWERPOINT_DOCUMENT).read(_oData);
            npf.createDocumentInputStream(HSLFSlideShow.POWERPOINT_DOCUMENT).read(_nData);
            for(int i=0; i<_oData.length; i++) {
                //System.out.println(i + "\t" + Integer.toHexString(i));
                assertEquals(_oData[i], _nData[i]);
            }
            npf.close();
        }
    }

    @Test
    public void testWithMacroStreams() throws IOException {
        // Check that they're apparently the same
        assertSlideShowWritesOutTheSame(hssC, pfsC);

        // Currently has a Macros stream
        assertNotNull( pfsC.getRoot().getEntry("Macros") );

        // Write out normally, will loose the macro stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        hssC.write(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        POIFSFileSystem pfsNew = new POIFSFileSystem(bais);
        assertFalse(pfsNew.getRoot().hasEntry("Macros"));
        pfsNew.close();

        // But if we write out with nodes preserved, will be there
        baos.reset();
        hssC.write(baos, true);
        bais = new ByteArrayInputStream(baos.toByteArray());
        pfsNew = new POIFSFileSystem(bais);
        assertTrue( pfsNew.getRoot().hasEntry("Macros") );
        pfsNew.close();
    }

    /**
     * Ensure that simply opening a slideshow (usermodel) view of it
     *  doesn't change things
     */
    @Test
    public void testSlideShowWritesOutTheSame() throws Exception {
        assertSlideShowWritesOutTheSame(hssA, pfsA);

        // Some bug in StyleTextPropAtom rewriting means this will fail
        // We need to identify and fix that first
        //assertSlideShowWritesOutTheSame(hssB, pfsB);
    }
    
    public void assertSlideShowWritesOutTheSame(HSLFSlideShowImpl hss, POIFSFileSystem pfs) throws IOException {
        // Create a slideshow covering it
        @SuppressWarnings("resource")
        HSLFSlideShow ss = new HSLFSlideShow(hss);
        ss.getSlides();
        ss.getNotes();

        // Now write out to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        hss.write(baos);

        // Build an input stream of it
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        // Use POIFS to query that lot
        POIFSFileSystem npfs = new POIFSFileSystem(bais);

        // Check that the "PowerPoint Document" sections have the same size
        DocumentEntry oProps = (DocumentEntry)pfs.getRoot().getEntry(HSLFSlideShow.POWERPOINT_DOCUMENT);
        DocumentEntry nProps = (DocumentEntry)npfs.getRoot().getEntry(HSLFSlideShow.POWERPOINT_DOCUMENT);
        assertEquals(oProps.getSize(),nProps.getSize());

        // Check that they contain the same data
        byte[] _oData = new byte[oProps.getSize()];
        byte[] _nData = new byte[nProps.getSize()];
        pfs.createDocumentInputStream(HSLFSlideShow.POWERPOINT_DOCUMENT).read(_oData);
        npfs.createDocumentInputStream(HSLFSlideShow.POWERPOINT_DOCUMENT).read(_nData);
        for(int i=0; i<_oData.length; i++) {
            if(_oData[i] != _nData[i])
                System.out.println(i + "\t" + Integer.toHexString(i));
            assertEquals(_oData[i], _nData[i]);
        }
        npfs.close();
    }
    
    @Test
    public void test48593() throws IOException {
        HSLFSlideShow ppt1 = new HSLFSlideShow();
        ppt1.createSlide();
        HSLFSlideShow ppt2 = HSLFTestDataSamples.writeOutAndReadBack(ppt1);
        ppt2.createSlide();
        HSLFSlideShow ppt3 = HSLFTestDataSamples.writeOutAndReadBack(ppt2);
        ppt3.createSlide();
        HSLFSlideShow ppt4 = HSLFTestDataSamples.writeOutAndReadBack(ppt3);
        ppt4.createSlide();
        HSLFTestDataSamples.writeOutAndReadBack(ppt4).close();
        ppt4.close();
        ppt3.close();
        ppt2.close();
        ppt1.close();
    }
}
