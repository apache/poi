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

import org.junit.Assert;
import org.junit.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTEm;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblLayoutType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STEm;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;

// aim is to get these classes loaded and included in poi-ooxml-schemas.jar
public class TestNecessaryOOXMLClasses {

    @Test
    public void testProblemClasses() {
        CTTblLayoutType ctTblLayoutType = CTTblLayoutType.Factory.newInstance();
        Assert.assertNotNull(ctTblLayoutType);
        STTblLayoutType stTblLayoutType = STTblLayoutType.Factory.newInstance();
        Assert.assertNotNull(stTblLayoutType);
        CTEm ctEm = CTEm.Factory.newInstance();
        Assert.assertNotNull(ctEm);
        STEm stEm = STEm.Factory.newInstance();
        Assert.assertNotNull(stEm);
        Assert.assertEquals(STEm.CIRCLE, STEm.Enum.forString("circle"));
    }
}
