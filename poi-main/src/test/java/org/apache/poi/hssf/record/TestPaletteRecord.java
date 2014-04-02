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

import java.util.Iterator;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * Verifies that custom palette editing works correctly
 *
 * @author Brian Sanders (bsanders at risklabs dot com)
 */
public final class TestPaletteRecord extends TestCase {

    /**
     * Tests that the default palette matches the constants of HSSFColor
     */
    public void testDefaultPalette() {
        PaletteRecord palette = new PaletteRecord();

        //make sure all the HSSFColor constants match
        Map colors = HSSFColor.getIndexHash();
        Iterator indexes = colors.keySet().iterator();
        while (indexes.hasNext())
        {
            Integer index = (Integer) indexes.next();
            HSSFColor c = (HSSFColor) colors.get(index);
            short[] rgbTriplet = c.getTriplet();
            byte[] paletteTriplet = palette.getColor(index.shortValue());
            String msg = "Expected HSSFColor constant to match PaletteRecord at index 0x"
                + Integer.toHexString(c.getIndex());
            assertEquals(msg, rgbTriplet[0], paletteTriplet[0] & 0xff);
            assertEquals(msg, rgbTriplet[1], paletteTriplet[1] & 0xff);
            assertEquals(msg, rgbTriplet[2], paletteTriplet[2] & 0xff);
        }
    }
}
