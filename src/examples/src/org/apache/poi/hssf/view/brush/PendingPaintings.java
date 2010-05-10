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
package org.apache.poi.hssf.view.brush;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to hold pending brush paintings.  The model is that some
 * border drawing requires drawing strokes after all the cells have been
 * painted. The list of pending paintings can be put in this object during the
 * initial paint of the component, and then executed at the appropriate time,
 * such as at the end of the containing object's {@link
 * JComponent#paintChildren(Graphics)} method.
 * <p/>
 * It is up to the parent component to invoke the {@link #paint(Graphics2D)}
 * method of this objet at that appropriate time.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class PendingPaintings {
    /**
     * The name of the client property that holds this object in the parent
     * component.
     */
    public static final String PENDING_PAINTINGS =
            PendingPaintings.class.getSimpleName();

    private final List<Painting> paintings;

    /** A single painting description. */
    public static class Painting {
        final Stroke stroke;
        final Color color;
        final Shape shape;
        final AffineTransform transform;

        /**
         * Creates a new painting description.
         *
         * @param stroke    The stroke to paint.
         * @param color     The color of the stroke.
         * @param shape     The shape of the stroke.
         * @param transform The transformation matrix to use.
         */
        public Painting(Stroke stroke, Color color, Shape shape,
                AffineTransform transform) {

            this.color = color;
            this.shape = shape;
            this.stroke = stroke;
            this.transform = transform;
        }

        /**
         * Draw the painting.
         *
         * @param g The graphics object to use to draw with.
         */
        public void draw(Graphics2D g) {
            g.setTransform(transform);
            g.setStroke(stroke);
            g.setColor(color);
            g.draw(shape);
        }
    }

    /**
     * Creates a new object on the given parent.  The created object will be
     * stored as a client property.
     *
     * @param parent
     */
    public PendingPaintings(JComponent parent) {
        paintings = new ArrayList<Painting>();
        parent.putClientProperty(PENDING_PAINTINGS, this);
    }

    /** Drops all pending paintings. */
    public void clear() {
        paintings.clear();
    }

    /**
     * Paints all pending paintings.  Once they have been painted they are
     * removed from the list of pending paintings (they aren't pending anymore,
     * after all).
     *
     * @param g The graphics object to draw with.
     */
    public void paint(Graphics2D g) {
        g.setBackground(Color.CYAN);
        AffineTransform origTransform = g.getTransform();
        for (Painting c : paintings) {
            c.draw(g);
        }
        g.setTransform(origTransform);

        clear();
    }

    /**
     * Adds a new pending painting to the list on the given component.  This
     * will find the first ancestor that has a {@link PendingPaintings} client
     * property, starting with the component itself.
     *
     * @param c      The component for which the painting is being added.
     * @param g      The graphics object to draw with.
     * @param stroke The stroke to draw.
     * @param color  The color to draw with.
     * @param shape  The shape to stroke.
     */
    public static void add(JComponent c, Graphics2D g, Stroke stroke,
            Color color, Shape shape) {

        add(c, new Painting(stroke, color, shape, g.getTransform()));
    }

    /**
     * Adds a new pending painting to the list on the given component.  This
     * will find the first ancestor that has a {@link PendingPaintings} client
     * property, starting with the component itself.
     *
     * @param c           The component for which the painting is being added.
     * @param newPainting The new painting.
     */
    public static void add(JComponent c, Painting newPainting) {
        PendingPaintings pending = pendingPaintingsFor(c);
        if (pending != null) {
            pending.paintings.add(newPainting);
        }
    }

    /**
     * Returns the pending painting object for the given component, if any. This
     * is retrieved from the first object found that has a {@link
     * #PENDING_PAINTINGS} client property, starting with this component and
     * looking up its ancestors (parent, parent's parent, etc.)
     * <p/>
     * This allows any descendant of a component that has a {@link
     * PendingPaintings} property to add its own pending paintings.
     *
     * @param c The component for which the painting is being added.
     *
     * @return The pending painting object for that component, or <tt>null</tt>
     *         if there is none.
     */
    public static PendingPaintings pendingPaintingsFor(JComponent c) {
        for (Component parent = c;
             parent != null;
             parent = parent.getParent()) {
            if (parent instanceof JComponent) {
                JComponent jc = (JComponent) parent;
                Object pd = jc.getClientProperty(PENDING_PAINTINGS);
                if (pd != null)
                    return (PendingPaintings) pd;
            }
        }
        return null;
    }
}