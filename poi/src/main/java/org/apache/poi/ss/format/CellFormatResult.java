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
package org.apache.poi.ss.format;

import java.awt.Color;

/**
 * This object contains the result of applying a cell format or cell format part
 * to a value.
 *
 * @author Ken Arnold, Industrious Media LLC
 * @see CellFormatPart#apply(Object)
 * @see CellFormat#apply(Object)
 */
public class CellFormatResult {
    /**
     * This is <tt>true</tt> if no condition was given that applied to the
     * value, or if the condition is satisfied.  If a condition is relevant, and
     * when applied the value fails the test, this is <tt>false</tt>.
     */
    public final boolean applies;

    /** The resulting text.  This will never be <tt>null</tt>. */
    public final String text;

    /**
     * The color the format sets, or <tt>null</tt> if the format sets no color.
     * This will always be <tt>null</tt> if {@link #applies} is <tt>false</tt>.
     */
    public final Color textColor;

    /**
     * Creates a new format result object.
     *
     * @param applies   The value for {@link #applies}.
     * @param text      The value for {@link #text}.
     * @param textColor The value for {@link #textColor}.
     */
    public CellFormatResult(boolean applies, String text, Color textColor) throws IllegalArgumentException {
        this.applies = applies;
        if (text == null)
            throw new IllegalArgumentException("CellFormatResult text may not be null");
        this.text = text;
        this.textColor = (applies ? textColor : null);
    }
}