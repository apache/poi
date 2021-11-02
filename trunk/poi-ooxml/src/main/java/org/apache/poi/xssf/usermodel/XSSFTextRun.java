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

import java.awt.Color;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextFont;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextNormalAutofit;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextStrikeType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;

/**
 * Represents a run of text within the containing text body. The run element is the
 * lowest level text separation mechanism within a text body.
 */
public class XSSFTextRun {
    private final CTRegularTextRun _r;
    private final XSSFTextParagraph _p;

    XSSFTextRun(CTRegularTextRun r, XSSFTextParagraph p){
        _r = r;
        _p = p;
    }

    XSSFTextParagraph getParentParagraph(){
        return _p;
    }

    public String getText(){
        return _r.getT();
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

        CTTextCharacterProperties rPr = getRPr();
        if(rPr.isSetSolidFill()){
            CTSolidColorFillProperties fill = rPr.getSolidFill();

            if(fill.isSetSrgbClr()){
                CTSRgbColor clr = fill.getSrgbClr();
                byte[] rgb = clr.getVal();
                return new Color(0xFF & rgb[0], 0xFF & rgb[1], 0xFF & rgb[2]);
            }
        }

        return new Color(0, 0, 0);
    }

    /**
     *
     * @param fontSize  font size in points.
     * The value of {@code -1} unsets the Sz attribute from the underlying xml bean
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
        double size = XSSFFont.DEFAULT_FONT_SIZE;   // default font size
        CTTextNormalAutofit afit = getParentParagraph().getParentShape().getTxBody().getBodyPr().getNormAutofit();
        if(afit != null) scale = (double)afit.getFontScale() / 100000;

        CTTextCharacterProperties rPr = getRPr();
        if(rPr.isSetSz()){
            size = rPr.getSz()*0.01;
        }

        return size * scale;
    }

    /**
     *
     * @return the spacing between characters within a text run,
     * If this attribute is omitted then a value of 0 or no adjustment is assumed.
     */
    public double getCharacterSpacing(){
        CTTextCharacterProperties rPr = getRPr();
        if(rPr.isSetSpc()){
            return Units.toPoints(POIXMLUnits.parseLength(rPr.xgetSpc()));
        }
        return 0;
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
     * The value of {@code null} unsets the Typeface attribute from the underlying xml.
     */
    public void setFont(String typeface){
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
        CTTextCharacterProperties rPr = getRPr();
        CTTextFont font = rPr.getLatin();
        if(font != null){
            return font.getTypeface();
        }
        return XSSFFont.DEFAULT_FONT_NAME;
    }

    public byte getPitchAndFamily(){
        CTTextCharacterProperties rPr = getRPr();
        CTTextFont font = rPr.getLatin();
        if(font != null){
            return font.getPitchFamily();
        }
        return 0;
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
        CTTextCharacterProperties rPr = getRPr();
        if(rPr.isSetStrike()){
            return rPr.getStrike() != STTextStrikeType.NO_STRIKE;
        }
        return false;
    }

    /**
     * @return whether a run of text will be formatted as a superscript text. Default is false.
     */
    public boolean isSuperscript() {
        CTTextCharacterProperties rPr = getRPr();
        if(rPr.isSetBaseline()){
            return POIXMLUnits.parsePercent(rPr.xgetBaseline()) > 0;
        }
        return false;
    }

    /**
     *  Set the baseline for both the superscript and subscript fonts.
     *  <p>
     *     The size is specified using a percentage.
     *     Positive values indicate superscript, negative values indicate subscript.
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
        CTTextCharacterProperties rPr = getRPr();
        if(rPr.isSetBaseline()){
            return POIXMLUnits.parsePercent(rPr.xgetBaseline()) < 0;
        }
        return false;
    }

    /**
     * @return whether a run of text will be formatted as a superscript text. Default is false.
     */
    public TextCap getTextCap() {
        CTTextCharacterProperties rPr = getRPr();
        if(rPr.isSetCap()){
            return TextCap.values()[rPr.getCap().intValue() - 1];
        }
        return TextCap.NONE;
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
        CTTextCharacterProperties rPr = getRPr();
        if(rPr.isSetB()){
            return rPr.getB();
        }
        return false;
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
        CTTextCharacterProperties rPr = getRPr();
        if(rPr.isSetI()){
            return rPr.getI();
        }
        return false;
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
        CTTextCharacterProperties rPr = getRPr();
        if(rPr.isSetU()){
            return rPr.getU() != STTextUnderlineType.NONE;
        }
        return false;
    }

    protected CTTextCharacterProperties getRPr(){
        return _r.isSetRPr() ? _r.getRPr() : _r.addNewRPr();
    }

    @Override
    public String toString(){
        return "[" + getClass() + "]" + getText();
    }
}
