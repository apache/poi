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

package org.apache.poi.contrib.poibrowser;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.util.*;

/**
 * <p>This is a {@link TreeCellRenderer} implementation which is able
 * to render arbitrary objects. The {@link ExtendableTreeCellRenderer}
 * does not do the rendering itself but instead dispatches to
 * class-specific renderers. A class/renderer pair must be registered
 * using the {@link #register} method. If a class has no registered
 * renderer, the renderer of its closest superclass is used. Since the
 * {@link ExtendableTreeCellRenderer} always has a default renderer
 * for the {@link Object} class, rendering is always possible. The
 * default {@link Object} renderer can be replaced by another renderer
 * but it cannot be unregistered.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class ExtendableTreeCellRenderer implements TreeCellRenderer
{

    /**
     * <p>Maps classes to renderers.</p>
     */
    protected Map renderers;



    public ExtendableTreeCellRenderer()
    {
        renderers = new HashMap();
        register(Object.class, new DefaultTreeCellRenderer()
            {
                public Component getTreeCellRendererComponent
                    (JTree tree, Object value, boolean selected,
                     boolean expanded, boolean leaf, int row, boolean hasFocus)
                {
                    final String s = value.toString();
                    final JLabel l = new JLabel(s + "  ");
                    if (selected)
                    {
                        Util.invert(l);
                        l.setOpaque(true);
                    }
                    return l;
                }
            });
    }



    /**
     * <p>Registers a renderer for a class.</p>
     **/
    public void register(final Class c, final TreeCellRenderer renderer)
    {
        renderers.put(c, renderer);
    }



    /**
     * <p>Unregisters a renderer for a class. The renderer for the
     * {@link Object} class cannot be unregistered.</p>
     */
    public void unregister(final Class c)
    {
        if (c == Object.class)
            throw new IllegalArgumentException
                ("Renderer for Object cannot be unregistered.");
        renderers.put(c, null);
    }



    /**
     * <p>Renders an object in a tree cell depending of the object's
     * class.</p>
     *
     * @see TreeCellRenderer#getTreeCellRendererComponent
     */
    public Component getTreeCellRendererComponent
        (final JTree tree, final Object value, final boolean selected,
         final boolean expanded, final boolean leaf, final int row,
         final boolean hasFocus)
    {
        final String NULL = "null";
        TreeCellRenderer r;
        Object userObject;
        if (value == null)
            userObject = NULL;
        else
        {
            userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject == null)
                userObject = NULL;
        }
        r = findRenderer(userObject.getClass());
        return r.getTreeCellRendererComponent
            (tree, value, selected, expanded, leaf, row,
             hasFocus);
    }



    /**
     * <p>Find the renderer for the specified class.</p>
     */
    protected TreeCellRenderer findRenderer(final Class c)
    {
        final TreeCellRenderer r = (TreeCellRenderer) renderers.get(c);
        if (r != null)
            /* The class has a renderer. */
            return r;

        /* The class has no renderer, try the superclass, if any. */
        final Class superclass = c.getSuperclass();
        if (superclass != null) {
            return findRenderer(superclass);
        }
        return null;
    }

}
