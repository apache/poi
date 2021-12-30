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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.model.ParagraphPropertyFetcher;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape;

/**
 * Represents a paragraph of text within the containing text body.
 * The paragraph is the highest level text separation mechanism.
 */
public class XSSFTextParagraph implements Iterable<XSSFTextRun>{
    private final CTTextParagraph _p;
    private final CTShape _shape;
    private final List<XSSFTextRun> _runs;

    XSSFTextParagraph(CTTextParagraph p, CTShape ctShape){
        _p = p;
        _shape = ctShape;
        _runs = new ArrayList<>();

        for(XmlObject ch : _p.selectPath("*")){
            if(ch instanceof CTRegularTextRun){
                CTRegularTextRun r = (CTRegularTextRun)ch;
                _runs.add(new XSSFTextRun(r, this));
            } else if (ch instanceof CTTextLineBreak){
                CTTextLineBreak br = (CTTextLineBreak)ch;
                CTRegularTextRun r = CTRegularTextRun.Factory.newInstance();
                r.setRPr(br.getRPr());
                r.setT("\n");
                _runs.add(new XSSFTextRun(r, this));
            } else if (ch instanceof CTTextField){
                CTTextField f = (CTTextField)ch;
                CTRegularTextRun r = CTRegularTextRun.Factory.newInstance();
                r.setRPr(f.getRPr());
                r.setT(f.getT());
                _runs.add(new XSSFTextRun(r, this));
            }
        }
    }

    public String getText(){
        StringBuilder out = new StringBuilder();
        for (XSSFTextRun r : _runs) {
            out.append(r.getText());
        }
        return out.toString();
    }

    @Internal
    public CTTextParagraph getXmlObject(){
        return _p;
    }

    @Internal
    public CTShape getParentShape(){
        return _shape;
    }

    public List<XSSFTextRun> getTextRuns(){
        return _runs;
    }

    public Iterator<XSSFTextRun> iterator(){
        return _runs.iterator();
    }

    /**
     * Add a new run of text
     *
     * @return a new run of text
     */
    public XSSFTextRun addNewTextRun(){
        CTRegularTextRun r = _p.addNewR();
        CTTextCharacterProperties rPr = r.addNewRPr();
        rPr.setLang("en-US");
        XSSFTextRun run = new XSSFTextRun(r, this);
        _runs.add(run);
        return run;
    }

    /**
     * Insert a line break
     *
     * @return text run representing this line break ('\n')
     */
    public XSSFTextRun addLineBreak(){
        CTTextLineBreak br = _p.addNewBr();
        CTTextCharacterProperties brProps = br.addNewRPr();
        if (!_runs.isEmpty()) {
            // by default line break has the font size of the last text run
            CTTextCharacterProperties prevRun = _runs.get(_runs.size() - 1).getRPr();
            brProps.set(prevRun);
        }
        CTRegularTextRun r = CTRegularTextRun.Factory.newInstance();
        r.setRPr(brProps);
        r.setT("\n");
        XSSFTextRun run = new XSSFLineBreak(r, this, brProps);
        _runs.add(run);
        return run;
    }

    /**
     * Returns the alignment that is applied to the paragraph.
     *
     * If this attribute is omitted, then a value of left is implied.
     * @return alignment that is applied to the paragraph
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
     * see {@link org.apache.poi.xssf.usermodel.TextAlign}.
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

    /**
     * Returns where vertically on a line of text the actual words are positioned. This deals
     * with vertical placement of the characters with respect to the baselines.
     *
     * If this attribute is omitted, then a value of baseline is implied.
     * @return alignment that is applied to the paragraph
     */
    public TextFontAlign getTextFontAlign(){
        ParagraphPropertyFetcher<TextFontAlign> fetcher = new ParagraphPropertyFetcher<TextFontAlign>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetFontAlgn()){
                    TextFontAlign val = TextFontAlign.values()[props.getFontAlgn().intValue() - 1];
                    setValue(val);
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue() == null ? TextFontAlign.BASELINE : fetcher.getValue();
    }

    /**
     * Determines where vertically on a line of text the actual words are positioned. This deals
     * with vertical placement of the characters with respect to the baselines. For instance
     * having text anchored to the top baseline, anchored to the bottom baseline, centered in
     * between, etc.
     *
     * @param align text font align
     */
    public void setTextFontAlign(TextFontAlign align){
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
        ParagraphPropertyFetcher<Color> fetcher = new ParagraphPropertyFetcher<Color>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetBuClr()){
                    if(props.getBuClr().isSetSrgbClr()){
                        CTSRgbColor clr = props.getBuClr().getSrgbClr();
                        byte[] rgb = clr.getVal();
                        setValue(new Color(0xFF & rgb[0], 0xFF & rgb[1], 0xFF & rgb[2]));
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
     * If bulletSize &gt;= 0, then bulletSize is a percentage of the font size.
     * If bulletSize &lt; 0, then it specifies the size in points
     * </p>
     *
     * @return the bullet size
     */
    public double getBulletFontSize(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetBuSzPct()){
                    setValue(POIXMLUnits.parsePercent(props.getBuSzPct().xgetVal()) * 0.001);
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
     * If bulletSize &gt;= 0, then bulletSize is a percentage of the font size.
     * If bulletSize &lt; 0, then it specifies the size in points
     * </p>
     */
    public void setBulletFontSize(double bulletSize){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();

        if(bulletSize >= 0) {
            // percentage
            CTTextBulletSizePercent pt = pr.isSetBuSzPct() ? pr.getBuSzPct() : pr.addNewBuSzPct();
            pt.setVal(Integer.toString((int)(bulletSize*1000)));
            // unset points if percentage is now set
            if(pr.isSetBuSzPts()) pr.unsetBuSzPts();
        } else {
            // points
            CTTextBulletSizePoint pt = pr.isSetBuSzPts() ? pr.getBuSzPts() : pr.addNewBuSzPts();
            pt.setVal((int)(-bulletSize*100));
            // unset percentage if points is now set
            if(pr.isSetBuSzPct()) pr.unsetBuSzPct();
        }
    }

    /**
     * Specifies the indent size that will be applied to the first line of text in the paragraph.
     *
     * @param value the indent in points, -1 to unset indent and use the default of 0.
     */
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
     * inset and applies only to this text paragraph. That is the text body inset and the LeftMargin
     * attributes are additive with respect to the text position.
     *
     * @param value the left margin of the paragraph, -1 to clear the margin and use the default of 0.
     */
    public void setLeftMargin(double value){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(value == -1) {
            if(pr.isSetMarL()) pr.unsetMarL();
        } else {
            pr.setMarL(Units.toEMU(value));
        }

    }

    /**
     *
     * @return the left margin of the paragraph
     */
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
     * inset and applies only to this text paragraph. That is the text body inset and the marR
     * attributes are additive with respect to the text position.
     *
     * @param value the right margin of the paragraph, -1 to clear the margin and use the default of 0.
     */
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
                    double val = Units.toPoints(POIXMLUnits.parseLength(props.xgetDefTabSz()));
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
                        double val = Units.toPoints(POIXMLUnits.parseLength(ts.xgetPos()));
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
    /**
     * Add a single tab stop to be used on a line of text when there are one or more tab characters
     * present within the text.
     *
     * @param value the position of the tab stop relative to the left margin
     */
    public void addTabStop(double value){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextTabStopList tabStops = pr.isSetTabLst() ? pr.getTabLst() : pr.addNewTabLst();
        tabStops.addNewTab().setPos(Units.toEMU(value));
    }

    /**
     * This element specifies the vertical line spacing that is to be used within a paragraph.
     * This may be specified in two different ways, percentage spacing and font point spacing:
     * <p>
     * If linespacing &gt;= 0, then linespacing is a percentage of normal line height
     * If linespacing &lt; 0, the absolute value of linespacing is the spacing in points
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
     * If linespacing &gt;= 0, then linespacing is a percentage of normal line height.
     * If linespacing &lt; 0, the absolute value of linespacing is the spacing in points
     * </p>
     *
     * @return the vertical line spacing.
     */
    public double getLineSpacing(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetLnSpc()){
                    CTTextSpacing spc = props.getLnSpc();

                    if(spc.isSetSpcPct()) setValue( POIXMLUnits.parsePercent(spc.getSpcPct().xgetVal())*0.001 );
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
            CTTextNormalAutofit normAutofit = _shape.getTxBody().getBodyPr().getNormAutofit();
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
     * If spaceBefore &gt;= 0, then space is a percentage of normal line height.
     * If spaceBefore &lt; 0, the absolute value of linespacing is the spacing in points
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
     * If spaceBefore &gt;= 0, then space is a percentage of normal line height.
     * If spaceBefore &lt; 0, the absolute value of linespacing is the spacing in points
     * </p>
     *
     * @return the vertical white space before the paragraph
     */
    public double getSpaceBefore(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetSpcBef()){
                    CTTextSpacing spc = props.getSpcBef();

                    if(spc.isSetSpcPct()) setValue( POIXMLUnits.parsePercent(spc.getSpcPct().xgetVal())*0.001 );
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
     * Set the amount of vertical white space that will be present after the paragraph.
     * This space is specified in either percentage or points:
     * <p>
     * If spaceAfter &gt;= 0, then space is a percentage of normal line height.
     * If spaceAfter &lt; 0, the absolute value of linespacing is the spacing in points
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
     * If spaceBefore &gt;= 0, then space is a percentage of normal line height.
     * If spaceBefore &lt; 0, the absolute value of linespacing is the spacing in points
     * </p>
     *
     * @return the vertical white space after the paragraph
     */
    public double getSpaceAfter(){
        ParagraphPropertyFetcher<Double> fetcher = new ParagraphPropertyFetcher<Double>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetSpcAft()){
                    CTTextSpacing spc = props.getSpcAft();

                    if(spc.isSetSpcPct()) setValue( POIXMLUnits.parsePercent(spc.getSpcPct().xgetVal())*0.001 );
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
     * paragraph properties defined in the list of styles associated with the body of text
     * that this paragraph belongs to (therefore in the parent shape).
     * <p>
     * Note that the closest properties object to the text is used, therefore if there is
     * a conflict between the text paragraph properties and the list style properties for
     * this level then the text paragraph properties will take precedence.
     * </p>
     *
     * @param level the level (0 ... 4)
     */
    public void setLevel(int level){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();

        pr.setLvl(level);
    }

    /**
     * Returns the level of text properties that this paragraph will follow.
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
                if (props.isSetBuNone()) {
                    setValue(false);
                    return true;
                }
                if (props.isSetBuFont()) {
                    if (props.isSetBuChar() || props.isSetBuAutoNum()) {
                        setValue(true);
                        return true;
                    } /*else {
                        // Excel treats text with buFont but no char/autonum
                        //  as not bulleted
                        // Possibly the font is just used if bullets turned on again?
                    }*/
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue() == null ? false : fetcher.getValue();
    }

    /**
     * Set or unset this paragraph as a bullet point
     *
     * @param flag whether text in this paragraph has bullets
     */
    public void setBullet(boolean flag) {
        if(isBullet() == flag) return;

        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        if(!flag) {
            pr.addNewBuNone();

            if(pr.isSetBuAutoNum()) pr.unsetBuAutoNum();
            if(pr.isSetBuBlip()) pr.unsetBuBlip();
            if(pr.isSetBuChar()) pr.unsetBuChar();
            if(pr.isSetBuClr()) pr.unsetBuClr();
            if(pr.isSetBuClrTx()) pr.unsetBuClrTx();
            if(pr.isSetBuFont()) pr.unsetBuFont();
            if(pr.isSetBuFontTx()) pr.unsetBuFontTx();
            if(pr.isSetBuSzPct()) pr.unsetBuSzPct();
            if(pr.isSetBuSzPts()) pr.unsetBuSzPts();
            if(pr.isSetBuSzTx()) pr.unsetBuSzTx();
        } else {
            if(pr.isSetBuNone()) pr.unsetBuNone();
            if(!pr.isSetBuFont()) pr.addNewBuFont().setTypeface("Arial");
            if(!pr.isSetBuAutoNum()) pr.addNewBuChar().setChar("\u2022");
        }
    }

    /**
     * Set this paragraph as an automatic numbered bullet point
     *
     * @param scheme type of auto-numbering
     * @param startAt the number that will start number for a given sequence of automatically
     *        numbered bullets (1-based).
     */
    public void setBullet(ListAutoNumber scheme, int startAt) {
        if(startAt < 1) throw new IllegalArgumentException("Start Number must be greater or equal that 1") ;
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextAutonumberBullet lst = pr.isSetBuAutoNum() ? pr.getBuAutoNum() : pr.addNewBuAutoNum();
        lst.setType(STTextAutonumberScheme.Enum.forInt(scheme.ordinal() + 1));
        lst.setStartAt(startAt);

        if(!pr.isSetBuFont()) pr.addNewBuFont().setTypeface("Arial");
        if(pr.isSetBuNone()) pr.unsetBuNone();
        // remove these elements if present as it results in invalid content when opening in Excel.
        if(pr.isSetBuBlip()) pr.unsetBuBlip();
        if(pr.isSetBuChar()) pr.unsetBuChar();
    }

    /**
     * Set this paragraph as an automatic numbered bullet point
     *
     * @param scheme type of auto-numbering
     */
    public void setBullet(ListAutoNumber scheme) {
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        CTTextAutonumberBullet lst = pr.isSetBuAutoNum() ? pr.getBuAutoNum() : pr.addNewBuAutoNum();
        lst.setType(STTextAutonumberScheme.Enum.forInt(scheme.ordinal() + 1));

        if(!pr.isSetBuFont()) pr.addNewBuFont().setTypeface("Arial");
        if(pr.isSetBuNone()) pr.unsetBuNone();
        // remove these elements if present as it results in invalid content when opening in Excel.
        if(pr.isSetBuBlip()) pr.unsetBuBlip();
        if(pr.isSetBuChar()) pr.unsetBuChar();
    }

    /**
     * Returns whether this paragraph has automatic numbered bullets
     */
    public boolean isBulletAutoNumber() {
        ParagraphPropertyFetcher<Boolean> fetcher = new ParagraphPropertyFetcher<Boolean>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetBuAutoNum()) {
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
     * Returns the starting number if this paragraph has automatic numbered bullets, otherwise returns 0
     */
    public int getBulletAutoNumberStart() {
        ParagraphPropertyFetcher<Integer> fetcher = new ParagraphPropertyFetcher<Integer>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetBuAutoNum() && props.getBuAutoNum().isSetStartAt()) {
                    setValue(props.getBuAutoNum().getStartAt());
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);
        return fetcher.getValue() == null ? 0 : fetcher.getValue();
    }

    /**
     * Returns the auto number scheme if this paragraph has automatic numbered bullets, otherwise returns ListAutoNumber.ARABIC_PLAIN
     */
    public ListAutoNumber getBulletAutoNumberScheme() {
        ParagraphPropertyFetcher<ListAutoNumber> fetcher = new ParagraphPropertyFetcher<ListAutoNumber>(getLevel()){
            public boolean fetch(CTTextParagraphProperties props){
                if(props.isSetBuAutoNum()) {
                    setValue(ListAutoNumber.values()[props.getBuAutoNum().getType().intValue() - 1]);
                    return true;
                }
                return false;
            }
        };
        fetchParagraphProperty(fetcher);

        // Note: documentation does not define a default, return ListAutoNumber.ARABIC_PLAIN (1,2,3...)
        return fetcher.getValue() == null ? ListAutoNumber.ARABIC_PLAIN : fetcher.getValue();
    }


    @SuppressWarnings("rawtypes")
    private boolean fetchParagraphProperty(ParagraphPropertyFetcher visitor){
        boolean ok = false;

        if(_p.isSetPPr()) ok = visitor.fetch(_p.getPPr());

        if(!ok) {
            ok = visitor.fetch(_shape);
        }

        return ok;
    }

    @Override
    public String toString(){
        return "[" + getClass() + "]" + getText();
    }
}
