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

import org.apache.poi.hssf.record.HyperlinkRecord;
import org.apache.poi.ss.usermodel.Hyperlink;

/**
 * Represents an Excel hyperlink.
 *
 * @author Yegor Kozlov (yegor at apache dot org)
 */
public class HSSFHyperlink implements Hyperlink {

    /**
     * Link to a existing file or web page
     */
    public static final int LINK_URL = 1;

    /**
     * Link to a place in this document
     */
    public static final int LINK_DOCUMENT = 2;

    /**
     * Link to an E-mail address
     */
    public static final int LINK_EMAIL = 3;

    /**
     * Link to a file
     */
    public static final int LINK_FILE = 4;

    /**
     * Low-level record object that stores the actual hyperlink data
     */
    protected HyperlinkRecord record = null;

    /**
     * If we create a new hypelrink remember its type
     */
    protected int link_type;

    /**
     * Construct a new hyperlink
     *
     * @param type the type of hyperlink to create
     */
    public HSSFHyperlink( int type )
    {
        this.link_type = type;
        record = new HyperlinkRecord();
        switch(type){
            case LINK_URL:
            case LINK_EMAIL:
                record.newUrlLink();
                break;
            case LINK_FILE:
                record.newFileLink();
                break;
            case LINK_DOCUMENT:
                record.newDocumentLink();
                break;
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
    }

    /**
     * Return the row of the first cell that contains the hyperlink
     *
     * @return the 0-based row of the cell that contains the hyperlink
     */
    public int getFirstRow(){
        return record.getFirstRow();
    }

    /**
     * Set the row of the first cell that contains the hyperlink
     *
     * @param row the 0-based row of the first cell that contains the hyperlink
     */
    public void setFirstRow(int row){
        record.setFirstRow(row);
    }

    /**
     * Return the row of the last cell that contains the hyperlink
     *
     * @return the 0-based row of the last cell that contains the hyperlink
     */
    public int getLastRow(){
        return record.getLastRow();
    }

    /**
     * Set the row of the last cell that contains the hyperlink
     *
     * @param row the 0-based row of the last cell that contains the hyperlink
     */
    public void setLastRow(int row){
        record.setLastRow(row);
    }

    /**
     * Return the column of the first cell that contains the hyperlink
     *
     * @return the 0-based column of the first cell that contains the hyperlink
     */
    public int getFirstColumn(){
        return record.getFirstColumn();
    }

    /**
     * Set the column of the first cell that contains the hyperlink
     *
     * @param col the 0-based column of the first cell that contains the hyperlink
     */
    public void setFirstColumn(int col){
        record.setFirstColumn((short)col);
    }

    /**
     * Return the column of the last cell that contains the hyperlink
     *
     * @return the 0-based column of the last cell that contains the hyperlink
     */
    public int getLastColumn(){
        return record.getLastColumn();
    }

    /**
     * Set the column of the last cell that contains the hyperlink
     *
     * @param col the 0-based column of the last cell that contains the hyperlink
     */
    public void setLastColumn(int col){
        record.setLastColumn((short)col);
    }

    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, path to a file, etc.
     *
     * @return  the address of this hyperlink
     */
    public String getAddress(){
        return record.getAddress();
    }
    public String getTextMark(){
        return record.getTextMark();
    }

    /**
     * Convenience method equivalent to {@link #setAddress(String)}
     *
     * @param textMark the place in worksheet this hypelrink referes to, e.g. 'Target Sheet'!A1'
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
     * @param shortFilename the path to a file this hypelrink points to, e.g. 'readme.txt'
     */
    public void setShortFilename(String shortFilename) {
        record.setShortFilename(shortFilename);
    }

    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, patrh to a file, etc.
     *
     * @param address  the address of this hyperlink
     */
    public void setAddress(String address){
        record.setAddress(address);
    }

    /**
     * Return text label for this hyperlink
     *
     * @return  text to display
     */
    public String getLabel(){
        return record.getLabel();
    }

    /**
     * Sets text label for this hyperlink
     *
     * @param label text label for this hyperlink
     */
    public void setLabel(String label){
        record.setLabel(label);
    }

    /**
     * Return the type of this hyperlink
     *
     * @return the type of this hyperlink
     */
    public int getType(){
        return link_type;
    }
}
