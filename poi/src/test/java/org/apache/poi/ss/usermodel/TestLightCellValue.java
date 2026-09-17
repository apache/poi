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

package org.apache.poi.ss.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestLightCellValue {

    @Test
    void numberValues() {
        LightCellValue value = LightCellValue.of(2.5);
        assertEquals(CellType.NUMERIC, value.getCellType());
        assertEquals(2.5, value.getNumberValue());
        assertEquals("2.5", value.formatAsString());
        assertTrue(value.toString().endsWith("[2.5]"));
        assertNull(value.getStringValue());
        assertEquals(LightCellValue.of(2.5), value);
        assertNotEquals(LightCellValue.of(2.6), value);
        assertEquals(LightCellValue.of(2.5).hashCode(), value.hashCode());
    }

    @Test
    void textValues() {
        LightCellValue value = LightCellValue.of("abc");
        assertEquals(CellType.STRING, value.getCellType());
        assertEquals("abc", value.getStringValue());
        assertEquals("\"abc\"", value.formatAsString());
        assertNull(LightCellValue.of(null).getStringValue());
        assertEquals(0.0, value.getNumberValue());
    }

    @Test
    void booleanValuesAreCached() {
        assertSame(LightCellValue.of(true), LightCellValue.of(true));
        assertSame(LightCellValue.TRUE, LightCellValue.of(true));
        assertSame(LightCellValue.FALSE, LightCellValue.of(false));
        assertSame(LightCellValue.BooleanValue.TRUE, LightCellValue.TRUE);
        assertEquals(CellType.BOOLEAN, LightCellValue.TRUE.getCellType());
        assertEquals(Boolean.TRUE, LightCellValue.TRUE.getBooleanValue());
        assertEquals("TRUE", LightCellValue.TRUE.formatAsString());
        assertEquals("FALSE", LightCellValue.FALSE.formatAsString());
    }

    @Test
    void errorValuesAreCachedForStandardCodes() {
        int naCode = FormulaError.NA.getCode();
        assertSame(LightCellValue.error(naCode), LightCellValue.error(naCode));
        assertEquals("#N/A", LightCellValue.error(naCode).formatAsString());
        assertEquals((byte) naCode, LightCellValue.error(naCode).getErrorValue());
        assertEquals(CellType.ERROR, LightCellValue.error(naCode).getCellType());

        int bigCode = 10_000;
        assertNotEquals(LightCellValue.error(bigCode), LightCellValue.error(naCode));
        assertEquals((byte) bigCode, LightCellValue.error(bigCode).getErrorValue());
    }

    @Test
    void cellValueIsALightCellValue() {
        CellValue legacy = new CellValue(2.5);
        LightCellValue view = legacy;
        assertEquals(CellType.NUMERIC, view.getCellType());
        assertEquals(2.5, view.getNumberValue());
        assertEquals("2.5", view.formatAsString());

        CellValue text = new CellValue("abc");
        assertEquals("abc", ((LightCellValue) text).getStringValue());

        assertEquals(CellValue.TRUE.getBooleanValue(), LightCellValue.of(true).getBooleanValue());
        assertEquals(LightCellValue.FALSE.getBooleanValue(), CellValue.FALSE.getBooleanValue());
    }

    @Test
    void crossTypeReadsMatchCellValueBehavior() {
        LightCellValue number = LightCellValue.of(1.0);
        CellValue legacyNumber = new CellValue(1.0);
        assertEquals(legacyNumber.getStringValue(), number.getStringValue());
        assertEquals(legacyNumber.getBooleanValue(), number.getBooleanValue());
        assertEquals(legacyNumber.getErrorValue(), number.getErrorValue());

        LightCellValue bool = LightCellValue.of(true);
        CellValue legacyBool = CellValue.valueOf(true);
        assertEquals(legacyBool.getNumberValue(), bool.getNumberValue());
        assertEquals(legacyBool.getStringValue(), bool.getStringValue());
    }

    @Test
    void instanceOfDispatchOverSealedHierarchy() {
        assertEquals(4.0, twice(LightCellValue.of(2.0)));
        assertEquals(4.0, twice(new CellValue(2.0)));
        assertEquals(0.0, twice(LightCellValue.FALSE));
    }

    private double twice(LightCellValue value) {
        if (value instanceof LightCellValue.NumberValue numberValue) {
            return numberValue.getNumberValue() * 2;
        }
        if (value instanceof LightCellValue.TextValue textValue) {
            return Double.parseDouble(textValue.getStringValue()) * 2;
        }
        if (value instanceof LightCellValue.BooleanValue booleanValue) {
            return booleanValue.getBooleanValue() ? 2.0 : 0.0;
        }
        if (value instanceof LightCellValue.ErrorValue errorValue) {
            return errorValue.getErrorValue() * 2;
        }
        return value.getNumberValue() * 2;
    }
}
