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
package org.apache.poi.hssf.usermodel;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.record.HyperlinkRecord;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;

/**
 * Represents an Excel hyperlink.
 */
public class HSSFHyperlink implements Hyperlink {

    /**
     * Low-level record object that stores the actual hyperlink data
     */
    final protected HyperlinkRecord record;

    /**
     * If we create a new hyperlink remember its type
     */
    final protected HyperlinkType link_type;

    /**
     * Construct a new hyperlink
     * 
     * This method is internal to be used only by
     * {@link HSSFCreationHelper#createHyperlink(HyperlinkType)}.
     *
     * @param type the type of hyperlink to create
     */
    @Internal(since="3.15 beta 3")
    protected HSSFHyperlink( HyperlinkType type )
    {
        this.link_type = type;
        record = new HyperlinkRecord();
        switch(type){
            case URL:
            case EMAIL:
                record.newUrlLink();
                break;
            case FILE:
                record.newFileLink();
                break;
            case DOCUMENT:
                record.newDocumentLink();
                break;
            default:
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }

    /**
     * Initialize the hyperlink by a <code>HyperlinkRecord</code> record
     *
     * @param record
     */
    protected HSSFHyperlink( HyperlinkRecord record )
    {
        this.record = record;
        link_type = getType(record);
    }
    
    private static HyperlinkType getType(HyperlinkRecord record) {
        HyperlinkType link_type;
        // Figure out the type
        if (record.isFileLink()) {
            link_type = HyperlinkType.FILE;
        } else if(record.isDocumentLink()) {
            link_type = HyperlinkType.DOCUMENT;
        } else {
            if(record.getAddress() != null &&
                    record.getAddress().startsWith("mailto:")) {
                link_type = HyperlinkType.EMAIL;
            } else {
                link_type = HyperlinkType.URL;
            }
        }
        return link_type;
    }
    
    protected HSSFHyperlink(Hyperlink other) {
        if (other instanceof HSSFHyperlink) {
            HSSFHyperlink hlink = (HSSFHyperlink) other;
            record = hlink.record.clone();
            link_type = getType(record);
        }
        else {
            link_type = other.getType();
            record = new HyperlinkRecord();
            setFirstRow(other.getFirstRow());
            setFirstColumn(other.getFirstColumn());
            setLastRow(other.getLastRow());
            setLastColumn(other.getLastColumn());
        }
    }

    /**
     * Return the row of the first cell that contains the hyperlink
     *
     * @return the 0-based row of the cell that contains the hyperlink
     */
    @Override
    public int getFirstRow(){
        return record.getFirstRow();
    }

    /**
     * Set the row of the first cell that contains the hyperlink
     *
     * @param row the 0-based row of the first cell that contains the hyperlink
     */
    @Override
    public void setFirstRow(int row){
        record.setFirstRow(row);
    }

    /**
     * Return the row of the last cell that contains the hyperlink
     *
     * @return the 0-based row of the last cell that contains the hyperlink
     */
    @Override
    public int getLastRow(){
        return record.getLastRow();
    }

    /**
     * Set the row of the last cell that contains the hyperlink
     *
     * @param row the 0-based row of the last cell that contains the hyperlink
     */
    @Override
    public void setLastRow(int row){
        record.setLastRow(row);
    }

    /**
     * Return the column of the first cell that contains the hyperlink
     *
     * @return the 0-based column of the first cell that contains the hyperlink
     */
    @Override
    public int getFirstColumn(){
        return record.getFirstColumn();
    }

    /**
     * Set the column of the first cell that contains the hyperlink
     *
     * @param col the 0-based column of the first cell that contains the hyperlink
     */
    @Override
    public void setFirstColumn(int col){
        record.setFirstColumn((short)col);
    }

    /**
     * Return the column of the last cell that contains the hyperlink
     *
     * @return the 0-based column of the last cell that contains the hyperlink
     */
    @Override
    public int getLastColumn(){
        return record.getLastColumn();
    }

    /**
     * Set the column of the last cell that contains the hyperlink
     *
     * @param col the 0-based column of the last cell that contains the hyperlink
     */
    @Override
    public void setLastColumn(int col){
        record.setLastColumn((short)col);
    }

    /**
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     *
     * @return  the address of this hyperlink
     */
    @Override
    public String getAddress(){
        return record.getAddress();
    }
    public String getTextMark(){
        return record.getTextMark();
    }

    /**
     * Convenience method equivalent to {@link #setAddress(String)}
     *
     * @param textMark the place in worksheet this hyperlink refers to, e.g. 'Target Sheet'!A1'
     */
    public void setTextMark(String textMark) {
        record.setTextMark(textMark);
    }
    public String getShortFilename(){
        return record.getShortFilename();
    }
    /**
     * Convenience method equivalent to {@link #setAddress(String)}
     *
     * @param shortFilename the path to a file this hyperlink points to, e.g. 'readme.txt'
     */
    public void setShortFilename(String shortFilename) {
        record.setShortFilename(shortFilename);
    }

    /**
     * Hyperlink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     *
     * @param address  the address of this hyperlink
     */
    @Override
    public void setAddress(String address){
        record.setAddress(address);
    }

    /**
     * Return text label for this hyperlink
     *
     * @return  text to display
     */
    @Override
    public String getLabel(){
        return record.getLabel();
    }

    /**
     * Sets text label for this hyperlink
     *
     * @param label text label for this hyperlink
     */
    @Override
    public void setLabel(String label){
        record.setLabel(label);
    }

    /**
     * Return the type of this hyperlink
     *
     * @return the type of this hyperlink
     * @see HyperlinkType#forInt
     */
    @Override
    public HyperlinkType getType() {
        return link_type;
    }
    
    /**
     * Return the type of this hyperlink
     *
     * @return the type of this hyperlink
     * @deprecated use <code>getType()</code> instead
     */
    @Deprecated
    @Removal(version = "4.2")
    @Override
    public HyperlinkType getTypeEnum() {
        return getType();
    }
    
    /**
     * @return whether the objects have the same HyperlinkRecord
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof HSSFHyperlink)) return false;
        HSSFHyperlink otherLink = (HSSFHyperlink) other;
        return record == otherLink.record;
    }
    
    @Override
    public int hashCode() {
        return record.hashCode();
    }
}
