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

import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellComments;

/**
 * These enumerations specify how cell comments shall be displayed for paper printing purposes.
 *
 * @author Gisella Bronzetti
 */
public enum PrintCellComments {

    /**
     * Do not print cell comments.
     */
    NONE(STCellComments.NONE),
    /**
     * Print cell comments as displayed.
     */
    AS_DISPLAYED(STCellComments.AS_DISPLAYED),
    /**
     * Print cell comments at end of document.
     */
    AT_END(STCellComments.AT_END);


    private STCellComments.Enum comments;


    PrintCellComments(STCellComments.Enum comments) {
        this.comments = comments;
    }


    /**
     * Returns comments of cell
     *
     * @return String comments of cell
     */
    public STCellComments.Enum getValue() {
        return comments;
    }


    public static PrintCellComments valueOf(STCellComments.Enum cellComment) {
        switch (cellComment.intValue()) {
            case STCellComments.INT_AS_DISPLAYED:
                return AS_DISPLAYED;
            case STCellComments.INT_AT_END:
                return AT_END;
            case STCellComments.INT_NONE:
                return NONE;
        }
        throw new RuntimeException("PrintCellComments: value [" + cellComment + "] not supported");
    }

}
