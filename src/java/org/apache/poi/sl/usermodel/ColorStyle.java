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


public interface ColorStyle {
    Color getColor();
    
    /**
     * the opacity as expressed by a percentage value
     *
     * @return  opacity in percents in the range [0..100000]
     * or -1 if the value is not set
     */
    int getAlpha();
    
    /**
     * the luminance shift as expressed by a percentage relative to the input color
     *
     * @return  luminance shift in percents in the range [0..100000]
     * or -1 if the value is not set
     */
    int getLumOff();
    
    /**
     * the luminance as expressed by a percentage relative to the input color
     *
     * @return  luminance in percents in the range [0..100000]
     * or -1 if the value is not set
     */
    int getLumMod();
    
    /**
     * specifies a darker version of its input color.
     * A 10% shade is 10% of the input color combined with 90% black.
     * 
     * @return the value of the shade specified as percents in the range [0..100000]
     * with 0% indicating minimal shade and 100% indicating maximum
     * or -1 if the value is not set
     */
    int getShade();

    /**
     * specifies a lighter version of its input color.
     * A 10% tint is 10% of the input color combined with 90% white.
     *
     * @return the value of the tint specified as percents in the range [0..100000]
     * with 0% indicating minimal tint and 100% indicating maximum
     * or -1 if the value is not set
     */
    int getTint();
}
