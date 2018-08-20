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

import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.Enum;

/**
 * The width types for tables and table cells. Table width can be specified as "auto" (AUTO),
 * an absolute value in 20ths of a point (DXA), or as a percentage (PCT).
 * @since 4.0.0
 */
public enum TableWidthType {
    AUTO(STTblWidth.AUTO), /* Width is determined by content. */
    DXA(STTblWidth.DXA),   /* Width is an integer number of 20ths of a point (twips) */
    NIL(STTblWidth.NIL),   /* No width value set */
    PCT(STTblWidth.PCT);   /* Width is a percentage, e.g. "33.3%" or 50 times percentage value, rounded to an integer, */ 
                           /* e.g. 2500 for 50% */

    private Enum type = STTblWidth.NIL;

    TableWidthType(STTblWidth.Enum type) {
        this.type = type;
    }
    
    protected STTblWidth.Enum getSTWidthType() {
        return this.type;
    }
    
    /**
     * Get the underlying STTblWidth enum value.
     *
     * @return STTblWidth.Enum value
     */
    @Internal
    public STTblWidth.Enum getStWidthType() {
        return this.type;
    }
}
