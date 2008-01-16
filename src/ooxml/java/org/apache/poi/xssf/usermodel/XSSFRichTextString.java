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

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;


/**
 * TODO - the rich part
 */
public class XSSFRichTextString implements RichTextString {
    private String string;
    
    public XSSFRichTextString(String str) {
        this.string = str;
    }
    
    public void applyFont(int startIndex, int endIndex, short fontIndex) {
        // TODO Auto-generated method stub

    }

    public void applyFont(int startIndex, int endIndex, Font font) {
        // TODO Auto-generated method stub

    }

    public void applyFont(Font font) {
        // TODO Auto-generated method stub

    }

    public void applyFont(short fontIndex) {
        // TODO Auto-generated method stub

    }

    public void clearFormatting() {
        // TODO Auto-generated method stub

    }

    public int compareTo(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    public short getFontAtIndex(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    public short getFontOfFormattingRun(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getIndexOfFormattingRun(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getString() {
        return string;
    }

    public int length() {
        return string.length();
    }

    public int numFormattingRuns() {
        // TODO Auto-generated method stub
        return 0;
    }
}
