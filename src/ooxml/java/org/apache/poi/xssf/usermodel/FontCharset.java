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


/**
 * Charset represents the basic set of characters associated with a font (that it can display), and 
 * corresponds to the ANSI codepage (8-bit or DBCS) of that character set used by a given language. 
 * 
 * @author Gisella Bronzetti
 *
 */
public enum FontCharset {

     ANSI(0),
     DEFAULT(1),
     SYMBOL(2),
     MAC(77),
     SHIFTJIS(128),
     HANGEUL(129),
     HANGUL(129),
     JOHAB(130),
     GB2312(134),
     CHINESEBIG5(136),
     GREEK(161),
     TURKISH(162),
     VIETNAMESE(163),
     HEBREW(177),
     ARABIC(178),
     BALTIC(186),
     RUSSIAN(204),
     THAI(222),
     EASTEUROPE(238),
     OEM(255);

    
    private int charset;

    
    FontCharset(int value){
        charset = value;
    }

    /**
     * Returns index of this charset
     *
     * @return index of this charset
     */
    public byte getValue(){
        return (byte)charset;
    }
    
    public static FontCharset valueOf(int value){
	switch (value) {
	case 0:
	    return ANSI;
	case 1:
	    return DEFAULT;
	case 2:
	    return SYMBOL;
	case 77:
	    return MAC;
	case 128:
	    return SHIFTJIS;
	case 129:
	    return HANGEUL;
	case 130:
	    return JOHAB;
	case 134:
	    return GB2312;
	case 136:
	    return CHINESEBIG5;
	case 161:
	    return GREEK;
	case 162:
	    return TURKISH;
	case 163:
	    return VIETNAMESE;
	case 177:
	    return HEBREW;
	case 178:
	    return ARABIC;
	case 186:
	    return BALTIC;
	case 204:
	    return RUSSIAN;
	case 222:
	    return THAI;
	case 238:
	    return EASTEUROPE;
	case 255:
	    return OEM;
	}
	throw new RuntimeException("Charset value ["+ value +"] not supported");    
    }
    
}
