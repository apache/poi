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
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.poi.xslf.model.TextBodyPropertyFetcher;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextVerticalType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextWrappingType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
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
        PropertyFetcher<VerticalAlignment> fetcher = new TextBodyPropertyFetcher<VerticalAlignment>(){
            public boolean fetch(CTTextBodyProperties props){
                if(props.isSetAnchor()){
                    int val = props.getAnchor().intValue();
                    setValue(VerticalAlignment.values()[val - 1]);
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue() == null ? VerticalAlignment.TOP : fetcher.getValue();
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
        PropertyFetcher<Double> fetcher = new TextBodyPropertyFetcher<Double>(){
            public boolean fetch(CTTextBodyProperties props){
                if(props.isSetBIns()){
                    double val = Units.toPoints(props.getBIns());
                    setValue(val);
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue() == null ? 0 : fetcher.getValue();
    }

    /**
     *  Returns the distance (in points) between the left edge of the text frame
     *  and the left edge of the inscribed rectangle of the shape that contains
     *  the text.
     *
     * @return the left margin
     */
    public double getMarginLeft(){
        PropertyFetcher<Double> fetcher = new TextBodyPropertyFetcher<Double>(){
            public boolean fetch(CTTextBodyProperties props){
                if(props.isSetLIns()){
                    double val = Units.toPoints(props.getLIns());
                    setValue(val);
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue() == null ? 0 : fetcher.getValue();
    }

    /**
     *  Returns the distance (in points) between the right edge of the
     *  text frame and the right edge of the inscribed rectangle of the shape
     *  that contains the text.
     *
     * @return the right margin
     */
    public double getMarginRight(){
        PropertyFetcher<Double> fetcher = new TextBodyPropertyFetcher<Double>(){
            public boolean fetch(CTTextBodyProperties props){
                if(props.isSetRIns()){
                    double val = Units.toPoints(props.getRIns());
                    setValue(val);
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue() == null ? 0 : fetcher.getValue();
    }

    /**
     *  Returns the distance (in points) between the top of the text frame
     *  and the top of the inscribed rectangle of the shape that contains the text.
     *
     * @return the top margin
     */
    public double getMarginTop(){
        PropertyFetcher<Double> fetcher = new TextBodyPropertyFetcher<Double>(){
            public boolean fetch(CTTextBodyProperties props){
                if(props.isSetTIns()){
                    double val = Units.toPoints(props.getTIns());
                    setValue(val);
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue() == null ? 0 : fetcher.getValue();
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
        PropertyFetcher<Boolean> fetcher = new TextBodyPropertyFetcher<Boolean>(){
            public boolean fetch(CTTextBodyProperties props){
               if(props.isSetWrap()){
                    setValue(props.getWrap() == STTextWrappingType.SQUARE);
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue() == null ? true : fetcher.getValue();
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


    public Placeholder getTextType(){
        CTPlaceholder ph;
        XmlObject[] obj = getXmlObject().selectPath(
                "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:nvPr/p:ph");
        if(obj.length == 1){
            ph = (CTPlaceholder)obj[0];
            int val = ph.getType().intValue();
            return Placeholder.values()[val - 1];
        }
        else {
            return null;
        }
    }

    @Override
    public void draw(Graphics2D graphics){
        java.awt.Shape outline = getOutline();

        // shadow
        XSLFShadow shadow = getShadow();
        if(shadow != null) shadow.draw(graphics);

        //fill
        Color fillColor = getFillColor();
        if (fillColor != null) {
            graphics.setColor(fillColor);
            applyFill(graphics);
            graphics.fill(outline);
        }
 
        //border
        Color lineColor = getLineColor();
        if (lineColor != null){
            graphics.setColor(lineColor);
            applyStroke(graphics);
            graphics.draw(outline);
        }

        // text
        if(getText().length() > 0) drawText(graphics);
    }    

    /**
     * Compute the cumulative height occupied by the text
     */
    private double getTextHeight(){
        // dry-run in a 1x1 image and return the vertical advance
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        return drawParagraphs(img.createGraphics(), 0, 0);
    }

    void breakText(Graphics2D graphics){
        for(XSLFTextParagraph p : _paragraphs) p.breakText(graphics);
    }

    public void drawText(Graphics2D graphics) {
        breakText(graphics);

        Rectangle2D anchor = getAnchor();
        double x = anchor.getX() + getMarginLeft();
        double y = anchor.getY();

        // first dry-run to calculate the total height of the text
        double textHeight = getTextHeight();

        switch (getVerticalAlignment()){
            case TOP:
                y += getMarginTop();
                break;
            case BOTTOM:
                y += anchor.getHeight() - textHeight - getMarginBottom();
                break;
            default:
            case MIDDLE:
                double delta = anchor.getHeight() - textHeight -
                        getMarginTop() - getMarginBottom();
                y += getMarginTop()  + delta/2;
                break;
        }

        drawParagraphs(graphics, x, y);

    }


    /**
     * pain the paragraphs starting from top left (x,y)
     *
     * @return  the vertical advance, i.e. the cumulative space occupied by the text
     */
    private double drawParagraphs(Graphics2D graphics,  double x, double y) {
        double y0 = y;
        for(int i = 0; i < _paragraphs.size(); i++){
            XSLFTextParagraph p = _paragraphs.get(i);
            java.util.List<XSLFTextParagraph.TextFragment> lines = p.getTextLines();

            if(i > 0 && lines.size() > 0) {
                // the amount of vertical white space before the paragraph
                double spaceBefore = p.getSpaceBefore();
                if(spaceBefore > 0) {
                    // positive value means percentage spacing of the height of the first line, e.g.
                    // the higher the first line, the bigger the space before the paragraph
                    y += spaceBefore*0.01*lines.get(0).getHeight();
                } else {
                    // negative value means the absolute spacing in points
                    y += -spaceBefore;
                }
            }

            y += p.draw(graphics, x, y);

            if(i < _paragraphs.size() - 1) {
                double spaceAfter = p.getSpaceAfter();
                if(spaceAfter > 0) {
                    // positive value means percentage spacing of the height of the last line, e.g.
                    // the higher the last line, the bigger the space after the paragraph
                    y += spaceAfter*0.01*lines.get(lines.size() - 1).getHeight();
                } else {
                    // negative value means the absolute spacing in points
                    y += -spaceAfter;
                }
            }
        }
        return y - y0;
    }

}