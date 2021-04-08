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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.ss.usermodel.DifferentialStyleProvider;
import org.apache.poi.ss.usermodel.TableStyle;
import org.apache.poi.ss.usermodel.TableStyleType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.model.StylesTable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Table style names defined in the OOXML spec.
 * The actual styling is defined in presetTableStyles.xml
 */
public enum XSSFBuiltinTableStyle {
    /***/
    TableStyleDark1,
    /***/
    TableStyleDark2,
    /***/
    TableStyleDark3,
    /***/
    TableStyleDark4,
    /***/
    TableStyleDark5,
    /***/
    TableStyleDark6,
    /***/
    TableStyleDark7,
    /***/
    TableStyleDark8,
    /***/
    TableStyleDark9,
    /***/
    TableStyleDark10,
    /***/
    TableStyleDark11,
    /***/
    TableStyleLight1,
    /***/
    TableStyleLight2,
    /***/
    TableStyleLight3,
    /***/
    TableStyleLight4,
    /***/
    TableStyleLight5,
    /***/
    TableStyleLight6,
    /***/
    TableStyleLight7,
    /***/
    TableStyleLight8,
    /***/
    TableStyleLight9,
    /***/
    TableStyleLight10,
    /***/
    TableStyleLight11,
    /***/
    TableStyleLight12,
    /***/
    TableStyleLight13,
    /***/
    TableStyleLight14,
    /***/
    TableStyleLight15,
    /***/
    TableStyleLight16,
    /***/
    TableStyleLight17,
    /***/
    TableStyleLight18,
    /***/
    TableStyleLight19,
    /***/
    TableStyleLight20,
    /***/
    TableStyleLight21,
    /***/
    TableStyleMedium1,
    /***/
    TableStyleMedium2,
    /***/
    TableStyleMedium3,
    /***/
    TableStyleMedium4,
    /***/
    TableStyleMedium5,
    /***/
    TableStyleMedium6,
    /***/
    TableStyleMedium7,
    /***/
    TableStyleMedium8,
    /***/
    TableStyleMedium9,
    /***/
    TableStyleMedium10,
    /***/
    TableStyleMedium11,
    /***/
    TableStyleMedium12,
    /***/
    TableStyleMedium13,
    /***/
    TableStyleMedium14,
    /***/
    TableStyleMedium15,
    /***/
    TableStyleMedium16,
    /***/
    TableStyleMedium17,
    /***/
    TableStyleMedium18,
    /***/
    TableStyleMedium19,
    /***/
    TableStyleMedium20,
    /***/
    TableStyleMedium21,
    /***/
    TableStyleMedium22,
    /***/
    TableStyleMedium23,
    /***/
    TableStyleMedium24,
    /***/
    TableStyleMedium25,
    /***/
    TableStyleMedium26,
    /***/
    TableStyleMedium27,
    /***/
    TableStyleMedium28,
    /***/
    PivotStyleMedium1,
    /***/
    PivotStyleMedium2,
    /***/
    PivotStyleMedium3,
    /***/
    PivotStyleMedium4,
    /***/
    PivotStyleMedium5,
    /***/
    PivotStyleMedium6,
    /***/
    PivotStyleMedium7,
    /***/
    PivotStyleMedium8,
    /***/
    PivotStyleMedium9,
    /***/
    PivotStyleMedium10,
    /***/
    PivotStyleMedium11,
    /***/
    PivotStyleMedium12,
    /***/
    PivotStyleMedium13,
    /***/
    PivotStyleMedium14,
    /***/
    PivotStyleMedium15,
    /***/
    PivotStyleMedium16,
    /***/
    PivotStyleMedium17,
    /***/
    PivotStyleMedium18,
    /***/
    PivotStyleMedium19,
    /***/
    PivotStyleMedium20,
    /***/
    PivotStyleMedium21,
    /***/
    PivotStyleMedium22,
    /***/
    PivotStyleMedium23,
    /***/
    PivotStyleMedium24,
    /***/
    PivotStyleMedium25,
    /***/
    PivotStyleMedium26,
    /***/
    PivotStyleMedium27,
    /***/
    PivotStyleMedium28,
    /***/
    PivotStyleLight1,
    /***/
    PivotStyleLight2,
    /***/
    PivotStyleLight3,
    /***/
    PivotStyleLight4,
    /***/
    PivotStyleLight5,
    /***/
    PivotStyleLight6,
    /***/
    PivotStyleLight7,
    /***/
    PivotStyleLight8,
    /***/
    PivotStyleLight9,
    /***/
    PivotStyleLight10,
    /***/
    PivotStyleLight11,
    /***/
    PivotStyleLight12,
    /***/
    PivotStyleLight13,
    /***/
    PivotStyleLight14,
    /***/
    PivotStyleLight15,
    /***/
    PivotStyleLight16,
    /***/
    PivotStyleLight17,
    /***/
    PivotStyleLight18,
    /***/
    PivotStyleLight19,
    /***/
    PivotStyleLight20,
    /***/
    PivotStyleLight21,
    /***/
    PivotStyleLight22,
    /***/
    PivotStyleLight23,
    /***/
    PivotStyleLight24,
    /***/
    PivotStyleLight25,
    /***/
    PivotStyleLight26,
    /***/
    PivotStyleLight27,
    /***/
    PivotStyleLight28,
    /***/
    PivotStyleDark1,
    /***/
    PivotStyleDark2,
    /***/
    PivotStyleDark3,
    /***/
    PivotStyleDark4,
    /***/
    PivotStyleDark5,
    /***/
    PivotStyleDark6,
    /***/
    PivotStyleDark7,
    /***/
    PivotStyleDark8,
    /***/
    PivotStyleDark9,
    /***/
    PivotStyleDark10,
    /***/
    PivotStyleDark11,
    /***/
    PivotStyleDark12,
    /***/
    PivotStyleDark13,
    /***/
    PivotStyleDark14,
    /***/
    PivotStyleDark15,
    /***/
    PivotStyleDark16,
    /***/
    PivotStyleDark17,
    /***/
    PivotStyleDark18,
    /***/
    PivotStyleDark19,
    /***/
    PivotStyleDark20,
    /***/
    PivotStyleDark21,
    /***/
    PivotStyleDark22,
    /***/
    PivotStyleDark23,
    /***/
    PivotStyleDark24,
    /***/
    PivotStyleDark25,
    /***/
    PivotStyleDark26,
    /***/
    PivotStyleDark27,
    /***/
    PivotStyleDark28,
    ;

    /**
     * Interestingly, this is initialized after the enum instances, so using an {@link EnumMap} works.
     */
    private static final Map<XSSFBuiltinTableStyle, TableStyle> styleMap = new EnumMap<>(XSSFBuiltinTableStyle.class);

    private XSSFBuiltinTableStyle() {
    }

    /**
     * @return built-in {@link TableStyle} definition
     */
    public TableStyle getStyle() {
        init();
        return styleMap.get(this);
    }

    /**
     * NOTE: only checks by name, not definition.
     *
     * @param style
     * @return true if the style represents a built-in style, false if it is null or a custom style
     */
    public static boolean isBuiltinStyle(TableStyle style) {
        if (style == null) return false;
        try {
            XSSFBuiltinTableStyle.valueOf(style.getName());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Only init once - thus the synchronized.  Lazy, after creating instances,
     * and only when a style is actually needed, to avoid overhead for uses
     * that don't need the actual style definitions.
     * <p>
     * Public so clients can initialize the map on startup rather than lazily
     * during evaluation if desired.
     */
    public static synchronized void init() {
        if (!styleMap.isEmpty()) return;

        /*
         * initialize map.  Every built-in has this format:
         * <styleName>
         *   <dxfs>
         *     <dxf>...</dxf>
         *     ...
         *   </dxfs>
         *   <tableStyles count="1">
         *     <tableStyle>...</tableStyle>
         *   </tableStyles>
         * </styleName>
         */
        try {
            final InputStream is = XSSFBuiltinTableStyle.class.getResourceAsStream("presetTableStyles.xml");
            try {
                final Document doc = DocumentHelper.readDocument(is);

                final NodeList styleNodes = doc.getDocumentElement().getChildNodes();
                for (int i = 0; i < styleNodes.getLength(); i++) {
                    final Node node = styleNodes.item(i);
                    if (node.getNodeType() != Node.ELEMENT_NODE) continue; // only care about elements
                    final Element tag = (Element) node;
                    String styleName = tag.getTagName();
                    XSSFBuiltinTableStyle builtIn = XSSFBuiltinTableStyle.valueOf(styleName);

                    Node dxfsNode = tag.getElementsByTagName("dxfs").item(0);
                    Node tableStyleNode = tag.getElementsByTagName("tableStyles").item(0);

                    // hack because I can't figure out how to get XMLBeans to parse a sub-element in a standalone manner
                    // - build a fake styles.xml file with just this built-in
                    StylesTable styles = new StylesTable();
                    styles.readFrom(new ByteArrayInputStream(styleXML(dxfsNode, tableStyleNode).getBytes(StandardCharsets.UTF_8)));
                    styleMap.put(builtIn, new XSSFBuiltinTypeStyleStyle(builtIn, styles.getExplicitTableStyle(styleName)));
                }
            } finally {
                IOUtils.closeQuietly(is);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String styleXML(Node dxfsNode, Node tableStyleNode) throws IOException, TransformerException {
        // built-ins doc uses 1-based dxf indexing, Excel uses 0 based.
        // add a dummy node to adjust properly.
        dxfsNode.insertBefore(dxfsNode.getOwnerDocument().createElement("dxf"), dxfsNode.getFirstChild());

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
                .append("<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ")
                .append("xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" ")
                .append("xmlns:x14ac=\"http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac\" ")
                .append("xmlns:x16r2=\"http://schemas.microsoft.com/office/spreadsheetml/2015/02/main\" ")
                .append("mc:Ignorable=\"x14ac x16r2\">\n");
        sb.append(writeToString(dxfsNode));
        sb.append(writeToString(tableStyleNode));
        sb.append("</styleSheet>");
        return sb.toString();
    }

    private static String writeToString(Node node) throws IOException, TransformerException {
        try (StringWriter sw = new StringWriter()){
            Transformer transformer = XMLHelper.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        }
    }

    /**
     * implementation for built-in styles
     */
    protected static class XSSFBuiltinTypeStyleStyle implements TableStyle {

        private final XSSFBuiltinTableStyle builtIn;
        private final TableStyle style;

        /**
         * @param builtIn
         * @param style
         */
        protected XSSFBuiltinTypeStyleStyle(XSSFBuiltinTableStyle builtIn, TableStyle style) {
            this.builtIn = builtIn;
            this.style = style;
        }

        public String getName() {
            return style.getName();
        }

        public int getIndex() {
            return builtIn.ordinal();
        }

        public boolean isBuiltin() {
            return true;
        }

        public DifferentialStyleProvider getStyle(TableStyleType type) {
            return style.getStyle(type);
        }

    }
}
