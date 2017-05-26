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

package org.apache.poi.xssf.usermodel;

import java.util.EnumMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.DifferentialStyleProvider;
import org.apache.poi.ss.usermodel.TableStyle;
import org.apache.poi.ss.usermodel.TableStyleType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleElement;

/**
 * {@link TableStyle} implementation for styles defined in the OOXML styles.xml.
 * Also used for built-in styles via dummy XML generated from presetTableStyles.xml.
 */
public class XSSFTableStyle implements TableStyle {

    private final String name;
    private final int index;
    private final Map<TableStyleType, DifferentialStyleProvider> elementMap = new EnumMap<TableStyleType, DifferentialStyleProvider>(TableStyleType.class);

    /**
     * @param index style definition index or built-in ordinal depending on use
     * @param dxfs
     * @param tableStyle
     * @param colorMap indexed color map - default or custom
     * @see TableStyle#getIndex()
     */
    public XSSFTableStyle(int index, CTDxfs dxfs, CTTableStyle tableStyle, IndexedColorMap colorMap) {
        this.name = tableStyle.getName();
        this.index = index;
        for (CTTableStyleElement element : tableStyle.getTableStyleElementList()) {
            TableStyleType type = TableStyleType.valueOf(element.getType().toString());
            DifferentialStyleProvider dstyle = null;
            if (element.isSetDxfId()) {
                int idx = (int) element.getDxfId();
                CTDxf dxf;
                if (idx >= 0 && idx < dxfs.getCount()) {
                    dxf = dxfs.getDxfArray(idx);
                } else {
                    dxf = null;
                }
                int stripeSize = 0;
                if (element.isSetSize()) stripeSize = (int) element.getSize();
                if (dxf != null) dstyle = new XSSFDxfStyleProvider(dxf, stripeSize, colorMap);
            }
            elementMap.put(type, dstyle);
        }
    }
    
    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
    
    /**
     * Always false for these, these are user defined styles
     */
    public boolean isBuiltin() {
        return false;
    }
    
    public DifferentialStyleProvider getStyle(TableStyleType type) {
        return elementMap.get(type);
    }
    
}