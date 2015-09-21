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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.POIXMLException;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawTextShape;
import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.poi.xslf.model.TextBodyPropertyFetcher;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextListStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextVerticalType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextWrappingType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;

/**
 * Represents a shape that can hold text.
 */
@Beta
public abstract class XSLFTextShape extends XSLFSimpleShape
    implements TextShape<XSLFShape,XSLFTextParagraph> {
    private final List<XSLFTextParagraph> _paragraphs;

    @SuppressWarnings("deprecation")
    /*package*/ XSLFTextShape(XmlObject shape, XSLFSheet sheet) {
        super(shape, sheet);

        _paragraphs = new ArrayList<XSLFTextParagraph>();
        CTTextBody txBody = getTextBody(false);
        if (txBody != null) {
            for (CTTextParagraph p : txBody.getPArray()) {
                _paragraphs.add(new XSLFTextParagraph(p, this));
            }
        }
    }

    public Iterator<XSLFTextParagraph> iterator(){
        return getTextParagraphs().iterator();
    }

    /**
     *
     * @return  text contained within this shape or empty string
     */
    public String getText() {
        StringBuilder out = new StringBuilder();
        for (XSLFTextParagraph p : _paragraphs) {
            if (out.length() > 0) out.append('\n');
            out.append(p.getText());
        }
        return out.toString();
    }

    /**
     * unset text from this shape
     */
    public void clearText(){
        _paragraphs.clear();
        CTTextBody txBody = getTextBody(true);
        txBody.setPArray(null); // remove any existing paragraphs
    }

    public void setText(String text){
        clearText();

        addNewTextParagraph().addNewTextRun().setText(text);
    }

    @Override
    public List<XSLFTextParagraph> getTextParagraphs() {
        return _paragraphs;
    }

    /**
     * add a new paragraph run to this shape
     *
     * @return created paragraph run
     */
    public XSLFTextParagraph addNewTextParagraph() {
        CTTextBody txBody = getTextBody(true);
        CTTextParagraph p = txBody.addNewP();
        XSLFTextParagraph paragraph = new XSLFTextParagraph(p, this);
        _paragraphs.add(paragraph);
        return paragraph;
    }


    /**
     * Sets the type of vertical alignment for the text.
     *
     * @param anchor - the type of alignment.
     * A {@code null} values unsets this property.
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
     * @return the type of vertical alignment
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
     * Sets if the paragraphs are horizontal centered
     *
     * @param isCentered true, if the paragraphs are horizontal centered
     * A {@code null} values unsets this property.
     * 
     * @see TextShape#isHorizontalCentered()
     */
    public void setHorizontalCentered(Boolean isCentered){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
             if (isCentered == null) {
                if (bodyPr.isSetAnchorCtr()) bodyPr.unsetAnchorCtr();
            } else {
                bodyPr.setAnchorCtr(isCentered);
            }
        }
    }

    @Override
    public boolean isHorizontalCentered(){
        PropertyFetcher<Boolean> fetcher = new TextBodyPropertyFetcher<Boolean>(){
            public boolean fetch(CTTextBodyProperties props){
                if(props.isSetAnchorCtr()){
                    setValue(props.getAnchorCtr());
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
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
     * @return the bottom inset in points
     */
    public double getBottomInset(){
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
        // If this attribute is omitted, then a value of 0.05 inches is implied
        return fetcher.getValue() == null ? 3.6 : fetcher.getValue();
    }

    /**
     *  Returns the distance (in points) between the left edge of the text frame
     *  and the left edge of the inscribed rectangle of the shape that contains
     *  the text.
     *
     * @return the left inset in points
     */
    public double getLeftInset(){
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
        // If this attribute is omitted, then a value of 0.1 inches is implied
        return fetcher.getValue() == null ? 7.2 : fetcher.getValue();
    }

    /**
     *  Returns the distance (in points) between the right edge of the
     *  text frame and the right edge of the inscribed rectangle of the shape
     *  that contains the text.
     *
     * @return the right inset in points
     */
    public double getRightInset(){
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
        // If this attribute is omitted, then a value of 0.1 inches is implied
        return fetcher.getValue() == null ? 7.2 : fetcher.getValue();
    }

    /**
     *  Returns the distance (in points) between the top of the text frame
     *  and the top of the inscribed rectangle of the shape that contains the text.
     *
     * @return the top inset in points
     */
    public double getTopInset(){
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
        // If this attribute is omitted, then a value of 0.05 inches is implied
        return fetcher.getValue() == null ? 3.6 : fetcher.getValue();
    }

    /**
     * Sets the bottom margin.
     * @see #getBottomInset()
     *
     * @param margin    the bottom margin
     */
    public void setBottomInset(double margin){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(margin == -1) bodyPr.unsetBIns();
            else bodyPr.setBIns(Units.toEMU(margin));
        }
    }

    /**
     * Sets the left margin.
     * @see #getLeftInset()
     *
     * @param margin    the left margin
     */
    public void setLeftInset(double margin){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(margin == -1) bodyPr.unsetLIns();
            else bodyPr.setLIns(Units.toEMU(margin));
        }
    }

    /**
     * Sets the right margin.
     * @see #getRightInset()
     *
     * @param margin    the right margin
     */
    public void setRightInset(double margin){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(margin == -1) bodyPr.unsetRIns();
            else bodyPr.setRIns(Units.toEMU(margin));
        }
    }

    /**
     * Sets the top margin.
     * @see #getTopInset()
     *
     * @param margin    the top margin
     */
    public void setTopInset(double margin){
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if(margin == -1) bodyPr.unsetTIns();
            else bodyPr.setTIns(Units.toEMU(margin));
        }
    }

    @Override
    public Insets2D getInsets() {
        Insets2D insets = new Insets2D(getTopInset(), getLeftInset(), getBottomInset(), getRightInset());
        return insets;
    }
    
    
    /**
     * @return whether to wrap words within the bounding rectangle
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
     *
     * @param wrap  whether to wrap words within the bounding rectangle
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

    @Override
    public void setPlaceholder(Placeholder placeholder) {
        super.setPlaceholder(placeholder);
    }

    public Placeholder getTextType(){
        CTPlaceholder ph = getCTPlaceholder();
        if (ph == null) return null;

        int val = ph.getType().intValue();
        return Placeholder.values()[val - 1];
    }

    @Override
    public double getTextHeight(){
        DrawFactory drawFact = DrawFactory.getInstance(null);
        DrawTextShape dts = drawFact.getDrawable(this);
        return dts.getTextHeight();
    }

    /**
     * Adjust the size of the shape so it encompasses the text inside it.
     *
     * @return a <code>Rectangle2D</code> that is the bounds of this shape.
     */
    public Rectangle resizeToFitText(){
        Rectangle anchor = getAnchor();
        if(anchor.getWidth() == 0.)  throw new POIXMLException(
                "Anchor of the shape was not set.");
        double height = getTextHeight(); 
        height += 1; // add a pixel to compensate rounding errors
        
        anchor.setRect(anchor.getX(), anchor.getY(), anchor.getWidth(), height);
        setAnchor(anchor);
        
        return anchor;
    }   
    

    @Override
    void copy(XSLFShape other){
        super.copy(other);

        XSLFTextShape otherTS = (XSLFTextShape)other;
        CTTextBody otherTB = otherTS.getTextBody(false);
        CTTextBody thisTB = getTextBody(true);
        if (otherTB == null) {
            return;
        }
        
        thisTB.setBodyPr((CTTextBodyProperties)otherTB.getBodyPr().copy());

        if (thisTB.isSetLstStyle()) thisTB.unsetLstStyle();
        if (otherTB.isSetLstStyle()) {
            thisTB.setLstStyle((CTTextListStyle)otherTB.getLstStyle().copy());
        }
        
        boolean srcWordWrap = otherTS.getWordWrap();
        if(srcWordWrap != getWordWrap()){
            setWordWrap(srcWordWrap);
        }

        double leftInset = otherTS.getLeftInset();
        if(leftInset != getLeftInset()) {
            setLeftInset(leftInset);
        }
        double rightInset = otherTS.getRightInset();
        if(rightInset != getRightInset()) {
            setRightInset(rightInset);
        }
        double topInset = otherTS.getTopInset();
        if(topInset != getTopInset()) {
            setTopInset(topInset);
        }
        double bottomInset = otherTS.getBottomInset();
        if(bottomInset != getBottomInset()) {
            setBottomInset(bottomInset);
        }

        VerticalAlignment vAlign = otherTS.getVerticalAlignment();
        if(vAlign != getVerticalAlignment()) {
            setVerticalAlignment(vAlign);
        }

        clearText();
        
        for (XSLFTextParagraph srcP : otherTS.getTextParagraphs()) {
            XSLFTextParagraph tgtP = addNewTextParagraph();
            tgtP.copy(srcP);
        }
    }
}