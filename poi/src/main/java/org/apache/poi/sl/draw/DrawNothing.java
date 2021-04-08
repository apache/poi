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

import org.apache.poi.sl.usermodel.Shape;


public class DrawNothing implements Drawable {

    protected final Shape<?,?> shape;
    
    public DrawNothing(Shape<?,?> shape) {
        this.shape = shape;
    }
    
    /**
     * Apply 2-D transforms before drawing this shape. This includes rotation and flipping.
     *
     * @param graphics the graphics whos transform matrix will be modified
     */
    public void applyTransform(Graphics2D graphics) {
    }


    public void draw(Graphics2D graphics) {
    }

    public void drawContent(Graphics2D context) {
    }
}
