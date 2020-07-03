/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.streaming.examples;

import org.apache.poi.xssf.streaming.DeferredSXSSFSheet;
import org.apache.poi.xssf.streaming.DeferredSXSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;

import java.io.FileOutputStream;

/**
 * An example that outputs a simple generated workbook that uses deferred creation of the rows in the sheets.
 * When the workbook is written to an output stream, the output data is streamed immediately.
 * There is no collation of the data in temp files prior to writing the overall file.
 * This depends on how efficient the row generating functions are.
 */
public final class DeferredGeneration {

    private DeferredGeneration() {}

    public static void main(String[] args) throws Exception {
        String filename;
        if(args.length == 0) {
            filename = "deferred-generation.xlsx";
        } else {
            filename = args[0];
        }
        try (DeferredSXSSFWorkbook wb = new DeferredSXSSFWorkbook()){
            for(int i = 0; i < 10; i++) {
                DeferredSXSSFSheet sheet = wb.createSheet("Sheet" + i);
                sheet.setRowGenerator((sh) -> {
                    for (int r = 0; r < 1000; r++) {
                        SXSSFRow row = sheet.createRow(r);
                        for (int c = 0; c < 100; c++) {
                            SXSSFCell cell = row.createCell(c);
                            cell.setCellValue("abcd");
                        }
                    }
                });
            }
            wb.write(new FileOutputStream(filename));
            System.out.println("wrote " + filename);
        }
    }
}
