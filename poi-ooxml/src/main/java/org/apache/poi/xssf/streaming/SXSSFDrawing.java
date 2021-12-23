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

package org.apache.poi.xssf.streaming;

import java.util.Iterator;
import java.util.Spliterator;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.ObjectData;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;

/**
 * Streaming version of Drawing.
 * Delegates most tasks to the non-streaming XSSF code.
 * TODO: Potentially, Comment and Chart need a similar streaming wrapper like Picture.
 */
public class SXSSFDrawing implements Drawing<XSSFShape> {
    private final SXSSFWorkbook _wb;
    private final XSSFDrawing _drawing;

    public SXSSFDrawing(SXSSFWorkbook workbook, XSSFDrawing drawing) {
        this._wb = workbook;
        this._drawing = drawing;
    }

    @Override
    public SXSSFPicture createPicture(ClientAnchor anchor, int pictureIndex) {
        XSSFPicture pict = _drawing.createPicture(anchor, pictureIndex);
        return new SXSSFPicture(_wb, pict);
    }

    @Override
    public Comment createCellComment(ClientAnchor anchor) {
        return _drawing.createCellComment(anchor);
    }

    @Override
    public ClientAnchor createAnchor(int dx1, int dy1, int dx2, int dy2, int col1, int row1, int col2, int row2) {
        return _drawing.createAnchor(dx1, dy1, dx2, dy2, col1, row1, col2, row2);
    }

    @Override
    public ObjectData createObjectData(ClientAnchor anchor, int storageId, int pictureIndex) {
        return _drawing.createObjectData(anchor, storageId, pictureIndex);
    }

    @Override
    public Iterator<XSSFShape> iterator() {
        return _drawing.getShapes().iterator();
    }

    /**
     * @since POI 5.2.0
     */
    @Override
    public Spliterator<XSSFShape> spliterator() {
        return _drawing.getShapes().spliterator();
    }

}

