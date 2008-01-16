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

package org.apache.poi.ss.usermodel;

public interface Palette {

    /**
     * Retrieves the color at a given index
     *
     * @param index the palette index, between 0x8 to 0x40 inclusive
     * @return the color, or null if the index is not populated
     */
    Color getColor(short index);

    /**
     * Finds the first occurance of a given color
     *
     * @param red the RGB red component, between 0 and 255 inclusive
     * @param green the RGB green component, between 0 and 255 inclusive
     * @param blue the RGB blue component, between 0 and 255 inclusive
     * @return the color, or null if the color does not exist in this palette
     */
    Color findColor(byte red, byte green, byte blue);

    /**
     * Finds the closest matching color in the custom palette.  The
     * method for finding the distance between the colors is fairly
     * primative.
     *
     * @param red   The red component of the color to match.
     * @param green The green component of the color to match.
     * @param blue  The blue component of the color to match.
     * @return  The closest color or null if there are no custom
     *          colors currently defined.
     */
    Color findSimilarColor(byte red, byte green, byte blue);

    /**
     * Sets the color at the given offset
     *
     * @param index the palette index, between 0x8 to 0x40 inclusive
     * @param red the RGB red component, between 0 and 255 inclusive
     * @param green the RGB green component, between 0 and 255 inclusive
     * @param blue the RGB blue component, between 0 and 255 inclusive
     */
    void setColorAtIndex(short index, byte red, byte green, byte blue);

    /**
     * Adds a new color into an empty color slot.
     * @param red       The red component
     * @param green     The green component
     * @param blue      The blue component
     *
     * @return  The new custom color.
     *
     * @throws RuntimeException if there are more more free color indexes.
     */
    Color addColor(byte red, byte green, byte blue);

}