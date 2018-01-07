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

package org.apache.poi.xssf.binary;

import org.apache.poi.util.Internal;
import org.apache.poi.xssf.usermodel.helpers.HeaderFooterHelper;

/**
 * @since 3.16-beta3
 */
@Internal
class XSSFBHeaderFooter {

    private static final HeaderFooterHelper HEADER_FOOTER_HELPER = new HeaderFooterHelper();

    private final String headerFooterTypeLabel;
    private final boolean isHeader;
    private String rawString;


    XSSFBHeaderFooter(String headerFooterTypeLabel, boolean isHeader) {
        this.headerFooterTypeLabel = headerFooterTypeLabel;
        this.isHeader = isHeader;
    }

    String getHeaderFooterTypeLabel() {
        return headerFooterTypeLabel;
    }

    String getRawString() {
        return rawString;
    }

    String getString() {
        StringBuilder sb = new StringBuilder();
        String left = HEADER_FOOTER_HELPER.getLeftSection(rawString);
        String center = HEADER_FOOTER_HELPER.getCenterSection(rawString);
        String right = HEADER_FOOTER_HELPER.getRightSection(rawString);
        if (left != null && left.length() > 0) {
            sb.append(left);
        }
        if (center != null && center.length() > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(center);
        }
        if (right != null && right.length() > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(right);
        }
        return sb.toString();
    }

    void setRawString(String rawString) {
        this.rawString = rawString;
    }

    boolean isHeader() {
        return isHeader;
    }

}
