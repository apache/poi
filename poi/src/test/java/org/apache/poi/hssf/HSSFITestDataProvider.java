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

package org.apache.poi.hssf;

import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author Yegor Kozlov
 */
public final class HSSFITestDataProvider implements ITestDataProvider {
    public static final HSSFITestDataProvider instance = new HSSFITestDataProvider();

    private HSSFITestDataProvider(){
        // enforce singleton
    }
    
    @Override
    public HSSFWorkbook openSampleWorkbook(String sampleFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
    }
    
    public InputStream openWorkbookStream(String sampleFileName) {
        return HSSFTestDataSamples.openSampleFileStream(sampleFileName);
    }
    
    @Override
    public HSSFWorkbook writeOutAndReadBack(Workbook original) {
        if(!(original instanceof HSSFWorkbook)) {
            throw new IllegalArgumentException("Expected an instance of HSSFWorkbook");
        }
        return HSSFTestDataSamples.writeOutAndReadBack((HSSFWorkbook)original);
    }
    
    @Override
    public HSSFWorkbook createWorkbook(){
        return new HSSFWorkbook();
    }
    
    //************ SXSSF-specific methods ***************//
    @Override
    public HSSFWorkbook createWorkbook(int rowAccessWindowSize) {
        return createWorkbook();
    }
    
    @Override
    public void trackAllColumnsForAutosizing(Sheet sheet) {}
    //************ End SXSSF-specific methods ***************//
    
    @Override
    public FormulaEvaluator createFormulaEvaluator(Workbook wb) {
        return new HSSFFormulaEvaluator((HSSFWorkbook) wb);
    }

    @Override
    public byte[] getTestDataFileContent(String fileName) {
        return POIDataSamples.getSpreadSheetInstance().readFile(fileName);
    }
    
    @Override
    public SpreadsheetVersion getSpreadsheetVersion(){
        return SpreadsheetVersion.EXCEL97;
    }
    
    @Override
    public String getStandardFileNameExtension() {
        return "xls";
    }
}
