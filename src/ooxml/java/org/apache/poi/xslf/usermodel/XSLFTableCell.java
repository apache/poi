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

/**
 * Represents a cell of a table in a .pptx presentation
 */
public class XSLFTableCell extends XSLFTextShape implements TableCell<XSLFShape,XSLFTextParagraph> {
    static double defaultBorderWidth = 1.0;
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
            txBody.addNewBodyPr();
            txBody.addNewLstStyle();
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

    private CTLineProperties getCTLine(char bltr, boolean create) {
        CTTableCellProperties pr = getCellProperties(create);
        if (pr == null) return null;
        
        switch (bltr) {
            case 'b':
                return (pr.isSetLnB()) ? pr.getLnB() : (create ? pr.addNewLnB() : null);
            case 'l':
                return (pr.isSetLnL()) ? pr.getLnL() : (create ? pr.addNewLnL() : null);
            case 't':
                return (pr.isSetLnT()) ? pr.getLnT() : (create ? pr.addNewLnT() : null);
            case 'r':
                return (pr.isSetLnR()) ? pr.getLnR() : (create ? pr.addNewLnR() : null);
            default:
                return null;
        }
    }
    
    private void setBorderWidth(char bltr, double width) {
        CTLineProperties ln = getCTLine(bltr, true);
        ln.setW(Units.toEMU(width));
    }

    private double getBorderWidth(char bltr) {
        CTLineProperties ln = getCTLine(bltr, false);
        return (ln == null || !ln.isSetW()) ? defaultBorderWidth : Units.toPoints(ln.getW());
    }

    private void setBorderColor(char bltr, Color color) {
        CTLineProperties ln = getCTLine(bltr, true);

        if(color == null){
            ln.addNewNoFill();
            if(ln.isSetSolidFill()) ln.unsetSolidFill();
        } else {
            if(ln.isSetNoFill()) ln.unsetNoFill();

            if(!ln.isSetPrstDash()) ln.addNewPrstDash().setVal(STPresetLineDashVal.SOLID);
            ln.setCmpd(STCompoundLine.SNG);
            ln.setAlgn(STPenAlignment.CTR);
            ln.setCap(STLineCap.FLAT);
            ln.addNewRound();

            CTLineEndProperties hd = ln.addNewHeadEnd();
            hd.setType(STLineEndType.NONE);
            hd.setW(STLineEndWidth.MED);
            hd.setLen(STLineEndLength.MED);

            CTLineEndProperties tl = ln.addNewTailEnd();
            tl.setType(STLineEndType.NONE);
            tl.setW(STLineEndWidth.MED);
            tl.setLen(STLineEndLength.MED);

            CTSRgbColor rgb = CTSRgbColor.Factory.newInstance();
            rgb.setVal(new byte[]{(byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue()});
            ln.addNewSolidFill().setSrgbClr(rgb);
        }
    }    
    
    private Color getBorderColor(char bltr) {
        CTLineProperties ln = getCTLine(bltr,false);
        if (ln == null || ln.isSetNoFill() || !ln.isSetSolidFill()) return null;

        CTSolidColorFillProperties fill = ln.getSolidFill();
        if (!fill.isSetSrgbClr()) {
            // TODO for now return null for all colors except explicit RGB
            return null;
        }
        byte[] val = fill.getSrgbClr().getVal();
        return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
    }    
    
    public void setBorderLeft(double width) {
        setBorderWidth('l', width);
    }

    public double getBorderLeft() {
        return getBorderWidth('l');
    }

    public void setBorderLeftColor(Color color) {
        setBorderColor('l', color);
    }

    public Color getBorderLeftColor() {
        return getBorderColor('l');
    }

    public void setBorderRight(double width) {
        setBorderWidth('r', width);
    }

    public double getBorderRight() {
        return getBorderWidth('r');
    }

    public void setBorderRightColor(Color color) {
        setBorderColor('r', color);
    }

    public Color getBorderRightColor() {
        return getBorderColor('r');
    }

    public void setBorderTop(double width) {
        setBorderWidth('t', width);
    }

    public double getBorderTop() {
        return getBorderWidth('t');
    }

    public void setBorderTopColor(Color color) {
        setBorderColor('t', color);
    }

    public Color getBorderTopColor() {
        return getBorderColor('t');
    }

    public void setBorderBottom(double width) {
        setBorderWidth('b', width);
    }

    public double getBorderBottom() {
        return getBorderWidth('b');
    }

    public void setBorderBottomColor(Color color) {
        setBorderColor('b', color);
    }

    public Color getBorderBottomColor(){
        return getBorderColor('b');
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

}
