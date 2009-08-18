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

package org.apache.poi.hslf.model;

import org.apache.poi.ddf.*;
import org.apache.poi.hslf.usermodel.PictureData;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.blip.Bitmap;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.util.POILogger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * Represents a picture in a PowerPoint document.
 *
 * @author Yegor Kozlov
 */
public class Picture extends SimpleShape {

    /**
    *  Windows Enhanced Metafile (EMF)
    */
    public static final int EMF = 2;

    /**
    *  Windows Metafile (WMF)
    */
    public static final int WMF = 3;

    /**
    * Macintosh PICT
    */
    public static final int PICT = 4;

    /**
    *  JPEG
    */
    public static final int JPEG = 5;

    /**
    *  PNG
    */
    public static final int PNG = 6;

    /**
     * Windows DIB (BMP)
     */
    public static final byte DIB = 7;

    /**
     * Create a new <code>Picture</code>
     *
    * @param idx the index of the picture
     */
    public Picture(int idx){
        this(idx, null);
    }

    /**
     * Create a new <code>Picture</code>
     *
     * @param idx the index of the picture
     * @param parent the parent shape
     */
    public Picture(int idx, Shape parent) {
        super(null, parent);
        _escherContainer = createSpContainer(idx, parent instanceof ShapeGroup);
    }

    /**
      * Create a <code>Picture</code> object
      *
      * @param escherRecord the <code>EscherSpContainer</code> record which holds information about
      *        this picture in the <code>Slide</code>
      * @param parent the parent shape of this picture
      */
     protected Picture(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);
    }

    /**
     * Returns index associated with this picture.
     * Index starts with 1 and points to a EscherBSE record which
     * holds information about this picture.
     *
     * @return the index to this picture (1 based).
     */
    public int getPictureIndex(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)getEscherProperty(opt, EscherProperties.BLIP__BLIPTODISPLAY);
        return prop == null ? 0 : prop.getPropertyValue();
    }

    /**
     * Create a new Picture and populate the inital structure of the <code>EscherSp</code> record which holds information about this picture.

     * @param idx the index of the picture which referes to <code>EscherBSE</code> container.
     * @return the create Picture object
     */
    protected EscherContainerRecord createSpContainer(int idx, boolean isChild) {
        _escherContainer = super.createSpContainer(isChild);
        _escherContainer.setOptions((short)15);

        EscherSpRecord spRecord = _escherContainer.getChildById(EscherSpRecord.RECORD_ID);
        spRecord.setOptions((short)((ShapeTypes.PictureFrame << 4) | 0x2));

        //set default properties for a picture
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        setEscherProperty(opt, EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 0x800080);

        //another weird feature of powerpoint: for picture id we must add 0x4000.
        setEscherProperty(opt, (short)(EscherProperties.BLIP__BLIPTODISPLAY + 0x4000), idx);

        return _escherContainer;
    }

    /**
     * Resize this picture to the default size.
     * For PNG and JPEG resizes the image to 100%,
     * for other types sets the default size of 200x200 pixels.
     */
    public void setDefaultSize(){
        PictureData pict = getPictureData();
        if (pict  instanceof Bitmap){
            BufferedImage img = null;
            try {
               	img = ImageIO.read(new ByteArrayInputStream(pict.getData()));
            }
            catch (IOException e){}
            catch (NegativeArraySizeException ne) {}

            if(img != null) {
                // Valid image, set anchor from it
                setAnchor(new java.awt.Rectangle(0, 0, img.getWidth()*POINT_DPI/PIXEL_DPI, img.getHeight()*POINT_DPI/PIXEL_DPI));
            } else {
                // Invalid image, go with the default metafile size
                setAnchor(new java.awt.Rectangle(0, 0, 200, 200));
            }
        } else {
            //default size of a metafile picture is 200x200
            setAnchor(new java.awt.Rectangle(50, 50, 200, 200));
        }
    }

    /**
     * Returns the picture data for this picture.
     *
     * @return the picture data for this picture.
     */
    public PictureData getPictureData(){
        SlideShow ppt = getSheet().getSlideShow();
        PictureData[] pict = ppt.getPictureData();

        EscherBSERecord bse = getEscherBSERecord();
        if (bse == null){
            logger.log(POILogger.ERROR, "no reference to picture data found ");
        } else {
            for ( int i = 0; i < pict.length; i++ ) {
                if (pict[i].getOffset() ==  bse.getOffset()){
                    return pict[i];
                }
            }
            logger.log(POILogger.ERROR, "no picture found for our BSE offset " + bse.getOffset());
        }
        return null;
    }

    protected EscherBSERecord getEscherBSERecord(){
        SlideShow ppt = getSheet().getSlideShow();
        Document doc = ppt.getDocumentRecord();
        EscherContainerRecord dggContainer = doc.getPPDrawingGroup().getDggContainer();
        EscherContainerRecord bstore = (EscherContainerRecord)Shape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);
        if(bstore == null) {
            logger.log(POILogger.DEBUG, "EscherContainerRecord.BSTORE_CONTAINER was not found ");
            return null;
        }
        List lst = bstore.getChildRecords();
        int idx = getPictureIndex();
        if (idx == 0){
            logger.log(POILogger.DEBUG, "picture index was not found, returning ");
            return null;
        }
        return (EscherBSERecord)lst.get(idx-1);
    }

    /**
     * Name of this picture.
     *
     * @return name of this picture
     */
    public String getPictureName(){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        EscherComplexProperty prop = (EscherComplexProperty)getEscherProperty(opt, EscherProperties.BLIP__BLIPFILENAME);
        String name = null;
        if(prop != null){
            try {
                name = new String(prop.getComplexData(), "UTF-16LE");
                int idx = name.indexOf('\u0000');
                return idx == -1 ? name : name.substring(0, idx);
            } catch (UnsupportedEncodingException e){
                throw new HSLFException(e);
            }
        }
        return name;
    }

    /**
     * Name of this picture.
     *
     * @param name of this picture
     */
    public void setPictureName(String name){
        EscherOptRecord opt = (EscherOptRecord)getEscherChild(_escherContainer, EscherOptRecord.RECORD_ID);
        try {
            byte[] data = (name + '\u0000').getBytes("UTF-16LE");
            EscherComplexProperty prop = new EscherComplexProperty(EscherProperties.BLIP__BLIPFILENAME, false, data);
            opt.addEscherProperty(prop);
        } catch (UnsupportedEncodingException e){
            throw new HSLFException(e);
        }
    }

    /**
     * By default set the orininal image size
     */
    protected void afterInsert(Sheet sh){
        super.afterInsert(sh);

        EscherBSERecord bse = getEscherBSERecord();
        bse.setRef(bse.getRef() + 1);

        java.awt.Rectangle anchor = getAnchor();
        if (anchor.equals(new java.awt.Rectangle())){
            setDefaultSize();
        }
    }

    public void draw(Graphics2D graphics){
        AffineTransform at = graphics.getTransform();
        ShapePainter.paint(this, graphics);

        PictureData data = getPictureData();
        if(data != null) data.draw(graphics, this);

        graphics.setTransform(at);
    }
}
