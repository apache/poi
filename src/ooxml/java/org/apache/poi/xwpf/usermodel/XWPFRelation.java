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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author Yegor Kozlov
 */
public final class XWPFRelation extends POIXMLRelation {

    /**
     * A map to lookup POIXMLRelation by its relation type
     */
    private static final Map<String, XWPFRelation> _table = new HashMap<>();

    public static final XWPFRelation DOCUMENT = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml",
            PackageRelationshipTypes.CORE_DOCUMENT,
            "/word/document.xml",
            null
    );
    public static final XWPFRelation TEMPLATE = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.template.main+xml",
            PackageRelationshipTypes.CORE_DOCUMENT,
            "/word/document.xml",
            null
    );
    public static final XWPFRelation MACRO_DOCUMENT = new XWPFRelation(
            "application/vnd.ms-word.document.macroEnabled.main+xml",
            PackageRelationshipTypes.CORE_DOCUMENT,
            "/word/document.xml",
            null
    );
    public static final XWPFRelation MACRO_TEMPLATE_DOCUMENT = new XWPFRelation(
            "application/vnd.ms-word.template.macroEnabledTemplate.main+xml",
            PackageRelationshipTypes.CORE_DOCUMENT,
            "/word/document.xml",
            null
    );
    public static final XWPFRelation GLOSSARY_DOCUMENT = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document.glossary+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/glossaryDocument",
            "/word/glossary/document.xml",
            null
    );
    public static final XWPFRelation NUMBERING = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering",
            "/word/numbering.xml",
            XWPFNumbering.class
    );
    public static final XWPFRelation FONT_TABLE = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/fontTable",
            "/word/fontTable.xml",
            null
    );
    public static final XWPFRelation SETTINGS = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/settings",
            "/word/settings.xml",
            XWPFSettings.class
    );
    public static final XWPFRelation STYLES = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles",
            "/word/styles.xml",
            XWPFStyles.class
    );
    public static final XWPFRelation WEB_SETTINGS = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/webSettings",
            "/word/webSettings.xml",
            null
    );
    public static final XWPFRelation HEADER = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/header",
            "/word/header#.xml",
            XWPFHeader.class
    );
    public static final XWPFRelation FOOTER = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer",
            "/word/footer#.xml",
            XWPFFooter.class
    );
    public static final XWPFRelation THEME = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.theme+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme",
            "/word/theme/theme#.xml",
            null
    );

    public static final XWPFRelation WORKBOOK = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            POIXMLDocument.PACK_OBJECT_REL_TYPE,
            "/word/embeddings/Microsoft_Excel_Worksheet#.xlsx",
            XSSFWorkbook.class
    );

    public static final XWPFRelation CHART = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.drawingml.chart+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart",
            "/word/charts/chart#.xml",
            XWPFChart.class
    );
    public static final XWPFRelation HYPERLINK = new XWPFRelation(
            null,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
            null,
            null
    );
    public static final XWPFRelation COMMENT = new XWPFRelation(
            null,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments",
            null,
            null
    );
    public static final XWPFRelation FOOTNOTE = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.footnotes+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/footnotes",
            "/word/footnotes.xml",
            XWPFFootnotes.class
    );
    public static final XWPFRelation ENDNOTE = new XWPFRelation(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.endnotes+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/endnotes",
            "/word/endnotes.xml",
            XWPFEndnotes.class
    );
    /**
     * Supported image formats
     */
    public static final XWPFRelation IMAGE_EMF = new XWPFRelation(
            "image/x-emf",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.emf",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGE_WMF = new XWPFRelation(
            "image/x-wmf",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.wmf",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGE_PICT = new XWPFRelation(
            "image/pict",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.pict",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGE_JPEG = new XWPFRelation(
            "image/jpeg",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.jpeg",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGE_PNG = new XWPFRelation(
            "image/png",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.png",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGE_DIB = new XWPFRelation(
            "image/dib",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.dib",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGE_GIF = new XWPFRelation(
            "image/gif",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.gif",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGE_TIFF = new XWPFRelation(
            "image/tiff",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.tiff",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGE_EPS = new XWPFRelation(
            "image/x-eps",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.eps",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGE_BMP = new XWPFRelation(
            "image/x-ms-bmp",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.bmp",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGE_WPG = new XWPFRelation(
            "image/x-wpg",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/word/media/image#.wpg",
            XWPFPictureData.class
    );
    public static final XWPFRelation IMAGES = new XWPFRelation(
            null,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            null,
            XWPFPictureData.class
    );

    private XWPFRelation(String type, String rel, String defaultName, Class<? extends POIXMLDocumentPart> cls) {
        super(type, rel, defaultName, cls);
        _table.put(rel, this);
    }

    /**
     * Get POIXMLRelation by relation type
     *
     * @param rel relation type, for example,
     *            <code>http://schemas.openxmlformats.org/officeDocument/2006/relationships/image</code>
     * @return registered POIXMLRelation or null if not found
     */
    public static XWPFRelation getInstance(String rel) {
        return _table.get(rel);
    }

}
