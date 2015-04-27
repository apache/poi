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
import java.util.*;

import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.util.*;
import org.apache.poi.xslf.model.ParagraphPropertyFetcher;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType;

/**
 * Represents a paragraph of text within the containing text body.
 * The paragraph is the highest level text separation mechanism.
 *
 * @author Yegor Kozlov
 * @since POI-3.8
 */
@Beta
public class XSLFTextParagraph implements TextParagraph<XSLFTextRun> {
    private final CTTextParagraph _p;
    private final List<XSLFTextRun> _runs;
    private final XSLFTextShape _shape;

    XSLFTextParagraph(CTTextParagraph p, XSLFTextShape shape){
        _p = p;
        _runs = new ArrayList<XSLFTextRun>();
        _shape = shape;

        for(XmlObject ch : _p.selectPath("*")){
            if(ch instanceof CTRegularTextRun){
                CTRegularTextRun r = (CTRegularTextRun)ch;
                _runs.add(new XSLFTextRun(r, this));
            } else if (ch instanceof CTTextLineBreak){
                CTTextLineBreak br = (CTTextLineBreak)ch;
                CTRegularTextRun r = CTRegularTextRun.Factory.newInstance();
                r.setRPr(br.getRPr());
                r.setT("\n");
                _runs.add(new XSLFTextRun(r, this));
            } else if (ch instanceof CTTextField){
                CTTextField f = (CTTextField)ch;
                CTRegularTextRun r = CTRegularTextRun.Factory.newInstance();
                r.setRPr(f.getRPr());
                r.setT(f.getT());
                _runs.add(new XSLFTextRun(r, this));
            }
        }
    }

    public String getText(){
        StringBuilder out = new StringBuilder();
        for (XSLFTextRun r : _runs) {
            out.append(r.getRawText());
        }
        return out.toString();
    }

    String getRenderableText(){
        StringBuilder out = new StringBuilder();
        for (XSLFTextRun r : _runs) {
            out.append(r.getRenderableText());
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
        XSLFTextRun run = new XSLFTextRun(r, this);
        _runs.add(run);
        return run;
    }

    /**
     * Insert a line break
     *
     * @return text run representing this line break ('\n')
     */
    public XSLFTextRun addLineBreak(){
        CTTextLineBreak br = _p.addNewBr();
        CTTextCharacterProperties brProps = br.addNewRPr();
        if(_runs.size() > 0){
            // by default line break has the font size of the last text run
            CTTextCharacterProperties prevRun = _runs.get(_runs.size() - 1).getRPr();
            brProps.set(prevRun);
        }
        CTRegularTextRun r = CTRegularTextRun.Factory.newInstance();
        r.setRPr(brProps);
        r.setT("\n");
        XSLFTextRun run = new XSLFLineBreak(r, this, brProps);
        _runs.add(run);
        return run;
    }

    /**
     * Returns the alignment that is applied to the paragraph.
     *
     * If this attribute is omitted, then a value of left is implied.
     * @return ??? alignment that is applied to the paragraph
     */
    public TextAlign getTextAlign(){
        ParagraphPropertyFetcher<TextAlign> fetcher = new ParagraphPropertyFetcher<TextAlign>(getLevel()){
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
        return fetcher.getValue() == null ? TextAlign.LEFT : fetcher.getValue();
    }

    /**
     * Specifies the alignment that is to be applied to the paragraph.
     * Possible values for this include left, right, centered, justified and distributed,
     * see {@link org.apache.poi.sl.usermodel.TextAlign}.
     *
     * @param align text align
     */
    public void setTextAlign(TextAlign align){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(align == null) {
            if(pr.isSetAlgn()) pr.unsetAlgn();
        } else {
            pr.setAlgn(STTextAlignType.Enum.forInt(align.ordinal() + 1));
        }
    }

    @Override
    public FontAlign getFontAlign(){
        ParagraphPropertyFetcher<FontAlign> fetcher = new ParagraphPropertyFetcher<FontAlign>(getLevel()){
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
        return fetcher.getValue() == null ? FontAlign.AUTO : fetcher.getValue();
    }

    /**
     * Specifies the font alignment that is to be applied to the paragraph.
     * Possible values for this include auto, top, center, baseline and bottom.
     * see {@link org.apache.poi.sl.usermodel.TextParagraph.FontAlign}.
     *
     * @param align font align
     */
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
    public String getBulletFont(){
        ParagraphPropertyFetcher<String> fetcher = new ParagraphPropertyFetcher<String>(getLevel()){
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

    public void setBulletFont(String typeface){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextFont font = pr.isSetBuFont() ? pr.getBuFont() : pr.addNewBuFont();
        font.setTypeface(typeface);
    }

    /**
     * @return the character to be used in place of the standard bullet point
     */
    public String getBulletCharacter(){
        ParagraphPropertyFetcher<String> fetcher = new ParagraphPropertyFetcher<String>(getLevel()){
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
    public Color getBulletFontColor(){
        final XSLFTheme theme = getParentShape().getSheet().getTheme();
        ParagraphPropertyFetcher<Color> fetcher = new ParagraphPropertyFetcher<Color>(getLevel()){
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
        return fetcher.getValue();
    }

    /**
     * Set the color to be used on bullet characters within a given paragraph.
     *
     * @param color the bullet color
     */
    public void setBulletFontColor(Color color){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTColor c = pr.isSetBuClr() ? pr.getBuClr() : pr.addNewBuClr();
        CTSRgbColor clr = c.isSetSrgbClr() ? c.getSrgbClr() : c.addNewSrgbClr();
        clr.setVal(new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()});
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
    public double getBulletFontSize(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
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
        return fetcher.getValue() == null ? 100 : fetcher.getValue();
    }

    /**
     * Sets the bullet size that is to be used within a paragraph.
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If bulletSize >= 0, then bulletSize is a percentage of the font size.
     * If bulletSize < 0, then it specifies the size in points
     * </p>
     */
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
     * Specifies the indent size that will be applied to the first line of text in the paragraph.
     *
     * @param value the indent in points. 
     */
    @Override
    public void setIndent(double value){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(value == -1) {
            if(pr.isSetIndent()) pr.unsetIndent();
        } else {
            pr.setIndent(Units.toEMU(value));
        }
    }

    /**
     *
     * @return the indent applied to the first line of text in the paragraph.
     */
    @Override
    public double getIndent(){

        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetIndent()){
                    setValue(Units.toPoints(props.getIndent()));
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);

        return fetcher.getValue() == null ? 0 : fetcher.getValue();
    }

    /**
     * Specifies the left margin of the paragraph. This is specified in addition to the text body
     * inset and applies only to this text paragraph. That is the text body Inset and the LeftMargin
     * attributes are additive with respect to the text position.
     *
     * @param value the left margin (in points) of the paragraph
     */
    @Override
    public void setLeftMargin(double value){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(value == -1) {
            if(pr.isSetMarL()) pr.unsetMarL();
        } else {
            pr.setMarL(Units.toEMU(value));
        }

    }

    /**
     * @return the left margin (in points) of the paragraph
     */
    @Override
    public double getLeftMargin(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
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
        return fetcher.getValue() == null ? 0 : fetcher.getValue();
    }

    /**
     * Specifies the right margin of the paragraph. This is specified in addition to the text body
     * inset and applies only to this text paragraph. That is the text body Inset and the RightMargin
     * attributes are additive with respect to the text position.
     *
     * @param value the right margin (in points) of the paragraph
     */
    @Override
    public void setRightMargin(double value){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(value == -1) {
            if(pr.isSetMarR()) pr.unsetMarR();
        } else {
            pr.setMarR(Units.toEMU(value));
        }
    }

    /**
     *
     * @return the right margin of the paragraph
     */
    @Override
    public double getRightMargin(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
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
        // if the marL attribute is omitted, then a value of 347663 is implied
        return fetcher.getValue() == null ? 0 : fetcher.getValue();
    }

    /**
     *
     * @return the default size for a tab character within this paragraph in points
     */
    public double getDefaultTabSize(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
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
        return fetcher.getValue() == null ? 0 : fetcher.getValue();
    }

    public double getTabStop(final int idx){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetTabLst()){
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

    public void addTabStop(double value){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextTabStopList tabStops = pr.isSetTabLst() ? pr.getTabLst() : pr.addNewTabLst();
        tabStops.addNewTab().setPos(Units.toEMU(value));
    }

    /**
     * This element specifies the vertical line spacing that is to be used within a paragraph.
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If linespacing >= 0, then linespacing is a percentage of normal line height
     * If linespacing < 0, the absolute value of linespacing is the spacing in points
     * </p>
     * Examples:
     * <pre><code>
     *      // spacing will be 120% of the size of the largest text on each line
     *      paragraph.setLineSpacing(120);
     *
     *      // spacing will be 200% of the size of the largest text on each line
     *      paragraph.setLineSpacing(200);
     *
     *      // spacing will be 48 points
     *      paragraph.setLineSpacing(-48.0);
     * </code></pre>
     * 
     * @param linespacing the vertical line spacing
     */
    public void setLineSpacing(double linespacing){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextSpacing spc = CTTextSpacing.Factory.newInstance();
        if(linespacing >= 0) spc.addNewSpcPct().setVal((int)(linespacing*1000));
        else spc.addNewSpcPts().setVal((int)(-linespacing*100));
        pr.setLnSpc(spc);
    }

    /**
     * Returns the vertical line spacing that is to be used within a paragraph.
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If linespacing >= 0, then linespacing is a percentage of normal line height.
     * If linespacing < 0, the absolute value of linespacing is the spacing in points
     * </p>
     *
     * @return the vertical line spacing.
     */
    public double getLineSpacing(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetLnSpc()){
                    CTTextSpacing spc = props.getLnSpc();

                    if(spc.isSetSpcPct()) setValue( spc.getSpcPct().getVal()*0.001 );
                    else if (spc.isSetSpcPts()) setValue( -spc.getSpcPts().getVal()*0.01 );
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);

        double lnSpc = fetcher.getValue() == null ? 100 : fetcher.getValue();
        if(lnSpc > 0) {
            // check if the percentage value is scaled
            CTTextNormalAutofit normAutofit = getParentShape().getTextBodyPr().getNormAutofit();
            if(normAutofit != null) {
                double scale = 1 - (double)normAutofit.getLnSpcReduction() / 100000;
                lnSpc *= scale;
            }
        }
        
        return lnSpc;
    }

    /**
     * Set the amount of vertical white space that will be present before the paragraph.
     * This space is specified in either percentage or points:
     * <p>
     * If spaceBefore >= 0, then space is a percentage of normal line height.
     * If spaceBefore < 0, the absolute value of linespacing is the spacing in points
     * </p>
     * Examples:
     * <pre><code>
     *      // The paragraph will be formatted to have a spacing before the paragraph text.
     *      // The spacing will be 200% of the size of the largest text on each line
     *      paragraph.setSpaceBefore(200);
     *
     *      // The spacing will be a size of 48 points
     *      paragraph.setSpaceBefore(-48.0);
     * </code></pre>
     *
     * @param spaceBefore the vertical white space before the paragraph.
     */
    public void setSpaceBefore(double spaceBefore){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextSpacing spc = CTTextSpacing.Factory.newInstance();
        if(spaceBefore >= 0) spc.addNewSpcPct().setVal((int)(spaceBefore*1000));
        else spc.addNewSpcPts().setVal((int)(-spaceBefore*100));
        pr.setSpcBef(spc);
    }

    /**
     * The amount of vertical white space before the paragraph
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If spaceBefore >= 0, then space is a percentage of normal line height.
     * If spaceBefore < 0, the absolute value of linespacing is the spacing in points
     * </p>
     *
     * @return the vertical white space before the paragraph
     */
    public double getSpaceBefore(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetSpcBef()){
                    CTTextSpacing spc = props.getSpcBef();

                    if(spc.isSetSpcPct()) setValue( spc.getSpcPct().getVal()*0.001 );
                    else if (spc.isSetSpcPts()) setValue( -spc.getSpcPts().getVal()*0.01 );
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);

        double spcBef = fetcher.getValue() == null ? 0 : fetcher.getValue();
        return spcBef;
    }

    /**
     * Set the amount of vertical white space that will be present after the paragraph.
     * This space is specified in either percentage or points:
     * <p>
     * If spaceAfter >= 0, then space is a percentage of normal line height.
     * If spaceAfter < 0, the absolute value of linespacing is the spacing in points
     * </p>
     * Examples:
     * <pre><code>
     *      // The paragraph will be formatted to have a spacing after the paragraph text.
     *      // The spacing will be 200% of the size of the largest text on each line
     *      paragraph.setSpaceAfter(200);
     *
     *      // The spacing will be a size of 48 points
     *      paragraph.setSpaceAfter(-48.0);
     * </code></pre>
     *
     * @param spaceAfter the vertical white space after the paragraph.
     */
    public void setSpaceAfter(double spaceAfter){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextSpacing spc = CTTextSpacing.Factory.newInstance();
        if(spaceAfter >= 0) spc.addNewSpcPct().setVal((int)(spaceAfter*1000));
        else spc.addNewSpcPts().setVal((int)(-spaceAfter*100));
        pr.setSpcAft(spc);
    }

    /**
     * The amount of vertical white space after the paragraph
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If spaceBefore >= 0, then space is a percentage of normal line height.
     * If spaceBefore < 0, the absolute value of linespacing is the spacing in points
     * </p>
     *
     * @return the vertical white space after the paragraph
     */
    public double getSpaceAfter(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetSpcAft()){
                    CTTextSpacing spc = props.getSpcAft();

                    if(spc.isSetSpcPct()) setValue( spc.getSpcPct().getVal()*0.001 );
                    else if (spc.isSetSpcPts()) setValue( -spc.getSpcPts().getVal()*0.01 );
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue() == null ? 0 : fetcher.getValue();
    }

    /**
     * Specifies the particular level text properties that this paragraph will follow.
     * The value for this attribute formats the text according to the corresponding level
     * paragraph properties defined in the SlideMaster.
     *
     * @param level the level (0 ... 4)
     */
    public void setLevel(int level){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();

        pr.setLvl(level);
    }

    /**
     *
     * @return the text level of this paragraph (0-based). Default is 0.
     */
    public int getLevel(){
        CTTextParagraphProperties pr = _p.getPPr();
        if(pr == null) return 0;

        return pr.getLvl();

    }

    /**
     * Returns whether this paragraph has bullets
     */
    public boolean isBullet() {
        ParagraphPropertyFetcher<Boolean> fetcher = new ParagraphPropertyFetcher<Boolean>(getLevel()){
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
        if(!flag) {
            pr.addNewBuNone();
        } else {
            pr.addNewBuFont().setTypeface("Arial");
            pr.addNewBuChar().setChar("\u2022");
        }
    }

    /**
     * Specifies that automatic numbered bullet points should be applied to this paragraph
     *
     * @param scheme type of auto-numbering
     * @param startAt the number that will start number for a given sequence of automatically
    numbered bullets (1-based).
     */
    public void setBulletAutoNumber(ListAutoNumber scheme, int startAt) {
        if(startAt < 1) throw new IllegalArgumentException("Start Number must be greater or equal that 1") ;
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextAutonumberBullet lst = pr.isSetBuAutoNum() ? pr.getBuAutoNum() : pr.addNewBuAutoNum();
        lst.setType(STTextAutonumberScheme.Enum.forInt(scheme.ordinal() + 1));
        lst.setStartAt(startAt);
    }

    @Override
    public String toString(){
        return "[" + getClass() + "]" + getText();
    }


    CTTextParagraphProperties getDefaultMasterStyle(){
        CTPlaceholder ph = _shape.getCTPlaceholder();
        String defaultStyleSelector;
        if(ph == null) defaultStyleSelector = "otherStyle";   // no placeholder means plain text box
        else {
            switch(ph.getType().intValue()){
                case STPlaceholderType.INT_TITLE:
                case STPlaceholderType.INT_CTR_TITLE:
                    defaultStyleSelector = "titleStyle";
                    break;
                case STPlaceholderType.INT_FTR:
                case STPlaceholderType.INT_SLD_NUM:
                case STPlaceholderType.INT_DT:
                    defaultStyleSelector = "otherStyle";
                    break;
                default:
                    defaultStyleSelector = "bodyStyle";
                    break;
            }
        }
        int level = getLevel();

        // wind up and find the root master sheet which must be slide master
        XSLFSheet masterSheet = _shape.getSheet();
        while (masterSheet.getMasterSheet() != null){
            masterSheet = (XSLFSheet)masterSheet.getMasterSheet();
        }

        XmlObject[] o = masterSheet.getXmlObject().selectPath(
                "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' " +
                "declare namespace a='http://schemas.openxmlformats.org/drawingml/2006/main' " +
                ".//p:txStyles/p:" + defaultStyleSelector +"/a:lvl" +(level+1)+ "pPr");
        if (o.length == 1){
            return (CTTextParagraphProperties)o[0];
        } else {
                o = masterSheet.getXmlObject().selectPath(
                "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' " +
                "declare namespace a='http://schemas.openxmlformats.org/drawingml/2006/main' " +
                ".//p:notesStyle/a:lvl" +(level+1)+ "pPr");
                
            if (o.length == 1){
                return (CTTextParagraphProperties)o[0];
            }
            
            throw new IllegalArgumentException("Failed to fetch default style for " +
                    defaultStyleSelector + " and level=" + level);
        }
    }

    private <T> boolean fetchParagraphProperty(ParagraphPropertyFetcher<T> visitor){
        boolean ok = false;

        if(_p.isSetPPr()) ok = visitor.fetch(_p.getPPr());

        if(!ok) {
            XSLFTextShape shape = getParentShape();
            ok = shape.fetchShapeProperty(visitor);
            if(!ok){
                CTPlaceholder ph = shape.getCTPlaceholder();
                if(ph == null){
                    // if it is a plain text box then take defaults from presentation.xml
                    XMLSlideShow ppt = getParentShape().getSheet().getSlideShow();
                    CTTextParagraphProperties themeProps = ppt.getDefaultParagraphStyle(getLevel());
                    if(themeProps != null) ok = visitor.fetch(themeProps);
                }

                if(!ok){
                    // defaults for placeholders are defined in the slide master
                    CTTextParagraphProperties defaultProps = getDefaultMasterStyle();
                    if(defaultProps != null) ok = visitor.fetch(defaultProps);
                }
            }
        }

        return ok;
    }

    void copy(XSLFTextParagraph p){
        TextAlign srcAlign = p.getTextAlign();
        if(srcAlign != getTextAlign()){
            setTextAlign(srcAlign);
        }

        boolean isBullet = p.isBullet();
        if(isBullet != isBullet()){
            setBullet(isBullet);
            if(isBullet) {
                String buFont = p.getBulletFont();
                if(buFont != null && !buFont.equals(getBulletFont())){
                    setBulletFont(buFont);
                }
                String buChar = p.getBulletCharacter();
                if(buChar != null && !buChar.equals(getBulletCharacter())){
                    setBulletCharacter(buChar);
                }
                Color buColor = p.getBulletFontColor();
                if(buColor != null && !buColor.equals(getBulletFontColor())){
                    setBulletFontColor(buColor);
                }
                double buSize = p.getBulletFontSize();
                if(buSize != getBulletFontSize()){
                    setBulletFontSize(buSize);
                }
            }
        }

        double leftMargin = p.getLeftMargin();
        if(leftMargin != getLeftMargin()){
            setLeftMargin(leftMargin);
        }

        double indent = p.getIndent();
        if(indent != getIndent()){
            setIndent(indent);
        }

        double spaceAfter = p.getSpaceAfter();
        if(spaceAfter != getSpaceAfter()){
            setSpaceAfter(spaceAfter);
        }
        double spaceBefore = p.getSpaceBefore();
        if(spaceBefore != getSpaceBefore()){
            setSpaceBefore(spaceBefore);
        }
        double lineSpacing = p.getLineSpacing();
        if(lineSpacing != getLineSpacing()){
            setLineSpacing(lineSpacing);
        }

        List<XSLFTextRun> srcR = p.getTextRuns();
        List<XSLFTextRun> tgtR = getTextRuns();
        for(int i = 0; i < srcR.size(); i++){
            XSLFTextRun r1 = srcR.get(i);
            XSLFTextRun r2 = tgtR.get(i);
            r2.copy(r1);
        }
    }

    @Override
    public double getDefaultFontSize() {
        CTTextCharacterProperties endPr = _p.getEndParaRPr();
        return (endPr == null || !endPr.isSetSz()) ? 12 : (endPr.getSz() / 100);
    }

    @Override
    public String getDefaultFontFamily() {
        return (_runs.isEmpty() ? "Arial" : _runs.get(0).getFontFamily());
    }

    public BulletStyle getBulletStyle() {
        return new BulletStyle(){
            public String getBulletCharacter() {
                return XSLFTextParagraph.this.getBulletCharacter();
            }

            public String getBulletFont() {
                return XSLFTextParagraph.this.getBulletFont();
            }

            public double getBulletFontSize() {
                return XSLFTextParagraph.this.getBulletFontSize();
            }

            public Color getBulletFontColor() {
                return XSLFTextParagraph.this.getBulletFontColor();
            }
        };
    }
}
