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

package org.apache.poi.xdgf.usermodel.shape;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import org.apache.poi.xdgf.usermodel.XDGFShape;

public class ShapeDebuggerRenderer extends ShapeRenderer {

    ShapeVisitorAcceptor _debugAcceptor;

    public ShapeDebuggerRenderer() {
        super();
    }

    public ShapeDebuggerRenderer(Graphics2D g) {
        super(g);
    }

    public void setDebugAcceptor(ShapeVisitorAcceptor acceptor) {
        _debugAcceptor = acceptor;
    }

    @Override
    protected Path2D drawPath(XDGFShape shape) {

        Path2D path = super.drawPath(shape);
        if (_debugAcceptor == null || _debugAcceptor.accept(shape)) {

            // show numbers to associate shapes with ids.. doesn't always work
            Font f = _graphics.getFont();
            _graphics.scale(1, -1);
            _graphics.setFont(f.deriveFont(0.05F));

            String shapeId = "" + shape.getID();
            float shapeOffset = -0.1F;

            if (shape.hasMasterShape()) {
                shapeId += " MS:" + shape.getMasterShape().getID();
                shapeOffset -= 0.15F;
            }

            _graphics.drawString(shapeId, shapeOffset, 0);
            _graphics.setFont(f);
            _graphics.scale(1, -1);
        }

        return path;
    }

}
