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
package org.apache.poi.xwpf.usermodel;

import java.io.FileOutputStream;

/**
 * A simple WOrdprocessingML table created by POI XWPF API
 *
 * @author gisella bronzetti
 */
public class SimpleTable {

    public static void main(String[] args) throws Exception {
        XWPFDocument doc = new XWPFDocument();

        XWPFTable table=doc.createTable(3,3);
        
        table.getRow(1).getCell(1).setText("EXAMPLE OF TABLE");
        
        
        XWPFParagraph p1 = doc.createParagraph();

        XWPFRun r1 = p1.createRun();
        r1.setBold(true);
        r1.setText("The quick brown fox");
        r1.setBold(true);
        r1.setFontFamily("Courier");
        r1.setUnderline(UnderlinePatterns.DOT_DOT_DASH);
        r1.setTextPosition(100);

        table.getRow(0).getCell(0).setParagraph(p1);
        

        table.getRow(2).getCell(2).setText("only text");

        FileOutputStream out = new FileOutputStream("simpleTable.docx");
        doc.write(out);
        out.close();

    }
}
