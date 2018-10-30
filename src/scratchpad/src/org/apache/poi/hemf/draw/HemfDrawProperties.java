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

package org.apache.poi.hemf.draw;

import java.awt.Shape;
import java.awt.geom.Path2D;

import org.apache.poi.hwmf.draw.HwmfDrawProperties;

public class HemfDrawProperties extends HwmfDrawProperties {

    /** Path for path bracket operations */
    protected Path2D path = null;
    protected boolean usePathBracket = false;


    public HemfDrawProperties() {
    }

    public HemfDrawProperties(HemfDrawProperties other) {
        super(other);
        path = (other.path != null) ? (Path2D)other.path.clone() : null;
        // TODO: check how to clone
        clip = other.clip;
    }

    /**
     * @return the current path used for bracket operations
     */
    public Path2D getPath() {
        return path;
    }

    /**
     * Un-/Sets the bracket path
     * @param path the bracket path
     */
    public void setPath(Path2D path) {
        this.path = path;
    }

    /**
     * Use path (bracket) or graphics context for drawing operations
     * @return {@code true}, if the drawing should go to the path bracket,
     *      if {@code false} draw directly to the graphics context
     */
    public boolean getUsePathBracket() {
        return usePathBracket;
    }

    public void setUsePathBracket(boolean usePathBracket) {
        this.usePathBracket = usePathBracket;
    }
}
