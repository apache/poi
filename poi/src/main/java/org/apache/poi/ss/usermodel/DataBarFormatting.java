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

/**
 * High level representation for the DataBar Formatting 
 *  component of Conditional Formatting settings
 */
public interface DataBarFormatting {
    /**
     * Is the bar drawn from Left-to-Right, or from
     *  Right-to-Left
     */
    boolean isLeftToRight();
    /**
     * Control if the bar is drawn from Left-to-Right,
     *  or from Right-to-Left
     */
    void setLeftToRight(boolean ltr);
    
    /**
     * Should Icon + Value be displayed, or only the Icon?
     */
    boolean isIconOnly();
    /**
     * Control if only the Icon is shown, or Icon + Value
     */
    void setIconOnly(boolean only);
    
    /**
     * How much of the cell width, in %, should be given to
     *  the min value?
     */
    int getWidthMin();
    void setWidthMin(int width);
    
    /**
     * How much of the cell width, in %, should be given to
     *  the max value?
     */
    int getWidthMax();
    void setWidthMax(int width);
    
    Color getColor();
    void setColor(Color color);
    
    /**
     * The threshold that defines "everything from here down is minimum"
     */
    ConditionalFormattingThreshold getMinThreshold();
    /**
     * The threshold that defines "everything from here up is maximum"
     */
    ConditionalFormattingThreshold getMaxThreshold();
}
