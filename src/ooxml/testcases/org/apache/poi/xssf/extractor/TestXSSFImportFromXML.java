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

import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_SPREADSHEETML;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.xpath.XPathExpressionException;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFMap;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TestXSSFImportFromXML {

    @Test
    public void testImportFromXML() throws IOException, XPathExpressionException, SAXException{
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("CustomXMLMappings.xlsx")) {
            String name = "name";
            String teacher = "teacher";
            String tutor = "tutor";
            String cdl = "cdl";
            String duration = "duration";
            String topic = "topic";
            String project = "project";
            String credits = "credits";

            String testXML = "<CORSO>" +
                    "<NOME>" + name + "</NOME>" +
                    "<DOCENTE>" + teacher + "</DOCENTE>" +
                    "<TUTOR>" + tutor + "</TUTOR>" +
                    "<CDL>" + cdl + "</CDL>" +
                    "<DURATA>" + duration + "</DURATA>" +
                    "<ARGOMENTO>" + topic + "</ARGOMENTO>" +
                    "<PROGETTO>" + project + "</PROGETTO>" +
                    "<CREDITI>" + credits + "</CREDITI>" +
                    "</CORSO>\u0000";

            XSSFMap map = wb.getMapInfo().getXSSFMapByName("CORSO_mapping");
            assertNotNull(map);
            XSSFImportFromXML importer = new XSSFImportFromXML(map);

            importer.importFromXML(testXML);

            XSSFSheet sheet = wb.getSheetAt(0);

            XSSFRow row = sheet.getRow(0);
            assertTrue(row.getCell(0).getStringCellValue().equals(name));
            assertTrue(row.getCell(1).getStringCellValue().equals(teacher));
            assertTrue(row.getCell(2).getStringCellValue().equals(tutor));
            assertTrue(row.getCell(3).getStringCellValue().equals(cdl));
            assertTrue(row.getCell(4).getStringCellValue().equals(duration));
            assertTrue(row.getCell(5).getStringCellValue().equals(topic));
            assertTrue(row.getCell(6).getStringCellValue().equals(project));
            assertTrue(row.getCell(7).getStringCellValue().equals(credits));
        }
    }

    @Test(timeout=60000)
    public void testMultiTable() throws IOException, XPathExpressionException, SAXException{
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("CustomXMLMappings-complex-type.xlsx")) {
            String cellC6 = "c6";
            String cellC7 = "c7";
            String cellC8 = "c8";
            String cellC9 = "c9";

            StringBuilder testXML = new StringBuilder("<ns1:MapInfo xmlns:ns1=\"" + NS_SPREADSHEETML + "\" SelectionNamespaces=\"\">" +
                    "<ns1:Schema ID=\"" + cellC6 + "\" SchemaRef=\"a\" />" +
                    "<ns1:Schema ID=\"" + cellC7 + "\" SchemaRef=\"b\" />" +
                    "<ns1:Schema ID=\"" + cellC8 + "\" SchemaRef=\"c\" />" +
                    "<ns1:Schema ID=\"" + cellC9 + "\" SchemaRef=\"d\" />");

            int cellOffset = 10; // cell C10
            for (int i = 0; i < 10000; i++) {
                testXML.append("<ns1:Schema ID=\"c").append(i + cellOffset).append("\" SchemaRef=\"d\" />");
            }

            testXML.append("<ns1:Map ID=\"1\" Name=\"\" RootElement=\"\" SchemaID=\"\" ShowImportExportValidationErrors=\"\" AutoFit=\"\" Append=\"\" PreserveSortAFLayout=\"\" PreserveFormat=\"\">" + "<ns1:DataBinding DataBindingLoadMode=\"\" />" + "</ns1:Map>" + "<ns1:Map ID=\"2\" Name=\"\" RootElement=\"\" SchemaID=\"\" ShowImportExportValidationErrors=\"\" AutoFit=\"\" Append=\"\" PreserveSortAFLayout=\"\" PreserveFormat=\"\">" + "<ns1:DataBinding DataBindingLoadMode=\"\" />" + "</ns1:Map>" + "<ns1:Map ID=\"3\" Name=\"\" RootElement=\"\" SchemaID=\"\" ShowImportExportValidationErrors=\"\" AutoFit=\"\" Append=\"\" PreserveSortAFLayout=\"\" PreserveFormat=\"\">" + "<ns1:DataBinding DataBindingLoadMode=\"\" />" + "</ns1:Map>" + "</ns1:MapInfo>\u0000");

            XSSFMap map = wb.getMapInfo().getXSSFMapByName("MapInfo_mapping");
            assertNotNull(map);
            XSSFImportFromXML importer = new XSSFImportFromXML(map);

            importer.importFromXML(testXML.toString());

            //Check for Schema element
            XSSFSheet sheet = wb.getSheetAt(1);


            // check table size (+1 for the header row)
            assertEquals(3 + 1, wb.getTable("Tabella1").getRowCount());
            assertEquals(10004 + 1, wb.getTable("Tabella2").getRowCount());

            // table1 size was reduced, check that former table cells have been cleared
            assertEquals(CellType.BLANK, wb.getSheetAt(0).getRow(8).getCell(5).getCellType());

            // table2 size was increased, check that new table cells have been cleared
            assertEquals(CellType.BLANK, sheet.getRow(10).getCell(3).getCellType());

            assertEquals(cellC6, sheet.getRow(5).getCell(2).getStringCellValue());
            assertEquals(cellC7, sheet.getRow(6).getCell(2).getStringCellValue());
            assertEquals(cellC8, sheet.getRow(7).getCell(2).getStringCellValue());
            assertEquals(cellC9, sheet.getRow(8).getCell(2).getStringCellValue());
            assertEquals("c5001", sheet.getRow(5000).getCell(2).getStringCellValue());
        }
    }


    @Test
    public void testSingleAttributeCellWithNamespace() throws IOException, XPathExpressionException, SAXException{
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("CustomXMLMapping-singleattributenamespace.xlsx")) {
            int id = 1;
            String displayName = "dispName";
            String ref = "19";
            int count = 21;

            String testXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>" +
                    "<ns1:table xmlns:ns1=\"" + NS_SPREADSHEETML + "\" id=\"" + id + "\" displayName=\"" + displayName + "\" ref=\"" + ref + "\">" +
                    "<ns1:tableColumns count=\"" + count + "\" />" +
                    "</ns1:table>\u0000";
            XSSFMap map = wb.getMapInfo().getXSSFMapByName("table_mapping");
            assertNotNull(map);
            XSSFImportFromXML importer = new XSSFImportFromXML(map);
            importer.importFromXML(testXML);

            //Check for Schema element
            XSSFSheet sheet = wb.getSheetAt(0);

            assertEquals(new Double(id), sheet.getRow(28).getCell(1).getNumericCellValue(), 0);
            assertEquals(displayName, sheet.getRow(11).getCell(5).getStringCellValue());
            assertEquals(ref, sheet.getRow(14).getCell(7).getStringCellValue());
            assertEquals(new Double(count), sheet.getRow(18).getCell(3).getNumericCellValue(), 0);
        }
    }

    @Test
    public void testOptionalFields_Bugzilla_55864() throws IOException, XPathExpressionException, SAXException {
        try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("55864.xlsx")) {
            String testXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<PersonInfoRoot>" +
                    "<PersonData>" +
                    "<FirstName>Albert</FirstName>" +
                    "<LastName>Einstein</LastName>" +
                    "<BirthDate>1879-03-14</BirthDate>" +
                    "</PersonData>" +
                    "</PersonInfoRoot>";

            XSSFMap map = wb.getMapInfo().getXSSFMapByName("PersonInfoRoot_Map");
            assertNotNull(map);
            XSSFImportFromXML importer = new XSSFImportFromXML(map);

            importer.importFromXML(testXML);

            XSSFSheet sheet = wb.getSheetAt(0);

            XSSFRow rowHeadings = sheet.getRow(0);
            XSSFRow rowData = sheet.getRow(1);

            assertEquals("FirstName", rowHeadings.getCell(0).getStringCellValue());
            assertEquals("Albert", rowData.getCell(0).getStringCellValue());

            assertEquals("LastName", rowHeadings.getCell(1).getStringCellValue());
            assertEquals("Einstein", rowData.getCell(1).getStringCellValue());

            assertEquals("BirthDate", rowHeadings.getCell(2).getStringCellValue());
            assertEquals("1879-03-14", rowData.getCell(2).getStringCellValue());

            // Value for OptionalRating is declared optional (minOccurs=0) in 55864.xlsx
            assertEquals("OptionalRating", rowHeadings.getCell(3).getStringCellValue());
            assertNull("", rowData.getCell(3));
        }
    }

    @Test
    public void testOptionalFields_Bugzilla_57890() throws IOException, ParseException, XPathExpressionException, SAXException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("57890.xlsx");

        String testXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + "<TestInfoRoot>"
                + "<TestData>" + "<Int>" + Integer.MIN_VALUE + "</Int>" + "<UnsignedInt>12345</UnsignedInt>"
                + "<double>1.0000123</double>" + "<Date>1991-03-14</Date>" + "</TestData>" + "</TestInfoRoot>";

        XSSFMap map = wb.getMapInfo().getXSSFMapByName("TestInfoRoot_Map");
        assertNotNull(map);
        XSSFImportFromXML importer = new XSSFImportFromXML(map);

        importer.importFromXML(testXML);

        XSSFSheet sheet = wb.getSheetAt(0);

        XSSFRow rowHeadings = sheet.getRow(0);
        XSSFRow rowData = sheet.getRow(1);

        assertEquals("Date", rowHeadings.getCell(0).getStringCellValue());
        Date date = new SimpleDateFormat("yyyy-MM-dd", DateFormatSymbols.getInstance(Locale.ROOT)).parse("1991-3-14");
        assertEquals(date, rowData.getCell(0).getDateCellValue());

        assertEquals("Amount Int", rowHeadings.getCell(1).getStringCellValue());
        assertEquals(new Double(Integer.MIN_VALUE), rowData.getCell(1).getNumericCellValue(), 0);

        assertEquals("Amount Double", rowHeadings.getCell(2).getStringCellValue());
        assertEquals(1.0000123, rowData.getCell(2).getNumericCellValue(), 0);

        assertEquals("Amount UnsignedInt", rowHeadings.getCell(3).getStringCellValue());
        assertEquals(new Double(12345), rowData.getCell(3).getNumericCellValue(), 0);

        wb.close();
    }



}
