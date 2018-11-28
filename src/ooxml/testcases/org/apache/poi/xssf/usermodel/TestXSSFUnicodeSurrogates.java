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

    // "𝝊𝝋𝝌𝝍𝝎𝝏𝝐𝝑𝝒𝝓𝝔𝝕𝝖𝝗𝝘𝝙𝝚𝝛𝝜𝝝𝝞𝝟𝝠𝝡𝝢𝝣𝝤𝝥𝝦𝝧𝝨𝝩𝝪𝝫𝝬𝝭𝝮𝝯𝝰𝝱𝝲𝝳𝝴𝝵𝝶𝝷𝝸𝝹𝝺";
    private static String unicodeText =
        "\uD835\uDF4A\uD835\uDF4B\uD835\uDF4C\uD835\uDF4D\uD835\uDF4E\uD835\uDF4F\uD835\uDF50\uD835" +
        "\uDF51\uD835\uDF52\uD835\uDF53\uD835\uDF54\uD835\uDF55\uD835\uDF56\uD835\uDF57\uD835\uDF58" +
        "\uD835\uDF59\uD835\uDF5A\uD835\uDF5B\uD835\uDF5C\uD835\uDF5D\uD835\uDF5E\uD835\uDF5F\uD835" +
        "\uDF60\uD835\uDF61\uD835\uDF62\uD835\uDF63\uD835\uDF64\uD835\uDF65\uD835\uDF66\uD835\uDF67" +
        "\uD835\uDF68\uD835\uDF69\uD835\uDF6A\uD835\uDF6B\uD835\uDF6C\uD835\uDF6D\uD835\uDF6E\uD835" +
        "\uDF6F\uD835\uDF70\uD835\uDF71\uD835\uDF72\uD835\uDF73\uD835\uDF74\uD835\uDF75\uD835\uDF76" +
        "\uD835\uDF77\uD835\uDF78\uD835\uDF79\uD835\uDF7A";

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

