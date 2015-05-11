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

import java.util.*;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.record.*;

/**
 * Represents a hyperlink in a PowerPoint document
 *
 * @author Yegor Kozlov
 */
public final class HSLFHyperlink {
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
    private String title;
    private int startIndex, endIndex;

    /**
     * Gets the type of the hyperlink action.
     * Must be a <code>LINK_*</code>  constant</code>
     *
     * @return the hyperlink URL
     * @see InteractiveInfoAtom
     */
    public int getType() {
        return type;
    }

    public void setType(int val) {
        type = val;
        switch(type){
            case LINK_NEXTSLIDE:
                title = "NEXT";
                address = "1,-1,NEXT";
                break;
            case LINK_PREVIOUSSLIDE:
                title = "PREV";
                address = "1,-1,PREV";
                break;
            case LINK_FIRSTSLIDE:
                title = "FIRST";
                address = "1,-1,FIRST";
                break;
            case LINK_LASTSLIDE:
                title = "LAST";
                address = "1,-1,LAST";
                break;
            case LINK_SLIDENUMBER:
                break;
            default:
                title = "";
                address = "";
                break;
        }
    }

    /**
     * Gets the hyperlink URL
     *
     * @return the hyperlink URL
     */
    public String getAddress() {
        return address;
    }

    public void setAddress(HSLFSlide slide) {
        String href = slide._getSheetNumber() + ","+slide.getSlideNumber()+",Slide " + slide.getSlideNumber();
        setAddress(href);;
        setTitle("Slide " + slide.getSlideNumber());
        setType(HSLFHyperlink.LINK_SLIDENUMBER);
    }

    public void setAddress(String str) {
        address = str;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the hyperlink user-friendly title (if different from URL)
     *
     * @return the  hyperlink user-friendly title
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String str) {
        title = str;
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
     * Gets the ending character position
     *
     * @return the ending character position
     */
    public int getEndIndex() {
        return endIndex;
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
    public static List<HSLFHyperlink> find(List<HSLFTextParagraph> paragraphs){
        List<HSLFHyperlink> lst = new ArrayList<HSLFHyperlink>();
        if (paragraphs == null || paragraphs.isEmpty()) return lst;

        HSLFTextParagraph firstPara = paragraphs.get(0);
        
        HSLFSlideShow ppt = firstPara.getSheet().getSlideShow();
        //document-level container which stores info about all links in a presentation
        ExObjList exobj = ppt.getDocumentRecord().getExObjList();
        if (exobj == null) return lst;
        
        Record[] records = firstPara.getRecords();
        find(records, exobj, lst);

        return lst;
    }

    /**
     * Find hyperlink assigned to the supplied shape
     *
     * @param shape  <code>Shape</code> to lookup hyperlink in
     * @return found hyperlink or <code>null</code>
     */
    public static HSLFHyperlink find(HSLFShape shape){
        List<HSLFHyperlink> lst = new ArrayList<HSLFHyperlink>();
        HSLFSlideShow ppt = shape.getSheet().getSlideShow();
        //document-level container which stores info about all links in a presentation
        ExObjList exobj = ppt.getDocumentRecord().getExObjList();
        if (exobj == null) {
            return null;
        }

        EscherContainerRecord spContainer = shape.getSpContainer();
        for (Iterator<EscherRecord> it = spContainer.getChildIterator(); it.hasNext(); ) {
            EscherRecord obj = it.next();
            if (obj.getRecordId() ==  EscherClientDataRecord.RECORD_ID){
                byte[] data = obj.serialize();
                Record[] records = Record.findChildRecords(data, 8, data.length-8);
                find(records, exobj, lst);
            }
        }

        return lst.size() == 1 ? (HSLFHyperlink)lst.get(0) : null;
    }

    private static void find(Record[] records, ExObjList exobj, List<HSLFHyperlink> out){
        if (records == null) return;
        for (int i = 0; i < records.length; i++) {
            //see if we have InteractiveInfo in the textrun's records
            if(!(records[i] instanceof InteractiveInfo)) continue;
            
            InteractiveInfo hldr = (InteractiveInfo)records[i];
            InteractiveInfoAtom info = hldr.getInteractiveInfoAtom();
            int id = info.getHyperlinkID();
            ExHyperlink linkRecord = exobj.get(id);
            if (linkRecord == null) continue;
            
            HSLFHyperlink link = new HSLFHyperlink();
            link.title = linkRecord.getLinkTitle();
            link.address = linkRecord.getLinkURL();
            link.type = info.getAction();
            out.add(link);

            if (i+1 < records.length && records[i+1] instanceof TxInteractiveInfoAtom){
                TxInteractiveInfoAtom txinfo = (TxInteractiveInfoAtom)records[++i];
                link.startIndex = txinfo.getStartIndex();
                link.endIndex = txinfo.getEndIndex();
            }
        }
    }
}
