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

package org.apache.poi.xslf.usermodel;

import java.awt.*;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

/**
 * a renderable text fragment
*/
class TextFragment {
    final TextLayout _layout;
    final AttributedString _str;

    TextFragment(TextLayout layout, AttributedString str){
        _layout = layout;
        _str = str;
    }

    void draw(Graphics2D graphics, double x, double y){
        if(_str == null) {
            return;
        }

        double yBaseline = y + _layout.getAscent();

        Integer textMode = (Integer)graphics.getRenderingHint(XSLFRenderingHint.TEXT_RENDERING_MODE);
        if(textMode != null && textMode == XSLFRenderingHint.TEXT_AS_SHAPES){
            _layout.draw(graphics, (float)x, (float)yBaseline);
        } else {
            graphics.drawString(_str.getIterator(), (float)x, (float)yBaseline );
        }
    }

    /**
     * @return full height of this text run which is sum of ascent, descent and leading
     */
    public float getHeight(){
        double h = Math.ceil(_layout.getAscent()) + Math.ceil(_layout.getDescent()) + _layout.getLeading();
        return (float)h;
    }

    /**
     *
     * @return width if this text run
     */
    public float getWidth(){
        return _layout.getAdvance();
    }

    /**
     *
     * @return the string to be painted
     */
    public String getString(){
        if(_str == null) return "";

        AttributedCharacterIterator it = _str.getIterator();
         StringBuffer buf = new StringBuffer();
         for (char c = it.first(); c != it.DONE; c = it.next()) {
             buf.append(c);
         }
        return buf.toString();
    }

    @Override
    public String toString(){
        return "[" + getClass().getSimpleName() + "] " + getString();
    }
}
