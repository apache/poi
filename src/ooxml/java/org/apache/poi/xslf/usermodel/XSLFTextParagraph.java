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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.AutoNumberingScheme;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.TabStop.TabStopType;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.poi.xslf.model.ParagraphPropertyFetcher;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

/**
 * Represents a paragraph of text within the containing text body.
 * The paragraph is the highest level text separation mechanism.
 *
 * @since POI-3.8
 */
@Beta
public class XSLFTextParagraph implements TextParagraph<XSLFShape,XSLFTextParagraph,XSLFTextRun> {
    private final CTTextParagraph _p;
    private final List<XSLFTextRun> _runs;
    private final XSLFTextShape _shape;

    @FunctionalInterface
    private interface Procedure {
        void accept();
    }


    XSLFTextParagraph(CTTextParagraph p, XSLFTextShape shape){
        _p = p;
        _runs = new ArrayList<>();
        _shape = shape;

        XmlCursor c = _p.newCursor();
        try {
            if (c.toFirstChild()) {
                do {
                    XmlObject r = c.getObject();
                    if (r instanceof CTTextLineBreak) {
                        _runs.add(new XSLFLineBreak((CTTextLineBreak)r, this));
                    } else if (r instanceof CTRegularTextRun || r instanceof CTTextField) {
                        _runs.add(new XSLFTextRun(r, this));
                    }
                } while (c.toNextSibling());
            }
        } finally {
            c.dispose();
        }
    }

    public String getText(){
        StringBuilder out = new StringBuilder();
        for (XSLFTextRun r : _runs) {
            out.append(r.getRawText());
        }
        return out.toString();
    }

    @Internal
    public CTTextParagraph getXmlObject(){
        return _p;
    }

    public XSLFTextShape getParentShape() {
        return _shape;

    }

    @Override
    public List<XSLFTextRun> getTextRuns(){
        return _runs;
    }

    public Iterator<XSLFTextRun> iterator(){
        return _runs.iterator();
    }

    /**
     * Add a new run of text
     *
     * @return a new run of text
     */
    public XSLFTextRun addNewTextRun(){
        CTRegularTextRun r = _p.addNewR();
        CTTextCharacterProperties rPr = r.addNewRPr();
        rPr.setLang("en-US");
        XSLFTextRun run = newTextRun(r);
        _runs.add(run);
        return run;
    }

    /**
     * Insert a line break
     *
     * @return text run representing this line break ('\n')
     */
    @SuppressWarnings("WeakerAccess")
    public XSLFTextRun addLineBreak(){
        XSLFLineBreak run = new XSLFLineBreak(_p.addNewBr(), this);
        CTTextCharacterProperties brProps = run.getRPr(true);
        if(_runs.size() > 0){
            // by default line break has the font size of the last text run
            CTTextCharacterProperties prevRun = _runs.get(_runs.size() - 1).getRPr(true);
            brProps.set(prevRun);
            // don't copy hlink properties
            if (brProps.isSetHlinkClick()) {
                brProps.unsetHlinkClick();
            }
            if (brProps.isSetHlinkMouseOver()) {
                brProps.unsetHlinkMouseOver();
            }
        }
        _runs.add(run);
        return run;
    }

    @Override
    public TextAlign getTextAlign(){
        ParagraphPropertyFetcher<TextAlign> fetcher = new ParagraphPropertyFetcher<TextAlign>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetAlgn()){
                    TextAlign val = TextAlign.values()[props.getAlgn().intValue() - 1];
                    setValue(val);
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();
    }

    @Override
    public void setTextAlign(TextAlign align) {
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(align == null) {
            if(pr.isSetAlgn()) pr.unsetAlgn();
        } else {
            pr.setAlgn(STTextAlignType.Enum.forInt(align.ordinal() + 1));
        }
    }

    @Override
    public FontAlign getFontAlign(){
        ParagraphPropertyFetcher<FontAlign> fetcher = new ParagraphPropertyFetcher<FontAlign>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetFontAlgn()){
                    FontAlign val = FontAlign.values()[props.getFontAlgn().intValue() - 1];
                    setValue(val);
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();
    }

    /**
     * Specifies the font alignment that is to be applied to the paragraph.
     * Possible values for this include auto, top, center, baseline and bottom.
     * see {@link org.apache.poi.sl.usermodel.TextParagraph.FontAlign}.
     *
     * @param align font align
     */
    @SuppressWarnings("unused")
    public void setFontAlign(FontAlign align){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(align == null) {
            if(pr.isSetFontAlgn()) pr.unsetFontAlgn();
        } else {
            pr.setFontAlgn(STTextFontAlignType.Enum.forInt(align.ordinal() + 1));
        }
    }

    

    /**
     * @return the font to be used on bullet characters within a given paragraph
     */
    @SuppressWarnings("WeakerAccess")
    public String getBulletFont(){
        ParagraphPropertyFetcher<String> fetcher = new ParagraphPropertyFetcher<String>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetBuFont()){
                    setValue(props.getBuFont().getTypeface());
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();
    }

    @SuppressWarnings("WeakerAccess")
    public void setBulletFont(String typeface){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextFont font = pr.isSetBuFont() ? pr.getBuFont() : pr.addNewBuFont();
        font.setTypeface(typeface);
    }

    /**
     * @return the character to be used in place of the standard bullet point
     */
    @SuppressWarnings("WeakerAccess")
    public String getBulletCharacter(){
        ParagraphPropertyFetcher<String> fetcher = new ParagraphPropertyFetcher<String>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetBuChar()){
                    setValue(props.getBuChar().getChar());
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();
    }

    @SuppressWarnings("WeakerAccess")
    public void setBulletCharacter(String str){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextCharBullet c = pr.isSetBuChar() ? pr.getBuChar() : pr.addNewBuChar();
        c.setChar(str);
    }

    /**
     *
     * @return the color of bullet characters within a given paragraph.
     * A <code>null</code> value means to use the text font color.
     */
    @SuppressWarnings("WeakerAccess")
    public PaintStyle getBulletFontColor(){
        final XSLFTheme theme = getParentShape().getSheet().getTheme();
        ParagraphPropertyFetcher<Color> fetcher = new ParagraphPropertyFetcher<Color>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetBuClr()){
                    XSLFColor c = new XSLFColor(props.getBuClr(), theme, null);
                    setValue(c.getColor());
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        Color col = fetcher.getValue();
        return (col == null) ? null : DrawPaint.createSolidPaint(col);
    }

    @SuppressWarnings("WeakerAccess")
    public void setBulletFontColor(Color color) {
        setBulletFontColor(DrawPaint.createSolidPaint(color));
    }
    
    
    /**
     * Set the color to be used on bullet characters within a given paragraph.
     *
     * @param color the bullet color
     */
    @SuppressWarnings("WeakerAccess")
    public void setBulletFontColor(PaintStyle color) {
        if (!(color instanceof SolidPaint)) {
            throw new IllegalArgumentException("Currently XSLF only supports SolidPaint");
        }

        // TODO: implement setting bullet color to null
        SolidPaint sp = (SolidPaint)color;
        Color col = DrawPaint.applyColorTransform(sp.getSolidColor());
        
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTColor c = pr.isSetBuClr() ? pr.getBuClr() : pr.addNewBuClr();
        CTSRgbColor clr = c.isSetSrgbClr() ? c.getSrgbClr() : c.addNewSrgbClr();
        clr.setVal(new byte[]{(byte) col.getRed(), (byte) col.getGreen(), (byte) col.getBlue()});
    }

    /**
     * Returns the bullet size that is to be used within a paragraph.
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If bulletSize >= 0, then bulletSize is a percentage of the font size.
     * If bulletSize < 0, then it specifies the size in points
     * </p>
     *
     * @return the bullet size
     */
    @SuppressWarnings("WeakerAccess")
    public Double getBulletFontSize(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetBuSzPct()){
                    setValue(props.getBuSzPct().getVal() * 0.001);
                    return true;
                }
                if(props.isSetBuSzPts()){
                    setValue( - props.getBuSzPts().getVal() * 0.01);
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();
    }

    /**
     * Sets the bullet size that is to be used within a paragraph.
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If bulletSize >= 0, then bulletSize is a percentage of the font size.
     * If bulletSize < 0, then it specifies the size in points
     * </p>
     */
    @SuppressWarnings("WeakerAccess")
    public void setBulletFontSize(double bulletSize){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();

        if(bulletSize >= 0) {
            CTTextBulletSizePercent pt = pr.isSetBuSzPct() ? pr.getBuSzPct() : pr.addNewBuSzPct();
            pt.setVal((int)(bulletSize*1000));
            if(pr.isSetBuSzPts()) pr.unsetBuSzPts();
        } else {
            CTTextBulletSizePoint pt = pr.isSetBuSzPts() ? pr.getBuSzPts() : pr.addNewBuSzPts();
            pt.setVal((int)(-bulletSize*100));
            if(pr.isSetBuSzPct()) pr.unsetBuSzPct();
        }
   }

    /**
     * @return the auto numbering scheme, or null if not defined
     */
    @SuppressWarnings("WeakerAccess")
    public AutoNumberingScheme getAutoNumberingScheme() {
        ParagraphPropertyFetcher<AutoNumberingScheme> fetcher = new ParagraphPropertyFetcher<AutoNumberingScheme>(getIndentLevel()) {
            public boolean fetch(CTTextParagraphProperties props) {
                if (props.isSetBuAutoNum()) {
                    AutoNumberingScheme ans = AutoNumberingScheme.forOoxmlID(props.getBuAutoNum().getType().intValue());
                    if (ans != null) {
                        setValue(ans);
                        return true;
                    }
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();
    }

    /**
     * @return the auto numbering starting number, or null if not defined
     */
    @SuppressWarnings("WeakerAccess")
    public Integer getAutoNumberingStartAt() {
        ParagraphPropertyFetcher<Integer> fetcher = new ParagraphPropertyFetcher<Integer>(getIndentLevel()) {
            public boolean fetch(CTTextParagraphProperties props) {
                if (props.isSetBuAutoNum()) {
                    if (props.getBuAutoNum().isSetStartAt()) {
                        setValue(props.getBuAutoNum().getStartAt());
                        return true;
                    }
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();
    }
    
    
    @Override
    public void setIndent(Double indent){
        if ((indent == null) && !_p.isSetPPr()) return;
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(indent == null) {
            if(pr.isSetIndent()) pr.unsetIndent();
        } else {
            pr.setIndent(Units.toEMU(indent));
        }
    }

    @Override
    public Double getIndent() {

        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetIndent()){
                    setValue(Units.toPoints(props.getIndent()));
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);

        return fetcher.getValue();
    }

    @Override
    public void setLeftMargin(Double leftMargin){
        if (leftMargin == null && !_p.isSetPPr()) return;
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if (leftMargin == null) {
            if(pr.isSetMarL()) pr.unsetMarL();
        } else {
            pr.setMarL(Units.toEMU(leftMargin));
        }

    }

    /**
     * @return the left margin (in points) of the paragraph, null if unset
     */
    @Override
    public Double getLeftMargin(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetMarL()){
                    double val = Units.toPoints(props.getMarL());
                    setValue(val);
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        // if the marL attribute is omitted, then a value of 347663 is implied
        return fetcher.getValue();
    }

    @Override
    public void setRightMargin(Double rightMargin){
        if (rightMargin == null && !_p.isSetPPr()) return;
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(rightMargin == null) {
            if(pr.isSetMarR()) pr.unsetMarR();
        } else {
            pr.setMarR(Units.toEMU(rightMargin));
        }
    }

    /**
     *
     * @return the right margin of the paragraph, null if unset
     */
    @Override
    public Double getRightMargin(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetMarR()){
                    double val = Units.toPoints(props.getMarR());
                    setValue(val);
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();
    }

    @Override
    public Double getDefaultTabSize(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetDefTabSz()){
                    double val = Units.toPoints(props.getDefTabSz());
                    setValue(val);
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();
    }

    @SuppressWarnings("WeakerAccess")
    public double getTabStop(final int idx) {
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if (props.isSetTabLst()) {
                    CTTextTabStopList tabStops = props.getTabLst();
                    if(idx < tabStops.sizeOfTabArray() ) {
                        CTTextTabStop ts = tabStops.getTabArray(idx);
                        double val = Units.toPoints(ts.getPos());
                        setValue(val);
                        return true;
                    }
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue() == null ? 0. : fetcher.getValue();
    }

    @SuppressWarnings("WeakerAccess")
    public void addTabStop(double value){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextTabStopList tabStops = pr.isSetTabLst() ? pr.getTabLst() : pr.addNewTabLst();
        tabStops.addNewTab().setPos(Units.toEMU(value));
    }

    @Override
    public void setLineSpacing(Double lineSpacing){
        setSpacing(lineSpacing, props -> props::getLnSpc, props -> props::addNewLnSpc, props -> props::unsetLnSpc);
    }

    @Override
    public Double getLineSpacing(){
        final Double lnSpc = getSpacing(props -> props::getLnSpc);
        if (lnSpc != null && lnSpc > 0) {
            // check if the percentage value is scaled
            final CTTextNormalAutofit normAutofit = getParentShape().getTextBodyPr().getNormAutofit();
            if (normAutofit != null) {
                final double scale = 1 - (double)normAutofit.getLnSpcReduction() / 100000;
                return lnSpc * scale;
            }
        }
        
        return lnSpc;
    }

    @Override
    public void setSpaceBefore(Double spaceBefore){
        setSpacing(spaceBefore, props -> props::getSpcBef, props -> props::addNewSpcBef, props -> props::unsetSpcBef);
    }

    @Override
    public Double getSpaceBefore(){
        return getSpacing(props -> props::getSpcBef);
    }

    @Override
    public void setSpaceAfter(Double spaceAfter){
        setSpacing(spaceAfter, props -> props::getSpcAft, props -> props::addNewSpcAft, props -> props::unsetSpcAft);
    }

    @Override
    public Double getSpaceAfter() {
        return getSpacing(props -> props::getSpcAft);
    }

    private void setSpacing(final Double space,
        final Function<CTTextParagraphProperties,Supplier<CTTextSpacing>> getSpc,
        final Function<CTTextParagraphProperties,Supplier<CTTextSpacing>> addSpc,
        final Function<CTTextParagraphProperties,Procedure> unsetSpc
    ) {
        final CTTextParagraphProperties pPr = (space == null || _p.isSetPPr()) ?  _p.getPPr() : _p.addNewPPr();
        if (pPr == null) {
            return;
        }

        CTTextSpacing spc = getSpc.apply(pPr).get();

        if (space == null) {
            if (spc != null) {
                // unset the space before on null input
                unsetSpc.apply(pPr).accept();
            }
            return;
        }

        if (spc == null) {
            spc = addSpc.apply(pPr).get();
        }

        if (space >= 0) {
            if (spc.isSetSpcPts()) {
                spc.unsetSpcPts();
            }
            final CTTextSpacingPercent pct = spc.isSetSpcPct() ? spc.getSpcPct() : spc.addNewSpcPct();
            pct.setVal((int)(space*1000));
        } else {
            if (spc.isSetSpcPct()) {
                spc.unsetSpcPct();
            }
            final CTTextSpacingPoint pts = spc.isSetSpcPts() ? spc.getSpcPts() : spc.addNewSpcPts();
            pts.setVal((int)(-space*100));
        }
    }

    private Double getSpacing(final Function<CTTextParagraphProperties,Supplier<CTTextSpacing>> getSpc) {
        final ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getIndentLevel()){
            public boolean fetch(final CTTextParagraphProperties props){
                final CTTextSpacing spc = getSpc.apply(props).get();

                if (spc == null) {
                    return false;
                }

                if (spc.isSetSpcPct()) {
                    setValue( spc.getSpcPct().getVal()*0.001 );
                    return true;
                }

                if (spc.isSetSpcPts()) {
                    setValue( -spc.getSpcPts().getVal()*0.01 );
                    return true;
                }

                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();
    }

    @Override
    public void setIndentLevel(int level){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        pr.setLvl(level);
    }

    @Override
    public int getIndentLevel() {
        CTTextParagraphProperties pr = _p.getPPr();
        return (pr == null || !pr.isSetLvl()) ? 0 : pr.getLvl();
    }

    /**
     * Returns whether this paragraph has bullets
     */
    public boolean isBullet() {
        ParagraphPropertyFetcher<Boolean> fetcher = new ParagraphPropertyFetcher<Boolean>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetBuNone()) {
                    setValue(false);
                    return true;
                }
                if(props.isSetBuFont() || props.isSetBuChar()){
                    setValue(true);
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    /**
     *
     * @param flag whether text in this paragraph has bullets
     */
    public void setBullet(boolean flag) {
        if(isBullet() == flag) return;

        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(flag) {
            pr.addNewBuFont().setTypeface("Arial");
            pr.addNewBuChar().setChar("\u2022");
        } else {
            if (pr.isSetBuFont()) pr.unsetBuFont();
            if (pr.isSetBuChar()) pr.unsetBuChar();
            if (pr.isSetBuAutoNum()) pr.unsetBuAutoNum();
            if (pr.isSetBuBlip()) pr.unsetBuBlip();
            if (pr.isSetBuClr()) pr.unsetBuClr();
            if (pr.isSetBuClrTx()) pr.unsetBuClrTx();
            if (pr.isSetBuFont()) pr.unsetBuFont();
            if (pr.isSetBuFontTx()) pr.unsetBuFontTx();
            if (pr.isSetBuSzPct()) pr.unsetBuSzPct();
            if (pr.isSetBuSzPts()) pr.unsetBuSzPts();
            if (pr.isSetBuSzTx()) pr.unsetBuSzTx();
            pr.addNewBuNone();
        }
    }

    /**
     * Specifies that automatic numbered bullet points should be applied to this paragraph
     *
     * @param scheme type of auto-numbering
     * @param startAt the number that will start number for a given sequence of automatically
    numbered bullets (1-based).
     */
    @SuppressWarnings("WeakerAccess")
    public void setBulletAutoNumber(AutoNumberingScheme scheme, int startAt) {
        if(startAt < 1) throw new IllegalArgumentException("Start Number must be greater or equal that 1") ;
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextAutonumberBullet lst = pr.isSetBuAutoNum() ? pr.getBuAutoNum() : pr.addNewBuAutoNum();
        lst.setType(STTextAutonumberScheme.Enum.forInt(scheme.ooxmlId));
        lst.setStartAt(startAt);
    }

    @Override
    public String toString(){
        return "[" + getClass() + "]" + getText();
    }


    /**
     * @return master style text paragraph properties, or <code>null</code> if 
     * there are no master slides or the master slides do not contain a text paragraph
     */
    private CTTextParagraphProperties getDefaultMasterStyle(){
        CTPlaceholder ph = _shape.getPlaceholderDetails().getCTPlaceholder(false);
        String defaultStyleSelector;  
        switch(ph == null ? -1 : ph.getType().intValue()) {
            case STPlaceholderType.INT_TITLE:
            case STPlaceholderType.INT_CTR_TITLE:
                defaultStyleSelector = "titleStyle";
                break;
            case -1: // no placeholder means plain text box
            case STPlaceholderType.INT_FTR:
            case STPlaceholderType.INT_SLD_NUM:
            case STPlaceholderType.INT_DT:
                defaultStyleSelector = "otherStyle";
                break;
            default:
                defaultStyleSelector = "bodyStyle";
                break;
        }
        int level = getIndentLevel();

        // wind up and find the root master sheet which must be slide master
        final String nsPML = "http://schemas.openxmlformats.org/presentationml/2006/main";
        XSLFSheet masterSheet = _shape.getSheet();
        for (XSLFSheet m = masterSheet; m != null; m = (XSLFSheet)m.getMasterSheet()) {
            masterSheet = m;
            XmlObject xo = masterSheet.getXmlObject();
            XmlCursor cur = xo.newCursor();
            try {
                cur.push();
                if ((cur.toChild(nsPML, "txStyles") && cur.toChild(nsPML, defaultStyleSelector)) ||
            		(cur.pop() && cur.toChild(nsPML, "notesStyle"))) {
                    while (level >= 0) {
                        cur.push();
                    	if (cur.toChild(XSLFRelation.NS_DRAWINGML, "lvl" +(level+1)+ "pPr")) {
                    		return (CTTextParagraphProperties)cur.getObject();
                    	}
                    	cur.pop();
                    	level--;
                    }
                }
            } finally {
            	cur.dispose();
            }
        }
        
        return null;
    }

    private void fetchParagraphProperty(final ParagraphPropertyFetcher<?> visitor){
        final XSLFTextShape shape = getParentShape();
        final XSLFSheet sheet = shape.getSheet();
        
        if (!(sheet instanceof XSLFSlideMaster)) {
            if (_p.isSetPPr() && visitor.fetch(_p.getPPr())) {
                return;
            }
    
            if (shape.fetchShapeProperty(visitor)) {
                return;
            }

            if (fetchThemeProperty(visitor)) {
                return;
            }
        }

        fetchMasterProperty(visitor);
    }

    void fetchMasterProperty(final ParagraphPropertyFetcher<?> visitor) {
        // defaults for placeholders are defined in the slide master
        final CTTextParagraphProperties defaultProps = getDefaultMasterStyle();
        // TODO: determine master shape
        if (defaultProps != null) {
            visitor.fetch(defaultProps);
        }
    }

    boolean fetchThemeProperty(final ParagraphPropertyFetcher<?> visitor) {
        final XSLFTextShape shape = getParentShape();

        if (shape.isPlaceholder()) {
            return false;
        }

        // if it is a plain text box then take defaults from presentation.xml
        @SuppressWarnings("resource")
        final XMLSlideShow ppt = shape.getSheet().getSlideShow();
        final CTTextParagraphProperties themeProps = ppt.getDefaultParagraphStyle(getIndentLevel());
        return themeProps != null && visitor.fetch(themeProps);
    }

    void copy(XSLFTextParagraph other){
        if (other == this) return;
        
        CTTextParagraph thisP = getXmlObject();
        CTTextParagraph otherP = other.getXmlObject();
        
        if (thisP.isSetPPr()) thisP.unsetPPr();
        if (thisP.isSetEndParaRPr()) thisP.unsetEndParaRPr();
        
        _runs.clear();
        for (int i=thisP.sizeOfBrArray(); i>0; i--) {
            thisP.removeBr(i-1);
        }
        for (int i=thisP.sizeOfRArray(); i>0; i--) {
            thisP.removeR(i-1);
        }
        for (int i=thisP.sizeOfFldArray(); i>0; i--) {
            thisP.removeFld(i-1);
        }

        XmlCursor thisC = thisP.newCursor();
        thisC.toEndToken();
        XmlCursor otherC = otherP.newCursor();
        otherC.copyXmlContents(thisC);
        otherC.dispose();
        thisC.dispose();
        
        for (XSLFTextRun tr : other.getTextRuns()) {
            XmlObject xo = tr.getXmlObject();
            XSLFTextRun run = (xo instanceof CTTextLineBreak)
                ? newTextRun((CTTextLineBreak)xo)
                : newTextRun(xo);
            run.copy(tr);
            _runs.add(run);
        }

        // set properties again, in case we are based on a different
        // template
        TextAlign srcAlign = other.getTextAlign();
        if(srcAlign != getTextAlign()){
            setTextAlign(srcAlign);
        }

        boolean isBullet = other.isBullet();
        if(isBullet != isBullet()){
            setBullet(isBullet);
            if(isBullet) {
                String buFont = other.getBulletFont();
                if(buFont != null && !buFont.equals(getBulletFont())){
                    setBulletFont(buFont);
                }
                String buChar = other.getBulletCharacter();
                if(buChar != null && !buChar.equals(getBulletCharacter())){
                    setBulletCharacter(buChar);
                }
                PaintStyle buColor = other.getBulletFontColor();
                if(buColor != null && !buColor.equals(getBulletFontColor())){
                    setBulletFontColor(buColor);
                }
                Double buSize = other.getBulletFontSize();
                if(doubleNotEquals(buSize, getBulletFontSize())){
                    setBulletFontSize(buSize);
                }
            }
        }

        Double leftMargin = other.getLeftMargin();
        if (doubleNotEquals(leftMargin, getLeftMargin())){
            setLeftMargin(leftMargin);
        }

        Double indent = other.getIndent();
        if (doubleNotEquals(indent, getIndent())) {
            setIndent(indent);
        }

        Double spaceAfter = other.getSpaceAfter();
        if (doubleNotEquals(spaceAfter, getSpaceAfter())) {
            setSpaceAfter(spaceAfter);
        }
        
        Double spaceBefore = other.getSpaceBefore();
        if (doubleNotEquals(spaceBefore, getSpaceBefore())) {
            setSpaceBefore(spaceBefore);
        }
        
        Double lineSpacing = other.getLineSpacing();
        if (doubleNotEquals(lineSpacing, getLineSpacing())) {
            setLineSpacing(lineSpacing);
        }
    }

    private static boolean doubleNotEquals(Double d1, Double d2) {
        return !Objects.equals(d1, d2);
    }
    
    @Override
    public Double getDefaultFontSize() {
        CTTextCharacterProperties endPr = _p.getEndParaRPr();
        if (endPr == null || !endPr.isSetSz()) {
            // inherit the font size from the master style
            CTTextParagraphProperties masterStyle = getDefaultMasterStyle();
            if (masterStyle != null) {
                endPr = masterStyle.getDefRPr();
            }
        }
        return (endPr == null || !endPr.isSetSz()) ? 12 : (endPr.getSz() / 100.);
    }

    @Override
    public String getDefaultFontFamily() {
        return (_runs.isEmpty() ? "Arial" : _runs.get(0).getFontFamily());
    }

    @Override
    public BulletStyle getBulletStyle() {
        if (!isBullet()) return null;
        return new BulletStyle(){
            @Override
            public String getBulletCharacter() {
                return XSLFTextParagraph.this.getBulletCharacter();
            }

            @Override
            public String getBulletFont() {
                return XSLFTextParagraph.this.getBulletFont();
            }

            @Override
            public Double getBulletFontSize() {
                return XSLFTextParagraph.this.getBulletFontSize();
            }

            @Override
            public PaintStyle getBulletFontColor() {
                return XSLFTextParagraph.this.getBulletFontColor();
            }
            
            @Override
            public void setBulletFontColor(Color color) {
                setBulletFontColor(DrawPaint.createSolidPaint(color));
            }

            @Override
            public void setBulletFontColor(PaintStyle color) {
                XSLFTextParagraph.this.setBulletFontColor(color);
            }
            
            @Override
            public AutoNumberingScheme getAutoNumberingScheme() {
                return XSLFTextParagraph.this.getAutoNumberingScheme();
            }

            @Override
            public Integer getAutoNumberingStartAt() {
                return XSLFTextParagraph.this.getAutoNumberingStartAt();
            }

        };
    }

    @Override
    public void setBulletStyle(Object... styles) {
        if (styles.length == 0) {
            setBullet(false);
        } else {
            setBullet(true);
            for (Object ostyle : styles) {
                if (ostyle instanceof Number) {
                    setBulletFontSize(((Number)ostyle).doubleValue());
                } else if (ostyle instanceof Color) {
                    setBulletFontColor((Color)ostyle);
                } else if (ostyle instanceof Character) {
                    setBulletCharacter(ostyle.toString());
                } else if (ostyle instanceof String) {
                    setBulletFont((String)ostyle);
                } else if (ostyle instanceof AutoNumberingScheme) {
                    setBulletAutoNumber((AutoNumberingScheme)ostyle, 0);
                }
            }
        }
    }

    @Override
    public List<XSLFTabStop> getTabStops() {
        ParagraphPropertyFetcher<List<XSLFTabStop>> fetcher = new ParagraphPropertyFetcher<List<XSLFTabStop>>(getIndentLevel()){
            public boolean fetch(CTTextParagraphProperties props) {
                if (props.isSetTabLst()) {
                    final List<XSLFTabStop> list = new ArrayList<>();
                    //noinspection deprecation
                    for (final CTTextTabStop ta : props.getTabLst().getTabArray()) {
                        list.add(new XSLFTabStop(ta));
                    }
                    setValue(list);
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue();        
    }

    @Override
    public void addTabStops(double positionInPoints, TabStopType tabStopType) {
        final XSLFSheet sheet = getParentShape().getSheet();
        final CTTextParagraphProperties tpp;
        if (sheet instanceof XSLFSlideMaster) {
            tpp = getDefaultMasterStyle();
        } else {
            final CTTextParagraph xo = getXmlObject();
            tpp = (xo.isSetPPr()) ? xo.getPPr() : xo.addNewPPr();
        }

        if (tpp == null) {
            return;
        }
        final CTTextTabStopList stl = (tpp.isSetTabLst()) ? tpp.getTabLst() : tpp.addNewTabLst();
        XSLFTabStop tab = new XSLFTabStop(stl.addNewTab());
        tab.setPositionInPoints(positionInPoints);
        tab.setType(tabStopType);
    }

    @Override
    public void clearTabStops() {
        final XSLFSheet sheet = getParentShape().getSheet();
        CTTextParagraphProperties tpp = (sheet instanceof XSLFSlideMaster) ? getDefaultMasterStyle() : getXmlObject().getPPr();
        if (tpp != null && tpp.isSetTabLst()) {
            tpp.unsetTabLst();
        }
    }

    /**
     * Helper method for appending text and keeping paragraph and character properties.
     * The character properties are moved to the end paragraph marker
     */
    /* package */ void clearButKeepProperties() {
        CTTextParagraph thisP = getXmlObject();
        for (int i=thisP.sizeOfBrArray(); i>0; i--) {
            thisP.removeBr(i-1);
        }
        for (int i=thisP.sizeOfFldArray(); i>0; i--) {
            thisP.removeFld(i-1);
        }
        if (!_runs.isEmpty()) {
            int size = _runs.size();
            XSLFTextRun lastRun = _runs.get(size-1);
            CTTextCharacterProperties cpOther = lastRun.getRPr(false);
            if (cpOther != null) {
                if (thisP.isSetEndParaRPr()) {
                    thisP.unsetEndParaRPr();
                }
                CTTextCharacterProperties cp = thisP.addNewEndParaRPr();
                cp.set(cpOther);
            }
            for (int i=size; i>0; i--) {
                thisP.removeR(i-1);
            }
            _runs.clear();
        }
    }

    @Override
    public boolean isHeaderOrFooter() {
        CTPlaceholder ph = _shape.getPlaceholderDetails().getCTPlaceholder(false);
        int phId = (ph == null ? -1 : ph.getType().intValue());
        switch (phId) {
            case STPlaceholderType.INT_SLD_NUM:
            case STPlaceholderType.INT_DT:
            case STPlaceholderType.INT_FTR:
            case STPlaceholderType.INT_HDR:
                return true;
            default:
                return false;
        }
    }

    /**
     * Helper method to allow subclasses to provide their own text run
     *
     * @param r the xml reference
     * 
     * @return a new text paragraph
     * 
     * @since POI 3.15-beta2
     */
    protected XSLFTextRun newTextRun(XmlObject r) {
        return new XSLFTextRun(r, this);
    }

    @SuppressWarnings("WeakerAccess")
    protected XSLFTextRun newTextRun(CTTextLineBreak r) {
        return new XSLFLineBreak(r, this);
    }
}
