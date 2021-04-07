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

/**
 * Sets height rule values allowed for Table Rows
 */
public enum TableRowHeightRule {
    
    AUTO(1),
    EXACT(2),
    AT_LEAST(3);

    private static Map<Integer, TableRowHeightRule> imap = new HashMap<>();

    static {
        for (TableRowHeightRule p : values()) {
            imap.put(p.getValue(), p);
        }
    }

    private final int value;

    private TableRowHeightRule(int val) {
        value = val;
    }

    public static TableRowHeightRule valueOf(int type) {
        TableRowHeightRule err = imap.get(type);
        if (err == null) throw new IllegalArgumentException("Unknown table row height rule: " + type);
        return err;
    }

    public int getValue() {
        return value;
    }
}
