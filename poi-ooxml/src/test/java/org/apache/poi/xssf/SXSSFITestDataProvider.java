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
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author Yegor Kozlov
 */
public final class SXSSFITestDataProvider implements ITestDataProvider {
    public static final SXSSFITestDataProvider instance = new SXSSFITestDataProvider();

    private ArrayList<SXSSFWorkbook> instances = new ArrayList<SXSSFWorkbook>();

    private SXSSFITestDataProvider() {
        // enforce singleton
    }
    public Workbook openSampleWorkbook(String sampleFileName) {
    	XSSFWorkbook xssfWorkbook = XSSFITestDataProvider.instance.openSampleWorkbook(sampleFileName);
        SXSSFWorkbook swb = new SXSSFWorkbook(xssfWorkbook);
        instances.add(swb);
    	return swb;
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
        SXSSFWorkbook wb = new SXSSFWorkbook();
        instances.add(wb);
        return wb;
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

    public synchronized boolean cleanup(){
        boolean ok = true;
        for(int i = 0; i < instances.size(); i++){
            SXSSFWorkbook wb = instances.get(i);
            ok = ok && wb.dispose();
            instances.remove(i);
        }
        return ok;
    }
}
