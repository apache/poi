
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
 * Class to test DocumentDescriptor functionality
 *
 * @author Marc Johnson
 */

public class TestDocumentDescriptor
    extends TestCase
{

    /**
     * Constructor TestDocumentDescriptor
     *
     * @param name
     */

    public TestDocumentDescriptor(String name)
    {
        super(name);
    }

    /**
     * test equality
     */

    public void testEquality()
    {
        String[]            names =
        {
            "c1", "c2", "c3", "c4", "c5"
        };
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

    /**
     * main method to run the unit tests
     *
     * @param ignored_args
     */

    public static void main(String [] ignored_args)
    {
        System.out.println(
            "Testing org.apache.poi.poifs.eventfilesystem.DocumentDescriptor");
        junit.textui.TestRunner.run(TestDocumentDescriptor.class);
    }
}
