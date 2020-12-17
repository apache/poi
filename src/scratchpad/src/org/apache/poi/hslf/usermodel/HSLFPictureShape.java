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

package org.apache.poi.hslf.usermodel;

import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherComplexProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ddf.EscherSimpleProperty;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.sl.draw.DrawPictureShape;
import org.apache.poi.sl.usermodel.PictureShape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.Units;


/**
 * Represents a picture in a PowerPoint document.
 */
public class HSLFPictureShape extends HSLFSimpleShape implements PictureShape<HSLFShape,HSLFTextParagraph> {
    private static final POILogger LOG = POILogFactory.getLogger(HSLFPictureShape.class);

    /**
     * Create a new <code>Picture</code>
     *
    * @param data the picture data
     */
    public HSLFPictureShape(HSLFPictureData data){
        this(data, null);
    }

    /**
     * Create a new <code>Picture</code>
     *
     * @param data the picture data
     * @param parent the parent shape
     */
    public HSLFPictureShape(HSLFPictureData data, ShapeContainer<HSLFShape,HSLFTextParagraph> parent) {
        super(null, parent);
        createSpContainer(data.getIndex(), parent instanceof HSLFGroupShape);
    }

    /**
      * Create a <code>Picture</code> object
      *
      * @param escherRecord the <code>EscherSpContainer</code> record which holds information about
      *        this picture in the <code>Slide</code>
      * @param parent the parent shape of this picture
      */
     protected HSLFPictureShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
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
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherSimpleProperty prop = getEscherProperty(opt, EscherPropertyTypes.BLIP__BLIPTODISPLAY);
        return prop == null ? 0 : prop.getPropertyValue();
    }

    /**
     * Create a new Picture and populate the inital structure of the <code>EscherSp</code> record which holds information about this picture.

     * @param idx the index of the picture which refers to <code>EscherBSE</code> container.
     * @return the create Picture object
     */
    protected EscherContainerRecord createSpContainer(int idx, boolean isChild) {
        EscherContainerRecord ecr = super.createSpContainer(isChild);

        EscherSpRecord spRecord = ecr.getChildById(EscherSpRecord.RECORD_ID);
        spRecord.setOptions((short)((ShapeType.FRAME.nativeId << 4) | 0x2));

        //set default properties for a picture
        AbstractEscherOptRecord opt = getEscherOptRecord();
        setEscherProperty(opt, EscherPropertyTypes.PROTECTION__LOCKAGAINSTGROUPING, 0x800080);

        //another weird feature of powerpoint: for picture id we must add 0x4000.
        setEscherProperty(opt, EscherPropertyTypes.BLIP__BLIPTODISPLAY, true, idx);

        return ecr;
    }

    @SuppressWarnings("resource")
    @Override
    public HSLFPictureData getPictureData(){
        HSLFSlideShow ppt = getSheet().getSlideShow();
        List<HSLFPictureData> pict = ppt.getPictureData();

        EscherBSERecord bse = getEscherBSERecord();
        if (bse == null){
            LOG.log(POILogger.ERROR, "no reference to picture data found ");
        } else {
            for (HSLFPictureData pd : pict) {
                if (pd.getOffset() ==  bse.getOffset()){
                    return pd;
                }
            }
            LOG.log(POILogger.ERROR, "no picture found for our BSE offset ", bse.getOffset());
        }
        return null;
    }

    @SuppressWarnings("resource")
    protected EscherBSERecord getEscherBSERecord(){
        HSLFSlideShow ppt = getSheet().getSlideShow();
        Document doc = ppt.getDocumentRecord();
        EscherContainerRecord dggContainer = doc.getPPDrawingGroup().getDggContainer();
        EscherContainerRecord bstore = HSLFShape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);
        if(bstore == null) {
            LOG.log(POILogger.DEBUG, "EscherContainerRecord.BSTORE_CONTAINER was not found ");
            return null;
        }
        List<EscherRecord> lst = bstore.getChildRecords();
        int idx = getPictureIndex();
        if (idx == 0){
            LOG.log(POILogger.DEBUG, "picture index was not found, returning ");
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
        AbstractEscherOptRecord opt = getEscherOptRecord();
        EscherComplexProperty prop = getEscherProperty(opt, EscherPropertyTypes.BLIP__BLIPFILENAME);
        if (prop == null) return null;
        String name = StringUtil.getFromUnicodeLE(prop.getComplexData());
        return name.trim();
    }

    /**
     * Name of this picture.
     *
     * @param name of this picture
     */
    public void setPictureName(String name){
        AbstractEscherOptRecord opt = getEscherOptRecord();
        byte[] data = StringUtil.getToUnicodeLE(name + '\u0000');
        EscherComplexProperty prop = new EscherComplexProperty(EscherPropertyTypes.BLIP__BLIPFILENAME, false, data.length);
        prop.setComplexData(data);
        opt.addEscherProperty(prop);
    }

    /**
     * By default set the orininal image size
     */
    @Override
    protected void afterInsert(HSLFSheet sh){
        super.afterInsert(sh);

        EscherBSERecord bse = getEscherBSERecord();
        bse.setRef(bse.getRef() + 1);

        Rectangle2D anchor = getAnchor();
        if (anchor.isEmpty()){
            new DrawPictureShape(this).resize();
        }
    }


    @Override
    public Insets getClipping() {
        // The anchor specified by the escher properties is the displayed size,
        // i.e. the size of the already clipped image
        AbstractEscherOptRecord opt = getEscherOptRecord();
        
        double top    = getFractProp(opt, EscherPropertyTypes.BLIP__CROPFROMTOP);
        double bottom = getFractProp(opt, EscherPropertyTypes.BLIP__CROPFROMBOTTOM);
        double left   = getFractProp(opt, EscherPropertyTypes.BLIP__CROPFROMLEFT);
        double right  = getFractProp(opt, EscherPropertyTypes.BLIP__CROPFROMRIGHT);
        
        // if all crop values are zero (the default) then no crop rectangle is set, return null
        return (top==0 && bottom==0 && left==0 && right==0)
            ? null
            : new Insets((int)(top*100000), (int)(left*100000), (int)(bottom*100000), (int)(right*100000));
    }

    @Override
    public ShapeType getShapeType() {
        // this is kind of a hack, as picture/ole shapes can have a shape type of "frame"
        // but rendering is handled like a rectangle
        return ShapeType.RECT;
    }
    
    /**
     * @return the fractional property or 0 if not defined
     */
    private static double getFractProp(AbstractEscherOptRecord opt, EscherPropertyTypes type) {
        EscherSimpleProperty prop = getEscherProperty(opt, type);
        if (prop == null) return 0;
        int fixedPoint = prop.getPropertyValue();
        return Units.fixedPointToDouble(fixedPoint);
    }
}