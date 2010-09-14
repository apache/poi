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

import java.io.IOException;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLRelation;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.util.IOUtils;

/**
 * Raw picture data, normally attached to a WordprocessingML Drawing.
 * As a rule, pictures are stored in the /word/media/ part of a WordprocessingML package.
 */

/**
 * @author Philipp Epp
 *
 */
public class XWPFPictureData extends POIXMLDocumentPart {
	
    /**
     * Relationships for each known picture type
     */
    protected static final POIXMLRelation[] RELATIONS;
    static {
        RELATIONS = new POIXMLRelation[8];
        RELATIONS[Document.PICTURE_TYPE_EMF] =  XWPFRelation.IMAGE_EMF;
        RELATIONS[Document.PICTURE_TYPE_WMF] =  XWPFRelation.IMAGE_WMF;
        RELATIONS[Document.PICTURE_TYPE_PICT] = XWPFRelation.IMAGE_PICT;
        RELATIONS[Document.PICTURE_TYPE_JPEG] = XWPFRelation.IMAGE_JPEG;
        RELATIONS[Document.PICTURE_TYPE_PNG] =  XWPFRelation.IMAGE_PNG;
        RELATIONS[Document.PICTURE_TYPE_DIB] =  XWPFRelation.IMAGE_DIB;
    }
    /**
     * Create a new XWPFGraphicData node
     *
     */
	protected XWPFPictureData() {
        super();
    }

    /**
     * Construct XWPFPictureData from a package part
     *
     * @param part the package part holding the drawing data,
     * @param rel  the package relationship holding this drawing,
     * the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/image
     */
	
	public XWPFPictureData(PackagePart part, PackageRelationship rel) {
        super(part, rel);
    }
    
    /**
     * Gets the picture data as a byte array.
     * <p>
     * Note, that this call might be expensive since all the picture data is copied into a temporary byte array.
     * You can grab the picture data directly from the underlying package part as follows:
     * <br/>
     * <code>
     * InputStream is = getPackagePart().getInputStream();
     * </code>
     * </p>
     *
     * @return the Picture data.
     */
    public byte[] getData() {
          try {
			return IOUtils.toByteArray(getPackagePart().getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    /**
     * Returns the file name of the image, eg image7.jpg .
     * The original filename isn't always available, but if it
     *  can be found it's likely to be in the CTDrawing 
     */
    public String getFileName() {
       String name = getPackagePart().getPartName().getName();
       if(name == null)
          return null;
       return name.substring(name.lastIndexOf('/') + 1);
    }
    
    /**
     * Suggests a file extension for this image.
     *
     * @return the file extension.
     */
    public String suggestFileExtension() {
        return getPackagePart().getPartName().getExtension();
    }
    
    /**
     * Return an integer constant that specifies type of this picture
     *
     * @return an integer constant that specifies type of this picture 
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_EMF
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_WMF
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_PICT
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_JPEG
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_PNG
     * @see org.apache.poi.xwpf.usermodel.Document#PICTURE_TYPE_DIB
     */
    public int getPictureType(){
        String contentType = getPackagePart().getContentType();
        for (int i = 0; i < RELATIONS.length; i++) {
            if(RELATIONS[i] == null) continue;

            if(RELATIONS[i].getContentType().equals(contentType)){
                return i;
            }
        }
        return 0;
    }
}
