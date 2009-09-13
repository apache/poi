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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.POIDataSamples;

/**
 * @author Yegor Kozlov
 */
public final class HSSFITestDataProvider implements ITestDataProvider {

    public HSSFWorkbook openSampleWorkbook(String sampleFileName) {
		return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
	}

	public HSSFWorkbook writeOutAndReadBack(Workbook original) {
        if(!(original instanceof HSSFWorkbook)) {
            throw new IllegalArgumentException("Expected an instance of HSSFWorkbook");
        }

        return HSSFTestDataSamples.writeOutAndReadBack((HSSFWorkbook)original);
	}

    public HSSFWorkbook createWorkbook(){
        return new HSSFWorkbook();
    }

    public byte[] getTestDataFileContent(String fileName) {
        return POIDataSamples.getSpreadSheetInstance().readFile(fileName);
    }

    public SpreadsheetVersion getSpreadsheetVersion(){
        return SpreadsheetVersion.EXCEL97;
    }

    private HSSFITestDataProvider(){}
    private static HSSFITestDataProvider inst = new HSSFITestDataProvider();
    public static HSSFITestDataProvider getInstance(){
        return inst;
    }
}
