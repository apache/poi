/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.util.RangeAddress;
import org.apache.poi.hssf.util.SheetReferences;

/**
 * Title:        High Level Represantion of Named Range <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 */

public class HSSFName {
    private Workbook         book;
    private NameRecord       name;
    
    /** Creates new HSSFName   - called by HSSFWorkbook to create a sheet from
     * scratch.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createName()
     * @param name the Name Record
     * @param book - lowlevel Workbook object associated with the sheet.
     * @param book the Workbook */
    
    protected HSSFName(Workbook book, NameRecord name) {
        this.book = book;
        this.name = name;
    }
    
    /** Get the sheets name which this named range is referenced to
     * @return sheet name, which this named range refered to
     */    

    public String getSheetName() {
        String result ;
        short indexToExternSheet = name.getExternSheetNumber();
        
        result = book.findSheetNameFromExternSheet(indexToExternSheet);
        
        return result;
    }
    
    /** 
     * gets the name of the named range
     * @return named range name
     */    

    public String getNameName(){
        String result = name.getNameText();
        
        return result;
    }
    
    /** 
     * sets the name of the named range
     * @param nameName named range name to set
     */    

    public void setNameName(String nameName){
        name.setNameText(nameName);
        name.setNameTextLength((byte)nameName.length());
    }

    /** 
     * gets the reference of the named range
     * @return reference of the named range
     */    

    public String getReference() {
        String result;
        SheetReferences refs = book.getSheetReferences();
        result = name.getAreaReference(refs);

        return result;
    }

    

    /** 
     * sets the sheet name which this named range referenced to
     * @param sheetName the sheet name of the reference
     */    

    private void setSheetName(String sheetName){
        int sheetNumber = book.getSheetIndex(sheetName);

        short externSheetNumber = book.checkExternSheet(sheetNumber);
        name.setExternSheetNumber(externSheetNumber);
//        name.setIndexToSheet(externSheetNumber);

    }

  
    /** 
     * sets the reference of this named range
     * @param ref the reference to set
     */    

    public void setReference(String ref){

        RangeAddress ra = new RangeAddress(ref);

        String sheetName = ra.getSheetName();

        if (ra.hasSheetName()) {
            setSheetName(sheetName);
        }

		//allow the poi utilities to parse it out
        name.setAreaReference(ref);

    }

}

