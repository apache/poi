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

    public interface SolidPaint extends PaintStyle {
        ColorStyle getSolidColor();
    }

    public interface GradientPaint extends PaintStyle {
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
    
    public interface TexturePaint extends PaintStyle {
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
