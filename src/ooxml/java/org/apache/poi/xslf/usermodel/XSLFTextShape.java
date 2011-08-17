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

import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextVerticalType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextWrappingType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shape that can hold text.
 *
 * @author Yegor Kozlov
 */
@Beta
public abstract class XSLFTextShape extends XSLFSimpleShape {
    private final List<XSLFTextParagraph> _paragraphs;

    /*package*/ XSLFTextShape(XmlObject shape, XSLFSheet sheet) {
        super(shape, sheet);

        _paragraphs = new ArrayList<XSLFTextParagraph>();
        CTTextBody txBody = getTextBody(false);
        if (txBody != null) {
            for (CTTextParagraph p : txBody.getPList()) {
                _paragraphs.add(new XSLFTextParagraph(p, this));
            }
        }
    }

    // textual properties
    public String getText() {
        StringBuilder out = new StringBuilder();
        for (XSLFTextParagraph p : _paragraphs) {
            if (out.length() > 0) out.append('\n');
            out.append(p.getText());
        }
        return out.toString();
    }

    public List<XSLFTextParagraph> getTextParagraphs() {
        return _paragraphs;
    }

    public XSLFTextParagraph addNewTextParagraph() {
        CTTextBody txBody = getTextBody(true);
        CTTextParagraph p = txBody.addNewP();
        XSLFTextParagraph paragraph = new XSLFTextParagraph(p, this);
        _paragraphs.add(paragraph);
        return paragraph;
    }

    /**
     * Specifies a solid color fill. The shape is filled entirely with the specified color.
     *
     * @param color the solid color fill.
     * The value of <code>null</code> unsets the solidFIll attribute from the underlying xml
     */
    public void setFillColor(Color color) {
        CTShapeProperties spPr = getSpPr();
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
    public Color getFillColor(){
        CTShapeProperties spPr = getSpPr();
        if(!spPr.isSetSolidFill() ) return null;

        CTSolidColorFillProperties fill = spPr.getSolidFill();
        if(!fill.isSetSrgbClr()) {
            // TODO for now return null for all colors except explicit RGB
            return null;
        }
        byte[] val = fill.getSrgbClr().getVal();
        return new Color(0xFF & val[0], 0xFF & val[1], 0xFF & val[2]);
    }

    /**
     * Sets the type of vertical alignment for the text.
     * One of the <code>Anchor*</code> constants defined in this class.
     *
     * @param anchor - the type of alignment. Default is {@link org.apache.poi.xslf.usermodel.VerticalAlignment#TOP}
     */
    public void setVerticalAlignment(VerticalAlignment anchor){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
             if(anchor == null) {
                if(bodyPr.isSetAnchor()) bodyPr.unsetAnchor();
            } else {
                bodyPr.setAnchor(STTextAnchoringType.Enum.forInt(anchor.ordinal() + 1));
            }
        }
    }

    /**
     * Returns the type of vertical alignment for the text.
     *
     * @return the type of alignment
     */
    public VerticalAlignment getVerticalAlignment(){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            STTextAnchoringType.Enum val = bodyPr.getAnchor();
            if(val != null){
                return VerticalAlignment.values()[val.intValue() - 1];
            }
        }
        return VerticalAlignment.TOP;
    }

    /**
     *
     * @param orientation vertical orientation of the text
     */
    public void setTextDirection(TextDirection orientation){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(orientation == null) {
                if(bodyPr.isSetVert()) bodyPr.unsetVert();
            } else {
                bodyPr.setVert(STTextVerticalType.Enum.forInt(orientation.ordinal() + 1));
            }
        }
    }

    /**
     * @return vertical orientation of the text
     */
    public TextDirection getTextDirection(){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            STTextVerticalType.Enum val = bodyPr.getVert();
            if(val != null){
                return TextDirection.values()[val.intValue() - 1];
            }
        }
        return TextDirection.HORIZONTAL;
    }
    /**
     * Returns the distance (in points) between the bottom of the text frame
     * and the bottom of the inscribed rectangle of the shape that contains the text.
     *
     * @return the bottom margin or -1 if not set
     */
    public double getMarginBottom(){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            return bodyPr.isSetBIns() ? Units.toPoints(bodyPr.getBIns()) : -1;
        }
        return -1;
    }

    /**
     *  Returns the distance (in points) between the left edge of the text frame
     *  and the left edge of the inscribed rectangle of the shape that contains
     *  the text.
     *
     * @return the left margin
     */
    public double getMarginLeft(){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            return bodyPr.isSetLIns() ? Units.toPoints(bodyPr.getLIns()) : -1;
        }
        return -1;
    }

    /**
     *  Returns the distance (in points) between the right edge of the
     *  text frame and the right edge of the inscribed rectangle of the shape
     *  that contains the text.
     *
     * @return the right margin
     */
    public double getMarginRight(){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            return bodyPr.isSetRIns() ? Units.toPoints(bodyPr.getRIns()) : -1;
        }
        return -1;
    }

    /**
     *  Returns the distance (in points) between the top of the text frame
     *  and the top of the inscribed rectangle of the shape that contains the text.
     *
     * @return the top margin
     */
    public double getMarginTop(){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            return bodyPr.isSetTIns() ? Units.toPoints(bodyPr.getTIns()) : -1;
        }
        return -1;
    }

    /**
     * Sets the botom margin.
     * @see #getMarginBottom()
     *
     * @param margin    the bottom margin
     */
    public void setMarginBottom(double margin){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(margin == -1) bodyPr.unsetBIns();
            else bodyPr.setBIns(Units.toEMU(margin));
        }
    }

    /**
     * Sets the left margin.
     * @see #getMarginLeft()
     *
     * @param margin    the left margin
     */
    public void setMarginLeft(double margin){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(margin == -1) bodyPr.unsetLIns();
            else bodyPr.setLIns(Units.toEMU(margin));
        }
    }

    /**
     * Sets the right margin.
     * @see #getMarginRight()
     *
     * @param margin    the right margin
     */
    public void setMarginRight(double margin){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(margin == -1) bodyPr.unsetRIns();
            else bodyPr.setRIns(Units.toEMU(margin));
        }
    }

    /**
     * Sets the top margin.
     * @see #getMarginTop()
     *
     * @param margin    the top margin
     */
    public void setMarginTop(double margin){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(margin == -1) bodyPr.unsetTIns();
            else bodyPr.setTIns(Units.toEMU(margin));
        }
    }


    /**
     * Returns the value indicating word wrap.
     * One of the <code>Wrap*</code> constants defined in this class.
     *
     * @return the value indicating word wrap
     */
    public boolean getWordWrap(){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            return bodyPr.getWrap() == STTextWrappingType.SQUARE;
        }
        return false;
    }

    /**
     *  Specifies how the text should be wrapped
     *
     * @param wrap  the value indicating how the text should be wrapped
     */
    public void setWordWrap(boolean wrap){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            bodyPr.setWrap(wrap ? STTextWrappingType.SQUARE : STTextWrappingType.NONE);
        }
    }

    /**
     *
     * Specifies that a shape should be auto-fit to fully contain the text described within it.
     * Auto-fitting is when text within a shape is scaled in order to contain all the text inside
     *
     * @param value type of autofit
     */
    public void setTextAutofit(TextAutofit value){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(bodyPr.isSetSpAutoFit()) bodyPr.unsetSpAutoFit();
            if(bodyPr.isSetNoAutofit()) bodyPr.unsetNoAutofit();
            if(bodyPr.isSetNormAutofit()) bodyPr.unsetNormAutofit();

            switch(value){
                case NONE: bodyPr.addNewNoAutofit(); break;
                case NORMAL: bodyPr.addNewNormAutofit(); break;
                case SHAPE: bodyPr.addNewSpAutoFit(); break;
            }
        }
    }

    /**
     *
     * @return type of autofit
     */
    public TextAutofit getTextAutofit(){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(bodyPr.isSetNoAutofit()) return TextAutofit.NONE;
            else if (bodyPr.isSetNormAutofit()) return TextAutofit.NORMAL;
            else if (bodyPr.isSetSpAutoFit()) return TextAutofit.SHAPE;
        }
        return TextAutofit.NORMAL;
    }

    protected CTTextBodyProperties getTextBodyPr(){
        CTTextBody textBody = getTextBody(false);
        return textBody == null ? null : textBody.getBodyPr();
    }


    protected abstract CTTextBody getTextBody(boolean create);
}