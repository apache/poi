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

public interface Font {

    /**
     * Arial font
     */

    public final static String FONT_ARIAL          = "Arial";

    /**
     * Normal boldness (not bold)
     */

    public final static short  BOLDWEIGHT_NORMAL   = 0x190;

    /**
     * Bold boldness (bold)
     */

    public final static short  BOLDWEIGHT_BOLD     = 0x2bc;

    /**
     * normal type of black color.
     */

    public final static short  COLOR_NORMAL        = 0x7fff;

    /**
     * Dark Red color
     */

    public final static short  COLOR_RED           = 0xa;

    /**
     * no type offsetting (not super or subscript)
     */

    public final static short  SS_NONE             = 0;

    /**
     * superscript
     */

    public final static short  SS_SUPER            = 1;

    /**
     * subscript
     */

    public final static short  SS_SUB              = 2;

    /**
     * not underlined
     */

    public final static byte   U_NONE              = 0;

    /**
     * single (normal) underline
     */

    public final static byte   U_SINGLE            = 1;

    /**
     * double underlined
     */

    public final static byte   U_DOUBLE            = 2;

    /**
     * accounting style single underline
     */

    public final static byte   U_SINGLE_ACCOUNTING = 0x21;

    /**
     * accounting style double underline
     */

    public final static byte   U_DOUBLE_ACCOUNTING = 0x22;

    /**
     * ANSI character set
     */
    public final static byte ANSI_CHARSET = 0;

    /**
     * Default character set.
     */
    public final static byte DEFAULT_CHARSET = 1;

    /**
     * Symbol character set
     */
    public final static byte SYMBOL_CHARSET = 2;


}
