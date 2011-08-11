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
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextSpacing;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAlignType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a paragraph of text within the containing text body.
 * The paragraph is the highest level text separation mechanism.
 *
 * @author Yegor Kozlov
 * @since POI-3.8
 */
@Beta
public class XSLFTextParagraph implements Iterable<XSLFTextRun>{
    private final CTTextParagraph _p;
    private final List<XSLFTextRun> _runs;

    XSLFTextParagraph(CTTextParagraph p){
        _p = p;
        _runs = new ArrayList<XSLFTextRun>();
        for (CTRegularTextRun r : _p.getRList()) {
            _runs.add(new XSLFTextRun(r));
        }
    }

    public String getText(){
        StringBuilder out = new StringBuilder();
        for (CTRegularTextRun r : _p.getRList()) {
            out.append(r.getT());
        }
        return out.toString();
    }

    @Internal
    public CTTextParagraph getXmlObject(){
        return _p;
    }

    public List<XSLFTextRun> getTextRuns(){
        return _runs;
    }

    public Iterator<XSLFTextRun> iterator(){
        return _runs.iterator();
    }

    public XSLFTextRun addNewTextRun(){
        CTRegularTextRun r = _p.addNewR();
        XSLFTextRun run = new XSLFTextRun(r);
        _runs.add(run);
        return run;
    }

    public void addLineBreak(){
        _p.addNewBr();
    }

    /**
     * Returns the alignment that is applied to the paragraph.
     *
     * If this attribute is omitted, then a value of left is implied.
     * @return ??? alignment that is applied to the paragraph
     */
    public TextAlign getTextAlign(){
        CTTextParagraphProperties pr = _p.getPPr();
        if(pr == null || !pr.isSetAlgn()) return TextAlign.LEFT;

        return TextAlign.values()[pr.getAlgn().intValue() - 1];
    }

    /**
     * Specifies the alignment that is to be applied to the paragraph.
     * Possible values for this include left, right, centered, justified and distributed,
     * see {@link org.apache.poi.xslf.usermodel.TextAlign}.
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
     * Specifies the indent size that will be applied to the first line of text in the paragraph.
     *
     * @param value the indent in points. The value of -1 unsets the indent attribute
     * from the underlying xml bean.
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
        CTTextParagraphProperties pr = _p.getPPr();
        if(pr == null || !pr.isSetIndent()) return 0;

        return Units.toPoints(pr.getIndent());
    }

    /**
     * Specifies the left margin of the paragraph. This is specified in addition to the text body
     * inset and applies only to this text paragraph. That is the text body Inset and the LeftMargin
     * attributes are additive with respect to the text position.
     *
     * @param value the left margin of the paragraph
     */
    public void setLeftMargin(double value){
        CTTextParagraphProperties pr = _p.isSetPPr() ? _p.getPPr() : _p.addNewPPr();
        pr.setMarL(Units.toEMU(value));
    }

    /**
     *
     * @return the left margin of the paragraph
     */
    public double getLeftMargin(){
        CTTextParagraphProperties pr = _p.getPPr();
        if(pr == null || !pr.isSetMarL()) return 0;

        return Units.toPoints(pr.getMarL());
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
        CTTextParagraphProperties pr = _p.getPPr();
        if(pr == null || !pr.isSetLnSpc()) return 100; // TODO fetch from master

        CTTextSpacing spc = pr.getLnSpc();
        if(spc.isSetSpcPct()) return spc.getSpcPct().getVal()*0.001;
        else if (spc.isSetSpcPts()) return -spc.getSpcPts().getVal()*0.01;
        else return 100;
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
        CTTextParagraphProperties pr = _p.getPPr();
        if(pr == null || !pr.isSetSpcBef()) return 0;  // TODO fetch from master

        CTTextSpacing spc = pr.getSpcBef();
        if(spc.isSetSpcPct()) return spc.getSpcPct().getVal()*0.001;
        else if (spc.isSetSpcPts()) return -spc.getSpcPts().getVal()*0.01;
        else return 0;
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
        CTTextParagraphProperties pr = _p.getPPr();
        if(pr == null || !pr.isSetSpcAft()) return 0; // TODO fetch from master

        CTTextSpacing spc = pr.getSpcAft();
        if(spc.isSetSpcPct()) return spc.getSpcPct().getVal()*0.001;
        else if (spc.isSetSpcPts()) return -spc.getSpcPts().getVal()*0.01;
        else return 0;
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
     * @return the text level of this paragraph. Default is 0.
     */
    public int getLevel(){
        CTTextParagraphProperties pr = _p.getPPr();
        if(pr == null) return 0;

        return pr.getLvl();

    }

    @Override
    public String toString(){
        return "[" + getClass() + "]" + getText();
    }
}
