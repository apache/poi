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

package org.apache.poi.xssf.usermodel.examples;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Demonstrates how to insert pictures in a SpreadsheetML document
 *
 * @author Yegor Kozlov
 */
public class WorkingWithPictures {
    public static void main(String[] args) throws IOException {

        //create a new workbook
        Workbook wb = new XSSFWorkbook(); //or new HSSFWorkbook();
        CreationHelper helper = wb.getCreationHelper();

        //add a picture in this workbook.
        InputStream is = new FileInputStream(args[0]);
        byte[] bytes = IOUtils.toByteArray(is);
        is.close();
        int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);

        //create sheet
        Sheet sheet = wb.createSheet();

        //create drawing
        Drawing drawing = sheet.createDrawingPatriarch();

        //add a picture shape
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(1);
        anchor.setRow1(1);
        Picture pict = drawing.createPicture(anchor, pictureIdx);

        //auto-size picture
        pict.resize(2);

        //save workbook
        String file = "picture.xls";
        if(wb instanceof XSSFWorkbook) file += "x";
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fileOut.close();
    }
}
