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
package org.apache.poi.hssf.record.formula;

/**
 * Common interface for AreaPtg and Area3DPtg, and their
 *  child classes.
 */
public interface AreaI {
    /**
     * @return the first row in the area
     */
    public short getFirstRow();

    /**
     * @return last row in the range (x2 in x1,y1-x2,y2)
     */
    public short getLastRow();
    
    /**
     * @return the first column number in the area.
     */
    public short getFirstColumn();
    
    /**
     * @return lastcolumn in the area
     */
    public short getLastColumn();
    
    /**
     * @return isrelative first column to relative or not
     */
    public boolean isFirstColRelative();
    /**
     * @return lastcol relative or not
     */
    public boolean isLastColRelative();
    /**
     * @return whether or not the first row is a relative reference or not.
     */
    public boolean isFirstRowRelative();
    /**
     * @return last row relative or not
     */
    public boolean isLastRowRelative();
}