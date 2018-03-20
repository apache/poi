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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A tree view on XML, with nodes representing both elements and attributes. See
 * {@link XmlEntry}and {@link XmlModel}for information on how information
 * about the underlying XML is retrieved for use in the tree. Those classes use
 * XMLBeans to provide a wrapper by which the tree exposes the underlying XML.
 */
final class XmlTree extends JTree
{
    /**
     * Receives <root> XML instance, executing methods that 
     * edit the received instance or create a new one.
     * 
     * @param args An array in which the first item is a
     * path to the XML instance file.
     */
    public static void main(String[] args)
    {
        System.out.println("Creating XmlTree.\n");
        File xmlFile = new File(args[0]);
        XmlTreeFrame thisSample = new XmlTreeFrame(xmlFile);
    }
    
    /**
     * Constructs the tree using <em>xmlFile</em> as an original source of
     * nodes.
     * 
     * @param xmlFile The XML file the new tree should represent.
     */
    public XmlTree(File xmlFile)
    {
        setXmlFile(xmlFile);
    }

    /**
     * Sets the XML file that should be used to build the tree; the tree will be
     * refreshed to represent <em>xmlFile</em>.
     * 
     * @param xmlFile The XML file the new tree should represent.
     */
    public void setXmlFile(File xmlFile)
    {
        initComponents(xmlFile);
    }

    /**
     * Parses <em>xmlFile</em> into XMLBeans types (XmlObject instances),
     * returning the instance representing the root.
     * 
     * @param xmlFile The XML file to parse.
     * @return An XmlObject representing the root of the parsed XML.
     */
    private static XmlObject parseXml(File xmlFile)
    {
        XmlObject xml = XmlObject.Factory.newInstance();
        try
        {
            xml = XmlObject.Factory.parse(xmlFile);
        } catch (XmlException xmle)
        {
            System.err.println(xmle.toString());
        } catch (IOException ioe)
        {
            System.err.println(ioe.toString());
        }
        return xml;
    }

    /**
     * Sets up the components that make up this tree.
     * 
     * @param xmlFile The XML file the new tree should represent.
     */
    private void initComponents(File xmlFile)
    {
        // Parse the XML create an XmlModel from its root.
        XmlEntry rootEntry = new XmlEntry(parseXml(xmlFile));
        XmlModel treemodel = new XmlModel(rootEntry);

        // Set UI properties.
        setModel(treemodel);
        setRootVisible(true);
        setShowsRootHandles(true);
        setAutoscrolls(true);
        setEditable(false);
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        // Uncomment these lines to provide your own GIF files for
        // tree icons.
        //        renderer.setLeafIcon(createImageIcon("images/leaf.gif"));
        //        renderer.setOpenIcon(createImageIcon("images/minus.gif"));
        //        renderer.setClosedIcon(createImageIcon("images/plus.gif"));
        setCellRenderer(renderer);
        setRootVisible(false);
        setAutoscrolls(false);
    }

    /**
     * Creates an icon from a path that points at a GIF file. This method is
     * called to create tree node icons.
     * 
     * @param path The path to a GIF file.
     * @return An icon instance.
     */
    private static ImageIcon createImageIcon(String path)
    {
        File imgFile = new File(path);
        URL imgUrl = null;
        try
        {
            imgUrl = imgFile.toURL();
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        if (imgUrl != null)
        {
            return new ImageIcon(imgUrl);
        } else
        {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}