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
 * Class to test DocumentDescriptor functionality
 *
 * @author Marc Johnson
 */
public final class TestDocumentDescriptor extends TestCase {

    /**
     * test equality
     */
    public void testEquality() {
        String[] names = { "c1", "c2", "c3", "c4", "c5" };
        POIFSDocumentPath   a1    = new POIFSDocumentPath();
        POIFSDocumentPath   a2    = new POIFSDocumentPath(null);
        POIFSDocumentPath   a3    = new POIFSDocumentPath(new String[ 0 ]);
        POIFSDocumentPath   a4    = new POIFSDocumentPath(a1, null);
        POIFSDocumentPath   a5    = new POIFSDocumentPath(a1,
                                        new String[ 0 ]);
        POIFSDocumentPath[] paths = { a1, a2, a3, a4, a5 };

        for (int j = 0; j < paths.length; j++)
        {
            for (int k = 0; k < paths.length; k++)
            {
                for (int m = 0; m < names.length; m++)
                {
                    DocumentDescriptor d1 = new DocumentDescriptor(paths[ j ],
                                                names[ m ]);

                    for (int n = 0; n < names.length; n++)
                    {
                        DocumentDescriptor d2 =
                            new DocumentDescriptor(paths[ k ], names[ n ]);

                        if (m == n)
                        {
                            assertEquals("" + j + "," + k + "," + m + ","
                                         + n, d1, d2);
                        }
                        else
                        {
                            assertTrue("" + j + "," + k + "," + m + "," + n,
                                       !d1.equals(d2));
                        }
                    }
                }
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
                for (int m = 0; m < names.length; m++)
                {
                    DocumentDescriptor d1 =
                        new DocumentDescriptor(fullPaths[ j ], names[ m ]);

                    for (int n = 0; n < names.length; n++)
                    {
                        DocumentDescriptor d2 =
                            new DocumentDescriptor(builtUpPaths[ k ],
                                                   names[ n ]);

                        if ((k == j) && (m == n))
                        {
                            assertEquals("" + j + "," + k + "," + m + ","
                                         + n, d1, d2);
                        }
                        else
                        {
                            assertTrue("" + j + "," + k + "," + m + "," + n,
                                       !(d1.equals(d2)));
                        }
                    }
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
                for (int m = 0; m < names.length; m++)
                {
                    DocumentDescriptor d1 =
                        new DocumentDescriptor(badPaths[ j ], names[ m ]);

                    for (int n = 0; n < names.length; n++)
                    {
                        DocumentDescriptor d2 =
                            new DocumentDescriptor(builtUpPaths[ k ],
                                                   names[ n ]);

                        assertTrue("" + j + "," + k + "," + m + "," + n,
                                   !(d1.equals(d2)));
                    }
                }
            }
        }
    }
}
