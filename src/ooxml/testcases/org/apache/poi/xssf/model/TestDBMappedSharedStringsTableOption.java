package org.apache.poi.xssf.model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.SXSSFITestDataProvider;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class TestDBMappedSharedStringsTableOption {
    //Streaming version of workbook
    private SXSSFWorkbook workbook;

    private SXSSFSheet sheet;

    private File outputFile;
    public static final String TEST_OUTPUT_DIR = "poi.test.xssf.output.dir";

    @Before
    public void setUp() {
        outputFile = new File(System.getProperty(TEST_OUTPUT_DIR), "output.xlsx");
        setupWorkBook();
        setupBlankSheet();
    }

    private void setupWorkBook() {
        XSSFWorkbook wb = new XSSFWorkbook(SharedStringsTableType.LOW_FOOTPRINT_MAP_DB_SST);
        workbook = new SXSSFWorkbook(wb, 2, false, true);
    }

    private void setupBlankSheet() {
        sheet = (SXSSFSheet) workbook.createSheet("Employee Data");
    }

    @After
    public void cleanup() {
        outputFile.delete();
    }

    @Test
    public void testWrite100UniqueRecordsOf10Char() throws IOException {
        int recordCount = 100;
        addUniqueRecordsToSheet(0, 100, 10);
        writeAndAssertRecord(recordCount);
    }

    @Test
    public void testWrite1MUniqueRecordsOf100Char() {
        int recordCount = 1000000;
        addUniqueRecordsToSheet(0, recordCount, 100);
        writeAndAssertRecord(recordCount);
    }

    @Test
    public void testWriteFromTextFile() {
        int recordCount = 3;
        File textInputFile = new File(System.getProperty(TEST_OUTPUT_DIR), "temp.txt");
        try {
            FileWriter w = new FileWriter(textInputFile);
            for (int i = 1; i <= recordCount; i++) {
                w.write("Line" + i + ",FirstColumn,SecondColumn,ThirdColumn\r\n");
            }
            w.close();
        } catch (IOException e) {
        }
        addRecordsFromFile("temp.txt");
        writeAndAssertRecord(recordCount);
        textInputFile.delete();
    }

    @Test
    public void testWrite1MRandomRecordsOf10Char() {
        int recordCount = 100000;
        addRandomRecordsToSheet(0, recordCount, 200000, 10);
        writeAndAssertRecord(recordCount);
    }

    @Test
    public void test1MRecordHavingRepetitiveRecordsOf10Char() {
        int recordCount = 1000000;
        addUniqueRecordsToSheet(0, 200000, 10);
        addUniqueRecordsToSheet(200000, 200000, 10);
        addUniqueRecordsToSheet(400000, 200000, 10);
        addUniqueRecordsToSheet(600000, 200000, 10);
        addUniqueRecordsToSheet(800000, 200000, 10);
        writeAndAssertRecord(recordCount);
    }

    @Test
    public void testWriteAllDuplicateRecord() {
        int recordCount = 100000;
        addRepeatingRecordsToSheet(recordCount);
        writeAndAssertRecord(recordCount);
    }

    private void writeAndAssertRecord(int recordCount) {
        System.out.print("Started writing.....");
        //NOTE: all tests can be executed within -Xmx100M by commenting out out code below
        //----
        XSSFWorkbook wb = (XSSFWorkbook) SXSSFITestDataProvider.instance.writeOutAndReadBack(workbook);
        System.out.println("File creation done...Asserting");
        assertRows(wb, recordCount);
        //----
    }

    private void addUniqueRecordsToSheet(int fromRowNum, int numberOfRecords, int constantStringLength) {
        System.out.print("adding records to sheet.....");
        int i = 0;
        String constantString = getStringOf(constantStringLength);
        while (i++ < numberOfRecords) {
            if (i % 10000 == 0) System.out.print(i + ",");
            Row row = sheet.createRow(fromRowNum++);
            Object[] objArr = new Object[]{constantString + i};
            int cellNum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellNum++);
                if (obj instanceof String) {
                    cell.setCellValue((String) obj);
                } else if (obj instanceof Integer)
                    cell.setCellValue((Integer) obj);
            }
        }
    }

    private String getStringOf(int length) {
        StringBuilder str = new StringBuilder();
        for (int j = 0; j < length; j++) {
            str.append("a");
        }
        return str.toString();
    }

    private void addRandomRecordsToSheet(int fromRowNum, int numberOfRecords, int recordLength, int constantStringLength) {
        int i = 0;
        String constantString = getStringOf(constantStringLength);
        while (i++ < numberOfRecords) {
            if (i % 1000 == 0) System.out.print(i + ",");
            Row row = sheet.createRow(fromRowNum++);
            Object[] objArr = new Object[]{constantString + new Random().nextInt(recordLength)};
            int cellNum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellNum++);
                if (obj instanceof String)
                    cell.setCellValue((String) obj);
                else if (obj instanceof Integer)
                    cell.setCellValue((Integer) obj);
            }
        }
    }

    private void addRecordsFromFile(String fileName) {
        System.out.print("adding records to sheet.....");
        try {
            int fromRowNum = 0;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            String line = null;
            while ((line = br.readLine()) != null) {
                Row row = sheet.createRow(fromRowNum++);
                Object[] objArr = line.split(",");
                int cellNum = 0;
                for (Object obj : objArr) {
                    Cell cell = row.createCell(cellNum++);
                    if (obj instanceof String)
                        cell.setCellValue((String) obj);
                    else if (obj instanceof Integer)
                        cell.setCellValue((Integer) obj);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRepeatingRecordsToSheet(int count) {
        int rownum = 0;
        int i = 0;
        String constantString = getStringOf(10);
        while (i++ < count) {
            Row row = sheet.createRow(rownum++);
            Object[] objArr = new Object[]{constantString};
            int cellnum = 0;
            for (Object obj : objArr) {
                Cell cell = row.createCell(cellnum++);
                if (obj instanceof String)
                    cell.setCellValue((String) obj);
                else if (obj instanceof Integer)
                    cell.setCellValue((Integer) obj);
            }
        }
    }

    public void assertRows(Workbook wb, int expectedRecordCount) {
        assertEquals(expectedRecordCount, wb.getSheetAt(0).getLastRowNum() + 1);
    }

}
