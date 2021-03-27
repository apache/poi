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

package org.apache.poi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedString;

import org.junit.jupiter.api.Test;

/**
 * Minimal Test-Class found when running the Apache POI regression tests.
 *
 * This reproduces a crash introduced in JDK 12-ea+28 and JDK 13-ea+4
 *
 * This works in recent JDK 8, JDK 11 and at least up to JDK 12-ea+20
 *
 * https://bugs.openjdk.java.net/browse/JDK-8217768
 *
 * Should be fixed in JDK 12-ea+29 and JDK 13-ea+5
 */
class TestJDK12 {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = img.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.scale(200, 1);

            new TextLayout(new AttributedString("agriculture").getIterator(), graphics.getFontRenderContext());

            graphics.dispose();
            img.flush();
        });
    }

}
