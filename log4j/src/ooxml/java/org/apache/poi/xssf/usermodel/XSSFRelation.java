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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.xssf.model.CalculationChain;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.ExternalLinksTable;
import org.apache.poi.xssf.model.MapInfo;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.SingleXmlCells;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.ThemesTable;

/**
 * Defines namespaces, content types and normal file names / naming
 *  patterns, for the well-known XSSF format parts.
 */
public final class XSSFRelation extends POIXMLRelation {

    /**
     * A map to lookup POIXMLRelation by its relation type
     */
    private static final Map<String, XSSFRelation> _table = new HashMap<>();


    public static final XSSFRelation WORKBOOK = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/workbook",
        "/xl/workbook.xml"
    );

    public static final XSSFRelation MACROS_WORKBOOK = new XSSFRelation(
        "application/vnd.ms-excel.sheet.macroEnabled.main+xml",
        PackageRelationshipTypes.CORE_DOCUMENT,
        "/xl/workbook.xml"
    );

    public static final XSSFRelation TEMPLATE_WORKBOOK = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.template.main+xml",
        PackageRelationshipTypes.CORE_DOCUMENT,
        "/xl/workbook.xml"
    );

    public static final XSSFRelation MACRO_TEMPLATE_WORKBOOK = new XSSFRelation(
        "application/vnd.ms-excel.template.macroEnabled.main+xml",
        PackageRelationshipTypes.CORE_DOCUMENT,
        "/xl/workbook.xml"
    );

    public static final XSSFRelation MACRO_ADDIN_WORKBOOK = new XSSFRelation(
        "application/vnd.ms-excel.addin.macroEnabled.main+xml",
        PackageRelationshipTypes.CORE_DOCUMENT,
        "/xl/workbook.xml"
    );

    public static final XSSFRelation XLSB_BINARY_WORKBOOK = new XSSFRelation(
        "application/vnd.ms-excel.sheet.binary.macroEnabled.main",
        PackageRelationshipTypes.CORE_DOCUMENT,
        "/xl/workbook.bin"
    );

    public static final XSSFRelation WORKSHEET = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet",
        "/xl/worksheets/sheet#.xml",
        XSSFSheet::new, XSSFSheet::new
    );

    public static final XSSFRelation CHARTSHEET = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.chartsheet+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/chartsheet",
        "/xl/chartsheets/sheet#.xml",
        null, XSSFChartSheet::new
    );

    public static final XSSFRelation SHARED_STRINGS = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings",
        "/xl/sharedStrings.xml",
        SharedStringsTable::new, SharedStringsTable::new
    );

    public static final XSSFRelation STYLES = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml",
        PackageRelationshipTypes.STYLE_PART,
        "/xl/styles.xml",
        StylesTable::new, StylesTable::new
    );

    public static final XSSFRelation DRAWINGS = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.drawing+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing",
        "/xl/drawings/drawing#.xml",
        XSSFDrawing::new, XSSFDrawing::new
    );

    public static final XSSFRelation VML_DRAWINGS = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.vmlDrawing",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/vmlDrawing",
        "/xl/drawings/vmlDrawing#.vml",
        XSSFVMLDrawing::new, XSSFVMLDrawing::new
    );

    public static final XSSFRelation CHART = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.drawingml.chart+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/chart",
        "/xl/charts/chart#.xml",
        XSSFChart::new, XSSFChart::new
    );

    public static final XSSFRelation CUSTOM_XML_MAPPINGS = new XSSFRelation(
        "application/xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/xmlMaps",
        "/xl/xmlMaps.xml",
        MapInfo::new, MapInfo::new
    );

    public static final XSSFRelation SINGLE_XML_CELLS = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.tableSingleCells+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/tableSingleCells",
        "/xl/tables/tableSingleCells#.xml",
        SingleXmlCells::new, SingleXmlCells::new
    );

    public static final XSSFRelation TABLE = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.table+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/table",
        "/xl/tables/table#.xml",
        XSSFTable::new, XSSFTable::new
    );

    public static final XSSFRelation IMAGES = new XSSFRelation(
        null,
        PackageRelationshipTypes.IMAGE_PART,
        null,
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_EMF = new XSSFRelation(
        "image/x-emf",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.emf",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_WMF = new XSSFRelation(
        "image/x-wmf",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.wmf",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_PICT = new XSSFRelation(
        "image/pict",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.pict",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_JPEG = new XSSFRelation(
        "image/jpeg",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.jpeg",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_PNG = new XSSFRelation(
        "image/png",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.png",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_DIB = new XSSFRelation(
        "image/dib",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.dib",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_GIF = new XSSFRelation(
        "image/gif",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.gif",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_TIFF = new XSSFRelation(
        "image/tiff",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.tiff",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_EPS = new XSSFRelation(
        "image/x-eps",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.eps",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_BMP = new XSSFRelation(
        "image/x-ms-bmp",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.bmp",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation IMAGE_WPG = new XSSFRelation(
        "image/x-wpg",
        PackageRelationshipTypes.IMAGE_PART,
        "/xl/media/image#.wpg",
        XSSFPictureData::new, XSSFPictureData::new
    );

    public static final XSSFRelation SHEET_COMMENTS = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.comments+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/comments",
        "/xl/comments#.xml",
        CommentsTable::new, CommentsTable::new
    );

    public static final XSSFRelation SHEET_HYPERLINKS = new XSSFRelation(
        null,
        PackageRelationshipTypes.HYPERLINK_PART,
        null
    );

    public static final XSSFRelation OLEEMBEDDINGS = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.oleObject",
        POIXMLDocument.OLE_OBJECT_REL_TYPE,
        "/xl/embeddings/oleObject#.bin"
    );

    public static final XSSFRelation PACKEMBEDDINGS = new XSSFRelation(
        null,
        POIXMLDocument.PACK_OBJECT_REL_TYPE,
        null
    );

    public static final XSSFRelation VBA_MACROS = new XSSFRelation(
        "application/vnd.ms-office.vbaProject",
        "http://schemas.microsoft.com/office/2006/relationships/vbaProject",
        "/xl/vbaProject.bin",
        XSSFVBAPart::new, XSSFVBAPart::new
    );

    public static final XSSFRelation ACTIVEX_CONTROLS = new XSSFRelation(
        "application/vnd.ms-office.activeX+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/control",
        "/xl/activeX/activeX#.xml"
    );

    public static final XSSFRelation ACTIVEX_BINS = new XSSFRelation(
        "application/vnd.ms-office.activeX",
        "http://schemas.microsoft.com/office/2006/relationships/activeXControlBinary",
        "/xl/activeX/activeX#.bin"
    );

    public static final XSSFRelation MACRO_SHEET_BIN = new XSSFRelation(
        null,//TODO: figure out what this should be?
        "http://schemas.microsoft.com/office/2006/relationships/xlMacrosheet",
        "/xl/macroSheets/sheet#.bin"
    );

    public static final XSSFRelation INTL_MACRO_SHEET_BIN = new XSSFRelation(
        null,//TODO: figure out what this should be?
        "http://schemas.microsoft.com/office/2006/relationships/xlIntlMacrosheet",
        "/xl/macroSheets/sheet#.bin"
    );

    public static final XSSFRelation DIALOG_SHEET_BIN = new XSSFRelation(
        null,//TODO: figure out what this should be?
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/dialogsheet",
        "/xl/dialogSheets/sheet#.bin"
    );

    public static final XSSFRelation THEME = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.theme+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme",
        "/xl/theme/theme#.xml",
        ThemesTable::new, ThemesTable::new
    );

    public static final XSSFRelation CALC_CHAIN = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.calcChain+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/calcChain",
        "/xl/calcChain.xml",
        CalculationChain::new, CalculationChain::new
    );

    public static final XSSFRelation EXTERNAL_LINKS = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.externalLink+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/externalLink",
        "/xl/externalLinks/externalLink#.xmll",
        ExternalLinksTable::new, ExternalLinksTable::new
    );

    public static final XSSFRelation PRINTER_SETTINGS = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.printerSettings",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/printerSettings",
        "/xl/printerSettings/printerSettings#.bin"
   );
    public static final XSSFRelation PIVOT_TABLE = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.pivotTable+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/pivotTable",
        "/xl/pivotTables/pivotTable#.xml",
        XSSFPivotTable::new, XSSFPivotTable::new
    );
    public static final XSSFRelation PIVOT_CACHE_DEFINITION = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.pivotCacheDefinition+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/pivotCacheDefinition",
        "/xl/pivotCache/pivotCacheDefinition#.xml",
        XSSFPivotCacheDefinition::new, XSSFPivotCacheDefinition::new
    );
    public static final XSSFRelation PIVOT_CACHE_RECORDS = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.pivotCacheRecords+xml",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/pivotCacheRecords",
        "/xl/pivotCache/pivotCacheRecords#.xml",
        XSSFPivotCacheRecords::new, XSSFPivotCacheRecords::new
    );

    public static final XSSFRelation CTRL_PROP_RECORDS = new XSSFRelation(
        null,
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/ctrlProp",
        "/xl/ctrlProps/ctrlProp#.xml"
    );

    public static final XSSFRelation CUSTOM_PROPERTIES = new XSSFRelation(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.customProperty",
        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/customProperty",
        "/xl/customProperty#.bin"
    );

    public static final String NS_SPREADSHEETML = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    public static final String NS_DRAWINGML = "http://schemas.openxmlformats.org/drawingml/2006/main";
    public static final String NS_CHART = "http://schemas.openxmlformats.org/drawingml/2006/chart";


    private XSSFRelation(String type, String rel, String defaultName) {
        this(type,rel, defaultName, null, null);
    }

    private XSSFRelation(String type, String rel, String defaultName,
                         NoArgConstructor noArgConstructor,
                         PackagePartConstructor packagePartConstructor) {
        super(type, rel, defaultName, noArgConstructor, packagePartConstructor, null);
        _table.put(rel, this);
    }

    /**
     * Get POIXMLRelation by relation type
     *
     * @param rel relation type, for example,
     *    <code>http://schemas.openxmlformats.org/officeDocument/2006/relationships/image</code>
     * @return registered POIXMLRelation or null if not found
     */
    public static XSSFRelation getInstance(String rel) {
        return _table.get(rel);
    }
}
