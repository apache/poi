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

package org.apache.poi.hwpf;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class HWPFTestCase {
    protected HWPFDocFixture _hWPFDocFixture;

    @BeforeEach
    void setUp() throws Exception {
        // @TODO verify the constructors
        _hWPFDocFixture = new HWPFDocFixture(this, getTestFile());

        _hWPFDocFixture.setUp();
    }

    protected String getTestFile() {
        return HWPFDocFixture.DEFAULT_TEST_FILE;
    }

    @AfterEach
    void tearDown() {
        if (_hWPFDocFixture != null) {
            _hWPFDocFixture.tearDown();
        }

        _hWPFDocFixture = null;
    }

    public HWPFDocument writeOutAndRead(HWPFDocument doc) {
        try (UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get()) {
            doc.write(baos);
            try (InputStream is = baos.toInputStream()) {
                return new HWPFDocument(is);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
