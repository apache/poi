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

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

import java.util.Properties;

/**
 * Tests the implementation of the FontDetails class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestFontDetails extends TestCase {
    private Properties properties;
    private FontDetails fontDetails;

    protected void setUp() {
        properties = new Properties();
        properties.setProperty("font.Arial.height", "13");
        properties.setProperty("font.Arial.characters", "a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, ");
        properties.setProperty("font.Arial.widths",     "6, 6, 6, 6, 6, 3, 6, 6, 3, 4, 6, 3, 9, 6, 6, 6, 6, 4, 6, 3, 6, 7, 9, 6, 5, 5, 7, 7, 7, 7, 7, 6, 8, 7, 3, 6, 7, 6, 9, 7, 8, 7, 8, 7, 7, 5, 7, 7, 9, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, ");
        fontDetails = FontDetails.create("Arial", properties);
    }

    public void testCreate() {
        assertEquals(13, fontDetails.getHeight());
        assertEquals(6, fontDetails.getCharWidth('a'));
        assertEquals(3, fontDetails.getCharWidth('f'));
    }

    public void testGetStringWidth() {
        assertEquals(9, fontDetails.getStringWidth("af"));
    }

    public void testGetCharWidth() {
        assertEquals(6, fontDetails.getCharWidth('a'));
        assertEquals(9, fontDetails.getCharWidth('='));
    }
}
