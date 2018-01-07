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
package org.apache.poi.sl.usermodel;

/**
 * Specifies possible rectangle alignment types.
 * See org.openxmlformats.schemas.drawingml.x2006.main.STRectAlignment
 * 
 * @see org.apache.poi.sl.draw.binding.STRectAlignment
 */
public enum RectAlign {
    /** Top-Left rectangle alignment */
    TOP_LEFT("tl"),

    /** Top rectangle alignment */
    TOP("t"),

    /** Top-Right rectangle alignment */
    TOP_RIGHT("tr"),

    /** Left rectangle alignment */
    LEFT("l"),

    /** Center rectangle alignment */
    CENTER("ctr"),

    /** Right rectangle alignment */
    RIGHT("r"),

    /** Bottom-Left rectangle alignment */
    BOTTOM_LEFT("bl"),

    /** Bottom rectangle alignment */
    BOTTOM("b"),

    /** Bottom-Right rectangle alignment */
    BOTTOM_RIGHT("br");

    /** The corresponding xml enum value */
    private final String dir;

    private RectAlign(String dir) {
        this.dir = dir;
    }


    /**
     * The string representation,
     * which corresponds to the internal XML enum value
     */
    @Override
    public String toString() {
        return dir;
    }

}

/* ************************************************************************** */
