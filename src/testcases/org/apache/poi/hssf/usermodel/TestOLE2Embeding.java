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

package org.apache.poi.hssf.usermodel;

import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;

/**
 * 
 */
public final class TestOLE2Embeding extends TestCase {

    public void testEmbeding() {
        // This used to break, until bug #43116 was fixed
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("ole2-embedding.xls");

        // Check we can get at the Escher layer still
        workbook.getAllPictures();
    }

    public void testEmbeddedObjects() throws Exception {
        HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("ole2-embedding.xls");

        List objects = workbook.getAllEmbeddedObjects();
        assertEquals("Wrong number of objects", 2, objects.size());
        assertEquals("Wrong name for first object", "MBD06CAB431",
                ((HSSFObjectData)
                objects.get(0)).getDirectory().getName());
        assertEquals("Wrong name for second object", "MBD06CAC85A",
                ((HSSFObjectData)
                objects.get(1)).getDirectory().getName());
    }
}

