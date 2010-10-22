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

package org.apache.poi.poifs.poibrowser;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.apache.poi.hpsf.HPSFException;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSDocumentPath;

/**
 * <p>Organizes document information in a tree model in order to be
 * e.g. displayed in a Swing {@link javax.swing.JTree}. An instance of this
 * class is created with a root tree node ({@link MutableTreeNode}) and
 * registered as a {@link POIFSReaderListener} with a {@link
 * org.apache.poi.poifs.eventfilesystem.POIFSReader}. While the latter processes
 * a POI filesystem it calls this class' {@link #processPOIFSReaderEvent} for
 * each document it has been registered for. This method appends the document it
 * processes at the appropriate position into the tree rooted at the
 * above mentioned root tree node.</p>
 *
 * <p>The root tree node should be the root tree node of a {@link
 * javax.swing.tree.TreeModel}.</p>
 *
 * <p>A top-level element in the tree model, i.e. an immediate child
 * node of the root node, describes a POI filesystem as such. It is
 * suggested to use the file's name (as seen by the operating system)
 * but it could be any other string.</p>
 *
 * <p>The value of a tree node is a {@link DocumentDescriptor}. Unlike
 * a {@link org.apache.poi.poifs.filesystem.POIFSDocument} which may be as heavy
 * as many megabytes, an instance of {@link DocumentDescriptor} is a
 * light-weight object and contains only some meta-information about a
 * document.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
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
     * registered with a
     * {@link org.apache.poi.poifs.eventfilesystem.POIFSReader}.</p>
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

        is.close();

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
        /* else - The path is somewhere down in the POI filesystem's
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
