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

import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.TextObjectRecord;
import org.apache.poi.hssf.record.HyperlinkRecord;
import org.apache.poi.ddf.*;

import java.util.Map;
import java.util.List;
import java.util.Iterator;

/**
 * Represents a hyperlink.
 *
 * @author Yegor Kozlov
 */
public class HSSFHyperlink {

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
     * Unknown type
     */
    public static final int LINK_UNKNOWN = 4;

    /**
     * Low-level record object that stores the actual hyperlink data
     */
    private HyperlinkRecord record = null;

    protected HSSFHyperlink( HyperlinkRecord record )
    {
        this.record = record;
    }

    /**
     * Return the row of the cell that contains the hyperlink
     *
     * @return the 0-based row of the cell that contains the hyperlink
     */
    public int getRow(){
        return record.getRow();
    }

    /**
     * Set the row of the cell that contains the hyperlink
     *
     * @param row the 0-based row of the cell that contains the hyperlink
     */
    public void setRow(int row){
        record.setRow(row);
    }

    /**
     * Return the column of the cell that contains the hyperlink
     *
     * @return the 0-based column of the cell that contains the hyperlink
     */
    public short getColumn(){
        return record.getColumn();
    }

    /**
     * Set the column of the cell that contains the hyperlink
     *
     * @param col the 0-based column of the cell that contains the hyperlink
     */
    public void setColumn(short col){
        record.setColumn(col);
    }

    /**
     * Hypelink address. Depending on the hyperlink type it can be URL, e-mail, etc.
     *
     * @return  the address of this hyperlink
     */
    public String getAddress(){
        return record.getUrlString();
    }

    /**
     * Return text to display for this hyperlink
     *
     * @return  text to display
     */
    public String getLabel(){
        return record.getLabel();
    }

    /**
     * Return the type of this hyperlink
     *
     * @return the type of this hyperlink
     */
    public int getType(){
        throw new RuntimeException("Not implemented");
    }
}
