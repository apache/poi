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

import java.awt.geom.Rectangle2D;

public interface GroupShape<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> extends Shape<S,P>, ShapeContainer<S,P>, PlaceableShape<S,P> {

    /**
     * Gets the coordinate space of this group.  All children are constrained
     * to these coordinates.
     *
     * @return the coordinate space of this group
     */
    Rectangle2D getInteriorAnchor();
    
    /**
     * Sets the coordinate space of this group.  All children are constrained
     * to these coordinates.
     *
     * @param anchor the coordinate space of this group
     */
    void setInteriorAnchor(Rectangle2D anchor);
}
