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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Removal;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

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

    /**
     * Specifies the font size.
     *
     * @return value representing the font size (non-integer size will be rounded with half rounding up,
     * -1 is returned if size not set)
     * @deprecated use {@link #getFontSizeAsDouble()}
     */
    @Deprecated
    @Removal(version = "6.0.0")
    public int getFontSize() {
        BigDecimal bd = getFontSizeAsBigDecimal(0);
        return bd == null ? -1 : bd.intValue();
    }

    /**
     * Specifies the font size.
     *
     * @return value representing the font size (can be null if size is not set)
     * @since POI 5.0.0
     */
    public Double getFontSizeAsDouble() {
        BigDecimal bd = getFontSizeAsBigDecimal(1);
        return bd == null ? null : bd.doubleValue();
    }

    private BigDecimal getFontSizeAsBigDecimal(int scale) {
        return (rpr != null && rpr.sizeOfSzArray() > 0)
            ? BigDecimal.valueOf(Units.toPoints(POIXMLUnits.parseLength(rpr.getSzArray(0).xgetVal()))).divide(BigDecimal.valueOf(4), scale, RoundingMode.HALF_UP)
            : null;
    }
}
