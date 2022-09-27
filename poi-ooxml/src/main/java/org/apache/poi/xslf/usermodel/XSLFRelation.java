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

import static org.apache.poi.openxml4j.opc.PackageRelationshipTypes.HDPHOTO_PART;
import static org.apache.poi.openxml4j.opc.PackageRelationshipTypes.IMAGE_PART;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@SuppressWarnings({"unused", "WeakerAccess"})
@Beta
public final class XSLFRelation extends POIXMLRelation {
    /* package */ static final String NS_DRAWINGML = XSSFRelation.NS_DRAWINGML;

    /**
     * A map to lookup POIXMLRelation by its relation type
     */
    private static final Map<String, XSLFRelation> _table = new HashMap<>();

    public static final XSLFRelation MAIN = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"
    );

    public static final XSLFRelation MACRO = new XSLFRelation(
        "application/vnd.ms-powerpoint.slideshow.macroEnabled.main+xml"
    );

    public static final XSLFRelation MACRO_TEMPLATE = new XSLFRelation(
        "application/vnd.ms-powerpoint.template.macroEnabled.main+xml"
    );

    public static final XSLFRelation PRESENTATIONML = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.slideshow.main+xml"
    );

    public static final XSLFRelation PRESENTATIONML_TEMPLATE = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.template.main+xml"
    );

    public static final XSLFRelation PRESENTATION_MACRO = new XSLFRelation(
        "application/vnd.ms-powerpoint.presentation.macroEnabled.main+xml"
    );

    public static final XSLFRelation THEME_MANAGER = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.themeManager+xml"
    );

    public static final XSLFRelation NOTES = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesSlide",
        "/ppt/notesSlides/notesSlide#.xml",
        XSLFNotes::new, XSLFNotes::new
    );

    public static final XSLFRelation SLIDE = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.slide+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide",
        "/ppt/slides/slide#.xml",
        XSLFSlide::new, XSLFSlide::new
    );

    public static final XSLFRelation SLIDE_LAYOUT = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout",
        "/ppt/slideLayouts/slideLayout#.xml",
        null, XSLFSlideLayout::new
    );

    public static final XSLFRelation SLIDE_MASTER = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster",
        "/ppt/slideMasters/slideMaster#.xml",
        null, XSLFSlideMaster::new
    );

    public static final XSLFRelation NOTES_MASTER = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.notesMaster+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesMaster",
        "/ppt/notesMasters/notesMaster#.xml",
        XSLFNotesMaster::new, XSLFNotesMaster::new
    );

    public static final XSLFRelation COMMENTS = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.comments+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments",
        "/ppt/comments/comment#.xml",
        XSLFComments::new, XSLFComments::new
    );

    public static final XSLFRelation COMMENT_AUTHORS = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.commentAuthors+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/commentAuthors",
        "/ppt/commentAuthors.xml",
        XSLFCommentAuthors::new, XSLFCommentAuthors::new
    );

    public static final XSLFRelation HYPERLINK = new XSLFRelation(
        null,
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink",
        null
    );

    public static final XSLFRelation THEME = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.theme+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme",
        "/ppt/theme/theme#.xml",
        XSLFTheme::new, XSLFTheme::new
    );

    public static final XSLFRelation VML_DRAWING = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.vmlDrawing",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/vmlDrawing",
        "/ppt/drawings/vmlDrawing#.vml"
    );

    // this is not the same as in XSSFRelation.WORKBOOK, as it is usually used by embedded charts
    // referencing the original embedded excel workbook
    public static final XSLFRelation WORKBOOK = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        POIXMLDocument.PACK_OBJECT_REL_TYPE,
        "/ppt/embeddings/Microsoft_Excel_Worksheet#.xlsx",
        XSSFWorkbook::new, XSSFWorkbook::new
    );

    public static final XSLFRelation CHART = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.drawingml.chart+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart",
        "/ppt/charts/chart#.xml",
        XSLFChart::new, XSLFChart::new
    );

    public static final XSLFRelation DIAGRAM_DRAWING = new XSLFRelation(
            "application/vnd.ms-office.drawingml.diagramDrawing+xml",
            "http://schemas.microsoft.com/office/2007/relationships/diagramDrawing",
            "/ppt/diagrams/drawing#.xml",
            XSLFDiagramDrawing::new, XSLFDiagramDrawing::new
    );

    public static final XSLFRelation IMAGE_EMF = new XSLFRelation(
        PictureType.EMF.contentType,
        IMAGE_PART,
        "/ppt/media/image#.emf",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_WMF = new XSLFRelation(
        PictureType.WMF.contentType,
        IMAGE_PART,
        "/ppt/media/image#.wmf",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_PICT = new XSLFRelation(
        PictureType.PICT.contentType,
        IMAGE_PART,
        "/ppt/media/image#.pict",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_JPEG = new XSLFRelation(
        PictureType.JPEG.contentType,
        IMAGE_PART,
        "/ppt/media/image#.jpeg",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_PNG = new XSLFRelation(
        PictureType.PNG.contentType,
        IMAGE_PART,
        "/ppt/media/image#.png",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_DIB = new XSLFRelation(
        PictureType.DIB.contentType,
        IMAGE_PART,
        "/ppt/media/image#.dib",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_GIF = new XSLFRelation(
        PictureType.GIF.contentType,
        IMAGE_PART,
        "/ppt/media/image#.gif",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_TIFF = new XSLFRelation(
        PictureType.TIFF.contentType,
        IMAGE_PART,
        "/ppt/media/image#.tiff",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_EPS = new XSLFRelation(
        PictureType.EPS.contentType,
        IMAGE_PART,
        "/ppt/media/image#.eps",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_BMP = new XSLFRelation(
        PictureType.BMP.contentType,
        IMAGE_PART,
        "/ppt/media/image#.bmp",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_WPG = new XSLFRelation(
        PictureType.WPG.contentType,
        IMAGE_PART,
        "/ppt/media/image#.wpg",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_WDP = new XSLFRelation(
        PictureType.WDP.contentType,
        IMAGE_PART,
        "/ppt/media/image#.wdp",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation HDPHOTO_WDP = new XSLFRelation(
        PictureType.WDP.contentType,
        HDPHOTO_PART,
        "/ppt/media/hdphoto#.wdp",
        XSLFPictureData::new, XSLFPictureData::new
    );
    public static final XSLFRelation IMAGE_SVG = new XSLFRelation(
        PictureType.SVG.contentType,
        IMAGE_PART,
        "/ppt/media/image#.svg",
        XSLFPictureData::new, XSLFPictureData::new
    );

    public static final XSLFRelation IMAGES = new XSLFRelation(
        null,
        IMAGE_PART,
        null,
        XSLFPictureData::new, XSLFPictureData::new
    );

    public static final XSLFRelation TABLE_STYLES = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/tableStyles",
        "/ppt/tableStyles.xml",
        XSLFTableStyles::new, XSLFTableStyles::new
    );

    public static final XSLFRelation OLE_OBJECT = new XSLFRelation(
        "application/vnd.openxmlformats-officedocument.oleObject",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/oleObject",
        "/ppt/embeddings/oleObject#.bin",
        XSLFObjectData::new, XSLFObjectData::new
    );

    public static final XSLFRelation FONT = new XSLFRelation(
        "application/x-fontdata",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/font",
        "/ppt/fonts/font#.fntdata",
        XSLFFontData::new, XSLFFontData::new
    );


    private XSLFRelation(String type) {
        this(type, null, null, null, null);
    }

    private XSLFRelation(String type, String rel, String defaultName) {
        this(type, rel, defaultName, null, null);
    }

    private XSLFRelation(String type, String rel, String defaultName,
                         NoArgConstructor noArgConstructor,
                         PackagePartConstructor packagePartConstructor) {
        super(type, rel, defaultName, noArgConstructor, packagePartConstructor, null);
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
