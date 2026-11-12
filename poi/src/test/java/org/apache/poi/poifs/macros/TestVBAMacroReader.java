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
import org.apache.poi.poifs.filesystem.DirectoryNode;
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

    @Test
    void setMaxStreamSizeIsRespectedByVbaProject() throws IOException {
        int prevLimit = VBAMacroReader.getMaxStreamSize();
        try {
            File f = POIDataSamples.getSpreadSheetInstance().getFile("SimpleMacro.xlsm");
            int vbaSize = extractZipEntry(f, "xl/vbaProject.bin").length;

            VBAMacroReader.setMaxStreamSize(vbaSize - 1);
            assertThrows(RecordFormatException.class, () -> new VBAMacroReader(f),
                    "Expected RecordFormatException when vbaProject.bin exceeds the limit");
            try (InputStream is = new FileInputStream(f)) {
                assertThrows(RecordFormatException.class, () -> new VBAMacroReader(is),
                        "Expected RecordFormatException when vbaProject.bin exceeds the limit");
            }

            VBAMacroReader.setMaxStreamSize(vbaSize);
            try (VBAMacroReader r = new VBAMacroReader(f)) {
                assertFalse(r.readMacros().isEmpty(), "macros should be read when within the limit");
            }
        } finally {
            VBAMacroReader.setMaxStreamSize(prevLimit);
        }
    }

    @Test
    void setMaxStreamSizeRejectsNonPositive() {
        assertThrows(IllegalArgumentException.class, () -> VBAMacroReader.setMaxStreamSize(0));
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

    /**
     * The module stream can be read before the dir stream that holds its offset (bug 59858 layout).
     * A bad offset must then fall back to the brute-force search, as it does in the other order.
     */
    @Test
    void badModuleOffsetFromLaterDirStream() throws IOException {
        final String source = "Attribute VB_Name = \"Module1\"";
        // the module stream starts with a chunk whose first token is a copy token,
        // the real compressed source follows it
        final ByteArrayOutputStream moduleStream = new ByteArrayOutputStream();
        moduleStream.write(new byte[]{0x01, 0x02, (byte) 0xB0, 0x01, 0x00, 0x00});
        moduleStream.write(compressLiterals(source.getBytes(StandardCharsets.US_ASCII)));

        final ByteArrayOutputStream dir = new ByteArrayOutputStream();
        // MODULE_STREAM_NAME "Module1", then MODULE_OFFSET 0, which points at the broken chunk
        writeShort(dir, 0x1A);
        writeInt(dir, 7);
        dir.write("Module1".getBytes(StandardCharsets.US_ASCII));
        writeShort(dir, 0x32);
        writeInt(dir, 14);
        dir.write("Module1".getBytes(StandardCharsets.UTF_16LE));
        writeShort(dir, 0x31);
        writeInt(dir, 4);
        writeInt(dir, 0);

        try (POIFSFileSystem fs = new POIFSFileSystem();
             VBAMacroReader reader = new VBAMacroReader(fs)) {
            DirectoryNode moduleDir = (DirectoryNode) fs.getRoot().createDirectory("first").createDirectory("VBA");
            moduleDir.createDocument("Module1", new ByteArrayInputStream(moduleStream.toByteArray()));
            DirectoryNode dirDir = (DirectoryNode) fs.getRoot().createDirectory("second").createDirectory("VBA");
            dirDir.createDocument("dir", new ByteArrayInputStream(compressLiterals(dir.toByteArray())));

            VBAMacroReader.ModuleMap modules = new VBAMacroReader.ModuleMap();
            reader.readMacros(moduleDir, modules);
            reader.readMacros(dirDir, modules);
            assertEquals(source, new String(modules.get("Module1").buf, StandardCharsets.US_ASCII));
        }
    }

    @Test
    void moduleMapKeyedByStreamNameUnicode() throws IOException {
        final String unicodeStreamName = "\u9ad9\u6a4b\u2460";
        final String source = "Attribute VB_Name = \"Mod1\"\r\nSub Run()\r\nEnd Sub\r\n";
        // Simulate an MBCS StreamName that does not round-trip to the UTF-16 entry name
        final byte[] mbcsName = "???".getBytes(StandardCharsets.US_ASCII);
        final byte[] utf16Name = unicodeStreamName.getBytes(StandardCharsets.UTF_16LE);

        final ByteArrayOutputStream dir = new ByteArrayOutputStream();
        // PROJECT_CODEPAGE = 1252
        writeShort(dir, 0x0003);
        writeInt(dir, 2);
        writeShort(dir, 1252);
        // MODULE_STREAM_NAME (MBCS + 0x0032 + UTF-16LE)
        writeShort(dir, 0x001A);
        writeInt(dir, mbcsName.length);
        dir.writeBytes(mbcsName);
        writeShort(dir, 0x0032);
        writeInt(dir, utf16Name.length);
        dir.writeBytes(utf16Name);
        // MODULE_OFFSET = 0
        writeShort(dir, 0x0031);
        writeInt(dir, 4);
        writeInt(dir, 0);
        // DIR_STREAM_TERMINATOR
        writeShort(dir, 0x0010);
        writeInt(dir, 0);

        final ByteArrayOutputStream projectWm = new ByteArrayOutputStream();
        projectWm.writeBytes(mbcsName);
        projectWm.write(0);
        projectWm.writeBytes(utf16Name);
        projectWm.write(new byte[]{0, 0, 0, 0});

        try (POIFSFileSystem fs = new POIFSFileSystem();
             VBAMacroReader reader = new VBAMacroReader(fs)) {
            DirectoryNode vba = (DirectoryNode) fs.getRoot().createDirectory("VBA");
            vba.createDocument("dir", new ByteArrayInputStream(compressLiterals(dir.toByteArray())));
            vba.createDocument(unicodeStreamName, new ByteArrayInputStream(
                    compressLiterals(source.getBytes(StandardCharsets.US_ASCII))));
            fs.getRoot().createDocument("PROJECTwm", new ByteArrayInputStream(projectWm.toByteArray()));
            fs.getRoot().createDocument("PROJECT", new ByteArrayInputStream(
                    "Module=???\r\n".getBytes(StandardCharsets.US_ASCII)));

            Map<String, String> macros = reader.readMacros();
            assertEquals(Collections.singletonMap(unicodeStreamName, source), macros);

            Map<String, Module> modules = reader.readMacroModules();
            assertEquals(Module.ModuleType.Module, modules.get(unicodeStreamName).geModuleType());
        }
    }

    /**
     * a dir stream that fails to decompress is reported as an IOException
     */
    @Test
    void badDirStreamIsIOException() throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem();
             VBAMacroReader reader = new VBAMacroReader(fs)) {
            DirectoryNode vba = (DirectoryNode) fs.getRoot().createDirectory("VBA");
            // a chunk whose first token is a copy token
            vba.createDocument("dir", new ByteArrayInputStream(
                    new byte[]{0x01, 0x02, (byte) 0xB0, 0x01, 0x00, 0x00}));
            VBAMacroReader.ModuleMap modules = new VBAMacroReader.ModuleMap();
            assertThrows(IOException.class, () -> reader.readMacros(vba, modules));
        }
    }

    @Test
    void unsupportedProjectCodepageFallsBackToWindows1252() throws IOException {
        final String source = "Attribute VB_Name = \"Module1\"\r\nSub Run()\r\nMsgBox \"\u00e9\"\r\nEnd Sub\r\n";
        final byte[] sourceBytes = source.getBytes(StringUtil.WIN_1252);
        final byte[] asciiName = "Module1".getBytes(StandardCharsets.US_ASCII);
        final byte[] utf16Name = "Module1".getBytes(StandardCharsets.UTF_16LE);

        // Test both an unknown positive code page (UnsupportedCharsetException)
        // and code page 0 (UnsupportedEncodingException from CodePageUtil).
        for (int unsupportedCodepage : new int[]{10025, 0}) {
            final ByteArrayOutputStream dir = new ByteArrayOutputStream();
            writeShort(dir, 0x0003);
            writeInt(dir, 2);
            writeShort(dir, unsupportedCodepage);
            writeShort(dir, 0x001A);
            writeInt(dir, asciiName.length);
            dir.writeBytes(asciiName);
            writeShort(dir, 0x0032);
            writeInt(dir, utf16Name.length);
            dir.writeBytes(utf16Name);
            writeShort(dir, 0x0031);
            writeInt(dir, 4);
            writeInt(dir, 0);
            writeShort(dir, 0x0010);
            writeInt(dir, 0);

            try (POIFSFileSystem fs = new POIFSFileSystem();
                 VBAMacroReader reader = new VBAMacroReader(fs)) {
                DirectoryNode vba = (DirectoryNode) fs.getRoot().createDirectory("VBA");
                vba.createDocument("dir", new ByteArrayInputStream(compressLiterals(dir.toByteArray())));
                vba.createDocument("Module1", new ByteArrayInputStream(compressLiterals(sourceBytes)));

                Map<String, String> macros = reader.readMacros();
                assertEquals(Collections.singletonMap("Module1", source), macros);
            }
        }
    }

    @Test
    void setMaxStreamSizeIsRespectedByModule() throws IOException {
        final int chunks = 100;
        // RLE header + chunks that each expand to 4096 bytes
        final ByteArrayOutputStream moduleStream = new ByteArrayOutputStream();
        moduleStream.write(0x01);
        moduleStream.writeBytes(expandingChunks(chunks));

        int prevLimit = VBAMacroReader.getMaxStreamSize();
        try {
            VBAMacroReader.setMaxStreamSize(chunks * 4096);
            VBAMacroReader.ModuleMap modules = readModuleWithDir(moduleStream.toByteArray(), 0);
            assertEquals(chunks * 4096, modules.get("Module1").buf.length);

            VBAMacroReader.setMaxStreamSize(chunks * 4096 - 1);
            assertThrows(RecordFormatException.class, () -> readModuleWithDir(moduleStream.toByteArray(), 0));
        } finally {
            VBAMacroReader.setMaxStreamSize(prevLimit);
        }
    }

    /**
     * When the module offset is bad, the brute-force search for the module source
     * must apply the stream size limit too
     */
    @Test
    void setMaxStreamSizeIsRespectedByBruteForceSearch() throws IOException {
        final int chunks = 100;
        final byte[] source = "Attribute VB_Name = \"Module1\"".getBytes(StandardCharsets.US_ASCII);
        final ByteArrayOutputStream moduleStream = new ByteArrayOutputStream();
        // a chunk whose first token is a copy token, so the offset 0 is bad
        moduleStream.write(new byte[]{0x01, 0x02, (byte) 0xB0, 0x01, 0x00, 0x00});
        // the real compressed source, followed by chunks that each expand to 4096 bytes
        moduleStream.writeBytes(compressLiterals(source));
        moduleStream.writeBytes(expandingChunks(chunks));
        final int expectedSize = source.length + chunks * 4096;

        int prevLimit = VBAMacroReader.getMaxStreamSize();
        try {
            VBAMacroReader.setMaxStreamSize(expectedSize);
            VBAMacroReader.ModuleMap modules = readModuleWithDir(moduleStream.toByteArray(), 0);
            assertEquals(expectedSize, modules.get("Module1").buf.length);

            VBAMacroReader.setMaxStreamSize(expectedSize - 1);
            assertThrows(RecordFormatException.class, () -> readModuleWithDir(moduleStream.toByteArray(), 0));
        } finally {
            VBAMacroReader.setMaxStreamSize(prevLimit);
        }
    }

    /**
     * Reads the module stream "Module1" first, then a dir stream giving its offset
     */
    private static VBAMacroReader.ModuleMap readModuleWithDir(byte[] moduleStream, int moduleOffset) throws IOException {
        final ByteArrayOutputStream dir = new ByteArrayOutputStream();
        writeShort(dir, 0x1A);
        writeInt(dir, 7);
        dir.write("Module1".getBytes(StandardCharsets.US_ASCII));
        writeShort(dir, 0x32);
        writeInt(dir, 14);
        dir.write("Module1".getBytes(StandardCharsets.UTF_16LE));
        writeShort(dir, 0x31);
        writeInt(dir, 4);
        writeInt(dir, moduleOffset);

        try (POIFSFileSystem fs = new POIFSFileSystem();
             VBAMacroReader reader = new VBAMacroReader(fs)) {
            DirectoryNode moduleDir = (DirectoryNode) fs.getRoot().createDirectory("first").createDirectory("VBA");
            moduleDir.createDocument("Module1", new ByteArrayInputStream(moduleStream));
            DirectoryNode dirDir = (DirectoryNode) fs.getRoot().createDirectory("second").createDirectory("VBA");
            dirDir.createDocument("dir", new ByteArrayInputStream(compressLiterals(dir.toByteArray())));

            VBAMacroReader.ModuleMap modules = new VBAMacroReader.ModuleMap();
            reader.readMacros(moduleDir, modules);
            reader.readMacros(dirDir, modules);
            return modules;
        }
    }

    /**
     * RLE chunks (without the leading signature byte) that each hold one literal
     * and one copy token, so that 6 bytes expand to 4096
     */
    private static byte[] expandingChunks(int count) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int c = 0; c < count; c++) {
            // compressed chunk of 6 bytes in total
            writeShort(out, 0xB000 | (6 - 3));
            // flag: first token is a literal, second token is a copy token
            out.write(0x02);
            out.write('A');
            // copy token: offset 1, length 4095 (4 bits of offset, 12 bits of length - 3)
            writeShort(out, 4095 - 3);
        }
        return out.toByteArray();
    }

    /**
     * RLE-compresses {@code data} into a single chunk of literals only
     */
    private static byte[] compressLiterals(byte[] data) {
        final ByteArrayOutputStream chunk = new ByteArrayOutputStream();
        for (int i = 0; i < data.length; i += 8) {
            chunk.write(0);
            chunk.write(data, i, Math.min(8, data.length - i));
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x01);
        writeShort(out, 0xB000 | (chunk.size() - 1));
        out.writeBytes(chunk.toByteArray());
        return out.toByteArray();
    }

    private static void writeShort(ByteArrayOutputStream out, int value) {
        out.write(value);
        out.write(value >> 8);
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        writeShort(out, value);
        writeShort(out, value >> 16);
    }

    private static void putZipEntry(ZipOutputStream zos, String name, byte[] data) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(data);
        zos.closeEntry();
    }
}
