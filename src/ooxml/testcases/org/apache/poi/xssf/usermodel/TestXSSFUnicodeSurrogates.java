package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.TempFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TestXSSFUnicodeSurrogates {

    private static String unicodeText = "𝝊𝝋𝝌𝝍𝝎𝝏𝝐𝝑𝝒𝝓𝝔𝝕𝝖𝝗𝝘𝝙𝝚𝝛𝝜𝝝𝝞𝝟𝝠𝝡𝝢𝝣𝝤𝝥𝝦𝝧𝝨𝝩𝝪𝝫𝝬𝝭𝝮𝝯𝝰𝝱𝝲𝝳𝝴𝝵𝝶𝝷𝝸𝝹𝝺";

    @Test
    public void testWriteUnicodeSurrogates() throws IOException {
        String sheetName = "Sheet1";
        File tf = TempFile.createTempFile("poi-xmlbeans-test", ".xlsx");
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(sheetName);
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue(unicodeText);
            try (FileOutputStream os = new FileOutputStream(tf)) {
                wb.write(os);
            }
            try (FileInputStream fis = new FileInputStream(tf);
                 XSSFWorkbook wb2 = new XSSFWorkbook(fis)) {
                Sheet sheet2 = wb2.getSheet(sheetName);
                Cell cell2 = sheet2.getRow(0).getCell(0);
                Assert.assertEquals(unicodeText, cell2.getStringCellValue());
            }
        } finally {
            tf.delete();
        }
    }
}

