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

package org.apache.poi.xwpf.usermodel;

import java.util.HashMap;
import java.util.Map;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;

/**
 * Specifies all types of alignment which are available to be applied to objects in a
 * WordprocessingML document
 *
 * @author Yegor Kozlov
 */
public enum ParagraphAlignment {
    //YK: TODO document each alignment option

    START(STJc.INT_START), // 1
    CENTER(STJc.INT_CENTER), // 2
    END(STJc.INT_END), // 3
    BOTH(STJc.INT_BOTH), // 4
    MEDIUM_KASHIDA(STJc.INT_MEDIUM_KASHIDA), // 5
    DISTRIBUTE(STJc.INT_DISTRIBUTE), // 6
    NUM_TAB(STJc.INT_NUM_TAB), // 7
    HIGH_KASHIDA(STJc.INT_HIGH_KASHIDA), // 8
    LOW_KASHIDA(STJc.INT_LOW_KASHIDA), // 9
    THAI_DISTRIBUTE(STJc.INT_THAI_DISTRIBUTE), // 10
    LEFT(STJc.INT_LEFT), // 11
    RIGHT(STJc.INT_RIGHT) // 12
    ;

    private static final Map<Integer, ParagraphAlignment> imap = new HashMap<>();

    static {
        for (ParagraphAlignment p : values()) {
            imap.put(p.getValue(), p);
        }
    }

    private final int value;

    private ParagraphAlignment(int val) {
        value = val;
    }

    public static ParagraphAlignment valueOf(int type) {
        ParagraphAlignment err = imap.get(type);
        if (err == null) throw new IllegalArgumentException("Unknown paragraph alignment: " + type);
        return err;
    }

    public int getValue() {
        return value;
    }

}
