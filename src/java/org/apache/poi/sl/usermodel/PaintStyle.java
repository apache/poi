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

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.io.InputStream;
import java.util.List;


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

    enum FlipMode {
        /** not flipped/mirrored */
        NONE,
        /** flipped/mirrored/duplicated along the x axis */
        X,
        /** flipped/mirrored/duplicated along the y axis */
        Y,
        /** flipped/mirrored/duplicated along the x and y axis */
        XY
    }

    enum TextureAlignment {
        BOTTOM("b"),
        BOTTOM_LEFT("bl"),
        BOTTOM_RIGHT("br"),
        CENTER("ctr"),
        LEFT("l"),
        RIGHT("r"),
        TOP("t"),
        TOP_LEFT("tl"),
        TOP_RIGHT("tr");

        private final String ooxmlId;

        TextureAlignment(String ooxmlId) {
            this.ooxmlId = ooxmlId;
        }

        public static TextureAlignment fromOoxmlId(String ooxmlId) {
            for (TextureAlignment ta : values()) {
                if (ta.ooxmlId.equals(ooxmlId)) {
                    return ta;
                }
            }
            return null;
        }
    }


    interface SolidPaint extends PaintStyle {
        ColorStyle getSolidColor();
    }

    interface GradientPaint extends PaintStyle {
        enum GradientType { linear, circular, rectangular, shape }

        /**
         * @return the angle of the gradient
         */
        double getGradientAngle();
        ColorStyle[] getGradientColors();
        float[] getGradientFractions();
        boolean isRotatedWithShape();
        GradientType getGradientType();

        default Insets2D getFillToInsets() {
            return null;
        }
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

        /**
         * @return {@code true}, if the rotation of the shape is also applied to the texture paint
         */
        default boolean isRotatedWithShape() { return true; }

        /**
         * @return the dimensions of the tiles in percent of the shape dimensions
         * or {@code null} if no scaling is applied
         */
        default Dimension2D getScale() { return null; }

        /**
         * @return the offset of the tiles in points or {@code null} if there's no offset
         */
        default Point2D getOffset() { return null; }

        /**
         * @return the flip/mirroring/duplication mode
         */
        default FlipMode getFlipMode() { return FlipMode.NONE; }


        default TextureAlignment getAlignment() { return null; }

        /**
         * Specifies the portion of the blip or image that is used for the fill.<p>
         *
         * Each edge of the image is defined by a percentage offset from the edge of the bounding box.
         * A positive percentage specifies an inset and a negative percentage specifies an outset.<p>
         *
         * The percentage are ints based on 100000, so 100% = 100000.<p>
         *
         * So, for example, a left offset of 25% specifies that the left edge of the image is located
         * to the right of the bounding box's left edge by 25% of the bounding box's width.
         *
         * @return the cropping insets of the source image
         */
        default Insets2D getInsets() {
            return null;
        }

        /**
         * The stretch specifies the edges of a fill rectangle.<p>
         *
         * Each edge of the fill rectangle is defined by a perentage offset from the corresponding edge
         * of the picture's bounding box. A positive percentage specifies an inset and a negative percentage
         * specifies an outset.<p>
         *
         * The percentage are ints based on 100000, so 100% = 100000.
         *
         * @return the stretching in the destination image
         */
        default Insets2D getStretch() {
            return null;
        }


        /**
         * For pattern images, the duo tone defines the black/white pixel color replacement
         */
        default List<ColorStyle> getDuoTone() {
            return null;
        }

    }
}
