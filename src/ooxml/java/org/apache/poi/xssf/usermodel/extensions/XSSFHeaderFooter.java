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

package org.apache.poi.xssf.usermodel.extensions;

import org.apache.poi.ss.usermodel.HeaderFooter;
import org.apache.poi.xssf.usermodel.helpers.HeaderFooterHelper;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHeaderFooter;

/**
 * Parent class of all XSSF headers and footers.
 * 
 * For a list of all the different fields that can be
 *  placed into a header or footer, such as page number,
 *  bold, underline etc, see 
 *  {@link org.apache.poi.hssf.usermodel.HeaderFooter}.
 */
public abstract class XSSFHeaderFooter implements HeaderFooter {
    private HeaderFooterHelper helper;
    private CTHeaderFooter headerFooter;

	private boolean stripFields = false;
	
    public XSSFHeaderFooter(CTHeaderFooter headerFooter) {
       this.headerFooter = headerFooter;
       this.helper = new HeaderFooterHelper();
    }
    
    public CTHeaderFooter getHeaderFooter() {
        return this.headerFooter;
    }

    public String getValue() {
        String value = getText();
        if(value == null)
        	return "";
        return value;
    }
    
    
	/**
	 * Are fields currently being stripped from
	 *  the text that this {@link XSSFHeaderFooter} returns?
	 *  Default is false, but can be changed
	 */
	public boolean areFieldsStripped() {
		return stripFields;
	}
	/**
	 * Should fields (eg macros) be stripped from
	 *  the text that this class returns?
	 * Default is not to strip.
	 * @param stripFields
	 */
	public void setAreFieldsStripped(boolean stripFields) {
		this.stripFields = stripFields;
	}
	
	public static String stripFields(String text) {
		return org.apache.poi.hssf.usermodel.HeaderFooter.stripFields(text);
	}

    
    public abstract String getText();
    
    protected abstract void setText(String text);

    public String getCenter() {
    	String text = helper.getCenterSection(getText()); 
    	if(stripFields)
    		return stripFields(text);
        return text;
    }

    public String getLeft() {
        String text = helper.getLeftSection(getText());
    	if(stripFields)
    		return stripFields(text);
        return text;
    }

    public String getRight() {
        String text = helper.getRightSection(getText());
    	if(stripFields)
    		return stripFields(text);
        return text;
    }

    public void setCenter(String newCenter) {
        setText(helper.setCenterSection(getText(), newCenter));
    }

    public void setLeft(String newLeft) {
        setText(helper.setLeftSection(getText(), newLeft));
    }

    public void setRight(String newRight) {
        setText(helper.setRightSection(getText(), newRight));
    }
}
