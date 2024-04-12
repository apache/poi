package org.apache.poi.xssf.usermodel;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestXSSFTemp {
  @Test
  public void testWrite() throws IOException {
    try(XSSFWorkbook wb = new XSSFWorkbook()) {
      XSSFSheet sheet = wb.createSheet();
      XSSFRow row = sheet.createRow(0);
      for (int i = 0; i < 10000; i++) {
        XSSFCell cell = row.createCell(i);
        cell.setCellValue("cell " + i);
      }
      try (UnsynchronizedByteArrayOutputStream bos = UnsynchronizedByteArrayOutputStream.builder().get()) {
        wb.write(bos);
      }
    }
  }
}
