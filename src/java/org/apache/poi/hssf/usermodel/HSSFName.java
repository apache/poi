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

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.util.RangeAddress;
import org.apache.poi.ss.usermodel.Name;

/**
 * Title:        High Level Represantion of Named Range <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 */

public class HSSFName implements Name {
    private HSSFWorkbook         book;
    private NameRecord       name;
    
    /** Creates new HSSFName   - called by HSSFWorkbook to create a sheet from
     * scratch.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createName()
     * @param name the Name Record
     * @param book lowlevel Workbook object associated with the sheet.
     */
    
    protected HSSFName(HSSFWorkbook book, NameRecord name) {
        this.book = book;
        this.name = name;
    }
    
    /** Get the sheets name which this named range is referenced to
     * @return sheet name, which this named range refered to
     */    

    public String getSheetName() {
        String result ;
        short indexToExternSheet = name.getExternSheetNumber();
        
        result = book.getWorkbook().findSheetNameFromExternSheet(indexToExternSheet);
        
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
        Workbook wb = book.getWorkbook();
        
        //Check to ensure no other names have the same case-insensitive name
        for ( int i = wb.getNumNames()-1; i >=0; i-- )
        {
        	NameRecord rec = wb.getNameRecord(i);
        	if (rec != name) {
        		if (rec.getNameText().equalsIgnoreCase(getNameName()))
        			throw new IllegalArgumentException("The workbook already contains this name (case-insensitive)");
        	}
        }
    }

    /** 
     * gets the reference of the named range
     * @return reference of the named range
     */    

    public String getReference() {
        String result;
        result = name.getAreaReference(book);

        return result;
    }

    

    /** 
     * sets the sheet name which this named range referenced to
     * @param sheetName the sheet name of the reference
     */    

    private void setSheetName(String sheetName){
        int sheetNumber = book.getSheetIndex(sheetName);

        short externSheetNumber = book.getExternalSheetIndex(sheetNumber);
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
