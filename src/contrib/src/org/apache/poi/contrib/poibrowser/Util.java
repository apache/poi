
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
import javax.swing.*;

/**
 * <p>Contains various (well, just one at the moment) static utility
 * methods.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-01-25
 */
public class Util
{

    /**
     * <p>Makes a Swing component inverted by swapping its foreground
     * and background colors. Hint: Depending on your needs it might
     * also be a good idea to call <tt>c.setOpaque(true)</tt>.</p>
     */
    public static void invert(final JComponent c)
    {
        final Color invBackground = c.getForeground();
        final Color invForeground = c.getBackground();
        c.setBackground(invBackground);
        c.setForeground(invForeground);
    }

}

