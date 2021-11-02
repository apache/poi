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
package org.apache.poi.xwpf.usermodel;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLatentStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLsdException;

/*
 * Latent styles are style names that are known to the client (i.e., Word) but that
 * are mapped to real styles dynamically within the client. This means that the only
 * thing you can know about a latent style is its name.
 * <p>
 * When generating DOCX files it is useful to know if a given style name is a
 * latent style so the DOCX generator can distinguish between attempts to
 * use a latent style and attempts to use a completely undefined style.
 * </p>
 */
public class XWPFLatentStyles {
    // As of 2016-06-10, POI does not contain a LatentStyle class, nor was one included in the patch for bug 48574.
    protected XWPFStyles styles; //LatentStyle shall know styles
    private CTLatentStyles latentStyles;

    protected XWPFLatentStyles() {
    }

    protected XWPFLatentStyles(CTLatentStyles latentStyles) {
        this(latentStyles, null);
    }

    protected XWPFLatentStyles(CTLatentStyles latentStyles, XWPFStyles styles) {
        this.latentStyles = latentStyles;
        this.styles = styles;
    }

    public int getNumberOfStyles() {
        return latentStyles.sizeOfLsdExceptionArray();
    }

    /**
     * Determines if the specified style name is the name of a latent style.
     * @param latentStyleName The name of the latent style to check for.
     * @return true if the latent style is defined.
     * @since 4.1.2
     */
    public boolean isLatentStyle(String latentStyleName) {
        for (CTLsdException lsd : latentStyles.getLsdExceptionArray()) {
            if (lsd.getName().equals(latentStyleName)) {
                return true;
            }
        }
        return false;
    }
}