/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.ss.usermodel.charts;

import org.apache.poi.util.Removal;

/**
 * Represents data model of the charts.
 *
 * @param <T> type of points the data source contents
 * @deprecated use XDDFDataSource instead
 */
@Deprecated
@Removal(version="4.2")
public interface ChartDataSource<T> {

    /**
     * Return number of points contained by data source.
     *
     * @return number of points contained by data source
     */
    int getPointCount();

    /**
     * Returns point value at specified index.
     *
     * @param index index to value from
     * @return point value at specified index.
     * @throws {@code IndexOutOfBoundsException} if index
     *                parameter not in range {@code 0 <= index <= pointCount}
     */
    T getPointAt(int index);

    /**
     * Returns {@code true} if charts data source is valid cell range.
     *
     * @return {@code true} if charts data source is valid cell range
     */
    boolean isReference();

    /**
     * Returns {@code true} if data source points should be treated as numbers.
     *
     * @return {@code true} if data source points should be treated as numbers
     */
    boolean isNumeric();

    /**
     * Returns formula representation of the data source. It is only applicable
     * for data source that is valid cell range.
     *
     * @return formula representation of the data source
     * @throws {@code UnsupportedOperationException} if the data source is not a
     *                reference.
     */
    String getFormulaString();
}
