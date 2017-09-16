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

import java.io.InputStream;



public interface PaintStyle {
    /**
     * The PaintStyle can be modified by secondary sources, e.g. the attributes in the preset shapes.
     * These modifications need to be taken into account when the final color is determined.
     */
    enum PaintModifier {
        /** don't use any paint/fill */
        NONE,
        /** use the paint/filling as-is */
        NORM,
        /** lighten the paint/filling */
        LIGHTEN,
        /** lighten (... a bit less) the paint/filling */
        LIGHTEN_LESS,
        /** darken the paint/filling */
        DARKEN,
        /** darken (... a bit less) the paint/filling */
        DARKEN_LESS
    }

    interface SolidPaint extends PaintStyle {
        ColorStyle getSolidColor();
    }

    interface GradientPaint extends PaintStyle {
        enum GradientType { linear, circular, shape }
        
        /**
         * @return the angle of the gradient
         */
        double getGradientAngle();
        ColorStyle[] getGradientColors();
        float[] getGradientFractions();
        boolean isRotatedWithShape();
        GradientType getGradientType();
    }    
    
    interface TexturePaint extends PaintStyle {
        /**
         * @return the raw image stream
         */
        InputStream getImageData();

        /**
         * @return the content type of the image data
         */
        String getContentType();
        
        /**
         * @return the alpha mask in percents [0..100000]
         */
        int getAlpha();
    }
}
