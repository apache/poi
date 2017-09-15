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
package org.apache.poi.xslf.usermodel;

import java.awt.Color;

import org.apache.poi.common.usermodel.fonts.FontCharset;
import org.apache.poi.common.usermodel.fonts.FontFamily;
import org.apache.poi.common.usermodel.fonts.FontGroup;
import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.common.usermodel.fonts.FontPitch;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.util.Beta;
import org.apache.poi.xslf.model.CharacterPropertyFetcher;
import org.apache.poi.xslf.usermodel.XSLFPropertiesDelegate.XSLFFillProperties;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTFontCollection;
import org.openxmlformats.schemas.drawingml.x2006.main.CTFontScheme;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextField;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextFont;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextLineBreak;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextNormalAutofit;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextStrikeType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;

/**
 * Represents a run of text within the containing text body. The run element is the
 * lowest level text separation mechanism within a text body.
 */
@Beta
public class XSLFTextRun implements TextRun {
    private final XmlObject _r;
    private final XSLFTextParagraph _p;

    protected XSLFTextRun(XmlObject r, XSLFTextParagraph p){
        _r = r;
        _p = p;
        if (!(r instanceof CTRegularTextRun || r instanceof CTTextLineBreak || r instanceof CTTextField)) {
            throw new OpenXML4JRuntimeException("unsupported text run of type "+r.getClass());
        }
    }

    XSLFTextParagraph getParentParagraph(){
        return _p;
    }

    @Override
    public String getRawText(){
        if (_r instanceof CTTextField) {
            return ((CTTextField)_r).getT();
        } else if (_r instanceof CTTextLineBreak) {
            return "\n";
        }
        return ((CTRegularTextRun)_r).getT();
    }

    String getRenderableText(){
        if (_r instanceof CTTextField) {
            CTTextField tf = (CTTextField)_r;
            XSLFSheet sheet = _p.getParentShape().getSheet();
            if ("slidenum".equals(tf.getType()) && sheet instanceof XSLFSlide) {
                return Integer.toString(((XSLFSlide)sheet).getSlideNumber());
            }
            return tf.getT();
        } else if (_r instanceof CTTextLineBreak) {
            return "\n";
        }


        String txt = ((CTRegularTextRun)_r).getT();
        TextCap cap = getTextCap();
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < txt.length(); i++) {
            char c = txt.charAt(i);
            if(c == '\t') {
                // TODO: finish support for tabs
                buf.append("  ");
            } else {
                switch (cap){
                    case ALL:
                        buf.append(Character.toUpperCase(c));
                        break;
                    case SMALL:
                        buf.append(Character.toLowerCase(c));
                        break;
                    default:
                        buf.append(c);
                }
            }
        }

        return buf.toString();
    }

    @Override
    public void setText(String text){
        if (_r instanceof CTTextField) {
            ((CTTextField)_r).setT(text);
        } else if (_r instanceof CTTextLineBreak) {
            // ignored
            return;
        } else {
            ((CTRegularTextRun)_r).setT(text);
        }
    }

    /**
     * Return the text run xmlbeans object.
     * Depending on the type of text run, this can be {@link CTTextField},
     * {@link CTTextLineBreak} or usually a {@link CTRegularTextRun}
     *
     * @return the xmlbeans object
     */
    public XmlObject getXmlObject(){
        return _r;
    }

    @Override
    public void setFontColor(Color color) {
        setFontColor(DrawPaint.createSolidPaint(color));
    }

    @Override
    public void setFontColor(PaintStyle color) {
        if (!(color instanceof SolidPaint)) {
            throw new IllegalArgumentException("Currently only SolidPaint is supported!");
        }
        SolidPaint sp = (SolidPaint)color;
        Color c = DrawPaint.applyColorTransform(sp.getSolidColor());

        CTTextCharacterProperties rPr = getRPr(true);
        CTSolidColorFillProperties fill = rPr.isSetSolidFill() ? rPr.getSolidFill() : rPr.addNewSolidFill();

        XSLFColor col = new XSLFColor(fill, getParentParagraph().getParentShape().getSheet().getTheme(), fill.getSchemeClr());
        col.setColor(c);
    }

    @Override
    public PaintStyle getFontColor(){
        final boolean hasPlaceholder = getParentParagraph().getParentShape().getPlaceholder() != null;
        CharacterPropertyFetcher<PaintStyle> fetcher = new CharacterPropertyFetcher<PaintStyle>(_p.getIndentLevel()){
            @Override
            public boolean fetch(CTTextCharacterProperties props){
                if (props == null) {
                    return false;
                }

                XSLFShape shape = _p.getParentShape();
                CTShapeStyle style = shape.getSpStyle();
                CTSchemeColor phClr = null;
                if (style != null && style.getFontRef() != null) {
                    phClr = style.getFontRef().getSchemeClr();
                }

                XSLFFillProperties fp = XSLFPropertiesDelegate.getFillDelegate(props);
                XSLFSheet sheet = shape.getSheet();
                PackagePart pp = sheet.getPackagePart();
                XSLFTheme theme = sheet.getTheme();
                PaintStyle ps = XSLFShape.selectPaint(fp, phClr, pp, theme, hasPlaceholder);

                if (ps != null)  {
                    setValue(ps);
                    return true;
                }

                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue();
    }

    @Override
    public void setFontSize(Double fontSize){
        CTTextCharacterProperties rPr = getRPr(true);
        if(fontSize == null) {
            if (rPr.isSetSz()) {
                rPr.unsetSz();
            }
        } else {
            if (fontSize < 1.0) {
                throw new IllegalArgumentException("Minimum font size is 1pt but was " + fontSize);
            }

            rPr.setSz((int)(100*fontSize));
        }
    }

    @Override
    public Double getFontSize(){
        double scale = 1;
        CTTextNormalAutofit afit = getParentParagraph().getParentShape().getTextBodyPr().getNormAutofit();
        if(afit != null) {
            scale = (double)afit.getFontScale() / 100000;
        }

        CharacterPropertyFetcher<Double> fetcher = new CharacterPropertyFetcher<Double>(_p.getIndentLevel()){
            @Override
            public boolean fetch(CTTextCharacterProperties props){
                if (props != null && props.isSetSz()) {
                    setValue(props.getSz()*0.01);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? null : fetcher.getValue()*scale;
    }

    /**
     *
     * @return the spacing between characters within a text run,
     * If this attribute is omitted than a value of 0 or no adjustment is assumed.
     */
    public double getCharacterSpacing(){

        CharacterPropertyFetcher<Double> fetcher = new CharacterPropertyFetcher<Double>(_p.getIndentLevel()){
            @Override
            public boolean fetch(CTTextCharacterProperties props){
                if (props != null && props.isSetSpc()) {
                    setValue(props.getSpc()*0.01);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? 0 : fetcher.getValue();
    }

    /**
     * Set the spacing between characters within a text run.
     * <p>
     * The spacing is specified in points. Positive values will cause the text to expand,
     * negative values to condense.
     * </p>
     *
     * @param spc  character spacing in points.
     */
    public void setCharacterSpacing(double spc){
        CTTextCharacterProperties rPr = getRPr(true);
        if(spc == 0.0) {
            if(rPr.isSetSpc()) {
                rPr.unsetSpc();
            }
        } else {
            rPr.setSpc((int)(100*spc));
        }
    }

    @Override
    public void setFontFamily(String typeface) {
        FontGroup fg = FontGroup.getFontGroupFirst(getRawText());
        new XSLFFontInfo(fg).setTypeface(typeface);
    }

    @Override
    public void setFontFamily(String typeface, FontGroup fontGroup) {
        new XSLFFontInfo(fontGroup).setTypeface(typeface);
    }

    @Override
    public void setFontInfo(FontInfo fontInfo, FontGroup fontGroup) {
        new XSLFFontInfo(fontGroup).copyFrom(fontInfo);
    }

    @Override
    public String getFontFamily() {
        FontGroup fg = FontGroup.getFontGroupFirst(getRawText());
        return new XSLFFontInfo(fg).getTypeface();
    }

    @Override
    public String getFontFamily(FontGroup fontGroup) {
        return new XSLFFontInfo(fontGroup).getTypeface();
    }

    @Override
    public FontInfo getFontInfo(FontGroup fontGroup) {
        XSLFFontInfo fontInfo = new XSLFFontInfo(fontGroup);
        return (fontInfo.getTypeface() != null) ? fontInfo : null;
    }

    @Override
    public byte getPitchAndFamily(){
        FontGroup fg = FontGroup.getFontGroupFirst(getRawText());
        XSLFFontInfo fontInfo = new XSLFFontInfo(fg);
        FontPitch pitch = fontInfo.getPitch();
        if (pitch == null) {
            pitch = FontPitch.VARIABLE;
        }
        FontFamily family = fontInfo.getFamily();
        if (family == null) {
            family = FontFamily.FF_SWISS;
        }
        return FontPitch.getNativeId(pitch, family);
    }

    @Override
    public void setStrikethrough(boolean strike) {
        getRPr(true).setStrike(strike ? STTextStrikeType.SNG_STRIKE : STTextStrikeType.NO_STRIKE);
    }

    @Override
    public boolean isStrikethrough() {
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getIndentLevel()){
            @Override
            public boolean fetch(CTTextCharacterProperties props){
                if(props != null && props.isSetStrike()) {
                    setValue(props.getStrike() != STTextStrikeType.NO_STRIKE);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    @Override
    public boolean isSuperscript() {
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getIndentLevel()){
            @Override
            public boolean fetch(CTTextCharacterProperties props){
                if (props != null && props.isSetBaseline()) {
                    setValue(props.getBaseline() > 0);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    /**
     *  Set the baseline for both the superscript and subscript fonts.
     *  <p>
     *     The size is specified using a percentage.
     *     Positive values indicate superscript, negative values indicate subscript.
     *  </p>
     *
     * @param baselineOffset
     */
    public void setBaselineOffset(double baselineOffset){
       getRPr(true).setBaseline((int) baselineOffset * 1000);
    }

    /**
     * Set whether the text in this run is formatted as superscript.
     * Default base line offset is 30%
     *
     * @see #setBaselineOffset(double)
     */
    public void setSuperscript(boolean flag){
        setBaselineOffset(flag ? 30. : 0.);
    }

    /**
     * Set whether the text in this run is formatted as subscript.
     * Default base line offset is -25%.
     *
     * @see #setBaselineOffset(double)
     */
    public void setSubscript(boolean flag){
        setBaselineOffset(flag ? -25.0 : 0.);
    }

    @Override
    public boolean isSubscript() {
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getIndentLevel()){
            @Override
            public boolean fetch(CTTextCharacterProperties props){
                if (props != null && props.isSetBaseline()) {
                    setValue(props.getBaseline() < 0);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    /**
     * @return whether a run of text will be formatted as a superscript text. Default is false.
     */
    @Override
    public TextCap getTextCap() {
        CharacterPropertyFetcher<TextCap> fetcher = new CharacterPropertyFetcher<TextCap>(_p.getIndentLevel()){
            @Override
            public boolean fetch(CTTextCharacterProperties props){
                if (props != null && props.isSetCap()) {
                    int idx = props.getCap().intValue() - 1;
                    setValue(TextCap.values()[idx]);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? TextCap.NONE : fetcher.getValue();
    }

    @Override
    public void setBold(boolean bold){
        getRPr(true).setB(bold);
    }

    @Override
    public boolean isBold(){
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getIndentLevel()){
            @Override
            public boolean fetch(CTTextCharacterProperties props){
                if (props != null && props.isSetB()) {
                    setValue(props.getB());
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    @Override
    public void setItalic(boolean italic){
        getRPr(true).setI(italic);
    }

    @Override
    public boolean isItalic(){
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getIndentLevel()){
            @Override
            public boolean fetch(CTTextCharacterProperties props){
                if (props != null && props.isSetI()) {
                    setValue(props.getI());
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    @Override
    public void setUnderlined(boolean underline) {
        getRPr(true).setU(underline ? STTextUnderlineType.SNG : STTextUnderlineType.NONE);
    }

    @Override
    public boolean isUnderlined(){
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getIndentLevel()){
            @Override
            public boolean fetch(CTTextCharacterProperties props){
                if (props != null && props.isSetU()) {
                    setValue(props.getU() != STTextUnderlineType.NONE);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    /**
     * Return the character properties
     *
     * @param create if true, create an empty character properties object if it doesn't exist
     * @return the character properties or null if create was false and the properties haven't exist
     */
    protected CTTextCharacterProperties getRPr(boolean create) {
        if (_r instanceof CTTextField) {
            CTTextField tf = (CTTextField)_r;
            if (tf.isSetRPr()) {
                return tf.getRPr();
            } else if (create) {
                return tf.addNewRPr();
            }
        } else if (_r instanceof CTTextLineBreak) {
            CTTextLineBreak tlb = (CTTextLineBreak)_r;
            if (tlb.isSetRPr()) {
                return tlb.getRPr();
            } else if (create) {
                return tlb.addNewRPr();
            }
        } else {
            CTRegularTextRun tr = (CTRegularTextRun)_r;
            if (tr.isSetRPr()) {
                return tr.getRPr();
            } else if (create) {
                return tr.addNewRPr();
            }
        }
        return null;
    }

    @Override
    public String toString(){
        return "[" + getClass() + "]" + getRawText();
    }

    @Override
    public XSLFHyperlink createHyperlink(){
        XSLFHyperlink hl = getHyperlink();
        if (hl != null) {
            return hl;
        }

        CTTextCharacterProperties rPr = getRPr(true);
        return new XSLFHyperlink(rPr.addNewHlinkClick(), _p.getParentShape().getSheet());
    }

    @Override
    public XSLFHyperlink getHyperlink(){
        CTTextCharacterProperties rPr = getRPr(false);
        if (rPr == null) {
            return null;
        }
        CTHyperlink hl = rPr.getHlinkClick();
        if (hl == null) {
            return null;
        }
        return new XSLFHyperlink(hl, _p.getParentShape().getSheet());
    }

    private boolean fetchCharacterProperty(CharacterPropertyFetcher<?> fetcher){
        XSLFTextShape shape = _p.getParentShape();
        XSLFSheet sheet = shape.getSheet();

        CTTextCharacterProperties rPr = getRPr(false);
        if (rPr != null && fetcher.fetch(rPr)) {
            return true;
        }

        if (shape.fetchShapeProperty(fetcher)) {
            return true;
        }

        CTPlaceholder ph = shape.getCTPlaceholder();
        if (ph == null){
            // if it is a plain text box then take defaults from presentation.xml
            @SuppressWarnings("resource")
            XMLSlideShow ppt = sheet.getSlideShow();
            // TODO: determine master shape
            CTTextParagraphProperties themeProps = ppt.getDefaultParagraphStyle(_p.getIndentLevel());
            if (themeProps != null && fetcher.fetch(themeProps)) {
                return true;
            }
        }

        // TODO: determine master shape
        CTTextParagraphProperties defaultProps =  _p.getDefaultMasterStyle();
        if(defaultProps != null && fetcher.fetch(defaultProps)) {
            return true;
        }

        return false;
    }

    void copy(XSLFTextRun r){
        String srcFontFamily = r.getFontFamily();
        if(srcFontFamily != null && !srcFontFamily.equals(getFontFamily())){
            setFontFamily(srcFontFamily);
        }

        PaintStyle srcFontColor = r.getFontColor();
        if(srcFontColor != null && !srcFontColor.equals(getFontColor())){
            setFontColor(srcFontColor);
        }

        double srcFontSize = r.getFontSize();
        if(srcFontSize  != getFontSize()){
            setFontSize(srcFontSize);
        }

        boolean bold = r.isBold();
        if(bold != isBold()) {
            setBold(bold);
        }

        boolean italic = r.isItalic();
        if(italic != isItalic()) {
            setItalic(italic);
        }

        boolean underline = r.isUnderlined();
        if(underline != isUnderlined()) {
            setUnderlined(underline);
        }

        boolean strike = r.isStrikethrough();
        if(strike != isStrikethrough()) {
            setStrikethrough(strike);
        }
    }


    @Override
    public FieldType getFieldType() {
        if (_r instanceof CTTextField) {
            CTTextField tf = (CTTextField)_r;
            if ("slidenum".equals(tf.getType())) {
                return FieldType.SLIDE_NUMBER;
            }
        }
        return null;
    }


    private class XSLFFontInfo implements FontInfo {
        private final FontGroup fontGroup;

        private XSLFFontInfo(FontGroup fontGroup) {
            this.fontGroup = (fontGroup != null) ? fontGroup : FontGroup.getFontGroupFirst(getRawText());
        }

        public void copyFrom(FontInfo fontInfo) {
            CTTextFont tf = getXmlObject(true);
            setTypeface(fontInfo.getTypeface());
            setCharset(fontInfo.getCharset());
            FontPitch pitch = fontInfo.getPitch();
            FontFamily family = fontInfo.getFamily();
            if (pitch == null && family == null) {
                if (tf.isSetPitchFamily()) {
                    tf.unsetPitchFamily();
                }
            } else {
                setPitch(pitch);
                setFamily(family);
            }
        }

        @Override
        public Integer getIndex() {
            return null;
        }

        @Override
        public void setIndex(int index) {
            throw new UnsupportedOperationException("setIndex not supported by XSLFFontInfo.");
        }

        @Override
        public String getTypeface() {
            CTTextFont tf = getXmlObject(false);
            return (tf != null && tf.isSetTypeface()) ? tf.getTypeface() : null;
        }

        @Override
        public void setTypeface(String typeface) {
            if (typeface != null) {
                getXmlObject(true).setTypeface(typeface);
                return;
            }
            
            CTTextCharacterProperties props = getRPr(false);
            if (props == null) {
                return;
            }
            FontGroup fg = FontGroup.getFontGroupFirst(getRawText());
            switch (fg) {
            default:
            case LATIN:
                if (props.isSetLatin()) {
                    props.unsetLatin();
                }
                break;
            case EAST_ASIAN:
                if (props.isSetEa()) {
                    props.unsetEa();
                }
                break;
            case COMPLEX_SCRIPT:
                if (props.isSetCs()) {
                    props.unsetCs();
                }
                break;
            case SYMBOL:
                if (props.isSetSym()) {
                    props.unsetSym();
                }
                break;
            }
        }

        @Override
        public FontCharset getCharset() {
            CTTextFont tf = getXmlObject(false);
            return (tf != null && tf.isSetCharset()) ? FontCharset.valueOf(tf.getCharset()&0xFF) : null;
        }

        @Override
        public void setCharset(FontCharset charset) {
            CTTextFont tf = getXmlObject(true);
            if (charset != null) {
                tf.setCharset((byte)charset.getNativeId());
            } else {
                if (tf.isSetCharset()) {
                    tf.unsetCharset();
                }
            }
        }

        @Override
        public FontFamily getFamily() {
            CTTextFont tf = getXmlObject(false);
            return (tf != null && tf.isSetPitchFamily()) ? FontFamily.valueOfPitchFamily(tf.getPitchFamily()) : null;
        }

        @Override
        public void setFamily(FontFamily family) {
            CTTextFont tf = getXmlObject(true);
            if (family == null && !tf.isSetPitchFamily()) {
                return;
            }
            FontPitch pitch = (tf.isSetPitchFamily())
                ? FontPitch.valueOfPitchFamily(tf.getPitchFamily())
                : FontPitch.VARIABLE;
            byte pitchFamily = FontPitch.getNativeId(pitch, family != null ? family : FontFamily.FF_SWISS);
            tf.setPitchFamily(pitchFamily);
        }

        @Override
        public FontPitch getPitch() {
            CTTextFont tf = getXmlObject(false);
            return (tf != null && tf.isSetPitchFamily()) ? FontPitch.valueOfPitchFamily(tf.getPitchFamily()) : null;
        }

        @Override
        public void setPitch(FontPitch pitch) {
            CTTextFont tf = getXmlObject(true);
            if (pitch == null && !tf.isSetPitchFamily()) {
                return;
            }
            FontFamily family = (tf.isSetPitchFamily())
                ? FontFamily.valueOfPitchFamily(tf.getPitchFamily())
                : FontFamily.FF_SWISS;
            byte pitchFamily = FontPitch.getNativeId(pitch != null ? pitch : FontPitch.VARIABLE, family);
            tf.setPitchFamily(pitchFamily);
        }

        private CTTextFont getXmlObject(boolean create) {
            if (create) {
                return getCTTextFont(getRPr(true), true);
            }

            CharacterPropertyFetcher<CTTextFont> visitor = new CharacterPropertyFetcher<CTTextFont>(_p.getIndentLevel()){
                @Override
                public boolean fetch(CTTextCharacterProperties props){
                    CTTextFont font = getCTTextFont(props, false);
                    if (font == null) {
                        return false;
                    }
                    setValue(font);
                    return true;
                }
            };
            fetchCharacterProperty(visitor);

            return  visitor.getValue();
        }

        private CTTextFont getCTTextFont(CTTextCharacterProperties props, boolean create) {
            if (props == null) {
                return null;
            }

            CTTextFont font;
            switch (fontGroup) {
            default:
            case LATIN:
                font = props.getLatin();
                if (font == null && create) {
                    font = props.addNewLatin();
                }
                break;
            case EAST_ASIAN:
                font = props.getEa();
                if (font == null && create) {
                    font = props.addNewEa();
                }
                break;
            case COMPLEX_SCRIPT:
                font = props.getCs();
                if (font == null && create) {
                    font = props.addNewCs();
                }
                break;
            case SYMBOL:
                font = props.getSym();
                if (font == null && create) {
                    font = props.addNewSym();
                }
                break;
            }

            if (font == null) {
                return null;
            }

            String typeface = font.isSetTypeface() ? font.getTypeface() : "";
            if (typeface.startsWith("+mj-") || typeface.startsWith("+mn-")) {
                //  "+mj-lt".equals(typeface) || "+mn-lt".equals(typeface)
                final XSLFTheme theme = _p.getParentShape().getSheet().getTheme();
                CTFontScheme fontTheme = theme.getXmlObject().getThemeElements().getFontScheme();
                CTFontCollection coll = typeface.startsWith("+mj-")
                    ? fontTheme.getMajorFont() : fontTheme.getMinorFont();
                // TODO: handle LCID codes
                // see https://blogs.msdn.microsoft.com/officeinteroperability/2013/04/22/office-open-xml-themes-schemes-and-fonts/
                String fgStr = typeface.substring(4);
                if ("ea".equals(fgStr)) {
                    font = coll.getEa();
                } else if ("cs".equals(fgStr)) {
                    font = coll.getCs();
                } else {
                    font = coll.getLatin();
                }
                // SYMBOL is missing
                
                if (font == null || !font.isSetTypeface() || "".equals(font.getTypeface())) {
                    font = coll.getLatin();
                }
            }

            return font;
        }
    }
}
