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

package org.apache.poi.xdgf.usermodel.section;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

/**
 * An iterator used to iterate over the base and master items
 *
 * @param <T>
 */
public class CombinedIterable<T> implements Iterable<T> {

    final SortedMap<Long, T> _baseItems;
    final SortedMap<Long, T> _masterItems;

    public CombinedIterable(SortedMap<Long, T> baseItems,
            SortedMap<Long, T> masterItems) {
        _baseItems = baseItems;
        _masterItems = masterItems;
    }

    @Override
    public Iterator<T> iterator() {

        final Iterator<Entry<Long, T>> vmasterI;

        if (_masterItems != null) {
            vmasterI = _masterItems.entrySet().iterator();
        } else {
            final Set<Entry<Long, T>> empty = Collections.emptySet();
            vmasterI = empty.iterator();
        }

        return new Iterator<T>() {

            Long lastI = Long.MIN_VALUE;

            Entry<Long, T> currentBase;
            Entry<Long, T> currentMaster;

            // grab the iterator for both
            Iterator<Entry<Long, T>> baseI = _baseItems.entrySet().iterator();
            Iterator<Entry<Long, T>> masterI = vmasterI;

            @Override
            public boolean hasNext() {
                return currentBase != null || currentMaster != null
                        || baseI.hasNext() || masterI.hasNext();
            }

            @Override
            public T next() {

                // TODO: This seems far more complex than it needs to be

                long baseIdx = Long.MAX_VALUE;
                long masterIdx = Long.MAX_VALUE;

                if (currentBase == null) {
                    while (baseI.hasNext()) {
                        currentBase = baseI.next();
                        if (currentBase.getKey() > lastI) {
                            baseIdx = currentBase.getKey();
                            break;
                        }
                    }
                } else {
                    baseIdx = currentBase.getKey();
                }

                if (currentMaster == null) {
                    while (masterI.hasNext()) {
                        currentMaster = masterI.next();
                        if (currentMaster.getKey() > lastI) {
                            masterIdx = currentMaster.getKey();
                            break;
                        }
                    }
                } else {
                    masterIdx = currentMaster.getKey();
                }

                T val;

                if (currentBase != null) {

                    if (baseIdx <= masterIdx) {
                        lastI = baseIdx;
                        val = currentBase.getValue();

                        // discard master if same as base
                        if (masterIdx == baseIdx) {
                            currentMaster = null;
                        }

                        currentBase = null;

                    } else {
                        lastI = masterIdx;
                        val = (currentMaster != null) ? currentMaster.getValue() : null;
                        currentMaster = null;
                    }

                } else if (currentMaster != null) {
                    lastI = currentMaster.getKey();
                    val = currentMaster.getValue();

                    currentMaster = null;
                } else {
                    throw new NoSuchElementException();
                }

                return val;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
