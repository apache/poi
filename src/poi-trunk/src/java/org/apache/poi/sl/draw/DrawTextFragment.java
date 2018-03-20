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
import java.awt.font.TextLayout;
import java.text.*;

public class DrawTextFragment implements Drawable  {
    final TextLayout layout;
    final AttributedString str;
    double x, y;
    
    public DrawTextFragment(TextLayout layout, AttributedString str) {
        this.layout = layout;
        this.str = str;
    }

    public void setPosition(double x, double y) {
        // TODO: replace it, by applyTransform????
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D graphics){
        if(str == null) {
            return;
        }

        double yBaseline = y + layout.getAscent();

        Integer textMode = (Integer)graphics.getRenderingHint(Drawable.TEXT_RENDERING_MODE);
        if(textMode != null && textMode == Drawable.TEXT_AS_SHAPES){
            layout.draw(graphics, (float)x, (float)yBaseline);
        } else {
            graphics.drawString(str.getIterator(), (float)x, (float)yBaseline );
        }
    }

    public void applyTransform(Graphics2D graphics) {
    }

    public void drawContent(Graphics2D graphics) {
    }
    
    public TextLayout getLayout() {
        return layout;
    }

    public AttributedString getAttributedString() {
        return str;
    }
    
    /**
     * @return full height of this text run which is sum of ascent, descent and leading
     */
    public float getHeight(){ 
        double h = layout.getAscent() + layout.getDescent() + getLeading();
        return (float)h;
    }

    /**
     * @return the leading height before/after a text line
     */
    public float getLeading() {
        // fix invalid leadings (leading == 0)
        double l = layout.getLeading();
        if (l == 0) {
            // see https://stackoverflow.com/questions/925147
            // we use a 115% value instead of the 120% proposed one, as this seems to be closer to LO/OO
            l = (layout.getAscent()+layout.getDescent())*0.15;
        }
        return (float)l;
    }
    
    /**
     *
     * @return width if this text run
     */
    public float getWidth(){
        return layout.getAdvance();
    }

    /**
     *
     * @return the string to be painted
     */
    public String getString(){
        if (str == null) return "";

        AttributedCharacterIterator it = str.getIterator();
         StringBuilder buf = new StringBuilder();
         for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
             buf.append(c);
         }
        return buf.toString();
    }

    @Override
    public String toString(){
        return "[" + getClass().getSimpleName() + "] " + getString();
    }
    
}
