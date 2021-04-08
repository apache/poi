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
 * style information for a specific table instance, referencing the document style
 * and indicating which optional portions of the style to apply.
 * 
 * @since 3.17 beta 1
 */
public interface TableStyleInfo {

    /**
     * @return true if alternating column styles should be applied
     */
    boolean isShowColumnStripes();
    
    /**
     * @return true if alternating row styles should be applied
     */
    boolean isShowRowStripes();
    
    /**
     * @return true if the distinct first column style should be applied
     */
    boolean isShowFirstColumn();
    
    /**
     * @return true if the distinct last column style should be applied
     */
    boolean isShowLastColumn();
    
    /**
     * @return the name of the style (may reference a built-in style)
     */
    String getName();
    
    /**
     * @return style definition
     */
    TableStyle getStyle();
}
