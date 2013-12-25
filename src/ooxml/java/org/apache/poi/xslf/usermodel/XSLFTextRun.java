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
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;

import org.apache.poi.util.Beta;
import org.apache.poi.xslf.model.CharacterPropertyFetcher;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeStyle;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextFont;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextNormalAutofit;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STSchemeColorVal;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextStrikeType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;

/**
 * Represents a run of text within the containing text body. The run element is the
 * lowest level text separation mechanism within a text body.
 *
 * @author Yegor Kozlov
 */
@Beta
public class XSLFTextRun {
    private final CTRegularTextRun _r;
    private final XSLFTextParagraph _p;

    XSLFTextRun(CTRegularTextRun r, XSLFTextParagraph p){
        _r = r;
        _p = p;
    }

    XSLFTextParagraph getParentParagraph(){
        return _p;
    }

    public String getText(){
        return _r.getT();
    }

    String getRenderableText(){
        String txt = _r.getT();
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

    /**
     * Replace a tab with the effective number of white spaces.
     */
    private String tab2space(){
        AttributedString string = new AttributedString(" ");
        // user can pass an object to convert fonts via a rendering hint
        string.addAttribute(TextAttribute.FAMILY, getFontFamily());

        string.addAttribute(TextAttribute.SIZE, (float)getFontSize());
        TextLayout l = new TextLayout(string.getIterator(), new FontRenderContext(null, true, true));
        double wspace = l.getAdvance();

        double tabSz = _p.getDefaultTabSize();

        int numSpaces = (int)Math.ceil(tabSz / wspace);
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < numSpaces; i++) {
            buf.append(' ');
        }
        return buf.toString();
    }
    
    public void setText(String text){
        _r.setT(text);
    }

    public CTRegularTextRun getXmlObject(){
        return _r;
    }

    public void setFontColor(Color color){
        CTTextCharacterProperties rPr = getRPr();
        CTSolidColorFillProperties fill = rPr.isSetSolidFill() ? rPr.getSolidFill() : rPr.addNewSolidFill();
        CTSRgbColor clr = fill.isSetSrgbClr() ? fill.getSrgbClr() : fill.addNewSrgbClr();
        clr.setVal(new byte[]{(byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue()});

        if(fill.isSetHslClr()) fill.unsetHslClr();
        if(fill.isSetPrstClr()) fill.unsetPrstClr();
        if(fill.isSetSchemeClr()) fill.unsetSchemeClr();
        if(fill.isSetScrgbClr()) fill.unsetScrgbClr();
        if(fill.isSetSysClr()) fill.unsetSysClr();

    }

    public Color getFontColor(){
        final XSLFTheme theme = _p.getParentShape().getSheet().getTheme();
        CTShapeStyle style = _p.getParentShape().getSpStyle();
        final CTSchemeColor phClr = style == null ? null : style.getFontRef().getSchemeClr();

        CharacterPropertyFetcher<Color> fetcher = new CharacterPropertyFetcher<Color>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                CTSolidColorFillProperties solidFill = props.getSolidFill();
                if(solidFill != null) {
                    boolean useCtxColor =
                            (solidFill.isSetSchemeClr() && solidFill.getSchemeClr().getVal() == STSchemeColorVal.PH_CLR)
                            || isFetchingFromMaster;
                    Color c = new XSLFColor(solidFill, theme, useCtxColor ? phClr : null).getColor();
                    setValue(c);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue();
    }

    /**
     *
     * @param fontSize  font size in points.
     * The value of <code>-1</code> unsets the Sz attribyte from the underlying xml bean
     */
    public void setFontSize(double fontSize){
        CTTextCharacterProperties rPr = getRPr();
        if(fontSize == -1.0) {
            if(rPr.isSetSz()) rPr.unsetSz();
        } else {
            if(fontSize < 1.0) {
                throw new IllegalArgumentException("Minimum font size is 1pt but was " + fontSize);
            }

            rPr.setSz((int)(100*fontSize));
        }
    }

    /**
     * @return font size in points or -1 if font size is not set.
     */
    public double getFontSize(){
        double scale = 1;
        CTTextNormalAutofit afit = getParentParagraph().getParentShape().getTextBodyPr().getNormAutofit();
        if(afit != null) scale = (double)afit.getFontScale() / 100000;

        CharacterPropertyFetcher<Double> fetcher = new CharacterPropertyFetcher<Double>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                if(props.isSetSz()){
                    setValue(props.getSz()*0.01);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? -1 : fetcher.getValue()*scale;
    }

    /**
     *
     * @return the spacing between characters within a text run,
     * If this attribute is omitted than a value of 0 or no adjustment is assumed.
     */
    public double getCharacterSpacing(){

        CharacterPropertyFetcher<Double> fetcher = new CharacterPropertyFetcher<Double>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                if(props.isSetSpc()){
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
        CTTextCharacterProperties rPr = getRPr();
        if(spc == 0.0) {
            if(rPr.isSetSpc()) rPr.unsetSpc();
        } else {
            rPr.setSpc((int)(100*spc));
        }
    }

    /**
     * Specifies the typeface, or name of the font that is to be used for this text run.
     *
     * @param typeface  the font to apply to this text run.
     * The value of <code>null</code> unsets the Typeface attrubute from the underlying xml.
     */
    public void setFontFamily(String typeface){
        setFontFamily(typeface, (byte)-1, (byte)-1, false);
    }

    public void setFontFamily(String typeface, byte charset, byte pictAndFamily, boolean isSymbol){
        CTTextCharacterProperties rPr = getRPr();

        if(typeface == null){
            if(rPr.isSetLatin()) rPr.unsetLatin();
            if(rPr.isSetCs()) rPr.unsetCs();
            if(rPr.isSetSym()) rPr.unsetSym();
        } else {
            if(isSymbol){
                CTTextFont font = rPr.isSetSym() ? rPr.getSym() : rPr.addNewSym();
                font.setTypeface(typeface);
            } else {
                CTTextFont latin = rPr.isSetLatin() ? rPr.getLatin() : rPr.addNewLatin();
                latin.setTypeface(typeface);
                if(charset != -1) latin.setCharset(charset);
                if(pictAndFamily != -1) latin.setPitchFamily(pictAndFamily);
            }
        }
    }

    /**
     * @return  font family or null if not set
     */
    public String getFontFamily(){
        final XSLFTheme theme = _p.getParentShape().getSheet().getTheme();

        CharacterPropertyFetcher<String> visitor = new CharacterPropertyFetcher<String>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                CTTextFont font = props.getLatin();
                if(font != null){
                    String typeface = font.getTypeface();
                    if("+mj-lt".equals(typeface)) {
                        typeface = theme.getMajorFont();
                    } else if ("+mn-lt".equals(typeface)){
                        typeface = theme.getMinorFont();
                    }
                    setValue(typeface);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(visitor);

        return  visitor.getValue();
    }

    public byte getPitchAndFamily(){
        final XSLFTheme theme = _p.getParentShape().getSheet().getTheme();

        CharacterPropertyFetcher<Byte> visitor = new CharacterPropertyFetcher<Byte>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                CTTextFont font = props.getLatin();
                if(font != null){
                    setValue(font.getPitchFamily());
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(visitor);

        return  visitor.getValue() == null ? 0 : visitor.getValue();
    }

    /**
     * Specifies whether a run of text will be formatted as strikethrough text.
     *
     * @param strike whether a run of text will be formatted as strikethrough text.
     */
    public void setStrikethrough(boolean strike) {
        getRPr().setStrike(strike ? STTextStrikeType.SNG_STRIKE : STTextStrikeType.NO_STRIKE);
    }

    /**
     * @return whether a run of text will be formatted as strikethrough text. Default is false.
     */
    public boolean isStrikethrough() {
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                if(props.isSetStrike()){
                    setValue(props.getStrike() != STTextStrikeType.NO_STRIKE);
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
    public boolean isSuperscript() {
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                if(props.isSetBaseline()){
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
       getRPr().setBaseline((int) baselineOffset * 1000);
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

    /**
     * @return whether a run of text will be formatted as a superscript text. Default is false.
     */
    public boolean isSubscript() {
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                if(props.isSetBaseline()){
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
    public TextCap getTextCap() {
        CharacterPropertyFetcher<TextCap> fetcher = new CharacterPropertyFetcher<TextCap>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                if(props.isSetCap()){
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

    /**
     * Specifies whether this run of text will be formatted as bold text
     *
     * @param bold whether this run of text will be formatted as bold text
     */
    public void setBold(boolean bold){
        getRPr().setB(bold);
    }

    /**
     * @return whether this run of text is formatted as bold text
     */
    public boolean isBold(){
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                if(props.isSetB()){
                    setValue(props.getB());
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    /**
     * @param italic whether this run of text is formatted as italic text
     */
    public void setItalic(boolean italic){
        getRPr().setI(italic);
    }

    /**
     * @return whether this run of text is formatted as italic text
     */
    public boolean isItalic(){
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                if(props.isSetI()){
                    setValue(props.getI());
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    /**
     * @param underline whether this run of text is formatted as underlined text
     */
    public void setUnderline(boolean underline) {
        getRPr().setU(underline ? STTextUnderlineType.SNG : STTextUnderlineType.NONE);
    }

    /**
     * @return whether this run of text is formatted as underlined text
     */
    public boolean isUnderline(){
        CharacterPropertyFetcher<Boolean> fetcher = new CharacterPropertyFetcher<Boolean>(_p.getLevel()){
            public boolean fetch(CTTextCharacterProperties props){
                if(props.isSetU()){
                    setValue(props.getU() != STTextUnderlineType.NONE);
                    return true;
                }
                return false;
            }
        };
        fetchCharacterProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    protected CTTextCharacterProperties getRPr(){
        return _r.isSetRPr() ? _r.getRPr() : _r.addNewRPr();
    }

    @Override
    public String toString(){
        return "[" + getClass() + "]" + getText();
    }

    public XSLFHyperlink createHyperlink(){
        XSLFHyperlink link = new XSLFHyperlink(_r.getRPr().addNewHlinkClick(), this);
        return link;
    }

    public XSLFHyperlink getHyperlink(){
        if(!_r.getRPr().isSetHlinkClick()) return null;


        return new XSLFHyperlink(_r.getRPr().getHlinkClick(), this);
    }

    private boolean fetchCharacterProperty(CharacterPropertyFetcher fetcher){
        boolean ok = false;

        if(_r.isSetRPr()) ok = fetcher.fetch(getRPr());

        if(!ok) {
            XSLFTextShape shape = _p.getParentShape();
            ok = shape.fetchShapeProperty(fetcher);
            if(!ok){
                CTPlaceholder ph = shape.getCTPlaceholder();
                if(ph == null){
                    // if it is a plain text box then take defaults from presentation.xml
                    XMLSlideShow ppt = shape.getSheet().getSlideShow();
                    CTTextParagraphProperties themeProps = ppt.getDefaultParagraphStyle(_p.getLevel());
                    if(themeProps != null) {
                        fetcher.isFetchingFromMaster = true;
                        ok = fetcher.fetch(themeProps);
                    }
                }
                if (!ok) {
                    CTTextParagraphProperties defaultProps =  _p.getDefaultMasterStyle();
                    if(defaultProps != null) {
                        fetcher.isFetchingFromMaster = true;
                        ok = fetcher.fetch(defaultProps);
                    }
                }
            }
        }

        return ok;
    }

    void copy(XSLFTextRun r){
        String srcFontFamily = r.getFontFamily();
        if(srcFontFamily != null && !srcFontFamily.equals(getFontFamily())){
            setFontFamily(srcFontFamily);
        }

        Color srcFontColor = r.getFontColor();
        if(srcFontColor != null && !srcFontColor.equals(getFontColor())){
            setFontColor(srcFontColor);
        }

        double srcFontSize = r.getFontSize();
        if(srcFontSize  != getFontSize()){
            setFontSize(srcFontSize);
        }

        boolean bold = r.isBold();
        if(bold != isBold()) setBold(bold);

        boolean italic = r.isItalic();
        if(italic != isItalic()) setItalic(italic);

        boolean underline = r.isUnderline();
        if(underline != isUnderline()) setUnderline(underline);

        boolean strike = r.isStrikethrough();
        if(strike != isStrikethrough()) setStrikethrough(strike);
    }
}
