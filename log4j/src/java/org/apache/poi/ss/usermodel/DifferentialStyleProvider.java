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
 * Interface for classes providing differential style definitions, such as conditional format rules
 * and table/pivot table styles.
 * 
 * @since 3.17 beta 1
 */
public interface DifferentialStyleProvider {

    /**
     * @return - border formatting object  if defined,  <code>null</code> otherwise
     */
    BorderFormatting getBorderFormatting();

    /**
     * @return - font formatting object  if defined,  <code>null</code> otherwise
     */
    FontFormatting getFontFormatting();

    /**
     *
     * @return number format defined for this rule, or null if the cell default should be used
     */
    ExcelNumberFormat getNumberFormat();

    /**
     * @return - pattern formatting object if defined, <code>null</code> otherwise
     */
    PatternFormatting getPatternFormatting();

    /**
     * This is the number of rows or columns in a band or stripe.
     * For styles that represent stripes, it must be > 1, for all others it is 0.
     * Not the greatest overloading by the OOXML spec.
     * @return number of rows/columns in a stripe for stripe styles, 0 for all others 
     */
    int getStripeSize();
}
