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

package org.apache.poi.xssf;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Yegor Kozlov
 */
public final class SXSSFITestDataProvider implements ITestDataProvider {
    public static final SXSSFITestDataProvider instance = new SXSSFITestDataProvider();

    private SXSSFITestDataProvider() {
        // enforce singleton
    }
    public Workbook openSampleWorkbook(String sampleFileName) {
        throw new IllegalArgumentException("SXSSF cannot read files");
    }

    public Workbook writeOutAndReadBack(Workbook wb) {
        if(!(wb instanceof SXSSFWorkbook)) {
            throw new IllegalArgumentException("Expected an instance of SXSSFWorkbook");
        }

        Workbook result;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            wb.write(baos);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            result = new XSSFWorkbook(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
    public SXSSFWorkbook createWorkbook(){
        return new SXSSFWorkbook();
    }
    public byte[] getTestDataFileContent(String fileName) {
        return POIDataSamples.getSpreadSheetInstance().readFile(fileName);
    }
    public SpreadsheetVersion getSpreadsheetVersion(){
        return SpreadsheetVersion.EXCEL2007;
    }
    public String getStandardFileNameExtension() {
        return "xlsx";
    }
}
