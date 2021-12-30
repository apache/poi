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

package org.apache.poi.xddf.usermodel.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.collections4.iterators.IteratorIterable;
import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextField;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextLineBreak;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextSpacing;

/**
 * Represents a paragraph of text within the containing text body. The paragraph
 * is the highest level text separation mechanism.
 */
@Beta
public class XDDFTextParagraph implements Iterable<XDDFTextRun> {
    private XDDFTextBody _parent;
    private XDDFParagraphProperties _properties;
    private final CTTextParagraph _p;
    private final ArrayList<XDDFTextRun> _runs;

    @Internal
    protected XDDFTextParagraph(CTTextParagraph paragraph, XDDFTextBody parent) {
        this._p = paragraph;
        this._parent = parent;

        final int count = paragraph.sizeOfBrArray() + paragraph.sizeOfFldArray() + paragraph.sizeOfRArray();
        this._runs = new ArrayList<>(count);

        for (XmlObject xo : paragraph.selectChildren(QNameSet.ALL)) {
            if (xo instanceof CTTextLineBreak) {
                _runs.add(new XDDFTextRun((CTTextLineBreak) xo, this));
            } else if (xo instanceof CTTextField) {
                _runs.add(new XDDFTextRun((CTTextField) xo, this));
            } else if (xo instanceof CTRegularTextRun) {
                _runs.add(new XDDFTextRun((CTRegularTextRun) xo, this));
            }
        }

        addDefaultRunProperties();
        addAfterLastRunProperties();
    }

    public void setText(String text) {
        // keep the properties of current last run
        XmlObject existing = null;
        if (!_runs.isEmpty()) {
            existing = _runs.get(_runs.size() - 1).getProperties().copy();
        }
        // remove all runs
        for (int i = _p.sizeOfBrArray() - 1; i >= 0; i--) {
            _p.removeBr(i);
        }
        for (int i = _p.sizeOfFldArray() - 1; i >= 0; i--) {
            _p.removeFld(i);
        }
        for (int i = _p.sizeOfRArray() - 1; i >= 0; i--) {
            _p.removeR(i);
        }
        _runs.clear();
        XDDFTextRun run = appendRegularRun(text);
        if (existing != null) {
            run.getProperties().set(existing);
        }
    }

    public String getText() {
        StringBuilder out = new StringBuilder();
        for (XDDFTextRun r : _runs) {
            out.append(r.getText());
        }
        return out.toString();
    }

    public XDDFTextBody getParentBody() {
        return _parent;
    }

    public List<XDDFTextRun> getTextRuns() {
        return _runs;
    }

    @Override
    public Iterator<XDDFTextRun> iterator() {
        return _runs.iterator();
    }

    /**
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<XDDFTextRun> spliterator() {
        return _runs.spliterator();
    }

    /**
     * Append a line break.
     *
     * @return text run representing this line break ('\n').
     */
    public XDDFTextRun appendLineBreak() {
        CTTextLineBreak br = _p.addNewBr();
        // by default, line break has the font properties of the last text run
        for (XDDFTextRun tr : new IteratorIterable<>(new ReverseListIterator<>(_runs))) {
            CTTextCharacterProperties prevProps = tr.getProperties();
            // let's find one which is not undefined
            if (prevProps != null) {
                br.setRPr((CTTextCharacterProperties) prevProps.copy());
                break;
            }
        }
        XDDFTextRun run = new XDDFTextRun(br, this);
        _runs.add(run);
        return run;
    }

    /**
     * Append a new text field.
     *
     * @return the new text field.
     */
    public XDDFTextRun appendField(String id, String type, String text) {
        CTTextField f = _p.addNewFld();
        f.setId(id);
        f.setType(type);
        f.setT(text);
        CTTextCharacterProperties rPr = f.addNewRPr();
        rPr.setLang(LocaleUtil.getUserLocale().toLanguageTag());
        XDDFTextRun run = new XDDFTextRun(f, this);
        _runs.add(run);
        return run;
    }

    /**
     * Append a new run of text.
     *
     * @return the new run of text.
     */
    public XDDFTextRun appendRegularRun(String text) {
        CTRegularTextRun r = _p.addNewR();
        r.setT(text);
        CTTextCharacterProperties rPr = r.addNewRPr();
        rPr.setLang(LocaleUtil.getUserLocale().toLanguageTag());
        XDDFTextRun run = new XDDFTextRun(r, this);
        _runs.add(run);
        return run;
    }

    /**
     * Returns the paragraph indentation level.
     *
     * @return indentation level of the paragraph.
     */
    public int getIndentationLevel() {
        if (_p.isSetPPr()) {
            return getProperties().getLevel();
        } else {
            return 0;
        }
    }

    /**
     * Specifies the paragraph indentation level, between 1 and 9.
     *
     * @param level
     *            new indentation level for the paragraph.
     *            Use <code>null</code> to unset the indentation level.
     */
    public void setIndentationLevel(Integer level) {
        if (_p.isSetPPr()) {
            getProperties().setLevel(level);
        }
    }

    /**
     * Returns the alignment that is applied to the paragraph.
     *
     * If this attribute is omitted, then a value of left is implied.
     *
     * @return alignment that is applied to the paragraph
     */
    public TextAlignment getTextAlignment() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetAlgn,
                CTTextParagraphProperties::getAlgn)
            .map(TextAlignment::valueOf).orElse(null);
    }

    /**
     * Specifies the alignment that is to be applied to the paragraph. Possible
     * values for this include left, right, centered, justified and distributed,
     *
     * @param align
     *            text alignment
     */
    public void setTextAlignment(TextAlignment align) {
        if (align != null || _p.isSetPPr()) {
            getOrCreateProperties().setTextAlignment(align);
        }
    }

    /**
     * Returns where vertically on a line of text the actual words are
     * positioned. This deals with vertical placement of the characters with
     * respect to the baselines.
     *
     * If this attribute is omitted, then a value of baseline is implied.
     *
     * @return alignment that is applied to the paragraph
     */
    public FontAlignment getFontAlignment() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetFontAlgn,
                CTTextParagraphProperties::getFontAlgn)
            .map(FontAlignment::valueOf).orElse(null);
    }

    /**
     * Determines where vertically on a line of text the actual words are
     * positioned. This deals with vertical placement of the characters with
     * respect to the baselines. For instance having text anchored to the top
     * baseline, anchored to the bottom baseline, centered in between, etc.
     *
     * @param align
     *            text font alignment
     */
    public void setFontAlignment(FontAlignment align) {
        if (align != null || _p.isSetPPr()) {
            getOrCreateProperties().setFontAlignment(align);
        }
    }

    /**
     *
     * @return the indentation, in points, applied to the first line of text in
     *         the paragraph.
     */
    public Double getIndentation() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetIndent,
                CTTextParagraphProperties::getIndent)
            .map(Units::toPoints).orElse(null);
    }

    /**
     * Specifies the indentation size that will be applied to the first line of
     * text in the paragraph.
     *
     * @param points
     *            the indentation in points. The value <code>null</code> unsets
     *            the indentation for this paragraph.
     *            <dl>
     *            <dt>Minimum inclusive =</dt>
     *            <dd>-4032</dd>
     *            <dt>Maximum inclusive =</dt>
     *            <dd>4032</dd>
     *            </dl>
     */
    public void setIndentation(Double points) {
        if (points != null || _p.isSetPPr()) {
            getOrCreateProperties().setIndentation(points);
        }
    }

    /**
     *
     * @return the left margin, in points, of the paragraph.
     */
    public Double getMarginLeft() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetMarL,
                CTTextParagraphProperties::getMarL)
            .map(Units::toPoints).orElse(null);
    }

    /**
     * Specifies the left margin of the paragraph. This is specified in addition
     * to the text body inset and applies only to this text paragraph. That is
     * the text body inset and the LeftMargin attributes are additive with
     * respect to the text position.
     *
     * @param points
     *            the margin in points. The value <code>null</code> unsets the
     *            left margin for this paragraph.
     *            <dl>
     *            <dt>Minimum inclusive =</dt>
     *            <dd>0</dd>
     *            <dt>Maximum inclusive =</dt>
     *            <dd>4032</dd>
     *            </dl>
     */
    public void setMarginLeft(Double points) {
        if (points != null || _p.isSetPPr()) {
            getOrCreateProperties().setMarginLeft(points);
        }
    }

    /**
     *
     * @return the right margin, in points, of the paragraph.
     */
    public Double getMarginRight() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetMarR,
                CTTextParagraphProperties::getMarR)
            .map(Units::toPoints).orElse(null);
    }

    /**
     * Specifies the right margin of the paragraph. This is specified in
     * addition to the text body inset and applies only to this text paragraph.
     * That is the text body inset and the RightMargin attributes are additive
     * with respect to the text position.
     *
     * @param points
     *            the margin in points. The value <code>null</code> unsets the
     *            right margin for this paragraph.
     *            <dl>
     *            <dt>Minimum inclusive =</dt>
     *            <dd>0</dd>
     *            <dt>Maximum inclusive =</dt>
     *            <dd>4032</dd>
     *            </dl>
     */
    public void setMarginRight(Double points) {
        if (points != null || _p.isSetPPr()) {
            getOrCreateProperties().setMarginRight(points);
        }
    }

    /**
     *
     * @return the default size for a tab character within this paragraph in
     *         points.
     */
    public Double getDefaultTabSize() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetDefTabSz,
                CTTextParagraphProperties::xgetDefTabSz)
            .map(POIXMLUnits::parseLength).map(Units::toPoints).orElse(null);
    }

    /**
     * Specifies the default size for a tab character within this paragraph.
     *
     * @param points
     *            the default tab size in points. The value <code>null</code>
     *            unsets the default tab size for this paragraph.
     */
    public void setDefaultTabSize(Double points) {
        if (points != null || _p.isSetPPr()) {
            getOrCreateProperties().setDefaultTabSize(points);
        }
    }

    /**
     * Returns the vertical line spacing that is to be used within a paragraph.
     * This may be specified in two different ways, percentage spacing or font
     * points spacing:
     * <p>
     * If line spacing is a percentage of normal line height, result is instance
     * of XDDFSpacingPercent. If line spacing is expressed in points, result is
     * instance of XDDFSpacingPoints.
     * </p>
     *
     * @return the vertical line spacing.
     */
    public XDDFSpacing getLineSpacing() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetLnSpc,
                CTTextParagraphProperties::getLnSpc)
            .map(this::extractSpacing).orElse(null);

    }

    /**
     * This element specifies the vertical line spacing that is to be used
     * within a paragraph. This may be specified in two different ways,
     * percentage spacing or font points spacing:
     * <p>
     * If spacing is instance of XDDFSpacingPercent, then line spacing is a
     * percentage of normal line height. If spacing is instance of
     * XDDFSpacingPoints, then line spacing is expressed in points.
     * </p>
     * Examples:
     *
     * <pre>
     * <code>
     *      // spacing will be 120% of the size of the largest text on each line
     *      paragraph.setLineSpacing(new XDDFSpacingPercent(120));
     *
     *      // spacing will be 200% of the size of the largest text on each line
     *      paragraph.setLineSpacing(new XDDFSpacingPercent(200));
     *
     *      // spacing will be 48 points
     *      paragraph.setLineSpacing(new XDDFSpacingPoints(48.0));
     * </code>
     * </pre>
     *
     * @param linespacing
     *            the vertical line spacing
     */
    public void setLineSpacing(XDDFSpacing linespacing) {
        if (linespacing != null || _p.isSetPPr()) {
            getOrCreateProperties().setLineSpacing(linespacing);
        }
    }

    /**
     * The amount of vertical white space before the paragraph. This may be
     * specified in two different ways, percentage spacing or font points
     * spacing:
     * <p>
     * If spacing is a percentage of normal line height, result is instance of
     * XDDFSpacingPercent. If spacing is expressed in points, result is instance
     * of XDDFSpacingPoints.
     * </p>
     *
     * @return the vertical white space before the paragraph.
     */
    public XDDFSpacing getSpaceBefore() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetSpcBef,
                CTTextParagraphProperties::getSpcBef)
            .map(this::extractSpacing).orElse(null);
    }

    /**
     * Set the amount of vertical white space that will be present before the
     * paragraph. This may be specified in two different ways, percentage
     * spacing or font points spacing:
     * <p>
     * If spacing is instance of XDDFSpacingPercent, then spacing is a
     * percentage of normal line height. If spacing is instance of
     * XDDFSpacingPoints, then spacing is expressed in points.
     * </p>
     * Examples:
     *
     * <pre>
     * <code>
     *      // The paragraph will be formatted to have a spacing before the paragraph text.
     *      // The spacing will be 200% of the size of the largest text on each line
     *      paragraph.setSpaceBefore(new XDDFSpacingPercent(200));
     *
     *      // The spacing will be a size of 48 points
     *      paragraph.setSpaceBefore(new XDDFSpacingPoints(48.0));
     * </code>
     * </pre>
     *
     * @param spaceBefore
     *            the vertical white space before the paragraph.
     */
    public void setSpaceBefore(XDDFSpacing spaceBefore) {
        if (spaceBefore != null || _p.isSetPPr()) {
            getOrCreateProperties().setSpaceBefore(spaceBefore);
        }
    }

    /**
     * The amount of vertical white space after the paragraph. This may be
     * specified in two different ways, percentage spacing or font points
     * spacing:
     * <p>
     * If spacing is a percentage of normal line height, result is instance of
     * XDDFSpacingPercent. If spacing is expressed in points, result is instance
     * of XDDFSpacingPoints.
     * </p>
     *
     * @return the vertical white space after the paragraph.
     */
    public XDDFSpacing getSpaceAfter() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetSpcAft,
                CTTextParagraphProperties::getSpcAft)
            .map(this::extractSpacing).orElse(null);
    }

    /**
     * Set the amount of vertical white space that will be present after the
     * paragraph. This may be specified in two different ways, percentage
     * spacing or font points spacing:
     * <p>
     * If spacing is instance of XDDFSpacingPercent, then spacing is a
     * percentage of normal line height. If spacing is instance of
     * XDDFSpacingPoints, then spacing is expressed in points.
     * </p>
     * Examples:
     *
     * <pre>
     * <code>
     *      // The paragraph will be formatted to have a spacing after the paragraph text.
     *      // The spacing will be 200% of the size of the largest text on each line
     *      paragraph.setSpaceAfter(new XDDFSpacingPercent(200));
     *
     *      // The spacing will be a size of 48 points
     *      paragraph.setSpaceAfter(new XDDFSpacingPoints(48.0));
     * </code>
     * </pre>
     *
     * @param spaceAfter
     *            the vertical white space after the paragraph.
     */
    public void setSpaceAfter(XDDFSpacing spaceAfter) {
        if (spaceAfter != null || _p.isSetPPr()) {
            getOrCreateProperties().setSpaceAfter(spaceAfter);
        }
    }

    /**
     *
     * @return the color of bullet characters within a given paragraph. A
     *         <code>null</code> value means to use the text font color.
     */
    public XDDFColor getBulletColor() {
        return findDefinedParagraphProperty(props -> props.isSetBuClr() || props.isSetBuClrTx(),
            props -> new XDDFParagraphBulletProperties(props).getBulletColor()).orElse(null);
    }

    /**
     * Set the color to be used on bullet characters within a given paragraph.
     *
     * @param color
     *            the bullet color
     */
    public void setBulletColor(XDDFColor color) {
        if (color != null || _p.isSetPPr()) {
            getOrCreateBulletProperties().setBulletColor(color);
        }
    }

    /**
     * Specifies the color to be used on bullet characters has to follow text
     * color within a given paragraph.
     */
    public void setBulletColorFollowText() {
        getOrCreateBulletProperties().setBulletColorFollowText();
    }

    /**
     *
     * @return the font of bullet characters within a given paragraph. A
     *         <code>null</code> value means to use the text font font.
     */
    public XDDFFont getBulletFont() {
        return findDefinedParagraphProperty(props -> props.isSetBuFont() || props.isSetBuFontTx(),
            props -> new XDDFParagraphBulletProperties(props).getBulletFont()).orElse(null);
    }

    /**
     * Set the font to be used on bullet characters within a given paragraph.
     *
     * @param font
     *            the bullet font
     */
    public void setBulletFont(XDDFFont font) {
        if (font != null || _p.isSetPPr()) {
            getOrCreateBulletProperties().setBulletFont(font);
        }
    }

    /**
     * Specifies the font to be used on bullet characters has to follow text
     * font within a given paragraph.
     */
    public void setBulletFontFollowText() {
        getOrCreateBulletProperties().setBulletFontFollowText();
    }

    /**
     * Returns the bullet size that is to be used within a paragraph. This may
     * be specified in three different ways, follows text size, percentage size
     * and font points size:
     * <p>
     * If returned value is instance of XDDFBulletSizeFollowText, then bullet
     * size is text size; If returned value is instance of
     * XDDFBulletSizePercent, then bullet size is a percentage of the font size;
     * If returned value is instance of XDDFBulletSizePoints, then bullet size
     * is specified in points.
     * </p>
     *
     * @return the bullet size
     */
    public XDDFBulletSize getBulletSize() {
        return findDefinedParagraphProperty(
            props -> props.isSetBuSzPct() || props.isSetBuSzPts() || props.isSetBuSzTx(),
            props -> new XDDFParagraphBulletProperties(props).getBulletSize()).orElse(null);
    }

    /**
     * Sets the bullet size that is to be used within a paragraph. This may be
     * specified in three different ways, follows text size, percentage size and
     * font points size:
     * <p>
     * If given value is instance of XDDFBulletSizeFollowText, then bullet size
     * is text size; If given value is instance of XDDFBulletSizePercent, then
     * bullet size is a percentage of the font size; If given value is instance
     * of XDDFBulletSizePoints, then bullet size is specified in points.
     * </p>
     *
     * @param size
     *            the bullet size specification
     */
    public void setBulletSize(XDDFBulletSize size) {
        if (size != null || _p.isSetPPr()) {
            getOrCreateBulletProperties().setBulletSize(size);
        }
    }

    public XDDFBulletStyle getBulletStyle() {
        return findDefinedParagraphProperty(
            props -> props.isSetBuAutoNum() || props.isSetBuBlip() || props.isSetBuChar() || props.isSetBuNone(),
            props -> new XDDFParagraphBulletProperties(props).getBulletStyle()).orElse(null);
    }

    public void setBulletStyle(XDDFBulletStyle style) {
        if (style != null || _p.isSetPPr()) {
            getOrCreateBulletProperties().setBulletStyle(style);
        }
    }

    public boolean hasEastAsianLineBreak() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetEaLnBrk,
                CTTextParagraphProperties::getEaLnBrk)
            .orElse(false);
    }

    public void setEastAsianLineBreak(Boolean value) {
        if (value != null || _p.isSetPPr()) {
            getOrCreateProperties().setEastAsianLineBreak(value);
        }
    }

    public boolean hasLatinLineBreak() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetLatinLnBrk,
                CTTextParagraphProperties::getLatinLnBrk)
            .orElse(false);
    }

    public void setLatinLineBreak(Boolean value) {
        if (value != null || _p.isSetPPr()) {
            getOrCreateProperties().setLatinLineBreak(value);
        }
    }

    public boolean hasHangingPunctuation() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetHangingPunct,
                CTTextParagraphProperties::getHangingPunct)
            .orElse(false);
    }

    public void setHangingPunctuation(Boolean value) {
        if (value != null || _p.isSetPPr()) {
            getOrCreateProperties().setHangingPunctuation(value);
        }
    }

    public boolean isRightToLeft() {
        return findDefinedParagraphProperty(
                CTTextParagraphProperties::isSetRtl,
                CTTextParagraphProperties::getRtl)
            .orElse(false);
    }

    public void setRightToLeft(Boolean value) {
        if (value != null || _p.isSetPPr()) {
            getOrCreateProperties().setRightToLeft(value);
        }
    }

    public XDDFTabStop addTabStop() {
        return getOrCreateProperties().addTabStop();
    }

    public XDDFTabStop insertTabStop(int index) {
        return getOrCreateProperties().insertTabStop(index);
    }

    public void removeTabStop(int index) {
        if (_p.isSetPPr()) {
            getProperties().removeTabStop(index);
        }
    }

    public XDDFTabStop getTabStop(int index) {
        if (_p.isSetPPr()) {
            return getProperties().getTabStop(index);
        } else {
            return null;
        }
    }

    public List<XDDFTabStop> getTabStops() {
        if (_p.isSetPPr()) {
            return getProperties().getTabStops();
        } else {
            return Collections.emptyList();
        }
    }

    public int countTabStops() {
        if (_p.isSetPPr()) {
            return getProperties().countTabStops();
        } else {
            return 0;
        }
    }

    public XDDFParagraphBulletProperties getOrCreateBulletProperties() {
        return getOrCreateProperties().getBulletProperties();
    }

    public XDDFParagraphBulletProperties getBulletProperties() {
        if (_p.isSetPPr()) {
            return getProperties().getBulletProperties();
        } else {
            return null;
        }
    }

    /**
     * @since 4.0.1
     */
    public XDDFRunProperties addDefaultRunProperties() {
        return getOrCreateProperties().addDefaultRunProperties();
    }

    public XDDFRunProperties getDefaultRunProperties() {
        if (_p.isSetPPr()) {
            return getProperties().getDefaultRunProperties();
        } else {
            return null;
        }
    }

    public void setDefaultRunProperties(XDDFRunProperties properties) {
        if (properties != null || _p.isSetPPr()) {
            getOrCreateProperties().setDefaultRunProperties(properties);
        }
    }

    public XDDFRunProperties addAfterLastRunProperties() {
        if (!_p.isSetEndParaRPr()) {
            _p.addNewEndParaRPr();
        }
        return getAfterLastRunProperties();
    }

    public XDDFRunProperties getAfterLastRunProperties() {
        if (_p.isSetEndParaRPr()) {
            return new XDDFRunProperties(_p.getEndParaRPr());
        } else {
            return null;
        }
    }

    public void setAfterLastRunProperties(XDDFRunProperties properties) {
        if (properties == null) {
            if (_p.isSetEndParaRPr()) {
                _p.unsetEndParaRPr();
            }
        } else {
            _p.setEndParaRPr(properties.getXmlObject());
        }
    }

    private XDDFSpacing extractSpacing(CTTextSpacing spacing) {
        if (spacing.isSetSpcPct()) {
            double scale = 1 - _parent.getBodyProperties().getAutoFit().getLineSpaceReduction() / 100_000.0;
            return new XDDFSpacingPercent(spacing, spacing.getSpcPct(), scale);
        } else if (spacing.isSetSpcPts()) {
            return new XDDFSpacingPoints(spacing, spacing.getSpcPts());
        }
        return null;
    }

    private XDDFParagraphProperties getProperties() {
        if (_properties == null) {
            _properties = new XDDFParagraphProperties(_p.getPPr());
        }
        return _properties;
    }

    private XDDFParagraphProperties getOrCreateProperties() {
        if (!_p.isSetPPr()) {
            _properties = new XDDFParagraphProperties(_p.addNewPPr());
        }
        return getProperties();
    }

    protected <R> Optional<R> findDefinedParagraphProperty(Predicate<CTTextParagraphProperties> isSet,
        Function<CTTextParagraphProperties, R> getter) {
        if (_p.isSetPPr()) {
            int level = _p.getPPr().isSetLvl() ? 1 + _p.getPPr().getLvl() : 0;
            return findDefinedParagraphProperty(isSet, getter, level);
        } else {
            return _parent.findDefinedParagraphProperty(isSet, getter, 0);
        }
    }

    private <R> Optional<R> findDefinedParagraphProperty(Predicate<CTTextParagraphProperties> isSet,
        Function<CTTextParagraphProperties, R> getter, int level) {
        final CTTextParagraphProperties props = _p.getPPr();
        if (props != null && isSet.test(props)) {
            return Optional.ofNullable(getter.apply(props));
        } else {
            return _parent.findDefinedParagraphProperty(isSet, getter, level);
        }
    }

    protected <R> Optional<R> findDefinedRunProperty(Predicate<CTTextCharacterProperties> isSet,
        Function<CTTextCharacterProperties, R> getter) {
        if (_p.isSetPPr()) {
            int level = _p.getPPr().isSetLvl() ? 1 + _p.getPPr().getLvl() : 0;
            return findDefinedRunProperty(isSet, getter, level);
        } else {
            return _parent.findDefinedRunProperty(isSet, getter, 0);
        }
    }

    private <R> Optional<R> findDefinedRunProperty(Predicate<CTTextCharacterProperties> isSet,
        Function<CTTextCharacterProperties, R> getter, int level) {
        final CTTextCharacterProperties props = _p.getPPr().isSetDefRPr() ? _p.getPPr().getDefRPr() : null;
        if (props != null && isSet.test(props)) {
            return Optional.ofNullable(getter.apply(props));
        } else {
            return _parent.findDefinedRunProperty(isSet, getter, level);
        }
    }
}
