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

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.InputStream;

/**
 * Demonstrates how you can extract embedded data from a .xlsx file
 */
public class EmbeddedObjects {
    public static void main(String[] args) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook(args[0]);
        for (PackagePart pPart : workbook.getAllEmbedds()) {
            String contentType = pPart.getContentType();
            // Excel Workbook – either binary or OpenXML
            if (contentType.equals("application/vnd.ms-excel")) {
                HSSFWorkbook embeddedWorkbook = new HSSFWorkbook(pPart.getInputStream());
            }
            // Excel Workbook – OpenXML file format
            else if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                XSSFWorkbook embeddedWorkbook = new XSSFWorkbook(pPart.getInputStream());
            }
            // Word Document – binary (OLE2CDF) file format
            else if (contentType.equals("application/msword")) {
                HWPFDocument document = new HWPFDocument(pPart.getInputStream());
            }
            // Word Document – OpenXML file format
            else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                XWPFDocument document = new XWPFDocument(pPart.getInputStream());
            }
            // PowerPoint Document – binary file format
            else if (contentType.equals("application/vnd.ms-powerpoint")) {
                HSLFSlideShow slideShow = new HSLFSlideShow(pPart.getInputStream());
            }
            // PowerPoint Document – OpenXML file format
            else if (contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                OPCPackage docPackage = OPCPackage.open(pPart.getInputStream());
                XSLFSlideShow slideShow = new XSLFSlideShow(docPackage);
            }
            // Any other type of embedded object.
            else {
                System.out.println("Unknown Embedded Document: " + contentType);
                InputStream inputStream = pPart.getInputStream();
            }
        }
    }
}