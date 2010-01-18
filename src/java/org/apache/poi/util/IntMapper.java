
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

import java.util.*;

/**
 * A List of objects that are indexed AND keyed by an int; also allows for getting
 * the index of a value in the list
 *
 * <p>I am happy is someone wants to re-implement this without using the
 * internal list and hashmap. If so could you please make sure that
 * you can add elements half way into the list and have the value-key mappings
 * update</p>
 *
 *
 * @author Jason Height
 */

public class IntMapper<T>
{
  private List<T> elements;
  private Map<T,Integer> valueKeyMap;

  private static final int _default_size = 10;

    /**
     * create an IntMapper of default size
     */

    public IntMapper()
    {
        this(_default_size);
    }

    public IntMapper(final int initialCapacity)
    {
        elements = new ArrayList<T>(initialCapacity);
        valueKeyMap = new HashMap<T,Integer>(initialCapacity);
    }

    /**
     * Appends the specified element to the end of this list
     *
     * @param value element to be appended to this list.
     *
     * @return true (as per the general contract of the Collection.add
     *         method).
     */
    public boolean add(final T value)
    {
      int index = elements.size();
      elements.add(value);
      valueKeyMap.put(value, index);
      return true;
    }

    public int size() {
      return elements.size();
    }

    public T get(int index) {
      return elements.get(index);
    }

    public int getIndex(T o) {
      Integer i = valueKeyMap.get(o);
      if (i == null)
        return -1;
      return i.intValue();
    }

    public Iterator<T> iterator() {
      return elements.iterator();
    }
}   // end public class IntMapper

