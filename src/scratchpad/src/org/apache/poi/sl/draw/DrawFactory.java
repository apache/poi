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

import static org.apache.poi.sl.draw.Drawable.DRAW_FACTORY;

import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.text.AttributedString;

import org.apache.poi.sl.usermodel.*;

public class DrawFactory {
    protected static ThreadLocal<DrawFactory> defaultFactory = new ThreadLocal<DrawFactory>();

    /**
     * Set a custom draw factory for the current thread.
     * This is a fallback, for operations where usercode can't set a graphics context.
     * Preferably use the rendering hint {@link Drawable#DRAW_FACTORY} to set the factory.
     *
     * @param factory
     */
    public static void setDefaultFactory(DrawFactory factory) {
        defaultFactory.set(factory);
    }

    public static DrawFactory getInstance(Graphics2D graphics) {
        // first try to find the factory over the rendering hing
        DrawFactory factory = (DrawFactory)graphics.getRenderingHint(DRAW_FACTORY);
        // secondly try the thread local default
        if (factory == null) {
            factory = defaultFactory.get();
        }
        // and at last, use the default factory
        if (factory == null) {
            factory = new DrawFactory();
            graphics.setRenderingHint(DRAW_FACTORY, factory);
        }
        return factory;
    }

    public Drawable getDrawable(Sheet sheet) {
        return new DrawSheet(sheet);
    }

    public Drawable getDrawable(MasterSheet sheet) {
        return new DrawMasterSheet(sheet);
    }

    @SuppressWarnings("unchecked")
    public Drawable getDrawable(Shape shape) {
        if (shape instanceof TextBox) {
            return getDrawable((TextBox)shape);
        } else if (shape instanceof FreeformShape) {
            return getDrawable((FreeformShape)shape);
        }

        throw new IllegalArgumentException("Unsupported shape type: "+shape.getClass());
    }

    public <T extends TextBox> DrawTextBox<T> getDrawable(T shape) {
        return new DrawTextBox<T>(shape);
    }

    public <T extends FreeformShape> DrawFreeformShape<T> getDrawable(T shape) {
        return new DrawFreeformShape<T>(shape);
    }

    
    public DrawTextParagraph getDrawable(TextParagraph paragraph) {
        return new DrawTextParagraph(paragraph);
    }

    public DrawTextFragment getTextFragment(TextLayout layout, AttributedString str) {
        return new DrawTextFragment(layout, str);
    }
    
    public DrawPaint getPaint(PlaceableShape shape) {
        return new DrawPaint(shape);
    }
}
