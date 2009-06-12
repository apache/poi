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

import junit.framework.TestCase;

/**
 * Class to test POIFSDocumentPath functionality
 *
 * @author Marc Johnson
 */
public final class TestPOIFSDocumentPath extends TestCase {


    /**
     * Test default constructor
     */
    public void testDefaultConstructor() {
        POIFSDocumentPath path = new POIFSDocumentPath();

        assertEquals(0, path.length());
    }

    /**
     * Test full path constructor
     */
    public void testFullPathConstructor() {
        String[] components =
        {
            "foo", "bar", "foobar", "fubar"
        };

        for (int j = 0; j < components.length; j++)
        {
            String[] params = new String[ j ];

            for (int k = 0; k < j; k++)
            {
                params[ k ] = components[ k ];
            }
            POIFSDocumentPath path = new POIFSDocumentPath(params);

            assertEquals(j, path.length());
            for (int k = 0; k < j; k++)
            {
                assertEquals(components[ k ], path.getComponent(k));
            }
            if (j == 0)
            {
                assertNull(path.getParent());
            }
            else
            {
                POIFSDocumentPath parent = path.getParent();

                assertNotNull(parent);
                assertEquals(j - 1, parent.length());
                for (int k = 0; k < j - 1; k++)
                {
                    assertEquals(components[ k ], parent.getComponent(k));
                }
            }
        }

        // test weird variants
        assertEquals(0, new POIFSDocumentPath(null).length());
        try
        {
            new POIFSDocumentPath(new String[]
            {
                "fu", ""
            });
            fail("should have caught IllegalArgumentException");
        }
        catch (IllegalArgumentException ignored)
        {
        }
        try
        {
            new POIFSDocumentPath(new String[]
            {
                "fu", null
            });
            fail("should have caught IllegalArgumentException");
        }
        catch (IllegalArgumentException ignored)
        {
        }
    }

    /**
     * Test relative path constructor
     */
    public void testRelativePathConstructor() {
        String[] initialComponents =
        {
            "a", "b", "c"
        };

        for (int n = 0; n < initialComponents.length; n++)
        {
            String[] initialParams = new String[ n ];

            for (int k = 0; k < n; k++)
            {
                initialParams[ k ] = initialComponents[ k ];
            }
            POIFSDocumentPath base       =
                new POIFSDocumentPath(initialParams);
            String[]          components =
            {
                "foo", "bar", "foobar", "fubar"
            };

            for (int j = 0; j < components.length; j++)
            {
                String[] params = new String[ j ];

                for (int k = 0; k < j; k++)
                {
                    params[ k ] = components[ k ];
                }
                POIFSDocumentPath path = new POIFSDocumentPath(base, params);

                assertEquals(j + n, path.length());
                for (int k = 0; k < n; k++)
                {
                    assertEquals(initialComponents[ k ],
                                 path.getComponent(k));
                }
                for (int k = 0; k < j; k++)
                {
                    assertEquals(components[ k ], path.getComponent(k + n));
                }
                if ((j + n) == 0)
                {
                    assertNull(path.getParent());
                }
                else
                {
                    POIFSDocumentPath parent = path.getParent();

                    assertNotNull(parent);
                    assertEquals(j + n - 1, parent.length());
                    for (int k = 0; k < (j + n - 1); k++)
                    {
                        assertEquals(path.getComponent(k),
                                     parent.getComponent(k));
                    }
                }
            }

            // test weird variants
            assertEquals(n, new POIFSDocumentPath(base, null).length());
            try
            {
                new POIFSDocumentPath(base, new String[]
                {
                    "fu", ""
                });
                fail("should have caught IllegalArgumentException");
            }
            catch (IllegalArgumentException ignored)
            {
            }
            try
            {
                new POIFSDocumentPath(base, new String[]
                {
                    "fu", null
                });
                fail("should have caught IllegalArgumentException");
            }
            catch (IllegalArgumentException ignored)
            {
            }
        }
    }

    /**
     * test equality
     */
    public void testEquality() {
        POIFSDocumentPath   a1    = new POIFSDocumentPath();
        POIFSDocumentPath   a2    = new POIFSDocumentPath(null);
        POIFSDocumentPath   a3    = new POIFSDocumentPath(new String[ 0 ]);
        POIFSDocumentPath   a4    = new POIFSDocumentPath(a1, null);
        POIFSDocumentPath   a5    = new POIFSDocumentPath(a1,
                                        new String[ 0 ]);
        POIFSDocumentPath[] paths =
        {
            a1, a2, a3, a4, a5
        };

        for (int j = 0; j < paths.length; j++)
        {
            for (int k = 0; k < paths.length; k++)
            {
                assertEquals(String.valueOf(j) + "<>" + String.valueOf(k),
                             paths[ j ], paths[ k ]);
            }
        }
        a2 = new POIFSDocumentPath(a1, new String[]
        {
            "foo"
        });
        a3 = new POIFSDocumentPath(a2, new String[]
        {
            "bar"
        });
        a4 = new POIFSDocumentPath(a3, new String[]
        {
            "fubar"
        });
        a5 = new POIFSDocumentPath(a4, new String[]
        {
            "foobar"
        });
        POIFSDocumentPath[] builtUpPaths =
        {
            a1, a2, a3, a4, a5
        };
        POIFSDocumentPath[] fullPaths    =
        {
            new POIFSDocumentPath(), new POIFSDocumentPath(new String[]
            {
                "foo"
            }), new POIFSDocumentPath(new String[]
            {
                "foo", "bar"
            }), new POIFSDocumentPath(new String[]
            {
                "foo", "bar", "fubar"
            }), new POIFSDocumentPath(new String[]
            {
                "foo", "bar", "fubar", "foobar"
            })
        };

        for (int k = 0; k < builtUpPaths.length; k++)
        {
            for (int j = 0; j < fullPaths.length; j++)
            {
                if (k == j)
                {
                    assertEquals(String.valueOf(j) + "<>"
                                 + String.valueOf(k), fullPaths[ j ],
                                                      builtUpPaths[ k ]);
                }
                else
                {
                    assertTrue(String.valueOf(j) + "<>" + String.valueOf(k),
                               !(fullPaths[ j ].equals(builtUpPaths[ k ])));
                }
            }
        }
        POIFSDocumentPath[] badPaths =
        {
            new POIFSDocumentPath(new String[]
            {
                "_foo"
            }), new POIFSDocumentPath(new String[]
            {
                "foo", "_bar"
            }), new POIFSDocumentPath(new String[]
            {
                "foo", "bar", "_fubar"
            }), new POIFSDocumentPath(new String[]
            {
                "foo", "bar", "fubar", "_foobar"
            })
        };

        for (int k = 0; k < builtUpPaths.length; k++)
        {
            for (int j = 0; j < badPaths.length; j++)
            {
                assertTrue(String.valueOf(j) + "<>" + String.valueOf(k),
                           !(fullPaths[ k ].equals(badPaths[ j ])));
            }
        }
    }
}
