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

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Utility to identify builtin formats
 * 
 * @author Yegor Kozlov
 */
public class BuiltinFormats {
    /**
     * The first user-defined format starts at 164.
     */
    public static final int FIRST_USER_DEFINED_FORMAT_INDEX = 164;

    private final static Map<Integer, String> formats;

    static {
        formats = new LinkedHashMap<Integer, String>();
        formats.put( 0, "General" );
        formats.put( 1, "0" );
        formats.put( 2, "0.00" );
        formats.put( 3, "#,##0" );
        formats.put( 4, "#,##0.00" );
        formats.put( 5, "($#,##0_);($#,##0)" );
        formats.put( 6, "($#,##0_);[Red]($#,##0)" );
        formats.put( 7, "($#,##0.00);($#,##0.00)" );
        formats.put( 8, "($#,##0.00_);[Red]($#,##0.00)" );
        formats.put( 9, "0%" );
        formats.put( 0xa, "0.00%" );
        formats.put( 0xb, "0.00E+00" );
        formats.put( 0xc, "# ?/?" );
        formats.put( 0xd, "# ??/??" );
        formats.put( 0xe, "m/d/yy" );
        formats.put( 0xf, "d-mmm-yy" );
        formats.put( 0x10, "d-mmm" );
        formats.put( 0x11, "mmm-yy" );
        formats.put( 0x12, "h:mm AM/PM" );
        formats.put( 0x13, "h:mm:ss AM/PM" );
        formats.put( 0x14, "h:mm" );
        formats.put( 0x15, "h:mm:ss" );
        formats.put( 0x16, "m/d/yy h:mm" );

        // 0x17 - 0x24 reserved for international and undocumented
        formats.put( 0x17, "0x17" );
        formats.put( 0x18, "0x18" );
        formats.put( 0x19, "0x19" );
        formats.put( 0x1a, "0x1a" );
        formats.put( 0x1b, "0x1b" );
        formats.put( 0x1c, "0x1c" );
        formats.put( 0x1d, "0x1d" );
        formats.put( 0x1e, "0x1e" );
        formats.put( 0x1f, "0x1f" );
        formats.put( 0x20, "0x20" );
        formats.put( 0x21, "0x21" );
        formats.put( 0x22, "0x22" );
        formats.put( 0x23, "0x23" );
        formats.put( 0x24, "0x24" );

        // 0x17 - 0x24 reserved for international and undocumented
        formats.put( 0x25, "(#,##0_);(#,##0)" );
        formats.put( 0x26, "(#,##0_);[Red](#,##0)" );
        formats.put( 0x27, "(#,##0.00_);(#,##0.00)" );
        formats.put( 0x28, "(#,##0.00_);[Red](#,##0.00)" );
        formats.put( 0x29, "_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)" );
        formats.put( 0x2a, "_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)" );
        formats.put( 0x2b, "_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)" );
        formats.put( 0x2c,
                "_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)" );
        formats.put( 0x2d, "mm:ss" );
        formats.put( 0x2e, "[h]:mm:ss" );
        formats.put( 0x2f, "mm:ss.0" );
        formats.put( 0x30, "##0.0E+0" );
        formats.put( 0x31, "@" );
    }

    /**
     * Get the index-formatString map with built-in data formats
     *
     * @return built-in data formats
     */
    public static Map<Integer, String> getBuiltinFormats(){
        return formats;
    }

    /**
     * Get the format string that matches the given format index
     *
     * @param index of a built in format
     * @return string represented at index of format or null if there is not a builtin format at that index
     */
    public static String getBuiltinFormat(int index){
        return formats.get(index);
    }

    /**
     * Get the format index that matches the given format string
     * <p>
     * Automatically converts "text" to excel's format string to represent text.
     * </p>
     * @param fmt string matching a built-in format
     * @return index of format or -1 if undefined.
     */
    public static int getBuiltinFormat(String fmt){
        int idx = -1;
        if (fmt.toUpperCase().equals("TEXT")) fmt = "@";

        for(int key : formats.keySet()) {
            if(fmt.equals(formats.get(key))) {
                idx = key;
                break;
            }
        }
        return idx;
    }
}
