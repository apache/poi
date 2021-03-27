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

package org.apache.poi.poifs.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Class to test POIFSDocumentPath functionality
 */
final class TestPOIFSDocumentPath {


    /**
     * Test default constructor
     */
    @Test
    void testDefaultConstructor() {
        POIFSDocumentPath path = new POIFSDocumentPath();

        assertEquals(0, path.length());
    }

    /**
     * Test full path constructor
     */
    @Test
    void testFullPathConstructor() {
        String[] components = {"foo", "bar", "foobar", "fubar"};

        for (int j = 0; j < components.length; j++) {
            String[] params = Arrays.copyOf(components, j);
            POIFSDocumentPath path = new POIFSDocumentPath(params);

            assertEquals(j, path.length());
            for (int k = 0; k < j; k++) {
                assertEquals(components[ k ], path.getComponent(k));
            }
            if (j == 0) {
                assertNull(path.getParent());
            } else {
                POIFSDocumentPath parent = path.getParent();

                assertNotNull(parent);
                assertEquals(j - 1, parent.length());
                for (int k = 0; k < j - 1; k++) {
                    assertEquals(components[ k ], parent.getComponent(k));
                }
            }
        }

        // test weird variants
        assertEquals(0, new POIFSDocumentPath(null).length());
        assertThrows(IllegalArgumentException.class, () -> new POIFSDocumentPath(new String[]{"fu", ""}));
        assertThrows(IllegalArgumentException.class, () -> new POIFSDocumentPath(new String[]{"fu", null}));
    }

    /**
     * Test relative path constructor
     */
    @Test
    void testRelativePathConstructor() {
        String[] initialComponents = {"a", "b", "c"};

        for (int n = 0; n < initialComponents.length; n++) {
            String[] initialParams = Arrays.copyOf(initialComponents, n);
            POIFSDocumentPath base = new POIFSDocumentPath(initialParams);
            String[] components = {"foo", "bar", "foobar", "fubar"};

            for (int j = 0; j < components.length; j++) {
                String[] params = Arrays.copyOf(components, j);
                POIFSDocumentPath path = new POIFSDocumentPath(base, params);

                assertEquals(j + n, path.length());
                for (int k = 0; k < n; k++) {
                    assertEquals(initialComponents[ k ], path.getComponent(k));
                }
                for (int k = 0; k < j; k++) {
                    assertEquals(components[ k ], path.getComponent(k + n));
                }
                if ((j + n) == 0) {
                    assertNull(path.getParent());
                } else {
                    POIFSDocumentPath parent = path.getParent();

                    assertNotNull(parent);
                    assertEquals(j + n - 1, parent.length());
                    for (int k = 0; k < (j + n - 1); k++) {
                        assertEquals(path.getComponent(k), parent.getComponent(k));
                    }
                }
            }

            // Test weird variants

            // This one is allowed, even if it's really odd
            assertEquals(n, new POIFSDocumentPath(base, null).length());
            new POIFSDocumentPath(base, new String[]{"fu", ""});

            // This one is allowed too
            new POIFSDocumentPath(base, new String[]{"", "fu"});

            // This one shouldn't be allowed
            assertThrows(IllegalArgumentException.class, () -> new POIFSDocumentPath(base, new String[]{"fu", null}));

            // Ditto
            assertThrows(IllegalArgumentException.class, () -> new POIFSDocumentPath(base, new String[]{null, "fu"}));
        }
    }

    /**
     * test equality
     */
    @Test
    void testEquality() {
        POIFSDocumentPath   a1    = new POIFSDocumentPath();
        POIFSDocumentPath   a2    = new POIFSDocumentPath(null);
        POIFSDocumentPath   a3    = new POIFSDocumentPath(new String[ 0 ]);
        POIFSDocumentPath   a4    = new POIFSDocumentPath(a1, null);
        POIFSDocumentPath   a5    = new POIFSDocumentPath(a1, new String[ 0 ]);

        POIFSDocumentPath[] paths = {a1, a2, a3, a4, a5};

        for (int j = 0; j < paths.length; j++) {
            for (int k = 0; k < paths.length; k++) {
                assertEquals(paths[ j ], paths[ k ], j + "<>" + k);
            }
        }
        a2 = new POIFSDocumentPath(a1, new String[]{"foo"});
        a3 = new POIFSDocumentPath(a2, new String[]{"bar"});
        a4 = new POIFSDocumentPath(a3, new String[]{"fubar"});
        a5 = new POIFSDocumentPath(a4, new String[]{"foobar"});
        POIFSDocumentPath[] builtUpPaths =
        {
            a1, a2, a3, a4, a5
        };
        POIFSDocumentPath[] fullPaths = {
            new POIFSDocumentPath(), new POIFSDocumentPath(new String[]{"foo"}),
            new POIFSDocumentPath(new String[]{"foo", "bar"}),
            new POIFSDocumentPath(new String[]{"foo", "bar", "fubar"}),
            new POIFSDocumentPath(new String[]{"foo", "bar", "fubar", "foobar"})
        };

        for (int k = 0; k < builtUpPaths.length; k++) {
            for (int j = 0; j < fullPaths.length; j++) {
                if (k == j) {
                    assertEquals(fullPaths[ j ], builtUpPaths[ k ], j + "<>" + k);
                } else {
                    assertNotEquals(fullPaths[j], builtUpPaths[k]);
                }
            }
        }
        POIFSDocumentPath[] badPaths = {
            new POIFSDocumentPath(new String[]{"_foo"}),
            new POIFSDocumentPath(new String[]{"foo", "_bar"}),
            new POIFSDocumentPath(new String[]{"foo", "bar", "_fubar"}),
            new POIFSDocumentPath(new String[]{"foo", "bar", "fubar", "_foobar"})
        };

        for (int k = 0; k < builtUpPaths.length; k++)
        {
            for (POIFSDocumentPath badPath : badPaths) {
                assertNotEquals(fullPaths[k], badPath);
            }
        }
    }
}
