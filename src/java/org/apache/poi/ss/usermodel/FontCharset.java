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

package org.apache.poi.ss.usermodel;

import org.apache.poi.util.Removal;

/**
 * Charset represents the basic set of characters associated with a font (that it can display), and 
 * corresponds to the ANSI codepage (8-bit or DBCS) of that character set used by a given language. 
 * 
 * @deprecated enum will be replaced by common version org.apache.poi.common.usermodel.FontCharset
 */
@Removal(version="4.0")
@Deprecated
public enum FontCharset {

     ANSI(0),
     DEFAULT(1),
     SYMBOL(2),
     MAC(77),
     SHIFTJIS(128),
     HANGEUL(129),
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

    private FontCharset(int value){
        charset = value;
    }

    /**
     * Returns value of this charset
     *
     * @return value of this charset
     */
    public int getValue(){
        return charset;
    }

    private static FontCharset[] _table = new FontCharset[256];
    static {
        for (FontCharset c : values()) {
            _table[c.getValue()] = c;
        }
    }

    public static FontCharset valueOf(int value){
        if(value >= _table.length)
           return null;
        return _table[value];
    }
}
