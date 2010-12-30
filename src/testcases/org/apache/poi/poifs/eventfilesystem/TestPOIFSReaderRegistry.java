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

package org.apache.poi.poifs.eventfilesystem;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.poi.poifs.filesystem.POIFSDocumentPath;

/**
 * Class to test POIFSReaderRegistry functionality
 *
 * @author Marc Johnson
 */
public final class TestPOIFSReaderRegistry extends TestCase {
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
     * Test empty registry
     */
    public void testEmptyRegistry() {
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
    public void testMixedRegistrationOperations() {
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
}
