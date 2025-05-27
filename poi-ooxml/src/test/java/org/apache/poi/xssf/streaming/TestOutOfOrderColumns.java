package org.apache.poi.xssf.streaming;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;


/**
 * Test creates cells in reverse column order in XSSF and SXSSF and expects
 * saved files to have fixed the order.
 * 
 * This is necessary because if columns in the saved file are out of order
 * Excel will show a repair dialog when opening the file and remove data.
 * 
 * @author schloemer
 */
public final class TestOutOfOrderColumns {

    @Test
    void outOfOrderColumnsXSSF() throws IOException {
        File file = TempFile.createTempFile("poi-xssf-reverse-column-order", ".xlsx");
        
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            
            Row row = sheet.createRow(0);
            // create cells in reverse order
            row.createCell(1).setCellValue("def");
            row.createCell(0).setCellValue("abc");

            try (FileOutputStream os = new FileOutputStream(file)) {
                wb.write(os);
            }
            
            validateOrder(file);
        }
    }

    @Test
    void outOfOrderColumnsSXSSF() throws IOException {
        File file = TempFile.createTempFile("poi-sxssf-reverse-column-order", ".xlsx");
        
        try (SXSSFWorkbook wb = new SXSSFWorkbook()) {
            Sheet sheet = wb.createSheet();
            
            Row row = sheet.createRow(0);
            // create cells in reverse order
            row.createCell(1).setCellValue("xyz");
            row.createCell(0).setCellValue("uvw");
            
            try (FileOutputStream os = new FileOutputStream(file)) {
                wb.write(os);
            }
            
            validateOrder(file);
        }
    }

    @Test
    /** this is the problematic case, as XSSF column sorting is skipped when saving with SXSSF. */
    void mixOfXSSFAndSXSSF() throws IOException {
        File file = TempFile.createTempFile("poi-sxssf-xssf-mix-reverse-column-order", ".xlsx");
        
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet();
            
            Row row1 = sheet.createRow(0);
            // create cells in reverse order
            row1.createCell(1).setCellValue("def");
            row1.createCell(0).setCellValue("abc");

            try (SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(wb)) {
                Sheet sSheet = sxssfWorkbook.getSheetAt(0);
                Row row2 = sSheet.createRow(1);
                // create cells in reverse order
                row2.createCell(1).setCellValue("xyz");
                row2.createCell(0).setCellValue("uvw");
                
                try (FileOutputStream os = new FileOutputStream(file)) {
                    sxssfWorkbook.write(os);
                }
                
                validateOrder(file);
            }
        }
    }

    private void validateOrder(File file) throws FileNotFoundException, IOException {
        // test if saved cells are in order
        try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(file))) {
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
            
            Row resultRow = xssfSheet.getRow(0);
            // POI doesn't show stored order because _cells TreeMap sorts it automatically.
            // The only way to test is to compare the xml.
            String s = resultRow.toString();
            assertTrue(s.matches("(?s).*A1.*B1.*"), s);
        }
    }

}
