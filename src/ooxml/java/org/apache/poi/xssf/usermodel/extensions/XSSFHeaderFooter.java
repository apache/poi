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

public abstract class XSSFHeaderFooter implements HeaderFooter {
    private HeaderFooterHelper helper;
    private CTHeaderFooter headerFooter;

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
    
    public abstract String getText();
    
    protected abstract void setText(String text);

    public String getCenter() {
        return helper.getCenterSection(getText());
    }

    public String getLeft() {
        return helper.getLeftSection(getText());
    }

    public String getRight() {
        return helper.getRightSection(getText());
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
