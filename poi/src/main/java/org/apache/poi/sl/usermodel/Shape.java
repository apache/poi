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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public interface Shape<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> {
	ShapeContainer<S,P> getParent();

   /**
    * @return the sheet this shape belongs to
    */
   Sheet<S,P> getSheet();

   /**
    * Returns the anchor (the bounding box rectangle) of this shape.
    * All coordinates are expressed in points (72 dpi).
    *
    * @return the anchor of this shape
    */
   Rectangle2D getAnchor();

   /**
    * @return human-readable name of this shape, e.g. "Rectange 3"
    *
    * @since POI 4.0.0
    */
   String getShapeName();

   /**
    * Convenience method to draw a single shape
    *
    * @param graphics the graphics context
    * @param bounds the rectangle to fit the shape to.
    *   if null, the bounds of the shape are used.
    */
   void draw(Graphics2D graphics, Rectangle2D bounds);


   /**
    * Returns a unique identifier for this shape within the current slide.
    * This ID may be used to assist in uniquely identifying this object so that it can
    * be referred to by other parts of the document.
    * <p>
    * If multiple objects within the same slide share the same id attribute value,
    * then the document shall be considered non-conformant.
    * </p>
    *
    * @return unique id of this shape
    */
   int getShapeId();
}
