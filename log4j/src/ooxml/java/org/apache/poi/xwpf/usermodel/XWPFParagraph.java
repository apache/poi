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
import java.util.function.Function;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.Paragraph;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

/**
 * <p>A Paragraph within a Document, Table, Header etc.</p>
 * <p>
 * <p>A paragraph has a lot of styling information, but the
 * actual text (possibly along with more styling) is held on
 * the child {@link XWPFRun}s.</p>
 */
public class XWPFParagraph implements IBodyElement, IRunBody, ISDTContents, Paragraph {
    private final CTP paragraph;
    protected IBody part;
    /**
     * For access to the document's hyperlink, comments, tables etc
     */
    protected XWPFDocument document;
    protected List<XWPFRun> runs;
    protected List<IRunElement> iruns;

    private StringBuilder footnoteText = new StringBuilder(64);

    public XWPFParagraph(CTP prgrph, IBody part) {
        this.paragraph = prgrph;
        this.part = part;

        this.document = part.getXWPFDocument();

        if (document == null) {
            throw new NullPointerException();
        }

        // Build up the character runs
        runs = new ArrayList<>();
        iruns = new ArrayList<>();
        buildRunsInOrderFromXml(paragraph);

        // Look for bits associated with the runs
        for (XWPFRun run : runs) {
            CTR r = run.getCTR();

            // Check for bits that only apply when attached to a core document
            // TODO Make this nicer by tracking the XWPFFootnotes directly
            XmlCursor c = r.newCursor();
            c.selectPath("child::*");
            while (c.toNextSelection()) {
                XmlObject o = c.getObject();
                if (o instanceof CTFtnEdnRef) {
                    CTFtnEdnRef ftn = (CTFtnEdnRef) o;
                    footnoteText.append(" [").append(ftn.getId()).append(": ");
                    XWPFAbstractFootnoteEndnote footnote =
                            ftn.getDomNode().getLocalName().equals("footnoteReference") ?
                                    document.getFootnoteByID(ftn.getId().intValue()) :
                                    document.getEndnoteByID(ftn.getId().intValue());
                    if (null != footnote) {
                        boolean first = true;
                        for (XWPFParagraph p : footnote.getParagraphs()) {
                            if (!first) {
                                footnoteText.append("\n");
                            }
                            first = false;
                            footnoteText.append(p.getText());
                        }
                    } else {
                        footnoteText.append("!!! End note with ID \"").append(ftn.getId()).append("\" not found in document.");
                    }
                    footnoteText.append("] ");

                }
            }
            c.dispose();
        }
    }

    /**
     * Identifies (in order) the parts of the paragraph /
     * sub-paragraph that correspond to character text
     * runs, and builds the appropriate runs for these.
     */
    @SuppressWarnings("deprecation")
    private void buildRunsInOrderFromXml(XmlObject object) {
        XmlCursor c = object.newCursor();
        c.selectPath("child::*");
        while (c.toNextSelection()) {
            XmlObject o = c.getObject();
            if (o instanceof CTR) {
                XWPFRun r = new XWPFRun((CTR) o, this);
                runs.add(r);
                iruns.add(r);
            }
            if (o instanceof CTHyperlink) {
                CTHyperlink link = (CTHyperlink)o;
                for (CTR r : link.getRArray()) {
                    XWPFHyperlinkRun hr = new XWPFHyperlinkRun(link, r, this);
                    runs.add(hr);
                    iruns.add(hr);
                }
            }
            if (o instanceof CTSimpleField) {
                CTSimpleField field = (CTSimpleField)o;
                for (CTR r : field.getRArray()) {
                    XWPFFieldRun fr = new XWPFFieldRun(field, r, this);
                    runs.add(fr);
                    iruns.add(fr);
                }
            }
            if (o instanceof CTSdtBlock) {
                XWPFSDT cc = new XWPFSDT((CTSdtBlock) o, part);
                iruns.add(cc);
            }
            if (o instanceof CTSdtRun) {
                XWPFSDT cc = new XWPFSDT((CTSdtRun) o, part);
                iruns.add(cc);
            }
            if (o instanceof CTRunTrackChange) {
                for (CTR r : ((CTRunTrackChange) o).getRArray()) {
                    XWPFRun cr = new XWPFRun(r, this);
                    runs.add(cr);
                    iruns.add(cr);
                }
            }
            if (o instanceof CTSmartTagRun) {
                // Smart Tags can be nested many times.
                // This implementation does not preserve the tagging information
                buildRunsInOrderFromXml(o);
            }
            if (o instanceof CTRunTrackChange) {
                // add all the insertions as text
                for (CTRunTrackChange change : ((CTRunTrackChange) o).getInsArray()) {
                    buildRunsInOrderFromXml(change);
                }
            }
        }
        c.dispose();
    }

    @Internal
    public CTP getCTP() {
        return paragraph;
    }

    public List<XWPFRun> getRuns() {
        return Collections.unmodifiableList(runs);
    }

    /**
     * Return literal runs and sdt/content control objects.
     *
     * @return List<IRunElement>
     */
    public List<IRunElement> getIRuns() {
        return Collections.unmodifiableList(iruns);
    }

    public boolean isEmpty() {
        return !paragraph.getDomNode().hasChildNodes();
    }

    @Override
    public XWPFDocument getDocument() {
        return document;
    }

    /**
     * Return the textual content of the paragraph, including text from pictures
     * and sdt elements in it.
     */
    public String getText() {
        StringBuilder out = new StringBuilder(64);
        for (IRunElement run : iruns) {
            if (run instanceof XWPFRun) {
                XWPFRun xRun = (XWPFRun) run;
                // don't include the text if reviewing is enabled and this is a deleted run
                if (xRun.getCTR().getDelTextArray().length == 0) {
                    out.append(xRun);
                }
            } else if (run instanceof XWPFSDT) {
                out.append(((XWPFSDT) run).getContent().getText());
            } else {
                out.append(run);
            }
        }
        out.append(footnoteText);
        return out.toString();
    }

    /**
     * Return styleID of the paragraph if style exist for this paragraph
     * if not, null will be returned
     *
     * @return styleID as String
     */
    public String getStyleID() {
        if (paragraph.getPPr() != null) {
            if (paragraph.getPPr().getPStyle() != null) {
                if (paragraph.getPPr().getPStyle().getVal() != null) {
                    return paragraph.getPPr().getPStyle().getVal();
                }
            }
        }
        return null;
    }

    /**
     * If style exist for this paragraph
     * NumId of the paragraph will be returned.
     * If style not exist null will be returned
     *
     * @return NumID as BigInteger
     */
    public BigInteger getNumID() {
        if (paragraph.getPPr() != null) {
            if (paragraph.getPPr().getNumPr() != null) {
                if (paragraph.getPPr().getNumPr().getNumId() != null) {
                    return paragraph.getPPr().getNumPr().getNumId().getVal();
                }
            }
        }
        return null;
    }

    /**
     * setNumID of Paragraph
     *
     * @param numPos
     */
    public void setNumID(BigInteger numPos) {
        if (paragraph.getPPr() == null) {
            paragraph.addNewPPr();
        }
        if (paragraph.getPPr().getNumPr() == null) {
            paragraph.getPPr().addNewNumPr();
        }
        if (paragraph.getPPr().getNumPr().getNumId() == null) {
            paragraph.getPPr().getNumPr().addNewNumId();
        }
        paragraph.getPPr().getNumPr().getNumId().setVal(numPos);
    }

    /**
     * setNumILvl of Paragraph
     *
     * @param iLvl
     * @since 4.1.2
     */
    public void setNumILvl(BigInteger iLvl) {
        if (paragraph.getPPr() == null) {
            paragraph.addNewPPr();
        }
        if (paragraph.getPPr().getNumPr() == null) {
            paragraph.getPPr().addNewNumPr();
        }
        if (paragraph.getPPr().getNumPr().getIlvl() == null) {
            paragraph.getPPr().getNumPr().addNewIlvl();
        }
        paragraph.getPPr().getNumPr().getIlvl().setVal(iLvl);
    }

    /**
     * Returns Ilvl of the numeric style for this paragraph.
     * Returns null if this paragraph does not have numeric style.
     *
     * @return Ilvl as BigInteger
     */
    public BigInteger getNumIlvl() {
        if (paragraph.getPPr() != null) {
            if (paragraph.getPPr().getNumPr() != null) {
                if (paragraph.getPPr().getNumPr().getIlvl() != null) {
                    return paragraph.getPPr().getNumPr().getIlvl().getVal();
                }
            }
        }
        return null;
    }

    /**
     * Returns numbering format for this paragraph, eg bullet or
     * lowerLetter.
     * Returns null if this paragraph does not have numeric style.
     */
    public String getNumFmt() {
        BigInteger numID = getNumID();
        XWPFNumbering numbering = document.getNumbering();
        if (numID != null && numbering != null) {
            XWPFNum num = numbering.getNum(numID);
            if (num != null) {
                BigInteger ilvl = getNumIlvl();
                BigInteger abstractNumId = num.getCTNum().getAbstractNumId().getVal();
                CTAbstractNum anum = numbering.getAbstractNum(abstractNumId).getAbstractNum();
                CTLvl level = null;
                for (int i = 0; i < anum.sizeOfLvlArray(); i++) {
                    CTLvl lvl = anum.getLvlArray(i);
                    if (lvl.getIlvl().equals(ilvl)) {
                        level = lvl;
                        break;
                    }
                }
                if (level != null && level.getNumFmt() != null
                        && level.getNumFmt().getVal() != null) {
                    return level.getNumFmt().getVal().toString();
                }
            }
        }
        return null;
    }

    /**
     * Returns the text that should be used around the paragraph level numbers.
     *
     * @return a string (e.g. "%1.") or null if the value is not found.
     */
    public String getNumLevelText() {
        BigInteger numID = getNumID();
        XWPFNumbering numbering = document.getNumbering();
        if (numID != null && numbering != null) {
            XWPFNum num = numbering.getNum(numID);
            if (num != null) {
                BigInteger ilvl = getNumIlvl();
                CTNum ctNum = num.getCTNum();
                if (ctNum == null) {
                    return null;
                }

                CTDecimalNumber ctDecimalNumber = ctNum.getAbstractNumId();
                if (ctDecimalNumber == null) {
                    return null;
                }

                BigInteger abstractNumId = ctDecimalNumber.getVal();
                if (abstractNumId == null) {
                    return null;
                }

                XWPFAbstractNum xwpfAbstractNum = numbering.getAbstractNum(abstractNumId);

                if (xwpfAbstractNum == null) {
                    return null;
                }

                CTAbstractNum anum = xwpfAbstractNum.getCTAbstractNum();

                if (anum == null) {
                    return null;
                }

                CTLvl level = null;
                for (int i = 0; i < anum.sizeOfLvlArray(); i++) {
                    CTLvl lvl = anum.getLvlArray(i);
                    if (lvl != null && lvl.getIlvl() != null && lvl.getIlvl().equals(ilvl)) {
                        level = lvl;
                        break;
                    }
                }
                if (level != null && level.getLvlText() != null
                        && level.getLvlText().getVal() != null) {
                    return level.getLvlText().getVal();
                }
            }
        }
        return null;
    }

    /**
     * Gets the numstartOverride for the paragraph numbering for this paragraph.
     *
     * @return returns the overridden start number or null if there is no override for this paragraph.
     */
    public BigInteger getNumStartOverride() {
        BigInteger numID = getNumID();
        XWPFNumbering numbering = document.getNumbering();
        if (numID != null && numbering != null) {
            XWPFNum num = numbering.getNum(numID);

            if (num != null) {
                CTNum ctNum = num.getCTNum();
                if (ctNum == null) {
                    return null;
                }
                BigInteger ilvl = getNumIlvl();
                CTNumLvl level = null;
                for (int i = 0; i < ctNum.sizeOfLvlOverrideArray(); i++) {
                    CTNumLvl ctNumLvl = ctNum.getLvlOverrideArray(i);
                    if (ctNumLvl != null && ctNumLvl.getIlvl() != null &&
                            ctNumLvl.getIlvl().equals(ilvl)) {
                        level = ctNumLvl;
                        break;
                    }
                }
                if (level != null && level.getStartOverride() != null) {
                    return level.getStartOverride().getVal();
                }
            }
        }
        return null;
    }

    /**
     * Indicates whether this paragraph should be kept on the same page as the next one.
     *
     * @since POI 4.1.1
     */
    public boolean isKeepNext() {
        if (getCTP() != null && getCTP().getPPr() != null && getCTP().getPPr().isSetKeepNext()) {
            return POIXMLUnits.parseOnOff(getCTP().getPPr().getKeepNext().xgetVal());
        }
        return false;
    }

    /**
     * Sets this paragraph to be kept on the same page as the next one or not.
     *
     * @since POI 4.1.1
     */
    public void setKeepNext(boolean keepNext) {
        CTOnOff state = CTOnOff.Factory.newInstance();
        state.setVal(keepNext ? STOnOff1.ON : STOnOff1.OFF);
        getCTP().getPPr().setKeepNext(state);
    }

    /**
     * Returns the text of the paragraph, but not of any objects in the
     * paragraph
     */
    public String getParagraphText() {
        StringBuilder out = new StringBuilder(64);
        for (XWPFRun run : runs) {
            out.append(run);
        }
        return out.toString();
    }

    /**
     * Returns any text from any suitable pictures in the paragraph
     */
    public String getPictureText() {
        StringBuilder out = new StringBuilder(64);
        for (XWPFRun run : runs) {
            out.append(run.getPictureText());
        }
        return out.toString();
    }

    /**
     * Returns the footnote text of the paragraph
     *
     * @return the footnote text or empty string if the paragraph does not have footnotes
     */
    public String getFootnoteText() {
        return footnoteText.toString();
    }

    /**
     * Returns the paragraph alignment which shall be applied to text in this
     * paragraph.
     * <p>
     * <p>
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
     * <p>
     * <p>
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
     * @return The raw alignment value, {@link #getAlignment()} is suggested
     */
    @Override
    public int getFontAlignment() {
        return getAlignment().getValue();
    }

    @Override
    public void setFontAlignment(int align) {
        ParagraphAlignment pAlign = ParagraphAlignment.valueOf(align);
        setAlignment(pAlign);
    }

    /**
     * Returns the text vertical alignment which shall be applied to text in
     * this paragraph.
     * <p>
     * If the line height (before any added spacing) is larger than one or more
     * characters on the line, all characters will be aligned to each other as
     * specified by this element.
     * </p>
     * <p>
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
     * <p>
     * If the line height (before any added spacing) is larger than one or more
     * characters on the line, all characters will be aligned to each other as
     * specified by this element.
     * </p>
     * <p>
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
     * Specifies the border which shall be displayed above a set of paragraphs
     * which have the same set of paragraph border settings.
     * <p>
     * <p>
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
     * <p>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no between border shall be applied
     * above identical paragraphs.
     * </p>
     * <b>This border can only be a line border.</b>
     *
     * @param border one of the defined Border styles, see {@link Borders}
     * @see Borders for a list of all types of borders
     */
    public void setBorderTop(Borders border) {
        CTPBdr ct = getCTPBrd(true);
        if (ct == null) {
            throw new RuntimeException("invalid paragraph state");
        }

        CTBorder pr = (ct.isSetTop()) ? ct.getTop() : ct.addNewTop();
        if (border.getValue() == Borders.NONE.getValue()) {
            ct.unsetTop();
        } else {
            pr.setVal(STBorder.Enum.forInt(border.getValue()));
        }
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
     * Specifies the border which shall be displayed below a set of paragraphs
     * which have the same set of paragraph border settings.
     * <p>
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
     * <p>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no between border shall be applied
     * below identical paragraphs.
     * </p>
     * <b>This border can only be a line border.</b>
     *
     * @param border one of the defined Border styles, see {@link Borders}
     * @see Borders a list of all types of borders
     */
    public void setBorderBottom(Borders border) {
        CTPBdr ct = getCTPBrd(true);
        CTBorder pr = ct.isSetBottom() ? ct.getBottom() : ct.addNewBottom();
        if (border.getValue() == Borders.NONE.getValue()) {
            ct.unsetBottom();
        } else {
            pr.setVal(STBorder.Enum.forInt(border.getValue()));
        }
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
     * Specifies the border which shall be displayed on the left side of the
     * page around the specified paragraph.
     * <p>
     * To determine if any two adjoining paragraphs should have a left border
     * which spans the full line height or not, the left border shall be drawn
     * between the top border or between border at the top (whichever would be
     * rendered for the current paragraph), and the bottom border or between
     * border at the bottom (whichever would be rendered for the current
     * paragraph).
     * </p>
     * <p>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no left border shall be applied.
     * </p>
     * <b>This border can only be a line border.</b>
     *
     * @param border one of the defined Border styles, see {@link Borders}
     * @see Borders for a list of all possible borders
     */
    public void setBorderLeft(Borders border) {
        CTPBdr ct = getCTPBrd(true);
        CTBorder pr = ct.isSetLeft() ? ct.getLeft() : ct.addNewLeft();
        if (border.getValue() == Borders.NONE.getValue()) {
            ct.unsetLeft();
        } else {
            pr.setVal(STBorder.Enum.forInt(border.getValue()));
        }
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
     * Specifies the border which shall be displayed on the right side of the
     * page around the specified paragraph.
     * <p>
     * To determine if any two adjoining paragraphs should have a right border
     * which spans the full line height or not, the right border shall be drawn
     * between the top border or between border at the top (whichever would be
     * rendered for the current paragraph), and the bottom border or between
     * border at the bottom (whichever would be rendered for the current
     * paragraph).
     * </p>
     * <p>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no right border shall be applied.
     * </p>
     * <b>This border can only be a line border.</b>
     *
     * @param border one of the defined Border styles, see {@link Borders}
     * @see Borders for a list of all possible borders
     */
    public void setBorderRight(Borders border) {
        CTPBdr ct = getCTPBrd(true);
        CTBorder pr = ct.isSetRight() ? ct.getRight() : ct.addNewRight();
        if (border.getValue() == Borders.NONE.getValue()) {
            ct.unsetRight();
        } else {
            pr.setVal(STBorder.Enum.forInt(border.getValue()));
        }
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
     * Specifies the border which shall be displayed between each paragraph in a
     * set of paragraphs which have the same set of paragraph border settings.
     * <p>
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
     * <p>
     * If this element is omitted on a given paragraph, its value is determined
     * by the setting previously set at any level of the style hierarchy (i.e.
     * that previous setting remains unchanged). If this setting is never
     * specified in the style hierarchy, then no between border shall be applied
     * between identical paragraphs.
     * </p>
     * <b>This border can only be a line border.</b>
     *
     * @param border one of the defined Border styles, see {@link Borders}
     * @see Borders for a list of all possible borders
     */
    public void setBorderBetween(Borders border) {
        CTPBdr ct = getCTPBrd(true);
        CTBorder pr = ct.isSetBetween() ? ct.getBetween() : ct.addNewBetween();
        if (border.getValue() == Borders.NONE.getValue()) {
            ct.unsetBetween();
        } else {
            pr.setVal(STBorder.Enum.forInt(border.getValue()));
        }
    }

    /**
     * Specifies that when rendering this document in a paginated
     * view, the contents of this paragraph are rendered on the start of a new
     * page in the document.
     * <p>
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
        final CTPPr ppr = getCTPPr();
        final CTOnOff ctPageBreak = ppr.isSetPageBreakBefore() ? ppr.getPageBreakBefore() : null;
        if (ctPageBreak == null) {
            return false;
        }
        return POIXMLUnits.parseOnOff(ctPageBreak.xgetVal());
    }

    /**
     * Specifies that when rendering this document in a paginated
     * view, the contents of this paragraph are rendered on the start of a new
     * page in the document.
     * <p>
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
        CTOnOff ctPageBreak = ppr.isSetPageBreakBefore() ? ppr
                .getPageBreakBefore() : ppr.addNewPageBreakBefore();
        ctPageBreak.setVal(pageBreak ? STOnOff1.ON : STOnOff1.OFF);
    }

    /**
     * Specifies the spacing that should be added after the last line in this
     * paragraph in the document in absolute units.
     *
     * @return int - value representing the spacing after the paragraph
     */
    public int getSpacingAfter() {
        CTSpacing spacing = getCTSpacing(false);
        return (spacing != null && spacing.isSetAfter()) ? (int)Units.toDXA(POIXMLUnits.parseLength(spacing.xgetAfter())) : -1;
    }

    /**
     * Specifies the spacing that should be added after the last line in this
     * paragraph in the document in absolute units.
     * <p>
     * If the afterLines attribute or the afterAutoSpacing attribute is also
     * specified, then this attribute value is ignored.
     * </p>
     *
     * @param spaces a positive whole number, whose contents consist of a
     *               measurement in twentieths of a point.
     */
    public void setSpacingAfter(int spaces) {
        CTSpacing spacing = getCTSpacing(true);
        if (spacing != null) {
            BigInteger bi = new BigInteger(Integer.toString(spaces));
            spacing.setAfter(bi);
        }

    }

    /**
     * Specifies the spacing that should be added after the last line in this
     * paragraph in the document in absolute units.
     *
     * @return int - value representing the spacing after the paragraph
     * @see #setSpacingAfterLines(int)
     */
    public int getSpacingAfterLines() {
        CTSpacing spacing = getCTSpacing(false);
        return (spacing != null && spacing.isSetAfterLines()) ? spacing.getAfterLines().intValue() : -1;
    }

    /**
     * Specifies the spacing that should be added after the last line in this
     * paragraph in the document in line units.
     * <b>The value of this attribute is
     * specified in one hundredths of a line.
     * </b>
     * <p>
     * If the afterAutoSpacing attribute
     * is also specified, then this attribute value is ignored. If this setting
     * is never specified in the style hierarchy, then its value shall be zero
     * (if needed)
     * </p>
     *
     * @param spaces a positive whole number, whose contents consist of a
     *               measurement in hundredths of a line
     */
    public void setSpacingAfterLines(int spaces) {
        CTSpacing spacing = getCTSpacing(true);
        BigInteger bi = new BigInteger(Integer.toString(spaces));
        spacing.setAfterLines(bi);
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
        return (spacing != null && spacing.isSetBefore()) ? (int)Units.toDXA(POIXMLUnits.parseLength(spacing.xgetBefore())) : -1;
    }

    /**
     * Specifies the spacing that should be added above the first line in this
     * paragraph in the document in absolute units.
     * <p>
     * If the beforeLines attribute or the beforeAutoSpacing attribute is also
     * specified, then this attribute value is ignored.
     * </p>
     *
     * @param spaces a positive whole number, whose contents consist of a
     *               measurement in twentieths of a point.
     */
    public void setSpacingBefore(int spaces) {
        CTSpacing spacing = getCTSpacing(true);
        BigInteger bi = new BigInteger(Integer.toString(spaces));
        spacing.setBefore(bi);
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
     * Specifies the spacing that should be added before the first line in this
     * paragraph in the document in line units. <b> The value of this attribute
     * is specified in one hundredths of a line. </b>
     * <p>
     * If the beforeAutoSpacing attribute is also specified, then this attribute
     * value is ignored. If this setting is never specified in the style
     * hierarchy, then its value shall be zero.
     * </p>
     *
     * @param spaces a positive whole number, whose contents consist of a
     *               measurement in hundredths of a line
     */
    public void setSpacingBeforeLines(int spaces) {
        CTSpacing spacing = getCTSpacing(true);
        BigInteger bi = new BigInteger(Integer.toString(spaces));
        spacing.setBeforeLines(bi);
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
     * Specifies how the spacing between lines is calculated as stored in the
     * line attribute. If this attribute is omitted, then it shall be assumed to
     * be of a value auto if a line attribute value is present.
     *
     * @param rule one of the defined rules, see {@link LineSpacingRule}
     * @see LineSpacingRule
     */
     // TODO Fix this to convert line to equivalent value, or deprecate this in
     //      favor of setSpacingLine(double, LineSpacingRule)
    public void setSpacingLineRule(LineSpacingRule rule) {
        CTSpacing spacing = getCTSpacing(true);
        spacing.setLineRule(STLineSpacingRule.Enum.forInt(rule.getValue()));
    }

    /**
     * Return the spacing between lines of a paragraph. The units of the return value depends on the
     * {@link LineSpacingRule}. If AUTO, the return value is in lines, otherwise the return
     * value is in points
     *
     * @return a double specifying points or lines.
     */
    public double getSpacingBetween() {
        CTSpacing spacing = getCTSpacing(false);
        if (spacing == null || !spacing.isSetLine()) {
            return -1;
        }

        double twips = Units.toDXA(POIXMLUnits.parseLength(spacing.xgetLine()));

        return twips / ((spacing.getLineRule() == null || spacing.getLineRule() == STLineSpacingRule.AUTO) ? 240 : 20);
    }

    /**
     * Sets the spacing between lines in a paragraph
     *
     * @param spacing - A double specifying spacing in inches or lines. If rule is
     *                  AUTO, then spacing is in lines. Otherwise spacing is in points.
     * @param rule - {@link LineSpacingRule} indicating how spacing is interpreted. If
     *               AUTO, then spacing value is in lines, and the height depends on the
     *               font size. If AT_LEAST, then spacing value is in inches, and is the
     *               minimum size of the line. If the line height is taller, then the
     *               line expands to match. If EXACT, then spacing is the exact line
     *               height. If the text is taller than the line height, then it is
     *               clipped at the top.
     */
    public void setSpacingBetween(double spacing, LineSpacingRule rule) {
        CTSpacing ctSp = getCTSpacing(true);
        if (rule == LineSpacingRule.AUTO) {
            ctSp.setLine(new BigInteger(String.valueOf(Math.round(spacing * 240.0))));
        } else {
            ctSp.setLine(new BigInteger(String.valueOf(Math.round(spacing * 20.0))));
        }
        ctSp.setLineRule(STLineSpacingRule.Enum.forInt(rule.getValue()));
    }

    /**
     * Sets the spacing between lines in a paragraph
     *
     * @param spacing - A double specifying spacing in lines.
     */
    public void setSpacingBetween(double spacing) {
        setSpacingBetween(spacing, LineSpacingRule.AUTO);
    }

    /**
     * Specifies the indentation which shall be placed between the left text
     * margin for this paragraph and the left edge of that paragraph's content
     * in a left to right paragraph, and the right text margin and the right
     * edge of that paragraph's text in a right to left paragraph
     * <p>
     * If this attribute is omitted, its value shall be assumed to be zero.
     * Negative values are defined such that the text is moved past the text margin,
     * positive values move the text inside the text margin.
     * </p>
     *
     * @return indentation in twips or null if indentation is not set
     */
    public int getIndentationLeft() {
        CTInd indentation = getCTInd(false);
        return (indentation != null && indentation.isSetLeft())
            ? (int)Units.toDXA(POIXMLUnits.parseLength(indentation.xgetLeft()))
            : -1;
    }

    /**
     * Specifies the indentation which shall be placed between the left text
     * margin for this paragraph and the left edge of that paragraph's content
     * in a left to right paragraph, and the right text margin and the right
     * edge of that paragraph's text in a right to left paragraph
     * <p>
     * If this attribute is omitted, its value shall be assumed to be zero.
     * Negative values are defined such that the text is moved past the text margin,
     * positive values move the text inside the text margin.
     * </p>
     *
     * @param indentation in twips
     */
    public void setIndentationLeft(int indentation) {
        CTInd indent = getCTInd(true);
        BigInteger bi = new BigInteger(Integer.toString(indentation));
        indent.setLeft(bi);
    }

    /**
     * Get the indentation which is placed at the left/start of this paragraph
     *
     * @return indentation in hundredths of a character unit or -1 if indentation is not set
     */
    public int getIndentationLeftChars() {
        CTInd indentation = getCTInd(false);
        return (indentation != null && indentation.isSetLeftChars()) ? indentation.getLeftChars().intValue()
                : -1;
    }

    /**
     * Specifies the indentation which shall be placed at the left/start of this paragraph
     * <p>
     * If this attribute is omitted, its value shall be assumed to be zero.
     * if the left/start attribute is specified, then its value is ignored, and is superseded by this value.
     * </p>
     *
     * @param indentation this value is specified in hundredths of a character unit
     */
    public void setIndentationLeftChars(int indentation) {
        CTInd indent = getCTInd(true);
        BigInteger bi = new BigInteger(Integer.toString(indentation));
        indent.setLeftChars(bi);
    }

    /**
     * Specifies the indentation which shall be placed between the right text
     * margin for this paragraph and the right edge of that paragraph's content
     * in a left to right paragraph, and the right text margin and the right
     * edge of that paragraph's text in a right to left paragraph
     * <p>
     * If this attribute is omitted, its value shall be assumed to be zero.
     * Negative values are defined such that the text is moved past the text margin,
     * positive values move the text inside the text margin.
     * </p>
     *
     * @return indentation in twips or null if indentation is not set
     */

    public int getIndentationRight() {
        CTInd indentation = getCTInd(false);
        return (indentation != null && indentation.isSetRight())
            ? (int)Units.toDXA(POIXMLUnits.parseLength(indentation.xgetRight()))
            : -1;
    }

    /**
     * Specifies the indentation which shall be placed between the right text
     * margin for this paragraph and the right edge of that paragraph's content
     * in a left to right paragraph, and the right text margin and the right
     * edge of that paragraph's text in a right to left paragraph
     * <p>
     * If this attribute is omitted, its value shall be assumed to be zero.
     * Negative values are defined such that the text is moved past the text margin,
     * positive values move the text inside the text margin.
     * </p>
     *
     * @param indentation in twips
     */
    public void setIndentationRight(int indentation) {
        CTInd indent = getCTInd(true);
        BigInteger bi = new BigInteger(Integer.toString(indentation));
        indent.setRight(bi);
    }

    /**
     * Get the indentation which is placed at the right/end of this paragraph
     *
     * @return indentation in hundredths of a character unit or -1 if indentation is not set
     */
    public int getIndentationRightChars() {
        CTInd indentation = getCTInd(false);
        return (indentation != null && indentation.isSetRightChars()) ? indentation.getRightChars().intValue()
                : -1;
    }

    /**
     * Specifies the indentation which shall be placed at the right/end of this paragraph
     * <p>
     * If this attribute is omitted, its value shall be assumed to be zero.
     * if the right/end attribute is specified, then its value is ignored, and is superseded by this value.
     * </p>
     *
     * @param indentation this value is specified in hundredths of a character unit
     */
    public void setIndentationRightChars(int indentation) {
        CTInd indent = getCTInd(true);
        BigInteger bi = new BigInteger(Integer.toString(indentation));
        indent.setRightChars(bi);
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
     * @return indentation in twips or null if indentation is not set
     */
    public int getIndentationHanging() {
        CTInd indentation = getCTInd(false);
        return (indentation != null && indentation.isSetHanging())
            ? (int)Units.toDXA(POIXMLUnits.parseLength(indentation.xgetHanging())) : -1;
    }

    /**
     * Specifies the indentation which shall be removed from the first line of
     * the parent paragraph, by moving the indentation on the first line back
     * towards the beginning of the direction of text flow.
     * This indentation is specified relative to the paragraph indentation which is specified for
     * all other lines in the parent paragraph.
     * <p>
     * The firstLine and hanging attributes are mutually exclusive, if both are specified, then the
     * firstLine value is ignored.
     * </p>
     *
     * @param indentation in twips
     */

    public void setIndentationHanging(int indentation) {
        CTInd indent = getCTInd(true);
        BigInteger bi = new BigInteger(Integer.toString(indentation));
        indent.setHanging(bi);
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
     * @return indentation in twips or null if indentation is not set
     */
    public int getIndentationFirstLine() {
        CTInd indentation = getCTInd(false);
        return (indentation != null && indentation.isSetFirstLine())
            ? (int)Units.toDXA(POIXMLUnits.parseLength(indentation.xgetFirstLine()))
            : -1;
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
     * @param indentation in twips
     */
    public void setIndentationFirstLine(int indentation) {
        CTInd indent = getCTInd(true);
        BigInteger bi = new BigInteger(Integer.toString(indentation));
        indent.setFirstLine(bi);
    }

    @Override
    public int getIndentFromLeft() {
        return getIndentationLeft();
    }

    @Override
    public void setIndentFromLeft(int dxaLeft) {
        setIndentationLeft(dxaLeft);
    }

    @Override
    public int getIndentFromRight() {
        return getIndentationRight();
    }

    @Override
    public void setIndentFromRight(int dxaRight) {
        setIndentationRight(dxaRight);
    }

    @Override
    public int getFirstLineIndent() {
        return getIndentationFirstLine();
    }

    @Override
    public void setFirstLineIndent(int first) {
        setIndentationFirstLine(first);
    }

    /**
     * This element specifies whether a consumer shall break Latin text which
     * exceeds the text extents of a line by breaking the word across two lines
     * (breaking on the character level) or by moving the word to the following
     * line (breaking on the word level).
     *
     * @return boolean
     */
    @Override
    public boolean isWordWrapped() {
        return getCTPPr().isSetWordWrap() && POIXMLUnits.parseOnOff(getCTPPr().getWordWrap());
    }

    /**
     * This element specifies whether a consumer shall break Latin text which
     * exceeds the text extents of a line by breaking the word across two lines
     * (breaking on the character level) or by moving the word to the following
     * line (breaking on the word level).
     *
     * @param wrap - boolean
     */
    @Override
    public void setWordWrapped(boolean wrap) {
        CTPPr ppr = getCTPPr();
        if (wrap) {
            CTOnOff wordWrap = ppr.isSetWordWrap() ? ppr.getWordWrap() : ppr.addNewWordWrap();
            wordWrap.setVal(STOnOff1.ON);
        } else {
            if (ppr.isSetWordWrap()) {
                ppr.unsetWordWrap();
            }
        }
    }

    public boolean isWordWrap() {
        return isWordWrapped();
    }

    @Deprecated
    public void setWordWrap(boolean wrap) {
        setWordWrapped(wrap);
    }

    /**
     * @return the style of the paragraph
     */
    public String getStyle() {
        CTPPr pr = getCTPPr();
        CTString style = pr.isSetPStyle() ? pr.getPStyle() : null;
        return style != null ? style.getVal() : null;
    }

    /**
     * Set the style ID for the paragraph
     *
     * @param styleId ID (not name) of the style to set for the paragraph, e.g. "Heading1" (not "Heading 1").
     */
    public void setStyle(String styleId) {
        CTPPr pr = getCTPPr();
        CTString style = pr.getPStyle() != null ? pr.getPStyle() : pr.addNewPStyle();
        style.setVal(styleId);
    }

    /**
     * Get a <b>copy</b> of the currently used CTPBrd, if none is used, return
     * a new instance.
     */
    private CTPBdr getCTPBrd(boolean create) {
        CTPPr pr = getCTPPr();
        CTPBdr ct = pr.isSetPBdr() ? pr.getPBdr() : null;
        if (create && ct == null) {
            ct = pr.addNewPBdr();
        }
        return ct;
    }

    /**
     * Get a <b>copy</b> of the currently used CTSpacing, if none is used,
     * return a new instance.
     */
    private CTSpacing getCTSpacing(boolean create) {
        CTPPr pr = getCTPPr();
        CTSpacing ct = pr.getSpacing();
        if (create && ct == null) {
            ct = pr.addNewSpacing();
        }
        return ct;
    }

    /**
     * Get a <b>copy</b> of the currently used CTPInd, if none is used, return
     * a new instance.
     */
    private CTInd getCTInd(boolean create) {
        CTPPr pr = getCTPPr();
        CTInd ct = pr.getInd();
        if (create && ct == null) {
            ct = pr.addNewInd();
        }
        return ct;
    }

    /**
     * Get a <b>copy</b> of the currently used CTPPr, if none is used, return
     * a new instance.
     */
    private CTPPr getCTPPr() {
        return paragraph.getPPr() == null ? paragraph.addNewPPr()
                : paragraph.getPPr();
    }


    /**
     * add a new run at the end of the position of
     * the content of parameter run
     *
     * @param run
     */
    protected void addRun(CTR run) {
        int pos;
        pos = paragraph.sizeOfRArray();
        paragraph.addNewR();
        paragraph.setRArray(pos, run);
    }

    /**
     * Appends a new run to this paragraph
     *
     * @return a new text run
     */
    public XWPFRun createRun() {
        XWPFRun xwpfRun = new XWPFRun(paragraph.addNewR(), (IRunBody)this);
        runs.add(xwpfRun);
        iruns.add(xwpfRun);
        return xwpfRun;
    }

    /**
     * Appends a new hyperlink run to this paragraph
     *
     * @return a new hyperlink run
     * @since POI 4.1.1
     */
    public XWPFHyperlinkRun createHyperlinkRun(String uri) {
        // Create a relationship ID for this link.
        String rId = getPart().getPackagePart().addExternalRelationship(
                uri, XWPFRelation.HYPERLINK.getRelation()
        ).getId();

        // Create the run.
        CTHyperlink ctHyperLink = getCTP().addNewHyperlink();
        ctHyperLink.setId(rId);
        ctHyperLink.addNewR();

        // Append this run to the paragraph.
        XWPFHyperlinkRun link = new XWPFHyperlinkRun(ctHyperLink, ctHyperLink.getRArray(0), this);
        runs.add(link);
        iruns.add(link);
        return link;
    }

    /**
     * Appends a new field run to this paragraph
     *
     * @return a new field run
     */
    public XWPFFieldRun createFieldRun() {
        CTSimpleField ctSimpleField = paragraph.addNewFldSimple();
        XWPFFieldRun newRun = new XWPFFieldRun(ctSimpleField, ctSimpleField.addNewR(), this);
        runs.add(newRun);
        iruns.add(newRun);
        return newRun;
    }

    /**
     * insert a new Run in all runs
     *
     * @param pos The position at which the new run should be added.
     *
     * @return the inserted run or null if the given pos is out of bounds.
     */
    public XWPFRun insertNewRun(int pos) {
        if (pos == runs.size()) {
            return createRun();
        }
        return insertNewProvidedRun(pos, newCursor -> {
            String uri = CTR.type.getName().getNamespaceURI();
            String localPart = "r";
            // creates a new run, cursor is positioned inside the new
            // element
            newCursor.beginElement(localPart, uri);
            // move the cursor to the START token to the run just created
            newCursor.toParent();
            CTR r = (CTR) newCursor.getObject();
            return new XWPFRun(r, (IRunBody)this);
        });
    }

    /**
     * insert a new hyperlink Run in all runs
     *
     * @param pos The position at which the new run should be added.
     * @param uri hyperlink uri
     *
     * @return the inserted run or null if the given pos is out of bounds.
     */
    public XWPFHyperlinkRun insertNewHyperlinkRun(int pos, String uri) {
        if (pos == runs.size()) {
            return createHyperlinkRun(uri);
        }
        XWPFHyperlinkRun newRun = insertNewProvidedRun(pos, newCursor -> {
            String namespaceURI = CTHyperlink.type.getName().getNamespaceURI();
            String localPart = "hyperlink";
            newCursor.beginElement(localPart, namespaceURI);
            // move the cursor to the START token to the hyperlink just created
            newCursor.toParent();
            CTHyperlink ctHyperLink = (CTHyperlink) newCursor.getObject();
            return new XWPFHyperlinkRun(ctHyperLink, ctHyperLink.addNewR(), this);
        });

        if (newRun != null) {
            String rId = getPart().getPackagePart().addExternalRelationship(
                    uri, XWPFRelation.HYPERLINK.getRelation()
            ).getId();
            newRun.getCTHyperlink().setId(rId);
        }

        return newRun;
    }

    /**
     * insert a new field Run in all runs
     *
     * @param pos The position at which the new run should be added.
     *
     * @return the inserted run or null if the given pos is out of bounds.
     */
    public XWPFFieldRun insertNewFieldRun(int pos) {
        if (pos == runs.size()) {
            return createFieldRun();
        }
        return insertNewProvidedRun(pos, newCursor -> {
            String uri = CTSimpleField.type.getName().getNamespaceURI();
            String localPart = "fldSimple";
            newCursor.beginElement(localPart, uri);
            // move the cursor to the START token to the field just created
            newCursor.toParent();
            CTSimpleField ctSimpleField = (CTSimpleField) newCursor.getObject();
            return new XWPFFieldRun(ctSimpleField, ctSimpleField.addNewR(), this);
        });
    }

    /**
     * insert a new run provided by  in all runs
     *
     * @param <T> XWPFRun or XWPFHyperlinkRun or XWPFFieldRun
     * @param pos The position at which the new run should be added.
     * @param provider provide a new run at position of the given cursor.
     * @return the inserted run or null if the given pos is out of bounds.
     */
    private <T extends XWPFRun> T insertNewProvidedRun(int pos, Function<XmlCursor, T> provider) {
        if (pos >= 0 && pos < runs.size()) {
            XWPFRun run = runs.get(pos);
            CTR ctr = run.getCTR();
            XmlCursor newCursor = ctr.newCursor();
            if (!isCursorInParagraph(newCursor)) {
                // look up correct position for CTP -> XXX -> R array
                newCursor.toParent();
            }
            if (isCursorInParagraph(newCursor)) {
                // provide a new run
                T newRun = provider.apply(newCursor);

                // To update the iruns, find where we're going
                // in the normal runs, and go in there
                int iPos = iruns.size();
                int oldAt = iruns.indexOf(run);
                if (oldAt != -1) {
                    iPos = oldAt;
                }
                iruns.add(iPos, newRun);
                // Runs itself is easy to update
                runs.add(pos, newRun);
                return newRun;
            }
            newCursor.dispose();
        }
        return null;
    }

    /**
     * verifies that cursor is on the right position
     *
     * @param cursor
     * @return
     */
    private boolean isCursorInParagraph(XmlCursor cursor) {
        XmlCursor verify = cursor.newCursor();
        verify.toParent();
        boolean result = (verify.getObject() == this.paragraph);
        verify.dispose();
        return result;
    }

    /**
     * this methods parse the paragraph and search for the string searched.
     * If it finds the string, it will return true and the position of the String
     * will be saved in the parameter startPos.
     *
     * @param searched
     * @param startPos
     */
    public TextSegment searchText(String searched, PositionInParagraph startPos) {
        int startRun = startPos.getRun(),
            startText = startPos.getText(),
            startChar = startPos.getChar();
        int beginRunPos = 0, candCharPos = 0;
        boolean newList = false;

        CTR[] rArray = paragraph.getRArray();
        for (int runPos = startRun; runPos < rArray.length; runPos++) {
            int beginTextPos = 0, beginCharPos = 0, textPos = 0, charPos;
            CTR ctRun = rArray[runPos];
            XmlCursor c = ctRun.newCursor();
            c.selectPath("./*");
            try {
                while (c.toNextSelection()) {
                    XmlObject o = c.getObject();
                    if (o instanceof CTText) {
                        if (textPos >= startText) {
                            String candidate = ((CTText) o).getStringValue();
                            if (runPos == startRun) {
                                charPos = startChar;
                            } else {
                                charPos = 0;
                            }

                            for (; charPos < candidate.length(); charPos++) {
                                if ((candidate.charAt(charPos) == searched.charAt(0)) && (candCharPos == 0)) {
                                    beginTextPos = textPos;
                                    beginCharPos = charPos;
                                    beginRunPos = runPos;
                                    newList = true;
                                }
                                if (candidate.charAt(charPos) == searched.charAt(candCharPos)) {
                                    if (candCharPos + 1 < searched.length()) {
                                        candCharPos++;
                                    } else if (newList) {
                                        TextSegment segment = new TextSegment();
                                        segment.setBeginRun(beginRunPos);
                                        segment.setBeginText(beginTextPos);
                                        segment.setBeginChar(beginCharPos);
                                        segment.setEndRun(runPos);
                                        segment.setEndText(textPos);
                                        segment.setEndChar(charPos);
                                        return segment;
                                    }
                                } else {
                                    candCharPos = 0;
                                }
                            }
                        }
                        textPos++;
                    } else if (o instanceof CTProofErr) {
                        c.removeXml();
                    } else if (o instanceof CTRPr) {
                        //do nothing
                    } else {
                        candCharPos = 0;
                    }
                }
            } finally {
                c.dispose();
            }
        }
        return null;
    }

    /**
     * get a Text
     *
     * @param segment
     */
    public String getText(TextSegment segment) {
        int runBegin = segment.getBeginRun();
        int textBegin = segment.getBeginText();
        int charBegin = segment.getBeginChar();
        int runEnd = segment.getEndRun();
        int textEnd = segment.getEndText();
        int charEnd = segment.getEndChar();
        StringBuilder out = new StringBuilder();
        CTR[] rArray = paragraph.getRArray();
        for (int i = runBegin; i <= runEnd; i++) {
            CTText[] tArray = rArray[i].getTArray();
            int startText = 0, endText = tArray.length - 1;
            if (i == runBegin) {
                startText = textBegin;
            }
            if (i == runEnd) {
                endText = textEnd;
            }
            for (int j = startText; j <= endText; j++) {
                String tmpText = tArray[j].getStringValue();
                int startChar = 0, endChar = tmpText.length() - 1;
                if ((j == textBegin) && (i == runBegin)) {
                    startChar = charBegin;
                }
                if ((j == textEnd) && (i == runEnd)) {
                    endChar = charEnd;
                }
                out.append(tmpText, startChar, endChar + 1);
            }
        }
        return out.toString();
    }

    /**
     * removes a Run at the position pos in the paragraph
     *
     * @param pos
     * @return true if the run was removed
     */
    public boolean removeRun(int pos) {
        if (pos >= 0 && pos < runs.size()) {
            XWPFRun run = runs.get(pos);
            // CTP -> CTHyperlink -> R array
            if (run instanceof XWPFHyperlinkRun
                    && isTheOnlyCTHyperlinkInRuns((XWPFHyperlinkRun) run)) {
                XmlCursor c = ((XWPFHyperlinkRun) run).getCTHyperlink()
                        .newCursor();
                c.removeXml();
                c.dispose();
                runs.remove(pos);
                iruns.remove(run);
                return true;
            }
            // CTP -> CTField -> R array
            if (run instanceof XWPFFieldRun
                    && isTheOnlyCTFieldInRuns((XWPFFieldRun) run)) {
                XmlCursor c = ((XWPFFieldRun) run).getCTField().newCursor();
                c.removeXml();
                c.dispose();
                runs.remove(pos);
                iruns.remove(run);
                return true;
            }
            XmlCursor c = run.getCTR().newCursor();
            c.removeXml();
            c.dispose();
            runs.remove(pos);
            iruns.remove(run);
            return true;
        }
        return false;
    }

    /**
     * Is there only one ctHyperlink in all runs
     *
     * @param run
     *            hyperlink run
     * @return
     */
    private boolean isTheOnlyCTHyperlinkInRuns(XWPFHyperlinkRun run) {
        CTHyperlink ctHyperlink = run.getCTHyperlink();
        long count = runs.stream().filter(r -> (r instanceof XWPFHyperlinkRun
                && ctHyperlink == ((XWPFHyperlinkRun) r).getCTHyperlink()))
                .count();
        return count <= 1;
    }

    /**
     * Is there only one ctField in all runs
     *
     * @param run
     *            field run
     * @return
     */
    private boolean isTheOnlyCTFieldInRuns(XWPFFieldRun run) {
        CTSimpleField ctField = run.getCTField();
        long count = runs.stream().filter(r -> (r instanceof XWPFFieldRun
                && ctField == ((XWPFFieldRun) r).getCTField())).count();
        return count <= 1;
    }

    /**
     * returns the type of the BodyElement Paragraph
     *
     * @see org.apache.poi.xwpf.usermodel.IBodyElement#getElementType()
     */
    @Override
    public BodyElementType getElementType() {
        return BodyElementType.PARAGRAPH;
    }

    @Override
    public IBody getBody() {
        return part;
    }

    /**
     * returns the part of the bodyElement
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    @Override
    public POIXMLDocumentPart getPart() {
        if (part != null) {
            return part.getPart();
        }
        return null;
    }

    /**
     * returns the partType of the bodyPart which owns the bodyElement
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
     */
    @Override
    public BodyType getPartType() {
        return part.getPartType();
    }

    /**
     * adds a new Run to the Paragraph
     *
     * @param r
     */
    public void addRun(XWPFRun r) {
        if (!runs.contains(r)) {
            runs.add(r);
        }
    }

    /**
     * return the XWPFRun-Element which owns the CTR run-Element
     *
     * @param r
     */
    public XWPFRun getRun(CTR r) {
        for (int i = 0; i < getRuns().size(); i++) {
            if (getRuns().get(i).getCTR() == r) {
                return getRuns().get(i);
            }
        }
        return null;
    }

    /**
     * Add a new run with a reference to the specified footnote.
     * The footnote reference run will have the style name "FootnoteReference".
     *
     * @param footnote Footnote to which to add a reference.
     * @since 4.0.0
     */
    public void addFootnoteReference(XWPFAbstractFootnoteEndnote footnote) {
        XWPFRun run = createRun();
        CTR ctRun = run.getCTR();
        ctRun.addNewRPr().addNewRStyle().setVal("FootnoteReference");
        if (footnote instanceof XWPFEndnote) {
            ctRun.addNewEndnoteReference().setId(footnote.getId());
        } else {
            ctRun.addNewFootnoteReference().setId(footnote.getId());
        }
    }
}
