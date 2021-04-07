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

package org.apache.poi.ss.usermodel;

import org.apache.poi.hssf.record.cf.Threshold;

/**
 * High level representation for the Color Scale / Colour Scale /
 *  Color Gradient Formatting component of Conditional Formatting settings
 */
public interface ColorScaleFormatting {
    /**
     * How many control points should be used to map 
     *  the colours? Normally 2 or 3
     */
    int getNumControlPoints();
    /**
     * Sets the number of control points to use to map
     *  the colours. Should normally be 2 or 3.
     * <p>After updating, you need to ensure that the
     *  {@link Threshold} count and Color count match
     */
    void setNumControlPoints(int num);

    /**
     * Gets the list of colours that are interpolated
     *  between.
     */
    Color[] getColors();
    /**
     * Sets the list of colours that are interpolated
     *  between. The number must match {@link #getNumControlPoints()}
     */
    void setColors(Color[] colors);
    
    /**
     * Gets the list of thresholds
     */
    ConditionalFormattingThreshold[] getThresholds();
    /**
     * Sets the of thresholds. The number must match
     *  {@link #getNumControlPoints()}
     */
    void setThresholds(ConditionalFormattingThreshold[] thresholds);
    /**
     * Creates a new, empty Threshold
     */
    ConditionalFormattingThreshold createThreshold();
}
