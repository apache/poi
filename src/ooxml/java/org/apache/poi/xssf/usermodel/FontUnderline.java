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

import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnderlineValues;


/**
 * the different types of possible underline formatting
 * 
 * @author Gisella Bronzetti
 *
 */
public enum FontUnderline {
    
/**
 * Double-line underlining under each character in the
 * cell. underlines are drawn through the descenders of
 * characters such as g and p.
 */
   DOUBLE(STUnderlineValues.DOUBLE),
   DOUBLE_ACCOUNTING(STUnderlineValues.DOUBLE_ACCOUNTING),
   NONE(STUnderlineValues.NONE),
   SINGLE(STUnderlineValues.SINGLE),
   SINGLE_ACCOUNTING(STUnderlineValues.SINGLE_ACCOUNTING);
    
    private STUnderlineValues.Enum underline;

    
    FontUnderline(STUnderlineValues.Enum value){
        underline = value;
    }

    /**
     * Returns index of this font family
     *
     * @return index of this font family
     */
    public STUnderlineValues.Enum getValue(){
        return underline;
    }
    
    public static FontUnderline valueOf(STUnderlineValues.Enum underline){
	switch (underline.intValue()) {
	case STUnderlineValues.INT_DOUBLE:
	    return DOUBLE;
	case STUnderlineValues.INT_DOUBLE_ACCOUNTING:
	    return DOUBLE_ACCOUNTING;
	case STUnderlineValues.INT_SINGLE:
	    return SINGLE;
	case STUnderlineValues.INT_SINGLE_ACCOUNTING:
	    return SINGLE_ACCOUNTING;
	case STUnderlineValues.INT_NONE:
	    return NONE;
	}
	throw new RuntimeException("Underline value ["+ underline +"] not supported");    
    }
    
    
}
