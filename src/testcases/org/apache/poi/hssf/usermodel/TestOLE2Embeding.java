package org.apache.poi.hssf.usermodel;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;

public class TestOLE2Embeding extends TestCase {
  public void testEmbeding() throws Exception {
    String dirname = System.getProperty("HSSF.testdata.path");
    String filename = dirname + "/ole2-embedding.xls";

    File file = new File(filename);
    FileInputStream in = new FileInputStream(file);
    HSSFWorkbook workbook;

	// This used to break, until bug #43116 was fixed
    workbook = new HSSFWorkbook(in);

    in.close();

    // Check we can get at the Escher layer still
    workbook.getAllPictures();
  }
}

