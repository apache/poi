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

package org.apache.poi.hslf.model;

import org.apache.poi.hslf.record.CString;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.HeadersFootersAtom;
import org.apache.poi.hslf.record.HeadersFootersContainer;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.SheetContainer;
import org.apache.poi.hslf.usermodel.HSLFSheet;
import org.apache.poi.hslf.usermodel.HSLFSimpleShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.sl.usermodel.Placeholder;

/**
 * Header / Footer settings.
 *
 * You can get these on slides, or across all notes
 */
public final class HeadersFooters {

    private static final String _ppt2007tag = "___PPT12";

    private final HeadersFootersContainer _container;
    private final HSLFSheet _sheet;
    private final boolean _ppt2007;


    public HeadersFooters(HSLFSlideShow ppt, short headerFooterType) {
        this(ppt.getSlideMasters().get(0), headerFooterType);
    }

    public HeadersFooters(HSLFSheet sheet, short headerFooterType) {
        _sheet = sheet;
        
        @SuppressWarnings("resource")
        HSLFSlideShow ppt = _sheet.getSlideShow();
        Document doc = ppt.getDocumentRecord();
        
        // detect if this ppt was saved in Office2007
        String tag = ppt.getSlideMasters().get(0).getProgrammableTag();
        _ppt2007 = _ppt2007tag.equals(tag);

        SheetContainer sc = _sheet.getSheetContainer();
        HeadersFootersContainer hdd = (HeadersFootersContainer)sc.findFirstOfType(RecordTypes.HeadersFooters.typeID);
        // boolean ppt2007 = sc.findFirstOfType(RecordTypes.RoundTripContentMasterId.typeID) != null;

        if (hdd == null) {
            for (Record ch : doc.getChildRecords()) {
                if (ch instanceof HeadersFootersContainer
                    && ((HeadersFootersContainer) ch).getOptions() == headerFooterType) {
                    hdd = (HeadersFootersContainer) ch;
                    break;
                }
            }
        }
        
        if (hdd == null) {
            hdd = new HeadersFootersContainer(headerFooterType);
            Record lst = doc.findFirstOfType(RecordTypes.List.typeID);
            doc.addChildAfter(hdd, lst);
        }
        _container = hdd;
    }

    /**
     * Headers's text
     *
     * @return Headers's text
     */
    public String getHeaderText(){
        CString cs = _container == null ? null : _container.getHeaderAtom();
        return getPlaceholderText(Placeholder.HEADER, cs);
    }

    /**
     * Sets headers's text
     *
     * @param text headers's text
     */
    public void setHeaderText(String text){
        setHeaderVisible(true);
        CString cs = _container.getHeaderAtom();
        if (cs == null) {
            cs = _container.addHeaderAtom();
        }

        cs.setText(text);
    }

    /**
     * Footer's text
     *
     * @return Footer's text
     */
    public String getFooterText(){
        CString cs = _container == null ? null : _container.getFooterAtom();
        return getPlaceholderText(Placeholder.FOOTER, cs);
    }

    /**
     * Sets footers's text
     *
     * @param text footers's text
     */
    public void setFootersText(String text){
        setFooterVisible(true);
        CString cs = _container.getFooterAtom();
        if (cs == null) {
            cs = _container.addFooterAtom();
        }

        cs.setText(text);
    }

    /**
     * This is the date that the user wants in the footers, instead of today's date.
     *
     * @return custom user date
     */
    public String getDateTimeText(){
        CString cs = _container == null ? null : _container.getUserDateAtom();
        return getPlaceholderText(Placeholder.DATETIME, cs);
    }

    /**
     * Sets custom user date to be displayed instead of today's date.
     *
     * @param text custom user date
     */
    public void setDateTimeText(String text){
        setUserDateVisible(true);
        setDateTimeVisible(true);
        CString cs = _container.getUserDateAtom();
        if (cs == null) {
            cs = _container.addUserDateAtom();
        }

        cs.setText(text);
    }

    /**
     * whether the footer text is displayed.
     */
    public boolean isFooterVisible(){
        return isVisible(HeadersFootersAtom.fHasFooter, Placeholder.FOOTER);
    }

    /**
     * whether the footer text is displayed.
     */
    public void setFooterVisible(boolean flag){
        setFlag(HeadersFootersAtom.fHasFooter, flag);
    }

    /**
     * whether the header text is displayed.
     */
    public boolean isHeaderVisible(){
        return isVisible(HeadersFootersAtom.fHasHeader, Placeholder.HEADER);
    }

    /**
     * whether the header text is displayed.
     */
    public void setHeaderVisible(boolean flag){
        setFlag(HeadersFootersAtom.fHasHeader, flag);
    }

    /**
     * whether the date is displayed in the footer.
     */
    public boolean isDateTimeVisible(){
        return isVisible(HeadersFootersAtom.fHasDate, Placeholder.DATETIME);
    }

    /**
     * whether the date is displayed in the footer.
     */
    public void setDateTimeVisible(boolean flag){
        setFlag(HeadersFootersAtom.fHasDate, flag);
    }

    /**
     * whether the custom user date is used instead of today's date.
     */
    public boolean isUserDateVisible(){
        return isVisible(HeadersFootersAtom.fHasUserDate, Placeholder.DATETIME);
    }

    /**
     * whether the date is displayed in the footer.
     */
    public void setUserDateVisible(boolean flag){
        setFlag(HeadersFootersAtom.fHasUserDate, flag);
    }

    /**
     * whether the slide number is displayed in the footer.
     */
    public boolean isSlideNumberVisible(){
        return isVisible(HeadersFootersAtom.fHasSlideNumber, Placeholder.SLIDE_NUMBER);
    }

    /**
     * whether the slide number is displayed in the footer.
     */
    public void setSlideNumberVisible(boolean flag){
        setFlag(HeadersFootersAtom.fHasSlideNumber, flag);
    }

    /**
     *  An integer that specifies the format ID to be used to style the datetime.
     *
     * @return an integer that specifies the format ID to be used to style the datetime.
     */
    public int getDateTimeFormat(){
        return _container.getHeadersFootersAtom().getFormatId();
    }

    /**
     *  An integer that specifies the format ID to be used to style the datetime.
     *
     * @param formatId an integer that specifies the format ID to be used to style the datetime.
     */
    public void setDateTimeFormat(int formatId){
        _container.getHeadersFootersAtom().setFormatId(formatId);
    }

    private boolean isVisible(int flag, Placeholder placeholderId){
        boolean visible;
        if(_ppt2007){
            HSLFSimpleShape ss = _sheet.getPlaceholder(placeholderId);
            visible = ss instanceof HSLFTextShape && ((HSLFTextShape)ss).getText() != null;
        } else {
            visible = _container.getHeadersFootersAtom().getFlag(flag);
        }
        return visible;
    }

    private String getPlaceholderText(Placeholder ph, CString cs) {
        String text;
        if (_ppt2007) {
            HSLFSimpleShape ss = _sheet.getPlaceholder(ph);
            text = (ss instanceof HSLFTextShape) ? ((HSLFTextShape)ss).getText() : null;

            // default text in master placeholders is not visible
            if("*".equals(text)) {
                text = null;
            }
        } else {
            text = (cs == null) ? null : cs.getText();
        }
        return text;
    }

    private void setFlag(int type, boolean flag) {
        _container.getHeadersFootersAtom().setFlag(type, flag);
    }

    /**
     * @return true, if this is a ppt 2007 document and header/footer are stored as placeholder shapes
     */
    public boolean isPpt2007() {
        return _ppt2007;
    }
}
