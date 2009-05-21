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

import org.apache.poi.hslf.record.*;
import org.apache.poi.hslf.usermodel.SlideShow;

/**
 * Header / Footer settings.
 *
 * You can get these on slides, or across all notes
 *
 * @author Yegor Kozlov
 */
public final class HeadersFooters {

    private HeadersFootersContainer _container;
    private boolean _newRecord;
    private SlideShow _ppt;
    private Sheet _sheet;
    private boolean _ppt2007;


    public HeadersFooters(HeadersFootersContainer rec, SlideShow ppt, boolean newRecord, boolean isPpt2007){
        _container = rec;
        _newRecord = newRecord;
        _ppt = ppt;
        _ppt2007 = isPpt2007;
    }

    public HeadersFooters(HeadersFootersContainer rec, Sheet sheet, boolean newRecord, boolean isPpt2007){
        _container = rec;
        _newRecord = newRecord;
        _sheet = sheet;
        _ppt2007 = isPpt2007;
    }

    /**
     * Headers's text
     *
     * @return Headers's text
     */
    public String getHeaderText(){
        CString cs = _container == null ? null : _container.getHeaderAtom();
        return getPlaceholderText(OEPlaceholderAtom.MasterHeader, cs);
    }

    /**
     * Sets headers's text
     *
     * @param text headers's text
     */
    public void setHeaderText(String text){
        if(_newRecord) attach();

        setHeaderVisible(true);
        CString cs = _container.getHeaderAtom();
        if(cs == null) cs = _container.addHeaderAtom();

        cs.setText(text);
    }

    /**
     * Footer's text
     *
     * @return Footer's text
     */
    public String getFooterText(){
        CString cs = _container == null ? null : _container.getFooterAtom();
        return getPlaceholderText(OEPlaceholderAtom.MasterFooter, cs);
    }

    /**
     * Sets footers's text
     *
     * @param text footers's text
     */
    public void setFootersText(String text){
        if(_newRecord) attach();

        setFooterVisible(true);
        CString cs = _container.getFooterAtom();
        if(cs == null) cs = _container.addFooterAtom();

        cs.setText(text);
    }

    /**
     * This is the date that the user wants in the footers, instead of today's date.
     *
     * @return custom user date
     */
    public String getDateTimeText(){
        CString cs = _container == null ? null : _container.getUserDateAtom();
        return getPlaceholderText(OEPlaceholderAtom.MasterDate, cs);
    }

    /**
     * Sets custom user date to be displayed instead of today's date.
     *
     * @param text custom user date
     */
    public void setDateTimeText(String text){
        if(_newRecord) attach();

        setUserDateVisible(true);
        setDateTimeVisible(true);
        CString cs = _container.getUserDateAtom();
        if(cs == null) cs = _container.addUserDateAtom();

        cs.setText(text);
    }

    /**
     * whether the footer text is displayed.
     */
    public boolean isFooterVisible(){
        return isVisible(HeadersFootersAtom.fHasFooter, OEPlaceholderAtom.MasterFooter);
    }

    /**
     * whether the footer text is displayed.
     */
    public void setFooterVisible(boolean flag){
        if(_newRecord) attach();
        _container.getHeadersFootersAtom().setFlag(HeadersFootersAtom.fHasFooter, flag);
    }

    /**
     * whether the header text is displayed.
     */
    public boolean isHeaderVisible(){
        return isVisible(HeadersFootersAtom.fHasHeader, OEPlaceholderAtom.MasterHeader);
    }

    /**
     * whether the header text is displayed.
     */
    public void setHeaderVisible(boolean flag){
        if(_newRecord) attach();
        _container.getHeadersFootersAtom().setFlag(HeadersFootersAtom.fHasHeader, flag);
    }

    /**
     * whether the date is displayed in the footer.
     */
    public boolean isDateTimeVisible(){
        return isVisible(HeadersFootersAtom.fHasDate, OEPlaceholderAtom.MasterDate);
    }

    /**
     * whether the date is displayed in the footer.
     */
    public void setDateTimeVisible(boolean flag){
        if(_newRecord) attach();
        _container.getHeadersFootersAtom().setFlag(HeadersFootersAtom.fHasDate, flag);
    }

    /**
     * whether the custom user date is used instead of today's date.
     */
    public boolean isUserDateVisible(){
        return isVisible(HeadersFootersAtom.fHasUserDate, OEPlaceholderAtom.MasterDate);
    }

    /**
     * whether the date is displayed in the footer.
     */
    public void setUserDateVisible(boolean flag){
        if(_newRecord) attach();
        _container.getHeadersFootersAtom().setFlag(HeadersFootersAtom.fHasUserDate, flag);
    }

    /**
     * whether the slide number is displayed in the footer.
     */
    public boolean isSlideNumberVisible(){
        return isVisible(HeadersFootersAtom.fHasSlideNumber, OEPlaceholderAtom.MasterSlideNumber);
    }

    /**
     * whether the slide number is displayed in the footer.
     */
    public void setSlideNumberVisible(boolean flag){
        if(_newRecord) attach();
        _container.getHeadersFootersAtom().setFlag(HeadersFootersAtom.fHasSlideNumber, flag);
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
        if(_newRecord) attach();
        _container.getHeadersFootersAtom().setFormatId(formatId);
    }

    /**
     * Attach this HeadersFootersContainer to the parent Document record
     */
    private void attach(){
        Document doc = _ppt.getDocumentRecord();
        Record[] ch = doc.getChildRecords();
        Record lst = null;
        for (int i=0; i < ch.length; i++){
            if(ch[i].getRecordType() == RecordTypes.List.typeID){
                lst = ch[i];
                break;
            }
        }
        doc.addChildAfter(_container, lst);
        _newRecord = false;
    }

    private boolean isVisible(int flag, int placeholderId){
        boolean visible;
        if(_ppt2007){
            Sheet master = _sheet != null ? _sheet : _ppt.getSlidesMasters()[0];
            TextShape placeholder = master.getPlaceholder(placeholderId);
            visible = placeholder != null && placeholder.getText() != null;
        } else {
            visible = _container.getHeadersFootersAtom().getFlag(flag);
        }
        return visible;
    }

    private String getPlaceholderText(int placeholderId, CString cs){
        String text = null;
        if(_ppt2007){
            Sheet master = _sheet != null ? _sheet : _ppt.getSlidesMasters()[0];
            TextShape placeholder = master.getPlaceholder(placeholderId);
            if(placeholder != null) text = placeholder.getText();

            //default text in master placeholders is not visible
            if("*".equals(text)) text = null;
        } else {
            text = cs == null ? null : cs.getText();
        }
        return text;
    }

}
