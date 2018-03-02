/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.samples.xmltree;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

/**
 * Provides a frame within which to display an instance of the {@link XmlTree},
 * along with a pane within which to display the XML corresponding to a node
 * selected in the tree. The tree's data is managed by the {@link XmlModel}and
 * {@link XmlEntry}classes, along with XmlTree. The XmlTreeFrame class is
 * merely a container to show the tree in use.
 */
final class XmlTreeFrame extends JFrame
{
    // Variables for UI components.
    private XmlTree treeXmlTree;

    private JButton btnRefresh;

    private JPanel pnlContent;

    private JPanel pnlSelectionPanel;

    private JPanel pnlTree;

    private JScrollPane scrContent;

    private JScrollPane scrTree;

    private JSplitPane splTreeContent;

    private JTextField txtFileName;

    private JTextPane txtpnlContent;

    /**
     * Constructs the frame with an XML file to use for the tree.
     * 
     * @param xmlFile The file containing XML that the tree should represent.
     */
    public XmlTreeFrame(File xmlFile)
    {
        initComponents(xmlFile);
    }

    /**
     * Initializes UI components, setting properties and adding event listeners.
     * 
     * @param xmlFile The XML file to be represented by the tree.
     */
    private void initComponents(File xmlFile)
    {
        // Set properties for this frame.
        getContentPane().setLayout(new GridBagLayout());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("XML Tree View");
        setName("frmXmlTreeView");

        // Create the top panel that will contain text box and refresh button.
        pnlSelectionPanel = new JPanel();
        pnlSelectionPanel.setLayout(new GridBagLayout());

        // Create the text box to display the XML file path.
        txtFileName = new JTextField();
        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(4, 4, 0, 4);
        gridBagConstraints.weightx = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        txtFileName.setText(xmlFile.getAbsolutePath());
        pnlSelectionPanel.add(txtFileName, gridBagConstraints);

        // Create the refresh button.
        btnRefresh = new JButton();
        btnRefresh.setText("Refresh");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        pnlSelectionPanel.add(btnRefresh, gridBagConstraints);

        // Add the selection panel to this frame.
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 0;
        getContentPane().add(pnlSelectionPanel, gridBagConstraints);

        // Create the split plane that separates the tree and the content panes.
        splTreeContent = new JSplitPane();

        // Create the components for the left side of the split pane:
        // the panel, scrolling panel, and the XML tree it will contain.
        pnlTree = new JPanel();
        scrTree = new JScrollPane();
        treeXmlTree = new XmlTree(xmlFile);
        scrTree.setViewportView(treeXmlTree);
        pnlTree.setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        pnlTree.add(scrTree, gridBagConstraints);

        // Put the tree panel in the left side of the split pane.
        splTreeContent.setLeftComponent(pnlTree);

        // Create the components for the left side of the split pane:
        // the panel, scrolling panel, and the XML tree it will contain.
        pnlContent = new JPanel();
        scrContent = new JScrollPane();
        txtpnlContent = new JTextPane();
        scrContent.setViewportView(txtpnlContent);
        pnlContent.setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        pnlContent.add(scrContent, gridBagConstraints);

        // Put the content panel in the right side of the split pane.
        splTreeContent.setRightComponent(pnlContent);

        // Set the rest of the split pane's properties,
        splTreeContent.setDividerLocation(170);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridheight = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        getContentPane().add(splTreeContent, gridBagConstraints);

        // Add a listener to get mouse clicks on the tree nodes.
        treeXmlTree.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent event)
            {
                if (event.getClickCount() == 1)
                {
                    XmlEntry selection = (XmlEntry) treeXmlTree
                            .getLastSelectedPathComponent();
                    // selection might be null if the user clicked one of the
                    // expandy/collapsy things without selecting a node.
                    if (selection == null)
                    {
                        return;
                    }
                    // Get the pretty-printed XML text and put it in the
                    // window on the right.
                    XmlObject node = selection.getXml();
                    XmlCursor nodeCursor = node.newCursor();
                    XmlOptions options = new XmlOptions();
                    options.setSavePrettyPrint();
                    options.setSavePrettyPrintIndent(4);
                    String xmlString = nodeCursor.xmlText(options);
                    txtpnlContent.setText(xmlString);
                }
            }

            // Don't respond to these events.
            public void mouseEntered(MouseEvent event)
            {}

            public void mouseExited(MouseEvent event)
            {}

            public void mousePressed(MouseEvent event)
            {}

            public void mouseReleased(MouseEvent event)
            {}
        });

        // Add a listener to get mouse clicks on the Refresh button.
        btnRefresh.addMouseListener(new MouseListener()
        {
            public void mouseClicked(MouseEvent event)
            {
                // Get the text from the file path box and make a file from it.
                String filePath = txtFileName.getText();
                File xmlFile = new File(filePath);

                // If the path points to a file, build the tree all over again.
                if (xmlFile.exists())
                {
                    treeXmlTree.setXmlFile(xmlFile);
                    txtpnlContent.setText("");
                } else
                {
                    JOptionPane.showMessageDialog(null,
                            "The path you gave appears "
                                    + "not to point to a file.",
                            "XmlTree Message", JOptionPane.ERROR_MESSAGE);
                }

            }

            // Don't respond to these events.
            public void mouseEntered(MouseEvent event)
            {}

            public void mouseExited(MouseEvent event)
            {}

            public void mousePressed(MouseEvent event)
            {}

            public void mouseReleased(MouseEvent event)
            {}
        });

        // Size all the components to their preferred size.
        pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 640);
        this.setVisible(true);
    }
}