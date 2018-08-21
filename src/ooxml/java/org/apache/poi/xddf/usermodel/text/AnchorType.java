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

package org.apache.poi.xddf.usermodel.text;

import java.util.HashMap;

import org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType;

public enum AnchorType {
    BOTTOM(STTextAnchoringType.B),
    CENTER(STTextAnchoringType.CTR),
    DISTRIBUTED(STTextAnchoringType.DIST),
    JUSTIFIED(STTextAnchoringType.JUST),
    TOP(STTextAnchoringType.T);

    final STTextAnchoringType.Enum underlying;

    AnchorType(STTextAnchoringType.Enum caps) {
        this.underlying = caps;
    }

    private final static HashMap<STTextAnchoringType.Enum, AnchorType> reverse = new HashMap<STTextAnchoringType.Enum, AnchorType>();
    static {
        for (AnchorType value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static AnchorType valueOf(STTextAnchoringType.Enum caps) {
        return reverse.get(caps);
    }
}
