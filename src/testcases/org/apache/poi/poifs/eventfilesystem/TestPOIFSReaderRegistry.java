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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.poifs.filesystem.POIFSDocumentPath;
import org.junit.Test;

/**
 * Class to test POIFSReaderRegistry functionality
 *
 * @author Marc Johnson
 */
public final class TestPOIFSReaderRegistry {
    private final POIFSReaderListener[] listeners =
    {
        new Listener(), new Listener(), new Listener(), new Listener()
    };
    private final POIFSDocumentPath[]   paths     =
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
    private final String[]              names     =
    {
        "a0", "a1", "a2", "a3"
    };

    /**
     * Test empty registry
     */
    @Test
    public void testEmptyRegistry() {
        POIFSReaderRegistry registry = new POIFSReaderRegistry();

        for (POIFSDocumentPath path : paths) {
            for (String name : names) {
                Iterator<POIFSReaderListener> listeners =
                    registry.getListeners(path, name).iterator();

                assertTrue(!listeners.hasNext());
            }
        }
    }

    /**
     * Test mixed registration operations
     */
    @Test
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
                        registry.registerListener(
                                listeners[ j ], paths[ k ], names[ n ]);
                    }
                }
            }
        }
        for (int k = 0; k < paths.length; k++)
        {
            for (int n = 0; n < names.length; n++)
            {
                Iterable<POIFSReaderListener> listeners =
                    registry.getListeners(paths[ k ], names[ n ]);

                if (k == n)
                {
                    assertTrue(!listeners.iterator().hasNext());
                }
                else
                {
                    Set<POIFSReaderListener> registeredListeners =
                            new HashSet<>();

                    for (POIFSReaderListener rl : listeners) {
                        registeredListeners.add(rl);
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
        for (POIFSReaderListener listener : listeners) {
            registry.registerListener(listener);
        }
        for (POIFSDocumentPath path : paths) {
            for (String name : names) {
                Iterable<POIFSReaderListener> listeners =
                    registry.getListeners(path, name);
                Set<POIFSReaderListener> registeredListeners =
                        new HashSet<>();

                for (POIFSReaderListener rl : listeners) {
                    registeredListeners.add(rl);
                }
                assertEquals(this.listeners.length,
                             registeredListeners.size());
                for (POIFSReaderListener listener : this.listeners) {
                    assertTrue(registeredListeners
                        .contains(listener));
                }
            }
        }
    }
}
