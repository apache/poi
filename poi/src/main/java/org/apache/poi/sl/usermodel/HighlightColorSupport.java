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

package org.apache.poi.sl.usermodel;

import java.awt.Color;

import org.apache.poi.common.usermodel.fonts.FontGroup;
import org.apache.poi.common.usermodel.fonts.FontInfo;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.util.Internal;

/**
 * An interface to provide support for get/set of highlight color in <code>XLSFTextRuns</code> instances.
 * Spec Ref: 21.1.2.3.4 (ECMA-376-1 5th ed. Ecma Office Open XML Part 1 - Fundamentals And Markup Language Reference.pdf)
 *
 * @since POI 5.2.4
 */
@SuppressWarnings({"unused","java:S1452"})
public interface HighlightColorSupport
{

    /**
     * Returns the font highlight (background) color for this text run.
     * This returns a {@link SolidPaint}, or null if no highlight is set.
     *
     * @return The font highlight (background) colour associated with the run, null if no highlight.
     *
     * @see org.apache.poi.sl.draw.DrawPaint#getPaint(java.awt.Graphics2D, PaintStyle)
     * @see SolidPaint#getSolidColor()
     * @since POI 5.2.4
     */
    PaintStyle getHighlightColor();

    /**
     * Set the highlight (background) color for this text run.
     *
     * @param color The highlight (background) color to set.
     *
     * @see org.apache.poi.sl.draw.DrawPaint#createSolidPaint(Color)
     * @since POI 5.2.4
     */
    void setHighlightColor(final PaintStyle color);

    /**
     * Sets the font highlight (background) color for this text run - convenience function
     *
     * @param color The highlight (background) color to set.
     * @since POI 5.2.4
     */
    void setHighlightColor(final Color color);

}
