/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */


/*
 * HSSFErrorConstants.java
 *
 * Created on January 19, 2002, 9:30 AM
 */
package org.apache.poi.hssf.usermodel;

/**
 * contains constants representing Excel error codes.
 * @author  Michael Harhen
 */

public interface HSSFErrorConstants
{
    public static final byte ERROR_NULL  = 0x00;   // #NULL!
    public static final byte ERROR_DIV_0 = 0x07;   // #DIV/0!
    public static final byte ERROR_VALUE = 0x0f;   // #VALUE!
    public static final byte ERROR_REF   = 0x17;   // #REF!
    public static final byte ERROR_NAME  = 0x1d;   // #NAME?
    public static final byte ERROR_NUM   = 0x24;   // #NUM!
    public static final byte ERROR_NA    = 0x2a;   // #N/A
}
