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

package org.apache.poi.ss.formula;

public class SheetRangeIdentifier extends SheetIdentifier {
    public NameIdentifier _lastSheetIdentifier;

    public SheetRangeIdentifier(String bookName, NameIdentifier firstSheetIdentifier, NameIdentifier lastSheetIdentifier) {
        super(bookName, firstSheetIdentifier);
        _lastSheetIdentifier = lastSheetIdentifier;
    }
    public NameIdentifier getFirstSheetIdentifier() {
        return super.getSheetIdentifier();
    }
    public NameIdentifier getLastSheetIdentifier() {
        return _lastSheetIdentifier;
    }
    protected void asFormulaString(StringBuffer sb) {
        super.asFormulaString(sb);
        sb.append(':');
        if (_lastSheetIdentifier.isQuoted()) {
            sb.append("'").append(_lastSheetIdentifier.getName()).append("'");
        } else {
            sb.append(_lastSheetIdentifier.getName());
        }
    }
}