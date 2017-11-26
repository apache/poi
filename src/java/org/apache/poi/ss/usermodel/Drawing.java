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
package org.apache.poi.ss.usermodel;

/**
 * High level representation of spreadsheet drawing.
 */
public interface Drawing<T extends Shape> extends ShapeContainer<T> {
	/**
	 * Creates a picture.
	 * @param anchor       the client anchor describes how this picture is
	 *                     attached to the sheet.
	 * @param pictureIndex the index of the picture in the workbook collection
	 *                     of pictures.
	 *
	 * @return the newly created picture.
	 */
	Picture createPicture(ClientAnchor anchor, int pictureIndex);

	/**
	 * Creates a comment.
	 * @param anchor the client anchor describes how this comment is attached
	 *               to the sheet.
	 * @return the newly created comment.
	 */
	Comment createCellComment(ClientAnchor anchor);

	/**
	 * Creates a new client anchor and sets the top-left and bottom-right
	 * coordinates of the anchor.
	 *
	 * @param dx1  the x coordinate in EMU within the first cell.
	 * @param dy1  the y coordinate in EMU within the first cell.
	 * @param dx2  the x coordinate in EMU within the second cell.
	 * @param dy2  the y coordinate in EMU within the second cell.
	 * @param col1 the column (0 based) of the first cell.
	 * @param row1 the row (0 based) of the first cell.
	 * @param col2 the column (0 based) of the second cell.
	 * @param row2 the row (0 based) of the second cell.
	 * @return the newly created client anchor
	 */
	ClientAnchor createAnchor(int dx1, int dy1, int dx2, int dy2, int col1, int row1, int col2, int row2);

    /**
     * Adds a new OLE Package Shape
     *
     * @param anchor       the client anchor describes how this picture is
     *                     attached to the sheet.
     * @param storageId    the storageId returned by {@link Workbook#addOlePackage(byte[], String, String, String)}
     * @param pictureIndex the index of the picture (used as preview image) in the
     *                     workbook collection of pictures.
     *
     * @return newly created shape
     */
    ObjectData createObjectData(ClientAnchor anchor, int storageId, int pictureIndex);
}
