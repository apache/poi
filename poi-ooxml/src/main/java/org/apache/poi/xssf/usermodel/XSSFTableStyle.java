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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.DifferentialStyleProvider;
import org.apache.poi.ss.usermodel.TableStyle;
import org.apache.poi.ss.usermodel.TableStyleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxfs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleElement;

/**
 * {@link TableStyle} implementation for styles defined in the OOXML styles.xml.
 * Also used for built-in styles via dummy XML generated from presetTableStyles.xml.
 */
public class XSSFTableStyle implements TableStyle {
    private static final Logger LOG = LogManager.getLogger(XSSFTableStyle.class);

    private final String name;
    private final int index;
    private final Map<TableStyleType, DifferentialStyleProvider> elementMap = new EnumMap<>(TableStyleType.class);

    /**
     * @param index style definition index or built-in ordinal depending on use
     * @param colorMap indexed color map - default or custom
     * @see TableStyle#getIndex()
     */
    public XSSFTableStyle(int index, CTDxfs dxfs, CTTableStyle tableStyle, IndexedColorMap colorMap) {
        this.name = tableStyle.getName();
        this.index = index;

        List<CTDxf> dxfList = new ArrayList<>();

        // CT* classes don't handle "mc:AlternateContent" elements, so get the Dxf instances manually
        try (XmlCursor cur = dxfs.newCursor()) {
            // sometimes there are namespaces sometimes not.
            String xquery = "declare namespace x='"+XSSFRelation.NS_SPREADSHEETML+"' .//x:dxf | .//dxf";
            cur.selectPath(xquery);
            while (cur.toNextSelection()) {
                XmlObject obj = cur.getObject();
                String parentName = obj.getDomNode().getParentNode().getNodeName();
                // ignore alternate content choices, we won't know anything about their namespaces
                if (parentName.equals("mc:Fallback") || parentName.equals("x:dxfs") || parentName.contentEquals("dxfs")) {
                    CTDxf dxf;
                    try {
                        if (obj instanceof CTDxf) {
                            dxf = (CTDxf) obj;
                        } else {
                            dxf = CTDxf.Factory.parse(obj.newXMLStreamReader(), new XmlOptions().setDocumentType(CTDxf.type));
                        }
                        if (dxf != null) dxfList.add(dxf);
                    } catch (XmlException e) {
                        LOG.atWarn().withThrowable(e).log("Error parsing XSSFTableStyle");
                    }
                }
            }
        }

        for (CTTableStyleElement element : tableStyle.getTableStyleElementList()) {
            TableStyleType type = TableStyleType.valueOf(element.getType().toString());
            DifferentialStyleProvider dstyle = null;
            if (element.isSetDxfId()) {
                int idx = (int) element.getDxfId();
                CTDxf dxf;
                dxf = dxfList.get(idx);
                int stripeSize = 0;
                if (element.isSetSize()) stripeSize = (int) element.getSize();
                if (dxf != null) dstyle = new XSSFDxfStyleProvider(dxf, stripeSize, colorMap);
            }
            elementMap.put(type, dstyle);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getIndex() {
        return index;
    }

    /**
     * Always false for these, these are user defined styles
     */
    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public DifferentialStyleProvider getStyle(TableStyleType type) {
        return elementMap.get(type);
    }

}