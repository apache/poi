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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.apache.poi.poifs.eventfilesystem.POIFSReader;

/**
 * <p>The main class of the POI Browser. It shows the structure of POI
 * filesystems (Microsoft Office documents) in a {@link
 * JTree}. Specify their filenames on the command line!</p>
 *
 * @see POIFSReader
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
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
        setTitle("POI Browser 0.09");
        setVisible(true);
    }

}
