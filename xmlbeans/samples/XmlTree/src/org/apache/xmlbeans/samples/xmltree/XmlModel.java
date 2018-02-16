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

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import java.util.Vector;

/**
 * Defines a data model for the XmlTree. Through the data model, the tree can
 * retrieve information about the underlying hierarchical data, including the
 * root of the hierarchy, children of specified nodes, and so on. This data
 * model interacts with the underlying XML data through {@link XmlEntry}
 * instances (known as "user objects" in the context of JTree data models). The
 * XmlEntry class knows how to retrieve XML-specific hierarchical information as
 * it is represented by the XmlObject XMLBeans type. In other words, from the
 * tree's perspective, XmlEntry wraps XmlObject.
 */
final class XmlModel implements TreeModel
{
    private final XmlEntry m_rootEntry;

    private final Vector m_treeModelListeners = new Vector();

    /**
     * Creates a new instance of the model using <em>entry</em> as a root
     * node.
     * 
     * @param entry The root node.
     */
    public XmlModel(XmlEntry entry)
    {
        m_rootEntry = entry;
    }

    /**
     * Gets the child of <em>node</em> at <em>index</em>.
     * 
     * @param node The parent whose child to get.
     * @param index The index of the child to get.
     * @return The child as an XmlEntry instance.
     */
    public Object getChild(Object node, int index)
    {
        XmlEntry entry = (XmlEntry) node;
        return entry.getChild(index);
    }

    /**
     * Gets the number of children that <em>node</em> has.
     * 
     * @param node The tree node whose children should be counted.
     * @return The number of children.
     */
    public int getChildCount(Object node)
    {
        XmlEntry entry = (XmlEntry) node;
        return entry.getChildCount();
    }

    /**
     * Gets the index of <em>childNode</em> as a child of <em>parentNode</em>.
     * 
     * @param parentNode The parent tree node whose children should be checked.
     * @param childNode The tree node whose child index should be returned.
     * @return The index of <em>childNode</em>; -1 if either
     *         <em>parentNode</em> or <em>childNode</em> is null.
     */
    public int getIndexOfChild(Object parentNode, Object childNode)
    {
        int childIndex = 0;
        XmlEntry parent = (XmlEntry) parentNode;
        XmlEntry[] children = parent.getChildren();
        for (int i = 0; i < children.length; i++)
        {
            if (children[i].equals(childNode))
            {
                childIndex = i;
            }
        }
        return childIndex;
    }

    /**
     * Gets the root of this model.
     * 
     * @return An XmlEntry instance representing the XML's root element.
     */
    public Object getRoot()
    {
        return m_rootEntry;
    }

    /**
     * Determines whether <em>node</em> has any children, returning
     * <code>true</code> if it doesn't.
     * 
     * @param node The node to test.
     * @return <code>true</code> if <em>node</em> has no children;
     *         otherwise, <code>false</code>.
     */
    public boolean isLeaf(Object node)
    {
        XmlEntry entry = (XmlEntry) node;
        return entry.getChildCount() == 0;
    }

    /**
     * Called when the user has altered the value for the item identified by
     * <em>treePath</em> to <em>newValue</em>.
     * 
     * @param treePath The item whose path has changed.
     * @param newValue The new value.
     */
    public void valueForPathChanged(TreePath treePath, Object newValue)
    {
        System.out.println("Path changing: " + treePath.toString() + "; "
                + newValue.toString());
    }

    /**
     * Adds a listener.
     * 
     * @param treeModelListener The listener to add.
     */
    public void addTreeModelListener(TreeModelListener treeModelListener)
    {
        m_treeModelListeners.addElement(treeModelListener);
    }

    /**
     * Removes a listener added by addTreeModelListener.
     * 
     * @param treeModelListener The listener to remove.
     */
    public void removeTreeModelListener(TreeModelListener treeModelListener)
    {
        m_treeModelListeners.removeElement(treeModelListener);
    }
}