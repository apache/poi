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

import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import org.apache.poi.poifs.eventfilesystem.*;

/**
 * <p>The main class of the POI Browser. It shows the structure of POI
 * filesystems (Microsoft Office documents) in a {@link
 * JTree}. Specify their filenames on the command line!</p>
 *
 * @see POIFSReader
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-01-19
 */
public class POIBrowser extends JFrame
{

    /**
     * <p>The tree's root node must be visible to all methods.</p>
     */
    protected MutableTreeNode rootNode;



    /**
     * <p>Takes a bunch of file names as command line parameters,
     * opens each of them as a POI filesystem and displays their
     * internal structures in a {@link JTree}.</p>
     */
    public static void main(String[] args)
    {
        new POIBrowser().run(args);
    }



    protected void run(String[] args)
    {
        addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                        System.exit(0);
                }
            });

        /* Create the tree model with a root node. The latter is
         * invisible but it must be present because a tree model
         * always needs a root. */
        rootNode = new DefaultMutableTreeNode("POI Filesystems");
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

        /* Create the tree UI element. */
        final JTree treeUI = new JTree(treeModel);
        getContentPane().add(new JScrollPane(treeUI));

        /* Add the POI filesystems to the tree. */
        int displayedFiles = 0;
        for (int i = 0; i < args.length; i++)
        {
            final String filename = args[i];
            try
            {
                POIFSReader r = new POIFSReader();
                r.registerListener(new TreeReaderListener(filename, rootNode));
                r.read(new FileInputStream(filename));
                displayedFiles++;
            }
            catch (IOException ex)
            {
                System.err.println(filename + ": " + ex);
            }
            catch (Throwable t)
            {
                System.err.println("Unexpected exception while reading \"" +
                                   filename + "\":");
                t.printStackTrace(System.err);
            }
        }

        /* Exit if there is no file to display (none specified or only
         * files with problems). */
        if (displayedFiles == 0)
        {
            System.out.println("No POI filesystem(s) to display.");
            System.exit(0);
        }

        /* Make the tree UI element visible. */
        treeUI.setRootVisible(true);
        treeUI.setShowsRootHandles(true);
        ExtendableTreeCellRenderer etcr = new ExtendableTreeCellRenderer();
        etcr.register(DocumentDescriptor.class,
                      new DocumentDescriptorRenderer());
        etcr.register(PropertySetDescriptor.class,
                      new PropertySetDescriptorRenderer());
        treeUI.setCellRenderer(etcr);
        setSize(600, 450);
        setTitle("POI Browser 0.06");
        setVisible(true);
    }

}
