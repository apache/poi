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

import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.StrokeStyle;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCap;
import org.apache.poi.sl.usermodel.StrokeStyle.LineCompound;
import org.apache.poi.sl.usermodel.StrokeStyle.LineDash;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineEndProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCellProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.STCompoundLine;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineCap;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndLength;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndType;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndWidth;
import org.openxmlformats.schemas.drawingml.x2006.main.STPenAlignment;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextVerticalType;

/**
 * Represents a cell of a table in a .pptx presentation
 */
public class XSLFTableCell extends XSLFTextShape implements TableCell<XSLFShape,XSLFTextParagraph> {
    private CTTableCellProperties _tcPr = null;

    /*package*/ XSLFTableCell(CTTableCell cell, XSLFSheet sheet){
        super(cell, sheet);
    }

    @Override
    protected CTTextBody getTextBody(boolean create){
        CTTableCell cell = (CTTableCell)getXmlObject();
        CTTextBody txBody = cell.getTxBody();
        if (txBody == null && create) {
            txBody = cell.addNewTxBody();
            XSLFAutoShape.initTextBody(txBody);
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

    protected CTTableCellProperties getCellProperties(boolean create) {
        if (_tcPr == null) {
            CTTableCell cell = (CTTableCell)getXmlObject();
            _tcPr = cell.getTcPr();
            if (_tcPr == null && create) {
                _tcPr = cell.addNewTcPr();
            }
        }
        return _tcPr;
    }

    @Override
    public void setLeftInset(double margin){
        CTTableCellProperties pr = getCellProperties(true);
        pr.setMarL(Units.toEMU(margin));
    }

    @Override
    public void setRightInset(double margin){
        CTTableCellProperties pr = getCellProperties(true);
        pr.setMarR(Units.toEMU(margin));
    }

    @Override
    public void setTopInset(double margin){
        CTTableCellProperties pr = getCellProperties(true);
        pr.setMarT(Units.toEMU(margin));
    }

    @Override
    public void setBottomInset(double margin){
        CTTableCellProperties pr = getCellProperties(true);
        pr.setMarB(Units.toEMU(margin));
    }

    private CTLineProperties getCTLine(BorderEdge edge, boolean create) {
        if (edge == null) {
            throw new IllegalArgumentException("BorderEdge needs to be specified.");
        }

        CTTableCellProperties pr = getCellProperties(create);
        if (pr == null) return null;

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
        if (pr == null) return;
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
            public PaintStyle getPaint() {
                return DrawPaint.createSolidPaint(getBorderColor(edge));
            }

            public LineCap getLineCap() {
                return getBorderCap(edge);
            }

            public LineDash getLineDash() {
                return getBorderDash(edge);
            }

            public LineCompound getLineCompound() {
                return getBorderCompound(edge);
            }

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

    public Double getBorderWidth(BorderEdge edge) {
        CTLineProperties ln = getCTLine(edge, false);
        return (ln == null || !ln.isSetW()) ? null : Units.toPoints(ln.getW());
    }

    @Override
    public void setBorderWidth(BorderEdge edge, double width) {
        CTLineProperties ln = getCTLine(edge, true);
        ln.setW(Units.toEMU(width));
    }

    private CTLineProperties setBorderDefaults(BorderEdge edge) {
        CTLineProperties ln = getCTLine(edge, true);
        if (ln.isSetNoFill()) {
            ln.unsetNoFill();
        }

        if(!ln.isSetPrstDash()) {
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

        CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
        rgb.setVal(new byte[]{(byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue()});
        ln.addNewSolidFill().setSrgbClr(rgb);
    }

    public Color getBorderColor(BorderEdge edge) {
        CTLineProperties ln = getCTLine(edge, false);
        if (ln == null || ln.isSetNoFill() || !ln.isSetSolidFill()) return null;

        CTSolidColorFillProperties fill = ln.getSolidFill();
        if (!fill.isSetSrgbClr()) {
            // TODO for now return null for all colors except explicit RGB
            return null;
        }
        byte[] val = fill.getSrgbClr().getVal();
        return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
    }

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
        ln.getPrstDash().setVal(STPresetLineDashVal.Enum.forInt(dash.ooxmlId));
    }

    public LineCap getBorderCap(BorderEdge edge) {
        CTLineProperties ln = getCTLine(edge, false);
        if (ln == null || ln.isSetNoFill() || !ln.isSetSolidFill() || !ln.isSetCap()) {
            return null;
        }
        
        return LineCap.fromOoxmlId(ln.getCap().intValue());
    }

    public void setBorderCap(BorderEdge edge, LineCap cap) {
        if (cap == null) {
            throw new IllegalArgumentException("LineCap need to be specified.");
        }

        CTLineProperties ln = setBorderDefaults(edge);
        ln.setCap(STLineCap.Enum.forInt(cap.ooxmlId));
    }



    /**
     * Specifies a solid color fill. The shape is filled entirely with the specified color.
     *
     * @param color the solid color fill.
     * The value of <code>null</code> unsets the solidFIll attribute from the underlying xml
     */
    @Override
    public void setFillColor(Color color) {
        CTTableCellProperties spPr = getCellProperties(true);
        if (color == null) {
            if(spPr.isSetSolidFill()) spPr.unsetSolidFill();
        }
        else {
            CTSolidColorFillProperties fill = spPr.isSetSolidFill() ? spPr.getSolidFill() : spPr.addNewSolidFill();

            CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
            rgb.setVal(new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()});

            fill.setSrgbClr(rgb);
        }
    }

    /**
     *
     * @return solid fill color of null if not set
     */
    @Override
    public Color getFillColor(){
        CTTableCellProperties spPr = getCellProperties(false);
        if (spPr == null || !spPr.isSetSolidFill()) return null;

        CTSolidColorFillProperties fill = spPr.getSolidFill();
        if (!fill.isSetSrgbClr()) {
            // TODO for now return null for all colors except explicit RGB
            return null;
        }
        byte[] val = fill.getSrgbClr().getVal();
        return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
    }

    void setGridSpan(int gridSpan_) {
        ((CTTableCell)getXmlObject()).setGridSpan(gridSpan_);
    }

    void setRowSpan(int rowSpan_) {
        ((CTTableCell)getXmlObject()).setRowSpan(rowSpan_);
    }

    void setHMerge(boolean merge_) {
        ((CTTableCell)getXmlObject()).setHMerge(merge_);
    }

    void setVMerge(boolean merge_) {
        ((CTTableCell)getXmlObject()).setVMerge(merge_);
    }

    @Override
    public void setVerticalAlignment(VerticalAlignment anchor){
    	CTTableCellProperties cellProps = getCellProperties(true);
		if(anchor == null) {
			if(cellProps.isSetAnchor()) {
				cellProps.unsetAnchor();
			}
		} else {
			cellProps.setAnchor(STTextAnchoringType.Enum.forInt(anchor.ordinal() + 1));
		}
    }

    @Override
    public VerticalAlignment getVerticalAlignment(){
        CTTableCellProperties cellProps = getCellProperties(false);

        VerticalAlignment align = VerticalAlignment.TOP;
        if(cellProps != null && cellProps.isSetAnchor()) {
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
        if(orientation == null) {
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
}
