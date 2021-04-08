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

package org.apache.poi.xwpf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

// aim is to get these classes loaded and included in poi-ooxml-lite.jar
class TestNecessaryOOXMLClasses {

    @Test
    void testProblemClasses() {
        CTTblLayoutType ctTblLayoutType = CTTblLayoutType.Factory.newInstance();
        assertNotNull(ctTblLayoutType);
        STTblLayoutType stTblLayoutType = STTblLayoutType.Factory.newInstance();
        assertNotNull(stTblLayoutType);
        CTEm ctEm = CTEm.Factory.newInstance();
        assertNotNull(ctEm);
        STEm stEm = STEm.Factory.newInstance();
        assertNotNull(stEm);
        assertEquals(STEm.CIRCLE, STEm.Enum.forString("circle"));
        STHexColorAuto stHexColorAuto = STHexColorAuto.Factory.newInstance();
        assertNotNull(stHexColorAuto);
    }
}
