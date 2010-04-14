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

package org.apache.poi.xslf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextLineBreak;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

public class DrawingParagraph {
    private final CTTextParagraph p;

    public DrawingParagraph(CTTextParagraph p) {
        this.p = p;
    }

    public CharSequence getText() {
        StringBuilder text = new StringBuilder();

        XmlCursor c = p.newCursor();
        c.selectPath("./*");
        while (c.toNextSelection()) {
            XmlObject o = c.getObject();
            if (o instanceof CTRegularTextRun) {
                CTRegularTextRun txrun = (CTRegularTextRun) o;
                text.append(txrun.getT());
            } else if (o instanceof CTTextLineBreak) {
                text.append('\n');
            }
        }
        
        return text;
    }
}
