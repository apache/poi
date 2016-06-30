/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
 */

package org.apache.poi.hssf.dev;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 *  Utility to test that POI produces readable output
 *  after re-saving xls files.
 *
 *  Usage: ReSave [-dg] input.xls
 *    -dg    initialize drawings, causes to re-build escher aggregates in all sheets
 *    -bos   only write to memory instead of a file
 */
public class ReSave {
    public static void main(String[] args) throws Exception {
        boolean initDrawing = false;
        boolean saveToMemory = false;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for(String filename : args) {
            if(filename.equals("-dg")) {
                initDrawing = true;
            } else if(filename.equals("-bos")) {
                saveToMemory = true;
            } else {
                System.out.print("reading " + filename + "...");
                FileInputStream is = new FileInputStream(filename);
                HSSFWorkbook wb = new HSSFWorkbook(is);
                try {
                    System.out.println("done");
    
                    for(int i = 0; i < wb.getNumberOfSheets(); i++){
                        HSSFSheet sheet = wb.getSheetAt(i);
                        if(initDrawing) {
                            /*HSSFPatriarch dg =*/ sheet.getDrawingPatriarch();
                        }
                    }

                    OutputStream os;
                    if (saveToMemory) {
                        bos.reset();
                        os = bos;
                    } else {
                        String outputFile = filename.replace(".xls", "-saved.xls");
                        System.out.print("saving to " + outputFile + "...");
                        os = new FileOutputStream(outputFile);
                    }
                    
                    try {
                        wb.write(os);
                    } finally {
                        os.close();
                    }
                    System.out.println("done");
                } finally {
                    wb.close();
                    is.close();
                }
            }
        }
    }
}
