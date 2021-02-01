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
 * Data table style definition.  Includes style elements for various table components.
 * Any number of style elements may be represented, and any cell may be styled by
 * multiple elements.  The order of elements in {@link TableStyleType} defines precedence.
 * 
 * @since 3.17 beta 1
 */
public interface TableStyle {

    /**
     * @return name (may be a built-in name)
     */
    String getName();
    
    /**
     * Some clients may care where in the table style list this definition came from, so we'll track it.
     * The spec only references these by name, unlike Dxf records, which these definitions reference by index 
     * (XML definition order).  Nice of MS to be consistent when defining the ECMA standard.
     * Use org.apache.poi.xssf.usermodel.XSSFBuiltinTableStyle.isBuiltinStyle(TableStyle) to determine whether the index is for a built-in style or explicit user style
     * @return index from org.apache.poi.xssf.model.StylesTable.getExplicitTableStyle(String) or org.apache.poi.xssf.usermodel.XSSFBuiltinTableStyle.ordinal()
     */
    int getIndex();
    
    /**
     *
     * @return true if this is a built-in style defined in the OOXML specification, false if it is a user style
     */
    boolean isBuiltin();
    
    /**
     *
     * @param type
     * @return style definition for the given type, or null if not defined in this style.
     */
    DifferentialStyleProvider getStyle(TableStyleType type);
}
