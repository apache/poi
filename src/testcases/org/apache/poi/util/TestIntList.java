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

import junit.framework.TestCase;

/**
 * Class to test IntList
 *
 * @author Marc Johnson
 */
public final class TestIntList extends TestCase {

    public void testConstructors() {
        IntList list = new IntList();

        assertTrue(list.isEmpty());
        list.add(0);
        list.add(1);
        IntList list2 = new IntList(list);

        assertEquals(list, list2);
        IntList list3 = new IntList(2);

        assertTrue(list3.isEmpty());
    }

    public void testAdd() {
        IntList list      = new IntList();
        int[]   testArray =
        {
            0, 1, 2, 3, 5
        };

        for (int j = 0; j < testArray.length; j++)
        {
            list.add(testArray[ j ]);
        }
        for (int j = 0; j < testArray.length; j++)
        {
            assertEquals(testArray[ j ], list.get(j));
        }
        assertEquals(testArray.length, list.size());

        // add at the beginning
        list.add(0, -1);
        assertEquals(-1, list.get(0));
        assertEquals(testArray.length + 1, list.size());
        for (int j = 0; j < testArray.length; j++)
        {
            assertEquals(testArray[ j ], list.get(j + 1));
        }

        // add in the middle
        list.add(5, 4);
        assertEquals(4, list.get(5));
        assertEquals(testArray.length + 2, list.size());
        for (int j = 0; j < list.size(); j++)
        {
            assertEquals(j - 1, list.get(j));
        }

        // add at the end
        list.add(list.size(), 6);
        assertEquals(testArray.length + 3, list.size());
        for (int j = 0; j < list.size(); j++)
        {
            assertEquals(j - 1, list.get(j));
        }

        // add past end
        try
        {
            list.add(list.size() + 1, 8);
            fail("should have thrown exception");
        }
        catch (IndexOutOfBoundsException e)
        {

            // as expected
        }

        // test growth
        list = new IntList(0);
        for (int j = 0; j < 1000; j++)
        {
            list.add(j);
        }
        assertEquals(1000, list.size());
        for (int j = 0; j < 1000; j++)
        {
            assertEquals(j, list.get(j));
        }
        list = new IntList(0);
        for (int j = 0; j < 1000; j++)
        {
            list.add(0, j);
        }
        assertEquals(1000, list.size());
        for (int j = 0; j < 1000; j++)
        {
            assertEquals(j, list.get(999 - j));
        }
    }

    public void testAddAll() {
        IntList list = new IntList();

        for (int j = 0; j < 5; j++)
        {
            list.add(j);
        }
        IntList list2 = new IntList(0);

        list2.addAll(list);
        list2.addAll(list);
        assertEquals(2 * list.size(), list2.size());
        for (int j = 0; j < 5; j++)
        {
            assertEquals(list2.get(j), j);
            assertEquals(list2.get(j + list.size()), j);
        }
        IntList empty = new IntList();
        int     limit = list.size();

        for (int j = 0; j < limit; j++)
        {
            assertTrue(list.addAll(j, empty));
            assertEquals(limit, list.size());
        }
        try
        {
            list.addAll(limit + 1, empty);
            fail("should have thrown an exception");
        }
        catch (IndexOutOfBoundsException e)
        {

            // as expected
        }

        // try add at beginning
        empty.addAll(0, list);
        assertEquals(empty, list);

        // try in the middle
        empty.addAll(1, list);
        assertEquals(2 * list.size(), empty.size());
        assertEquals(list.get(0), empty.get(0));
        assertEquals(list.get(0), empty.get(1));
        assertEquals(list.get(1), empty.get(2));
        assertEquals(list.get(1), empty.get(6));
        assertEquals(list.get(2), empty.get(3));
        assertEquals(list.get(2), empty.get(7));
        assertEquals(list.get(3), empty.get(4));
        assertEquals(list.get(3), empty.get(8));
        assertEquals(list.get(4), empty.get(5));
        assertEquals(list.get(4), empty.get(9));

        // try at the end
        empty.addAll(empty.size(), list);
        assertEquals(3 * list.size(), empty.size());
        assertEquals(list.get(0), empty.get(0));
        assertEquals(list.get(0), empty.get(1));
        assertEquals(list.get(0), empty.get(10));
        assertEquals(list.get(1), empty.get(2));
        assertEquals(list.get(1), empty.get(6));
        assertEquals(list.get(1), empty.get(11));
        assertEquals(list.get(2), empty.get(3));
        assertEquals(list.get(2), empty.get(7));
        assertEquals(list.get(2), empty.get(12));
        assertEquals(list.get(3), empty.get(4));
        assertEquals(list.get(3), empty.get(8));
        assertEquals(list.get(3), empty.get(13));
        assertEquals(list.get(4), empty.get(5));
        assertEquals(list.get(4), empty.get(9));
        assertEquals(list.get(4), empty.get(14));
    }

    public void testClear() {
        IntList list = new IntList();

        for (int j = 0; j < 500; j++)
        {
            list.add(j);
        }
        assertEquals(500, list.size());
        list.clear();
        assertEquals(0, list.size());
        for (int j = 0; j < 500; j++)
        {
            list.add(j + 1);
        }
        assertEquals(500, list.size());
        for (int j = 0; j < 500; j++)
        {
            assertEquals(j + 1, list.get(j));
        }
    }

    public void testContains() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j += 2)
        {
            list.add(j);
        }
        for (int j = 0; j < 1000; j++)
        {
            if (j % 2 == 0)
            {
                assertTrue(list.contains(j));
            }
            else
            {
                assertTrue(!list.contains(j));
            }
        }
    }

    public void testContainsAll() {
        IntList list = new IntList();

        assertTrue(list.containsAll(list));
        for (int j = 0; j < 10; j++)
        {
            list.add(j);
        }
        IntList list2 = new IntList(list);

        assertTrue(list2.containsAll(list));
        assertTrue(list.containsAll(list2));
        list2.add(10);
        assertTrue(list2.containsAll(list));
        assertTrue(!list.containsAll(list2));
        list.add(11);
        assertTrue(!list2.containsAll(list));
        assertTrue(!list.containsAll(list2));
    }

    public void testEquals() {
        IntList list = new IntList();

        assertEquals(list, list);
        assertTrue(!list.equals(null));
        IntList list2 = new IntList(200);

        assertEquals(list, list2);
        assertEquals(list2, list);
        assertEquals(list.hashCode(), list2.hashCode());
        list.add(0);
        list.add(1);
        list2.add(1);
        list2.add(0);
        assertTrue(!list.equals(list2));
        list2.removeValue(1);
        list2.add(1);
        assertEquals(list, list2);
        assertEquals(list2, list);
        list2.add(2);
        assertTrue(!list.equals(list2));
        assertTrue(!list2.equals(list));
    }

    public void testGet() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            list.add(j);
        }
        for (int j = 0; j < 1001; j++)
        {
            try
            {
                assertEquals(j, list.get(j));
                if (j == 1000)
                {
                    fail("should have gotten exception");
                }
            }
            catch (IndexOutOfBoundsException e)
            {
                if (j != 1000)
                {
                    fail("unexpected IndexOutOfBoundsException");
                }
            }
        }
    }

    public void testIndexOf() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            list.add(j / 2);
        }
        for (int j = 0; j < 1000; j++)
        {
            if (j < 500)
            {
                assertEquals(j * 2, list.indexOf(j));
            }
            else
            {
                assertEquals(-1, list.indexOf(j));
            }
        }
    }

    public void testIsEmpty() {
        IntList list1 = new IntList();
        IntList list2 = new IntList(1000);
        IntList list3 = new IntList(list1);

        assertTrue(list1.isEmpty());
        assertTrue(list2.isEmpty());
        assertTrue(list3.isEmpty());
        list1.add(1);
        list2.add(2);
        list3 = new IntList(list2);
        assertTrue(!list1.isEmpty());
        assertTrue(!list2.isEmpty());
        assertTrue(!list3.isEmpty());
        list1.clear();
        list2.remove(0);
        list3.removeValue(2);
        assertTrue(list1.isEmpty());
        assertTrue(list2.isEmpty());
        assertTrue(list3.isEmpty());
    }

    public void testLastIndexOf() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            list.add(j / 2);
        }
        for (int j = 0; j < 1000; j++)
        {
            if (j < 500)
            {
                assertEquals(1 + j * 2, list.lastIndexOf(j));
            }
            else
            {
                assertEquals(-1, list.indexOf(j));
            }
        }
    }

    public void testRemove() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            list.add(j);
        }
        for (int j = 0; j < 1000; j++)
        {
            assertEquals(j, list.remove(0));
            assertEquals(999 - j, list.size());
        }
        for (int j = 0; j < 1000; j++)
        {
            list.add(j);
        }
        for (int j = 0; j < 1000; j++)
        {
            assertEquals(999 - j, list.remove(999 - j));
            assertEquals(999 - j, list.size());
        }
        try
        {
            list.remove(0);
            fail("should have caught IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {

            // as expected
        }
    }

    public void testRemoveValue() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            list.add(j / 2);
        }
        for (int j = 0; j < 1000; j++)
        {
            if (j < 500)
            {
                assertTrue(list.removeValue(j));
                assertTrue(list.removeValue(j));
            }
            assertTrue(!list.removeValue(j));
        }
    }

    public void testRemoveAll() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            list.add(j);
        }
        IntList listCopy = new IntList(list);
        IntList listOdd  = new IntList();
        IntList listEven = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            if (j % 2 == 0)
            {
                listEven.add(j);
            }
            else
            {
                listOdd.add(j);
            }
        }
        list.removeAll(listEven);
        assertEquals(list, listOdd);
        list.removeAll(listOdd);
        assertTrue(list.isEmpty());
        listCopy.removeAll(listOdd);
        assertEquals(listCopy, listEven);
        listCopy.removeAll(listEven);
        assertTrue(listCopy.isEmpty());
    }

    public void testRetainAll() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            list.add(j);
        }
        IntList listCopy = new IntList(list);
        IntList listOdd  = new IntList();
        IntList listEven = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            if (j % 2 == 0)
            {
                listEven.add(j);
            }
            else
            {
                listOdd.add(j);
            }
        }
        list.retainAll(listOdd);
        assertEquals(list, listOdd);
        list.retainAll(listEven);
        assertTrue(list.isEmpty());
        listCopy.retainAll(listEven);
        assertEquals(listCopy, listEven);
        listCopy.retainAll(listOdd);
        assertTrue(listCopy.isEmpty());
    }

    public void testSet() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            list.add(j);
        }
        for (int j = 0; j < 1001; j++)
        {
            try
            {
                list.set(j, j + 1);
                if (j == 1000)
                {
                    fail("Should have gotten exception");
                }
                assertEquals(j + 1, list.get(j));
            }
            catch (IndexOutOfBoundsException e)
            {
                if (j != 1000)
                {
                    fail("premature exception");
                }
            }
        }
    }

    public void testSize() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            assertEquals(j, list.size());
            list.add(j);
            assertEquals(j + 1, list.size());
        }
        for (int j = 0; j < 1000; j++)
        {
            assertEquals(1000 - j, list.size());
            list.removeValue(j);
            assertEquals(999 - j, list.size());
        }
    }

    public void testToArray() {
        IntList list = new IntList();

        for (int j = 0; j < 1000; j++)
        {
            list.add(j);
        }
        int[] a1 = list.toArray();

        assertEquals(a1.length, list.size());
        for (int j = 0; j < 1000; j++)
        {
            assertEquals(a1[ j ], list.get(j));
        }
        int[] a2 = new int[ list.size() ];
        int[] a3 = list.toArray(a2);

        assertSame(a2, a3);
        for (int j = 0; j < 1000; j++)
        {
            assertEquals(a2[ j ], list.get(j));
        }
        int[] aShort = new int[ list.size() - 1 ];
        int[] aLong  = new int[ list.size() + 1 ];
        int[] a4     = list.toArray(aShort);
        int[] a5     = list.toArray(aLong);

        assertTrue(a4 != aShort);
        assertTrue(a5 != aLong);
        assertEquals(a4.length, list.size());
        for (int j = 0; j < 1000; j++)
        {
            assertEquals(a3[ j ], list.get(j));
        }
        assertEquals(a5.length, list.size());
        for (int j = 0; j < 1000; j++)
        {
            assertEquals(a5[ j ], list.get(j));
        }
    }
}
