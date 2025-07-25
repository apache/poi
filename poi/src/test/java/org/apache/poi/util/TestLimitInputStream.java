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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestLimitInputStream {
    @Test
    void testLimitInputStream() throws IOException {
        String text = "test1234567890";
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        try (LimitInputStream lis = new LimitInputStream(bis, 1024)) {
            assertEquals(text, new String(IOUtils.toByteArray(lis), StandardCharsets.UTF_8));
        }
    }

    @Test
    void testLimitReached() throws IOException {
        String text = "test1234567890";
        ByteArrayInputStream bis = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        try (LimitInputStream lis = new LimitInputStream(bis, 5)) {
            assertThrows(IOException.class, () -> IOUtils.toByteArray(lis));
        }
    }
}
