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

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdn;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class XWPFFootnote implements Iterable<XWPFParagraph> {
    private List<XWPFParagraph> paragraphs = new ArrayList<XWPFParagraph>();

    public XWPFFootnote(XWPFDocument document, CTFtnEdn body) {
        for (CTP p : body.getPArray())	{
            paragraphs.add(new XWPFParagraph(p, document));
        }
    }

    public List<XWPFParagraph> getParagraphs() {
        return paragraphs;
    }

    public Iterator<XWPFParagraph> iterator(){
        return paragraphs.iterator();
    }

}
