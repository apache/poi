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

package org.apache.poi.xssf.dev;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;

/**
 * Utility which loads a SpreadsheetML file and saves it back.
 * This is a handy tool to investigate read-write round trip safety.
 *
 * @author Yegor Kozlov
 */
public final class XSSFSave {
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            XSSFWorkbook wb = new XSSFWorkbook(args[i]);

            int sep = args[i].lastIndexOf('.');
            String outfile = args[i].substring(0, sep) + "-save.xls" + (wb.isMacroEnabled() ? "m" : "x");
            FileOutputStream out = new FileOutputStream(outfile);
            wb.write(out);
            out.close();
        }
    }

}
