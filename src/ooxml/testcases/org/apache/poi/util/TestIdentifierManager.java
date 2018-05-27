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
package org.apache.poi.util;

import junit.framework.TestCase;
import org.apache.poi.ooxml.util.IdentifierManager;

public class TestIdentifierManager extends TestCase
{
    public void testBasic()
    {
        IdentifierManager manager = new IdentifierManager(0L,100L);
        assertEquals(101L,manager.getRemainingIdentifiers());
        assertEquals(0L,manager.reserveNew());
        assertEquals(100L,manager.getRemainingIdentifiers());
        assertEquals(1L,manager.reserve(0L));
        assertEquals(99L,manager.getRemainingIdentifiers());
    }

    public void testLongLimits()
    {
        long min = IdentifierManager.MIN_ID;
        long max = IdentifierManager.MAX_ID;
        IdentifierManager manager = new IdentifierManager(min,max);
        assertTrue("Limits lead to a long variable overflow", max - min + 1 > 0);
        assertTrue("Limits lead to a long variable overflow", manager.getRemainingIdentifiers() > 0);
        assertEquals(min,manager.reserveNew());
        assertEquals(max,manager.reserve(max));
        assertEquals(max - min -1, manager.getRemainingIdentifiers());
        manager.release(max);
        manager.release(min);
    }
    
    public void testReserve()
    {
        IdentifierManager manager = new IdentifierManager(10L,30L);
        assertEquals(12L,manager.reserve(12L));
        long reserve = manager.reserve(12L);
        assertFalse("Same id must be reserved twice!",reserve == 12L);
        assertTrue(manager.release(12L));
        assertTrue(manager.release(reserve));
        assertFalse(manager.release(12L));
        assertFalse(manager.release(reserve));
        
        manager = new IdentifierManager(0L,2L);
        assertEquals(0L,manager.reserve(0L));
        assertEquals(1L,manager.reserve(1L));
        assertEquals(2L,manager.reserve(2L));
        try
        {
            manager.reserve(0L);
            fail("Exception expected");
        }
        catch(IllegalStateException e)
        {
            // expected
        }
        try
        {
            manager.reserve(1L);
            fail("Exception expected");
        }
        catch(IllegalStateException e)
        {
            // expected
        }
        try
        {
            manager.reserve(2L);
            fail("Exception expected");
        }
        catch(IllegalStateException e)
        {
            // expected
        }
    }

    public void testReserveNew()
    {
        IdentifierManager manager = new IdentifierManager(10L,12L);
        assertSame(10L,manager.reserveNew());
        assertSame(11L,manager.reserveNew());
        assertSame(12L,manager.reserveNew());
        try {
            manager.reserveNew();
            fail("IllegalStateException expected");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }
    
    public void testRelease() {
        IdentifierManager manager = new IdentifierManager(10L,20L);
        assertEquals(10L,manager.reserve(10L));
        assertEquals(11L,manager.reserve(11L));
        assertEquals(12L,manager.reserve(12L));
        assertEquals(13L,manager.reserve(13L));
        assertEquals(14L,manager.reserve(14L));
        
        assertTrue(manager.release(10L));
        assertEquals(10L,manager.reserve(10L));
        assertTrue(manager.release(10L));
        
        assertTrue(manager.release(11L));
        assertEquals(11L,manager.reserve(11L));
        assertTrue(manager.release(11L));
        assertFalse(manager.release(11L));
        assertFalse(manager.release(10L));

        assertEquals(10L,manager.reserve(10L));
        assertEquals(11L,manager.reserve(11L));
        assertTrue(manager.release(12L));
    }
}
