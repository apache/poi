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

import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCell;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTableCellProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STPenAlignment;
import org.openxmlformats.schemas.drawingml.x2006.main.STCompoundLine;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineCap;
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTLineEndProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndLength;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndType;
import org.openxmlformats.schemas.drawingml.x2006.main.STLineEndWidth;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;

import java.awt.*;

/**
 * Represents a cell of a table in a .pptx presentation
 *
 * @author Yegor Kozlov
 */
public class XSLFTableCell extends XSLFTextShape {
    static double defaultBorderWidth = 1.0;

    /*package*/ XSLFTableCell(CTTableCell cell, XSLFSheet sheet){
        super(cell, sheet);
    }

    @Override
    public CTTableCell getXmlObject(){
        return (CTTableCell)super.getXmlObject();
    }

    @Override
    protected CTTextBody getTextBody(boolean create){
        CTTableCell cell = getXmlObject();
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

    @Override
    public void setMarginLeft(double margin){
        CTTableCellProperties pr = getXmlObject().getTcPr();
        if(pr == null) pr = getXmlObject().addNewTcPr();

        pr.setMarL(Units.toEMU(margin));
    }
    
    @Override
    public void setMarginRight(double margin){
        CTTableCellProperties pr = getXmlObject().getTcPr();
        if(pr == null) pr = getXmlObject().addNewTcPr();

        pr.setMarR(Units.toEMU(margin));
    }

    @Override
    public void setMarginTop(double margin){
        CTTableCellProperties pr = getXmlObject().getTcPr();
        if(pr == null) pr = getXmlObject().addNewTcPr();

        pr.setMarT(Units.toEMU(margin));
    }

    @Override
    public void setMarginBottom(double margin){
        CTTableCellProperties pr = getXmlObject().getTcPr();
        if(pr == null) pr = getXmlObject().addNewTcPr();

        pr.setMarB(Units.toEMU(margin));
    }

    public void setBorderLeft(double width){
        CTTableCellProperties pr = getXmlObject().getTcPr();

        CTLineProperties ln = pr.isSetLnL() ? pr.getLnL() : pr.addNewLnL();
        ln.setW(Units.toEMU(width));
    }

    public double getBorderLeft(){
        CTTableCellProperties pr = getXmlObject().getTcPr();

        CTLineProperties ln = pr.getLnL();
        return ln == null || !ln.isSetW() ? defaultBorderWidth : Units.toPoints(ln.getW());
    }

    public void setBorderLeftColor(Color color){
        CTTableCellProperties pr = getXmlObject().getTcPr();
        CTLineProperties ln = pr.isSetLnL() ? pr.getLnL() : pr.addNewLnL();
        setLineColor(ln, color);
    }

    public Color getBorderLeftColor(){
        return getLineColor(getXmlObject().getTcPr().getLnL());
    }

    public void setBorderRight(double width){
        CTTableCellProperties pr = getXmlObject().getTcPr();

        CTLineProperties ln = pr.isSetLnR() ? pr.getLnR() : pr.addNewLnR();
        ln.setW(Units.toEMU(width));
    }

    public double getBorderRight(){
        CTTableCellProperties pr = getXmlObject().getTcPr();

        CTLineProperties ln = pr.getLnR();
        return ln == null || !ln.isSetW() ? defaultBorderWidth : Units.toPoints(ln.getW());
    }

    public void setBorderRightColor(Color color){
        CTTableCellProperties pr = getXmlObject().getTcPr();
        CTLineProperties ln = pr.isSetLnR() ? pr.getLnR() : pr.addNewLnR();
        setLineColor(ln, color);
    }

    public Color getBorderRightColor(){
        return getLineColor(getXmlObject().getTcPr().getLnR());
    }

    public void setBorderTop(double width){
        CTTableCellProperties pr = getXmlObject().getTcPr();

        CTLineProperties ln = pr.isSetLnT() ? pr.getLnT() : pr.addNewLnT();
        ln.setW(Units.toEMU(width));
    }

    public double getBorderTop(){
        CTTableCellProperties pr = getXmlObject().getTcPr();

        CTLineProperties ln = pr.getLnT();
        return ln == null || !ln.isSetW() ? defaultBorderWidth : Units.toPoints(ln.getW());
    }

    public void setBorderTopColor(Color color){
        CTTableCellProperties pr = getXmlObject().getTcPr();
        CTLineProperties ln = pr.isSetLnT() ? pr.getLnT() : pr.addNewLnT();
        setLineColor(ln, color);
    }

    public Color getBorderTopColor(){
        return getLineColor(getXmlObject().getTcPr().getLnT());
    }

    public void setBorderBottom(double width){
        CTTableCellProperties pr = getXmlObject().getTcPr();

        CTLineProperties ln = pr.isSetLnB() ? pr.getLnB() : pr.addNewLnB();
        ln.setW(Units.toEMU(width));
    }

    public double getBorderBottom(){
        CTTableCellProperties pr = getXmlObject().getTcPr();

        CTLineProperties ln = pr.getLnB();
        return ln == null || !ln.isSetW() ? defaultBorderWidth : Units.toPoints(ln.getW());
    }

    public void setBorderBottomColor(Color color){
        CTTableCellProperties pr = getXmlObject().getTcPr();
        CTLineProperties ln = pr.isSetLnB() ? pr.getLnB() : pr.addNewLnB();
        setLineColor(ln, color);
    }

    public Color getBorderBottomColor(){
        return getLineColor(getXmlObject().getTcPr().getLnB());
    }

    private void setLineColor(CTLineProperties ln, Color color){
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

    private Color getLineColor(CTLineProperties ln){
        if(ln == null || ln.isSetNoFill() || !ln.isSetSolidFill()) return null;

        CTSolidColorFillProperties fill = ln.getSolidFill();
        if(!fill.isSetSrgbClr()) {
            // TODO for now return null for all colors except explicit RGB
            return null;
        }
        byte[] val = fill.getSrgbClr().getVal();
        return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
    }
    /**
     * Specifies a solid color fill. The shape is filled entirely with the specified color.
     *
     * @param color the solid color fill.
     * The value of <code>null</code> unsets the solidFIll attribute from the underlying xml
     */
    @Override
    public void setFillColor(Color color) {
        CTTableCellProperties spPr = getXmlObject().getTcPr();
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
        CTTableCellProperties spPr = getXmlObject().getTcPr();
        if(!spPr.isSetSolidFill() ) return null;

        CTSolidColorFillProperties fill = spPr.getSolidFill();
        if(!fill.isSetSrgbClr()) {
            // TODO for now return null for all colors except explicit RGB
            return null;
        }
        byte[] val = fill.getSrgbClr().getVal();
        return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
    }

}
