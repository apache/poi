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

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.util.Internal;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

/**
 * Wrapper class around String so that we can use it in Comment.
 * Nothing has been implemented yet except for {@link #getString()}.
 *
 * @since 3.16-beta3
 */
@Internal
class XSSFBRichTextString extends XSSFRichTextString {

    private final String string;

    XSSFBRichTextString(String string) {
        this.string = string;
    }

    @Override
    @NotImplemented
    public void applyFont(int startIndex, int endIndex, short fontIndex) {

    }

    @Override
    @NotImplemented
    public void applyFont(int startIndex, int endIndex, Font font) {

    }

    @Override
    @NotImplemented
    public void applyFont(Font font) {

    }

    @Override
    @NotImplemented
    public void clearFormatting() {

    }

    @Override
    public String getString() {
        return string;
    }

    @Override
    public int length() {
        return string.length();
    }

    @Override
    @NotImplemented
    public int numFormattingRuns() {
        return 0;
    }

    @Override
    @NotImplemented
    public int getIndexOfFormattingRun(int index) {
        return 0;
    }

    @Override
    @NotImplemented
    public void applyFont(short fontIndex) {

    }
}
