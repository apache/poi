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

package org.apache.poi.hssf.util;

/**
 * Common conversion functions between Excel style A1, C27 style
 *  cell references, and POI usermodel style row=0, column=0
 *  style references.
 * @author  Avik Sengupta
 * @author  Dennis Doubleday (patch to seperateRowColumns())
 */
public final class CellReference extends org.apache.poi.ss.util.CellReference {
    /**
     * Create an cell ref from a string representation.  Sheet names containing special characters should be
     * delimited and escaped as per normal syntax rules for formulas.
     */
    public CellReference(String cellRef) {
    	super(cellRef);
    }

    public CellReference(int pRow, int pCol) {
    	super(pRow, pCol, true, true);
    }
    
    public CellReference(int pRow, int pCol, boolean pAbsRow, boolean pAbsCol) {
        super(null, pRow, pCol, pAbsRow, pAbsCol);
    }

    public CellReference(String pSheetName, int pRow, int pCol, boolean pAbsRow, boolean pAbsCol) {
    	super(pSheetName, pRow, pCol, pAbsRow, pAbsCol);
    }
}
