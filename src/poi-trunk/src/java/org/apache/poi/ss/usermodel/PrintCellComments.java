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

/**
 * These enumerations specify how cell comments shall be displayed for paper printing purposes.
 *
 * @author Gisella Bronzetti
 */
public enum PrintCellComments {

    /**
     * Do not print cell comments.
     */
    NONE(1),
    /**
     * Print cell comments as displayed.
     */
    AS_DISPLAYED(2),
    /**
     * Print cell comments at end of document.
     */
    AT_END(3);


    private int comments;

    private PrintCellComments(int comments) {
        this.comments = comments;
    }

    public int getValue() {
        return comments;
    }

    private static PrintCellComments[] _table = new PrintCellComments[4];
    static {
        for (PrintCellComments c : values()) {
            _table[c.getValue()] = c;
        }
    }

    public static PrintCellComments valueOf(int value){
        return _table[value];
    }
}
