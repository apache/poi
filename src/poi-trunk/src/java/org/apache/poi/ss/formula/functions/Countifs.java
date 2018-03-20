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


package org.apache.poi.ss.formula.functions;

/**
 * Implementation for the function COUNTIFS
 * <p>
 * Syntax: COUNTIFS(criteria_range1, criteria1, [criteria_range2, criteria2])
 * </p>
 */

public class Countifs extends Baseifs {
    /**
     * Singleton
     */
    public static final FreeRefFunction instance = new Countifs();

    /**
     * https://support.office.com/en-us/article/COUNTIFS-function-dda3dc6e-f74e-4aee-88bc-aa8c2a866842?ui=en-US&rs=en-US&ad=US
     * COUNTIFS(criteria_range1, criteria1, [criteria_range2, criteria2]...)
     * need at least 2 arguments and need to have an even number of arguments (criteria_range1, criteria1 plus x*(criteria_range, criteria))
     * @see org.apache.poi.ss.formula.functions.Baseifs#hasInitialRange()
     */
    protected boolean hasInitialRange() {
        return false;
    }
}

