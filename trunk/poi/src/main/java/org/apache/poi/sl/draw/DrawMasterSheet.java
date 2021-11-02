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

import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.SimpleShape;
import org.apache.poi.sl.usermodel.Slide;


public class DrawMasterSheet extends DrawSheet {

    public DrawMasterSheet(MasterSheet<?,?> sheet) {
        super(sheet);
    }

    /**
     * Checks if this {@code sheet} displays the specified shape.
     *
     * Subclasses can override it and skip certain shapes from drawings,
     * for instance, slide masters and layouts don't display placeholders
     */
    @Override
    protected boolean canDraw(Graphics2D graphics, Shape<?,?> shape) {
        Slide<?,?> slide = (Slide<?,?>)graphics.getRenderingHint(Drawable.CURRENT_SLIDE);
        if (shape instanceof SimpleShape) {
            // in XSLF, slidenumber and date shapes aren't marked as placeholders opposed to HSLF
            Placeholder ph = ((SimpleShape<?,?>)shape).getPlaceholder();
            if (ph != null) {
                return slide.getDisplayPlaceholder(ph);
            }
        }
        return slide.getFollowMasterGraphics();
    }
}
