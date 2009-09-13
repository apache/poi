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
 * The enumeration value indicating the print orientation for a sheet.
 *
 * @author Gisella Bronzetti
 */
public enum PrintOrientation {

    /**
     * orientation not specified
     */
    DEFAULT(1),
    /**
     * portrait orientation
     */
    PORTRAIT(2),
    /**
     * landscape orientations
     */
    LANDSCAPE(3);


    private int orientation;

    private PrintOrientation(int orientation) {
        this.orientation = orientation;
    }


    public int getValue() {
        return orientation;
    }


    private static PrintOrientation[] _table = new PrintOrientation[4];
    static {
        for (PrintOrientation c : values()) {
            _table[c.getValue()] = c;
        }
    }

    public static PrintOrientation valueOf(int value){
        return _table[value];
    }
}
