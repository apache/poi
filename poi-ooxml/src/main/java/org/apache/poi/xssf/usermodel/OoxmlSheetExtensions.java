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

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Methods exposed by XSSF, SXSSF and related sheets - that are additional to the ones on the {@link Sheet} interface.
 *
 * @since POI 5.2.0
 */
public interface OoxmlSheetExtensions {
    /**
     * Get VML drawing for this sheet (aka 'legacy' drawing).
     *
     * @param autoCreate if true, then a new VML drawing part is created
     *
     * @return the VML drawing or {@code null} if the drawing was not found and autoCreate=false
     *         (or this method is not implemented)
     */
    XSSFVMLDrawing getVMLDrawing(boolean autoCreate);
}
