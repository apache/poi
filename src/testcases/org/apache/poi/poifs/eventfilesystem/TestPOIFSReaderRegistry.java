
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

package org.apache.poi.poifs.eventfilesystem;

import junit.framework.*;

import java.util.*;

import org.apache.poi.poifs.filesystem.POIFSDocumentPath;

/**
 * Class to test POIFSReaderRegistry functionality
 *
 * @author Marc Johnson
 */

public class TestPOIFSReaderRegistry
    extends TestCase
{
    private POIFSReaderListener[] listeners =
    {
        new Listener(), new Listener(), new Listener(), new Listener()
    };
    private POIFSDocumentPath[]   paths     =
    {
        new POIFSDocumentPath(), new POIFSDocumentPath(new String[]
        {
            "a"
        }), new POIFSDocumentPath(new String[]
        {
            "b"
        }), new POIFSDocumentPath(new String[]
        {
            "c"
        })
    };
    private String[]              names     =
    {
        "a0", "a1", "a2", "a3"
    };

    /**
     * Constructor TestPOIFSReaderRegistry
     *
     * @param name
     */

    public TestPOIFSReaderRegistry(String name)
    {
        super(name);
    }

    /**
     * Test empty registry
     */

    public void testEmptyRegistry()
    {
        POIFSReaderRegistry registry = new POIFSReaderRegistry();

        for (int j = 0; j < paths.length; j++)
        {
            for (int k = 0; k < names.length; k++)
            {
                Iterator listeners = registry.getListeners(paths[ j ],
                                                           names[ k ]);

                assertTrue(!listeners.hasNext());
            }
        }
    }

    /**
     * Test mixed registration operations
     */

    public void testMixedRegistrationOperations()
    {
        POIFSReaderRegistry registry = new POIFSReaderRegistry();

        for (int j = 0; j < listeners.length; j++)
        {
            for (int k = 0; k < paths.length; k++)
            {
                for (int n = 0; n < names.length; n++)
                {
                    if ((j != k) && (k != n))
                    {
                        registry.registerListener(listeners[ j ], paths[ k ],
                                                  names[ n ]);
                    }
                }
            }
        }
        for (int k = 0; k < paths.length; k++)
        {
            for (int n = 0; n < names.length; n++)
            {
                Iterator listeners = registry.getListeners(paths[ k ],
                                                           names[ n ]);

                if (k == n)
                {
                    assertTrue(!listeners.hasNext());
                }
                else
                {
                    Set registeredListeners = new HashSet();

                    while (listeners.hasNext())
                    {
                        registeredListeners.add(listeners.next());
                    }
                    assertEquals(this.listeners.length - 1,
                                 registeredListeners.size());
                    for (int j = 0; j < this.listeners.length; j++)
                    {
                        if (j == k)
                        {
                            assertTrue(!registeredListeners
                                .contains(this.listeners[ j ]));
                        }
                        else
                        {
                            assertTrue(registeredListeners
                                .contains(this.listeners[ j ]));
                        }
                    }
                }
            }
        }
        for (int j = 0; j < listeners.length; j++)
        {
            registry.registerListener(listeners[ j ]);
        }
        for (int k = 0; k < paths.length; k++)
        {
            for (int n = 0; n < names.length; n++)
            {
                Iterator listeners           =
                    registry.getListeners(paths[ k ], names[ n ]);
                Set      registeredListeners = new HashSet();

                while (listeners.hasNext())
                {
                    registeredListeners.add(listeners.next());
                }
                assertEquals(this.listeners.length,
                             registeredListeners.size());
                for (int j = 0; j < this.listeners.length; j++)
                {
                    assertTrue(registeredListeners
                        .contains(this.listeners[ j ]));
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
            "Testing org.apache.poi.poifs.eventfilesystem.POIFSReaderRegistry");
        junit.textui.TestRunner.run(TestPOIFSReaderRegistry.class);
    }
}
