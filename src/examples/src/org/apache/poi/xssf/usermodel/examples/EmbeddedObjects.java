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

import java.io.InputStream;

import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Demonstrates how you can extract embedded data from a .xlsx file
 */
public class EmbeddedObjects {
    public static void main(String[] args) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook(args[0]);
        for (PackagePart pPart : workbook.getAllEmbedds()) {
            String contentType = pPart.getContentType();
            if (contentType.equals("application/vnd.ms-excel")) {
                // Excel Workbook - either binary or OpenXML
                HSSFWorkbook embeddedWorkbook = new HSSFWorkbook(pPart.getInputStream());
                embeddedWorkbook.close();
            } else if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                // Excel Workbook - OpenXML file format
                XSSFWorkbook embeddedWorkbook = new XSSFWorkbook(pPart.getInputStream());
                embeddedWorkbook.close();
            } else if (contentType.equals("application/msword")) {
                // Word Document - binary (OLE2CDF) file format
                HWPFDocument document = new HWPFDocument(pPart.getInputStream());
                document.close();
            } else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                // Word Document - OpenXML file format
                XWPFDocument document = new XWPFDocument(pPart.getInputStream());
                document.close();
            } else if (contentType.equals("application/vnd.ms-powerpoint")) {
                // PowerPoint Document - binary file format
                HSLFSlideShowImpl slideShow = new HSLFSlideShowImpl(pPart.getInputStream());
                slideShow.close();
            } else if (contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                // PowerPoint Document - OpenXML file format
                OPCPackage docPackage = OPCPackage.open(pPart.getInputStream());
                XSLFSlideShow slideShow = new XSLFSlideShow(docPackage);
                slideShow.close();
            } else {
                // Any other type of embedded object.
                System.out.println("Unknown Embedded Document: " + contentType);
                InputStream inputStream = pPart.getInputStream();
                inputStream.close();
            }
        }
        workbook.close();
    }
}