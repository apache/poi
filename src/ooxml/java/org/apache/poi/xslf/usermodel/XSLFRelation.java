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
package org.apache.poi.xslf.usermodel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@SuppressWarnings({"unused", "WeakerAccess"})
@Beta
public final class XSLFRelation extends POIXMLRelation {
    /* package */ static final String NS_DRAWINGML = "http://schemas.openxmlformats.org/drawingml/2006/main";

    /**
     * A map to lookup POIXMLRelation by its relation type
     */
    private static final Map<String, XSLFRelation> _table = new HashMap<>();

    public static final XSLFRelation MAIN = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml",
            null, null, null
    );

    public static final XSLFRelation MACRO = new XSLFRelation(
            "application/vnd.ms-powerpoint.slideshow.macroEnabled.main+xml",
            null, null, null
    );

    public static final XSLFRelation MACRO_TEMPLATE = new XSLFRelation(
            "application/vnd.ms-powerpoint.template.macroEnabled.main+xml",
            null, null, null
    );

    public static final XSLFRelation PRESENTATIONML = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.slideshow.main+xml",
            null, null, null
    );

    public static final XSLFRelation PRESENTATIONML_TEMPLATE = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.template.main+xml",
            null, null, null
    );

    public static final XSLFRelation PRESENTATION_MACRO = new XSLFRelation(
            "application/vnd.ms-powerpoint.presentation.macroEnabled.main+xml",
            null, null, null
    );

    public static final XSLFRelation THEME_MANAGER = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.themeManager+xml",
            null, null, null
    );

    public static final XSLFRelation NOTES = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesSlide",
            "/ppt/notesSlides/notesSlide#.xml",
            XSLFNotes.class
    );

    public static final XSLFRelation SLIDE = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.slide+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide",
            "/ppt/slides/slide#.xml",
            XSLFSlide.class
    );

    public static final XSLFRelation SLIDE_LAYOUT = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout",
            "/ppt/slideLayouts/slideLayout#.xml",
            XSLFSlideLayout.class
    );

    public static final XSLFRelation SLIDE_MASTER = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster",
            "/ppt/slideMasters/slideMaster#.xml",
            XSLFSlideMaster.class
    );

    public static final XSLFRelation NOTES_MASTER = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.notesMaster+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesMaster",
            "/ppt/notesMasters/notesMaster#.xml",
            XSLFNotesMaster.class
    );

    public static final XSLFRelation COMMENTS = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.comments+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments",
            "/ppt/comments/comment#.xml",
            XSLFComments.class
    );

    public static final XSLFRelation COMMENT_AUTHORS = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.commentAuthors+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/commentAuthors",
            "/ppt/commentAuthors.xml",
            XSLFCommentAuthors.class
    );

    public static final XSLFRelation HYPERLINK = new XSLFRelation(
            null,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
            null,
            null
    );

    public static final XSLFRelation THEME = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.theme+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme",
            "/ppt/theme/theme#.xml",
            XSLFTheme.class
    );

    public static final XSLFRelation VML_DRAWING = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.vmlDrawing",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/vmlDrawing",
            "/ppt/drawings/vmlDrawing#.vml",
            null
    );

    // this is not the same as in XSSFRelation.WORKBOOK, as it is usually used by embedded charts
    // referencing the original embedded excel workbook
    public static final XSLFRelation WORKBOOK = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            POIXMLDocument.PACK_OBJECT_REL_TYPE,
            "/ppt/embeddings/Microsoft_Excel_Worksheet#.xlsx",
            XSSFWorkbook.class
    );

    public static final XSLFRelation CHART = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.drawingml.chart+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart",
            "/ppt/charts/chart#.xml",
            XSLFChart.class
    );

    public static final XSLFRelation IMAGE_EMF = new XSLFRelation(
            PictureType.EMF.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.emf",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_WMF = new XSLFRelation(
            PictureType.WMF.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.wmf",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_PICT = new XSLFRelation(
            PictureType.PICT.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.pict",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_JPEG = new XSLFRelation(
            PictureType.JPEG.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.jpeg",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_PNG = new XSLFRelation(
            PictureType.PNG.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.png",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_DIB = new XSLFRelation(
            PictureType.DIB.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.dib",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_GIF = new XSLFRelation(
            PictureType.GIF.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.gif",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_TIFF = new XSLFRelation(
            PictureType.TIFF.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.tiff",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_EPS = new XSLFRelation(
            PictureType.EPS.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.eps",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_BMP = new XSLFRelation(
            PictureType.BMP.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.bmp",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_WPG = new XSLFRelation(
            PictureType.WPG.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.wpg",
            XSLFPictureData.class
    );
    public static final XSLFRelation IMAGE_WDP = new XSLFRelation(
            PictureType.WDP.contentType,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            "/ppt/media/image#.wdp",
            XSLFPictureData.class
    );

    public static final XSLFRelation IMAGES = new XSLFRelation(
            null,
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
            null,
            XSLFPictureData.class
    );

    public static final XSLFRelation TABLE_STYLES = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/tableStyles",
            "/ppt/tableStyles.xml",
            XSLFTableStyles.class
    );

    public static final XSLFRelation OLE_OBJECT = new XSLFRelation(
            "application/vnd.openxmlformats-officedocument.oleObject",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/oleObject",
            "/ppt/embeddings/oleObject#.bin",
            XSLFObjectData.class
    );


    private XSLFRelation(String type, String rel, String defaultName, Class<? extends POIXMLDocumentPart> cls) {
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
    public static XSLFRelation getInstance(String rel) {
        return _table.get(rel);
    }
}
