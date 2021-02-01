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

package org.apache.poi.poifs.crypt;

public enum ChainingMode {
    // ecb - only for standard encryption
    ecb("ECB", 1, null),
    cbc("CBC", 2, "ChainingModeCBC"),
    /* Cipher feedback chaining (CFB), with an 8-bit window */
    cfb("CFB8", 3, "ChainingModeCFB");

    public final String jceId;
    public final int ecmaId;
    public final String xmlId;

    ChainingMode(String jceId, int ecmaId, String xmlId) {
        this.jceId = jceId;
        this.ecmaId = ecmaId;
        this.xmlId = xmlId;
    }

    public static ChainingMode fromXmlId(String xmlId) {
        for (ChainingMode cm : values()) {
            if (cm.xmlId != null && cm.xmlId.equals(xmlId)) {
                return cm;
            }
        }
        return null;
    }
}