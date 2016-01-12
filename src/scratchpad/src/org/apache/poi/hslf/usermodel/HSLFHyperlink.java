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

import org.apache.poi.hslf.record.ExHyperlink;
import org.apache.poi.hslf.record.ExObjList;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.apache.poi.hslf.record.InteractiveInfo;
import org.apache.poi.hslf.record.InteractiveInfoAtom;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.TxInteractiveInfoAtom;
import org.apache.poi.sl.usermodel.Hyperlink;

/**
 * Represents a hyperlink in a PowerPoint document
 */
public final class HSLFHyperlink implements Hyperlink {
    public static final byte LINK_NEXTSLIDE = InteractiveInfoAtom.LINK_NextSlide;
    public static final byte LINK_PREVIOUSSLIDE = InteractiveInfoAtom.LINK_PreviousSlide;
    public static final byte LINK_FIRSTSLIDE = InteractiveInfoAtom.LINK_FirstSlide;
    public static final byte LINK_LASTSLIDE = InteractiveInfoAtom.LINK_LastSlide;
    public static final byte LINK_SLIDENUMBER = InteractiveInfoAtom.LINK_SlideNumber;
    public static final byte LINK_URL = InteractiveInfoAtom.LINK_Url;
    public static final byte LINK_NULL = InteractiveInfoAtom.LINK_NULL;

    private int id=-1;
    private int type;
    private String address;
    private String label;
    private int startIndex, endIndex;

    /**
     * Gets the type of the hyperlink action.
     * Must be a <code>LINK_*</code>  constant</code>
     *
     * @return the hyperlink URL
     * @see InteractiveInfoAtom
     */
    @Override
    public int getType() {
        return type;
    }

    public void setType(int val) {
        type = val;
        switch(type){
            case LINK_NEXTSLIDE:
                label = "NEXT";
                address = "1,-1,NEXT";
                break;
            case LINK_PREVIOUSSLIDE:
                label = "PREV";
                address = "1,-1,PREV";
                break;
            case LINK_FIRSTSLIDE:
                label = "FIRST";
                address = "1,-1,FIRST";
                break;
            case LINK_LASTSLIDE:
                label = "LAST";
                address = "1,-1,LAST";
                break;
            case LINK_SLIDENUMBER:
                break;
            default:
                label = "";
                address = "";
                break;
        }
    }

    @Override
    public String getAddress() {
        return address;
    }

    public void setAddress(HSLFSlide slide) {
        String href = slide._getSheetNumber() + ","+slide.getSlideNumber()+",Slide " + slide.getSlideNumber();
        setAddress(href);;
        setLabel("Slide " + slide.getSlideNumber());
        setType(HSLFHyperlink.LINK_SLIDENUMBER);
    }

    @Override
    public void setAddress(String str) {
        address = str;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String str) {
        label = str;
    }

    /**
     * Gets the beginning character position
     *
     * @return the beginning character position
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Sets the beginning character position
     *
     * @param startIndex the beginning character position
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
    
    /**
     * Gets the ending character position
     *
     * @return the ending character position
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Sets the ending character position
     *
     * @param endIndex the ending character position
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
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
    public static List<HSLFHyperlink> find(List<HSLFTextParagraph> paragraphs){
        List<HSLFHyperlink> lst = new ArrayList<HSLFHyperlink>();
        if (paragraphs == null || paragraphs.isEmpty()) return lst;

        HSLFTextParagraph firstPara = paragraphs.get(0);
        
        HSLFSlideShow ppt = firstPara.getSheet().getSlideShow();
        //document-level container which stores info about all links in a presentation
        ExObjList exobj = ppt.getDocumentRecord().getExObjList();
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
    public static HSLFHyperlink find(HSLFShape shape){
        HSLFSlideShow ppt = shape.getSheet().getSlideShow();
        //document-level container which stores info about all links in a presentation
        ExObjList exobj = ppt.getDocumentRecord().getExObjList();
        HSLFEscherClientDataRecord cldata = shape.getClientData(false);

        if (exobj != null && cldata != null) {
            List<HSLFHyperlink> lst = new ArrayList<HSLFHyperlink>();
            find(cldata.getHSLFChildRecords(), exobj, lst);
            return lst.isEmpty() ? null : (HSLFHyperlink)lst.get(0);
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
            int id = info.getHyperlinkID();
            ExHyperlink linkRecord = exobj.get(id);
            if (linkRecord == null) {
                continue;
            }
            
            HSLFHyperlink link = new HSLFHyperlink();
            link.setId(id);
            link.setType(info.getAction());
            link.setLabel(linkRecord.getLinkTitle());
            link.setAddress(linkRecord.getLinkURL());
            out.add(link);

            if (iter.hasNext()) {
                r = iter.next();
                if (!(r instanceof TxInteractiveInfoAtom)) {
                    iter.previous();
                    continue;
                }
                TxInteractiveInfoAtom txinfo = (TxInteractiveInfoAtom)r;
                link.setStartIndex(txinfo.getStartIndex());
                link.setEndIndex(txinfo.getEndIndex());
            }
        }
    }
}
