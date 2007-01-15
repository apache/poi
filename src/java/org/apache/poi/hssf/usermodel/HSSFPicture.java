/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.hssf.usermodel;

/**
 * Represents a escher picture.  Eg. A GIF, JPEG etc...
 *
 * @author Glen Stampoultzis
 * @version $Id$
 */
public class HSSFPicture
        extends HSSFSimpleShape
{
    public static final int PICTURE_TYPE_EMF = 0;                // Windows Enhanced Metafile
    public static final int PICTURE_TYPE_WMF = 1;                // Windows Metafile
    public static final int PICTURE_TYPE_PICT = 2;               // Macintosh PICT
    public static final int PICTURE_TYPE_JPEG = 3;               // JFIF
    public static final int PICTURE_TYPE_PNG = 4;                // PNG
    public static final int PICTURE_TYPE_DIB = 5;                // Windows DIB

    int pictureIndex;

    /**
     * Constructs a picture object.
     */
    HSSFPicture( HSSFShape parent, HSSFAnchor anchor )
    {
        super( parent, anchor );
        setShapeType(OBJECT_TYPE_PICTURE);
    }

    public int getPictureIndex()
    {
        return pictureIndex;
    }

    public void setPictureIndex( int pictureIndex )
    {
        this.pictureIndex = pictureIndex;
    }
}
