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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionUtilTest {
    @Test
    void testRuntimeException() {
        assertFalse(ExceptionUtil.isFatal(new RuntimeException("runtime issue")));
    }

    @Test
    void testPlainException() {
        assertFalse(ExceptionUtil.isFatal(new Exception("plain issue")));
    }

    @Test
    void testOutOfMemoryError() {
        assertTrue(ExceptionUtil.isFatal(new OutOfMemoryError("oom")));
    }

    @Test
    void testVirtualMachineError() {
        assertTrue(ExceptionUtil.isFatal(new VirtualMachineError(){}));
    }


    @Test
    void testThreadDeath() {
        assertTrue(ExceptionUtil.isFatal(new ThreadDeath()));
    }

    @Test
    void testInterruptedException() {
        assertTrue(ExceptionUtil.isFatal(new InterruptedException()));
    }

    @Test
    void testLinkageError() {
        assertTrue(ExceptionUtil.isFatal(new LinkageError()));
    }
}
