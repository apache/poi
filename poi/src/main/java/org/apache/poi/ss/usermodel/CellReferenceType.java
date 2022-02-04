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
 * Types of cell references.
 * @since POI 5.2.1
 */
public enum CellReferenceType {

    /**
     * Cells are referenced in the form A1, B4, etc.
     */
    A1,
    
    /**
     * Cells are referenced in the form R1C1, R4C2, etc.
     */
    R1C1,
    
    /**
     * The cell reference type is not defined explicitly by <code>A1</code> is the default in this case.
     */
    UNKNOWN
}
