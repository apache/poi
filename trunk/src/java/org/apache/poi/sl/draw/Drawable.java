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

package org.apache.poi.sl.draw;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.poi.util.Internal;


public interface Drawable {
    class DrawableHint extends RenderingHints.Key {
        protected DrawableHint(int id) {
            super(id);
        }
        
        public boolean isCompatibleValue(Object val) {
            return true;
        }
        
        public String toString() {
            switch (intKey()) {
            case 1: return "DRAW_FACTORY";
            case 2: return "GROUP_TRANSFORM";
            case 3: return "IMAGE_RENDERER";
            case 4: return "TEXT_RENDERING_MODE";
            case 5: return "GRADIENT_SHAPE";
            case 6: return "PRESET_GEOMETRY_CACHE";
            case 7: return "FONT_HANDLER";
            case 8: return "FONT_FALLBACK";
            case 9: return "FONT_MAP";
            case 10: return "GSAVE";
            case 11: return "GRESTORE";
            default: return "UNKNOWN_ID "+intKey();
            }
        }
    }
    
    /**
     * {@link DrawFactory} which will be used to draw objects into this graphics context
     */
    DrawableHint DRAW_FACTORY = new DrawableHint(1);

    /**
     * Key will be internally used to store affine transformation temporarily within group shapes
     */
    @Internal
    DrawableHint GROUP_TRANSFORM = new DrawableHint(2);

    /**
     * Use a custom image renderer of an instance of {@link ImageRenderer}
     */
    DrawableHint IMAGE_RENDERER = new DrawableHint(3);

    /**
     *  how to render text:
     *
     *  {@link #TEXT_AS_CHARACTERS} (default) means to draw via
     *   {@link java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float)}.
     *   This mode draws text as characters. Use it if the target graphics writes the actual
     *   character codes instead of glyph outlines (PDFGraphics2D, SVGGraphics2D, etc.)
     *
     *   {@link #TEXT_AS_SHAPES} means to render via
     *   {@link java.awt.font.TextLayout#draw(java.awt.Graphics2D, float, float)}.
     *   This mode draws glyphs as shapes and provides some advanced capabilities such as
     *   justification and font substitution. Use it if the target graphics is an image.
     *
     */
    DrawableHint TEXT_RENDERING_MODE = new DrawableHint(4);

    /**
     * PathGradientPaint needs the shape to be set.
     * It will be achieved through setting it in the rendering hints
     */
    DrawableHint GRADIENT_SHAPE = new DrawableHint(5);


    /**
     * Internal key for caching the preset geometries
     */
    DrawableHint PRESET_GEOMETRY_CACHE = new DrawableHint(6);
    
    /**
     * draw text via {@link java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float)}
     */
    int TEXT_AS_CHARACTERS = 1;

    /**
     * draw text via {@link java.awt.font.TextLayout#draw(java.awt.Graphics2D, float, float)}
     */
    int TEXT_AS_SHAPES = 2;

    /**
     * Use this object to resolve unknown / missing fonts when rendering slides
     */
    DrawableHint FONT_HANDLER = new DrawableHint(7);
    DrawableHint FONT_FALLBACK = new DrawableHint(8);
    DrawableHint FONT_MAP = new DrawableHint(9);
    
    DrawableHint GSAVE = new DrawableHint(10);
    DrawableHint GRESTORE = new DrawableHint(11);
    
    
    
    /**
     * Apply 2-D transforms before drawing this shape. This includes rotation and flipping.
     *
     * @param graphics the graphics whos transform matrix will be modified
     */
    void applyTransform(Graphics2D graphics);
    
    /**
     * Draw this shape into the supplied canvas
     *
     * @param graphics the graphics to draw into
     */
    void draw(Graphics2D graphics);
    
    /**
     * draw any content within this shape (image, text, etc.).
     *
     * @param graphics the graphics to draw into
     */
    void drawContent(Graphics2D graphics);    
}
