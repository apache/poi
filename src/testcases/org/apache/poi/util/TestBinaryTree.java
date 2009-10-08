
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

package org.apache.poi.util;

import junit.framework.*;

import java.util.*;

/**
 * Class TestBinaryTree
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 */
public final class TestBinaryTree extends TestCase {

    public void testSize() {
        Map m = new BinaryTree();

        assertEquals(0, m.size());
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ].getValue());
            assertEquals(k + 1, m.size());
        }
        int count = m.size();

        for (int k = 0; k < nodes.length; k++)
        {
            m.remove(nodes[ k ].getKey());
            --count;
            assertEquals(count, m.size());

            // failed remove should not affect size
            m.remove(nodes[ k ].getKey());
            assertEquals(count, m.size());
        }
    }

    public void testIsEmpty() {
        Map m = new BinaryTree();

        assertTrue(m.isEmpty());
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ].getValue());
            assertTrue(!m.isEmpty());
        }
        int count = m.size();

        for (int k = 0; k < nodes.length; k++)
        {
            m.remove(nodes[ k ].getKey());
            --count;
            if (count == 0)
            {
                assertTrue(m.isEmpty());
            }
            else
            {
                assertTrue(!m.isEmpty());
            }

            // failed remove should not affect emptiness
            m.remove(nodes[ k ].getKey());
            if (count == 0)
            {
                assertTrue(m.isEmpty());
            }
            else
            {
                assertTrue(!m.isEmpty());
            }
        }
    }

    public void testContainsKey() {
        Map m = new BinaryTree();

        try
        {
            m.containsKey(new Object());
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        try
        {
            m.containsKey(null);
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        assertTrue(!m.containsKey("foo"));
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            assertTrue(m.containsKey(nodes[ k ].getKey()));
        }
        assertTrue(!m.containsKey(Integer.valueOf(-1)));
        try
        {
            m.containsKey("foo");
            fail("Should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        for (int k = 0; k < nodes.length; k++)
        {
            m.remove(nodes[ k ].getKey());
            assertTrue(!m.containsKey(nodes[ k ].getKey()));
        }
    }

    public void testContainsValue() {
        Map           m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            assertTrue(m.containsValue(nodes[ k ]));
        }
        for (int k = 0; k < nodes.length; k++)
        {
            m.remove(nodes[ k ].getKey());
            assertTrue(!m.containsValue(nodes[ k ]));
        }
    }

    public void testGet() {
        Map m = new BinaryTree();

        try
        {
            m.get(new Object());
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        try
        {
            m.get(null);
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        assertNull(m.get("foo"));
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            assertSame(m.get(nodes[ k ].getKey()), nodes[ k ]);
        }
        assertNull(m.get(Integer.valueOf(-1)));
        try
        {
            m.get("foo");
            fail("Should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        for (int k = 0; k < nodes.length; k++)
        {
            assertNotNull(m.get(nodes[ k ].getKey()));
            m.remove(nodes[ k ].getKey());
            assertNull(m.get(nodes[ k ].getKey()));
        }
    }

    public void testPut() {
        Map m = new BinaryTree();

        try
        {
            m.put(new Object(), "foo");
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        try
        {
            m.put(null, "foo");
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        try
        {
            m.put("foo", null);
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        try
        {
            m.put("foo", new Object());
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        LocalTestNode[] nodes = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            assertNull(m.put(nodes[ k ].getKey(), nodes[ k ].getValue()));
            try
            {
                m.put(nodes[ k ].getKey(), "foo");
            }
            catch (IllegalArgumentException ignored)
            {
            }
        }
    }

    public void testRemove() {
        BinaryTree    m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        try
        {
            m.remove(null);
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        try
        {
            m.remove(new Object());
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        assertNull(m.remove(Integer.valueOf(-1)));
        try
        {
            m.remove("foo");
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        for (int k = 0; k < nodes.length; k += 2)
        {
            Comparable key = nodes[ k ].getKey();

            assertNotNull(m.get(key));
            assertSame(nodes[ k ], m.remove(key));
            assertNull(m.remove(key));
            assertNull(m.get(key));
        }
        for (int k = 1; k < nodes.length; k += 2)
        {
            Comparable key = nodes[ k ].getKey();

            assertNotNull(m.get(key));
            assertSame(nodes[ k ], m.remove(key));
            assertNull(m.remove(key));
            assertNull(m.get(key));
        }
        assertTrue(m.isEmpty());
    }

    public void testPutAll() {
        Map           m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        Map m1 = new HashMap();

        m1.put(null, "foo");
        try
        {
            m.putAll(m1);
            fail("Should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        m1 = new HashMap();
        m1.put(new Object(), "bar");
        try
        {
            m.putAll(m1);
            fail("Should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        m1 = new HashMap();
        m1.put("fubar", null);
        try
        {
            m.putAll(m1);
            fail("Should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        m1 = new HashMap();
        m1.put("fubar", new Object());
        try
        {
            m.putAll(m1);
            fail("Should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        assertEquals(nodes.length, m.size());
        m  = new BinaryTree();
        m1 = new HashMap();
        for (int k = 0; k < nodes.length; k++)
        {
            m1.put(nodes[ k ].getKey(), nodes[ k ].getValue());
        }
        m.putAll(m1);
        assertEquals(nodes.length, m.size());
        for (int k = 0; k < nodes.length; k++)
        {
            assertSame(nodes[ k ].getValue(), m.get(nodes[ k ].getKey()));
        }
    }

    public void testClear() {
        Map           m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ].getValue());
            assertTrue(!m.isEmpty());
        }
        assertTrue(!m.isEmpty());
        for (int k = 0; k < nodes.length; k++)
        {
            assertTrue(m.containsKey(nodes[ k ].getKey()));
            assertTrue(m.containsValue(nodes[ k ].getValue()));
        }
        m.clear();
        assertTrue(m.isEmpty());
        for (int k = 0; k < nodes.length; k++)
        {
            assertTrue(!m.containsKey(nodes[ k ].getKey()));
            assertTrue(!m.containsValue(nodes[ k ].getValue()));
        }
    }

    public void testKeySet() {
        testKeySet(new BinaryTree());
        Map           m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        testKeySet(m);
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        int count = m.size();

        for (Iterator iter = m.keySet().iterator(); iter.hasNext(); )
        {
            iter.next();
            iter.remove();
            --count;
            assertEquals(count, m.size());
        }
        assertTrue(m.isEmpty());
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        Set s = m.keySet();

        try
        {
            s.remove(null);
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        try
        {
            s.remove(new Object());
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        for (int k = 0; k < nodes.length; k++)
        {
            Comparable key = nodes[ k ].getKey();

            assertTrue(s.remove(key));
            assertTrue(!s.contains(key));
            assertTrue(!m.containsKey(key));
            assertTrue(!m.containsValue(nodes[ k ]));
        }
        assertTrue(m.isEmpty());
        m = new BinaryTree();
        Collection c1 = new LinkedList();
        Collection c2 = new LinkedList();

        c2.add(Integer.valueOf(-99));
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ].getKey());
            c2.add(nodes[ k ].getKey());
        }
        assertTrue(m.keySet().containsAll(c1));
        assertTrue(!m.keySet().containsAll(c2));
        m  = new BinaryTree();
        c1 = new LinkedList();
        c1.add(Integer.valueOf(-55));
        try
        {
            m.keySet().addAll(c1);
            fail("should have caught exception of addAll()");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ].getKey());
        }
        assertTrue(!m.keySet().retainAll(c1));
        assertEquals(nodes.length, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            if (k % 2 == 1)
            {
                c1.add(nodes[ k ].getKey());
            }
        }
        assertTrue(m.keySet().retainAll(c1));
        assertEquals(nodes.length / 2, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(m.keySet().retainAll(c1));
        assertEquals(0, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(!m.keySet().removeAll(c1));
        assertEquals(nodes.length, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            if (k % 2 == 0)
            {
                c1.add(nodes[ k ].getKey());
            }
        }
        assertTrue(m.keySet().removeAll(c1));
        assertEquals(nodes.length / 2, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ].getKey());
        }
        assertTrue(m.keySet().removeAll(c1));
        assertTrue(m.size() == 0);
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        m.keySet().clear();
        assertTrue(m.size() == 0);
    }

    public void testValues() {
        testValues(new BinaryTree());
        Map           m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        testValues(m);
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        int count = m.size();

        for (Iterator iter = m.values().iterator(); iter.hasNext(); )
        {
            iter.next();
            iter.remove();
            --count;
            assertEquals(count, m.size());
        }
        assertTrue(m.isEmpty());
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        count = m.size();
        Collection s = m.values();

        for (int k = 0; k < count; k++)
        {
            assertTrue(s.remove(nodes[ k ]));
            assertTrue(!s.contains(nodes[ k ]));
            assertTrue(!m.containsKey(nodes[ k ].getKey()));
            assertTrue(!m.containsValue(nodes[ k ]));
        }
        assertTrue(m.isEmpty());
        m = new BinaryTree();
        Collection c1 = new LinkedList();
        Collection c2 = new LinkedList();

        c2.add(new LocalTestNode(-123));
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ]);
            c2.add(nodes[ k ]);
        }
        assertTrue(m.values().containsAll(c1));
        assertTrue(!m.values().containsAll(c2));
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ]);
        }
        try
        {
            m.values().addAll(c1);
            fail("should have caught exception of addAll()");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ]);
        }
        assertTrue(!m.values().retainAll(c1));
        assertEquals(nodes.length, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            if (k % 2 == 1)
            {
                c1.add(nodes[ k ]);
            }
        }
        assertTrue(m.values().retainAll(c1));
        assertEquals(nodes.length / 2, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(m.values().retainAll(c1));
        assertEquals(0, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(!m.values().removeAll(c1));
        assertEquals(nodes.length, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            if (k % 2 == 0)
            {
                c1.add(nodes[ k ]);
            }
        }
        assertTrue(m.values().removeAll(c1));
        assertEquals(nodes.length / 2, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ]);
        }
        assertTrue(m.values().removeAll(c1));
        assertEquals(0, m.size());
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        m.values().clear();
        assertEquals(0, m.size());
    }

    public void testEntrySet() {
        testEntrySet(new BinaryTree());
        Map           m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        testEntrySet(m);
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        try
        {
            (( Map.Entry ) m.entrySet().iterator().next())
                .setValue(new LocalTestNode(-1));
            fail("Should have caught UnsupportedOperationException");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        int count = m.size();

        for (Iterator iter = m.entrySet().iterator(); iter.hasNext(); )
        {
            iter.next();
            iter.remove();
            --count;
            assertEquals(count, m.size());
        }
        assertTrue(m.isEmpty());
        m = new BinaryTree();
        Collection c1 = new LinkedList();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ].getKey());
        }
        try
        {
            m.entrySet().addAll(c1);
            fail("should have caught exception of addAll()");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        m.entrySet().clear();
        assertEquals(0, m.size());
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        int x = 0;

        for (Iterator iter = m.entrySet().iterator(); iter.hasNext(); )
        {
            Map.Entry entry = ( Map.Entry ) iter.next();

            assertSame(entry.getKey(), nodes[ x ].getKey());
            assertSame(entry.getValue(), nodes[ x ]);
            x++;
        }
    }

    public void testEquals() {
        Map           m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(!m.equals(null));
        assertEquals(m, m);
        Map m1 = new HashMap();

        for (int k = 0; k < nodes.length; k++)
        {
            m1.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertEquals(m, m1);
        m1 = new BinaryTree();
        for (int k = 0; k < (nodes.length - 1); k++)
        {
            m1.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(!m.equals(m1));
        m1 = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m1.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        LocalTestNode node1 = new LocalTestNode(-1000);

        m1.put(node1.getKey(), node1);
        assertTrue(!m.equals(m1));
        m1 = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m1.put(nodes[ k ].getKey(), nodes[ nodes.length - (k + 1) ]);
        }
        assertTrue(!m.equals(m1));
        m1 = new BinaryTree();
        for (int k = nodes.length - 1; k >= 0; k--)
        {
            m1.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertEquals(m, m1);
    }

    public void testHashCode() {
        Map           m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        Map m1 = new BinaryTree();

        for (int k = nodes.length - 1; k >= 0; k--)
        {
            m1.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(m.hashCode() == m1.hashCode());
    }

    public void testConstructors() {
        BinaryTree m = new BinaryTree();

        assertTrue(m.isEmpty());
        BinaryTree m1 = new BinaryTree(m);

        assertTrue(m1.isEmpty());
        m1 = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m1.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        m = new BinaryTree(m1);
        assertEquals(m, m1);
        Map m2 = new HashMap();

        for (int k = 0; k < nodes.length; k++)
        {
            m2.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        m = new BinaryTree(m2);
        assertEquals(m, m2);

        // reject duplicated values
        m2 = new HashMap();
        m2.put("1", "foo");
        m2.put("2", "foo");
        try
        {
            m = new BinaryTree(m2);
            fail("Should have caught IllegalArgumentException");
        }
        catch (IllegalArgumentException ignored)
        {
        }

        // reject null values
        m2.put("2", null);
        try
        {
            m = new BinaryTree(m2);
            fail("Should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }

        // reject non-Comparable values
        m2.put("2", new Object());
        try
        {
            m = new BinaryTree(m2);
            fail("Should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }

        // reject incompatible values
        m2.put("2", Integer.valueOf(2));
        try
        {
            m = new BinaryTree(m2);
            fail("Should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }

        // reject incompatible keys
        m2.remove("2");
        m2.put(Integer.valueOf(2), "bad key");
        try
        {
            m = new BinaryTree(m2);
            fail("Should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }

        // reject non-Comparable keys
        m2.clear();
        m2.put("1", "foo");
        m2.put(new Object(), "bad key");
        try
        {
            m = new BinaryTree(m2);
            fail("Should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
    }

    public void testGetKeyForValue() {
        BinaryTree m = new BinaryTree();

        try
        {
            m.getKeyForValue(new Object());
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        try
        {
            m.getKeyForValue(null);
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        assertNull(m.getKeyForValue("foo"));
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            assertSame(m.getKeyForValue(nodes[ k ]), nodes[ k ].getKey());
        }
        assertNull(m.getKeyForValue(new LocalTestNode(-1)));
        try
        {
            m.getKeyForValue("foo");
            fail("Should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        for (int k = 0; k < nodes.length; k++)
        {
            assertNotNull(m.getKeyForValue(nodes[ k ]));
            m.remove(nodes[ k ].getKey());
            assertNull(m.getKeyForValue(nodes[ k ]));
        }
    }

    public void testRemoveValue() {
        BinaryTree    m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        try
        {
            m.removeValue(null);
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        try
        {
            m.removeValue(new Object());
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        assertNull(m.remove(Integer.valueOf(-1)));
        try
        {
            m.removeValue("foo");
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        for (int k = 0; k < nodes.length; k += 2)
        {
            assertNotNull(m.getKeyForValue(nodes[ k ]));
            assertSame(nodes[ k ].getKey(), m.removeValue(nodes[ k ]));
            assertNull(m.removeValue(nodes[ k ]));
            assertNull(m.getKeyForValue(nodes[ k ]));
        }
        for (int k = 1; k < nodes.length; k += 2)
        {
            assertNotNull(m.getKeyForValue(nodes[ k ]));
            assertSame(nodes[ k ].getKey(), m.removeValue(nodes[ k ]));
            assertNull(m.removeValue(nodes[ k ]));
            assertNull(m.getKeyForValue(nodes[ k ]));
        }
        assertTrue(m.isEmpty());
    }

    public void testEntrySetByValue() {
        testEntrySetByValue(new BinaryTree());
        BinaryTree    m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        testEntrySetByValue(m);
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        try
        {
            (( Map.Entry ) m.entrySetByValue().iterator().next())
                .setValue(new LocalTestNode(-1));
            fail("Should have caught UnsupportedOperationException");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        int count = m.size();

        for (Iterator iter = m.entrySetByValue().iterator(); iter.hasNext(); )
        {
            iter.next();
            iter.remove();
            --count;
            assertEquals(count, m.size());
        }
        assertTrue(m.isEmpty());
        m = new BinaryTree();
        Collection c1 = new LinkedList();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ].getKey());
        }
        try
        {
            m.entrySetByValue().addAll(c1);
            fail("should have caught exception of addAll()");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        m.entrySetByValue().clear();
        assertEquals(0, m.size());
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        int x = 0;

        for (Iterator iter = m.entrySetByValue().iterator(); iter.hasNext(); )
        {
            Map.Entry entry = ( Map.Entry ) iter.next();

            assertSame(entry.getKey(), nodes[ x ].getKey());
            assertSame(entry.getValue(), nodes[ x ]);
            x++;
        }
    }

    public void testKeySetByValue() {
        testKeySetByValue(new BinaryTree());
        BinaryTree    m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        testKeySetByValue(m);
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        int count = m.size();

        for (Iterator iter = m.keySetByValue().iterator(); iter.hasNext(); )
        {
            iter.next();
            iter.remove();
            --count;
            assertEquals(count, m.size());
        }
        assertTrue(m.isEmpty());
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        Set s = m.keySetByValue();

        try
        {
            s.remove(null);
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        try
        {
            s.remove(new Object());
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        for (int k = 0; k < nodes.length; k++)
        {
            Comparable key = nodes[ k ].getKey();

            assertTrue(s.remove(key));
            assertTrue(!s.contains(key));
            assertTrue(!m.containsKey(key));
            assertTrue(!m.containsValue(nodes[ k ]));
        }
        assertTrue(m.isEmpty());
        m = new BinaryTree();
        Collection c1 = new LinkedList();
        Collection c2 = new LinkedList();

        c2.add(Integer.valueOf(-99));
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ].getKey());
            c2.add(nodes[ k ].getKey());
        }
        assertTrue(m.keySetByValue().containsAll(c1));
        assertTrue(!m.keySetByValue().containsAll(c2));
        m  = new BinaryTree();
        c1 = new LinkedList();
        c1.add(Integer.valueOf(-55));
        try
        {
            m.keySetByValue().addAll(c1);
            fail("should have caught exception of addAll()");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ].getKey());
        }
        assertTrue(!m.keySetByValue().retainAll(c1));
        assertEquals(nodes.length, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            if (k % 2 == 1)
            {
                c1.add(nodes[ k ].getKey());
            }
        }
        assertTrue(m.keySetByValue().retainAll(c1));
        assertEquals(nodes.length / 2, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(m.keySetByValue().retainAll(c1));
        assertEquals(0, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(!m.keySetByValue().removeAll(c1));
        assertEquals(nodes.length, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            if (k % 2 == 0)
            {
                c1.add(nodes[ k ].getKey());
            }
        }
        assertTrue(m.keySetByValue().removeAll(c1));
        assertEquals(nodes.length / 2, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ].getKey());
        }
        assertTrue(m.keySetByValue().removeAll(c1));
        assertTrue(m.size() == 0);
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        m.keySetByValue().clear();
        assertTrue(m.size() == 0);
    }

    public void testValuesByValue() {
        testValuesByValue(new BinaryTree());
        BinaryTree    m       = new BinaryTree();
        LocalTestNode nodes[] = makeLocalNodes();

        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        testValuesByValue(m);
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        int count = m.size();

        for (Iterator iter = m.valuesByValue().iterator(); iter.hasNext(); )
        {
            iter.next();
            iter.remove();
            --count;
            assertEquals(count, m.size());
        }
        assertTrue(m.isEmpty());
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        count = m.size();
        Collection s = m.valuesByValue();

        for (int k = 0; k < count; k++)
        {
            assertTrue(s.remove(nodes[ k ]));
            assertTrue(!s.contains(nodes[ k ]));
            assertTrue(!m.containsKey(nodes[ k ].getKey()));
            assertTrue(!m.containsValue(nodes[ k ]));
        }
        assertTrue(m.isEmpty());
        m = new BinaryTree();
        Collection c1 = new LinkedList();
        Collection c2 = new LinkedList();

        c2.add(new LocalTestNode(-123));
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ]);
            c2.add(nodes[ k ]);
        }
        assertTrue(m.valuesByValue().containsAll(c1));
        assertTrue(!m.valuesByValue().containsAll(c2));
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ]);
        }
        try
        {
            m.valuesByValue().addAll(c1);
            fail("should have caught exception of addAll()");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ]);
        }
        assertTrue(!m.valuesByValue().retainAll(c1));
        assertEquals(nodes.length, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            if (k % 2 == 1)
            {
                c1.add(nodes[ k ]);
            }
        }
        assertTrue(m.valuesByValue().retainAll(c1));
        assertEquals(nodes.length / 2, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(m.valuesByValue().retainAll(c1));
        assertEquals(0, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        assertTrue(!m.valuesByValue().removeAll(c1));
        assertEquals(nodes.length, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            if (k % 2 == 0)
            {
                c1.add(nodes[ k ]);
            }
        }
        assertTrue(m.valuesByValue().removeAll(c1));
        assertEquals(nodes.length / 2, m.size());
        m  = new BinaryTree();
        c1 = new LinkedList();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
            c1.add(nodes[ k ]);
        }
        assertTrue(m.valuesByValue().removeAll(c1));
        assertEquals(0, m.size());
        m = new BinaryTree();
        for (int k = 0; k < nodes.length; k++)
        {
            m.put(nodes[ k ].getKey(), nodes[ k ]);
        }
        m.valuesByValue().clear();
        assertEquals(0, m.size());
    }

    /* ********** START helper methods ********** */
    private static void testKeySet(final Map m) {
        Set s = m.keySet();

        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        LocalTestNode node = new LocalTestNode(-1);

        m.put(node.getKey(), node);
        assertTrue(s.contains(node.getKey()));
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        m.remove(node.getKey());
        assertTrue(!s.contains(node.getKey()));
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        try
        {
            s.contains(null);
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        try
        {
            s.contains(new Object());
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        for (int k = 0; k < m.size(); k++)
        {
            assertTrue(s.contains(Integer.valueOf(k)));
        }
        int count = 0;

        for (Iterator iter = s.iterator(); iter.hasNext(); )
        {
            iter.next();
            ++count;
        }
        assertEquals(count, s.size());

        // force the map to have some content
        m.put(node.getKey(), node);
        Iterator      iter  = m.keySet().iterator();
        LocalTestNode node2 = new LocalTestNode(-2);

        m.put(node2.getKey(), node2);
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a put");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        m.remove(node2.getKey());
        iter = s.iterator();
        m.remove(node.getKey());
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a Map remove");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        m.put(node.getKey(), node);
        iter = s.iterator();
        s.remove(node.getKey());
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a Set remove");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        iter  = s.iterator();
        count = 0;
        boolean terminated = false;

        try
        {
            while (true)
            {
                iter.next();
                ++count;
            }
        }
        catch (NoSuchElementException ignored)
        {
            terminated = true;
        }
        assertTrue(terminated);
        assertEquals(m.size(), count);
        iter = s.iterator();
        try
        {
            iter.remove();
            fail("Should have thrown exception");
        }
        catch (IllegalStateException ignored)
        {
        }
        m.put(node.getKey(), node);
        iter = s.iterator();
        iter.next();
        m.put(node2.getKey(), node2);
        try
        {
            iter.remove();
            fail("should have thrown exception");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        Iterator iter2 = s.iterator();

        iter2.next();
        LocalTestNode node3 = new LocalTestNode(-3);

        m.put(node3.getKey(), node3);
        try
        {
            iter2.remove();
            fail("should have thrown exception");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        int r_count = 0;

        for (iter = s.iterator(); iter.hasNext(); )
        {
            if (iter.next().equals(node.getKey()))
            {
                try
                {
                    iter.remove();
                    ++r_count;
                    iter.remove();
                    fail("2nd remove should have failed");
                }
                catch (IllegalStateException ignored)
                {
                    assertEquals(1, r_count);
                }
            }
        }
        assertEquals(1, r_count);
        assertTrue(!s.contains(node.getKey()));
        r_count = 0;
        m.put(node.getKey(), node);
        Object[] a1 = s.toArray();

        assertEquals(s.size(), a1.length);
        if (a1.length > 1)
        {
            Comparable first = ( Comparable ) a1[ 0 ];

            for (int k = 1; k < a1.length; k++)
            {
                Comparable second = ( Comparable ) a1[ k ];

                assertTrue(first.compareTo(second) < 0);
                first = second;
            }
            iter  = s.iterator();
            first = ( Comparable ) iter.next();
            for (; iter.hasNext(); )
            {
                Comparable second = ( Comparable ) iter.next();

                assertTrue(first.compareTo(second) < 0);
                first = second;
            }
        }
        try
        {
            String array2[] = ( String [] ) s.toArray(new String[ 0 ]);

            if (s.size() != 0)
            {
                fail("should have caught exception creating an invalid array");
            }
        }
        catch (ArrayStoreException ignored)
        {
        }
        Comparable array2[] =
            ( Comparable [] ) s.toArray(new Comparable[ 0 ]);
        Integer    array3[] =
            ( Integer [] ) s.toArray(new Integer[ s.size() ]);

        if (array3.length > 1)
        {
            Integer first = array3[ 0 ];

            for (int k = 1; k < array3.length; k++)
            {
                Integer second = array3[ k ];

                assertTrue(first.compareTo(second) < 0);
                first = second;
            }
        }
        try
        {
            s.add("foo");
            fail("should have thrown an exception");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        assertTrue(!s.equals(null));
        assertEquals(s, s);
        Set hs = new HashSet(s);

        assertEquals(s, hs);
        assertEquals(hs, s);
        assertTrue(s.hashCode() == hs.hashCode());
    }

    private static void testKeySetByValue(final BinaryTree m) {
        Set s = m.keySetByValue();

        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        LocalTestNode node = new LocalTestNode(-1);

        m.put(node.getKey(), node);
        assertTrue(s.contains(node.getKey()));
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        m.remove(node.getKey());
        assertTrue(!s.contains(node.getKey()));
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        try
        {
            s.contains(null);
            fail("should have caught NullPointerException");
        }
        catch (NullPointerException ignored)
        {
        }
        try
        {
            s.contains(new Object());
            fail("should have caught ClassCastException");
        }
        catch (ClassCastException ignored)
        {
        }
        for (int k = 0; k < m.size(); k++)
        {
            assertTrue(s.contains(Integer.valueOf(k)));
        }
        int count = 0;

        for (Iterator iter = s.iterator(); iter.hasNext(); )
        {
            iter.next();
            ++count;
        }
        assertEquals(count, s.size());

        // force the map to have some content
        m.put(node.getKey(), node);
        Iterator      iter  = m.keySetByValue().iterator();
        LocalTestNode node2 = new LocalTestNode(-2);

        m.put(node2.getKey(), node2);
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a put");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        m.remove(node2.getKey());
        iter = s.iterator();
        m.remove(node.getKey());
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a Map remove");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        m.put(node.getKey(), node);
        iter = s.iterator();
        s.remove(node.getKey());
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a Set remove");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        iter  = s.iterator();
        count = 0;
        boolean terminated = false;

        try
        {
            while (true)
            {
                iter.next();
                ++count;
            }
        }
        catch (NoSuchElementException ignored)
        {
            terminated = true;
        }
        assertTrue(terminated);
        assertEquals(m.size(), count);
        iter = s.iterator();
        try
        {
            iter.remove();
            fail("Should have thrown exception");
        }
        catch (IllegalStateException ignored)
        {
        }
        m.put(node.getKey(), node);
        iter = s.iterator();
        iter.next();
        m.put(node2.getKey(), node2);
        try
        {
            iter.remove();
            fail("should have thrown exception");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        Iterator iter2 = s.iterator();

        iter2.next();
        LocalTestNode node3 = new LocalTestNode(-3);

        m.put(node3.getKey(), node3);
        try
        {
            iter2.remove();
            fail("should have thrown exception");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        int r_count = 0;

        for (iter = s.iterator(); iter.hasNext(); )
        {
            if (iter.next().equals(node.getKey()))
            {
                try
                {
                    iter.remove();
                    ++r_count;
                    iter.remove();
                    fail("2nd remove should have failed");
                }
                catch (IllegalStateException ignored)
                {
                    assertEquals(1, r_count);
                }
            }
        }
        assertEquals(1, r_count);
        assertTrue(!s.contains(node.getKey()));
        r_count = 0;
        m.put(node.getKey(), node);
        Object[] a1 = s.toArray();

        assertEquals(s.size(), a1.length);

//          if (a1.length > 1)
//          {
//              Comparable first = ( Comparable ) a1[ 0 ];
//              for (int k = 1; k < a1.length; k++)
//              {
//                  Comparable second = ( Comparable ) a1[ k ];
//                  assertTrue(first.compareTo(second) < 0);
//                  first = second;
//              }
//              iter  = s.iterator();
//              first = ( Comparable ) iter.next();
//              for (; iter.hasNext(); )
//              {
//                  Comparable second = ( Comparable ) iter.next();
//                  assertTrue(first.compareTo(second) < 0);
//                  first = second;
//              }
//          }
        try
        {
            String array2[] = ( String [] ) s.toArray(new String[ 0 ]);

            if (s.size() != 0)
            {
                fail("should have caught exception creating an invalid array");
            }
        }
        catch (ArrayStoreException ignored)
        {
        }
        Comparable array2[] =
            ( Comparable [] ) s.toArray(new Comparable[ 0 ]);
        Integer    array3[] =
            ( Integer [] ) s.toArray(new Integer[ s.size() ]);

//          if (array3.length > 1)
//          {
//              Integer first = array3[ 0 ];
//              for (int k = 1; k < array3.length; k++)
//              {
//                  Integer second = array3[ k ];
//                  assertTrue(first.compareTo(second) < 0);
//                  first = second;
//              }
//          }
        try
        {
            s.add("foo");
            fail("should have thrown an exception");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        assertTrue(!s.equals(null));
        assertEquals(s, s);
        Set hs = new HashSet(s);

        assertEquals(s, hs);
        assertEquals(hs, s);
        assertTrue(s.hashCode() == hs.hashCode());
    }

    private static void testValues(Map m) {
        Collection s = m.values();

        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        LocalTestNode node = new LocalTestNode(-1);

        m.put(node.getKey(), node);
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        m.remove(node.getKey());
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        assertTrue(!s.contains(node));
        for (int k = 0; k < m.size(); k++)
        {
            assertTrue(s.contains(new LocalTestNode(k)));
        }
        m.put(node.getKey(), node);
        assertTrue(s.contains(node));
        m.remove(node.getKey());
        assertTrue(!s.contains(node));
        int count = 0;

        for (Iterator iter = s.iterator(); iter.hasNext(); )
        {
            iter.next();
            ++count;
        }
        assertEquals(s.size(), count);
        LocalTestNode node4 = new LocalTestNode(-4);

        m.put(node4.getKey(), node4);
        Iterator iter = s.iterator();

        m.put(node.getKey(), node);
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a put");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        iter = s.iterator();
        m.remove(node.getKey());
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a Map remove");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        m.put(node.getKey(), node);
        iter = s.iterator();
        s.remove(node);
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a Set remove");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        iter  = s.iterator();
        count = 0;
        boolean terminated = false;

        try
        {
            while (true)
            {
                iter.next();
                ++count;
            }
        }
        catch (NoSuchElementException ignored)
        {
            terminated = true;
        }
        assertTrue(terminated);
        assertEquals(m.size(), count);
        iter = s.iterator();
        try
        {
            iter.remove();
            fail("Should have thrown exception");
        }
        catch (IllegalStateException ignored)
        {
        }
        Iterator iter2 = s.iterator();

        try
        {
            iter2.remove();
            fail("Should have thrown exception");
        }
        catch (IllegalStateException ignored)
        {
        }
        m.put(node.getKey(), node);
        iter = s.iterator();
        iter.next();
        LocalTestNode node2 = new LocalTestNode(-2);

        m.put(node2.getKey(), node2);
        try
        {
            iter.remove();
            fail("should have thrown exception");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        LocalTestNode node3 = new LocalTestNode(-3);

        m.put(node3.getKey(), node3);
        iter2 = s.iterator();
        while (iter2.hasNext())
        {
            iter2.next();
        }
        int r_count = 0;

        for (iter = s.iterator(); iter.hasNext(); )
        {
            if (iter.next().equals(node3))
            {
                try
                {
                    iter.remove();
                    ++r_count;
                    iter.remove();
                    fail("2nd remove should have failed");
                }
                catch (IllegalStateException ignored)
                {
                    assertEquals(1, r_count);
                }
            }
        }
        assertEquals(1, r_count);
        assertTrue(!s.contains(node3));
        Object[] a1 = s.toArray();

        assertTrue(a1.length == s.size());
        if (a1.length > 1)
        {
            Comparable first = ( Comparable ) a1[ 0 ];

            for (int k = 1; k < a1.length; k++)
            {
                Comparable second = ( Comparable ) a1[ k ];

                assertTrue(first.compareTo(second) < 0);
                first = second;
            }
            iter  = s.iterator();
            first = ( Comparable ) iter.next();
            for (; iter.hasNext(); )
            {
                Comparable second = ( Comparable ) iter.next();

                assertTrue(first.compareTo(second) < 0);
                first = second;
            }
        }
        try
        {
            String array2[] = ( String [] ) s.toArray(new String[ 0 ]);

            if (s.size() != 0)
            {
                fail("should have caught exception creating an invalid array");
            }
        }
        catch (ArrayStoreException ignored)
        {
        }
        m.remove(node.getKey());
        m.remove(node2.getKey());
        m.remove(node3.getKey());
        LocalTestNode array2[] =
            ( LocalTestNode [] ) s.toArray(new LocalTestNode[ 0 ]);
        LocalTestNode array3[] =
            ( LocalTestNode [] ) s.toArray(new LocalTestNode[ s.size() ]);

        if (array3.length > 1)
        {
            LocalTestNode first = array3[ 0 ];

            for (int k = 1; k < array3.length; k++)
            {
                LocalTestNode second = array3[ k ];

                assertTrue(first.compareTo(second) < 0);
                first = second;
            }
        }
        try
        {
            s.add(node.getKey());
            fail("should have thrown an exception");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        assertTrue(!s.equals(null));
        assertEquals(s, s);
        Set hs = new HashSet(s);

        assertTrue(!s.equals(hs));
        assertTrue(!hs.equals(s));
    }

    private static void testValuesByValue(BinaryTree m) {
        Collection s = m.valuesByValue();

        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        LocalTestNode node = new LocalTestNode(-1);

        m.put(node.getKey(), node);
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        m.remove(node.getKey());
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        assertTrue(!s.contains(node));
        for (int k = 0; k < m.size(); k++)
        {
            assertTrue(s.contains(new LocalTestNode(k)));
        }
        m.put(node.getKey(), node);
        assertTrue(s.contains(node));
        m.remove(node.getKey());
        assertTrue(!s.contains(node));
        int count = 0;

        for (Iterator iter = s.iterator(); iter.hasNext(); )
        {
            iter.next();
            ++count;
        }
        assertEquals(s.size(), count);
        LocalTestNode node4 = new LocalTestNode(-4);

        m.put(node4.getKey(), node4);
        Iterator iter = s.iterator();

        m.put(node.getKey(), node);
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a put");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        iter = s.iterator();
        m.remove(node.getKey());
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a Map remove");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        m.put(node.getKey(), node);
        iter = s.iterator();
        s.remove(node);
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a Set remove");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        iter  = s.iterator();
        count = 0;
        boolean terminated = false;

        try
        {
            while (true)
            {
                iter.next();
                ++count;
            }
        }
        catch (NoSuchElementException ignored)
        {
            terminated = true;
        }
        assertTrue(terminated);
        assertEquals(m.size(), count);
        iter = s.iterator();
        try
        {
            iter.remove();
            fail("Should have thrown exception");
        }
        catch (IllegalStateException ignored)
        {
        }
        Iterator iter2 = s.iterator();

        try
        {
            iter2.remove();
            fail("Should have thrown exception");
        }
        catch (IllegalStateException ignored)
        {
        }
        m.put(node.getKey(), node);
        iter = s.iterator();
        iter.next();
        LocalTestNode node2 = new LocalTestNode(-2);

        m.put(node2.getKey(), node2);
        try
        {
            iter.remove();
            fail("should have thrown exception");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        LocalTestNode node3 = new LocalTestNode(-3);

        m.put(node3.getKey(), node3);
        iter2 = s.iterator();
        while (iter2.hasNext())
        {
            iter2.next();
        }
        int r_count = 0;

        for (iter = s.iterator(); iter.hasNext(); )
        {
            if (iter.next().equals(node3))
            {
                try
                {
                    iter.remove();
                    ++r_count;
                    iter.remove();
                    fail("2nd remove should have failed");
                }
                catch (IllegalStateException ignored)
                {
                    assertEquals(1, r_count);
                }
            }
        }
        assertEquals(1, r_count);
        assertTrue(!s.contains(node3));
        Object[] a1 = s.toArray();

        assertTrue(a1.length == s.size());
        try
        {
            String array2[] = ( String [] ) s.toArray(new String[ 0 ]);

            if (s.size() != 0)
            {
                fail("should have caught exception creating an invalid array");
            }
        }
        catch (ArrayStoreException ignored)
        {
        }
        m.remove(node.getKey());
        m.remove(node2.getKey());
        m.remove(node3.getKey());
        LocalTestNode array2[] =
            ( LocalTestNode [] ) s.toArray(new LocalTestNode[ 0 ]);
        LocalTestNode array3[] =
            ( LocalTestNode [] ) s.toArray(new LocalTestNode[ s.size() ]);

        try
        {
            s.add(node.getKey());
            fail("should have thrown an exception");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        assertTrue(!s.equals(null));
        assertEquals(s, s);
        Set hs = new HashSet(s);

        assertTrue(!s.equals(hs));
        assertTrue(!hs.equals(s));
    }

    private static void testEntrySet(Map m) {
        Set s = m.entrySet();

        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        LocalTestNode node = new LocalTestNode(-1);

        m.put(node.getKey(), node);
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        m.remove(node.getKey());
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        int count = 0;

        for (Iterator iter = s.iterator(); iter.hasNext(); )
        {
            iter.next();
            ++count;
        }
        assertEquals(s.size(), count);
        LocalTestNode node2 = new LocalTestNode(-2);

        if (m.size() == 0)
        {
            m.put(node2.getKey(), node2);
        }
        Iterator iter = s.iterator();

        m.put(node.getKey(), node);
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a put");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        m.remove(node2.getKey());
        iter = s.iterator();
        m.remove(node.getKey());
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a Map remove");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        m.put(node.getKey(), node);
        iter  = s.iterator();
        count = 0;
        boolean terminated = false;

        try
        {
            while (true)
            {
                iter.next();
                ++count;
            }
        }
        catch (NoSuchElementException ignored)
        {
            terminated = true;
        }
        assertTrue(terminated);
        assertEquals(m.size(), count);
        iter = s.iterator();
        try
        {
            iter.remove();
            fail("Should have thrown exception");
        }
        catch (IllegalStateException ignored)
        {
        }
        iter = s.iterator();
        iter.next();
        LocalTestNode node3 = new LocalTestNode(-3);

        m.put(node3.getKey(), node3);
        try
        {
            iter.remove();
            fail("should have thrown exception");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        int r_count = 0;
        int when    = m.size() / 2;
        int timer   = 0;

        for (iter = s.iterator(); iter.hasNext(); )
        {
            iter.next();
            if (timer == when)
            {
                try
                {
                    iter.remove();
                    ++r_count;
                    iter.remove();
                    fail("2nd remove should have failed");
                }
                catch (IllegalStateException ignored)
                {
                    assertEquals(1, r_count);
                }
            }
            timer++;
        }
        assertEquals(1, r_count);
        Iterator iter2 = s.iterator();

        try
        {
            iter2.remove();
            fail("Should have thrown exception");
        }
        catch (IllegalStateException ignored)
        {
        }
        iter2 = s.iterator();
        while (iter2.hasNext())
        {
            iter2.next();
        }
        LocalTestNode node4 = new LocalTestNode(-4);

        m.put(node4.getKey(), node4);
        try
        {
            iter2.remove();
            fail("should have thrown exception");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        Object[] a1 = s.toArray();

        assertTrue(a1.length == s.size());
        if (a1.length > 1)
        {
            Map.Entry first = ( Map.Entry ) a1[ 0 ];

            for (int k = 1; k < a1.length; k++)
            {
                Map.Entry second = ( Map.Entry ) a1[ k ];

                assertTrue((( Comparable ) first.getKey())
                    .compareTo(( Comparable ) second.getKey()) < 0);
                first = second;
            }
            iter  = s.iterator();
            first = ( Map.Entry ) iter.next();
            for (; iter.hasNext(); )
            {
                Map.Entry second = ( Map.Entry ) iter.next();

                assertTrue((( Comparable ) first.getKey())
                    .compareTo(( Comparable ) second.getKey()) < 0);
                first = second;
            }
        }
        try
        {
            Integer array2[] = ( Integer [] ) s.toArray(new Integer[ 0 ]);

            if (s.size() != 0)
            {
                fail("should have caught exception creating an invalid array");
            }
        }
        catch (ArrayStoreException ignored)
        {
        }
        Map.Entry array2[] = ( Map.Entry [] ) s.toArray(new Map.Entry[ 0 ]);
        Map.Entry array3[] =
            ( Map.Entry [] ) s.toArray(new Map.Entry[ s.size() ]);

        if (array3.length > 1)
        {
            Comparable first =
                ( Comparable ) (( Map.Entry ) array3[ 0 ]).getKey();

            for (int k = 1; k < array3.length; k++)
            {
                Comparable second =
                    ( Comparable ) (( Map.Entry ) array3[ k ]).getKey();

                assertTrue(first.compareTo(second) < 0);
                first = second;
            }
        }
        try
        {
            s.add(node.getKey());
            fail("should have thrown an exception");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        assertTrue(!s.equals(null));
        assertEquals("SetEquality 1", s, s);
        Set hs = new HashSet(s);

        assertEquals("SetEquality 2", s, hs);
        assertEquals("SetEquality 3", hs, s);
        assertTrue(s.hashCode() == hs.hashCode());
    }

    private static void testEntrySetByValue(BinaryTree m) {
        Set s = m.entrySetByValue();

        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        LocalTestNode node = new LocalTestNode(-1);

        m.put(node.getKey(), node);
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        m.remove(node.getKey());
        assertEquals(m.size(), s.size());
        assertEquals(m.isEmpty(), s.isEmpty());
        int count = 0;

        for (Iterator iter = s.iterator(); iter.hasNext(); )
        {
            iter.next();
            ++count;
        }
        assertEquals(s.size(), count);
        LocalTestNode node2 = new LocalTestNode(-2);

        if (m.size() == 0)
        {
            m.put(node2.getKey(), node2);
        }
        Iterator iter = s.iterator();

        m.put(node.getKey(), node);
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a put");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        m.remove(node2.getKey());
        iter = s.iterator();
        m.remove(node.getKey());
        try
        {
            iter.next();
            fail("next() should have thrown an exception after a Map remove");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        m.put(node.getKey(), node);
        iter  = s.iterator();
        count = 0;
        boolean terminated = false;

        try
        {
            while (true)
            {
                iter.next();
                ++count;
            }
        }
        catch (NoSuchElementException ignored)
        {
            terminated = true;
        }
        assertTrue(terminated);
        assertEquals(m.size(), count);
        iter = s.iterator();
        try
        {
            iter.remove();
            fail("Should have thrown exception");
        }
        catch (IllegalStateException ignored)
        {
        }
        iter = s.iterator();
        iter.next();
        LocalTestNode node3 = new LocalTestNode(-3);

        m.put(node3.getKey(), node3);
        try
        {
            iter.remove();
            fail("should have thrown exception");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        int r_count = 0;
        int when    = m.size() / 2;
        int timer   = 0;

        for (iter = s.iterator(); iter.hasNext(); )
        {
            iter.next();
            if (timer == when)
            {
                try
                {
                    iter.remove();
                    ++r_count;
                    iter.remove();
                    fail("2nd remove should have failed");
                }
                catch (IllegalStateException ignored)
                {
                    assertEquals(1, r_count);
                }
            }
            timer++;
        }
        assertEquals(1, r_count);
        Iterator iter2 = s.iterator();

        try
        {
            iter2.remove();
            fail("Should have thrown exception");
        }
        catch (IllegalStateException ignored)
        {
        }
        iter2 = s.iterator();
        while (iter2.hasNext())
        {
            iter2.next();
        }
        LocalTestNode node4 = new LocalTestNode(-4);

        m.put(node4.getKey(), node4);
        try
        {
            iter2.remove();
            fail("should have thrown exception");
        }
        catch (ConcurrentModificationException ignored)
        {
        }
        Object[] a1 = s.toArray();

        assertTrue(a1.length == s.size());
        if (a1.length > 1)
        {
            Map.Entry first = ( Map.Entry ) a1[ 0 ];

            for (int k = 1; k < a1.length; k++)
            {
                Map.Entry second = ( Map.Entry ) a1[ k ];

                assertTrue((( Comparable ) first.getKey())
                    .compareTo(( Comparable ) second.getKey()) < 0);
                first = second;
            }
            iter  = s.iterator();
            first = ( Map.Entry ) iter.next();
            for (; iter.hasNext(); )
            {
                Map.Entry second = ( Map.Entry ) iter.next();

                assertTrue((( Comparable ) first.getKey())
                    .compareTo(( Comparable ) second.getKey()) < 0);
                first = second;
            }
        }
        try
        {
            Integer array2[] = ( Integer [] ) s.toArray(new Integer[ 0 ]);

            if (s.size() != 0)
            {
                fail("should have caught exception creating an invalid array");
            }
        }
        catch (ArrayStoreException ignored)
        {
        }
        Map.Entry array2[] = ( Map.Entry [] ) s.toArray(new Map.Entry[ 0 ]);
        Map.Entry array3[] =
            ( Map.Entry [] ) s.toArray(new Map.Entry[ s.size() ]);

        if (array3.length > 1)
        {
            Comparable first =
                ( Comparable ) (( Map.Entry ) array3[ 0 ]).getValue();

            for (int k = 1; k < array3.length; k++)
            {
                Comparable second =
                    ( Comparable ) (( Map.Entry ) array3[ k ]).getValue();

                assertTrue(first.compareTo(second) < 0);
                first = second;
            }
        }
        try
        {
            s.add(node.getKey());
            fail("should have thrown an exception");
        }
        catch (UnsupportedOperationException ignored)
        {
        }
        assertTrue(!s.equals(null));
        assertEquals("SetEquality 1", s, s);
        Set hs = new HashSet(s);

        assertEquals("SetEquality 2", s, hs);
        assertEquals("SetEquality 3", hs, s);
        assertTrue(s.hashCode() == hs.hashCode());
    }

    private LocalTestNode [] makeLocalNodes()
    {
        LocalTestNode nodes[] = new LocalTestNode[ 1023 ];

        for (int k = 0; k < nodes.length; k++)
        {
            nodes[ k ] = new LocalTestNode(k);
        }
        return nodes;
    }
}
