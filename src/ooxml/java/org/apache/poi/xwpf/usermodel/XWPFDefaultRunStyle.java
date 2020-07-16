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

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Default Character Run style, from which other styles will override
 * TODO Share logic with {@link XWPFRun} which also uses CTRPr
 */
public class XWPFDefaultRunStyle {
    private CTRPr rpr;

    public XWPFDefaultRunStyle(CTRPr rpr) {
        this.rpr = rpr;
    }

    protected CTRPr getRPr() {
        return rpr;
    }

    public int getFontSize() {
        BigDecimal bd = getFontSizeAsBigDecimal(0);
        return bd == null ? -1 : bd.intValue();
    }

    public Double getFontSizeAsDouble() {
        BigDecimal bd = getFontSizeAsBigDecimal(1);
        return bd == null ? null : bd.doubleValue();
    }

    private BigDecimal getFontSizeAsBigDecimal(int scale) {
        return (rpr != null && rpr.isSetSz()) ?
                new BigDecimal(rpr.getSz().getVal()).divide(BigDecimal.valueOf(2)).setScale(scale, RoundingMode.HALF_UP) :
                null;
    }
}
