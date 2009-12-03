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
import java.util.Arrays;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFtnEdnRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPBdr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPTab;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPicture;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRunTrackChange;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTextAlignment;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STLineSpacingRule;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTextAlignment;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/**
 * Sketch of XWPF paragraph class
 */
public class XWPFParagraph {
    private CTP paragraph;
    protected XWPFDocument document; // XXX: we'd like to have access to
    // document's hyperlink, comments and
    // other tables
    /**
     * TODO - replace with RichText String
     */
    private StringBuffer text = new StringBuffer();
    private StringBuffer pictureText = new StringBuffer();
    private StringBuffer footnoteText = new StringBuffer();


    public XWPFParagraph(CTP prgrph) {
        this(prgrph, null);
    }


    protected XWPFParagraph(CTP prgrph, XWPFDocument docRef) {
        this.paragraph = prgrph;
        this.document = docRef;

        if (!isEmpty()) {
            // All the runs to loop over
            // TODO - replace this with some sort of XPath expression
            // to directly find all the CTRs, in the right order
            ArrayList<CTR> rs = new ArrayList<CTR>();
            rs.addAll(Arrays.asList(paragraph.getRArray()));

            for (CTSdtRun sdt : paragraph.getSdtArray()) {
                CTSdtContentRun run = sdt.getSdtContent();
                rs.addAll(Arrays.asList(run.getRArray()));
            }
            for (CTRunTrackChange c : paragraph.getDelArray()) {
                rs.addAll(Arrays.asList(c.getRArray()));
            }

            for (CTRunTrackChange c : paragraph.getInsArray()) {
                rs.addAll(Arrays.asList(c.getRArray()));
            }

            // Get text of the paragraph
            for (int j = 0; j < rs.size(); j++) {
                // Grab the text and tabs of the paragraph
                // Do so in a way that preserves the ordering
                XmlCursor c = rs.get(j).newCursor();
                c.selectPath("./*");
                while (c.toNextSelection()) {
                    XmlObject o = c.getObject();
                    if (o instanceof CTText) {
                        text.append(((CTText) o).getStringValue());
                    }
                    if (o instanceof CTPTab) {
                        text.append("\t");
                    }
                    //got a reference to a footnote
                    if (o instanceof CTFtnEdnRef) {
                        CTFtnEdnRef ftn = (CTFtnEdnRef) o;
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

                // Loop over pictures inside our
                // paragraph, looking for text in them
                CTPicture[] picts = rs.get(j).getPictArray();
                for (int k = 0; k < picts.length; k++) {
                    XmlObject[] t = picts[k]
                            .selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:t");
                    for (int m = 0; m < t.length; m++) {
                        NodeList kids = t[m].getDomNode().getChildNodes();
                        for (int n = 0; n < kids.getLength(); n++) {
                            if (kids.item(n) instanceof Text) {
                                pictureText.append("\n");
                                pictureText.append(kids.item(n).getNodeValue());
                            }
                        }
                    }
                }
            }
        }
    }

    @Internal
    public CTP getCTP() {
        return paragraph;
    }

    public boolean isEmpty() {
        return !paragraph.getDomNode().hasChildNodes();
    }

    public XWPFDocument getDocument() {
        return document;
    }

    /**
     * Return the textual content of the paragraph, including text from pictures
     * in it.
     */
    public String getText() {
        StringBuffer out = new StringBuffer();
        out.append(text).append(footnoteText).append(pictureText);
        return out.toString();
    }

    /**
     * Returns the text of the paragraph, but not of any objects in the
     * paragraph
     */
    public String getParagraphText() {
        return text.toString();
    }

    /**
     * Returns any text from any suitable pictures in the paragraph
     */
    public String getPictureText() {
        return pictureText.toString();
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
     * Gets the style of the paragraph
     * @return
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

}
