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

package org.apache.poi.hwpf.model;

import static org.apache.poi.POITestCase.assertReflectEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.poi.hwpf.HWPFDocFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class TestFileInformationBlock {
    private FileInformationBlock _fileInformationBlock;
    private HWPFDocFixture _hWPFDocFixture;

    @Test
    void testReadWrite() throws Exception {
        final FibBase expected = _fileInformationBlock.getFibBase();
        int size = _fileInformationBlock.getSize();
        byte[] buf = new byte[size];
        expected.serialize(buf, 0);

        FileInformationBlock newFileInformationBlock = new FileInformationBlock(buf);
        FibBase actual = newFileInformationBlock.getFibBase();

        assertReflectEquals(expected, actual);
        assertNotNull(_fileInformationBlock.toString());
    }

    @BeforeEach
    void setUp() throws Exception {
        /** @todo verify the constructors */
        _hWPFDocFixture = new HWPFDocFixture(this, HWPFDocFixture.DEFAULT_TEST_FILE);
        _hWPFDocFixture.setUp();
        _fileInformationBlock = _hWPFDocFixture._fib;
    }

    @AfterEach
    void tearDown() throws Exception {
        _fileInformationBlock = null;
        _hWPFDocFixture.tearDown();
        _hWPFDocFixture = null;
    }
}
