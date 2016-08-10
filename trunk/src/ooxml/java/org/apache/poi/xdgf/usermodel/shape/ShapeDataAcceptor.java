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

package org.apache.poi.xdgf.usermodel.shape;

import org.apache.poi.xdgf.usermodel.XDGFShape;

/**
 * This acceptor only allows traversal to shapes that have useful data
 * associated with them, and tries to elide details that aren't useful when
 * analyzing the content of a document.
 *
 * Useful is subjective of course, and is defined as any of:
 *
 * - Has non-empty text - Is a 1d shape, such as a line - User specified shapes
 * - The outline of stencil objects - TODO
 */
public class ShapeDataAcceptor implements ShapeVisitorAcceptor {

    @Override
    public boolean accept(XDGFShape shape) {

        if (shape.isDeleted())
            return false;

        // text is interesting
        if (shape.hasText() && shape.getTextAsString().length() != 0)
            return true;

        // 1d shapes are interesting, they create connections
        if (shape.isShape1D())
            return true;

        // User specified shapes are interesting
        if (!shape.hasMaster() && !shape.hasMasterShape())
            return true;

        if (shape.hasMaster() && !shape.hasMasterShape())
            return true;

        // include stencil content, but try to elide stencil interiors
        // if (shape.getXmlObject().isSetMaster())
        // return true;

        if (shape.hasMasterShape() && shape.getMasterShape().isTopmost())
            return true;

        return false;
    }

}
