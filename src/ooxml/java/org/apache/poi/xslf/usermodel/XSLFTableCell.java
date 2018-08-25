/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCap;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.apache.poi.xslf.usermodel.XSLFPropertiesDelegate.XSLFFillProperties;
import org.apache.poi.xslf.usermodel.XSLFTableStyle.TablePartStyle;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTFontReference;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineEndProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTable;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCellProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTablePartStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableStyleCellStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableStyleTextStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.STCompoundLine;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineCap;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndLength;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndType;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndWidth;
import org.openxmlformats.schemas.drawingml.x2006.main.STOnOffStyleType;
import org.openxmlformats.schemas.drawingml.x2006.main.STPenAlignment;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextVerticalType;

/**
 * Represents a cell of a table in a .pptx presentation
 */
public class XSLFTableCell extends XSLFTextShape implements TableCell<XSLFShape, XSLFTextParagraph> {
    private CTTableCellProperties _tcPr;
    private final XSLFTable table;
    private int row, col;

    /**
     * Volatile/temporary anchor - e.g. for rendering
     */
    private Rectangle2D anchor;

    /* package */ XSLFTableCell(CTTableCell cell, XSLFTable table) {
        super(cell, table.getSheet());
        this.table = table;
    }

    @Override
    protected CTTextBody getTextBody(boolean create) {
        CTTableCell cell = getCell();
        CTTextBody txBody = cell.getTxBody();
        if (txBody == null && create) {
            XDDFTextBody body = new XDDFTextBody(this);
            initTextBody(body);
            cell.setTxBody(body.getXmlObject());
            txBody = cell.getTxBody();
        }
        return txBody;
    }

    static CTTableCell prototype() {
        CTTableCell cell = CTTableCell.Factory.newInstance();
        CTTableCellProperties pr = cell.addNewTcPr();
        pr.addNewLnL().addNewNoFill();
        pr.addNewLnR().addNewNoFill();
        pr.addNewLnT().addNewNoFill();
        pr.addNewLnB().addNewNoFill();
        return cell;
    }

    @SuppressWarnings("WeakerAccess")
    protected CTTableCellProperties getCellProperties(boolean create) {
        if (_tcPr == null) {
            CTTableCell cell = getCell();
            _tcPr = cell.getTcPr();
            if (_tcPr == null && create) {
                _tcPr = cell.addNewTcPr();
            }
        }
        return _tcPr;
    }

    @Override
    public void setLeftInset(double margin) {
        CTTableCellProperties pr = getCellProperties(true);
        pr.setMarL(Units.toEMU(margin));
    }

    @Override
    public void setRightInset(double margin) {
        CTTableCellProperties pr = getCellProperties(true);
        pr.setMarR(Units.toEMU(margin));
    }

    @Override
    public void setTopInset(double margin) {
        CTTableCellProperties pr = getCellProperties(true);
        pr.setMarT(Units.toEMU(margin));
    }

    @Override
    public void setBottomInset(double margin) {
        CTTableCellProperties pr = getCellProperties(true);
        pr.setMarB(Units.toEMU(margin));
    }

    private CTLineProperties getCTLine(BorderEdge edge, boolean create) {
        if (edge == null) {
            throw new IllegalArgumentException("BorderEdge needs to be specified.");
        }

        CTTableCellProperties pr = getCellProperties(create);
        if (pr == null) {
            return null;
        }

        switch (edge) {
        case bottom:
            return (pr.isSetLnB()) ? pr.getLnB() : (create ? pr.addNewLnB() : null);
        case left:
            return (pr.isSetLnL()) ? pr.getLnL() : (create ? pr.addNewLnL() : null);
        case top:
            return (pr.isSetLnT()) ? pr.getLnT() : (create ? pr.addNewLnT() : null);
        case right:
            return (pr.isSetLnR()) ? pr.getLnR() : (create ? pr.addNewLnR() : null);
        default:
            return null;
        }
    }

    @Override
    public void removeBorder(BorderEdge edge) {
        CTTableCellProperties pr = getCellProperties(false);
        if (pr == null) {
            return;
        }
        switch (edge) {
        case bottom:
            if (pr.isSetLnB()) {
                pr.unsetLnB();
            }
            break;
        case left:
            if (pr.isSetLnL()) {
                pr.unsetLnL();
            }
            break;
        case top:
            if (pr.isSetLnT()) {
                pr.unsetLnT();
            }
            break;
        case right:
            if (pr.isSetLnR()) {
                pr.unsetLnB();
            }
            break;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public StrokeStyle getBorderStyle(final BorderEdge edge) {
        final Double width = getBorderWidth(edge);
        return (width == null) ? null : new StrokeStyle() {
            @Override
            public PaintStyle getPaint() {
                return DrawPaint.createSolidPaint(getBorderColor(edge));
            }

            @Override
            public LineCap getLineCap() {
                return getBorderCap(edge);
            }

            @Override
            public LineDash getLineDash() {
                return getBorderDash(edge);
            }

            @Override
            public LineCompound getLineCompound() {
                return getBorderCompound(edge);
            }

            @Override
            public double getLineWidth() {
                return width;
            }
        };
    }

    @Override
    public void setBorderStyle(BorderEdge edge, StrokeStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("StrokeStyle needs to be specified.");
        }

        LineCap cap = style.getLineCap();
        if (cap != null) {
            setBorderCap(edge, cap);
        }

        LineCompound compound = style.getLineCompound();
        if (compound != null) {
            setBorderCompound(edge, compound);
        }

        LineDash dash = style.getLineDash();
        if (dash != null) {
            setBorderDash(edge, dash);
        }

        double width = style.getLineWidth();
        setBorderWidth(edge, width);
    }

    @SuppressWarnings("WeakerAccess")
    public Double getBorderWidth(BorderEdge edge) {
        CTLineProperties ln = getCTLine(edge, false);
        return (ln == null || !ln.isSetW()) ? null : Units.toPoints(ln.getW());
    }

    @Override
    public void setBorderWidth(BorderEdge edge, double width) {
        final CTLineProperties ln = getCTLine(edge, true);
        if (ln == null) {
            return;
        }
        ln.setW(Units.toEMU(width));
    }

    private CTLineProperties setBorderDefaults(BorderEdge edge) {
        final CTLineProperties ln = getCTLine(edge, true);
        if (ln == null) {
            throw new IllegalStateException("CTLineProperties couldn't be initialized");
        }

        if (ln.isSetNoFill()) {
            ln.unsetNoFill();
        }

        if (!ln.isSetPrstDash()) {
            ln.addNewPrstDash().setVal(STPresetLineDashVal.SOLID);
        }
        if (!ln.isSetCmpd()) {
            ln.setCmpd(STCompoundLine.SNG);
        }
        if (!ln.isSetAlgn()) {
            ln.setAlgn(STPenAlignment.CTR);
        }
        if (!ln.isSetCap()) {
            ln.setCap(STLineCap.FLAT);
        }
        if (!ln.isSetRound()) {
            ln.addNewRound();
        }

        if (!ln.isSetHeadEnd()) {
            CTLineEndProperties hd = ln.addNewHeadEnd();
            hd.setType(STLineEndType.NONE);
            hd.setW(STLineEndWidth.MED);
            hd.setLen(STLineEndLength.MED);
        }

        if (!ln.isSetTailEnd()) {
            CTLineEndProperties tl = ln.addNewTailEnd();
            tl.setType(STLineEndType.NONE);
            tl.setW(STLineEndWidth.MED);
            tl.setLen(STLineEndLength.MED);
        }

        return ln;
    }

    @Override
    public void setBorderColor(BorderEdge edge, Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Colors need to be specified.");
        }

        CTLineProperties ln = setBorderDefaults(edge);
        CTSolidColorFillProperties fill = ln.addNewSolidFill();
        XSLFColor c = new XSLFColor(fill, getSheet().getTheme(), fill.getSchemeClr());
        c.setColor(color);
    }

    @SuppressWarnings("WeakerAccess")
    public Color getBorderColor(BorderEdge edge) {
        CTLineProperties ln = getCTLine(edge, false);
        if (ln == null || ln.isSetNoFill() || !ln.isSetSolidFill()) {
            return null;
        }

        CTSolidColorFillProperties fill = ln.getSolidFill();
        XSLFColor c = new XSLFColor(fill, getSheet().getTheme(), fill.getSchemeClr());
        return c.getColor();
    }

    @SuppressWarnings("WeakerAccess")
    public LineCompound getBorderCompound(BorderEdge edge) {
        CTLineProperties ln = getCTLine(edge, false);
        if (ln == null || ln.isSetNoFill() || !ln.isSetSolidFill() || !ln.isSetCmpd()) {
            return null;
        }

        return LineCompound.fromOoxmlId(ln.getCmpd().intValue());
    }

    @Override
    public void setBorderCompound(BorderEdge edge, LineCompound compound) {
        if (compound == null) {
            throw new IllegalArgumentException("LineCompound need to be specified.");
        }

        CTLineProperties ln = setBorderDefaults(edge);
        ln.setCmpd(STCompoundLine.Enum.forInt(compound.ooxmlId));
    }

    @SuppressWarnings("WeakerAccess")
    public LineDash getBorderDash(BorderEdge edge) {
        CTLineProperties ln = getCTLine(edge, false);
        if (ln == null || ln.isSetNoFill() || !ln.isSetSolidFill() || !ln.isSetPrstDash()) {
            return null;
        }

        return LineDash.fromOoxmlId(ln.getPrstDash().getVal().intValue());
    }

    @Override
    public void setBorderDash(BorderEdge edge, LineDash dash) {
        if (dash == null) {
            throw new IllegalArgumentException("LineDash need to be specified.");
        }

        CTLineProperties ln = setBorderDefaults(edge);
        if (!ln.isSetPrstDash()) {
            ln.addNewPrstDash();
        }
        ln.getPrstDash().setVal(STPresetLineDashVal.Enum.forInt(dash.ooxmlId));
    }

    @SuppressWarnings("WeakerAccess")
    public LineCap getBorderCap(BorderEdge edge) {
        CTLineProperties ln = getCTLine(edge, false);
        if (ln == null || ln.isSetNoFill() || !ln.isSetSolidFill() || !ln.isSetCap()) {
            return null;
        }

        return LineCap.fromOoxmlId(ln.getCap().intValue());
    }

    @SuppressWarnings("WeakerAccess")
    public void setBorderCap(BorderEdge edge, LineCap cap) {
        if (cap == null) {
            throw new IllegalArgumentException("LineCap need to be specified.");
        }

        CTLineProperties ln = setBorderDefaults(edge);
        ln.setCap(STLineCap.Enum.forInt(cap.ooxmlId));
    }

    /**
     * Specifies a solid color fill. The shape is filled entirely with the
     * specified color.
     *
     * @param color
     *            the solid color fill. The value of <code>null</code> unsets
     *            the solidFIll attribute from the underlying xml
     */
    @Override
    public void setFillColor(Color color) {
        CTTableCellProperties spPr = getCellProperties(true);
        if (color == null) {
            if (spPr.isSetSolidFill()) {
                spPr.unsetSolidFill();
            }
        } else {
            CTSolidColorFillProperties fill = spPr.isSetSolidFill() ? spPr.getSolidFill() : spPr.addNewSolidFill();
            XSLFColor c = new XSLFColor(fill, getSheet().getTheme(), fill.getSchemeClr());
            c.setColor(color);
        }
    }

    /**
     *
     * @return solid fill color of null if not set
     */
    @Override
    public Color getFillColor() {
        PaintStyle ps = getFillPaint();
        if (ps instanceof SolidPaint) {
            ColorStyle cs = ((SolidPaint) ps).getSolidColor();
            return DrawPaint.applyColorTransform(cs);
        }

        return null;
    }

    @SuppressWarnings("resource")
    @Override
    public PaintStyle getFillPaint() {
        XSLFSheet sheet = getSheet();
        XSLFTheme theme = sheet.getTheme();
        final boolean hasPlaceholder = getPlaceholder() != null;
        XmlObject props = getCellProperties(false);
        XSLFFillProperties fp = XSLFPropertiesDelegate.getFillDelegate(props);
        if (fp != null) {
            PaintStyle paint = selectPaint(fp, null, sheet.getPackagePart(), theme, hasPlaceholder);
            if (paint != null) {
                return paint;
            }
        }

        CTTablePartStyle tps = getTablePartStyle(null);
        if (tps == null || !tps.isSetTcStyle()) {
            tps = getTablePartStyle(TablePartStyle.wholeTbl);
            if (tps == null || !tps.isSetTcStyle()) {
                return null;
            }
        }

        XMLSlideShow slideShow = sheet.getSlideShow();
        CTTableStyleCellStyle tcStyle = tps.getTcStyle();
        if (tcStyle.isSetFill()) {
            props = tcStyle.getFill();
        } else if (tcStyle.isSetFillRef()) {
            props = tcStyle.getFillRef();
        } else {
            return null;
        }

        fp = XSLFPropertiesDelegate.getFillDelegate(props);
        if (fp != null) {
            PaintStyle paint = XSLFShape.selectPaint(fp, null, slideShow.getPackagePart(), theme, hasPlaceholder);
            if (paint != null) {
                return paint;
            }
        }

        return null;
    }

    /**
     * Retrieves the part style depending on the location of this cell
     *
     * @param tablePartStyle
     *            the part to be returned, usually this is null and only set
     *            when used as a helper method
     * @return the table part style
     */
    private CTTablePartStyle getTablePartStyle(TablePartStyle tablePartStyle) {
        CTTable ct = table.getCTTable();
        if (!ct.isSetTblPr()) {
            return null;
        }

        CTTableProperties pr = ct.getTblPr();
        boolean bandRow = (pr.isSetBandRow() && pr.getBandRow());
        boolean firstRow = (pr.isSetFirstRow() && pr.getFirstRow());
        boolean lastRow = (pr.isSetLastRow() && pr.getLastRow());
        boolean bandCol = (pr.isSetBandCol() && pr.getBandCol());
        boolean firstCol = (pr.isSetFirstCol() && pr.getFirstCol());
        boolean lastCol = (pr.isSetLastCol() && pr.getLastCol());

        TablePartStyle tps;
        if (tablePartStyle != null) {
            tps = tablePartStyle;
        } else if (row == 0 && firstRow) {
            tps = TablePartStyle.firstRow;
        } else if (row == table.getNumberOfRows() - 1 && lastRow) {
            tps = TablePartStyle.lastRow;
        } else if (col == 0 && firstCol) {
            tps = TablePartStyle.firstCol;
        } else if (col == table.getNumberOfColumns() - 1 && lastCol) {
            tps = TablePartStyle.lastCol;
        } else {
            tps = TablePartStyle.wholeTbl;

            int br = row + (firstRow ? 1 : 0);
            int bc = col + (firstCol ? 1 : 0);
            if (bandRow && (br & 1) == 0) {
                tps = TablePartStyle.band1H;
            } else if (bandCol && (bc & 1) == 0) {
                tps = TablePartStyle.band1V;
            }
        }

        XSLFTableStyle tabStyle = table.getTableStyle();
        if (tabStyle == null) {
            return null;
        }

        CTTablePartStyle part = tabStyle.getTablePartStyle(tps);
        return (part == null) ? tabStyle.getTablePartStyle(TablePartStyle.wholeTbl) : part;
    }

    void setGridSpan(int gridSpan_) {
        getCell().setGridSpan(gridSpan_);
    }

    @Override
    public int getGridSpan() {
        CTTableCell c = getCell();
        return (c.isSetGridSpan()) ? c.getGridSpan() : 1;
    }

    void setRowSpan(int rowSpan_) {
        getCell().setRowSpan(rowSpan_);
    }

    @Override
    public int getRowSpan() {
        CTTableCell c = getCell();
        return (c.isSetRowSpan()) ? c.getRowSpan() : 1;
    }

    void setHMerge() {
        getCell().setHMerge(true);
    }

    void setVMerge() {
        getCell().setVMerge(true);
    }

    @Override
    public void setVerticalAlignment(VerticalAlignment anchor) {
        CTTableCellProperties cellProps = getCellProperties(true);
        if (anchor == null) {
            if (cellProps.isSetAnchor()) {
                cellProps.unsetAnchor();
            }
        } else {
            cellProps.setAnchor(STTextAnchoringType.Enum.forInt(anchor.ordinal() + 1));
        }
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        CTTableCellProperties cellProps = getCellProperties(false);

        VerticalAlignment align = VerticalAlignment.TOP;
        if (cellProps != null && cellProps.isSetAnchor()) {
            int ival = cellProps.getAnchor().intValue();
            align = VerticalAlignment.values()[ival - 1];
        }
        return align;
    }

    /**
     * @since POI 3.15-beta2
     */
    @Override
    public void setTextDirection(TextDirection orientation) {
        CTTableCellProperties cellProps = getCellProperties(true);
        if (orientation == null) {
            if (cellProps.isSetVert()) {
                cellProps.unsetVert();
            }
        } else {
            STTextVerticalType.Enum vt;
            switch (orientation) {
            default:
            case HORIZONTAL:
                vt = STTextVerticalType.HORZ;
                break;
            case VERTICAL:
                vt = STTextVerticalType.VERT;
                break;
            case VERTICAL_270:
                vt = STTextVerticalType.VERT_270;
                break;
            case STACKED:
                vt = STTextVerticalType.WORD_ART_VERT;
                break;
            }

            cellProps.setVert(vt);
        }
    }

    /**
     * @since POI 3.15-beta2
     */
    @Override
    public TextDirection getTextDirection() {
        CTTableCellProperties cellProps = getCellProperties(false);

        STTextVerticalType.Enum orientation;
        if (cellProps != null && cellProps.isSetVert()) {
            orientation = cellProps.getVert();
        } else {
            orientation = STTextVerticalType.HORZ;
        }

        switch (orientation.intValue()) {
        default:
        case STTextVerticalType.INT_HORZ:
            return TextDirection.HORIZONTAL;
        case STTextVerticalType.INT_VERT:
        case STTextVerticalType.INT_EA_VERT:
        case STTextVerticalType.INT_MONGOLIAN_VERT:
            return TextDirection.VERTICAL;
        case STTextVerticalType.INT_VERT_270:
            return TextDirection.VERTICAL_270;
        case STTextVerticalType.INT_WORD_ART_VERT:
        case STTextVerticalType.INT_WORD_ART_VERT_RTL:
            return TextDirection.STACKED;
        }
    }

    private CTTableCell getCell() {
        return (CTTableCell) getXmlObject();
    }

    /* package */ void setRowColIndex(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Return a fake-xfrm which is used for calculating the text height
     */
    protected CTTransform2D getXfrm() {
        Rectangle2D anc = getAnchor();
        CTTransform2D xfrm = CTTransform2D.Factory.newInstance();
        CTPoint2D off = xfrm.addNewOff();
        off.setX(Units.toEMU(anc.getX()));
        off.setY(Units.toEMU(anc.getY()));
        CTPositiveSize2D size = xfrm.addNewExt();
        size.setCx(Units.toEMU(anc.getWidth()));
        size.setCy(Units.toEMU(anc.getHeight()));
        return xfrm;
    }

    /**
     * There's no real anchor for table cells - this method is used to
     * temporarily store the location of the cell for a later retrieval, e.g.
     * for rendering
     *
     * @since POI 3.15-beta2
     */
    @Override
    public void setAnchor(Rectangle2D anchor) {
        if (this.anchor == null) {
            this.anchor = (Rectangle2D) anchor.clone();
        } else {
            this.anchor.setRect(anchor);
        }
    }

    /**
     * @since POI 3.15-beta2
     */
    @Override
    public Rectangle2D getAnchor() {
        if (anchor == null) {
            table.updateCellAnchor();
        }
        // anchor should be set, after updateCellAnchor is through
        assert (anchor != null);
        return anchor;
    }

    /**
     * @since POI 3.15-beta2
     */
    @Override
    public boolean isMerged() {
        CTTableCell c = getCell();
        return (c.isSetHMerge() && c.getHMerge()) || (c.isSetVMerge() && c.getVMerge());
    }

    /**
     * @since POI 3.15-beta2
     */
    @Override
    protected XSLFCellTextParagraph newTextParagraph(CTTextParagraph p) {
        return new XSLFCellTextParagraph(p, this);
    }

    @Override
    protected XmlObject getShapeProperties() {
        return getCellProperties(false);
    }

    /**
     * @since POI 3.15-beta2
     */
    private final class XSLFCellTextParagraph extends XSLFTextParagraph {
        private XSLFCellTextParagraph(CTTextParagraph p, XSLFTextShape shape) {
            super(p, shape);
        }

        @Override
        protected XSLFCellTextRun newTextRun(XmlObject r) {
            return new XSLFCellTextRun(r, this);
        }
    }

    /**
     * @since POI 3.15-beta2
     */
    private final class XSLFCellTextRun extends XSLFTextRun {
        private XSLFCellTextRun(XmlObject r, XSLFTextParagraph p) {
            super(r, p);
        }

        @Override
        public PaintStyle getFontColor() {
            CTTableStyleTextStyle txStyle = getTextStyle();
            if (txStyle == null) {
                return super.getFontColor();
            }

            CTSchemeColor phClr = null;
            CTFontReference fontRef = txStyle.getFontRef();
            if (fontRef != null) {
                phClr = fontRef.getSchemeClr();
            }

            XSLFTheme theme = getSheet().getTheme();
            final XSLFColor c = new XSLFColor(txStyle, theme, phClr);
            return DrawPaint.createSolidPaint(c.getColorStyle());
        }

        @Override
        public boolean isBold() {
            CTTableStyleTextStyle txStyle = getTextStyle();
            if (txStyle == null) {
                return super.isBold();
            } else {
                return txStyle.isSetB() && txStyle.getB().intValue() == STOnOffStyleType.INT_ON;
            }
        }

        @Override
        public boolean isItalic() {
            CTTableStyleTextStyle txStyle = getTextStyle();
            if (txStyle == null) {
                return super.isItalic();
            } else {
                return txStyle.isSetI() && txStyle.getI().intValue() == STOnOffStyleType.INT_ON;
            }
        }

        private CTTableStyleTextStyle getTextStyle() {
            CTTablePartStyle tps = getTablePartStyle(null);
            if (tps == null || !tps.isSetTcTxStyle()) {
                tps = getTablePartStyle(TablePartStyle.wholeTbl);
            }
            return (tps == null) ? null : tps.getTcTxStyle();
        }
    }
}
