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

package org.apache.poi.xssf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.util.TempFile;

/**
 * Centralises logic for finding/opening sample files in the src/testcases/org/apache/poi/hssf/hssf/data folder. 
 * 
 * @author Josh Micich
 */
public class XSSFTestDataSamples {
	public static final XSSFWorkbook openSampleWorkbook(String sampleName) {
		InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleName);
		try {
			return new XSSFWorkbook(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
    public static <R extends Workbook> R writeOutAndReadBack(R wb) {
    	Workbook result;
		try {
	    	if (wb instanceof HSSFWorkbook) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
                wb.write(baos);
                InputStream is = new ByteArrayInputStream(baos.toByteArray());
	    		result = new HSSFWorkbook(is);
	    	} else if (wb instanceof XSSFWorkbook) {
                File tmp = TempFile.createTempFile("poi-ooxml-", ".xlsx");
                FileOutputStream out = new FileOutputStream(tmp);
                wb.write(out);
                out.close();
                OPCPackage pkg = OPCPackage.open(tmp.getAbsolutePath());
    			result = new XSSFWorkbook(pkg);
	    	} else {
	    		throw new RuntimeException("Unexpected workbook type (" 
	    				+ wb.getClass().getName() + ")");
	    	}
		} catch (InvalidFormatException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		@SuppressWarnings("unchecked")
		R r = (R) result;
		return r;
    }
}
