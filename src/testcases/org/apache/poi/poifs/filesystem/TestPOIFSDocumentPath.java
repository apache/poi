
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.poifs.filesystem;

import junit.framework.*;

/**
 * Class to test POIFSDocumentPath functionality
 *
 * @author Marc Johnson
 */

public class TestPOIFSDocumentPath
    extends TestCase
{

    /**
     * Constructor TestPOIFSDocumentPath
     *
     * @param name
     */

    public TestPOIFSDocumentPath(String name)
    {
        super(name);
    }

    /**
     * Test default constructor
     */

    public void testDefaultConstructor()
    {
        POIFSDocumentPath path = new POIFSDocumentPath();

        assertEquals(0, path.length());
    }

    /**
     * Test full path constructor
     */

    public void testFullPathConstructor()
    {
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

    public void testRelativePathConstructor()
    {
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

    public void testEquality()
    {
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

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println(
            "Testing org.apache.poi.poifs.eventfilesystem.POIFSDocumentPath");
        junit.textui.TestRunner.run(TestPOIFSDocumentPath.class);
    }
}
