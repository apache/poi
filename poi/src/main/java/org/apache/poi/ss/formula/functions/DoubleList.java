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
}
