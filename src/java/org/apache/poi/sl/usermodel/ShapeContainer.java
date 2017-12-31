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

import java.util.List;


public interface ShapeContainer<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> extends Iterable<S> {
    /**
     * Returns an list containing all of the elements in this container in proper
     * sequence (from first to last element).
     *
     * @return an list containing all of the elements in this container in proper
     *         sequence
     */
	List<S> getShapes();

	void addShape(S shape);

    /**
     * Removes the specified shape from this sheet, if it is present
     * (optional operation).  If this sheet does not contain the element,
     * it is unchanged.
     *
     * @param shape the shape to be removed from this sheet, if present
     * @return <tt>true</tt> if this sheet contained the specified element
     * @throws IllegalArgumentException if the type of the specified shape
     *         is incompatible with this sheet (optional)
     */
	boolean removeShape(S shape);

    /**
     * create a new shape with a predefined geometry and add it to this shape container
     */
    AutoShape<S,P> createAutoShape();

    /**
     * create a new shape with a custom geometry
     */
    FreeformShape<S,P> createFreeform();

    /**
     * create a text box
     */
	TextBox<S,P> createTextBox();
	
    /**
     * create a connector
     */
	ConnectorShape<S,P> createConnector();
	
    /**
     * create a group of shapes belonging to this container
     */
	GroupShape<S,P> createGroup();
	
    /**
     * create a picture belonging to this container
     */
	PictureShape<S,P> createPicture(PictureData pictureData);
	
    /**
     * Create a new Table of the given number of rows and columns
     *
     * @param numRows the number of rows
     * @param numCols the number of columns
     */
	TableShape<S,P> createTable(int numRows, int numCols);
	
	/**
	 * Create a new OLE object shape with the given pictureData as preview image
	 *
	 * @param pictureData the preview image
	 */
    ObjectShape<?,?> createOleShape(PictureData pictureData);
}
