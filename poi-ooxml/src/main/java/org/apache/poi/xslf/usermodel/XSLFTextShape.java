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

import org.apache.poi.POIXMLException;
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
import org.openxmlformats.schemas.presentationml.x2006.main.CTApplicationNonVisualDrawingProps;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a shape that can hold text.
 *
 * @author Yegor Kozlov
 */
@Beta
public abstract class XSLFTextShape extends XSLFSimpleShape implements Iterable<XSLFTextParagraph>{
    private final List<XSLFTextParagraph> _paragraphs;

    /**
     * whether the text was broken into lines.
     */
    private boolean _isTextBroken;

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

    public Iterator<XSLFTextParagraph> iterator(){
        return _paragraphs.iterator();
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

    /**
     *
     * @return text paragraphs in this shape
     */
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
     * A <code>null</code> values unsets this property.
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
     * Sets the botom margin.
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


    /**
     * Specifies that the corresponding shape should be represented by the generating application
     * as a placeholder. When a shape is considered a placeholder by the generating application
     * it can have special properties to alert the user that they may enter content into the shape.
     * Different types of placeholders are allowed and can be specified by using the placeholder
     * type attribute for this element
     *
     * @param placeholder
     */
    public void setPlaceholder(Placeholder placeholder){
        CTShape sh =  (CTShape)getXmlObject();
        CTApplicationNonVisualDrawingProps nv = sh.getNvSpPr().getNvPr();
        if(placeholder == null) {
            if(nv.isSetPh()) nv.unsetPh();
        } else {
            nv.addNewPh().setType(STPlaceholderType.Enum.forInt(placeholder.ordinal() + 1));
        }
    }

    /**
     * Compute the cumulative height occupied by the text
     */
    public double getTextHeight(){
        // dry-run in a 1x1 image and return the vertical advance
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        breakText(graphics);
        return drawParagraphs(graphics, 0, 0);
    }

    /**
     * Adjust the size of the shape so it encompasses the text inside it.
     *
     * @return a <code>Rectangle2D</code> that is the bounds of this shape.
     */
    public Rectangle2D resizeToFitText(){
        Rectangle2D anchor = getAnchor();
        if(anchor.getWidth() == 0.)  throw new POIXMLException(
                "Anchor of the shape was not set.");
        double height = getTextHeight(); 
        height += 1; // add a pixel to compensate rounding errors
        
        anchor.setRect(anchor.getX(), anchor.getY(), anchor.getWidth(), height);
        setAnchor(anchor);
        
        return anchor;
    }   
    
    /**
     * break the contained text into lines
    */
    private void breakText(Graphics2D graphics){
        if(!_isTextBroken) {
            for(XSLFTextParagraph p : _paragraphs) p.breakText(graphics);

            _isTextBroken = true;
        }
    }

    @Override
    public void drawContent(Graphics2D graphics) {
        breakText(graphics);

        RenderableShape rShape = new RenderableShape(this);
        Rectangle2D anchor = rShape.getAnchor(graphics);
        double x = anchor.getX() + getLeftInset();
        double y = anchor.getY();

        // remember the initial transform
        AffineTransform tx = graphics.getTransform();

        // Transform of text in flipped shapes is special.
        // At this point the flip and rotation transform is already applied
        // (see XSLFShape#applyTransform ), but we need to restore it to avoid painting "upside down".
        // See Bugzilla 54210.

        if(getFlipVertical()){
            graphics.translate(anchor.getX(), anchor.getY() + anchor.getHeight());
            graphics.scale(1, -1);
            graphics.translate(-anchor.getX(), -anchor.getY());

            // text in vertically flipped shapes is rotated by 180 degrees
            double centerX = anchor.getX() + anchor.getWidth()/2;
            double centerY = anchor.getY() + anchor.getHeight()/2;
            graphics.translate(centerX, centerY);
            graphics.rotate(Math.toRadians(180));
            graphics.translate(-centerX, -centerY);
        }

        // Horizontal flipping applies only to shape outline and not to the text in the shape.
        // Applying flip second time restores the original not-flipped transform
        if(getFlipHorizontal()){
            graphics.translate(anchor.getX() + anchor.getWidth(), anchor.getY());
            graphics.scale(-1, 1);
            graphics.translate(-anchor.getX() , -anchor.getY());
        }


        // first dry-run to calculate the total height of the text
        double textHeight = getTextHeight();

        switch (getVerticalAlignment()){
            case TOP:
                y += getTopInset();
                break;
            case BOTTOM:
                y += anchor.getHeight() - textHeight - getBottomInset();
                break;
            default:
            case MIDDLE:
                double delta = anchor.getHeight() - textHeight -
                        getTopInset() - getBottomInset();
                y += getTopInset()  + delta/2;
                break;
        }

        drawParagraphs(graphics, x, y);

        // restore the transform
        graphics.setTransform(tx);
    }


    /**
     * paint the paragraphs starting from top left (x,y)
     *
     * @return  the vertical advance, i.e. the cumulative space occupied by the text
     */
    private double drawParagraphs(Graphics2D graphics,  double x, double y) {
        double y0 = y;
        for(int i = 0; i < _paragraphs.size(); i++){
            XSLFTextParagraph p = _paragraphs.get(i);
            List<TextFragment> lines = p.getTextLines();

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

    @Override
    void copy(XSLFShape sh){
        super.copy(sh);

        XSLFTextShape tsh = (XSLFTextShape)sh;

        boolean srcWordWrap = tsh.getWordWrap();
        if(srcWordWrap != getWordWrap()){
            setWordWrap(srcWordWrap);
        }

        double leftInset = tsh.getLeftInset();
        if(leftInset != getLeftInset()) {
            setLeftInset(leftInset);
        }
        double rightInset = tsh.getRightInset();
        if(rightInset != getRightInset()) {
            setRightInset(rightInset);
        }
        double topInset = tsh.getTopInset();
        if(topInset != getTopInset()) {
            setTopInset(topInset);
        }
        double bottomInset = tsh.getBottomInset();
        if(bottomInset != getBottomInset()) {
            setBottomInset(bottomInset);
        }

        VerticalAlignment vAlign = tsh.getVerticalAlignment();
        if(vAlign != getVerticalAlignment()) {
            setVerticalAlignment(vAlign);
        }

        List<XSLFTextParagraph> srcP = tsh.getTextParagraphs();
        List<XSLFTextParagraph> tgtP = getTextParagraphs();
        for(int i = 0; i < srcP.size(); i++){
            XSLFTextParagraph p1 = srcP.get(i);
            XSLFTextParagraph p2 = tgtP.get(i);
            p2.copy(p1);
        }

    }
}