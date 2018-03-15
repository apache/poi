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

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;

/**
 * Default Paragraph style, from which other styles will override
 * TODO Share logic with {@link XWPFParagraph} which also uses CTPPr
 */
public class XWPFDefaultParagraphStyle {
    private CTPPr ppr;

    public XWPFDefaultParagraphStyle(CTPPr ppr) {
        this.ppr = ppr;
    }

    protected CTPPr getPPr() {
        return ppr;
    }

    public int getSpacingAfter() {
        if (ppr.isSetSpacing())
            return ppr.getSpacing().getAfter().intValue();
        return -1;
    }
}
