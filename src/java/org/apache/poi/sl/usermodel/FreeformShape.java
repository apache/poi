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

package org.apache.poi.sl.usermodel;

import java.awt.geom.Path2D;

public interface FreeformShape<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> extends AutoShape<S,P> {
    /**
     * Gets the shape path.<p>
     *
     * The path is translated in the shape's coordinate system, i.e.
     * freeform.getPath2D().getBounds2D() equals to freeform.getAnchor()
     * (small discrepancies are possible due to rounding errors)
     *
     * @return the path
     */
    Path2D getPath();

    /**
     * Set the shape path
     *
     * @param path  shape outline
     * @return the number of points written
     */
    int setPath(Path2D path);
}
