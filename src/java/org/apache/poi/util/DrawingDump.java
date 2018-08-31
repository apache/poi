
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
        
package org.apache.poi.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Dump out the aggregated escher records
 */
public final class DrawingDump {
    private DrawingDump() {
    }

    public static void main( String[] args ) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(System.out, Charset.defaultCharset());
        PrintWriter pw = new PrintWriter(osw);
        POIFSFileSystem fs = new POIFSFileSystem(new File(args[0]));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        try {
            pw.println( "Drawing group:" );
            wb.dumpDrawingGroupRecords(true);
    
            int i = 1;
            for (Sheet sheet : wb)
            {
                pw.println( "Sheet " + i + "(" + sheet.getSheetName() + "):" );
                ((HSSFSheet) sheet).dumpDrawingRecords(true, pw);
            }
        } finally {
            wb.close();
            fs.close();
        }
    }
}
