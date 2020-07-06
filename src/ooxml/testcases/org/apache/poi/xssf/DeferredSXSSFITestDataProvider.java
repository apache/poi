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
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.DeferredSXSSFSheet;
import org.apache.poi.xssf.streaming.DeferredSXSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

public final class DeferredSXSSFITestDataProvider implements ITestDataProvider {
    public static final DeferredSXSSFITestDataProvider instance = new DeferredSXSSFITestDataProvider();

    // an instance of all DeferredSXSSFWorkbooks opened by this TestDataProvider,
    // so that the temporary files created can be disposed up by cleanup()
    private final Collection<DeferredSXSSFWorkbook> instances = new ArrayList<>();

    private DeferredSXSSFITestDataProvider() {
        // enforce singleton
    }

    @Override
    public Workbook openSampleWorkbook(String sampleFileName) {
        XSSFWorkbook xssfWorkbook = XSSFITestDataProvider.instance.openSampleWorkbook(sampleFileName);
        DeferredSXSSFWorkbook swb = new DeferredSXSSFWorkbook(xssfWorkbook);
        instances.add(swb);
        return swb;
    }

    /**
     * Returns an XSSFWorkbook since SXSSFWorkbook is write-only
     */
    @Override
    public XSSFWorkbook writeOutAndReadBack(Workbook wb) {
        // wb is usually an SXSSFWorkbook, but must also work on an XSSFWorkbook
        // since workbooks must be able to be written out and read back
        // several times in succession
        if(!(wb instanceof SXSSFWorkbook || wb instanceof XSSFWorkbook)) {
            throw new IllegalArgumentException("Expected an instance of XSSFWorkbook");
        }

        XSSFWorkbook result;
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

    @Override
    public DeferredSXSSFWorkbook createWorkbook() {
        DeferredSXSSFWorkbook wb = new DeferredSXSSFWorkbook();
        instances.add(wb);
        return wb;
    }
    
    //************ SXSSF-specific methods ***************//
    @Override
    public DeferredSXSSFWorkbook createWorkbook(int rowAccessWindowSize) {
        DeferredSXSSFWorkbook wb = new DeferredSXSSFWorkbook(rowAccessWindowSize);
        instances.add(wb);
        return wb;
    }
    
    @Override
    public void trackAllColumnsForAutosizing(Sheet sheet) {
        ((DeferredSXSSFSheet)sheet).trackAllColumnsForAutoSizing();
    }
    //************ End SXSSF-specific methods ***************//
    
    @Override
    public FormulaEvaluator createFormulaEvaluator(Workbook wb) {
        return new XSSFFormulaEvaluator(((DeferredSXSSFWorkbook) wb).getXSSFWorkbook());
    }

    @Override
    public byte[] getTestDataFileContent(String fileName) {
        return POIDataSamples.getSpreadSheetInstance().readFile(fileName);
    }

    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        return SpreadsheetVersion.EXCEL2007;
    }

    @Override
    public String getStandardFileNameExtension() {
        return "xlsx";
    }

    public synchronized boolean cleanup() {
        boolean ok = true;
        for(final DeferredSXSSFWorkbook wb : instances) {
            ok = ok && wb.dispose();
        }
        instances.clear();
        return ok;
    }
}
