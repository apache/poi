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
                    Codec.hexEncode(ps.getByteOrder()));
        text.append("\nFormat: " +
                    Codec.hexEncode(ps.getFormat()));
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
