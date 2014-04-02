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

import static org.junit.Assert.assertEquals;

import org.apache.poi.xwpf.usermodel.XWPFRun.FontCharRange;
import org.junit.Test;

public class TestXWPFBugs {
    @Test
    public void bug55802() throws Exception {
        String blabla =
            "Bir, iki, \u00fc\u00e7, d\u00f6rt, be\u015f,\n"+
            "\nalt\u0131, yedi, sekiz, dokuz, on.\n"+
            "\nK\u0131rm\u0131z\u0131 don,\n"+
            "\ngel bizim bah\u00e7eye kon,\n"+
            "\nsar\u0131 limon";
        XWPFDocument doc = new XWPFDocument();
        XWPFRun run = doc.createParagraph().createRun();
        
        for (String str : blabla.split("\n")) {
            run.setText(str);
            run.addBreak();
        }

        run.setFontFamily("Times New Roman");
        run.setFontSize(20);
        assertEquals(run.getFontFamily(), "Times New Roman");
        assertEquals(run.getFontFamily(FontCharRange.cs), "Times New Roman");
        assertEquals(run.getFontFamily(FontCharRange.eastAsia), "Times New Roman");
        assertEquals(run.getFontFamily(FontCharRange.hAnsi), "Times New Roman");
        run.setFontFamily("Arial", FontCharRange.hAnsi);
        assertEquals(run.getFontFamily(FontCharRange.hAnsi), "Arial");
    }

}
