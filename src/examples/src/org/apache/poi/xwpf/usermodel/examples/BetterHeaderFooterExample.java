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
package org.apache.poi.xwpf.usermodel.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class BetterHeaderFooterExample {

    public static void main(String[] args) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {

            XWPFParagraph p = doc.createParagraph();

            XWPFRun r = p.createRun();
            r.setText("Some Text");
            r.setBold(true);
            r = p.createRun();
            r.setText("Goodbye");

            // create header/footer functions insert an empty paragraph
            XWPFHeader head = doc.createHeader(HeaderFooterType.DEFAULT);
            head.createParagraph().createRun().setText("header");

            XWPFFooter foot = doc.createFooter(HeaderFooterType.DEFAULT);
            foot.createParagraph().createRun().setText("footer");

            try (OutputStream os = new FileOutputStream(new File("header2.docx"))) {
                doc.write(os);
            }
        }
    }
}
