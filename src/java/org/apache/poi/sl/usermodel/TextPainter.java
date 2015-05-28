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

import java.awt.RenderingHints;
import java.text.AttributedString;

/**
 * Common parent for painting Text into a Graphics2D object
 *  for rendering
 */
public interface TextPainter {
    public static final Key KEY_FONTFALLBACK = new Key(50, "Font fallback map");
    public static final Key KEY_FONTMAP = new Key(51, "Font map");

    public static class TextElement {
        public AttributedString _text;
        public int _textOffset;
        public AttributedString _bullet;
        public int _bulletOffset;
        public int _align;
        public float ascent, descent;
        public float advance;
        public int textStartIndex, textEndIndex;
    }

    public static class Key extends RenderingHints.Key {
        String description;

        public Key(int paramInt, String paramString) {
            super(paramInt);
            this.description = paramString;
        }

        public final int getIndex() {
            return intKey();
        }

        public final String toString() {
            return this.description;
        }

        public boolean isCompatibleValue(Object paramObject) {
            return true;
        }
    }
}
