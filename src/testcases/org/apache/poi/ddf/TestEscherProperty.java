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

package org.apache.poi.ddf;

import junit.framework.TestCase;

import java.util.List;
import java.lang.reflect.Field;

import org.apache.poi.util.HexRead;
import org.apache.poi.util.HexDump;

/**
 * @author Yegor Kozlov
 */
public class TestEscherProperty extends TestCase
{
    /**
     * assure that EscherProperty.getName() returns correct name for complex properties
     * See Bugzilla 50401 
     */
    public void testPropertyNames() throws Exception {
        EscherProperty p1 = new EscherSimpleProperty( EscherProperties.GROUPSHAPE__SHAPENAME, 0);
        assertEquals("groupshape.shapename", p1.getName());
        assertEquals(EscherProperties.GROUPSHAPE__SHAPENAME, p1.getPropertyNumber());
        assertFalse(p1.isComplex());

        EscherProperty p2 = new EscherComplexProperty(
                EscherProperties.GROUPSHAPE__SHAPENAME, false, new byte[10]);
        assertEquals("groupshape.shapename", p2.getName());
        assertEquals(EscherProperties.GROUPSHAPE__SHAPENAME, p2.getPropertyNumber());
        assertTrue(p2.isComplex());
        assertFalse(p2.isBlipId());

        EscherProperty p3 = new EscherComplexProperty(
                EscherProperties.GROUPSHAPE__SHAPENAME, true, new byte[10]);
        assertEquals("groupshape.shapename", p3.getName());
        assertEquals(EscherProperties.GROUPSHAPE__SHAPENAME, p3.getPropertyNumber());
        assertTrue(p3.isComplex());
        assertTrue(p3.isBlipId());
    }
}