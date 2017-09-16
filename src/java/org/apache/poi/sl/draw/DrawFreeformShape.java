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
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.poi.sl.draw.geom.Outline;
import org.apache.poi.sl.draw.geom.Path;
import org.apache.poi.sl.usermodel.*;

public class DrawFreeformShape extends DrawAutoShape {
    public DrawFreeformShape(FreeformShape<?,?> shape) {
        super(shape);
    }

    protected Collection<Outline> computeOutlines(Graphics2D graphics) {
        List<Outline> lst = new ArrayList<>();
        FreeformShape<?,?> fsh = (FreeformShape<?, ?>) getShape();
        Path2D sh = fsh.getPath();

        AffineTransform tx = (AffineTransform)graphics.getRenderingHint(Drawable.GROUP_TRANSFORM);
        if (tx == null) {
            tx = new AffineTransform();
        }

        java.awt.Shape canvasShape = tx.createTransformedShape(sh);

        FillStyle fs = fsh.getFillStyle();
        StrokeStyle ss = fsh.getStrokeStyle();
        Path path = new Path(fs != null, ss != null);
        lst.add(new Outline(canvasShape, path));
        return lst;
    }

    @Override
    protected TextShape<?,? extends TextParagraph<?,?,? extends TextRun>> getShape() {
        return (TextShape<?,? extends TextParagraph<?,?,? extends TextRun>>)shape;
    }
}
