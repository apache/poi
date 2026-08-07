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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.util.RecordFormatException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

    @Test
    void maxStringLengthDefaultIsLarge() {
        // Default should be large enough to handle real-world VBA projects
        assertTrue(VBAMacroReader.getMaxStringLength() >= 20_000,
                "Default MAX_STRING_LENGTH should be at least 20_000");
    }

    @Test
    void setMaxStringLengthIsRespectedByModuleRead() throws IOException {
        int prevLimit = VBAMacroReader.getMaxStringLength();
        try {
            // Set an absurdly small limit so the module stream read is rejected
            VBAMacroReader.setMaxStringLength(1);
            File f = POIDataSamples.getSpreadSheetInstance().getFile("SimpleMacro.xls");
            try (VBAMacroReader r = new VBAMacroReader(f)) {
                assertThrows(RecordFormatException.class, r::readMacros,
                        "Expected RecordFormatException when MAX_STRING_LENGTH is exceeded during module read");
            }
        } finally {
            VBAMacroReader.setMaxStringLength(prevLimit);
        }
    }

    /**
     * Verify that VBAMacroReader prefers the vbaProject.bin at a canonical OPC path
     * (xl/vbaProject.bin) over one at a non-canonical path (docProps/vbaProject.bin),
     * regardless of ZIP stream order.
     *
     * This prevents evasion where a crafted file places a benign decoy VBA project at a
     * non-standard path that appears earlier in the ZIP stream than the real project.
     */
    @Test
    void prefersCanonicalVbaProjectPath() throws IOException {
        File realSrc = POIDataSamples.getSpreadSheetInstance().getFile("SimpleMacro.xlsm");
        File decoySrc = POIDataSamples.getSpreadSheetInstance().getFile("testNames.xlsm");

        byte[] realVba = extractZipEntry(realSrc, "xl/vbaProject.bin");
        byte[] decoyVba = extractZipEntry(decoySrc, "xl/vbaProject.bin");

        // Ground-truth: what each project's macros look like
        Map<String, String> realMacros;
        try (VBAMacroReader r = new VBAMacroReader(realSrc)) {
            realMacros = new TreeMap<>(r.readMacros());
        }
        Map<String, String> decoyMacros;
        try (VBAMacroReader r = new VBAMacroReader(decoySrc)) {
            decoyMacros = new TreeMap<>(r.readMacros());
        }
        assertFalse(realMacros.equals(decoyMacros), "test fixtures must have different macros");

        // Craft an .xlsm with decoy at docProps/vbaProject.bin (non-canonical, first in stream)
        // and real at xl/vbaProject.bin (canonical, second in stream).
        byte[] crafted = craftDualVbaXlsm(realVba, decoyVba, /*decoyFirst=*/true);

        // VBAMacroReader should return the REAL macros (from the canonical xl/ path),
        // not the decoy (from the non-canonical docProps/ path).
        Map<String, String> seen;
        try (VBAMacroReader r = new VBAMacroReader(new ByteArrayInputStream(crafted))) {
            seen = new TreeMap<>(r.readMacros());
        }
        assertEquals(realMacros, seen,
                "VBAMacroReader must prefer canonical xl/vbaProject.bin over non-canonical docProps/vbaProject.bin");

        // Reverse order: real first, decoy second — should still get real macros.
        byte[] craftedReverse = craftDualVbaXlsm(realVba, decoyVba, /*decoyFirst=*/false);
        Map<String, String> seenReverse;
        try (VBAMacroReader r = new VBAMacroReader(new ByteArrayInputStream(craftedReverse))) {
            seenReverse = new TreeMap<>(r.readMacros());
        }
        assertEquals(realMacros, seenReverse,
                "VBAMacroReader must return canonical xl/vbaProject.bin regardless of stream order");
    }

    private static byte[] extractZipEntry(File zip, String entryName) throws IOException {
        try (ZipFile zf = new ZipFile(zip)) {
            ZipEntry e = zf.getEntry(entryName);
            assertNotNull(e, "entry '" + entryName + "' not in " + zip);
            try (InputStream in = zf.getInputStream(e)) {
                return in.readAllBytes();
            }
        }
    }

    /**
     * Assemble a minimal .xlsm with two vbaProject.bin entries:
     * real at xl/vbaProject.bin (canonical) and decoy at docProps/vbaProject.bin (non-canonical).
     * Stream order is controlled by {@code decoyFirst}.
     */
    private static byte[] craftDualVbaXlsm(byte[] realVba, byte[] decoyVba, boolean decoyFirst) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            putZipEntry(zos, "[Content_Types].xml",
                    ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Default Extension=\"bin\" ContentType=\"application/vnd.ms-office.vbaProject\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.ms-excel.sheet.macroEnabled.main+xml\"/>"
                    + "</Types>").getBytes(StandardCharsets.UTF_8));

            putZipEntry(zos, "_rels/.rels",
                    ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" "
                    + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" "
                    + "Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>").getBytes(StandardCharsets.UTF_8));

            putZipEntry(zos, "xl/workbook.xml",
                    ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                    + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"Sheet1\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                    + "</workbook>").getBytes(StandardCharsets.UTF_8));

            putZipEntry(zos, "xl/_rels/workbook.xml.rels",
                    ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rIdVba\" "
                    + "Type=\"http://schemas.microsoft.com/office/2006/relationships/vbaProject\" "
                    + "Target=\"vbaProject.bin\"/>"
                    + "</Relationships>").getBytes(StandardCharsets.UTF_8));

            if (decoyFirst) {
                putZipEntry(zos, "docProps/vbaProject.bin", decoyVba);
                putZipEntry(zos, "xl/vbaProject.bin", realVba);
            } else {
                putZipEntry(zos, "xl/vbaProject.bin", realVba);
                putZipEntry(zos, "docProps/vbaProject.bin", decoyVba);
            }
        }
        return bos.toByteArray();
    }

    private static void putZipEntry(ZipOutputStream zos, String name, byte[] data) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(data);
        zos.closeEntry();
    }
}
