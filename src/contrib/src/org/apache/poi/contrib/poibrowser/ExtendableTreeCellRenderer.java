/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 */

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
 * @version $Id$
 * @since 2002-01-22
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
        if (superclass != null)
            return findRenderer(superclass);
        else
            return null;
    }

}
