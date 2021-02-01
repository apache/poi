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

package org.apache.poi.hssf.record;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.util.HSSFColor;
import org.junit.jupiter.api.Test;

/**
 * Verifies that custom palette editing works correctly
 */
final class TestPaletteRecord {

    /**
     * Tests that the default palette matches the constants of HSSFColor
     */
    @Test
    void testDefaultPalette() {
        PaletteRecord palette = new PaletteRecord();

        //make sure all the HSSFColor constants match
        Map<Integer, HSSFColor> colors = HSSFColor.getIndexHash();
        for (Entry<Integer, HSSFColor> entry : colors.entrySet()) {
            int index = entry.getKey();
            HSSFColor c = entry.getValue();
            short[] rgbTriplet = c.getTriplet();
            byte[] paletteTriplet = palette.getColor((short) index);
            String msg = "Expected HSSFColor constant to match PaletteRecord at index" + (index == c.getIndex2() ? "2" : "") + " 0x"
                + Integer.toHexString(index);
            assertNotNull(paletteTriplet);
            assertEquals(rgbTriplet[0], paletteTriplet[0] & 0xff, msg);
            assertEquals(rgbTriplet[1], paletteTriplet[1] & 0xff, msg);
            assertEquals(rgbTriplet[2], paletteTriplet[2] & 0xff, msg);
        }
    }
}
