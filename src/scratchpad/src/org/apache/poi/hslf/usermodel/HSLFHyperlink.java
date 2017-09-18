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

package org.apache.poi.hslf.usermodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hslf.record.ExHyperlink;
import org.apache.poi.hslf.record.ExHyperlinkAtom;
import org.apache.poi.hslf.record.ExObjList;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.apache.poi.hslf.record.InteractiveInfo;
import org.apache.poi.hslf.record.InteractiveInfoAtom;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.TxInteractiveInfoAtom;
import org.apache.poi.sl.usermodel.Hyperlink;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.util.Removal;

/**
 * Represents a hyperlink in a PowerPoint document
 */
public final class HSLFHyperlink implements Hyperlink<HSLFShape,HSLFTextParagraph> {
    private final ExHyperlink exHyper;
    private final InteractiveInfo info;
    private TxInteractiveInfoAtom txinfo;

    protected HSLFHyperlink(ExHyperlink exHyper, InteractiveInfo info) {
        this.info = info;
        this.exHyper = exHyper;
    }

    public ExHyperlink getExHyperlink() {
        return exHyper;
    }
    
    public InteractiveInfo getInfo() {
        return info;
    }
    
    public TxInteractiveInfoAtom getTextRunInfo() {
        return txinfo;
    }
    
    protected void setTextRunInfo(TxInteractiveInfoAtom txinfo) {
        this.txinfo = txinfo;
    }

    /**
     * Creates a new Hyperlink and assign it to a shape
     * This is only a helper method - use {@link HSLFSimpleShape#createHyperlink()} instead!
     *
     * @param shape the shape which receives the hyperlink
     * @return the new hyperlink
     * 
     * @see HSLFSimpleShape#createHyperlink()
     */
    /* package */ static HSLFHyperlink createHyperlink(HSLFSimpleShape shape) {
        // TODO: check if a hyperlink already exists
        ExHyperlink exHyper = new ExHyperlink();
        int linkId = shape.getSheet().getSlideShow().addToObjListAtom(exHyper);
        ExHyperlinkAtom obj = exHyper.getExHyperlinkAtom();
        obj.setNumber(linkId);
        InteractiveInfo info = new InteractiveInfo();
        info.getInteractiveInfoAtom().setHyperlinkID(linkId);
        HSLFEscherClientDataRecord cldata = shape.getClientData(true);
        cldata.addChild(info);
        HSLFHyperlink hyper = new HSLFHyperlink(exHyper, info);
        hyper.linkToNextSlide();
        shape.setHyperlink(hyper);
        return hyper;
    }

    /**
     * Creates a new Hyperlink for a textrun.
     * This is only a helper method - use {@link HSLFTextRun#createHyperlink()} instead!
     *
     * @param run the run which receives the hyperlink
     * @return the new hyperlink
     * 
     * @see HSLFTextRun#createHyperlink()
     */
    /* package */ static HSLFHyperlink createHyperlink(HSLFTextRun run) {
        // TODO: check if a hyperlink already exists
        ExHyperlink exHyper = new ExHyperlink();
        int linkId = run.getTextParagraph().getSheet().getSlideShow().addToObjListAtom(exHyper);
        ExHyperlinkAtom obj = exHyper.getExHyperlinkAtom();
        obj.setNumber(linkId);
        InteractiveInfo info = new InteractiveInfo();
        info.getInteractiveInfoAtom().setHyperlinkID(linkId);
        // don't add the hyperlink now to text paragraph records
        // this will be done, when the paragraph is saved
        HSLFHyperlink hyper = new HSLFHyperlink(exHyper, info);
        hyper.linkToNextSlide();
        
        TxInteractiveInfoAtom txinfo = new TxInteractiveInfoAtom();
        int startIdx = run.getTextParagraph().getStartIdxOfTextRun(run);
        int endIdx = startIdx + run.getLength();
        txinfo.setStartIndex(startIdx);
        txinfo.setEndIndex(endIdx);
        hyper.setTextRunInfo(txinfo);
        
        run.setHyperlink(hyper);
        return hyper;
    }
    

    /**
     * Gets the type of the hyperlink action.
     * Must be a <code>LINK_*</code>  constant</code>
     *
     * @return the hyperlink URL
     * @see InteractiveInfoAtom
     */
    @Override
    public HyperlinkType getType() {
        switch (info.getInteractiveInfoAtom().getHyperlinkType()) {
            case InteractiveInfoAtom.LINK_Url:
                return (exHyper.getLinkURL().startsWith("mailto:")) ? HyperlinkType.EMAIL : HyperlinkType.URL;
            case InteractiveInfoAtom.LINK_NextSlide:
            case InteractiveInfoAtom.LINK_PreviousSlide:
            case InteractiveInfoAtom.LINK_FirstSlide:
            case InteractiveInfoAtom.LINK_LastSlide:
            case InteractiveInfoAtom.LINK_SlideNumber:
                return HyperlinkType.DOCUMENT;
            case InteractiveInfoAtom.LINK_CustomShow:
            case InteractiveInfoAtom.LINK_OtherPresentation:
            case InteractiveInfoAtom.LINK_OtherFile:
                return HyperlinkType.FILE;
            default:
            case InteractiveInfoAtom.LINK_NULL:
                return HyperlinkType.NONE;
        }
    }
    
    /**
     * Gets the type of the hyperlink action.
     * Must be a <code>LINK_*</code>  constant</code>
     *
     * @return the hyperlink URL
     * @see InteractiveInfoAtom
     * @deprecated use <code>getType</code> instead
     */
    @Deprecated
    @Removal(version = "4.2")
    @Override
    public HyperlinkType getTypeEnum() {
        return getType();
    }

    @Override
    public void linkToEmail(String emailAddress) {
        InteractiveInfoAtom iia = info.getInteractiveInfoAtom();
        iia.setAction(InteractiveInfoAtom.ACTION_HYPERLINK);
        iia.setJump(InteractiveInfoAtom.JUMP_NONE);
        iia.setHyperlinkType(InteractiveInfoAtom.LINK_Url);
        exHyper.setLinkURL("mailto:"+emailAddress);
        exHyper.setLinkTitle(emailAddress);
        exHyper.setLinkOptions(0x10);
    }

    @Override
    public void linkToUrl(String url) {
        InteractiveInfoAtom iia = info.getInteractiveInfoAtom();
        iia.setAction(InteractiveInfoAtom.ACTION_HYPERLINK);
        iia.setJump(InteractiveInfoAtom.JUMP_NONE);
        iia.setHyperlinkType(InteractiveInfoAtom.LINK_Url);
        exHyper.setLinkURL(url);
        exHyper.setLinkTitle(url);
        exHyper.setLinkOptions(0x10);
    }

    @Override
    public void linkToSlide(Slide<HSLFShape,HSLFTextParagraph> slide) {
        assert(slide instanceof HSLFSlide);
        HSLFSlide sl = (HSLFSlide)slide;
        int slideNum = slide.getSlideNumber();
        String alias = "Slide "+slideNum;

        InteractiveInfoAtom iia = info.getInteractiveInfoAtom();
        iia.setAction(InteractiveInfoAtom.ACTION_HYPERLINK);
        iia.setJump(InteractiveInfoAtom.JUMP_NONE);
        iia.setHyperlinkType(InteractiveInfoAtom.LINK_SlideNumber);

        linkToDocument(sl._getSheetNumber(),slideNum,alias,0x30);
    }

    @Override
    public void linkToNextSlide() {
        InteractiveInfoAtom iia = info.getInteractiveInfoAtom();
        iia.setAction(InteractiveInfoAtom.ACTION_JUMP);
        iia.setJump(InteractiveInfoAtom.JUMP_NEXTSLIDE);
        iia.setHyperlinkType(InteractiveInfoAtom.LINK_NextSlide);

        linkToDocument(1,-1,"NEXT",0x10);
    }

    @Override
    public void linkToPreviousSlide() {
        InteractiveInfoAtom iia = info.getInteractiveInfoAtom();
        iia.setAction(InteractiveInfoAtom.ACTION_JUMP);
        iia.setJump(InteractiveInfoAtom.JUMP_PREVIOUSSLIDE);
        iia.setHyperlinkType(InteractiveInfoAtom.LINK_PreviousSlide);

        linkToDocument(1,-1,"PREV",0x10);
    }

    @Override
    public void linkToFirstSlide() {
        InteractiveInfoAtom iia = info.getInteractiveInfoAtom();
        iia.setAction(InteractiveInfoAtom.ACTION_JUMP);
        iia.setJump(InteractiveInfoAtom.JUMP_FIRSTSLIDE);
        iia.setHyperlinkType(InteractiveInfoAtom.LINK_FirstSlide);

        linkToDocument(1,-1,"FIRST",0x10);
    }

    @Override
    public void linkToLastSlide() {
        InteractiveInfoAtom iia = info.getInteractiveInfoAtom();
        iia.setAction(InteractiveInfoAtom.ACTION_JUMP);
        iia.setJump(InteractiveInfoAtom.JUMP_LASTSLIDE);
        iia.setHyperlinkType(InteractiveInfoAtom.LINK_LastSlide);

        linkToDocument(1,-1,"LAST",0x10);
    }

    private void linkToDocument(int sheetNumber, int slideNumber, String alias, int options) {
        exHyper.setLinkURL(sheetNumber+","+slideNumber+","+alias);
        exHyper.setLinkTitle(alias);
        exHyper.setLinkOptions(options);
    }

    @Override
    public String getAddress() {
        return exHyper.getLinkURL();
    }

    @Override
    public void setAddress(String str) {
        exHyper.setLinkURL(str);
    }

    public int getId() {
        return exHyper.getExHyperlinkAtom().getNumber();
    }

    @Override
    public String getLabel() {
        return exHyper.getLinkTitle();
    }

    @Override
    public void setLabel(String label) {
        exHyper.setLinkTitle(label);
    }

    /**
     * Gets the beginning character position
     *
     * @return the beginning character position
     */
    public int getStartIndex() {
        return (txinfo == null) ? -1 : txinfo.getStartIndex();
    }

    /**
     * Sets the beginning character position
     *
     * @param startIndex the beginning character position
     */
    public void setStartIndex(int startIndex) {
        if (txinfo != null) {
            txinfo.setStartIndex(startIndex);
        }
    }

    /**
     * Gets the ending character position
     *
     * @return the ending character position
     */
    public int getEndIndex() {
        return (txinfo == null) ? -1 : txinfo.getEndIndex();
    }

    /**
     * Sets the ending character position
     *
     * @param endIndex the ending character position
     */
    public void setEndIndex(int endIndex) {
        if (txinfo != null) {
            txinfo.setEndIndex(endIndex);
        }
    }

    /**
     * Find hyperlinks in a text shape
     *
     * @param shape  <code>TextRun</code> to lookup hyperlinks in
     * @return found hyperlinks or <code>null</code> if not found
     */
    public static List<HSLFHyperlink> find(HSLFTextShape shape){
        return find(shape.getTextParagraphs());
    }

    /**
     * Find hyperlinks in a text paragraph
     *
     * @param paragraphs  List of <code>TextParagraph</code> to lookup hyperlinks
     * @return found hyperlinks
     */
    @SuppressWarnings("resource")
    protected static List<HSLFHyperlink> find(List<HSLFTextParagraph> paragraphs){
        List<HSLFHyperlink> lst = new ArrayList<>();
        if (paragraphs == null || paragraphs.isEmpty()) return lst;

        HSLFTextParagraph firstPara = paragraphs.get(0);

        HSLFSlideShow ppt = firstPara.getSheet().getSlideShow();
        //document-level container which stores info about all links in a presentation
        ExObjList exobj = ppt.getDocumentRecord().getExObjList(false);
        if (exobj != null) {
            Record[] records = firstPara.getRecords();
            find(Arrays.asList(records), exobj, lst);
        }

        return lst;
    }

    /**
     * Find hyperlink assigned to the supplied shape
     *
     * @param shape  <code>Shape</code> to lookup hyperlink in
     * @return found hyperlink or <code>null</code>
     */
    @SuppressWarnings("resource")
    protected static HSLFHyperlink find(HSLFShape shape){
        HSLFSlideShow ppt = shape.getSheet().getSlideShow();
        //document-level container which stores info about all links in a presentation
        ExObjList exobj = ppt.getDocumentRecord().getExObjList(false);
        HSLFEscherClientDataRecord cldata = shape.getClientData(false);

        if (exobj != null && cldata != null) {
            List<HSLFHyperlink> lst = new ArrayList<>();
            find(cldata.getHSLFChildRecords(), exobj, lst);
            return lst.isEmpty() ? null : lst.get(0);
        }

        return null;
    }

    private static void find(List<? extends Record> records, ExObjList exobj, List<HSLFHyperlink> out){
        ListIterator<? extends Record> iter = records.listIterator();
        while (iter.hasNext()) {
            Record r = iter.next();
            // see if we have InteractiveInfo in the textrun's records
            if (!(r instanceof InteractiveInfo)) {
                continue;
            }

            InteractiveInfo hldr = (InteractiveInfo)r;
            InteractiveInfoAtom info = hldr.getInteractiveInfoAtom();
            if (info == null) {
                continue;
            }
            int id = info.getHyperlinkID();
            ExHyperlink exHyper = exobj.get(id);
            if (exHyper == null) {
                continue;
            }

            HSLFHyperlink link = new HSLFHyperlink(exHyper, hldr);
            out.add(link);

            if (iter.hasNext()) {
                r = iter.next();
                if (!(r instanceof TxInteractiveInfoAtom)) {
                    iter.previous();
                    continue;
                }
                link.setTextRunInfo((TxInteractiveInfoAtom)r);
            }
        }
    }
}
