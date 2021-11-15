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

package org.apache.poi.ss.usermodel;

import org.apache.poi.util.Beta;

import java.util.HashMap;
import java.util.Map;

/**
 * Used when the cells are being copied from one workbook to another. Data like cell styles
 * need to be managed so that we do not create too many items in the destination workbook.
 */
@Beta
public class CellCopyContext {
    private final Map<CellStyle, CellStyle> styleMap = new HashMap<>();

    /**
     * @param srcStyle style in source workbook
     * @return style that srcStyle is mapped to or null if no mapping exists
     */
    public CellStyle getMappedStyle(CellStyle srcStyle) {
        return styleMap.get(srcStyle);
    }

    /**
     * @param srcStyle style in source workbook
     * @param mappedStyle equivalent style in destination workbook
     */
    public void putMappedStyle(CellStyle srcStyle, CellStyle mappedStyle) {
        styleMap.put(srcStyle, mappedStyle);
    }
}
