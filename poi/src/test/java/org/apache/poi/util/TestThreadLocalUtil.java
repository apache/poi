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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.sl.draw.DrawFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class TestThreadLocalUtil {
    private final MemoryLeakVerifier verifier = new MemoryLeakVerifier();

    @AfterEach
    void tearDown() {
        verifier.assertGarbageCollected();
    }

    @Test
    public void testClearThreadLocalsNoData() {
        // simply calling it without any thread locals should work
        ThreadLocalUtil.clearAllThreadLocals();
    }

    @Test
    public void testClearThreadLocalsWithData() {
        DrawFactory factory = new DrawFactory();

        // use the memory leak verifier to ensure that the thread-local is
        // released after the clear-call below
        verifier.addObject(factory);

        // store the object in a thread-local
        DrawFactory.setDefaultFactory(factory);

        // retrieving it works now
        assertEquals(factory, DrawFactory.getInstance(null));

        // then clear them so that the verifier in tearDown() does not
        // see the reference any longer
        ThreadLocalUtil.clearAllThreadLocals();
    }
}