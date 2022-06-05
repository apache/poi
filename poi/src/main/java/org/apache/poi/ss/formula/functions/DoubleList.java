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

package org.apache.poi.ss.formula.functions;

import java.util.Arrays;

final class DoubleList {
    static final double[] EMPTY_DOUBLE_ARRAY = {};

    private double[] _array;
    private int _count;

    public DoubleList() {
        _array = new double[8];
        _count = 0;
    }

    public double[] toArray() {
        return _count < 1 ? EMPTY_DOUBLE_ARRAY : Arrays.copyOf(_array, _count);
    }

    private void ensureCapacity(int reqSize) {
        if (reqSize > _array.length) {
            int newSize = reqSize * 3 / 2; // grow with 50% extra
            _array = Arrays.copyOf(_array, newSize);
        }
    }

    public void add(double value) {
        ensureCapacity(_count + 1);
        _array[_count] = value;
        _count++;
    }

    public int getLength() {
        return _count;
    }
}
