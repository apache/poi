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
package org.apache.poi.xwpf.usermodel;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

/**
 * Sketch of XWPFTable class. Only table's text is being hold.
 * 
 * @author Yury Batrakov (batrakov at gmail.com)
 * 
 */
public class XWPFTable
{
    protected StringBuffer text=new StringBuffer(); 
    
    public XWPFTable(CTTbl table) {
        for(CTRow row : table.getTrArray()) {
        	StringBuffer rowText = new StringBuffer();
            for(CTTc cell : row.getTcArray()) {
                for(CTP ctp : cell.getPArray()) {
                    XWPFParagraph p = new XWPFParagraph(ctp, null);
                    if(rowText.length() > 0) {
                    	rowText.append('\t');
                    }
                    rowText.append(p.getText());
                }
            }
            if(rowText.length() > 0) {
            	this.text.append(rowText);
            	this.text.append('\n');
            }
        }
    }
    
    public String getText() {
        return text.toString();
    }
}
