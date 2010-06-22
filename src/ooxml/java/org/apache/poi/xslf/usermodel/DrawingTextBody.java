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

import java.util.List;

import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;

public class DrawingTextBody {
    private final CTTextBody textBody;

    public DrawingTextBody(CTTextBody textBody) {
        this.textBody = textBody;
    }

    public DrawingParagraph[] getParagraphs() {
        List<CTTextParagraph> paragraphs = textBody.getPList();
        DrawingParagraph[] o = new DrawingParagraph[paragraphs.size()];

        for (int i=0; i<o.length; i++) {
            o[i] = new DrawingParagraph(paragraphs.get(i));
        }

        return o;
    }
}
