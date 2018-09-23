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

package org.apache.poi.hemf.draw;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.poi.hemf.record.emf.HemfBounded;
import org.apache.poi.hemf.record.emf.HemfRecord;
import org.apache.poi.hwmf.draw.HwmfGraphics;

public class HemfGraphics extends HwmfGraphics {

    private final Deque<AffineTransform> transforms = new ArrayDeque<>();

    public HemfGraphics(Graphics2D graphicsCtx, Rectangle2D bbox) {
        super(graphicsCtx,bbox);
        // add dummy entry for object index 0, as emf is 1-based
        addObjectTableEntry((ctx)->{});
    }

    @Override
    public HemfDrawProperties getProperties() {
        if (prop == null) {
            prop = new HemfDrawProperties();
        }
        return (HemfDrawProperties)prop;
    }

    @Override
    public void saveProperties() {
        assert(prop != null);
        propStack.add(prop);
        prop = new HemfDrawProperties((HemfDrawProperties)prop);
    }

    @Override
    public void updateWindowMapMode() {
        // ignore window settings
    }

    public void draw(HemfRecord r) {
        if (r instanceof HemfBounded) {
            saveTransform();
            final HemfBounded bounded = (HemfBounded)r;
            final Rectangle2D tgt = bounded.getRecordBounds();
            if (tgt != null && !tgt.isEmpty()) {
                final Rectangle2D src = bounded.getShapeBounds(this);
                if (src != null && !src.isEmpty()) {
                    graphicsCtx.translate(tgt.getCenterX() - src.getCenterX(), tgt.getCenterY() - src.getCenterY());
                    graphicsCtx.translate(src.getCenterX(), src.getCenterY());
                    graphicsCtx.scale(tgt.getWidth() / src.getWidth(), tgt.getHeight() / src.getHeight());
                    graphicsCtx.translate(-src.getCenterX(), -src.getCenterY());
                }
            }
        }

        r.draw(this);

        if (r instanceof HemfBounded) {
            restoreTransform();
        }
    }


    /** saves the current affine transform on the stack */
    private void saveTransform() {
        transforms.push(graphicsCtx.getTransform());
    }

    /** restore the last saved affine transform */
    private void restoreTransform() {
        graphicsCtx.setTransform(transforms.pop());
    }
}
