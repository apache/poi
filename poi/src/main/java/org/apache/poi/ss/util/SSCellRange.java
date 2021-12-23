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

package org.apache.poi.ss.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.util.Internal;

/**
 * For POI internal use only
 */
@Internal
public final class SSCellRange<K extends Cell> implements CellRange<K> {

    private final int _height;
    private final int _width;
    private final K[] _flattenedArray;
    private final int _firstRow;
    private final int _firstColumn;

    private SSCellRange(int firstRow, int firstColumn, int height, int width, K[] flattenedArray) {
        _firstRow = firstRow;
        _firstColumn = firstColumn;
        _height = height;
        _width = width;
        _flattenedArray = flattenedArray.clone();
    }

    public static <B extends Cell> SSCellRange<B> create(int firstRow, int firstColumn, int height, int width, List<B> flattenedList, Class<B> cellClass) {
        int nItems = flattenedList.size();
        if (height * width != nItems) {
            throw new IllegalArgumentException("Array size mismatch.");
        }

        @SuppressWarnings("unchecked")
        B[] flattenedArray = (B[]) Array.newInstance(cellClass, nItems);
        flattenedList.toArray(flattenedArray);
        return new SSCellRange<>(firstRow, firstColumn, height, width, flattenedArray);
    }

    @Override
    public int getHeight() {
        return _height;
    }
    @Override
    public int getWidth() {
        return _width;
    }
    @Override
    public int size() {
        return _height*_width;
    }

    @Override
    public String getReferenceText() {
        CellRangeAddress cra = new CellRangeAddress(_firstRow, _firstRow+_height-1, _firstColumn, _firstColumn+_width-1);
        return cra.formatAsString();
    }

    @Override
    public K getTopLeftCell() {
        return _flattenedArray[0];
    }

    @Override
    public K getCell(int relativeRowIndex, int relativeColumnIndex) {
        if (relativeRowIndex < 0 || relativeRowIndex >= _height) {
            throw new ArrayIndexOutOfBoundsException("Specified row " + relativeRowIndex
                    + " is outside the allowable range (0.." + (_height-1) + ").");
        }
        if (relativeColumnIndex < 0 || relativeColumnIndex >= _width) {
            throw new ArrayIndexOutOfBoundsException("Specified colummn " + relativeColumnIndex
                    + " is outside the allowable range (0.." + (_width-1) + ").");
        }
        int flatIndex = _width * relativeRowIndex + relativeColumnIndex;
        return _flattenedArray[flatIndex];
    }
    @Override
    public K[] getFlattenedCells() {
        return _flattenedArray.clone();
    }

    @Override
    public K[][] getCells() {
        Class<?> itemCls = _flattenedArray.getClass();
        @SuppressWarnings("unchecked")
        K[][] result = (K[][]) Array.newInstance(itemCls, _height);
        itemCls = itemCls.getComponentType();
        for (int r=_height-1; r>=0; r--) {
            @SuppressWarnings("unchecked")
            K[] row = (K[]) Array.newInstance(itemCls, _width);
            int flatIndex = _width * r;
            System.arraycopy(_flattenedArray, flatIndex, row, 0, _width);
        }
        return result;
    }

    @Override
    public Iterator<K> iterator() {
        return Stream.of(_flattenedArray).iterator();
    }

    /**
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<K> spliterator() {
        return Stream.of(_flattenedArray).spliterator();
    }
}
