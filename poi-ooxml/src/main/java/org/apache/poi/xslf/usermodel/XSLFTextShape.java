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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawTextShape;
import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.text.TextContainer;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.apache.poi.xslf.model.PropertyFetcher;
import org.apache.poi.xslf.model.TextBodyPropertyFetcher;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextListStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextVerticalType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextWrappingType;

/**
 * Represents a shape that can hold text.
 */
@Beta
public abstract class XSLFTextShape extends XSLFSimpleShape
    implements TextContainer, TextShape<XSLFShape, XSLFTextParagraph> {
    private final List<XSLFTextParagraph> _paragraphs;

    /* package */ XSLFTextShape(XmlObject shape, XSLFSheet sheet) {
        super(shape, sheet);

        _paragraphs = new ArrayList<>();
        CTTextBody txBody = getTextBody(false);
        if (txBody != null) {
            for (CTTextParagraph p : txBody.getPArray()) {
                _paragraphs.add(newTextParagraph(p));
            }
        }
    }

    @Beta
    public XDDFTextBody getTextBody() {
        CTTextBody txBody = getTextBody(false);
        if (txBody == null) {
            return null;
        }
        return new XDDFTextBody(this, txBody);
    }

    @Override
    public Iterator<XSLFTextParagraph> iterator() {
        return getTextParagraphs().iterator();
    }

    /**
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<XSLFTextParagraph> spliterator() {
        return getTextParagraphs().spliterator();
    }

    @Override
    public String getText() {
        StringBuilder out = new StringBuilder();
        for (XSLFTextParagraph p : _paragraphs) {
            if (out.length() > 0) {
                out.append('\n');
            }
            out.append(p.getText());
        }
        return out.toString();
    }

    /**
     * unset text from this shape
     */
    public void clearText() {
        _paragraphs.clear();
        CTTextBody txBody = getTextBody(true);
        txBody.setPArray(null); // remove any existing paragraphs
    }

    @Override
    public XSLFTextRun setText(String text) {
        // calling clearText or setting to a new Array leads to a
        // XmlValueDisconnectedException
        if (!_paragraphs.isEmpty()) {
            CTTextBody txBody = getTextBody(false);
            int cntPs = txBody.sizeOfPArray();
            for (int i = cntPs; i > 1; i--) {
                txBody.removeP(i - 1);
                _paragraphs.remove(i - 1);
            }

            _paragraphs.get(0).clearButKeepProperties();
        }

        return appendText(text, false);
    }

    @Override
    public XSLFTextRun appendText(String text, boolean newParagraph) {
        if (text == null) {
            return null;
        }

        // copy properties from last paragraph / textrun or paragraph end marker
        CTTextParagraphProperties otherPPr = null;
        CTTextCharacterProperties otherRPr = null;

        boolean firstPara;
        XSLFTextParagraph para;
        if (_paragraphs.isEmpty()) {
            firstPara = false;
            para = null;
        } else {
            firstPara = !newParagraph;
            para = _paragraphs.get(_paragraphs.size() - 1);
            CTTextParagraph ctp = para.getXmlObject();
            otherPPr = ctp.getPPr();
            List<XSLFTextRun> runs = para.getTextRuns();
            if (!runs.isEmpty()) {
                XSLFTextRun r0 = runs.get(runs.size() - 1);
                otherRPr = r0.getRPr(false);
                if (otherRPr == null) {
                    otherRPr = ctp.getEndParaRPr();
                }
            }
            // don't copy endParaRPr to the run in case there aren't any other
            // runs
            // this is the case when setText() was called initially
            // otherwise the master style will be overridden/ignored
        }

        XSLFTextRun run = null;
        for (String lineTxt : text.split("\\r\\n?|\\n")) {
            if (!firstPara) {
                if (para != null) {
                    CTTextParagraph ctp = para.getXmlObject();
                    CTTextCharacterProperties unexpectedRPr = ctp.getEndParaRPr();
                    if (unexpectedRPr != null && unexpectedRPr != otherRPr) {
                        ctp.unsetEndParaRPr();
                    }
                }
                para = addNewTextParagraph();
                if (otherPPr != null) {
                    para.getXmlObject().setPPr(otherPPr);
                }
            }
            boolean firstRun = true;
            for (String runText : lineTxt.split("[\u000b]")) {
                if (!firstRun) {
                    para.addLineBreak();
                }
                run = para.addNewTextRun();
                run.setText(runText);
                if (otherRPr != null) {
                    run.getRPr(true).set(otherRPr);
                }
                firstRun = false;
            }
            firstPara = false;
        }

        assert (run != null);
        return run;
    }

    /**
     * Get the TextParagraphs for this text box. Removing an item from this list will not reliably remove
     * the item from the underlying document. Use <code>removeTextParagraph</code> for that.
     *
     * @return the TextParagraphs for this text box
     */
    @Override
    public List<XSLFTextParagraph> getTextParagraphs() {
        return Collections.unmodifiableList(_paragraphs);
    }

    /**
     * add a new paragraph run to this shape
     *
     * @return created paragraph run
     */
    public XSLFTextParagraph addNewTextParagraph() {
        CTTextBody txBody = getTextBody(false);
        CTTextParagraph p;
        if (txBody == null) {
            txBody = getTextBody(true);
            new XDDFTextBody(this, txBody).initialize();
            p = txBody.getPArray(0);
            p.removeR(0);
        } else {
            p = txBody.addNewP();
        }
        XSLFTextParagraph paragraph = newTextParagraph(p);
        _paragraphs.add(paragraph);
        return paragraph;
    }

    /**
     * @param paragraph paragraph to remove
     * @return whether the paragraph was removed
     * @since POI 5.2.2
     */
    public boolean removeTextParagraph(XSLFTextParagraph paragraph) {
        CTTextParagraph ctTextParagraph = paragraph.getXmlObject();
        CTTextBody txBody = getTextBody(false);
        if (txBody != null) {
            if (_paragraphs.remove(paragraph)) {
                for (int i = 0; i < txBody.sizeOfPArray(); i++) {
                    if (txBody.getPArray(i).equals(ctTextParagraph)) {
                        txBody.removeP(i);
                        return true;
                    }
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public void setVerticalAlignment(VerticalAlignment anchor) {
        CTTextBodyProperties bodyPr = getTextBodyPr(true);
        if (bodyPr != null) {
            if (anchor == null) {
                if (bodyPr.isSetAnchor()) {
                    bodyPr.unsetAnchor();
                }
            } else {
                bodyPr.setAnchor(STTextAnchoringType.Enum.forInt(anchor.ordinal() + 1));
            }
        }
    }

    @Override
    public VerticalAlignment getVerticalAlignment() {
        PropertyFetcher<VerticalAlignment> fetcher = new TextBodyPropertyFetcher<VerticalAlignment>() {
            @Override
            public boolean fetch(CTTextBodyProperties props) {
                if (props.isSetAnchor()) {
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

    @Override
    public void setHorizontalCentered(Boolean isCentered) {
        CTTextBodyProperties bodyPr = getTextBodyPr(true);
        if (bodyPr != null) {
            if (isCentered == null) {
                if (bodyPr.isSetAnchorCtr()) {
                    bodyPr.unsetAnchorCtr();
                }
            } else {
                bodyPr.setAnchorCtr(isCentered);
            }
        }
    }

    @Override
    public boolean isHorizontalCentered() {
        PropertyFetcher<Boolean> fetcher = new TextBodyPropertyFetcher<Boolean>() {
            @Override
            public boolean fetch(CTTextBodyProperties props) {
                if (props.isSetAnchorCtr()) {
                    setValue(props.getAnchorCtr());
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue() != null && fetcher.getValue();
    }

    @Override
    public void setTextDirection(TextDirection orientation) {
        CTTextBodyProperties bodyPr = getTextBodyPr(true);
        if (bodyPr != null) {
            if (orientation == null) {
                if (bodyPr.isSetVert()) {
                    bodyPr.unsetVert();
                }
            } else {
                bodyPr.setVert(STTextVerticalType.Enum.forInt(orientation.ordinal() + 1));
            }
        }
    }

    @Override
    public TextDirection getTextDirection() {
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            STTextVerticalType.Enum val = bodyPr.getVert();
            if (val != null) {
                switch (val.intValue()) {
                default:
                case STTextVerticalType.INT_HORZ:
                    return TextDirection.HORIZONTAL;
                case STTextVerticalType.INT_EA_VERT:
                case STTextVerticalType.INT_MONGOLIAN_VERT:
                case STTextVerticalType.INT_VERT:
                    return TextDirection.VERTICAL;
                case STTextVerticalType.INT_VERT_270:
                    return TextDirection.VERTICAL_270;
                case STTextVerticalType.INT_WORD_ART_VERT_RTL:
                case STTextVerticalType.INT_WORD_ART_VERT:
                    return TextDirection.STACKED;
                }
            }
        }
        return TextDirection.HORIZONTAL;
    }

    @Override
    public Double getTextRotation() {
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null && bodyPr.isSetRot()) {
            return bodyPr.getRot() / 60000.;
        }
        return null;
    }

    @Override
    public void setTextRotation(Double rotation) {
        CTTextBodyProperties bodyPr = getTextBodyPr(true);
        if (bodyPr != null) {
            bodyPr.setRot((int) (rotation * 60000.));
        }
    }

    /**
     * Returns the distance (in points) between the bottom of the text frame and
     * the bottom of the inscribed rectangle of the shape that contains the
     * text.
     *
     * @return the bottom inset in points
     */
    public double getBottomInset() {
        PropertyFetcher<Double> fetcher = new TextBodyPropertyFetcher<Double>() {
            @Override
            public boolean fetch(CTTextBodyProperties props) {
                if (props.isSetBIns()) {
                    double val = Units.toPoints(POIXMLUnits.parseLength(props.xgetBIns()));
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
     * Returns the distance (in points) between the left edge of the text frame
     * and the left edge of the inscribed rectangle of the shape that contains
     * the text.
     *
     * @return the left inset in points
     */
    public double getLeftInset() {
        PropertyFetcher<Double> fetcher = new TextBodyPropertyFetcher<Double>() {
            @Override
            public boolean fetch(CTTextBodyProperties props) {
                if (props.isSetLIns()) {
                    double val = Units.toPoints(POIXMLUnits.parseLength(props.xgetLIns()));
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
     * Returns the distance (in points) between the right edge of the text frame
     * and the right edge of the inscribed rectangle of the shape that contains
     * the text.
     *
     * @return the right inset in points
     */
    public double getRightInset() {
        PropertyFetcher<Double> fetcher = new TextBodyPropertyFetcher<Double>() {
            @Override
            public boolean fetch(CTTextBodyProperties props) {
                if (props.isSetRIns()) {
                    double val = Units.toPoints(POIXMLUnits.parseLength(props.xgetRIns()));
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
     * Returns the distance (in points) between the top of the text frame and
     * the top of the inscribed rectangle of the shape that contains the text.
     *
     * @return the top inset in points
     */
    public double getTopInset() {
        PropertyFetcher<Double> fetcher = new TextBodyPropertyFetcher<Double>() {
            @Override
            public boolean fetch(CTTextBodyProperties props) {
                if (props.isSetTIns()) {
                    double val = Units.toPoints(POIXMLUnits.parseLength(props.xgetTIns()));
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
     *
     * @see #getBottomInset()
     *
     * @param margin
     *            the bottom margin
     */
    public void setBottomInset(double margin) {
        CTTextBodyProperties bodyPr = getTextBodyPr(true);
        if (bodyPr != null) {
            if (margin == -1) {
                bodyPr.unsetBIns();
            } else {
                bodyPr.setBIns(Units.toEMU(margin));
            }
        }
    }

    /**
     * Sets the left margin.
     *
     * @see #getLeftInset()
     *
     * @param margin
     *            the left margin
     */
    public void setLeftInset(double margin) {
        CTTextBodyProperties bodyPr = getTextBodyPr(true);
        if (bodyPr != null) {
            if (margin == -1) {
                bodyPr.unsetLIns();
            } else {
                bodyPr.setLIns(Units.toEMU(margin));
            }
        }
    }

    /**
     * Sets the right margin.
     *
     * @see #getRightInset()
     *
     * @param margin
     *            the right margin
     */
    public void setRightInset(double margin) {
        CTTextBodyProperties bodyPr = getTextBodyPr(true);
        if (bodyPr != null) {
            if (margin == -1) {
                bodyPr.unsetRIns();
            } else {
                bodyPr.setRIns(Units.toEMU(margin));
            }
        }
    }

    /**
     * Sets the top margin.
     *
     * @see #getTopInset()
     *
     * @param margin
     *            the top margin
     */
    public void setTopInset(double margin) {
        CTTextBodyProperties bodyPr = getTextBodyPr(true);
        if (bodyPr != null) {
            if (margin == -1) {
                bodyPr.unsetTIns();
            } else {
                bodyPr.setTIns(Units.toEMU(margin));
            }
        }
    }

    @Override
    public Insets2D getInsets() {
        return new Insets2D(getTopInset(), getLeftInset(), getBottomInset(), getRightInset());
    }

    @Override
    public void setInsets(Insets2D insets) {
        setTopInset(insets.top);
        setLeftInset(insets.left);
        setBottomInset(insets.bottom);
        setRightInset(insets.right);
    }

    @Override
    public boolean getWordWrap() {
        PropertyFetcher<Boolean> fetcher = new TextBodyPropertyFetcher<Boolean>() {
            @Override
            public boolean fetch(CTTextBodyProperties props) {
                if (props.isSetWrap()) {
                    setValue(props.getWrap() == STTextWrappingType.SQUARE);
                    return true;
                }
                return false;
            }
        };
        fetchShapeProperty(fetcher);
        return fetcher.getValue() == null || fetcher.getValue();
    }

    @Override
    public void setWordWrap(boolean wrap) {
        CTTextBodyProperties bodyPr = getTextBodyPr(true);
        if (bodyPr != null) {
            bodyPr.setWrap(wrap ? STTextWrappingType.SQUARE : STTextWrappingType.NONE);
        }
    }

    /**
     *
     * Specifies that a shape should be auto-fit to fully contain the text
     * described within it. Auto-fitting is when text within a shape is scaled
     * in order to contain all the text inside
     *
     * @param value
     *            type of autofit
     */
    public void setTextAutofit(TextAutofit value) {
        CTTextBodyProperties bodyPr = getTextBodyPr(true);
        if (bodyPr != null) {
            if (bodyPr.isSetSpAutoFit()) {
                bodyPr.unsetSpAutoFit();
            }
            if (bodyPr.isSetNoAutofit()) {
                bodyPr.unsetNoAutofit();
            }
            if (bodyPr.isSetNormAutofit()) {
                bodyPr.unsetNormAutofit();
            }

            switch (value) {
            case NONE:
                bodyPr.addNewNoAutofit();
                break;
            case NORMAL:
                bodyPr.addNewNormAutofit();
                break;
            case SHAPE:
                bodyPr.addNewSpAutoFit();
                break;
            }
        }
    }

    /**
     *
     * @return type of autofit
     */
    public TextAutofit getTextAutofit() {
        CTTextBodyProperties bodyPr = getTextBodyPr();
        if (bodyPr != null) {
            if (bodyPr.isSetNoAutofit()) {
                return TextAutofit.NONE;
            } else if (bodyPr.isSetNormAutofit()) {
                return TextAutofit.NORMAL;
            } else if (bodyPr.isSetSpAutoFit()) {
                return TextAutofit.SHAPE;
            }
        }
        return TextAutofit.NORMAL;
    }

    protected CTTextBodyProperties getTextBodyPr() {
        return getTextBodyPr(false);
    }

    protected CTTextBodyProperties getTextBodyPr(boolean create) {
        CTTextBody textBody = getTextBody(create);
        if (textBody == null) {
            return null;
        }
        CTTextBodyProperties textBodyPr = textBody.getBodyPr();
        if (textBodyPr == null && create) {
            textBodyPr = textBody.addNewBodyPr();
        }
        return textBodyPr;
    }

    protected abstract CTTextBody getTextBody(boolean create);

    @Override
    public void setPlaceholder(Placeholder placeholder) {
        super.setPlaceholder(placeholder);
    }

    public Placeholder getTextType() {
        return getPlaceholder();
    }

    @Override
    public double getTextHeight() {
        return getTextHeight(null);
    }

    @Override
    public double getTextHeight(Graphics2D graphics) {
        DrawFactory drawFact = DrawFactory.getInstance(graphics);
        DrawTextShape dts = drawFact.getDrawable(this);
        return dts.getTextHeight(graphics);
    }

    @Override
    public Rectangle2D resizeToFitText() {
        return resizeToFitText(null);
    }

    @Override
    public Rectangle2D resizeToFitText(Graphics2D graphics) {
        Rectangle2D anchor = getAnchor();

        if (anchor.getWidth() == 0.) {
            throw new POIXMLException("Anchor of the shape was not set.");
        }
        double height = getTextHeight(graphics);
        height += 1; // add a pixel to compensate rounding errors

        Insets2D insets = getInsets();
        anchor.setRect(anchor.getX(), anchor.getY(), anchor.getWidth(), height + insets.top + insets.bottom);
        setAnchor(anchor);

        return anchor;
    }

    @Override
    void copy(XSLFShape other) {
        super.copy(other);

        XSLFTextShape otherTS = (XSLFTextShape) other;
        CTTextBody otherTB = otherTS.getTextBody(false);
        if (otherTB == null) {
            return;
        }

        CTTextBody thisTB = getTextBody(true);
        thisTB.setBodyPr((CTTextBodyProperties) otherTB.getBodyPr().copy());

        if (thisTB.isSetLstStyle()) {
            thisTB.unsetLstStyle();
        }
        if (otherTB.isSetLstStyle()) {
            thisTB.setLstStyle((CTTextListStyle) otherTB.getLstStyle().copy());
        }

        boolean srcWordWrap = otherTS.getWordWrap();
        if (srcWordWrap != getWordWrap()) {
            setWordWrap(srcWordWrap);
        }

        double leftInset = otherTS.getLeftInset();
        if (leftInset != getLeftInset()) {
            setLeftInset(leftInset);
        }
        double rightInset = otherTS.getRightInset();
        if (rightInset != getRightInset()) {
            setRightInset(rightInset);
        }
        double topInset = otherTS.getTopInset();
        if (topInset != getTopInset()) {
            setTopInset(topInset);
        }
        double bottomInset = otherTS.getBottomInset();
        if (bottomInset != getBottomInset()) {
            setBottomInset(bottomInset);
        }

        VerticalAlignment vAlign = otherTS.getVerticalAlignment();
        if (vAlign != getVerticalAlignment()) {
            setVerticalAlignment(vAlign);
        }

        clearText();

        for (XSLFTextParagraph srcP : otherTS.getTextParagraphs()) {
            XSLFTextParagraph tgtP = addNewTextParagraph();
            tgtP.copy(srcP);
        }
    }

    @Override
    public void setTextPlaceholder(TextPlaceholder placeholder) {
        switch (placeholder) {
        default:
        case NOTES:
        case HALF_BODY:
        case QUARTER_BODY:
        case BODY:
            setPlaceholder(Placeholder.BODY);
            break;
        case TITLE:
            setPlaceholder(Placeholder.TITLE);
            break;
        case CENTER_BODY:
            setPlaceholder(Placeholder.BODY);
            setHorizontalCentered(true);
            break;
        case CENTER_TITLE:
            setPlaceholder(Placeholder.CENTERED_TITLE);
            break;
        case OTHER:
            setPlaceholder(Placeholder.CONTENT);
            break;
        }
    }

    @Override
    public TextPlaceholder getTextPlaceholder() {
        Placeholder ph = getTextType();
        if (ph == null) {
            return TextPlaceholder.BODY;
        }
        switch (ph) {
        case BODY:
            return TextPlaceholder.BODY;
        case TITLE:
            return TextPlaceholder.TITLE;
        case CENTERED_TITLE:
            return TextPlaceholder.CENTER_TITLE;
        default:
        case CONTENT:
            return TextPlaceholder.OTHER;
        }
    }

    /**
     * Helper method to allow subclasses to provide their own text paragraph
     *
     * @param p
     *            the xml reference
     *
     * @return a new text paragraph
     *
     * @since POI 3.15-beta2
     */
    protected XSLFTextParagraph newTextParagraph(CTTextParagraph p) {
        return new XSLFTextParagraph(p, this);
    }

    @Override
    public <R> Optional<R> findDefinedParagraphProperty(Predicate<CTTextParagraphProperties> isSet,
        Function<CTTextParagraphProperties, R> getter) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public <R> Optional<R> findDefinedRunProperty(Predicate<CTTextCharacterProperties> isSet,
        Function<CTTextCharacterProperties, R> getter) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

}
