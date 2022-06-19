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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.util.ImageUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.model.Comments;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlipFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTAbsoluteAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTConnector;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTGroupShape;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.STEditAs;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTOleObject;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTOleObjects;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

/**
 * Represents a SpreadsheetML drawing
 */
public final class XSSFDrawing extends POIXMLDocumentPart implements Drawing<XSSFShape> {
    private static final Logger LOG = LogManager.getLogger(XSSFDrawing.class);

    /**
     * Root element of the SpreadsheetML Drawing part
     */
    private CTDrawing drawing;
    private long numOfGraphicFrames;

    protected static final String NAMESPACE_A = XSSFRelation.NS_DRAWINGML;
    protected static final String NAMESPACE_C = XSSFRelation.NS_CHART;

    /**
     * Create a new SpreadsheetML drawing
     *
     * @see XSSFSheet#createDrawingPatriarch()
     */
    protected XSSFDrawing() {
        super();
        drawing = newDrawing();
    }

    /**
     * Construct a SpreadsheetML drawing from a package part
     *
     * @param part
     *            the package part holding the drawing data, the content type
     *            must be
     *            <code>application/vnd.openxmlformats-officedocument.drawing+xml</code>
     *
     * @since POI 3.14-Beta1
     */
    public XSSFDrawing(PackagePart part) throws IOException, XmlException {
        super(part);
        XmlOptions options = new XmlOptions(DEFAULT_XML_OPTIONS);
        // Removing root element
        options.setLoadReplaceDocumentElement(null);
        try (InputStream is = part.getInputStream()) {
            drawing = CTDrawing.Factory.parse(is, options);
        }
    }

    /**
     * Construct a new CTDrawing bean. By default, it's just an empty
     * placeholder for drawing objects
     *
     * @return a new CTDrawing bean
     */
    private static CTDrawing newDrawing() {
        return CTDrawing.Factory.newInstance();
    }

    /**
     * Return the underlying CTDrawing bean, the root element of the
     * SpreadsheetML Drawing part.
     *
     * @return the underlying CTDrawing bean
     */
    @Internal
    public CTDrawing getCTDrawing() {
        return drawing;
    }

    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);

        /*
         * Saved drawings must have the following namespaces set: <xdr:wsDr
         * xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
         * xmlns:xdr=
         * "http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing">
         */
        xmlOptions
            .setSaveSyntheticDocumentElement(new QName(CTDrawing.type.getName().getNamespaceURI(), "wsDr", "xdr"));

        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            drawing.save(out, xmlOptions);
        }
    }

    @Override
    public XSSFClientAnchor createAnchor(int dx1, int dy1, int dx2, int dy2, int col1, int row1, int col2, int row2) {
        return new XSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1, col2, row2);
    }

    /**
     * Constructs a textbox under the drawing.
     *
     * @param anchor
     *            the client anchor describes how this group is attached to the
     *            sheet.
     * @return the newly created textbox.
     */
    public XSSFTextBox createTextbox(XSSFClientAnchor anchor) {
        long shapeId = newShapeId();
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTShape ctShape = ctAnchor.addNewSp();
        ctShape.set(XSSFSimpleShape.prototype());
        ctShape.getNvSpPr().getCNvPr().setId(shapeId);
        XSSFTextBox shape = new XSSFTextBox(this, ctShape);
        shape.anchor = anchor;
        return shape;

    }

    /**
     * Creates a picture.
     *
     * @param anchor
     *            the client anchor describes how this picture is attached to
     *            the sheet.
     * @param pictureIndex
     *            the index of the picture in the workbook collection of
     *            pictures,
     *            {@link XSSFWorkbook#getAllPictures()}
     *            .
     *
     * @return the newly created picture shape.
     */
    public XSSFPicture createPicture(XSSFClientAnchor anchor, int pictureIndex) {
        PackageRelationship rel = addPictureReference(pictureIndex);

        long shapeId = newShapeId();
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTPicture ctShape = ctAnchor.addNewPic();
        ctShape.set(XSSFPicture.prototype());

        ctShape.getNvPicPr().getCNvPr().setId(shapeId);

        XSSFPicture shape = new XSSFPicture(this, ctShape);
        shape.anchor = anchor;
        shape.setPictureReference(rel);
        ctShape.getSpPr().setXfrm(createXfrm(anchor));

        return shape;
    }

    @Override
    public XSSFPicture createPicture(ClientAnchor anchor, int pictureIndex) {
        return createPicture((XSSFClientAnchor) anchor, pictureIndex);
    }

    /**
     * Creates a chart.
     *
     * @param anchor
     *            the client anchor describes how this chart is attached to the
     *            sheet.
     * @return the newly created chart
     * @see XSSFDrawing#createChart(ClientAnchor)
     */
    public XSSFChart createChart(XSSFClientAnchor anchor) {
        RelationPart rp = createChartRelationPart();
        XSSFChart chart = rp.getDocumentPart();
        String chartRelId = rp.getRelationship().getId();

        XSSFGraphicFrame frame = createGraphicFrame(anchor);
        frame.setChart(chart, chartRelId);
        frame.getCTGraphicalObjectFrame().setXfrm(createXfrm(anchor));

        return chart;
    }

    protected RelationPart createChartRelationPart() {
        XSSFWorkbook wb = getSheet().getWorkbook();
        XSSFFactory factory = wb == null ? XSSFFactory.getInstance() : wb.getXssfFactory();
        OPCPackage pkg = getPackagePart().getPackage();
        int chartNumber = pkg.getPartsByContentType(XSSFRelation.CHART.getContentType())
            .size() + 1;

        // some broken files have incorrectly named package parts,
        // so we need to avoid duplicates here by checking and increasing
        // the part-number
        try {
            while (pkg.getPart(PackagingURIHelper.createPartName(XSSFRelation.CHART.getFileName(chartNumber))) != null) {
                chartNumber++;
            }
        } catch (InvalidFormatException e) {
            throw new IllegalStateException("Failed for " + chartNumber, e);
        }

        return createRelationship(XSSFRelation.CHART, factory, chartNumber, false);
    }

    /**
     * Creates a chart.
     *
     * @param anchor
     *            the client anchor describes how this chart is attached to the
     *            sheet.
     * @return the newly created chart
     */

    public XSSFChart createChart(ClientAnchor anchor) {
        return createChart((XSSFClientAnchor) anchor);
    }

    /**
     * Imports the chart from the <code>srcChart</code> into this drawing.
     *
     * @param srcChart
     *            the source chart to be cloned into this drawing.
     * @return the newly created chart.
     * @since 4.0.0
     */
    public XSSFChart importChart(XSSFChart srcChart) {
        CTTwoCellAnchor anchor = ((XSSFDrawing) srcChart.getParent()).getCTDrawing().getTwoCellAnchorArray(0);
        CTMarker from = (CTMarker) anchor.getFrom().copy();
        CTMarker to = (CTMarker) anchor.getTo().copy();
        XSSFClientAnchor destAnchor = new XSSFClientAnchor(from, to);
        destAnchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        XSSFChart destChart = createChart(destAnchor);
        destChart.getCTChartSpace().set(srcChart.getCTChartSpace().copy());
        return destChart;
    }

    /**
     * Add the indexed picture to this drawing relations
     *
     * @param pictureIndex the index of the picture in the workbook collection of pictures,
     *            {@link XSSFWorkbook#getAllPictures()}           .
     */
    protected PackageRelationship addPictureReference(int pictureIndex) {
        XSSFPictureData data = getSheet().getWorkbook().getAllPictures().get(pictureIndex);
        XSSFPictureData pic = new XSSFPictureData(data.getPackagePart());
        RelationPart rp = addRelation(null, XSSFRelation.IMAGES, pic);
        return rp.getRelationship();
    }

    /**
     * Creates a simple shape. This includes such shapes as lines, rectangles,
     * and ovals.
     *
     * @param anchor
     *            the client anchor describes how this group is attached to the
     *            sheet.
     * @return the newly created shape.
     */
    public XSSFSimpleShape createSimpleShape(XSSFClientAnchor anchor) {
        long shapeId = newShapeId();
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTShape ctShape = ctAnchor.addNewSp();
        ctShape.set(XSSFSimpleShape.prototype());
        ctShape.getNvSpPr().getCNvPr().setId(shapeId);
        ctShape.getSpPr().setXfrm(createXfrm(anchor));
        XSSFSimpleShape shape = new XSSFSimpleShape(this, ctShape);
        shape.anchor = anchor;
        return shape;
    }

    /**
     * Creates a simple shape. This includes such shapes as lines, rectangles,
     * and ovals.
     *
     * @param anchor
     *            the client anchor describes how this group is attached to the
     *            sheet.
     * @return the newly created shape.
     */
    public XSSFConnector createConnector(XSSFClientAnchor anchor) {
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTConnector ctShape = ctAnchor.addNewCxnSp();
        ctShape.set(XSSFConnector.prototype());

        XSSFConnector shape = new XSSFConnector(this, ctShape);
        shape.anchor = anchor;
        return shape;
    }

    /**
     * Creates a simple shape. This includes such shapes as lines, rectangles,
     * and ovals.
     *
     * @param anchor
     *            the client anchor describes how this group is attached to the
     *            sheet.
     * @return the newly created shape.
     */
    public XSSFShapeGroup createGroup(XSSFClientAnchor anchor) {
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTGroupShape ctGroup = ctAnchor.addNewGrpSp();
        ctGroup.set(XSSFShapeGroup.prototype());
        CTTransform2D xfrm = createXfrm(anchor);
        CTGroupTransform2D grpXfrm = ctGroup.getGrpSpPr().getXfrm();
        grpXfrm.setOff(xfrm.getOff());
        grpXfrm.setExt(xfrm.getExt());
        grpXfrm.setChExt(xfrm.getExt());

        XSSFShapeGroup shape = new XSSFShapeGroup(this, ctGroup);
        shape.anchor = anchor;
        return shape;
    }

    /**
     * Creates a comment.
     *
     * @param anchor
     *            the client anchor describes how this comment is attached to
     *            the sheet.
     * @return the newly created comment.
     */
    @Override
    public XSSFComment createCellComment(ClientAnchor anchor) {
        XSSFSheet sheet = getSheet();

        Comments comments = sheet.getCommentsTable(true);
        return comments.createNewComment(anchor);
    }

    /**
     * Creates a new graphic frame.
     *
     * @param anchor
     *            the client anchor describes how this frame is attached to the
     *            sheet
     * @return the newly created graphic frame
     */
    private XSSFGraphicFrame createGraphicFrame(XSSFClientAnchor anchor) {
        CTTwoCellAnchor ctAnchor = createTwoCellAnchor(anchor);
        CTGraphicalObjectFrame ctGraphicFrame = ctAnchor.addNewGraphicFrame();
        ctGraphicFrame.set(XSSFGraphicFrame.prototype());
        ctGraphicFrame.setXfrm(createXfrm(anchor));

        long frameId = numOfGraphicFrames++;
        XSSFGraphicFrame graphicFrame = new XSSFGraphicFrame(this, ctGraphicFrame);
        graphicFrame.setAnchor(anchor);
        graphicFrame.setId(frameId);
        graphicFrame.setName("Diagramm" + frameId);
        return graphicFrame;
    }

    @Override
    public XSSFObjectData createObjectData(ClientAnchor anchor, int storageId, int pictureIndex) {
        XSSFSheet sh = getSheet();
        PackagePart sheetPart = sh.getPackagePart();

        /*
         * The shape id of the ole object seems to be a legacy shape id.
         *
         * see 5.3.2.1 legacyDrawing (Legacy Drawing Object): Legacy Shape ID
         * that is unique throughout the entire document. Legacy shape IDs
         * should be assigned based on which portion of the document the drawing
         * resides on. The assignment of these ids is broken down into clusters
         * of 1024 values. The first cluster is 1-1024, the second 1025-2048 and
         * so on.
         *
         * Ole shapes seem to start with 1025 on the first sheet ... and not
         * sure, if the ids need to be reindexed when sheets are removed or more
         * than 1024 shapes are on a given sheet (see #51332 for a similar
         * issue)
         */
        XSSFSheet sheet = getSheet();
        XSSFWorkbook wb = sheet.getWorkbook();
        int sheetIndex = wb.getSheetIndex(sheet);
        long shapeId = (sheetIndex + 1L) * 1024 + newShapeId();

        // add reference to OLE part
        final XSSFRelation rel = XSSFRelation.OLEEMBEDDINGS;
        PackagePartName olePN;
        try {
            olePN = PackagingURIHelper.createPartName(rel.getFileName(storageId));
        } catch (InvalidFormatException e) {
            throw new POIXMLException(e);
        }
        PackageRelationship olePR = sheetPart.addRelationship(olePN, TargetMode.INTERNAL, rel.getRelation());

        // add reference to image part
        XSSFPictureData imgPD = sh.getWorkbook().getAllPictures().get(pictureIndex);
        PackagePartName imgPN = imgPD.getPackagePart().getPartName();
        PackageRelationship imgSheetPR = sheetPart.addRelationship(imgPN, TargetMode.INTERNAL,
            PackageRelationshipTypes.IMAGE_PART);
        PackageRelationship imgDrawPR = getPackagePart().addRelationship(imgPN, TargetMode.INTERNAL,
            PackageRelationshipTypes.IMAGE_PART);

        // add OLE part metadata to sheet
        CTWorksheet cwb = sh.getCTWorksheet();
        CTOleObjects oo = cwb.isSetOleObjects() ? cwb.getOleObjects() : cwb.addNewOleObjects();

        CTOleObject ole1 = oo.addNewOleObject();
        ole1.setProgId("Package");
        ole1.setShapeId(shapeId);
        ole1.setId(olePR.getId());

        CTTwoCellAnchor ctAnchor;
        try (XmlCursor cur1 = ole1.newCursor()) {
            cur1.toEndToken();
            cur1.beginElement("objectPr", XSSFRelation.NS_SPREADSHEETML);
            cur1.insertAttributeWithValue("id", PackageRelationshipTypes.CORE_PROPERTIES_ECMA376_NS, imgSheetPR.getId());
            cur1.insertAttributeWithValue("defaultSize", "0");
            cur1.beginElement("anchor", XSSFRelation.NS_SPREADSHEETML);
            cur1.insertAttributeWithValue("moveWithCells", "1");

            ctAnchor = createTwoCellAnchor((XSSFClientAnchor) anchor);

            try (XmlCursor cur2 = ctAnchor.newCursor()) {
                cur2.copyXmlContents(cur1);
            }

            cur1.toParent();
            cur1.toFirstChild();
            cur1.setName(new QName(XSSFRelation.NS_SPREADSHEETML, "from"));
            cur1.toNextSibling();
            cur1.setName(new QName(XSSFRelation.NS_SPREADSHEETML, "to"));
        }

        // add a new shape and link OLE & image part
        CTShape ctShape = ctAnchor.addNewSp();
        ctShape.set(XSSFObjectData.prototype());
        ctShape.getSpPr().setXfrm(createXfrm((XSSFClientAnchor) anchor));

        // workaround for not having the vmlDrawing filled
        CTBlipFillProperties blipFill = ctShape.getSpPr().addNewBlipFill();
        blipFill.addNewBlip().setEmbed(imgDrawPR.getId());
        blipFill.addNewStretch().addNewFillRect();

        CTNonVisualDrawingProps cNvPr = ctShape.getNvSpPr().getCNvPr();
        cNvPr.setId(shapeId);
        cNvPr.setName("Object " + shapeId);

        try (XmlCursor extCur = cNvPr.getExtLst().getExtArray(0).newCursor()) {
            extCur.toFirstChild();
            extCur.setAttributeText(new QName("spid"), "_x0000_s" + shapeId);
        }

        XSSFObjectData shape = new XSSFObjectData(this, ctShape);
        shape.anchor = (XSSFClientAnchor) anchor;

        return shape;
    }

    /**
     * Returns all charts in this drawing.
     */
    public List<XSSFChart> getCharts() {
        List<XSSFChart> charts = new ArrayList<>();
        for (POIXMLDocumentPart part : getRelations()) {
            if (part instanceof XSSFChart) {
                charts.add((XSSFChart) part);
            }
        }
        return charts;
    }

    /**
     * Create and initialize a CTTwoCellAnchor that anchors a shape against
     * top-left and bottom-right cells.
     *
     * @return a new CTTwoCellAnchor
     */
    private CTTwoCellAnchor createTwoCellAnchor(XSSFClientAnchor anchor) {
        CTTwoCellAnchor ctAnchor = drawing.addNewTwoCellAnchor();
        ctAnchor.setFrom(anchor.getFrom());
        ctAnchor.setTo(anchor.getTo());
        ctAnchor.addNewClientData();
        anchor.setTo(ctAnchor.getTo());
        anchor.setFrom(ctAnchor.getFrom());
        STEditAs.Enum editAs;
        switch (anchor.getAnchorType()) {
        case DONT_MOVE_AND_RESIZE:
            editAs = STEditAs.ABSOLUTE;
            break;
        case MOVE_AND_RESIZE:
            editAs = STEditAs.TWO_CELL;
            break;
        case MOVE_DONT_RESIZE:
            editAs = STEditAs.ONE_CELL;
            break;
        default:
            editAs = STEditAs.ONE_CELL;
        }
        ctAnchor.setEditAs(editAs);
        return ctAnchor;
    }

    private CTTransform2D createXfrm(XSSFClientAnchor anchor) {
        CTTransform2D xfrm = CTTransform2D.Factory.newInstance();
        CTPoint2D off = xfrm.addNewOff();
        off.setX(anchor.getDx1());
        off.setY(anchor.getDy1());
        XSSFSheet sheet = getSheet();
        double widthPx = 0;
        for (int col = anchor.getCol1(); col < anchor.getCol2(); col++) {
            widthPx += sheet.getColumnWidthInPixels(col);
        }
        double heightPx = 0;
        for (int row = anchor.getRow1(); row < anchor.getRow2(); row++) {
            heightPx += ImageUtils.getRowHeightInPixels(sheet, row);
        }
        long width = Units.pixelToEMU((int) widthPx);
        long height = Units.pixelToEMU((int) heightPx);
        CTPositiveSize2D ext = xfrm.addNewExt();
        ext.setCx(width - anchor.getDx1() + anchor.getDx2());
        ext.setCy(height - anchor.getDy1() + anchor.getDy2());

        // TODO: handle vflip/hflip
        return xfrm;
    }

    private long newShapeId() {
        return 1L + drawing.sizeOfAbsoluteAnchorArray() + drawing.sizeOfOneCellAnchorArray() + drawing
            .sizeOfTwoCellAnchorArray();
    }

    /**
     * @return list of shapes in this drawing
     */
    public List<XSSFShape> getShapes() {
        List<XSSFShape> lst = new ArrayList<>();
        try (XmlCursor cur = drawing.newCursor()) {
            if (cur.toFirstChild()) {
                addShapes(cur, lst);
            }
        }
        return lst;
    }

    /**
     * @return list of shapes in this shape group
     */
    public List<XSSFShape> getShapes(XSSFShapeGroup groupshape) {
        List<XSSFShape> lst = new ArrayList<>();
        try (XmlCursor cur = groupshape.getCTGroupShape().newCursor()) {
            addShapes(cur, lst);
        }
        return lst;
    }

    private void addShapes(XmlCursor cur, List<XSSFShape> lst) {
        do {
            cur.push();
            if (cur.toFirstChild()) {
                do {
                    XmlObject obj = cur.getObject();

                    XSSFShape shape;
                    if (obj instanceof CTMarker) {
                        // ignore anchor elements
                        continue;
                    } else if (obj instanceof CTPicture) {
                        shape = new XSSFPicture(this, (CTPicture) obj);
                    } else if (obj instanceof CTConnector) {
                        shape = new XSSFConnector(this, (CTConnector) obj);
                    } else if (obj instanceof CTShape) {
                        shape = hasOleLink(obj) ? new XSSFObjectData(this, (CTShape) obj)
                            : new XSSFSimpleShape(this, (CTShape) obj);
                    } else if (obj instanceof CTGraphicalObjectFrame) {
                        shape = new XSSFGraphicFrame(this, (CTGraphicalObjectFrame) obj);
                    } else if (obj instanceof CTGroupShape) {
                        shape = new XSSFShapeGroup(this, (CTGroupShape) obj);
                    } else if (obj instanceof XmlAnyTypeImpl) {
                        LOG.atWarn().log("trying to parse AlternateContent, this unlinks the returned Shapes from the underlying xml content, so those shapes can't be used to modify the drawing, i.e. modifications will be ignored!");

                        // XmlAnyTypeImpl is returned for AlternateContent
                        // parts, which might contain a CTDrawing
                        cur.push();
                        cur.toFirstChild();
                        try {
                            // need to parse AlternateContent again,
                            // otherwise the child elements aren't typed,
                            // but also XmlAnyTypes
                            CTDrawing alterWS = CTDrawing.Factory.parse(cur.newXMLStreamReader());
                            try (XmlCursor cur2 = alterWS.newCursor()) {
                                if (cur2.toFirstChild()) {
                                    addShapes(cur2, lst);
                                }
                            }
                        } catch (XmlException e) {
                            LOG.atWarn().withThrowable(e).log("unable to parse CTDrawing in alternate content.");
                        } finally {
                            cur.pop();
                        }
                        continue;
                    } else {
                        // ignore anything else
                        continue;
                    }

                    assert (shape != null);
                    shape.anchor = getAnchorFromParent(obj);
                    lst.add(shape);

                } while (cur.toNextSibling());
            }
            cur.pop();
        } while (cur.toNextSibling());
    }

    private boolean hasOleLink(XmlObject shape) {
        QName uriName = new QName(null, "uri");
        String xquery = "declare namespace a='" + XSSFRelation.NS_DRAWINGML + "' .//a:extLst/a:ext";
        try (XmlCursor cur = shape.newCursor()) {
            cur.selectPath(xquery);
            while (cur.toNextSelection()) {
                String uri = cur.getAttributeText(uriName);
                if ("{63B3BB69-23CF-44E3-9099-C40C66FF867C}".equals(uri)) {
                    return true;
                }
            }
        }
        return false;
    }

    private XSSFAnchor getAnchorFromParent(XmlObject obj) {
        XSSFAnchor anchor = null;

        XmlObject parentXbean = null;
        try (XmlCursor cursor = obj.newCursor()) {
            if (cursor.toParent()) {
                parentXbean = cursor.getObject();
            }
        }
        if (parentXbean != null) {
            if (parentXbean instanceof CTTwoCellAnchor) {
                CTTwoCellAnchor ct = (CTTwoCellAnchor) parentXbean;
                anchor = new XSSFClientAnchor(ct.getFrom(), ct.getTo());
            } else if (parentXbean instanceof CTOneCellAnchor) {
                CTOneCellAnchor ct = (CTOneCellAnchor) parentXbean;
                anchor = new XSSFClientAnchor(getSheet(), ct.getFrom(), ct.getExt());
            } else if (parentXbean instanceof CTAbsoluteAnchor) {
                CTAbsoluteAnchor ct = (CTAbsoluteAnchor) parentXbean;
                anchor = new XSSFClientAnchor(getSheet(), ct.getPos(), ct.getExt());
            }
        }
        return anchor;
    }

    @Override
    public Iterator<XSSFShape> iterator() {
        return getShapes().iterator();
    }

    /**
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<XSSFShape> spliterator() {
        return getShapes().spliterator();
    }

    /**
     * @return the sheet associated with the drawing
     */
    public XSSFSheet getSheet() {
        return (XSSFSheet) getParent();
    }

}
