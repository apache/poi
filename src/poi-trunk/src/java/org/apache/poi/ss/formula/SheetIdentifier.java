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

public class SheetIdentifier {
    public String _bookName;
    public NameIdentifier _sheetIdentifier;

    public SheetIdentifier(String bookName, NameIdentifier sheetIdentifier) {
        _bookName = bookName;
        _sheetIdentifier = sheetIdentifier;
    }
    public String getBookName() {
        return _bookName;
    }
    public NameIdentifier getSheetIdentifier() {
        return _sheetIdentifier;
    }
    protected void asFormulaString(StringBuffer sb) {
        if (_bookName != null) {
            sb.append(" [").append(_sheetIdentifier.getName()).append("]");
        }
        if (_sheetIdentifier.isQuoted()) {
            sb.append("'").append(_sheetIdentifier.getName()).append("'");
        } else {
            sb.append(_sheetIdentifier.getName());
        }
    }
    public String asFormulaString() {
        StringBuffer sb = new StringBuffer(32);
        asFormulaString(sb);
        return sb.toString();
    }
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName());
        sb.append(" [");
        asFormulaString(sb);
        sb.append("]");
        return sb.toString();
    }
}