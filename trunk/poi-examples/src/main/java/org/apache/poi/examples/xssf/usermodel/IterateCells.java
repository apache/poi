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

package org.apache.poi.examples.xssf.usermodel;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *  Iterate over rows and cells
 */
@SuppressWarnings({"java:S106","java:S4823","java:S1192"})
public final class IterateCells {

    private IterateCells() {}

    public static void main(String[] args) throws IOException {
        try (
                FileInputStream is = new FileInputStream(args[0]);
                Workbook wb = new XSSFWorkbook(is)
            ) {
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                System.out.println(wb.getSheetName(i));
                for (Row row : sheet) {
                    System.out.println("rownum: " + row.getRowNum());
                    for (Cell cell : row) {
                        System.out.println(cell);
                    }
                }
            }
        }
    }
}
