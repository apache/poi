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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import javax.xml.namespace.QName;

/**
 * Represents the data for a single node in the XmlTree. This class (known as a
 * "user object" from the JTree perspective) provides a way to get information
 * about the node by essentially wrapping the XmlObject instance that the node
 * represents. The {@link XmlModel}class represents the XML data model to the
 * tree by calling methods of this object.
 */
public final class XmlEntry
{
    private XmlObject[] m_children = new XmlObject[0];

    private final XmlObject m_currentXml;

    private String m_label;

    /**
     * Constructs a entry using <em>xml</em> as the data source.
     * 
     * @param xml The XML this entry will represent.
     */
    public XmlEntry(XmlObject xml)
    {
        m_currentXml = xml;
        m_children = collectChildren(xml);

        // Add a cursor and use it to extract information to display in the
        // tree.
        XmlCursor cursor = xml.newCursor();
        if (!cursor.currentTokenType().isStart())
        {
            cursor.toFirstChild();
        }
        m_label = cursor.getAttributeText(new QName("label"));
        if (m_label == null || m_label.equals(""))
        {
            m_label = cursor.getName().getLocalPart();
        }
        cursor.dispose();
    }

    /**
     * Collects the children of the <em>xml</em> element.
     * 
     * @param xml The XML element whose children should be collected.
     * @return An array of <em>xml</em>'s children.
     */
    private XmlObject[] collectChildren(XmlObject xml)
    {
        return xml.selectPath("./*");
    }

    /**
     * Gets the number of children of the XML this entry represents.
     * 
     * @return The number of children.
     */
    public int getChildCount()
    {
        return m_children.length;
    }

    /**
     * Gets the child at <em>index</em> from among the children of the XML
     * this entry represents.
     * 
     * @param index The index number for the child to get.
     * @return An entry representing the child.
     */
    public XmlEntry getChild(int index)
    {
        XmlEntry childEntry = new XmlEntry(m_children[index]);
        return childEntry;
    }

    /**
     * Gets the children of the XML this entry represents.
     * 
     * @return An entry array representing the children.
     */
    public XmlEntry[] getChildren()
    {
        XmlEntry[] entryChildren = new XmlEntry[getChildCount()];
        for (int i = 0; i < getChildCount(); i++)
        {
            entryChildren[i] = new XmlEntry(m_children[i]);
        }
        return entryChildren;
    }

    /**
     * Returns a name that can be used as a tree node label.
     * 
     * @return The name of the element or attribute this entry represents.
     */
    public String toString()
    {
        return m_label;
    }

    /**
     * Gets the XML that this instance represents.
     * 
     * @return An XmlObject instance representing the XML.
     */
    public XmlObject getXml()
    {
        return m_currentXml;
    }
}