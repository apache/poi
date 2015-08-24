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

import org.apache.poi.sl.usermodel.GroupShape;
import org.apache.poi.sl.usermodel.TableShape;

public class DrawTableShape extends DrawShape {
    // to be implemented ...
    public DrawTableShape(TableShape<?,?> shape) {
        super(shape);
    }
    
    protected Drawable getDrawable(Graphics2D graphics) {
        if (shape instanceof GroupShape) {
            DrawFactory df = DrawFactory.getInstance(graphics);
            return df.getDrawable((GroupShape<?,?>)shape);
        }
        return null;
    }

    public void applyTransform(Graphics2D graphics) {
        Drawable d = getDrawable(graphics);
        if (d != null) {
            d.applyTransform(graphics);
        }
    }

    public void draw(Graphics2D graphics) {
        Drawable d = getDrawable(graphics);
        if (d != null) {
            d.draw(graphics);
        }
    }

    public void drawContent(Graphics2D graphics) {
        Drawable d = getDrawable(graphics);
        if (d != null) {
            d.drawContent(graphics);
        }
    }

    
}
