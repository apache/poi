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

package org.apache.poi.poifs.macros;

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.skipTest;
import static org.apache.poi.POITestCase.testPassesNow;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.StringUtil;
import org.junit.Ignore;
import org.junit.Test;

public class TestVBAMacroReader {
    private static final Map<POIDataSamples, String> expectedMacroContents;

    protected static String readVBA(POIDataSamples poiDataSamples) {
        File macro = poiDataSamples.getFile("SimpleMacro.vba");
        final byte[] bytes;
        try {
            FileInputStream stream = new FileInputStream(macro);
            try {
                bytes = IOUtils.toByteArray(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String testMacroContents = new String(bytes, StringUtil.UTF8);
        
        if (! testMacroContents.startsWith("Sub ")) {
            throw new IllegalArgumentException("Not a macro");
        }

        return testMacroContents.substring(testMacroContents.indexOf("()")+3);
    }

    static {
        final Map<POIDataSamples, String> _expectedMacroContents = new HashMap<POIDataSamples, String>();
        final POIDataSamples[] dataSamples = {
                POIDataSamples.getSpreadSheetInstance(),
                POIDataSamples.getSlideShowInstance(),
                POIDataSamples.getDocumentInstance(),
                POIDataSamples.getDiagramInstance()
        };
        for (POIDataSamples sample : dataSamples) {
            _expectedMacroContents.put(sample, readVBA(sample));
        }
        expectedMacroContents = Collections.unmodifiableMap(_expectedMacroContents);
    }
    
    //////////////////////////////// From Stream /////////////////////////////
    @Test
    public void HSSFfromStream() throws Exception {
        fromStream(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xls");
    }
    @Test
    public void XSSFfromStream() throws Exception {
        fromStream(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xlsm");
    }
    @Ignore("bug 59302: Found 0 macros")
    @Test
    public void HSLFfromStream() throws Exception {
        fromStream(POIDataSamples.getSlideShowInstance(), "SimpleMacro.ppt");
    }
    @Test
    public void XSLFfromStream() throws Exception {
        fromStream(POIDataSamples.getSlideShowInstance(), "SimpleMacro.pptm");
    }
    @Test
    public void HWPFfromStream() throws Exception {
        fromStream(POIDataSamples.getDocumentInstance(), "SimpleMacro.doc");
    }
    @Test
    public void XWPFfromStream() throws Exception {
        fromStream(POIDataSamples.getDocumentInstance(), "SimpleMacro.docm");
    }
    @Ignore("Found 0 macros")
    @Test
    public void HDGFfromStream() throws Exception {
        fromStream(POIDataSamples.getDiagramInstance(), "SimpleMacro.vsd");
    }
    @Test
    public void XDGFfromStream() throws Exception {
        fromStream(POIDataSamples.getDiagramInstance(), "SimpleMacro.vsdm");
    }

    //////////////////////////////// From File /////////////////////////////
    @Test
    public void HSSFfromFile() throws Exception {
        fromFile(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xls");
    }
    @Test
    public void XSSFfromFile() throws Exception {
        fromFile(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xlsm");
    }
    @Ignore("bug 59302: Found 0 macros")
    @Test
    public void HSLFfromFile() throws Exception {
        fromFile(POIDataSamples.getSlideShowInstance(), "SimpleMacro.ppt");
    }
    @Test
    public void XSLFfromFile() throws Exception {
        fromFile(POIDataSamples.getSlideShowInstance(), "SimpleMacro.pptm");
    }
    @Test
    public void HWPFfromFile() throws Exception {
        fromFile(POIDataSamples.getDocumentInstance(), "SimpleMacro.doc");
    }
    @Test
    public void XWPFfromFile() throws Exception {
        fromFile(POIDataSamples.getDocumentInstance(), "SimpleMacro.docm");
    }
    @Ignore("Found 0 macros")
    @Test
    public void HDGFfromFile() throws Exception {
        fromFile(POIDataSamples.getDiagramInstance(), "SimpleMacro.vsd");
    }
    @Test
    public void XDGFfromFile() throws Exception {
        fromFile(POIDataSamples.getDiagramInstance(), "SimpleMacro.vsdm");
    }

    //////////////////////////////// From NPOIFS /////////////////////////////
    @Test
    public void HSSFfromNPOIFS() throws Exception {
        fromNPOIFS(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xls");
    }
    @Ignore("bug 59302: Found 0 macros")
    @Test
    public void HSLFfromNPOIFS() throws Exception {
        fromNPOIFS(POIDataSamples.getSlideShowInstance(), "SimpleMacro.ppt");
    }
    @Test
    public void HWPFfromNPOIFS() throws Exception {
        fromNPOIFS(POIDataSamples.getDocumentInstance(), "SimpleMacro.doc");
    }
    @Ignore("Found 0 macros")
    @Test
    public void HDGFfromNPOIFS() throws Exception {
        fromNPOIFS(POIDataSamples.getDiagramInstance(), "SimpleMacro.vsd");
    }

    protected void fromFile(POIDataSamples dataSamples, String filename) throws IOException {
        File f = dataSamples.getFile(filename);
        VBAMacroReader r = new VBAMacroReader(f);
        try {
            assertMacroContents(dataSamples, r);
        } finally {
            r.close();
        }
    }

    protected void fromStream(POIDataSamples dataSamples, String filename) throws IOException {
        InputStream fis = dataSamples.openResourceAsStream(filename);
        try {
            VBAMacroReader r = new VBAMacroReader(fis);
            try {
                assertMacroContents(dataSamples, r);
            } finally {
                r.close();
            }
        } finally {
            fis.close();
        }
    }

    protected void fromNPOIFS(POIDataSamples dataSamples, String filename) throws IOException {
        File f = dataSamples.getFile(filename);
        NPOIFSFileSystem fs = new NPOIFSFileSystem(f);
        try {
            VBAMacroReader r = new VBAMacroReader(fs);
            try {
                assertMacroContents(dataSamples, r);
            } finally {
                r.close();
            }
        } finally {
            fs.close();
        }
    }
    
    protected void assertMacroContents(POIDataSamples samples, VBAMacroReader r) throws IOException {
        assertNotNull(r);
        Map<String,String> contents = r.readMacros();
        assertNotNull(contents);
        assertFalse("Found 0 macros", contents.isEmpty());
        /*
        assertEquals(5, contents.size());
        
        // Check the ones without scripts
        String[] noScripts = new String[] { "ThisWorkbook",
                "Sheet1", "Sheet2", "Sheet3" };
        for (String entry : noScripts) {
            assertTrue(entry, contents.containsKey(entry));
            
            String content = contents.get(entry);
            assertContains(content, "Attribute VB_Exposed = True");
            assertContains(content, "Attribute VB_Customizable = True");
            assertContains(content, "Attribute VB_TemplateDerived = False");
            assertContains(content, "Attribute VB_GlobalNameSpace = False");
            assertContains(content, "Attribute VB_Exposed = True");
        }
        */
        
        // Check the script one
        assertContains(contents, "Module1");
        String content = contents.get("Module1");
        assertNotNull(content);
        assertContains(content, "Attribute VB_Name = \"Module1\"");
        //assertContains(content, "Attribute TestMacro.VB_Description = \"This is a test macro\"");

        // And the macro itself
        String testMacroNoSub = expectedMacroContents.get(samples);
        assertContains(content, testMacroNoSub);
    }
    
    @Ignore
    @Test
    public void bug59830() throws IOException {
        // This file is intentionally omitted from the test-data directory
        // unless we can extract the vbaProject.bin from this Word 97-2003 file
        // so that it's less likely to be opened and executed on a Windows computer.
        // The file is attached to bug 59830.
        // The Macro Virus only affects Windows computers, as it makes a
        // subprocess call to powershell.exe with an encoded payload
        // The document contains macros that execute on workbook open if macros
        // are enabled
        File doc = POIDataSamples.getDocumentInstance().getFile("macro_virus.doc.do_not_open");
        VBAMacroReader reader = new VBAMacroReader(doc);
        Map<String, String> macros = reader.readMacros();
        assertNotNull(macros);
        reader.close();
    }
    
    // This test is written as expected-to-fail and should be rewritten
    // as expected-to-pass when the bug is fixed.
    @Test
    public void bug59858() throws IOException {
        try {
            fromFile(POIDataSamples.getSpreadSheetInstance(), "59858.xls");
            testPassesNow(59858);
        } catch (IOException e) {
            if (e.getMessage().matches("Module offset for '.+' was never read.")) {
                //e.printStackTrace();
                // NPE when reading module.offset in VBAMacroReader.readMacros (approx line 258)
                skipTest(e);
            } else {
                // something unexpected failed
                throw e;
            }
        }
    }
    
    // This test is written as expected-to-fail and should be rewritten
    // as expected-to-pass when the bug is fixed.
    @Test
    public void bug60158() throws IOException {
        try {
            fromFile(POIDataSamples.getDocumentInstance(), "60158.docm");
            testPassesNow(60158);
        } catch (ArrayIndexOutOfBoundsException e) {
            skipTest(e);
        }
    }
}
