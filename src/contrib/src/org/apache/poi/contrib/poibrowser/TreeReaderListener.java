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

import java.io.*;
import java.util.*;
import javax.swing.tree.*;
import org.apache.poi.hpsf.*;
import org.apache.poi.poifs.eventfilesystem.*;
import org.apache.poi.poifs.filesystem.*;

/**
 * <p>Organizes document information in a tree model in order to be
 * e.g. displayed in a Swing {@link JTree}. An instance of this class
 * is created with a root tree node ({@link MutableTreeNode}) and
 * registered as a {@link POIFSReaderListener} with a {@link
 * POIFSReader}. While the latter processes a POI filesystem it calls
 * this class' {@link #processPOIFSReaderEvent} for each document it
 * has been registered for. This method appends the document it
 * processes at the appropriate position into the tree rooted at the
 * above mentioned root tree node.</p>
 *
 * <p>The root tree node should be the root tree node of a {@link
 * TreeModel}.</p>
 *
 * <p>A top-level element in the tree model, i.e. an immediate child
 * node of the root node, describes a POI filesystem as such. It is
 * suggested to use the file's name (as seen by the operating system)
 * but it could be any other string.</p>
 *
 * <p>The value of a tree node is a {@link DocumentDescriptor}. Unlike
 * a {@link POIFSDocument} which may be as heavy as many megabytes, an
 * instance of {@link DocumentDescriptor} is a light-weight object and
 * contains only some meta-information about a document.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-01-24
 */
public class TreeReaderListener implements POIFSReaderListener
{

    /**
     * <p>The tree's root node. POI filesystems get attached to this
     * node as children.</p>
     */
    protected MutableTreeNode rootNode;

    /**
     * <p>Maps filenames and POI document paths to their associated
     * tree nodes.</p>
     */
    protected Map pathToNode;

    /**
     * <p>The name of the file this {@link TreeReaderListener}
     * processes. It is used to identify a top-level element in the
     * tree. Alternatively any other string can be used. It is just a
     * label which should identify a POI filesystem.</p>
     */
    protected String filename;



    /**
     * <p>Creates a {@link TreeReaderListener} which should then be
     * registered with a {@link POIFSReader}.</p>
     *
     * @param filename The name of the POI filesystem, i.e. the name
     * of the file the POI filesystem resides in. Alternatively any
     * other string can be used.
     *
     * @param rootNode All document information will be attached as
     * descendands to this tree node.
     */
    public TreeReaderListener(final String filename,
                              final MutableTreeNode rootNode)
    {
        this.filename = filename;
        this.rootNode = rootNode;
        pathToNode = new HashMap(15); // Should be a reasonable guess.
    }



    /** <p>The number of bytes to dump.</p> */
    private int nrOfBytes = 50;

    public void setNrOfBytes(final int nrOfBytes)
    {
        this.nrOfBytes = nrOfBytes;
    }

    public int getNrOfBytes()
    {
        return nrOfBytes;
    }



    /**
     * <p>A document in the POI filesystem has been opened for
     * reading. This method retrieves properties of the document and
     * adds them to a tree model.</p>
     */
    public void processPOIFSReaderEvent(final POIFSReaderEvent event)
    {
        DocumentDescriptor d;
        final DocumentInputStream is = event.getStream();
        if (!is.markSupported())
            throw new UnsupportedOperationException(is.getClass().getName() +
                " does not support mark().");

        /* Try do handle this document as a property set. We receive
         * an exception if is no property set and handle it as a
         * document of some other format. We are not concerned about
         * that document's details. */
        try
        {
            d = new PropertySetDescriptor(event.getName(), event.getPath(),
                                          is, nrOfBytes);
        }
        catch (HPSFException ex)
        {
            d = new DocumentDescriptor(event.getName(), event.getPath(),
                                       is, nrOfBytes);
        }
        catch (Throwable t)
        {
            System.err.println
                ("Unexpected exception while processing " +
                event.getName() + " in " + event.getPath().toString());
            t.printStackTrace(System.err);
            throw new RuntimeException(t.getMessage());
        }

        try
        {
            is.close();
        }
        catch (IOException ex)
        {
            System.err.println
                ("Unexpected exception while closing " +
                event.getName() + " in " + event.getPath().toString());
            ex.printStackTrace(System.err);
        }

        final MutableTreeNode parentNode = getNode(d.path, filename, rootNode);
        final MutableTreeNode nameNode = new DefaultMutableTreeNode(d.name);
        parentNode.insert(nameNode, 0);
        final MutableTreeNode dNode = new DefaultMutableTreeNode(d);
        nameNode.insert(dNode, 0);
    }



    /**
     * <p>Locates the parent node for a document entry in the tree
     * model. If the parent node does not yet exist it will be
     * created, too. This is done recursively, if needed.</p>
     *
     * @param path The tree node for this path is located.
     *
     * @param fsName The name of the POI filesystem. This is just a
     * string which is displayed in the tree at the top lovel.
     *
     * @param root The root node.
     */
    private MutableTreeNode getNode(final POIFSDocumentPath path,
                                    final String fsName,
                                    final MutableTreeNode root)
    {
        MutableTreeNode n = (MutableTreeNode) pathToNode.get(path);
        if (n != null)
            /* Node found in map, just return it. */
            return n;
        if (path.length() == 0)
        {
            /* This is the root path of the POI filesystem. Its tree
             * node is resp. must be located below the tree node of
             * the POI filesystem itself. This is a tree node with the
             * POI filesystem's name (this the operating system file's
             * name) as its key it the path-to-node map. */
            n = (MutableTreeNode) pathToNode.get(fsName);
            if (n == null)
            {
                /* A tree node for the POI filesystem does not yet
                 * exist. */
                n = new DefaultMutableTreeNode(fsName);
                pathToNode.put(fsName, n);
                root.insert(n, 0);
            }
            return n;
        }
        else
        {
            /* The path is somewhere down in the POI filesystem's
             * hierarchy. We need the tree node of this path's parent
             * and attach our new node to it. */
            final String name = path.getComponent(path.length() - 1);
            final POIFSDocumentPath parentPath = path.getParent();
            final MutableTreeNode parentNode =
                getNode(parentPath, fsName, root);
            n = new DefaultMutableTreeNode(name);
            pathToNode.put(path, n);
            parentNode.insert(n, 0);
            return n;
        }
    }

}
