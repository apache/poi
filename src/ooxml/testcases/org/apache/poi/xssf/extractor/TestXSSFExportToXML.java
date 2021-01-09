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

package org.apache.poi.xssf.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.MapInfo;
import org.apache.poi.xssf.usermodel.XSSFMap;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Roberto Manicardi
 */
public final class TestXSSFExportToXML {

    @Test
    void testExportToXML() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("CustomXMLMappings.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(1);
                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xml = os.toString("UTF-8");

                assertNotNull(xml);
                assertFalse(xml.isEmpty());

                String docente = xml.split("<DOCENTE>")[1].split("</DOCENTE>")[0].trim();
                String nome = xml.split("<NOME>")[1].split("</NOME>")[0].trim();
                String tutor = xml.split("<TUTOR>")[1].split("</TUTOR>")[0].trim();
                String cdl = xml.split("<CDL>")[1].split("</CDL>")[0].trim();
                String durata = xml.split("<DURATA>")[1].split("</DURATA>")[0].trim();
                String argomento = xml.split("<ARGOMENTO>")[1].split("</ARGOMENTO>")[0].trim();
                String progetto = xml.split("<PROGETTO>")[1].split("</PROGETTO>")[0].trim();
                String crediti = xml.split("<CREDITI>")[1].split("</CREDITI>")[0].trim();

                assertEquals("ro", docente);
                assertEquals("ro", nome);
                assertEquals("ds", tutor);
                assertEquals("gs", cdl);
                assertEquals("g", durata);
                assertEquals("gvvv", argomento);
                assertEquals("aaaa", progetto);
                assertEquals("aa", crediti);

                parseXML(xml);

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testExportToXMLInverseOrder() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples
                .openSampleWorkbook("CustomXmlMappings-inverse-order.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(1);
                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xml = os.toString("UTF-8");

                assertNotNull(xml);
                assertFalse(xml.isEmpty());

                String docente = xml.split("<DOCENTE>")[1].split("</DOCENTE>")[0].trim();
                String nome = xml.split("<NOME>")[1].split("</NOME>")[0].trim();
                String tutor = xml.split("<TUTOR>")[1].split("</TUTOR>")[0].trim();
                String cdl = xml.split("<CDL>")[1].split("</CDL>")[0].trim();
                String durata = xml.split("<DURATA>")[1].split("</DURATA>")[0].trim();
                String argomento = xml.split("<ARGOMENTO>")[1].split("</ARGOMENTO>")[0].trim();
                String progetto = xml.split("<PROGETTO>")[1].split("</PROGETTO>")[0].trim();
                String crediti = xml.split("<CREDITI>")[1].split("</CREDITI>")[0].trim();

                assertEquals("aa", nome);
                assertEquals("aaaa", docente);
                assertEquals("gvvv", tutor);
                assertEquals("g", cdl);
                assertEquals("gs", durata);
                assertEquals("ds", argomento);
                assertEquals("ro", progetto);
                assertEquals("ro", crediti);

                parseXML(xml);

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testXPathOrdering() throws IOException {
        try (XSSFWorkbook wb = XSSFTestDataSamples
                .openSampleWorkbook("CustomXmlMappings-inverse-order.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (p instanceof MapInfo) {
                    MapInfo mapInfo = (MapInfo) p;

                    XSSFMap map = mapInfo.getXSSFMapById(1);
                    XSSFExportToXml exporter = new XSSFExportToXml(map);

                    assertEquals(1, exporter.compare("/CORSO/DOCENTE", "/CORSO/NOME"));
                    assertEquals(-1, exporter.compare("/CORSO/NOME", "/CORSO/DOCENTE"));
                }

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testMultiTable() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples
                .openSampleWorkbook("CustomXMLMappings-complex-type.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (p instanceof MapInfo) {
                    MapInfo mapInfo = (MapInfo) p;

                    XSSFMap map = mapInfo.getXSSFMapById(2);

                    assertNotNull(map);

                    XSSFExportToXml exporter = new XSSFExportToXml(map);
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    exporter.exportToXML(os, true);
                    String xml = os.toString("UTF-8");

                    assertNotNull(xml);

                    String[] regexConditions = {
                            "<MapInfo", "</MapInfo>",
                            "<Schema ID=\"1\" Namespace=\"\" SchemaRef=\"\"/>",
                            "<Schema ID=\"4\" Namespace=\"\" SchemaRef=\"\"/>",
                            "DataBinding",
                            "Map Append=\"false\" AutoFit=\"false\" ID=\"1\"",
                            "Map Append=\"false\" AutoFit=\"false\" ID=\"5\"",
                    };

                    for (String condition : regexConditions) {
                        Pattern pattern = Pattern.compile(condition);
                        Matcher matcher = pattern.matcher(xml);
                        assertTrue(matcher.find());
                    }
                }

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    @Disabled(value="Fails, but I don't know if it is ok or not...")
    void testExportToXMLSingleAttributeNamespace() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("CustomXMLMapping-singleattributenamespace.xlsx")) {

            for (XSSFMap map : wb.getCustomXMLMappings()) {
                XSSFExportToXml exporter = new XSSFExportToXml(map);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
            }
        }
    }

    @Test
    void test55850ComplexXmlExport() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("55850.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(2);

                assertNotNull(map, "XSSFMap is null");

                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xmlData = os.toString("UTF-8");

                assertNotNull(xmlData);
                assertFalse(xmlData.isEmpty());

                String a = xmlData.split("<A>")[1].split("</A>")[0].trim();
                String b = a.split("<B>")[1].split("</B>")[0].trim();
                String c = b.split("<C>")[1].split("</C>")[0].trim();
                String d = c.split("<D>")[1].split("</Dd>")[0].trim();
                String e = d.split("<E>")[1].split("</EA>")[0].trim();

                String euro = e.split("<EUR>")[1].split("</EUR>")[0].trim();
                String chf = e.split("<CHF>")[1].split("</CHF>")[0].trim();

                assertEquals("15", euro);
                assertEquals("19", chf);

                parseXML(xmlData);

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testFormulaCells_Bugzilla_55927() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("55927.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(1);

                assertNotNull(map, "XSSFMap is null");

                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xmlData = os.toString("UTF-8");

                assertNotNull(xmlData);
                assertFalse(xmlData.isEmpty());

                assertEquals("2012-01-13", xmlData.split("<DATE>")[1].split("</DATE>")[0].trim());
                assertEquals("2012-02-16", xmlData.split("<FORMULA_DATE>")[1].split("</FORMULA_DATE>")[0].trim());

                parseXML(xmlData);

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testFormulaCells_Bugzilla_55926() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("55926.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(1);

                assertNotNull(map, "XSSFMap is null");

                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xmlData = os.toString("UTF-8");

                assertNotNull(xmlData);
                assertFalse(xmlData.isEmpty());

                String a = xmlData.split("<A>")[1].split("</A>")[0].trim();
                String doubleValue = a.split("<DOUBLE>")[1].split("</DOUBLE>")[0].trim();
                String stringValue = a.split("<STRING>")[1].split("</STRING>")[0].trim();

                assertEquals("Hello World", stringValue);
                assertEquals("5.1", doubleValue);

                parseXML(xmlData);

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testXmlExportIgnoresEmptyCells_Bugzilla_55924() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("55924.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(1);

                assertNotNull(map, "XSSFMap is null");

                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xmlData = os.toString("UTF-8");

                assertNotNull(xmlData);
                assertFalse(xmlData.isEmpty());

                String a = xmlData.split("<A>")[1].split("</A>")[0].trim();
                String euro = a.split("<EUR>")[1].split("</EUR>")[0].trim();
                assertEquals("1", euro);

                parseXML(xmlData);

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testXmlExportSchemaWithXSAllTag_Bugzilla_56169() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56169.xlsx")) {

            for (XSSFMap map : wb.getCustomXMLMappings()) {
                XSSFExportToXml exporter = new XSSFExportToXml(map);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xmlData = os.toString("UTF-8");

                assertNotNull(xmlData);
                assertFalse(xmlData.isEmpty());

                String a = xmlData.split("<A>")[1].split("</A>")[0].trim();
                String a_b = a.split("<B>")[1].split("</B>")[0].trim();
                String a_b_c = a_b.split("<C>")[1].split("</C>")[0].trim();
                String a_b_c_e = a_b_c.split("<E>")[1].split("</EA>")[0].trim();
                String a_b_c_e_euro = a_b_c_e.split("<EUR>")[1].split("</EUR>")[0].trim();
                String a_b_c_e_chf = a_b_c_e.split("<CHF>")[1].split("</CHF>")[0].trim();

                assertEquals("1", a_b_c_e_euro);
                assertEquals("2", a_b_c_e_chf);

                String a_b_d = a_b.split("<D>")[1].split("</Dd>")[0].trim();
                String a_b_d_e = a_b_d.split("<E>")[1].split("</EA>")[0].trim();

                String a_b_d_e_euro = a_b_d_e.split("<EUR>")[1].split("</EUR>")[0].trim();
                String a_b_d_e_chf = a_b_d_e.split("<CHF>")[1].split("</CHF>")[0].trim();

                assertEquals("3", a_b_d_e_euro);
                assertEquals("4", a_b_d_e_chf);
            }
        }
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    void testXmlExportCompare_Bug_55923() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("55923.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(4);

                assertNotNull(map, "XSSFMap is null");

                XSSFExportToXml exporter = new XSSFExportToXml(map);
                assertEquals(0, exporter.compare("", ""));
                assertEquals(0, exporter.compare("/", "/"));
                assertEquals(0, exporter.compare("//", "//"));
                assertEquals(0, exporter.compare("/a/", "/b/"));

                assertEquals(-1, exporter.compare("/ns1:Entry/ns1:A/ns1:B/ns1:C/ns1:E/ns1:EUR",
                        "/ns1:Entry/ns1:A/ns1:B/ns1:C/ns1:E/ns1:CHF"));

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testXmlExportSchemaOrderingBug_Bugzilla_55923() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("55923.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(4);

                assertNotNull(map, "XSSFMap is null");

                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xmlData = os.toString("UTF-8");

                assertNotNull(xmlData);
                assertFalse(xmlData.isEmpty());

                String a = xmlData.split("<A>")[1].split("</A>")[0].trim();
                String a_b = a.split("<B>")[1].split("</B>")[0].trim();
                String a_b_c = a_b.split("<C>")[1].split("</C>")[0].trim();
                String a_b_c_e = a_b_c.split("<E>")[1].split("</EA>")[0].trim();
                String a_b_c_e_euro = a_b_c_e.split("<EUR>")[1].split("</EUR>")[0].trim();
                String a_b_c_e_chf = a_b_c_e.split("<CHF>")[1].split("</CHF>")[0].trim();

                assertEquals("1", a_b_c_e_euro);
                assertEquals("2", a_b_c_e_chf);

                String a_b_d = a_b.split("<D>")[1].split("</Dd>")[0].trim();
                String a_b_d_e = a_b_d.split("<E>")[1].split("</EA>")[0].trim();

                String a_b_d_e_euro = a_b_d_e.split("<EUR>")[1].split("</EUR>")[0].trim();
                String a_b_d_e_chf = a_b_d_e.split("<CHF>")[1].split("</CHF>")[0].trim();

                assertEquals("3", a_b_d_e_euro);
                assertEquals("4", a_b_d_e_chf);

                found = true;
            }
            assertTrue(found);
        }
    }

    private void parseXML(String xmlData) throws IOException, SAXException {
        DocumentBuilder docBuilder = XMLHelper.newDocumentBuilder();
        docBuilder.parse(new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testExportDataTypes() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("55923.xlsx")) {

            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(0);

            Cell cString = row.createCell(0);
            cString.setCellValue("somestring");

            Cell cBoolean = row.createCell(1);
            cBoolean.setCellValue(true);

            Cell cError = row.createCell(2);
            cError.setCellErrorValue(FormulaError.NUM.getCode());

            Cell cFormulaString = row.createCell(3);
            cFormulaString.setCellFormula("A1");

            Cell cFormulaNumeric = row.createCell(4);
            cFormulaNumeric.setCellFormula("F1");

            Cell cNumeric = row.createCell(5);
            cNumeric.setCellValue(1.2);

            Cell cDate = row.createCell(6);
            cDate.setCellValue(new Date());

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(4);

                assertNotNull(map, "XSSFMap is null");

                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xmlData = os.toString("UTF-8");

                assertNotNull(xmlData);
                assertFalse(xmlData.isEmpty());

                parseXML(xmlData);

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testValidateFalse() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("55923.xlsx")) {
            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(4);

                assertNotNull(map, "XSSFMap is null");

                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, false);
                String xmlData = os.toString("UTF-8");

                assertNotNull(xmlData);
                assertFalse(xmlData.isEmpty());

                parseXML(xmlData);

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testRefElementsInXmlSchema_Bugzilla_56730() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56730.xlsx")) {

            boolean found = false;
            for (POIXMLDocumentPart p : wb.getRelations()) {

                if (!(p instanceof MapInfo)) {
                    continue;
                }
                MapInfo mapInfo = (MapInfo) p;

                XSSFMap map = mapInfo.getXSSFMapById(1);

                assertNotNull(map, "XSSFMap is null");

                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, true);
                String xmlData = os.toString("UTF-8");

                assertNotNull(xmlData);
                assertFalse(xmlData.isEmpty());

                assertEquals("2014-12-31", xmlData.split("<DATE>")[1].split("</DATE>")[0].trim());
                assertEquals("12.5", xmlData.split("<REFELEMENT>")[1].split("</REFELEMENT>")[0].trim());

                parseXML(xmlData);

                found = true;
            }
            assertTrue(found);
        }
    }

    @Test
    void testBug59026() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("59026.xlsx")) {
            Collection<XSSFMap> mappings = wb.getCustomXMLMappings();
            assertTrue(mappings.size() > 0);
            for (XSSFMap map : mappings) {
                XSSFExportToXml exporter = new XSSFExportToXml(map);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                exporter.exportToXML(os, false);
                assertNotNull(os.toString("UTF-8"));
            }
        }
    }

    @Test
    void testExportTableWithNonMappedColumn_Bugzilla_61281() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("61281.xlsx")) {
            for (XSSFMap map : wb.getCustomXMLMappings()) {
                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                exporter.exportToXML(bos, true);
                assertNotNull(DocumentHelper.readDocument(new ByteArrayInputStream(bos.toByteArray())));
                String exportedXml = bos.toString("UTF-8");
                assertEquals("<Test><Test>1</Test></Test>", exportedXml.replaceAll("\\s+", ""));
            }
        }
    }

    @Test
    void testXXEInSchema() throws Exception {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("xxe_in_schema.xlsx")) {
            for (XSSFMap map : wb.getCustomXMLMappings()) {
                XSSFExportToXml exporter = new XSSFExportToXml(map);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                assertThrows(SAXParseException.class, () -> exporter.exportToXML(bos, true));
            }
        }
    }
}
