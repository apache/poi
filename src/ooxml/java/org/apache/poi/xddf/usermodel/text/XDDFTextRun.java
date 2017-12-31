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

import java.util.LinkedList;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import org.apache.poi.common.usermodel.fonts.FontGroup;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFFillProperties;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextField;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextLineBreak;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextCapsType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextStrikeType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;

@Beta
public class XDDFTextRun {
    private XDDFTextParagraph _parent;
    private XDDFRunProperties _properties;
    private CTTextLineBreak _tlb;
    private CTTextField _tf;
    private CTRegularTextRun _rtr;

    @Internal
    protected XDDFTextRun(CTTextLineBreak run, XDDFTextParagraph parent) {
        this._tlb = run;
        this._parent = parent;
    }

    @Internal
    protected XDDFTextRun(CTTextField run, XDDFTextParagraph parent) {
        this._tf = run;
        this._parent = parent;
    }

    @Internal
    protected XDDFTextRun(CTRegularTextRun run, XDDFTextParagraph parent) {
        this._rtr = run;
        this._parent = parent;
    }

    public XDDFTextParagraph getParentParagraph() {
        return _parent;
    }

    public boolean isLineBreak() {
        return _tlb != null;
    }

    public boolean isField() {
        return _tf != null;
    }

    public boolean isRegularRun() {
        return _rtr != null;
    }

    public String getText() {
        if (isLineBreak()) {
            return "\n";
        } else if (isField()) {
            return _tf.getT();
        } else {
            return _rtr.getT();
        }
    }

    public void setText(String text) {
        if (isField()) {
            _tf.setT(text);
        } else if (isRegularRun()) {
            _rtr.setT(text);
        }
    }

    public void setDirty(Boolean dirty) {
        getOrCreateProperties().setDirty(dirty);
    }

    public Boolean getDirty() {
        return findDefinedProperty(props -> props.isSetDirty(), props -> props.getDirty())
            .orElse(null);
    }

    public void setSpellError(Boolean error) {
        getOrCreateProperties().setSpellError(error);
    }

    public Boolean getSpellError() {
        return findDefinedProperty(props -> props.isSetErr(), props -> props.getErr())
            .orElse(null);
    }

    public void setNoProof(Boolean noproof) {
        getOrCreateProperties().setNoProof(noproof);
    }

    public Boolean getNoProof() {
        return findDefinedProperty(props -> props.isSetNoProof(), props -> props.getNoProof())
            .orElse(null);
    }

    public void setNormalizeHeights(Boolean normalize) {
        getOrCreateProperties().setNormalizeHeights(normalize);
    }

    public Boolean getNormalizeHeights() {
        return findDefinedProperty(props -> props.isSetNormalizeH(), props -> props.getNormalizeH())
            .orElse(null);
    }

    public void setKumimoji(Boolean kumimoji) {
        getOrCreateProperties().setKumimoji(kumimoji);
    }

    public boolean isKumimoji() {
        return findDefinedProperty(props -> props.isSetKumimoji(), props -> props.getKumimoji())
            .orElse(false);
    }

    /**
     * Specifies whether this run of text will be formatted as bold text.
     *
     * @param bold
     *            whether this run of text will be formatted as bold text.
     */
    public void setBold(Boolean bold) {
        getOrCreateProperties().setBold(bold);
    }

    /**
     * @return whether this run of text is formatted as bold text.
     */
    public boolean isBold() {
        return findDefinedProperty(props -> props.isSetB(), props -> props.getB())
            .orElse(false);
    }

    /**
     * @param italic
     *            whether this run of text is formatted as italic text.
     */
    public void setItalic(Boolean italic) {
        getOrCreateProperties().setItalic(italic);
    }

    /**
     * @return whether this run of text is formatted as italic text.
     */
    public boolean isItalic() {
        return findDefinedProperty(props -> props.isSetI(), props -> props.getI())
            .orElse(false);
    }

    /**
     * @param strike
     *            which strike style this run of text is formatted with.
     */
    public void setStrikeThrough(StrikeType strike) {
        getOrCreateProperties().setStrikeThrough(strike);
    }

    /**
     * @return whether this run of text is formatted as striked text.
     */
    public boolean isStrikeThrough() {
        return findDefinedProperty(props -> props.isSetStrike(), props -> props.getStrike())
            .map(strike -> strike != STTextStrikeType.NO_STRIKE)
            .orElse(false);
    }

    /**
     * @return which strike style this run of text is formatted with.
     */
    public StrikeType getStrikeThrough() {
        return findDefinedProperty(props -> props.isSetStrike(), props -> props.getStrike())
            .map(strike -> StrikeType.valueOf(strike))
            .orElse(null);
    }

    /**
     * @param underline
     *            which underline style this run of text is formatted with.
     */
    public void setUnderline(UnderlineType underline) {
        getOrCreateProperties().setUnderline(underline);
    }

    /**
     * @return whether this run of text is formatted as underlined text.
     */
    public boolean isUnderline() {
        return findDefinedProperty(props -> props.isSetU(), props -> props.getU())
            .map(underline -> underline != STTextUnderlineType.NONE)
            .orElse(false);
    }

    /**
     * @return which underline style this run of text is formatted with.
     */
    public UnderlineType getUnderline() {
        return findDefinedProperty(props -> props.isSetU(), props -> props.getU())
            .map(underline -> UnderlineType.valueOf(underline))
            .orElse(null);
    }

    /**
     * @param caps
     *            which caps style this run of text is formatted with.
     */
    public void setCapitals(CapsType caps) {
        getOrCreateProperties().setCapitals(caps);
    }

    /**
     * @return whether this run of text is formatted as capitalized text.
     */
    public boolean isCapitals() {
        return findDefinedProperty(props -> props.isSetCap(), props -> props.getCap())
            .map(caps -> caps != STTextCapsType.NONE)
            .orElse(false);
    }

    /**
     * @return which caps style this run of text is formatted with.
     */
    public CapsType getCapitals() {
        return findDefinedProperty(props -> props.isSetCap(), props -> props.getCap())
            .map(caps -> CapsType.valueOf(caps))
            .orElse(null);
    }

    /**
     * @return whether a run of text will be formatted as a subscript text.
     *         Default is false.
     */
    public boolean isSubscript() {
        return findDefinedProperty(props -> props.isSetBaseline(), props -> props.getBaseline())
            .map(baseline -> baseline < 0)
            .orElse(false);
    }

    /**
     * @return whether a run of text will be formatted as a superscript text.
     *         Default is false.
     */
    public boolean isSuperscript() {
        return findDefinedProperty(props -> props.isSetBaseline(), props -> props.getBaseline())
            .map(baseline -> baseline > 0)
            .orElse(false);
    }

    /**
     *  Set the baseline for both the superscript and subscript fonts.
     *  <p>
     *     The size is specified using a percentage.
     *     Positive values indicate superscript, negative values indicate subscript.
     *  </p>
     *
     * @param offset
     */
    public void setBaseline(Double offset) {
        if (offset == null) {
            getOrCreateProperties().setBaseline(null);
        } else {
            getOrCreateProperties().setBaseline((int) (offset * 1000));
        }
    }

    /**
     * Set whether the text in this run is formatted as superscript.
     * <p>
     * The size is specified using a percentage.
     * </p>
     *
     * @param offset
     */
    public void setSuperscript(Double offset) {
        setBaseline(offset == null ? null : Math.abs(offset));
    }

    /**
     * Set whether the text in this run is formatted as subscript.
     * <p>
     * The size is specified using a percentage.
     * </p>
     *
     * @param offset
     */
    public void setSubscript(Double offset) {
        setBaseline(offset == null ? null : -Math.abs(offset));
    }

    public void setFillProperties(XDDFFillProperties properties) {
        getOrCreateProperties().setFillProperties(properties);
    }

    public void setFontColor(XDDFColor color) {
        XDDFSolidFillProperties props = new XDDFSolidFillProperties();
        props.setColor(color);
        setFillProperties(props);
    }

    public XDDFColor getFontColor() {
        XDDFSolidFillProperties solid = findDefinedProperty(props -> props.isSetSolidFill(), props -> props.getSolidFill())
            .map(props -> new XDDFSolidFillProperties(props))
            .orElse(new XDDFSolidFillProperties());
        return solid.getColor();
    }

    /**
     * <em>Note</em>: In order to get fonts to unset the property for a given font family use
     * {@link XDDFFont#unsetFontForGroup(FontGroup)}
     *
     * @param fonts
     *            to set or unset on the run.
     */
    public void setFonts(XDDFFont[] fonts) {
        getOrCreateProperties().setFonts(fonts);
    }

    public XDDFFont[] getFonts() {
        LinkedList<XDDFFont> list = new LinkedList<>();

        findDefinedProperty(props -> props.isSetCs(), props -> props.getCs())
            .map(font -> new XDDFFont(FontGroup.COMPLEX_SCRIPT, font))
            .ifPresent(font -> list.add(font));
        findDefinedProperty(props -> props.isSetEa(), props -> props.getEa())
            .map(font -> new XDDFFont(FontGroup.EAST_ASIAN, font))
            .ifPresent(font -> list.add(font));
        findDefinedProperty(props -> props.isSetLatin(), props -> props.getLatin())
            .map(font -> new XDDFFont(FontGroup.LATIN, font))
            .ifPresent(font -> list.add(font));
        findDefinedProperty(props -> props.isSetSym(), props -> props.getSym())
            .map(font -> new XDDFFont(FontGroup.SYMBOL, font))
            .ifPresent(font -> list.add(font));

        return list.toArray(new XDDFFont[list.size()]);
    }

    /**
     * @param size
     *            font size in points. The value <code>null</code> unsets the
     *            size for this run.
     *            <dl>
     *            <dt>Minimum inclusive =</dt>
     *            <dd>1</dd>
     *            <dt>Maximum inclusive =</dt>
     *            <dd>400</dd></dt>
     *
     */
    public void setFontSize(Double size) {
        getOrCreateProperties().setFontSize(size);
    }

    public Double getFontSize() {
        Integer size = findDefinedProperty(props -> props.isSetSz(), props -> props.getSz())
            .orElse(100 * XSSFFont.DEFAULT_FONT_SIZE); // default font size
        double scale = _parent.getParentBody().getBodyProperties().getAutoFit().getFontScale() / 10_000_000.0;
        return size * scale;
    }

    /**
     * Set the kerning of characters within a text run.
     * <p>
     * The value <code>null</code> unsets the kerning for this run.
     * </p>
     *
     * @param kerning
     *            character kerning in points.
     *            <dl>
     *            <dt>Minimum inclusive =</dt>
     *            <dd>0</dd>
     *            <dt>Maximum inclusive =</dt>
     *            <dd>4000</dd></dt>
     */
    public void setCharacterKerning(Double kerning) {
        getOrCreateProperties().setCharacterKerning(kerning);
    }

    /**
     *
     * @return the kerning of characters within a text run,
     * If this attribute is omitted then returns <code>null</code>.
     */
    public Double getCharacterKerning() {
        return findDefinedProperty(props -> props.isSetKern(), props -> props.getKern())
            .map(kerning -> 0.01 * kerning)
            .orElse(null);
    }

    /**
     * Set the spacing between characters within a text run.
     * <p>
     * The spacing is specified in points. Positive values will cause the text to expand,
     * negative values to condense.
     * </p>
     * <p>
     * The value <code>null</code> unsets the spacing for this run.
     * </p>
     *
     * @param spacing
     *            character spacing in points.
     *            <dl>
     *            <dt>Minimum inclusive =</dt>
     *            <dd>-4000</dd>
     *            <dt>Maximum inclusive =</dt>
     *            <dd>4000</dd></dt>
     */
    public void setCharacterSpacing(Double spacing) {
        getOrCreateProperties().setCharacterSpacing(spacing);
    }

    /**
     *
     * @return the spacing between characters within a text run,
     * If this attribute is omitted then returns <code>null</code>.
     */
    public Double getCharacterSpacing() {
        return findDefinedProperty(props -> props.isSetSpc(), props -> props.getSpc())
            .map(spacing -> 0.01 * spacing)
            .orElse(null);
    }

    public void setBookmark(String bookmark) {
        getOrCreateProperties().setBookmark(bookmark);
    }

    public String getBookmark() {
        return findDefinedProperty(props -> props.isSetBmk(), props -> props.getBmk())
            .orElse(null);
    }

    public XDDFHyperlink linkToExternal(String url, PackagePart localPart, POIXMLRelation relation) {
        PackageRelationship rel = localPart.addExternalRelationship(url, relation.getRelation());
        XDDFHyperlink link = new XDDFHyperlink(rel.getId());
        getOrCreateProperties().setHyperlink(link);
        return link;
    }

    public XDDFHyperlink linkToAction(String action) {
        XDDFHyperlink link = new XDDFHyperlink("", action);
        getOrCreateProperties().setHyperlink(link);
        return link;
    }

    public XDDFHyperlink linkToInternal(String action, PackagePart localPart, POIXMLRelation relation, PackagePartName target) {
        PackageRelationship rel = localPart.addRelationship(target, TargetMode.INTERNAL, relation.getRelation());
        XDDFHyperlink link = new XDDFHyperlink(rel.getId(), action);
        getOrCreateProperties().setHyperlink(link);
        return link;
    }

    public XDDFHyperlink getHyperlink() {
        return findDefinedProperty(props -> props.isSetHlinkClick(), props -> props.getHlinkClick())
            .map(link -> new XDDFHyperlink(link))
            .orElse(null);
    }

    public XDDFHyperlink createMouseOver(String action) {
        XDDFHyperlink link = new XDDFHyperlink("", action);
        getOrCreateProperties().setMouseOver(link);
        return link;
    }

    public XDDFHyperlink getMouseOver() {
        return findDefinedProperty(props -> props.isSetHlinkMouseOver(), props -> props.getHlinkMouseOver())
            .map(link -> new XDDFHyperlink(link))
            .orElse(null);
    }

    public void setLanguage(Locale lang) {
        getOrCreateProperties().setLanguage(lang);
    }

    public Locale getLanguage() {
        return findDefinedProperty(props -> props.isSetLang(), props -> props.getLang())
            .map(lang -> Locale.forLanguageTag(lang))
            .orElse(null);
    }

    public void setAlternativeLanguage(Locale lang) {
        getOrCreateProperties().setAlternativeLanguage(lang);
    }

    public Locale getAlternativeLanguage() {
        return findDefinedProperty(props -> props.isSetAltLang(), props -> props.getAltLang())
            .map(lang -> Locale.forLanguageTag(lang))
            .orElse(null);
    }

    public void setHighlight(XDDFColor color) {
        getOrCreateProperties().setHighlight(color);
    }

    public XDDFColor getHighlight() {
        return findDefinedProperty(props -> props.isSetHighlight(), props -> props.getHighlight())
            .map(color -> XDDFColor.forColorContainer(color))
            .orElse(null);
    }

    public void setLineProperties(XDDFLineProperties properties) {
        getOrCreateProperties().setLineProperties(properties);
    }

    public XDDFLineProperties getLineProperties() {
        return findDefinedProperty(props -> props.isSetLn(), props -> props.getLn())
            .map(props -> new XDDFLineProperties(props))
            .orElse(null);
    }

    private <R> Optional<R> findDefinedProperty(Function<CTTextCharacterProperties, Boolean> isSet, Function<CTTextCharacterProperties, R> getter) {
        CTTextCharacterProperties props = getProperties();
        if (props != null && isSet.apply(props)) {
            return Optional.ofNullable(getter.apply(props));
        } else {
            return _parent.findDefinedRunProperty(isSet, getter);
        }
    }

    @Internal
    protected CTTextCharacterProperties getProperties() {
        if (isLineBreak() && _tlb.isSetRPr()) {
            return _tlb.getRPr();
        } else if (isField() && _tf.isSetRPr()) {
            return _tf.getRPr();
        } else if (isRegularRun() && _rtr.isSetRPr()) {
            return _rtr.getRPr();
        }
        return null;
    }

    private XDDFRunProperties getOrCreateProperties() {
        if (_properties == null) {
            if (isLineBreak()) {
                _properties = new XDDFRunProperties(_tlb.isSetRPr() ? _tlb.getRPr() : _tlb.addNewRPr());
            } else if (isField()) {
                _properties = new XDDFRunProperties(_tf.isSetRPr() ? _tf.getRPr() : _tf.addNewRPr());
            } else if (isRegularRun()) {
                _properties = new XDDFRunProperties(_rtr.isSetRPr() ? _rtr.getRPr() : _rtr.addNewRPr());
            }
        }
        return _properties;
    }
}
