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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.Section;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.util.HexDump;

/**
 * <p>Renders a {@link PropertySetDescriptor} by more or less dumping
 * the stuff into a {@link JTextArea}.</p>
 */
public class PropertySetDescriptorRenderer extends DocumentDescriptorRenderer
{

    @Override
    public Component getTreeCellRendererComponent(final JTree tree,
                                                  final Object value,
                                                  final boolean selectedCell,
                                                  final boolean expanded,
                                                  final boolean leaf,
                                                  final int row,
                                                  final boolean hasCellFocus)
    {
        final PropertySetDescriptor d = (PropertySetDescriptor)
            ((DefaultMutableTreeNode) value).getUserObject();
        final PropertySet ps = d.getPropertySet();
        final JPanel p = new JPanel();
        final JTextArea text = new JTextArea();
        text.setBackground(new Color(200, 255, 200));
        text.setFont(new Font("Monospaced", Font.PLAIN, 10));
        text.append(renderAsString(d));
        text.append("\nByte order: ");
        text.append(HexDump.toHex((short) ps.getByteOrder()));
        text.append("\nFormat: ");
        text.append(HexDump.toHex((short) ps.getFormat()));
        text.append("\nOS version: ");
        text.append(HexDump.toHex(ps.getOSVersion()));
        text.append("\nClass ID: ");
        text.append(HexDump.toHex(ps.getClassID().getBytes()));
        text.append("\nSection count: " + ps.getSectionCount());
        text.append(sectionsToString(ps.getSections()));
        p.add(text);

        if (ps instanceof SummaryInformation)
        {
            /* Use the convenience methods. */
            final SummaryInformation si = (SummaryInformation) ps;
            text.append("\n");
            text.append("\nTitle:               " + si.getTitle());
            text.append("\nSubject:             " + si.getSubject());
            text.append("\nAuthor:              " + si.getAuthor());
            text.append("\nKeywords:            " + si.getKeywords());
            text.append("\nComments:            " + si.getComments());
            text.append("\nTemplate:            " + si.getTemplate());
            text.append("\nLast Author:         " + si.getLastAuthor());
            text.append("\nRev. Number:         " + si.getRevNumber());
            text.append("\nEdit Time:           " + si.getEditTime());
            text.append("\nLast Printed:        " + si.getLastPrinted());
            text.append("\nCreate Date/Time:    " + si.getCreateDateTime());
            text.append("\nLast Save Date/Time: " + si.getLastSaveDateTime());
            text.append("\nPage Count:          " + si.getPageCount());
            text.append("\nWord Count:          " + si.getWordCount());
            text.append("\nChar Count:          " + si.getCharCount());
            // text.append("\nThumbnail:           " + si.getThumbnail());
            text.append("\nApplication Name:    " + si.getApplicationName());
            text.append("\nSecurity:            " + si.getSecurity());
        }

        if (selectedCell)
            Util.invert(text);
        return p;
    }



    /**
     * <p>Returns a string representation of a list of {@link
     * Section}s.</p>
     */
    protected String sectionsToString(final List<Section> sections)
    {
        final StringBuffer b = new StringBuffer();
        int count = 1;
        for (Iterator<Section>  i = sections.iterator(); i.hasNext();)
        {
            Section s = i.next();
            String d = toString(s, "Section " + count++);
            b.append(d);
        }
        return b.toString();
    }



    /**
     * <p>Returns a string representation of a {@link Section}.</p>
     * @param s the section
     * @param name the section's name
     * @return a string representation of the {@link Section}
     */
    protected String toString(final Section s, final String name)
    {
        final StringBuffer b = new StringBuffer();
        b.append("\n" + name + " Format ID: ");
        b.append(HexDump.toHex(s.getFormatID().getBytes()));
        b.append("\n" + name + " Offset: " + s.getOffset());
        b.append("\n" + name + " Section size: " + s.getSize());
        b.append("\n" + name + " Property count: " + s.getPropertyCount());

        final Property[] properties = s.getProperties();
        for (int i = 0; i < properties.length; i++)
        {
            final Property p = properties[i];
            final long id = p.getID();
            final long type = p.getType();
            final Object value = p.getValue();
            b.append('\n');
            b.append(name);
            b.append(", Name: ");
            b.append(id);
            b.append(" (");
            b.append(s.getPIDString(id));
            b.append("), Type: ");
            b.append(type);
            b.append(", Value: ");
            if (value instanceof byte[]) {
                byte[] buf = new byte[4];
                System.arraycopy(value, 0, buf, 0, 4);
                b.append(HexDump.toHex(buf));
                b.append(' ');
                System.arraycopy(value, ((byte[])value).length - 4, buf, 0, 4);
            } else if (value != null) {
                b.append(value);
            } else {
                b.append("null");
            }
        }
        return b.toString();
    }

}
