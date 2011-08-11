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

import org.apache.poi.util.Beta;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextFont;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextStrikeType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;

import java.awt.*;

/**
 * Represents a run of text within the containing text body. The run element is the
 * lowest level text separation mechanism within a text body.
 *
 * @author Yegor Kozlov
 */
@Beta
public class XSLFTextRun {
    private final CTRegularTextRun _r;

    XSLFTextRun(CTRegularTextRun r){
        _r = r;
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
        CTTextCharacterProperties rPr = getRpR();
        CTSolidColorFillProperties fill = rPr.isSetSolidFill() ? rPr.getSolidFill() : rPr.addNewSolidFill();
        CTSRgbColor clr = fill.isSetSrgbClr() ? fill.getSrgbClr() : fill.addNewSrgbClr();
        clr.setVal(new byte[]{(byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue()});
    }

    /**
     *
     * @param fontSize  font size in points.
     * The value of <code>-1</code> unsets the Sz attribyte from the underlying xml bean
     */
    public void setFontSize(double fontSize){
        CTTextCharacterProperties rPr = getRpR();
        if(fontSize == -1.0) {
            if(rPr.isSetSz()) rPr.unsetSz();
        } else {
            rPr.setSz((int)(100*fontSize));
        }
    }

    /**
     * @return font size in points or -1 if font size is not set.
     */
    public double getFontSize(){
        if(!_r.isSetRPr()) return -1;

        return _r.getRPr().getSz()*0.01;
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
        CTTextCharacterProperties rPr = getRpR();

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
     * @return  font family or null if niot set
     */
    public String getFontFamily(){
        if(!_r.isSetRPr() || !_r.getRPr().isSetLatin()) return null;

        return _r.getRPr().getLatin().getTypeface();
    }

    /**
     * Specifies whether a run of text will be formatted as strikethrough text.
     *
     * @param strike whether a run of text will be formatted as strikethrough text.
     */
    public void setStrikethrough(boolean strike){
        getRpR().setStrike(strike ? STTextStrikeType.SNG_STRIKE : STTextStrikeType.NO_STRIKE);
    }

    /**
     * @return whether a run of text will be formatted as strikethrough text. Default is false.
     */
    public boolean isStrikethrough(){
        if(!_r.isSetRPr()) return false;

        return _r.getRPr().getStrike() == STTextStrikeType.SNG_STRIKE;
    }

    /**
     * Specifies whether this run of text will be formatted as bold text
     *
     * @param bold whether this run of text will be formatted as bold text
     */
    public void setBold(boolean bold){
        getRpR().setB(bold);
    }

    /**
     * @return whether this run of text is formatted as bold text
     */
    public boolean isBold(){
        if(!_r.isSetRPr()) return false;

        return _r.getRPr().getB();
    }

    /**
     * @param italic whether this run of text is formatted as italic text
     */
    public void setItalic(boolean italic){
        getRpR().setI(italic);
    }

    /**
     * @return whether this run of text is formatted as italic text
     */
    public boolean isItalic(){
        if(!_r.isSetRPr()) return false;

        return _r.getRPr().getI();
    }

    /**
     * @param underline whether this run of text is formatted as underlined text
     */
    public void setUnderline(boolean underline){
        getRpR().setU(underline ? STTextUnderlineType.SNG : STTextUnderlineType.NONE);
    }

    /**
     * @return whether this run of text is formatted as underlined text
     */
    public boolean isUnderline(){
        if(!_r.isSetRPr() || !_r.getRPr().isSetU()) return false;

        return _r.getRPr().getU() != STTextUnderlineType.NONE;
    }

    protected CTTextCharacterProperties getRpR(){
        return _r.isSetRPr() ? _r.getRPr() : _r.addNewRPr();
    }

    @Override
    public String toString(){
        return "[" + getClass() + "]" + getText();
    }
    
}