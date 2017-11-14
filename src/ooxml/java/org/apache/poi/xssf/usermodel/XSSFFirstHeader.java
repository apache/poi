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

import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.xssf.usermodel.extensions.XSSFHeaderFooter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTHeaderFooter;

/**
 * 
 * First page header content. Corresponds to first printed page.
 * The first logical page in the sheet may not be printed, for example, if the print area is specified to 
 * be a range such that it falls outside the first page's scope.
 *
 */
public class XSSFFirstHeader extends XSSFHeaderFooter implements Header{

    /**
     * Create an instance of XSSFFirstHeader from the supplied XML bean
     * @see XSSFSheet#getFirstHeader()
     * @param headerFooter
     */
    protected XSSFFirstHeader(CTHeaderFooter headerFooter) {
        super(headerFooter);
        headerFooter.setDifferentFirst(true);
    }
    
    /**
     * Get the content text representing this header
     * @return text
     */
    public String getText() {
        return getHeaderFooter().getFirstHeader();
    }
    
    /**
     * Set a text for the header. If null unset the value
     * @see XSSFHeaderFooter to see how to create a string with Header/Footer Formatting Syntax
     * @param text - a string representing the header. 
     */
    public void setText(String text) {
    	if(text == null) {
    		getHeaderFooter().unsetFirstHeader();
    		if (!getHeaderFooter().isSetFirstFooter()) {
    		    getHeaderFooter().unsetDifferentFirst();
    		}
    	} else {
    		getHeaderFooter().setFirstHeader(text);
    	}
    }

}
