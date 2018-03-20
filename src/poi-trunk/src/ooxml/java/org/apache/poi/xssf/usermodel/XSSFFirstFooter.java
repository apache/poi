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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.xssf.usermodel.extensions.XSSFHeaderFooter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHeaderFooter;

/**
 * <p>
 * First page footer content. Corresponds to first printed page.  
 * The first logical page in the sheet may not be printed, for example, if the print area is specified to 
 * be a range such that it falls outside the first page's scope.
 * </p><p>
 * The first page footer is activated by the "Different First" Header/Footer property for the sheet.
 * If this property is not set, the first page footer is ignored.
 * </p><p>
 * Creating a first page header or footer sets this property by default, so all you need to do to
 * get an first page header or footer to display is to create one. Likewise, if both the first page
 * header and footer are usnset, then this property is unset, and the first page header and footer
 * are ignored.
 * </p>
 */
public class XSSFFirstFooter extends XSSFHeaderFooter implements Footer{

    /**
     * Create an instance of XSSFFirstFooter from the supplied XML bean
     * @see XSSFSheet#getFirstFooter()
     * @param headerFooter
     */
    protected XSSFFirstFooter(CTHeaderFooter headerFooter) {
        super(headerFooter);
        headerFooter.setDifferentFirst(true);
    }
    
    /**
     * Get the content text representing the footer
     * @return text
     */
    @Override
    public String getText() {
        return getHeaderFooter().getFirstFooter();
    }
    
    /**
     * Set a text for the footer. If null unset the value. If unsetting this header results 
     * in no First Header, or footer for the sheet, the 'differentFirst' property is unset as well.
     *  
     * @see XSSFHeaderFooter to see how to create a string with Header/Footer Formatting Syntax
     * @param text - a string representing the footer. 
     */
    @Override
    public void setText(String text) {
    	if(text == null) {
    		getHeaderFooter().unsetFirstFooter();
    		if (!getHeaderFooter().isSetFirstHeader()) {
    		    getHeaderFooter().unsetDifferentFirst();
    		}
    	} else {
    		getHeaderFooter().setFirstFooter(text);
    	}
    }
}
