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
package org.apache.poi.xwpf.usermodel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.util.Internal;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPBdr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTProofErr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRunTrackChange;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTextAlignment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTextAlignment;


/**
 * Sketch of XWPF paragraph class
 */
public class XWPFParagraph implements IBodyElement{
    private CTP paragraph;
    protected IBody part;
    /** For access to the document's hyperlink, comments, tables etc */
    protected XWPFDocument document;
    protected List<XWPFRun> runs;
    
    private StringBuffer footnoteText = new StringBuffer();

    public XWPFParagraph(CTP prgrph) {
        this(prgrph, null);
    }


    public XWPFParagraph(CTP prgrph, IBody part) {
        this.paragraph = prgrph;
        this.part = part;
        
        // We only care about the document (for comments,
        //  hyperlinks etc) if we're attached to the
        //  core document
        if(part instanceof XWPFDocument) {
           this.document = (XWPFDocument)part;
        }
        
        runs = new ArrayList<XWPFRun>();

       // Get all our child nodes in order, and process them
       //  into XWPFRuns where we can
       XmlCursor c = paragraph.newCursor();
       c.selectPath("child::*");
       while (c.toNextSelection()) {
          XmlObject o = c.getObject();
          if(o instanceof CTR) {
             runs.add(new XWPFRun((CTR)o, this));
          }
          if(o instanceof CTHyperlink) {
             CTHyperlink link = (CTHyperlink)o;
             for(CTR r : link.getRList()) {
                runs.add(new XWPFHyperlinkRun(link, r, this));
             }
          }
          if(o instanceof CTSdtRun) {
             CTSdtContentRun run = ((CTSdtRun)o).getSdtContent();
             for(CTR r : run.getRList()) {
                runs.add(new XWPFRun(r, this));
             }
          }
          if(o instanceof CTRunTrackChange) {
             for(CTR r : ((CTRunTrackChange)o).getRList()) {
                runs.add(new XWPFRun(r, this));
             }
          }
          if(o instanceof CTSimpleField) {
             for(CTR r : ((CTSimpleField)o).getRList()) {
                runs.add(new XWPFRun(r, this));
             }
          }
       }
       
       // Look for bits associated with the runs
       for(XWPFRun run : runs) {
          CTR r = run.getCTR();
          
          // Check for bits that only apply when
          //  attached to a core document
          // TODO Make this nicer by tracking the XWPFFootnotes directly
          if(document != null) {
             c = r.newCursor();
             c.selectPath("child::*");
             while (c.toNextSelection()) {
                XmlObject o = c.getObject();
                if(o instanceof CTFtnEdnRef) {
                   CTFtnEdnRef ftn = (CTFtnEdnRef)o;
                   footnoteText.append("[").append(ftn.getId()).append(": ");
                   XWPFFootnote footnote =
                      ftn.getDomNode().getLocalName().equals("footnoteReference") ?
                            document.getFootnoteByID(ftn.getId().intValue()) :
                            document.getEndnoteByID(ftn.getId().intValue());
   
                   boolean first = true;
                   for (XWPFParagraph p : footnote.getParagraphs()) {
                      if (!first) {
                         footnoteText.append("\n");
                         first = false;
                      }
                      footnoteText.append(p.getText());
                   }
   
                   footnoteText.append("]");
                }
             }
          }
      }
    }

    @Internal
    public CTP getCTP() {
        return paragraph;
    }

    public List<XWPFRun> getRuns(){
    	return Collections.unmodifiableList(runs);
    }

    public boolean isEmpty(){
        return !paragraph.getDomNode().hasChildNodes();
    }

    public XWPFDocument getDocument(){
        return document;
    }

    /**
     * Return the textual content of the paragraph, including text from pictures
     * in it.
     */
    public String getText() {
        StringBuffer out = new StringBuffer();
        for(XWPFRun run : runs) {
           out.append(run.toString());
        }
        out.append(footnoteText);
        return out.toString();
    }
	
	/**
	 * Return styleID of the paragraph if style exist for this paragraph
	 * if not, null will be returned     
	 * @return		styleID as String
	 */
    public String getStyleID(){
   		if (paragraph.getPPr() != null){
   			if(paragraph.getPPr().getPStyle()!= null){
   				if (paragraph.getPPr().getPStyle().getVal()!= null)
   					return paragraph.getPPr().getPStyle().getVal();
   			}
   		}
   		return null;
    }		
    /**
     * If style exist for this paragraph
     * NumId of the paragraph will be returned.
	 * If style not exist null will be returned     
     * @return	NumID as BigInteger
     */
    public BigInteger getNumID(){
    	if(paragraph.getPPr()!=null){
    		if(paragraph.getPPr().getNumPr()!=null){
    			if(paragraph.getPPr().getNumPr().getNumId()!=null)
    				return paragraph.getPPr().getNumPr().getNumId().getVal();
    		}
    	}
    	return null;
    }
    
    /**
     * setNumID of Paragraph
     * @param numPos
     */
    public void setNumID(BigInteger numPos) {
    	if(paragraph.getPPr()==null)
    		paragraph.addNewPPr();
    	if(paragraph.getPPr().getNumPr()==null)
    		paragraph.getPPr().addNewNumPr();
    	if(paragraph.getPPr().getNumPr().getNumId()==null){
    		paragraph.getPPr().getNumPr().addNewNumId();
    	}
    	paragraph.getPPr().getNumPr().getNumId().setVal(numPos);
    }

    /**
     * Returns the text of the paragraph, but not of any objects in the
     * paragraph
     */
    public String getParagraphText() {
       StringBuffer out = new StringBuffer();
       for(XWPFRun run : runs) {
          out.append(run.toString());
       }
       return out.toString();
    }

    /**
     * Returns any text from any suitable pictures in the paragraph
     */
    public String getPictureText() {
       StringBuffer out = new StringBuffer();
       for(XWPFRun run : runs) {
          out.append(run.getPictureText());
       }
       return out.toString();
    }

    /**
     * Returns the footnote text of the paragraph
     *
     * @return  the footnote text or empty string if the paragraph does not have footnotes
     */
    public String getFootnoteText() {
        return footnoteText.toString();
    }

    /**
     * Appends a new run to this paragraph
     *
     * @return a new text run
     */
    public XWPFRun createRun() {
        return new XWPFRun(paragraph.addNewR(), this);
    }

    /**
     * Returns the paragraph alignment which shall be applied to text in this
     * paragraph.
     * <p/>
     * <p/>
     * If this element is not set on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no alignment is applied to the
     * paragraph.
     * </p>
     *
     * @return the paragraph alignment of this paragraph.
     */
    public ParagraphAlignment getAlignment() {
        CTPPr pr = getCTPPr();
        return pr == null || !pr.isSetJc() ? ParagraphAlignment.LEFT
                : ParagraphAlignment.valueOf(pr.getJc().getVal().intValue());
    }

    /**
     * Specifies the paragraph alignment which shall be applied to text in this
     * paragraph.
     * <p/>
     * <p/>
     * If this element is not set on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no alignment is applied to the
     * paragraph.
     * </p>
     *
     * @param align the paragraph alignment to apply to this paragraph.
     */
    public void setAlignment(ParagraphAlignment align) {
        CTPPr pr = getCTPPr();
        CTJc jc = pr.isSetJc() ? pr.getJc() : pr.addNewJc();
        STJc.Enum en = STJc.Enum.forInt(align.getValue());
        jc.setVal(en);
    }

    /**
     * Returns the text vertical alignment which shall be applied to text in
     * this paragraph.
     * <p/>
     * If the line height (before any added spacing) is larger than one or more
     * characters on the line, all characters will be aligned to each other as
     * specified by this element.
     * </p>
     * <p/>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then the vertical alignment of all
     * characters on the line shall be automatically determined by the consumer.
     * </p>
     *
     * @return the vertical alignment of this paragraph.
     */
    public TextAlignment getVerticalAlignment() {
        CTPPr pr = getCTPPr();
        return (pr == null || !pr.isSetTextAlignment()) ? TextAlignment.AUTO
                : TextAlignment.valueOf(pr.getTextAlignment().getVal()
                .intValue());
    }

    /**
     * Specifies the text vertical alignment which shall be applied to text in
     * this paragraph.
     * <p/>
     * If the line height (before any added spacing) is larger than one or more
     * characters on the line, all characters will be aligned to each other as
     * specified by this element.
     * </p>
     * <p/>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then the vertical alignment of all
     * characters on the line shall be automatically determined by the consumer.
     * </p>
     *
     * @param valign the paragraph vertical alignment to apply to this
     *               paragraph.
     */
    public void setVerticalAlignment(TextAlignment valign) {
        CTPPr pr = getCTPPr();
        CTTextAlignment textAlignment = pr.isSetTextAlignment() ? pr
                .getTextAlignment() : pr.addNewTextAlignment();
        STTextAlignment.Enum en = STTextAlignment.Enum
                .forInt(valign.getValue());
        textAlignment.setVal(en);
    }

    /**
     * Specifies the border which shall be displayed above a set of paragraphs
     * which have the same set of paragraph border settings.
     * <p/>
     * <p/>
     * To determine if any two adjoining paragraphs shall have an individual top
     * and bottom border or a between border, the set of borders on the two
     * adjoining paragraphs are compared. If the border information on those two
     * paragraphs is identical for all possible paragraphs borders, then the
     * between border is displayed. Otherwise, the final paragraph shall use its
     * bottom border and the following paragraph shall use its top border,
     * respectively. If this border specifies a space attribute, that value
     * determines the space above the text (ignoring any spacing above) which
     * should be left before this border is drawn, specified in points.
     * </p>
     * <p/>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no between border shall be applied
     * above identical paragraphs.
     * </p>
     * <b>This border can only be a line border.</b>
     *
     * @param border
     * @see Borders for a list of all types of borders
     */
    public void setBorderTop(Borders border) {
        CTPBdr ct = getCTPBrd(true);

        CTBorder pr = (ct != null && ct.isSetTop()) ? ct.getTop() : ct.addNewTop();
        if (border.getValue() == Borders.NONE.getValue())
            ct.unsetTop();
        else
            pr.setVal(STBorder.Enum.forInt(border.getValue()));
    }

    /**
     * Specifies the border which shall be displayed above a set of paragraphs
     * which have the same set of paragraph border settings.
     *
     * @return paragraphBorder - the top border for the paragraph
     * @see #setBorderTop(Borders)
     * @see Borders a list of all types of borders
     */
    public Borders getBorderTop() {
        CTPBdr border = getCTPBrd(false);
        CTBorder ct = null;
        if (border != null) {
            ct = border.getTop();
        }
        STBorder.Enum ptrn = (ct != null) ? ct.getVal() : STBorder.NONE;
        return Borders.valueOf(ptrn.intValue());
    }

    /**
     * Specifies the border which shall be displayed below a set of paragraphs
     * which have the same set of paragraph border settings.
     * <p/>
     * To determine if any two adjoining paragraphs shall have an individual top
     * and bottom border or a between border, the set of borders on the two
     * adjoining paragraphs are compared. If the border information on those two
     * paragraphs is identical for all possible paragraphs borders, then the
     * between border is displayed. Otherwise, the final paragraph shall use its
     * bottom border and the following paragraph shall use its top border,
     * respectively. If this border specifies a space attribute, that value
     * determines the space after the bottom of the text (ignoring any space
     * below) which should be left before this border is drawn, specified in
     * points.
     * </p>
     * <p/>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no between border shall be applied
     * below identical paragraphs.
     * </p>
     * <b>This border can only be a line border.</b>
     *
     * @param border
     * @see Borders a list of all types of borders
     */
    public void setBorderBottom(Borders border) {
        CTPBdr ct = getCTPBrd(true);
        CTBorder pr = ct.isSetBottom() ? ct.getBottom() : ct.addNewBottom();
        if (border.getValue() == Borders.NONE.getValue())
            ct.unsetBottom();
        else
            pr.setVal(STBorder.Enum.forInt(border.getValue()));
    }

    /**
     * Specifies the border which shall be displayed below a set of
     * paragraphs which have the same set of paragraph border settings.
     *
     * @return paragraphBorder - the bottom border for the paragraph
     * @see #setBorderBottom(Borders)
     * @see Borders a list of all types of borders
     */
    public Borders getBorderBottom() {
        CTPBdr border = getCTPBrd(false);
        CTBorder ct = null;
        if (border != null) {
            ct = border.getBottom();
        }
        STBorder.Enum ptrn = ct != null ? ct.getVal() : STBorder.NONE;
        return Borders.valueOf(ptrn.intValue());
    }

    /**
     * Specifies the border which shall be displayed on the left side of the
     * page around the specified paragraph.
     * <p/>
     * To determine if any two adjoining paragraphs should have a left border
     * which spans the full line height or not, the left border shall be drawn
     * between the top border or between border at the top (whichever would be
     * rendered for the current paragraph), and the bottom border or between
     * border at the bottom (whichever would be rendered for the current
     * paragraph).
     * </p>
     * <p/>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no left border shall be applied.
     * </p>
     * <b>This border can only be a line border.</b>
     *
     * @param border
     * @see Borders for a list of all possible borders
     */
    public void setBorderLeft(Borders border) {
        CTPBdr ct = getCTPBrd(true);
        CTBorder pr = ct.isSetLeft() ? ct.getLeft() : ct.addNewLeft();
        if (border.getValue() == Borders.NONE.getValue())
            ct.unsetLeft();
        else
            pr.setVal(STBorder.Enum.forInt(border.getValue()));
    }

    /**
     * Specifies the border which shall be displayed on the left side of the
     * page around the specified paragraph.
     *
     * @return ParagraphBorder - the left border for the paragraph
     * @see #setBorderLeft(Borders)
     * @see Borders for a list of all possible borders
     */
    public Borders getBorderLeft() {
        CTPBdr border = getCTPBrd(false);
        CTBorder ct = null;
        if (border != null) {
            ct = border.getLeft();
        }
        STBorder.Enum ptrn = ct != null ? ct.getVal() : STBorder.NONE;
        return Borders.valueOf(ptrn.intValue());
    }

    /**
     * Specifies the border which shall be displayed on the right side of the
     * page around the specified paragraph.
     * <p/>
     * To determine if any two adjoining paragraphs should have a right border
     * which spans the full line height or not, the right border shall be drawn
     * between the top border or between border at the top (whichever would be
     * rendered for the current paragraph), and the bottom border or between
     * border at the bottom (whichever would be rendered for the current
     * paragraph).
     * </p>
     * <p/>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no right border shall be applied.
     * </p>
     * <b>This border can only be a line border.</b>
     *
     * @param border
     * @see Borders for a list of all possible borders
     */
    public void setBorderRight(Borders border) {
        CTPBdr ct = getCTPBrd(true);
        CTBorder pr = ct.isSetRight() ? ct.getRight() : ct.addNewRight();
        if (border.getValue() == Borders.NONE.getValue())
            ct.unsetRight();
        else
            pr.setVal(STBorder.Enum.forInt(border.getValue()));
    }

    /**
     * Specifies the border which shall be displayed on the right side of the
     * page around the specified paragraph.
     *
     * @return ParagraphBorder - the right border for the paragraph
     * @see #setBorderRight(Borders)
     * @see Borders for a list of all possible borders
     */
    public Borders getBorderRight() {
        CTPBdr border = getCTPBrd(false);
        CTBorder ct = null;
        if (border != null) {
            ct = border.getRight();
        }
        STBorder.Enum ptrn = ct != null ? ct.getVal() : STBorder.NONE;
        return Borders.valueOf(ptrn.intValue());
    }

    /**
     * Specifies the border which shall be displayed between each paragraph in a
     * set of paragraphs which have the same set of paragraph border settings.
     * <p/>
     * To determine if any two adjoining paragraphs should have a between border
     * or an individual top and bottom border, the set of borders on the two
     * adjoining paragraphs are compared. If the border information on those two
     * paragraphs is identical for all possible paragraphs borders, then the
     * between border is displayed. Otherwise, each paragraph shall use its
     * bottom and top border, respectively. If this border specifies a space
     * attribute, that value is ignored - this border is always located at the
     * bottom of each paragraph with an identical following paragraph, taking
     * into account any space after the line pitch.
     * </p>
     * <p/>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no between border shall be applied
     * between identical paragraphs.
     * </p>
     * <b>This border can only be a line border.</b>
     *
     * @param border
     * @see Borders for a list of all possible borders
     */
    public void setBorderBetween(Borders border) {
        CTPBdr ct = getCTPBrd(true);
        CTBorder pr = ct.isSetBetween() ? ct.getBetween() : ct.addNewBetween();
        if (border.getValue() == Borders.NONE.getValue())
            ct.unsetBetween();
        else
            pr.setVal(STBorder.Enum.forInt(border.getValue()));
    }

    /**
     * Specifies the border which shall be displayed between each paragraph in a
     * set of paragraphs which have the same set of paragraph border settings.
     *
     * @return ParagraphBorder - the between border for the paragraph
     * @see #setBorderBetween(Borders)
     * @see Borders for a list of all possible borders
     */
    public Borders getBorderBetween() {
        CTPBdr border = getCTPBrd(false);
        CTBorder ct = null;
        if (border != null) {
            ct = border.getBetween();
        }
        STBorder.Enum ptrn = ct != null ? ct.getVal() : STBorder.NONE;
        return Borders.valueOf(ptrn.intValue());
    }

    /**
     * Specifies that when rendering this document in a paginated
     * view, the contents of this paragraph are rendered on the start of a new
     * page in the document.
     * <p/>
     * If this element is omitted on a given paragraph,
     * its value is determined by the setting previously set at any level of the
     * style hierarchy (i.e. that previous setting remains unchanged). If this
     * setting is never specified in the style hierarchy, then this property
     * shall not be applied. Since the paragraph is specified to start on a new
     * page, it begins page two even though it could have fit on page one.
     * </p>
     *
     * @param pageBreak -
     *                  boolean value
     */
    public void setPageBreak(boolean pageBreak) {
        CTPPr ppr = getCTPPr();
        CTOnOff ct_pageBreak = ppr.isSetPageBreakBefore() ? ppr
                .getPageBreakBefore() : ppr.addNewPageBreakBefore();
        if (pageBreak)
            ct_pageBreak.setVal(STOnOff.TRUE);
        else
            ct_pageBreak.setVal(STOnOff.FALSE);
    }

    /**
     * Specifies that when rendering this document in a paginated
     * view, the contents of this paragraph are rendered on the start of a new
     * page in the document.
     * <p/>
     * If this element is omitted on a given paragraph,
     * its value is determined by the setting previously set at any level of the
     * style hierarchy (i.e. that previous setting remains unchanged). If this
     * setting is never specified in the style hierarchy, then this property
     * shall not be applied. Since the paragraph is specified to start on a new
     * page, it begins page two even though it could have fit on page one.
     * </p>
     *
     * @return boolean - if page break is set
     */
    public boolean isPageBreak() {
        CTPPr ppr = getCTPPr();
        CTOnOff ct_pageBreak = ppr.isSetPageBreakBefore() ? ppr
                .getPageBreakBefore() : null;
        if (ct_pageBreak != null
                && ct_pageBreak.getVal().intValue() == STOnOff.INT_TRUE) {
            return true;
        }
        return false;
    }

    /**
     * Specifies the spacing that should be added after the last line in this
     * paragraph in the document in absolute units.
     * <p/>
     * If the afterLines attribute or the afterAutoSpacing attribute is also
     * specified, then this attribute value is ignored.
     * </p>
     *
     * @param spaces -
     *               a positive whole number, whose contents consist of a
     *               measurement in twentieths of a point.
     */
    public void setSpacingAfter(int spaces) {
        CTSpacing spacing = getCTSpacing(true);
        if (spacing != null) {
            BigInteger bi = new BigInteger("" + spaces);
            spacing.setAfter(bi);
        }

    }

    /**
     * Specifies the spacing that should be added after the last line in this
     * paragraph in the document in absolute units.
     *
     * @return int - value representing the spacing after the paragraph
     */
    public int getSpacingAfter() {
        CTSpacing spacing = getCTSpacing(false);
        return (spacing != null && spacing.isSetAfter()) ? spacing.getAfter().intValue() : -1;
    }

    /**
     * Specifies the spacing that should be added after the last line in this
     * paragraph in the document in line units.
     * <b>The value of this attribute is
     * specified in one hundredths of a line.
     * </b>
     * <p/>
     * If the afterAutoSpacing attribute
     * is also specified, then this attribute value is ignored. If this setting
     * is never specified in the style hierarchy, then its value shall be zero
     * (if needed)
     * </p>
     *
     * @param spaces -
     *               a positive whole number, whose contents consist of a
     *               measurement in twentieths of a
     */
    public void setSpacingAfterLines(int spaces) {
        CTSpacing spacing = getCTSpacing(true);
        BigInteger bi = new BigInteger("" + spaces);
        spacing.setAfterLines(bi);
    }


    /**
     * Specifies the spacing that should be added after the last line in this
     * paragraph in the document in absolute units.
     *
     * @return bigInteger - value representing the spacing after the paragraph
     * @see #setSpacingAfterLines(int)
     */
    public int getSpacingAfterLines() {
        CTSpacing spacing = getCTSpacing(false);
        return (spacing != null && spacing.isSetAfterLines()) ? spacing.getAfterLines().intValue() : -1;
    }


    /**
     * Specifies the spacing that should be added above the first line in this
     * paragraph in the document in absolute units.
     * <p/>
     * If the beforeLines attribute or the beforeAutoSpacing attribute is also
     * specified, then this attribute value is ignored.
     * </p>
     *
     * @param spaces
     */
    public void setSpacingBefore(int spaces) {
        CTSpacing spacing = getCTSpacing(true);
        BigInteger bi = new BigInteger("" + spaces);
        spacing.setBefore(bi);
    }

    /**
     * Specifies the spacing that should be added above the first line in this
     * paragraph in the document in absolute units.
     *
     * @return the spacing that should be added above the first line
     * @see #setSpacingBefore(int)
     */
    public int getSpacingBefore() {
        CTSpacing spacing = getCTSpacing(false);
        return (spacing != null && spacing.isSetBefore()) ? spacing.getBefore().intValue() : -1;
    }

    /**
     * Specifies the spacing that should be added before the first line in this
     * paragraph in the document in line units. <b> The value of this attribute
     * is specified in one hundredths of a line. </b>
     * <p/>
     * If the beforeAutoSpacing attribute is also specified, then this attribute
     * value is ignored. If this setting is never specified in the style
     * hierarchy, then its value shall be zero.
     * </p>
     *
     * @param spaces
     */
    public void setSpacingBeforeLines(int spaces) {
        CTSpacing spacing = getCTSpacing(true);
        BigInteger bi = new BigInteger("" + spaces);
        spacing.setBeforeLines(bi);
    }

    /**
     * Specifies the spacing that should be added before the first line in this paragraph in the
     * document in line units.
     * The value of this attribute is specified in one hundredths of a line.
     *
     * @return the spacing that should be added before the first line in this paragraph
     * @see #setSpacingBeforeLines(int)
     */
    public int getSpacingBeforeLines() {
        CTSpacing spacing = getCTSpacing(false);
        return (spacing != null && spacing.isSetBeforeLines()) ? spacing.getBeforeLines().intValue() : -1;
    }


    /**
     * Specifies how the spacing between lines is calculated as stored in the
     * line attribute. If this attribute is omitted, then it shall be assumed to
     * be of a value auto if a line attribute value is present.
     *
     * @param rule
     * @see LineSpacingRule
     */
    public void setSpacingLineRule(LineSpacingRule rule) {
        CTSpacing spacing = getCTSpacing(true);
        spacing.setLineRule(STLineSpacingRule.Enum.forInt(rule.getValue()));
    }

    /**
     * Specifies how the spacing between lines is calculated as stored in the
     * line attribute. If this attribute is omitted, then it shall be assumed to
     * be of a value auto if a line attribute value is present.
     *
     * @return rule
     * @see LineSpacingRule
     * @see #setSpacingLineRule(LineSpacingRule)
     */
    public LineSpacingRule getSpacingLineRule() {
        CTSpacing spacing = getCTSpacing(false);
        return (spacing != null && spacing.isSetLineRule()) ? LineSpacingRule.valueOf(spacing
                .getLineRule().intValue()) : LineSpacingRule.AUTO;
    }


    /**
     * Specifies the indentation which shall be placed between the left text
     * margin for this paragraph and the left edge of that paragraph's content
     * in a left to right paragraph, and the right text margin and the right
     * edge of that paragraph's text in a right to left paragraph
     * <p/>
     * If this attribute is omitted, its value shall be assumed to be zero.
     * Negative values are defined such that the text is moved past the text margin,
     * positive values move the text inside the text margin.
     * </p>
     *
     * @param indentation
     */
    public void setIndentationLeft(int indentation) {
        CTInd indent = getCTInd(true);
        BigInteger bi = new BigInteger("" + indentation);
        indent.setLeft(bi);
    }

    /**
     * Specifies the indentation which shall be placed between the left text
     * margin for this paragraph and the left edge of that paragraph's content
     * in a left to right paragraph, and the right text margin and the right
     * edge of that paragraph's text in a right to left paragraph
     * <p/>
     * If this attribute is omitted, its value shall be assumed to be zero.
     * Negative values are defined such that the text is moved past the text margin,
     * positive values move the text inside the text margin.
     * </p>
     *
     * @return indentation or null if indentation is not set
     */
    public int getIndentationLeft() {
        CTInd indentation = getCTInd(false);
        return (indentation != null && indentation.isSetLeft()) ? indentation.getLeft().intValue()
                : -1;
    }

    /**
     * Specifies the indentation which shall be placed between the right text
     * margin for this paragraph and the right edge of that paragraph's content
     * in a left to right paragraph, and the right text margin and the right
     * edge of that paragraph's text in a right to left paragraph
     * <p/>
     * If this attribute is omitted, its value shall be assumed to be zero.
     * Negative values are defined such that the text is moved past the text margin,
     * positive values move the text inside the text margin.
     * </p>
     *
     * @param indentation
     */
    public void setIndentationRight(int indentation) {
        CTInd indent = getCTInd(true);
        BigInteger bi = new BigInteger("" + indentation);
        indent.setRight(bi);
    }

    /**
     * Specifies the indentation which shall be placed between the right text
     * margin for this paragraph and the right edge of that paragraph's content
     * in a left to right paragraph, and the right text margin and the right
     * edge of that paragraph's text in a right to left paragraph
     * <p/>
     * If this attribute is omitted, its value shall be assumed to be zero.
     * Negative values are defined such that the text is moved past the text margin,
     * positive values move the text inside the text margin.
     * </p>
     *
     * @return indentation or null if indentation is not set
     */

    public int getIndentationRight() {
        CTInd indentation = getCTInd(false);
        return (indentation != null && indentation.isSetRight()) ? indentation.getRight().intValue()
                : -1;
    }

    /**
     * Specifies the indentation which shall be removed from the first line of
     * the parent paragraph, by moving the indentation on the first line back
     * towards the beginning of the direction of text flow.
     * This indentation is specified relative to the paragraph indentation which is specified for
     * all other lines in the parent paragraph.
     * <p/>
     * The firstLine and hanging attributes are mutually exclusive, if both are specified, then the
     * firstLine value is ignored.
     * </p>
     *
     * @param indentation
     */

    public void setIndentationHanging(int indentation) {
        CTInd indent = getCTInd(true);
        BigInteger bi = new BigInteger("" + indentation);
        indent.setHanging(bi);
    }

    /**
     * Specifies the indentation which shall be removed from the first line of
     * the parent paragraph, by moving the indentation on the first line back
     * towards the beginning of the direction of text flow.
     * This indentation is
     * specified relative to the paragraph indentation which is specified for
     * all other lines in the parent paragraph.
     * The firstLine and hanging
     * attributes are mutually exclusive, if both are specified, then the
     * firstLine value is ignored.
     *
     * @return indentation or null if indentation is not set
     */
    public int getIndentationHanging() {
        CTInd indentation = getCTInd(false);
        return (indentation != null && indentation.isSetHanging()) ? indentation.getHanging().intValue() : -1;
    }

    /**
     * Specifies the additional indentation which shall be applied to the first
     * line of the parent paragraph. This additional indentation is specified
     * relative to the paragraph indentation which is specified for all other
     * lines in the parent paragraph.
     * The firstLine and hanging attributes are
     * mutually exclusive, if both are specified, then the firstLine value is
     * ignored.
     * If the firstLineChars attribute is also specified, then this
     * value is ignored. If this attribute is omitted, then its value shall be
     * assumed to be zero (if needed).
     *
     * @param indentation
     */
    public void setIndentationFirstLine(int indentation) {
        CTInd indent = getCTInd(true);
        BigInteger bi = new BigInteger("" + indentation);
        indent.setFirstLine(bi);
    }

    /**
     * Specifies the additional indentation which shall be applied to the first
     * line of the parent paragraph. This additional indentation is specified
     * relative to the paragraph indentation which is specified for all other
     * lines in the parent paragraph.
     * The firstLine and hanging attributes are
     * mutually exclusive, if both are specified, then the firstLine value is
     * ignored.
     * If the firstLineChars attribute is also specified, then this
     * value is ignored.
     * If this attribute is omitted, then its value shall be
     * assumed to be zero (if needed).
     *
     * @return indentation or null if indentation is not set
     */
    public int getIndentationFirstLine() {
        CTInd indentation = getCTInd(false);
        return (indentation != null && indentation.isSetFirstLine()) ? indentation.getFirstLine().intValue()
                : -1;
    }

    /**
     * This element specifies whether a consumer shall break Latin text which
     * exceeds the text extents of a line by breaking the word across two lines
     * (breaking on the character level) or by moving the word to the following
     * line (breaking on the word level).
     *
     * @param wrap - boolean
     */
    public void setWordWrap(boolean wrap) {
        CTOnOff wordWrap = getCTPPr().isSetWordWrap() ? getCTPPr()
                .getWordWrap() : getCTPPr().addNewWordWrap();
        if (wrap)
            wordWrap.setVal(STOnOff.TRUE);
        else
            wordWrap.unsetVal();
    }

    /**
     * This element specifies whether a consumer shall break Latin text which
     * exceeds the text extents of a line by breaking the word across two lines
     * (breaking on the character level) or by moving the word to the following
     * line (breaking on the word level).
     *
     * @return boolean
     */
    public boolean isWordWrap() {
        CTOnOff wordWrap = getCTPPr().isSetWordWrap() ? getCTPPr()
                .getWordWrap() : null;
        if (wordWrap != null) {
            return (wordWrap.getVal() == STOnOff.ON
                    || wordWrap.getVal() == STOnOff.TRUE || wordWrap.getVal() == STOnOff.X_1) ? true
                    : false;
        }
        return false;
    }

    /**
     * This method provides a style to the paragraph
     * This is useful when, e.g. an Heading style has to be assigned
     * @param newStyle
     */
    public void setStyle(String newStyle) {
    	CTPPr pr = getCTPPr();
    	CTString style = pr.getPStyle() != null ? pr.getPStyle() : pr.addNewPStyle();
    	style.setVal(newStyle);
    }
    
    /**
     * @return  the style of the paragraph
     */
    public String getStyle() {
    	CTPPr pr = getCTPPr();
    	CTString style = pr.isSetPStyle() ? pr.getPStyle() : null;
    	return style != null ? style.getVal() : null;
    }

    /**
     * Get a <b>copy</b> of the currently used CTPBrd, if none is used, return
     * a new instance.
     */
    private CTPBdr getCTPBrd(boolean create) {
        CTPPr pr = getCTPPr();
        CTPBdr ct = pr.isSetPBdr() ? pr.getPBdr() : null;
        if (create && ct == null)
            ct = pr.addNewPBdr();
        return ct;
    }

    /**
     * Get a <b>copy</b> of the currently used CTSpacing, if none is used,
     * return a new instance.
     */
    private CTSpacing getCTSpacing(boolean create) {
        CTPPr pr = getCTPPr();
        CTSpacing ct = pr.getSpacing() == null ? null : pr.getSpacing();
        if (create && ct == null)
            ct = pr.addNewSpacing();
        return ct;
    }

    /**
     * Get a <b>copy</b> of the currently used CTPInd, if none is used, return
     * a new instance.
     */
    private CTInd getCTInd(boolean create) {
        CTPPr pr = getCTPPr();
        CTInd ct = pr.getInd() == null ? null : pr.getInd();
        if (create && ct == null)
            ct = pr.addNewInd();
        return ct;
    }

    /**
     * Get a <b>copy</b> of the currently used CTPPr, if none is used, return
     * a new instance.
     */
    private CTPPr getCTPPr() {
        CTPPr pr = paragraph.getPPr() == null ? paragraph.addNewPPr()
                : paragraph.getPPr();
        return pr;
    }
    
    
    /**
     * add a new run at the end of the position of 
     * the content of parameter run
     * @param run
     */
    protected void addRun(CTR run){
    	int pos;
    	pos = paragraph.getRList().size();
    	paragraph.addNewR();
    	paragraph.setRArray(pos, run);
    }
    
    /**
     * this methods parse the paragraph and search for the string searched.
     * If it finds the string, it will return true and the position of the String
     * will be saved in the parameter startPos.
     * @param searched
     * @param startPos
     */
    public TextSegement searchText(String searched,PositionInParagraph startPos){
    	
    	int startRun = startPos.getRun(), 
    		startText = startPos.getText(),
    		startChar = startPos.getChar();
    	int beginRunPos = 0, candCharPos = 0;
    	boolean newList = false;
    	for (int runPos=startRun; runPos<paragraph.getRList().size(); runPos++) {
    		int beginTextPos = 0,beginCharPos = 0, textPos = 0,  charPos = 0;	
	    	CTR ctRun = paragraph.getRArray(runPos);
    		XmlCursor c = ctRun.newCursor();
	    	c.selectPath("./*");
	    	while(c.toNextSelection()){
	    		XmlObject o = c.getObject();
	    		if(o instanceof CTText){
	    			if(textPos>=startText){
		    			String candidate = ((CTText)o).getStringValue();
		    			if(runPos==startRun)
		    				charPos= startChar;
		    			else
		    				charPos = 0;	
		    			for(; charPos<candidate.length(); charPos++){
		    				if((candidate.charAt(charPos)==searched.charAt(0))&&(candCharPos==0)){
		    					beginTextPos = textPos;
		    					beginCharPos = charPos;
		    					beginRunPos = runPos;
		    					newList = true;
		    				}
		    				if(candidate.charAt(charPos)==searched.charAt(candCharPos)){
		    					if(candCharPos+1<searched.length())
		    						candCharPos++;
		    					else if(newList){
		    						TextSegement segement = new TextSegement();
			    					segement.setBeginRun(beginRunPos);
			    					segement.setBeginText(beginTextPos);
			    					segement.setBeginChar(beginCharPos);
			    					segement.setEndRun(runPos);
			    					segement.setEndText(textPos);
			    					segement.setEndChar(charPos);
			    					return segement;
		    					}
		    				}
		    				else
		    					candCharPos=0;
		    			}
		    		}
	    			textPos++;
	    		}
	    		else if(o instanceof CTProofErr){
	    			c.removeXml();
	    		}
	    		else if(o instanceof CTRPr);
	    			//do nothing
	    		else
	    			candCharPos=0;
	    	}
    	}
    	return null;
    }
    
    /**
     * insert a new Run in RunArray
     * @param pos
     * @return  the inserted run
     */
    public XWPFRun insertNewRun(int pos){
    	 if (pos >= 0 && pos <= paragraph.sizeOfRArray()) {
	    	CTR ctRun = paragraph.insertNewR(pos);
	    	XWPFRun newRun = new XWPFRun(ctRun, this);
	    	runs.add(newRun);
	    	return newRun;
    	 }
    	 return null;
    }
    
    
    
    /**
     * get a Text
     * @param segment
     */
    public String getText(TextSegement segment){
    int runBegin = segment.getBeginRun();
    int textBegin = segment.getBeginText();
    int charBegin = segment.getBeginChar(); 
    int runEnd = segment.getEndRun();
    int textEnd = segment.getEndText();
    int charEnd	= segment.getEndChar();
    StringBuffer out = new StringBuffer();
    	for(int i=runBegin; i<=runEnd;i++){
    		int startText=0, endText = paragraph.getRArray(i).getTList().size()-1;
    		if(i==runBegin)
    			startText=textBegin;
    		if(i==runEnd)
    			endText = textEnd;
    		for(int j=startText;j<=endText;j++){
    			String tmpText = paragraph.getRArray(i).getTArray(j).getStringValue();
    			int startChar=0, endChar = tmpText.length()-1;
    			if((j==textBegin)&&(i==runBegin))
    				startChar=charBegin;
    			if((j==textEnd)&&(i==runEnd)){
    				endChar = charEnd;
    			}
   				out.append(tmpText.substring(startChar, endChar+1));
		
    		}
    	}
    	return out.toString();
    }

    /**
     * removes a Run at the position pos in the paragraph
     * @param pos
     * @return true if the run was removed
     */
    public boolean removeRun(int pos){
    	 if (pos >= 0 && pos < paragraph.sizeOfRArray()){
    		 getCTP().removeR(pos);
    		 runs.remove(pos);
    		 return true;
    	 }
    	 return false;
    }

	/**
	 * returns the type of the BodyElement Paragraph
	 * @see org.apache.poi.xwpf.usermodel.IBodyElement#getElementType()
	 */
	public BodyElementType getElementType() {
		return BodyElementType.PARAGRAPH;
	}

	/**
	 * returns the part of the bodyElement
	 * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
	 */
	public IBody getPart() {
		if(part != null){
			return part.getPart();
		}
		return null;
	}

	/**
	 * returns the partType of the bodyPart which owns the bodyElement
	 * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
	 */
	public BodyType getPartType() {
		return part.getPartType();
	}
	
	/**
	 * adds a new Run to the Paragraph
	 */
	public void addRun(XWPFRun r){
		runs.add(r);
	}
	
	/**
	 * return the XWPFRun-Element which owns the CTR run-Element
	 */
	public XWPFRun getRun(CTR r){
		for(int i=0; i < getRuns().size(); i++){
			if(getRuns().get(i).getCTR() == r) return getRuns().get(i); 
		}
		return null;
	}
	


}//end class



