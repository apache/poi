
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.contrib.poibrowser;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.*;
import org.apache.poi.hpsf.*;
import org.apache.poi.hpsf.wellknown.*;

/**
 * <p>Renders a {@link PropertySetDescriptor} by more or less dumping
 * the stuff into a {@link JTextArea}.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-02-05
 */
public class PropertySetDescriptorRenderer extends DocumentDescriptorRenderer
{

    public Component getTreeCellRendererComponent(final JTree tree,
                                                  final Object value,
                                                  final boolean selected,
                                                  final boolean expanded,
                                                  final boolean leaf,
                                                  final int row,
                                                  final boolean hasFocus)
    {
        final PropertySetDescriptor d = (PropertySetDescriptor)
            ((DefaultMutableTreeNode) value).getUserObject();
        final PropertySet ps = d.getPropertySet();
        final JPanel p = new JPanel();
        final JTextArea text = new JTextArea();
        text.setBackground(new Color(200, 255, 200));
        text.setFont(new Font("Monospaced", Font.PLAIN, 10));
        text.append(renderAsString(d));
        text.append("\nByte order: " +
                    Codec.hexEncode((short) ps.getByteOrder()));
        text.append("\nFormat: " +
                    Codec.hexEncode((short) ps.getFormat()));
        text.append("\nOS version: " +
                    Codec.hexEncode(ps.getOSVersion()));
        text.append("\nClass ID: " +
                    Codec.hexEncode(ps.getClassID()));
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

        if (selected)
            Util.invert(text);
        return p;
    }



    /**
     * <p>Returns a string representation of a list of {@link
     * Section}s.</p>
     */
    protected String sectionsToString(final List sections)
    {
        final StringBuffer b = new StringBuffer();
        int count = 1;
        for (Iterator i = sections.iterator(); i.hasNext();)
        {
            Section s = (Section) i.next();
            b.append(toString(s, "Section " + count++));
        }
        return b.toString();
    }



    /**
     * <p>Returns a string representation of a {@link Section}.</p>
     */
    protected String toString(final Section s, final String name)
    {
        final StringBuffer b = new StringBuffer();
        b.append("\n" + name + " Format ID: ");
        b.append(Codec.hexEncode(s.getFormatID()));
        b.append("\n" + name + " Offset: " + s.getOffset());
        b.append("\n" + name + " Section size: " + s.getSize());
        b.append("\n" + name + " Property count: " + s.getPropertyCount());

        final Property[] properties = s.getProperties();
        for (int i = 0; i < properties.length; i++)
        {
            final Property p = properties[i];
            final Object value = p.getValue();
            b.append("\n" + name + " ");
            b.append("PID_");
            b.append(p.getID());
            b.append(' ');
            b.append(s.getPIDString(p.getID()) + ": ");
            if (value instanceof byte[])
            {
                byte[] b2 = (byte[]) value;
                b.append("0x" + Codec.hexEncode(b2, 0, 4));
                b.append(' ');
                b.append("0x" + Codec.hexEncode(b2, 4, b2.length - 4));
            }
            else
                b.append(value.toString());
        }
        return b.toString();
    }

}
