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

import org.apache.poi.POIDataSamples;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.StringUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class TestVBAMacroReader {
    private static final Map<POIDataSamples, String> expectedMacroContents;

    private static String readVBA(POIDataSamples poiDataSamples) {
        File macro = poiDataSamples.getFile("SimpleMacro.vba");
        final byte[] bytes;
        try {
            try (FileInputStream stream = new FileInputStream(macro)) {
                bytes = IOUtils.toByteArray(stream);
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
        final Map<POIDataSamples, String> _expectedMacroContents = new HashMap<>();
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
    void HSSFFromStream() throws Exception {
        fromStream(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xls");
    }
    @Test
    void XSSFFromStream() throws Exception {
        fromStream(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xlsm");
    }
    @Disabled("bug 59302: Found 0 macros; See org.apache.poi.hslf.usermodel.TestBugs.getMacrosFromHSLF()" +
            "for an example of how to get macros out of ppt. TODO: make integration across file formats more elegant")
    @Test
    void HSLFFromStream() throws Exception {
        fromStream(POIDataSamples.getSlideShowInstance(), "SimpleMacro.ppt");
    }
    @Test
    void XSLFFromStream() throws Exception {
        fromStream(POIDataSamples.getSlideShowInstance(), "SimpleMacro.pptm");
    }
    @Test
    void HWPFFromStream() throws Exception {
        fromStream(POIDataSamples.getDocumentInstance(), "SimpleMacro.doc");
    }
    @Test
    void XWPFFromStream() throws Exception {
        fromStream(POIDataSamples.getDocumentInstance(), "SimpleMacro.docm");
    }
    @Disabled("Found 0 macros")
    @Test
    void HDGFFromStream() throws Exception {
        fromStream(POIDataSamples.getDiagramInstance(), "SimpleMacro.vsd");
    }
    @Test
    void XDGFFromStream() throws Exception {
        fromStream(POIDataSamples.getDiagramInstance(), "SimpleMacro.vsdm");
    }

    //////////////////////////////// From File /////////////////////////////
    @Test
    void HSSFFromFile() throws Exception {
        fromFile(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xls");
    }
    @Test
    void XSSFFromFile() throws Exception {
        fromFile(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xlsm");
    }
    @Disabled("bug 59302: Found 0 macros; See org.apache.poi.hslf.usermodel.TestBugs.getMacrosFromHSLF()" +
            "for an example of how to get macros out of ppt. TODO: make integration across file formats more elegant")
    @Test
    void HSLFFromFile() throws Exception {
        fromFile(POIDataSamples.getSlideShowInstance(), "SimpleMacro.ppt");
    }
    @Test
    void XSLFFromFile() throws Exception {
        fromFile(POIDataSamples.getSlideShowInstance(), "SimpleMacro.pptm");
    }
    @Test
    void HWPFFromFile() throws Exception {
        fromFile(POIDataSamples.getDocumentInstance(), "SimpleMacro.doc");
    }
    @Test
    void XWPFFromFile() throws Exception {
        fromFile(POIDataSamples.getDocumentInstance(), "SimpleMacro.docm");
    }
    @Disabled("Found 0 macros")
    @Test
    void HDGFFromFile() throws Exception {
        fromFile(POIDataSamples.getDiagramInstance(), "SimpleMacro.vsd");
    }
    @Test
    void XDGFFromFile() throws Exception {
        fromFile(POIDataSamples.getDiagramInstance(), "SimpleMacro.vsdm");
    }

    //////////////////////////////// From POIFS /////////////////////////////
    @Test
    void HSSFFromPOIFS() throws Exception {
        fromPOIFS(POIDataSamples.getSpreadSheetInstance(), "SimpleMacro.xls");
    }
    @Disabled("bug 59302: Found 0 macros")
    @Test
    void HSLFFromPOIFS() throws Exception {
        fromPOIFS(POIDataSamples.getSlideShowInstance(), "SimpleMacro.ppt");
    }
    @Test
    void HWPFFromPOIFS() throws Exception {
        fromPOIFS(POIDataSamples.getDocumentInstance(), "SimpleMacro.doc");
    }
    @Disabled("Found 0 macros")
    @Test
    void HDGFFromPOIFS() throws Exception {
        fromPOIFS(POIDataSamples.getDiagramInstance(), "SimpleMacro.vsd");
    }

    private void fromFile(POIDataSamples dataSamples, String filename) throws IOException {
        File f = dataSamples.getFile(filename);
        try (VBAMacroReader r = new VBAMacroReader(f)) {
            assertMacroContents(dataSamples, r);
        }
    }

    private void fromStream(POIDataSamples dataSamples, String filename) throws IOException {
        try (InputStream fis = dataSamples.openResourceAsStream(filename)) {
            try (VBAMacroReader r = new VBAMacroReader(fis)) {
                assertMacroContents(dataSamples, r);
            }
        }
    }

    private void fromPOIFS(POIDataSamples dataSamples, String filename) throws IOException {
        File f = dataSamples.getFile(filename);
        try (POIFSFileSystem fs = new POIFSFileSystem(f)) {
            try (VBAMacroReader r = new VBAMacroReader(fs)) {
                assertMacroContents(dataSamples, r);
            }
        }
    }

    private void assertMacroContents(POIDataSamples samples, VBAMacroReader r) throws IOException {
        assertNotNull(r);
        Map<String,Module> contents = r.readMacroModules();
        assertNotNull(contents);
        assertFalse(contents.isEmpty(), "Found 0 macros");
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
        Module module = contents.get("Module1");
        assertNotNull(module);
        String content = module.getContent();
        assertContains(content, "Attribute VB_Name = \"Module1\"");
        //assertContains(content, "Attribute TestMacro.VB_Description = \"This is a test macro\"");

        assertEquals(Module.ModuleType.Module, module.geModuleType());
        // And the macro itself
        String testMacroNoSub = expectedMacroContents.get(samples);
        assertContains(content, testMacroNoSub);
    }

    @Test
    void bug59830() throws IOException {
        //test file is "609751.xls" in govdocs1
        File f = POIDataSamples.getSpreadSheetInstance().getFile("59830.xls");
        VBAMacroReader r = new VBAMacroReader(f);
        Map<String, String> macros = r.readMacros();
        assertEquals(29, macros.size());
        assertNotNull(macros.get("Module20"));
        assertContains(macros.get("Module20"), "here start of superscripting");
        r.close();
    }

    @Test
    void bug59858() throws IOException {
        File f = POIDataSamples.getSpreadSheetInstance().getFile("59858.xls");
        VBAMacroReader r = new VBAMacroReader(f);
        Map<String, String> macros = r.readMacros();
        assertEquals(11, macros.size());
        assertNotNull(macros.get("Sheet4"));
        assertContains(macros.get("Sheet4"), "intentional constituent");
        r.close();
    }

    @Test
    void bug60158() throws IOException {
        File f = POIDataSamples.getDocumentInstance().getFile("60158.docm");
        VBAMacroReader r = new VBAMacroReader(f);
        Map<String, String> macros = r.readMacros();
        assertEquals(2, macros.size());
        assertNotNull(macros.get("NewMacros"));
        assertContains(macros.get("NewMacros"), "' dirty");
        r.close();
    }

    @Test
    void bug60273() throws IOException {
        //test file derives from govdocs1 147240.xls
        File f = POIDataSamples.getSpreadSheetInstance().getFile("60273.xls");
        VBAMacroReader r = new VBAMacroReader(f);
        Map<String, String> macros = r.readMacros();
        assertEquals(2, macros.size());
        assertNotNull(macros.get("Module1"));
        assertContains(macros.get("Module1"), "9/8/2004");
        r.close();
    }

    @Test
    void bug60279() throws IOException {
        File f = POIDataSamples.getDocumentInstance().getFile("60279.doc");
        VBAMacroReader r = new VBAMacroReader(f);
        Map<String, String> macros = r.readMacros();
        assertEquals(1, macros.size());
        String content = macros.get("ThisDocument");
        assertContains(content, "Attribute VB_Base = \"1Normal.ThisDocument\"");
        assertContains(content, "Attribute VB_Customizable = True");
        r.close();
    }

    @Test
    void bug62624() throws IOException {
        //macro comes from Common Crawl: HRLOXHGMGLFIJQQU27RIWXOARRHAAAAS
        File f = POIDataSamples.getSpreadSheetInstance().getFile("62624.bin");
        VBAMacroReader r = new VBAMacroReader(f);

        Map<String, Module> macros = r.readMacroModules();
        assertEquals(13, macros.size());
        assertNotNull(macros.get("M\u00F3dulo1"));
        assertContains(macros.get("M\u00F3dulo1").getContent(), "Calcula_tributos");
        assertEquals(Module.ModuleType.Module, macros.get("M\u00F3dulo1").geModuleType());
        r.close();
    }

    @Test
    void bug62625() throws IOException {
        //macro comes from Common Crawl: 4BZ22N5QG5R2SUU2MNN47PO7VBQLNYIQ
        //A REFERENCE_NAME can sometimes only have an ascii string without
        //a reserved byte followed by the unicode string.
        //See https://github.com/decalage2/oletools/blob/master/oletools/olevba.py#L1516
        //and https://github.com/decalage2/oletools/pull/135 from (@c1fe)


        File f = POIDataSamples.getSpreadSheetInstance().getFile("62625.bin");
        VBAMacroReader r = new VBAMacroReader(f);

        Map<String, Module> macros = r.readMacroModules();
        assertEquals(20, macros.size());
        r.close();
    }
}
