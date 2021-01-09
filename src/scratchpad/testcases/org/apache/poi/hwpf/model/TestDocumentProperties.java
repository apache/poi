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

import org.apache.poi.hwpf.HWPFDocFixture;
import org.apache.poi.hwpf.model.types.DOPAbstractType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// TODO: Add DocumentProperties#equals ???

public final class TestDocumentProperties {
    private DocumentProperties _documentProperties;
    private HWPFDocFixture _hWPFDocFixture;

    @BeforeEach
    void setUp() throws Exception {
        // TODO verify the constructors
        _hWPFDocFixture = new HWPFDocFixture(this, HWPFDocFixture.DEFAULT_TEST_FILE);
        _hWPFDocFixture.setUp();
        _documentProperties = new DocumentProperties(_hWPFDocFixture._tableStream, _hWPFDocFixture._fib.getFcDop(), _hWPFDocFixture._fib.getLcbDop());
    }

    @AfterEach
    void tearDown() {
        _documentProperties = null;
        _hWPFDocFixture.tearDown();
        _hWPFDocFixture = null;
    }

    @Test
    void testReadWrite() throws Exception  {
        int size = DOPAbstractType.getSize();
        byte[] buf = new byte[size];
        _documentProperties.serialize(buf, 0);
        DocumentProperties newDocProperties = new DocumentProperties(buf, 0, size);

        assertReflectEquals(_documentProperties, newDocProperties);
    }
}
