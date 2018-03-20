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

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

import org.apache.poi.util.HexDump;

/**
 * <p>{@link TreeCellRenderer} for a {@link DocumentDescriptor}. The
 * renderer is extremly rudimentary since displays only the document's
 * name, its size and its fist few bytes.</p>
 */
public class DocumentDescriptorRenderer extends DefaultTreeCellRenderer
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
        final DocumentDescriptor d = (DocumentDescriptor)
            ((DefaultMutableTreeNode) value).getUserObject();
        final JPanel p = new JPanel();
        final JTextArea text = new JTextArea();
        text.append(renderAsString(d));
        text.setFont(new Font("Monospaced", Font.PLAIN, 10));
        p.add(text);
        if (selectedCell) {
            Util.invert(text);
        }
        return p;
    }


    /**
     * <p>Renders {@link DocumentDescriptor} as a string.</p>
     */
    protected String renderAsString(final DocumentDescriptor d) {
        return "Name: " +
                d.name +
                " " +
                HexDump.toHex(d.name) +
                "\n" +
                "Size: " +
                d.size +
                " bytes\n" +
                "First bytes: " +
                HexDump.toHex(d.bytes);
    }
}
