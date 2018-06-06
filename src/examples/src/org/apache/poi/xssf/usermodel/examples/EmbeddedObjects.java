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

import java.io.Closeable;
import java.io.InputStream;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Demonstrates how you can extract embedded data from a .xlsx file
 */
public class EmbeddedObjects {
    public static void main(String[] args) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(args[0])) {
            for (PackagePart pPart : workbook.getAllEmbeddedParts()) {
                String contentType = pPart.getContentType();
                try (InputStream is = pPart.getInputStream()) {
                    Closeable document;
                    if (contentType.equals("application/vnd.ms-excel")) {
                        // Excel Workbook - either binary or OpenXML
                        document = new HSSFWorkbook(is);
                    } else if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                        // Excel Workbook - OpenXML file format
                        document = new XSSFWorkbook(is);
                    } else if (contentType.equals("application/msword")) {
                        // Word Document - binary (OLE2CDF) file format
                        document = new HWPFDocument(is);
                    } else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                        // Word Document - OpenXML file format
                        document = new XWPFDocument(is);
                    } else if (contentType.equals("application/vnd.ms-powerpoint")) {
                        // PowerPoint Document - binary file format
                        document = new HSLFSlideShow(is);
                    } else if (contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                        // PowerPoint Document - OpenXML file format
                        document = new XMLSlideShow(is);
                    } else {
                        // Any other type of embedded object.
                        document = is;
                    }
                    document.close();
                }
            }
        }
    }
}