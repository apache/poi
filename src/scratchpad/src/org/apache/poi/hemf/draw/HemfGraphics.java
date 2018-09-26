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
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

import org.apache.poi.hemf.record.emf.HemfBounded;
import org.apache.poi.hemf.record.emf.HemfRecord;
import org.apache.poi.hwmf.draw.HwmfGraphics;
import org.apache.poi.hwmf.record.HwmfObjectTableEntry;
import org.apache.poi.util.Internal;

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

    @Internal
    public void draw(Consumer<Path2D> pathConsumer) {
        final HemfDrawProperties prop = getProperties();
        final boolean useBracket = prop.usePathBracket();

        final Path2D path;
        if (useBracket) {
            path = prop.getPath();
        } else {
            path = new Path2D.Double();
            Point2D pnt = prop.getLocation();
            path.moveTo(pnt.getX(),pnt.getY());
        }

        pathConsumer.accept(path);

        prop.setLocation(path.getCurrentPoint());
        if (!useBracket) {
            // TODO: when to use draw vs. fill?
            super.draw(path);
        }

    }

    /**
     * Adds or sets an record of type {@link HwmfObjectTableEntry} to the object table.
     * If the {@code index} is less than 1, the method acts the same as
     * {@link HwmfGraphics#addObjectTableEntry(HwmfObjectTableEntry)}, otherwise the
     * index is used to access the object table.
     * As the table is filled successively, the index must be between 1 and size+1
     *
     * @param entry the record to be stored
     * @param index the index to be overwritten, regardless if its content was unset before
     *
     * @see HwmfGraphics#addObjectTableEntry(HwmfObjectTableEntry)
     */
    public void addObjectTableEntry(HwmfObjectTableEntry entry, int index) {
        if (index < 1) {
            super.addObjectTableEntry(entry);
            return;
        }

        if (index > objectTable.size()) {
            throw new IllegalStateException("object table hasn't grown to this index yet");
        }

        if (index == objectTable.size()) {
            objectTable.add(entry);
        } else {
            objectTable.set(index, entry);
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
