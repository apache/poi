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

import org.apache.poi.ddf.AbstractEscherOptRecord;
import org.apache.poi.ddf.EscherComplexProperty;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.ExControl;
import org.apache.poi.hslf.record.ExObjList;
import org.apache.poi.hslf.record.ExObjRefAtom;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFPictureShape;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSheet;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.util.StringUtil;

/**
 * Represents an ActiveX control in a PowerPoint document.
 */
public final class ActiveXShape extends HSLFPictureShape {
    public static final int DEFAULT_ACTIVEX_THUMBNAIL = -1;

    /**
     * Create a new <code>Picture</code>
     *
    * @param pictureData the picture data
     */
    public ActiveXShape(int movieIdx, HSLFPictureData pictureData){
        super(pictureData, null);
        setActiveXIndex(movieIdx);
    }

    /**
      * Create a <code>Picture</code> object
      *
      * @param escherRecord the <code>EscherSpContainer</code> record which holds information about
      *        this picture in the <code>Slide</code>
      * @param parent the parent shape of this picture
      */
     protected ActiveXShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);
    }

    /**
     * Create a new Placeholder and initialize internal structures
     *
     * @return the created <code>EscherContainerRecord</code> which holds shape data
     */
    @Override
    protected EscherContainerRecord createSpContainer(int idx, boolean isChild) {
        EscherContainerRecord ecr = super.createSpContainer(idx, isChild);

        EscherSpRecord spRecord = ecr.getChildById(EscherSpRecord.RECORD_ID);
        spRecord.setFlags(EscherSpRecord.FLAG_HAVEANCHOR | EscherSpRecord.FLAG_HASSHAPETYPE | EscherSpRecord.FLAG_OLESHAPE);

        setShapeType(ShapeType.HOST_CONTROL);
        setEscherProperty(EscherProperties.BLIP__PICTUREID, idx);
        setEscherProperty(EscherProperties.LINESTYLE__COLOR, 0x8000001);
        setEscherProperty(EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x80008);
        setEscherProperty(EscherProperties.SHADOWSTYLE__COLOR, 0x8000002);
        setEscherProperty(EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, -1);

        HSLFEscherClientDataRecord cldata = getClientData(true);
        cldata.addChild(new ExObjRefAtom());

        return ecr;
    }

    /**
     * Assign a control to this shape
     *
     * @see org.apache.poi.hslf.usermodel.HSLFSlideShow#addMovie(String, int)
     * @param idx  the index of the movie
     */
    public void setActiveXIndex(int idx) {
        ExObjRefAtom oe = getClientDataRecord(RecordTypes.ExObjRefAtom.typeID);
        if (oe == null) {
            throw new HSLFException("OEShapeAtom for ActiveX doesn't exist");
        }
        oe.setExObjIdRef(idx);
    }

    public int getControlIndex(){
        int idx = -1;
        ExObjRefAtom oe = getClientDataRecord(RecordTypes.ExObjRefAtom.typeID);
        if(oe != null) idx = oe.getExObjIdRef();
        return idx;
    }

    /**
     * Set a property of this ActiveX control
     * @param key
     * @param value
     */
    public void setProperty(String key, String value){

    }

    /**
     * Document-level container that specifies information about an ActiveX control
     *
     * @return container that specifies information about an ActiveX control
     */
    public ExControl getExControl(){
        int idx = getControlIndex();
        Document doc = getSheet().getSlideShow().getDocumentRecord();
        ExObjList lst = (ExObjList)doc.findFirstOfType(RecordTypes.ExObjList.typeID);
        if (lst == null) {
            return null;
        }
        
        for (Record ch : lst.getChildRecords()) {
            if(ch instanceof ExControl){
                ExControl c = (ExControl)ch;
                if(c.getExOleObjAtom().getObjID() == idx){
                    return c;
                }
            }
        }
        return null;
    }

    @Override
    protected void afterInsert(HSLFSheet sheet){
        ExControl ctrl = getExControl();
        if (ctrl == null) {
            throw new NullPointerException("ExControl is not defined");
        }
        ctrl.getExControlAtom().setSlideId(sheet._getSheetNumber());

        String name = ctrl.getProgId() + "-" + getControlIndex() + '\u0000';
        byte[] data = StringUtil.getToUnicodeLE(name);
        EscherComplexProperty prop = new EscherComplexProperty(EscherProperties.GROUPSHAPE__SHAPENAME, false, data);
        AbstractEscherOptRecord opt = getEscherOptRecord();
        opt.addEscherProperty(prop);
    }
}
