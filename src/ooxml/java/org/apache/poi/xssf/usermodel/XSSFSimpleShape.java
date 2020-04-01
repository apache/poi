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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.SimpleShape;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFColorRgbBinary;
import org.apache.poi.xddf.usermodel.XDDFFillProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.text.TextContainer;
import org.apache.poi.xddf.usermodel.text.XDDFRunProperties;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.apache.poi.xddf.usermodel.text.XDDFTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetGeometry2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextFont;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.STShapeType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextHorzOverflowType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextVertOverflowType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextVerticalType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextWrappingType;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShapeNonVisual;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnderlineValues;

/**
 * Represents a shape with a predefined geometry in a SpreadsheetML drawing.
 * Possible shape types are defined in
 * {@link org.apache.poi.ss.usermodel.ShapeTypes}
 */
public class XSSFSimpleShape extends XSSFShape implements Iterable<XSSFTextParagraph>, SimpleShape, TextContainer {
    /**
     * The text body containing the paragraphs for this shape.
     */
    private final XDDFTextBody _textBody;
    /**
     * List of the paragraphs that make up the text in this shape
     */
    private final List<XSSFTextParagraph> _paragraphs;
    /**
     * A default instance of CTShape used for creating new shapes.
     */
    private static CTShape prototype;

    /**
     * Xml bean that stores properties of this shape
     */
    private CTShape ctShape;

    protected XSSFSimpleShape(XSSFDrawing drawing, CTShape ctShape) {
        this.drawing = drawing;
        this.ctShape = ctShape;

        _paragraphs = new ArrayList<>();

        // initialize any existing paragraphs - this will be the default body
        // paragraph in a new shape,
        // or existing paragraphs that have been loaded from the file
        CTTextBody body = ctShape.getTxBody();
        if (body == null) {
            _textBody = null;
        } else {
            _textBody = new XDDFTextBody(this, body);
            for (int i = 0; i < body.sizeOfPArray(); i++) {
                _paragraphs.add(new XSSFTextParagraph(body.getPArray(i), ctShape));
            }
        }
    }

    /**
     * Prototype with the default structure of a new auto-shape.
     */
    protected static CTShape prototype() {
        if (prototype == null) {
            CTShape shape = CTShape.Factory.newInstance();

            CTShapeNonVisual nv = shape.addNewNvSpPr();
            CTNonVisualDrawingProps nvp = nv.addNewCNvPr();
            nvp.setId(1);
            nvp.setName("Shape 1");
            nv.addNewCNvSpPr();

            CTShapeProperties sp = shape.addNewSpPr();
            CTTransform2D t2d = sp.addNewXfrm();
            CTPositiveSize2D p1 = t2d.addNewExt();
            p1.setCx(0);
            p1.setCy(0);
            CTPoint2D p2 = t2d.addNewOff();
            p2.setX(0);
            p2.setY(0);

            CTPresetGeometry2D geom = sp.addNewPrstGeom();
            geom.setPrst(STShapeType.RECT);
            geom.addNewAvLst();

            XDDFTextBody body = new XDDFTextBody(null, shape.addNewTxBody());
            XDDFTextParagraph p = body.initialize();
            XDDFRunProperties rp = p.getAfterLastRunProperties();
            XDDFColor black = new XDDFColorRgbBinary(new byte[] { 0, 0, 0 });
            XDDFFillProperties fp = new XDDFSolidFillProperties(black);
            rp.setFillProperties(fp);

            prototype = shape;
        }
        return prototype;
    }

    @Internal
    public CTShape getCTShape() {
        return ctShape;
    }

    @Beta
    public XDDFTextBody getTextBody() {
        return _textBody;
    }

    protected void setXfrm(CTTransform2D t2d) {
        ctShape.getSpPr().setXfrm(t2d);
    }

    @Override
    public Iterator<XSSFTextParagraph> iterator() {
        return _paragraphs.iterator();
    }

    /**
     * Returns the text from all paragraphs in the shape. Paragraphs are
     * separated by new lines.
     *
     * @return text contained within this shape or empty string
     */
    public String getText() {
        final int MAX_LEVELS = 9;
        StringBuilder out = new StringBuilder();
        List<Integer> levelCount = new ArrayList<>(MAX_LEVELS); // maximum 9
                                                                // levels
        // initialise the levelCount array - this maintains a record of the
        // numbering to be used at each level
        for (int k = 0; k < MAX_LEVELS; k++) {
            levelCount.add(0);
        }

        for (int i = 0; i < _paragraphs.size(); i++) {
            if (out.length() > 0) {
                out.append('\n');
            }
            XSSFTextParagraph p = _paragraphs.get(i);

            if (p.isBullet() && p.getText().length() > 0) {

                int level = Math.min(p.getLevel(), MAX_LEVELS - 1);

                if (p.isBulletAutoNumber()) {
                    i = processAutoNumGroup(i, level, levelCount, out);
                } else {
                    // indent appropriately for the level
                    for (int j = 0; j < level; j++) {
                        out.append('\t');
                    }
                    String character = p.getBulletCharacter();
                    out.append(character.length() > 0 ? character + " " : "- ");
                    out.append(p.getText());
                }
            } else {
                out.append(p.getText());

                // this paragraph is not a bullet, so reset the count array
                for (int k = 0; k < MAX_LEVELS; k++) {
                    levelCount.set(k, 0);
                }
            }
        }

        return out.toString();
    }

    /**
     *
     */
    private int processAutoNumGroup(int index, int level, List<Integer> levelCount, StringBuilder out) {
        XSSFTextParagraph p = _paragraphs.get(index);

        // The rules for generating the auto numbers are as follows. If the
        // following paragraph is also
        // an auto-number, has the same type/scheme (and startAt if defined on
        // this paragraph) then they are
        // considered part of the same group. An empty bullet paragraph is
        // counted as part of the same
        // group but does not increment the count for the group. A change of
        // type, startAt or the paragraph
        // not being a bullet resets the count for that level to 1.

        // first auto-number paragraph so initialise to 1 or the bullets startAt
        // if present
        int startAt = p.getBulletAutoNumberStart();
        ListAutoNumber scheme = p.getBulletAutoNumberScheme();
        if (levelCount.get(level) == 0) {
            levelCount.set(level, startAt == 0 ? 1 : startAt);
        }
        // indent appropriately for the level
        for (int j = 0; j < level; j++) {
            out.append('\t');
        }
        if (p.getText().length() > 0) {
            out.append(getBulletPrefix(scheme, levelCount.get(level)));
            out.append(p.getText());
        }
        while (true) {
            XSSFTextParagraph nextp = (index + 1) == _paragraphs.size() ? null : _paragraphs.get(index + 1);
            if (nextp == null) {
                break; // out of paragraphs
            }
            if (!(nextp.isBullet() && p.isBulletAutoNumber())) {
                break; // not an auto-number bullet
            }
            if (nextp.getLevel() > level) {
                // recurse into the new level group
                if (out.length() > 0) {
                    out.append('\n');
                }
                index = processAutoNumGroup(index + 1, nextp.getLevel(), levelCount, out);
                continue; // restart the loop given the new index
            } else if (nextp.getLevel() < level) {
                break; // changed level
            }
            ListAutoNumber nextScheme = nextp.getBulletAutoNumberScheme();
            int nextStartAt = nextp.getBulletAutoNumberStart();

            if (nextScheme == scheme && nextStartAt == startAt) {
                // bullet is valid, so increment i
                ++index;
                if (out.length() > 0) {
                    out.append('\n');
                }
                // indent for the level
                for (int j = 0; j < level; j++) {
                    out.append('\t');
                }
                // check for empty text - only output a bullet if there is text,
                // but it is still part of the group
                if (nextp.getText().length() > 0) {
                    // increment the count for this level
                    levelCount.set(level, levelCount.get(level) + 1);
                    out.append(getBulletPrefix(nextScheme, levelCount.get(level)));
                    out.append(nextp.getText());
                }
            } else {
                // something doesn't match so stop
                break;
            }
        }
        // end of the group so reset the count for this level
        levelCount.set(level, 0);

        return index;
    }

    /**
     * Returns a string containing an appropriate prefix for an auto-numbering
     * bullet
     *
     * @param scheme
     *            the auto-numbering scheme used by the bullet
     * @param value
     *            the value of the bullet
     * @return appropriate prefix for an auto-numbering bullet
     */
    private String getBulletPrefix(ListAutoNumber scheme, int value) {
        StringBuilder out = new StringBuilder();

        switch (scheme) {
        case ALPHA_LC_PARENT_BOTH:
        case ALPHA_LC_PARENT_R:
            if (scheme == ListAutoNumber.ALPHA_LC_PARENT_BOTH) {
                out.append('(');
            }
            out.append(valueToAlpha(value).toLowerCase(Locale.ROOT));
            out.append(')');
            break;
        case ALPHA_UC_PARENT_BOTH:
        case ALPHA_UC_PARENT_R:
            if (scheme == ListAutoNumber.ALPHA_UC_PARENT_BOTH) {
                out.append('(');
            }
            out.append(valueToAlpha(value));
            out.append(')');
            break;
        case ALPHA_LC_PERIOD:
            out.append(valueToAlpha(value).toLowerCase(Locale.ROOT));
            out.append('.');
            break;
        case ALPHA_UC_PERIOD:
            out.append(valueToAlpha(value));
            out.append('.');
            break;
        case ARABIC_PARENT_BOTH:
        case ARABIC_PARENT_R:
            if (scheme == ListAutoNumber.ARABIC_PARENT_BOTH) {
                out.append('(');
            }
            out.append(value);
            out.append(')');
            break;
        case ARABIC_PERIOD:
            out.append(value);
            out.append('.');
            break;
        case ARABIC_PLAIN:
            out.append(value);
            break;
        case ROMAN_LC_PARENT_BOTH:
        case ROMAN_LC_PARENT_R:
            if (scheme == ListAutoNumber.ROMAN_LC_PARENT_BOTH) {
                out.append('(');
            }
            out.append(valueToRoman(value).toLowerCase(Locale.ROOT));
            out.append(')');
            break;
        case ROMAN_UC_PARENT_BOTH:
        case ROMAN_UC_PARENT_R:
            if (scheme == ListAutoNumber.ROMAN_UC_PARENT_BOTH) {
                out.append('(');
            }
            out.append(valueToRoman(value));
            out.append(')');
            break;
        case ROMAN_LC_PERIOD:
            out.append(valueToRoman(value).toLowerCase(Locale.ROOT));
            out.append('.');
            break;
        case ROMAN_UC_PERIOD:
            out.append(valueToRoman(value));
            out.append('.');
            break;
        default:
            out.append('\u2022'); // can't set the font to wingdings so use the
                                  // default bullet character
            break;
        }
        out.append(" ");
        return out.toString();
    }

    /**
     * Convert an integer to its alpha equivalent e.g. 1 = A, 2 = B, 27 = AA etc
     */
    private String valueToAlpha(int value) {
        String alpha = "";
        int modulo;
        while (value > 0) {
            modulo = (value - 1) % 26;
            alpha = (char) (65 + modulo) + alpha;
            value = (value - modulo) / 26;
        }
        return alpha;
    }

    private static String[] _romanChars = new String[] { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V",
            "IV", "I" };
    private static int[] _romanAlphaValues = new int[] { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };

    /**
     * Convert an integer to its roman equivalent e.g. 1 = I, 9 = IX etc
     */
    private String valueToRoman(int value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; value > 0 && i < _romanChars.length; i++) {
            while (_romanAlphaValues[i] <= value) {
                out.append(_romanChars[i]);
                value -= _romanAlphaValues[i];
            }
        }
        return out.toString();
    }

    /**
     * Clear all text from this shape
     */
    public void clearText() {
        _paragraphs.clear();
        CTTextBody txBody = ctShape.getTxBody();
        txBody.setPArray(null); // remove any existing paragraphs
    }

    /**
     * Set a single paragraph of text on the shape. Note this will replace all
     * existing paragraphs created on the shape.
     *
     * @param text
     *            string representing the paragraph text
     */
    public void setText(String text) {
        clearText();

        addNewTextParagraph().addNewTextRun().setText(text);
    }

    /**
     * Set a single paragraph of text on the shape. Note this will replace all
     * existing paragraphs created on the shape.
     *
     * @param str
     *            rich text string representing the paragraph text
     */
    public void setText(XSSFRichTextString str) {

        XSSFWorkbook wb = (XSSFWorkbook) getDrawing().getParent().getParent();
        str.setStylesTableReference(wb.getStylesSource());

        CTTextParagraph p = CTTextParagraph.Factory.newInstance();
        if (str.numFormattingRuns() == 0) {
            CTRegularTextRun r = p.addNewR();
            CTTextCharacterProperties rPr = r.addNewRPr();
            rPr.setLang("en-US");
            rPr.setSz(1100);
            r.setT(str.getString());

        } else {
            for (int i = 0; i < str.getCTRst().sizeOfRArray(); i++) {
                CTRElt lt = str.getCTRst().getRArray(i);
                CTRPrElt ltPr = lt.getRPr();
                if (ltPr == null) {
                    ltPr = lt.addNewRPr();
                }

                CTRegularTextRun r = p.addNewR();
                CTTextCharacterProperties rPr = r.addNewRPr();
                rPr.setLang("en-US");

                applyAttributes(ltPr, rPr);

                r.setT(lt.getT());
            }
        }

        clearText();
        ctShape.getTxBody().setPArray(new CTTextParagraph[] { p });
        _paragraphs.add(new XSSFTextParagraph(ctShape.getTxBody().getPArray(0), ctShape));
    }

    /**
     * Returns a collection of the XSSFTextParagraphs that are attached to this
     * shape
     *
     * @return text paragraphs in this shape
     */
    public List<XSSFTextParagraph> getTextParagraphs() {
        return _paragraphs;
    }

    /**
     * Add a new paragraph run to this shape
     *
     * @return created paragraph run
     */
    public XSSFTextParagraph addNewTextParagraph() {
        CTTextBody txBody = ctShape.getTxBody();
        CTTextParagraph p = txBody.addNewP();
        XSSFTextParagraph paragraph = new XSSFTextParagraph(p, ctShape);
        _paragraphs.add(paragraph);
        return paragraph;
    }

    /**
     * Add a new paragraph run to this shape, set to the provided string
     *
     * @return created paragraph run
     */
    public XSSFTextParagraph addNewTextParagraph(String text) {
        XSSFTextParagraph paragraph = addNewTextParagraph();
        paragraph.addNewTextRun().setText(text);
        return paragraph;
    }

    /**
     * Add a new paragraph run to this shape, set to the provided rich text
     * string
     *
     * @return created paragraph run
     */
    public XSSFTextParagraph addNewTextParagraph(XSSFRichTextString str) {
        CTTextBody txBody = ctShape.getTxBody();
        CTTextParagraph p = txBody.addNewP();

        if (str.numFormattingRuns() == 0) {
            CTRegularTextRun r = p.addNewR();
            CTTextCharacterProperties rPr = r.addNewRPr();
            rPr.setLang("en-US");
            rPr.setSz(1100);
            r.setT(str.getString());

        } else {
            for (int i = 0; i < str.getCTRst().sizeOfRArray(); i++) {
                CTRElt lt = str.getCTRst().getRArray(i);
                CTRPrElt ltPr = lt.getRPr();
                if (ltPr == null) {
                    ltPr = lt.addNewRPr();
                }

                CTRegularTextRun r = p.addNewR();
                CTTextCharacterProperties rPr = r.addNewRPr();
                rPr.setLang("en-US");

                applyAttributes(ltPr, rPr);

                r.setT(lt.getT());
            }
        }

        // Note: the XSSFTextParagraph constructor will create its required
        // XSSFTextRuns from the provided CTTextParagraph
        XSSFTextParagraph paragraph = new XSSFTextParagraph(p, ctShape);
        _paragraphs.add(paragraph);

        return paragraph;
    }

    /**
     * Sets the type of horizontal overflow for the text.
     *
     * @param overflow
     *            - the type of horizontal overflow. A <code>null</code> values
     *            unsets this property.
     */
    public void setTextHorizontalOverflow(TextHorizontalOverflow overflow) {
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
        if (bodyPr != null) {
            if (overflow == null) {
                if (bodyPr.isSetHorzOverflow()) {
                    bodyPr.unsetHorzOverflow();
                }
            } else {
                bodyPr.setHorzOverflow(STTextHorzOverflowType.Enum.forInt(overflow.ordinal() + 1));
            }
        }
    }

    /**
     * Returns the type of horizontal overflow for the text.
     *
     * @return the type of horizontal overflow
     */
    public TextHorizontalOverflow getTextHorizontalOverflow() {
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
        if (bodyPr != null) {
            if (bodyPr.isSetHorzOverflow()) {
                return TextHorizontalOverflow.values()[bodyPr.getHorzOverflow().intValue() - 1];
            }
        }
        return TextHorizontalOverflow.OVERFLOW;
    }

    /**
     * Sets the type of vertical overflow for the text.
     *
     * @param overflow
     *            - the type of vertical overflow. A <code>null</code> values
     *            unsets this property.
     */
    public void setTextVerticalOverflow(TextVerticalOverflow overflow) {
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
        if (bodyPr != null) {
            if (overflow == null) {
                if (bodyPr.isSetVertOverflow()) {
                    bodyPr.unsetVertOverflow();
                }
            } else {
                bodyPr.setVertOverflow(STTextVertOverflowType.Enum.forInt(overflow.ordinal() + 1));
            }
        }
    }

    /**
     * Returns the type of vertical overflow for the text.
     *
     * @return the type of vertical overflow
     */
    public TextVerticalOverflow getTextVerticalOverflow() {
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
        if (bodyPr != null) {
            if (bodyPr.isSetVertOverflow()) {
                return TextVerticalOverflow.values()[bodyPr.getVertOverflow().intValue() - 1];
            }
        }
        return TextVerticalOverflow.OVERFLOW;
    }

    /**
     * Sets the type of vertical alignment for the text within the shape.
     *
     * @param anchor
     *            - the type of alignment. A <code>null</code> values unsets
     *            this property.
     */
    public void setVerticalAlignment(VerticalAlignment anchor) {
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
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

    /**
     * Returns the type of vertical alignment for the text within the shape.
     *
     * @return the type of vertical alignment
     */
    public VerticalAlignment getVerticalAlignment() {
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
        if (bodyPr != null) {
            if (bodyPr.isSetAnchor()) {
                return VerticalAlignment.values()[bodyPr.getAnchor().intValue() - 1];
            }
        }
        return VerticalAlignment.TOP;
    }

    /**
     * Sets the vertical orientation of the text
     *
     * @param orientation
     *            vertical orientation of the text A <code>null</code> values
     *            unsets this property.
     */
    public void setTextDirection(TextDirection orientation) {
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
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

    /**
     * Gets the vertical orientation of the text
     *
     * @return vertical orientation of the text
     */
    public TextDirection getTextDirection() {
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
        if (bodyPr != null) {
            STTextVerticalType.Enum val = bodyPr.getVert();
            if (val != null) {
                return TextDirection.values()[val.intValue() - 1];
            }
        }
        return TextDirection.HORIZONTAL;
    }

    /**
     * Returns the distance (in points) between the bottom of the text frame and
     * the bottom of the inscribed rectangle of the shape that contains the
     * text.
     *
     * @return the bottom inset in points
     */
    public double getBottomInset() {
        Double inset = _textBody.getBodyProperties().getBottomInset();
        if (inset == null) {
            // If this attribute is omitted, then a value of 0.05 inches is
            // implied
            return 3.6;
        } else {
            return inset;
        }
    }

    /**
     * Returns the distance (in points) between the left edge of the text frame
     * and the left edge of the inscribed rectangle of the shape that contains
     * the text.
     *
     * @return the left inset in points
     */
    public double getLeftInset() {
        Double inset = _textBody.getBodyProperties().getLeftInset();
        if (inset == null) {
            // If this attribute is omitted, then a value of 0.05 inches is
            // implied
            return 3.6;
        } else {
            return inset;
        }
    }

    /**
     * Returns the distance (in points) between the right edge of the text frame
     * and the right edge of the inscribed rectangle of the shape that contains
     * the text.
     *
     * @return the right inset in points
     */
    public double getRightInset() {
        Double inset = _textBody.getBodyProperties().getRightInset();
        if (inset == null) {
            // If this attribute is omitted, then a value of 0.05 inches is
            // implied
            return 3.6;
        } else {
            return inset;
        }
    }

    /**
     * Returns the distance (in points) between the top of the text frame and
     * the top of the inscribed rectangle of the shape that contains the text.
     *
     * @return the top inset in points
     */
    public double getTopInset() {
        Double inset = _textBody.getBodyProperties().getTopInset();
        if (inset == null) {
            // If this attribute is omitted, then a value of 0.05 inches is
            // implied
            return 3.6;
        } else {
            return inset;
        }
    }

    /**
     * Sets the bottom inset.
     *
     * @see #getBottomInset()
     *
     * @param margin
     *            the bottom margin
     */
    public void setBottomInset(double margin) {
        if (margin == -1) {
            _textBody.getBodyProperties().setBottomInset(null);
        } else {
            _textBody.getBodyProperties().setBottomInset(margin);
        }
    }

    /**
     * Sets the left inset.
     *
     * @see #getLeftInset()
     *
     * @param margin
     *            the left margin
     */
    public void setLeftInset(double margin) {
        if (margin == -1) {
            _textBody.getBodyProperties().setLeftInset(null);
        } else {
            _textBody.getBodyProperties().setLeftInset(margin);
        }
    }

    /**
     * Sets the right inset.
     *
     * @see #getRightInset()
     *
     * @param margin
     *            the right margin
     */
    public void setRightInset(double margin) {
        if (margin == -1) {
            _textBody.getBodyProperties().setRightInset(null);
        } else {
            _textBody.getBodyProperties().setRightInset(margin);
        }
    }

    /**
     * Sets the top inset.
     *
     * @see #getTopInset()
     *
     * @param margin
     *            the top margin
     */
    public void setTopInset(double margin) {
        if (margin == -1) {
            _textBody.getBodyProperties().setTopInset(null);
        } else {
            _textBody.getBodyProperties().setTopInset(margin);
        }
    }

    /**
     * @return whether to wrap words within the bounding rectangle
     */
    public boolean getWordWrap() {
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
        if (bodyPr != null) {
            if (bodyPr.isSetWrap()) {
                return bodyPr.getWrap() == STTextWrappingType.SQUARE;
            }
        }
        return true;
    }

    /**
     *
     * @param wrap
     *            whether to wrap words within the bounding rectangle
     */
    public void setWordWrap(boolean wrap) {
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
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
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
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
        CTTextBodyProperties bodyPr = ctShape.getTxBody().getBodyPr();
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

    /**
     * Gets the shape type, one of the constants defined in
     * {@link org.apache.poi.ss.usermodel.ShapeTypes}.
     *
     * @return the shape type
     * @see org.apache.poi.ss.usermodel.ShapeTypes
     */
    public int getShapeType() {
        return ctShape.getSpPr().getPrstGeom().getPrst().intValue();
    }

    /**
     * Sets the shape types.
     *
     * @param type
     *            the shape type, one of the constants defined in
     *            {@link org.apache.poi.ss.usermodel.ShapeTypes}.
     * @see org.apache.poi.ss.usermodel.ShapeTypes
     */
    public void setShapeType(int type) {
        ctShape.getSpPr().getPrstGeom().setPrst(STShapeType.Enum.forInt(type));
    }

    @Override
    protected CTShapeProperties getShapeProperties() {
        return ctShape.getSpPr();
    }

    /**
     * org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt to
     * org.openxmlformats.schemas.drawingml.x2006.main.CTFont adapter
     */
    private static void applyAttributes(CTRPrElt pr, CTTextCharacterProperties rPr) {

        if (pr.sizeOfBArray() > 0) {
            rPr.setB(pr.getBArray(0).getVal());
        }
        if (pr.sizeOfUArray() > 0) {
            STUnderlineValues.Enum u1 = pr.getUArray(0).getVal();
            if (u1 == STUnderlineValues.SINGLE) {
                rPr.setU(STTextUnderlineType.SNG);
            } else if (u1 == STUnderlineValues.DOUBLE) {
                rPr.setU(STTextUnderlineType.DBL);
            } else if (u1 == STUnderlineValues.NONE) {
                rPr.setU(STTextUnderlineType.NONE);
            }
        }
        if (pr.sizeOfIArray() > 0) {
            rPr.setI(pr.getIArray(0).getVal());
        }

        if (pr.sizeOfRFontArray() > 0) {
            CTTextFont rFont = rPr.isSetLatin() ? rPr.getLatin() : rPr.addNewLatin();
            rFont.setTypeface(pr.getRFontArray(0).getVal());
        }

        if (pr.sizeOfSzArray() > 0) {
            int sz = (int) (pr.getSzArray(0).getVal() * 100);
            rPr.setSz(sz);
        }

        if (pr.sizeOfColorArray() > 0) {
            CTSolidColorFillProperties fill = rPr.isSetSolidFill() ? rPr.getSolidFill() : rPr.addNewSolidFill();
            org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor xlsColor = pr.getColorArray(0);
            if (xlsColor.isSetRgb()) {
                CTSRgbColor clr = fill.isSetSrgbClr() ? fill.getSrgbClr() : fill.addNewSrgbClr();
                clr.setVal(xlsColor.getRgb());
            } else if (xlsColor.isSetIndexed()) {
                HSSFColor indexed = HSSFColor.getIndexHash().get((int) xlsColor.getIndexed());
                if (indexed != null) {
                    byte[] rgb = new byte[3];
                    rgb[0] = (byte) indexed.getTriplet()[0];
                    rgb[1] = (byte) indexed.getTriplet()[1];
                    rgb[2] = (byte) indexed.getTriplet()[2];
                    CTSRgbColor clr = fill.isSetSrgbClr() ? fill.getSrgbClr() : fill.addNewSrgbClr();
                    clr.setVal(rgb);
                }
            }
        }
    }

    @Override
    public String getShapeName() {
        return ctShape.getNvSpPr().getCNvPr().getName();
    }

    @Override
    public int getShapeId() {
        return (int) ctShape.getNvSpPr().getCNvPr().getId();
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
