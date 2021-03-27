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
 * <p>
 * Even page header value. Corresponds to even printed pages. Even page(s) in
 * the sheet may not be printed, for example, if the print area is specified to
 * be a range such that it falls outside an even page's scope. If no even header
 * is specified, then odd header value is assumed for even page headers.
 * </p><p>
 * The even header is activated by the "Different Even/Odd" Header/Footer property for the sheet.
 * If this property is not set, the even header is ignored, and the odd footer is used instead.
 * </p><p>
 * Creating an even header or footer sets this property by default, so all you need to do to
 * get an even header or footer to display is to create it. Likewise, if both the even header
 * and footer are usnset, then this property is unset, and the odd header and footer are used
 * for even pages.
 * </p>
 */
public class XSSFEvenHeader extends XSSFHeaderFooter implements Header {

    /**
     * Create an instance of XSSFEvenHeader from the supplied XML bean. If an even
     * header is created, The property "DifferentOddEven" is set for this sheet as well.
     * 
     * @see XSSFSheet#getEvenHeader()
     * @param headerFooter
     */
    protected XSSFEvenHeader(CTHeaderFooter headerFooter) {
        super(headerFooter);
        headerFooter.setDifferentOddEven(true);
    }

    /**
     * Get the content text representing this header
     * 
     * @return text
     */
    @Override
    public String getText() {
        return getHeaderFooter().getEvenHeader();
    }

    /**
     * Set a text for the header. If null, unset the value. If unsetting and there is no
     * Even Footer for this sheet, the "DifferentEvenOdd" property for this sheet is
     * unset.
     * 
     * @see XSSFHeaderFooter to see how to create a string with Header/Footer
     *      Formatting Syntax
     * @param text
     *            - a string representing the header.
     */
    @Override
    public void setText(String text) {
        if (text == null) {
            getHeaderFooter().unsetEvenHeader();
            if (!getHeaderFooter().isSetEvenFooter()) {
                getHeaderFooter().unsetDifferentOddEven();
            }
        } else {
            getHeaderFooter().setEvenHeader(text);
        }
    }

}
