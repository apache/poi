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


public final class AreaReference extends org.apache.poi.ss.util.AreaReference {
    /**
     * Create an area ref from a string representation.  Sheet names containing special characters should be
     * delimited and escaped as per normal syntax rules for formulas.<br/> 
     * The area reference must be contiguous (i.e. represent a single rectangle, not a union of rectangles)
     */
    public AreaReference(String reference) {
		super(reference);
    }
    
    /**
     * Creates an area ref from a pair of Cell References.
     * Also normalises such that the top-left
     */
    public AreaReference(CellReference topLeft, CellReference botRight) {
		super(topLeft, botRight);
    }
}
