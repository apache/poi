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

package org.apache.poi.xssf.util;

import java.util.Comparator;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;

public class CTColComparator {

    private CTColComparator() {}

    public static final Comparator<CTCol> BY_MAX = (col1, col2) -> {
        long col1max = col1.getMax();
        long col2max = col2.getMax();
        return Long.compare(col1max, col2max);
    };

    public static final Comparator<CTCol> BY_MIN_MAX = (col1, col2) -> {
        long col11min = col1.getMin();
        long col2min = col2.getMin();
        return col11min < col2min ? -1 : col11min > col2min ? 1 : BY_MAX.compare(col1, col2);
    };

}
