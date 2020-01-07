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

package org.apache.poi.xddf.usermodel.chart;

import org.apache.poi.util.Beta;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumVal;

@Beta
public interface XDDFNumericalDataSource<T extends Number> extends XDDFDataSource<T> {
    String getFormatCode();

    void setFormatCode(String formatCode);

    @Override
    default boolean isLiteral() {
        return false;
    }

    /**
     * @since POI 4.1.2
     */
    @Internal
    default void fillNumericalCache(CTNumData cache) {
        String formatCode = getFormatCode();
        if (formatCode == null) {
            if (cache.isSetFormatCode()) {
                cache.unsetFormatCode();
            }
        } else {
            cache.setFormatCode(formatCode);
        }
        cache.setPtArray(null); // unset old values
        final int numOfPoints = getPointCount();
        int effectiveNumOfPoints = 0;
        for (int i = 0; i < numOfPoints; ++i) {
            Object value = getPointAt(i);
            if (value != null) {
                CTNumVal ctNumVal = cache.addNewPt();
                ctNumVal.setIdx(i);
                ctNumVal.setV(value.toString());
                effectiveNumOfPoints++;
            }
        }
        if (effectiveNumOfPoints == 0) {
            cache.unsetPtCount();
        } else {
            if (cache.isSetPtCount()) {
                cache.getPtCount().setVal(numOfPoints);
            } else {
                cache.addNewPtCount().setVal(numOfPoints);
            }
        }
    }
}
