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

import org.openxmlformats.schemas.spreadsheetml.x2006.main.STOrientation;

/**
 * The enumeration value indicating the print orientation for a sheet.
 *
 * @author Gisella Bronzetti
 */
public enum PrintOrientation {

    /**
     * orientation not specified
     */
    DEFAULT(STOrientation.DEFAULT),
    /**
     * portrait orientation
     */
    PORTRAIT(STOrientation.PORTRAIT),
    /**
     * landscape orientations
     */
    LANDSCAPE(STOrientation.LANDSCAPE);


    private STOrientation.Enum orientation;


    PrintOrientation(STOrientation.Enum orientation) {
        this.orientation = orientation;
    }


    /**
     * Returns value of the orientation
     *
     * @return String value of the orientation
     */
    public STOrientation.Enum getValue() {
        return orientation;
    }


    public static PrintOrientation valueOf(STOrientation.Enum orient) {
        switch (orient.intValue()) {
            case STOrientation.INT_DEFAULT:
                return DEFAULT;
            case STOrientation.INT_LANDSCAPE:
                return LANDSCAPE;
            case STOrientation.INT_PORTRAIT:
                return PORTRAIT;
                /*
        default:
            return DEFAULT;
            */
        }
        throw new RuntimeException("Orientation value [" + orient + "] not supported");
    }

}
