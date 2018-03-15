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

import junit.framework.TestCase;
import org.apache.poi.hssf.HSSFTestDataSamples;

import java.io.IOException;
import java.util.List;

/**
 * Tests for the embedded object fetching support in HSSF
 */
public class TestEmbeddedObjects extends TestCase{
    public void testReadExistingObject() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("drawings.xls");
        List<HSSFObjectData> list = wb.getAllEmbeddedObjects();
        assertEquals(list.size(), 1);
        HSSFObjectData obj = list.get(0);
        assertNotNull(obj.getObjectData());
        assertNotNull(obj.getDirectory());
        assertNotNull(obj.getOLE2ClassName());
    }
    
    /**
     * Need to recurse into the shapes to find this one
     * See https://github.com/apache/poi/pull/2
     */
    public void testReadNestedObject() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("WithCheckBoxes.xls");
        List<HSSFObjectData> list = wb.getAllEmbeddedObjects();
        assertEquals(list.size(), 1);
        HSSFObjectData obj = list.get(0);
        assertNotNull(obj.getObjectData());
        assertNotNull(obj.getOLE2ClassName());
    }
    
    /**
     * One with large numbers of recursivly embedded resources
     * See https://github.com/apache/poi/pull/2
     */
    public void testReadManyNestedObjects() throws IOException {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("45538_form_Header.xls");
        List<HSSFObjectData> list = wb.getAllEmbeddedObjects();
        assertEquals(list.size(), 40);
    }
}
