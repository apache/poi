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

package org.apache.poi.ss.formula;

import java.util.EmptyStackException;

import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.util.Internal;

/**
 * Minimal, non-synchronized LIFO stack of {@link ValueEval}s backed by a growable
 * array. It is the allocation-free, single-threaded counterpart of the
 * {@link java.util.Stack} used by the classic evaluator's insurance loop: the
 * backing array is reused across evaluations so the hot standalone row loop does
 * not allocate per evaluation.
 */
@Internal
final class ValueEvalStack {

    private ValueEval[] _values;
    private int _size;

    ValueEvalStack() {
        this(16);
    }

    ValueEvalStack(int initialCapacity) {
        _values = new ValueEval[initialCapacity];
    }

    void push(ValueEval value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot push null onto the evaluation stack");
        }
        if (_size == _values.length) {
            grow();
        }
        _values[_size++] = value;
    }

    ValueEval pop() {
        int index = _size - 1;
        if (index < 0) {
            throw new EmptyStackException();
        }
        ValueEval value = _values[index];
        // avoid retaining a reference after the value has been consumed
        _values[index] = null;
        _size = index;
        return value;
    }

    ValueEval peek() {
        int index = _size - 1;
        if (index < 0) {
            throw new EmptyStackException();
        }
        return _values[index];
    }

    int size() {
        return _size;
    }

    boolean isEmpty() {
        return _size == 0;
    }

    ValueEval get(int index) {
        if (index < 0 || index >= _size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of range 0.." + (_size - 1));
        }
        return _values[index];
    }

    void clear() {
        int size = _size;
        for (int i = 0; i < size; i++) {
            _values[i] = null;
        }
        _size = 0;
    }

    private void grow() {
        int newCapacity = _values.length * 2;
        if (newCapacity < 32) {
            newCapacity = 32;
        }
        ValueEval[] newValues = new ValueEval[newCapacity];
        System.arraycopy(_values, 0, newValues, 0, _size);
        _values = newValues;
    }
}