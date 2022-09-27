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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.AbstractByteArrayOutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.ddf.EscherSpRecord;
import org.apache.poi.hslf.record.ExEmbed;
import org.apache.poi.hslf.record.ExObjList;
import org.apache.poi.hslf.record.ExObjRefAtom;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.Ole10Native;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.ObjectMetaData;
import org.apache.poi.sl.usermodel.ObjectMetaData.Application;
import org.apache.poi.sl.usermodel.ObjectShape;
import org.apache.poi.sl.usermodel.ShapeContainer;


/**
 * A shape representing embedded OLE object.
 */
public final class HSLFObjectShape extends HSLFPictureShape implements ObjectShape<HSLFShape,HSLFTextParagraph> {
    private static final Logger LOG = LogManager.getLogger(HSLFObjectShape.class);

    private ExEmbed _exEmbed;

    /**
     * Create a new {@code OLEShape}
     *
    * @param data the picture data
     */
    public HSLFObjectShape(HSLFPictureData data){
        super(data);
    }

    /**
     * Create a new {@code OLEShape}
     *
     * @param data the picture data
     * @param parent the parent shape
     */
    public HSLFObjectShape(HSLFPictureData data, ShapeContainer<HSLFShape,HSLFTextParagraph> parent) {
        super(data, parent);
    }

    /**
      * Create a {@code OLEShape} object
      *
      * @param escherRecord the {@code EscherSpContainer} record which holds information about
      *        this picture in the {@code Slide}
      * @param parent the parent shape of this picture
      */
    public HSLFObjectShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
        super(escherRecord, parent);
    }

    /**
     * Returns unique identifier for the OLE object.
     *
     * @return the unique identifier for the OLE object
     */
    public int getObjectID(){
        return getEscherProperty(EscherPropertyTypes.BLIP__PICTUREID);
    }

    /**
     * Set the unique identifier for the OLE object and
     * register it in the necessary structures
     *
     * @param objectId the unique identifier for the OLE object
     */
    public void setObjectID(int objectId){
        setEscherProperty(EscherPropertyTypes.BLIP__PICTUREID, objectId);

        EscherContainerRecord ecr = getSpContainer();
        EscherSpRecord spRecord = ecr.getChildById(EscherSpRecord.RECORD_ID);
        if (spRecord != null) {
            spRecord.setFlags(spRecord.getFlags() | EscherSpRecord.FLAG_OLESHAPE);
        } else {
            LOG.atWarn().log("Ole shape record not found.");
        }

        HSLFEscherClientDataRecord cldata = getClientData(true);
        ExObjRefAtom uer = null;
        for (Record r : cldata.getHSLFChildRecords()) {
            if (r.getRecordType() == RecordTypes.ExObjRefAtom.typeID) {
                uer = (ExObjRefAtom)r;
                break;
            }
        }
        if (uer == null) {
            uer = new ExObjRefAtom();
            cldata.addChild(uer);
        }
        uer.setExObjIdRef(objectId);
    }


    /**
     * Returns unique identifier for the OLE object.
     *
     * @return the unique identifier for the OLE object
     */
    @Override
    public HSLFObjectData getObjectData(){
        HSLFSlideShow ppt = getSheet().getSlideShow();
        HSLFObjectData[] ole = ppt.getEmbeddedObjects();

        //persist reference
        ExEmbed exEmbed = getExEmbed();
        HSLFObjectData data = null;
        if(exEmbed != null) {
            int ref = exEmbed.getExOleObjAtom().getObjStgDataRef();

            for (HSLFObjectData hod : ole) {
                if(hod.getExOleObjStg().getPersistId() == ref) {
                    data=hod;
                    // keep searching to return the last persistent object with that refId
                }
            }
        }
        if (data==null) {
            LOG.atWarn().log("OLE data not found");
        }

        return data;
    }

    /**
     * Return the record container for this embedded object.
     *
     * <p>
     * It contains:
     * 1. ExEmbedAtom.(4045)
     * 2. ExOleObjAtom (4035)
     * 3. CString (4026), Instance MenuName (1) used for menus and the Links dialog box.
     * 4. CString (4026), Instance ProgID (2) that stores the OLE Programmatic Identifier.
     *     A ProgID is a string that uniquely identifies a given object.
     * 5. CString (4026), Instance ClipboardName (3) that appears in the paste special dialog.
     * 6. MetaFile( 4033), optional
     * </p>
     */
    public ExEmbed getExEmbed(){
        return getExEmbed(false);
    }

    private ExEmbed getExEmbed(boolean create) {
        if (_exEmbed == null) {
            HSLFSlideShow ppt = getSheet().getSlideShow();

            ExObjList lst = ppt.getDocumentRecord().getExObjList(create);
            if(lst == null){
                LOG.atWarn().log("ExObjList not found");
                return null;
            }

            int id = getObjectID();
            for (Record ch : lst.getChildRecords()) {
                if(ch instanceof ExEmbed){
                    ExEmbed embd = (ExEmbed)ch;
                    if( embd.getExOleObjAtom().getObjID() == id) {
                        _exEmbed = embd;
                    }
                }
            }

            if (_exEmbed == null && create) {
                _exEmbed = new ExEmbed();
                _exEmbed.getExOleObjAtom().setObjID(id);
                lst.appendChildRecord(_exEmbed);
            }
        }
        return _exEmbed;
    }


    /**
     * Returns the instance name of the embedded object, e.g. "Document" or "Workbook".
     *
     * @return the instance name of the embedded object
     */
    public String getInstanceName(){
        ExEmbed ee = getExEmbed();
        return (ee == null) ? null : ee.getMenuName();
    }

    @Override
    public String getFullName(){
        ExEmbed ee = getExEmbed();
        return (ee == null) ? null : ee.getClipboardName();
    }

    public void setFullName(final String fullName) {
        final ExEmbed ex = getExEmbed(true);
        if (ex != null) {
            ex.setClipboardName(fullName);
        }
    }

    @Override
    public String getProgId(){
        ExEmbed ee = getExEmbed();
        return (ee == null) ? null : ee.getProgId();
    }

    public void setProgId(final String progId) {
        final ExEmbed ex = getExEmbed(true);
        if (ex != null) {
            ex.setProgId(progId);
        }
    }

    @Override
    public OutputStream updateObjectData(final Application application, final ObjectMetaData metaData) {
        final ObjectMetaData md = (application != null) ? application.getMetaData() : metaData;
        if (md == null) {
            throw new IllegalStateException("either application or metaData needs to be set");
        }

        // can't use UnsynchronizedByteArrayOutputStream here, because it's final
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                addUpdatedData(md,this);
            }
        };
    }

    private void addUpdatedData(ObjectMetaData md, AbstractByteArrayOutputStream baos) throws IOException {
        try (InputStream bis = FileMagic.prepareToCheckMagic(baos.toInputStream())) {
            final FileMagic fm = FileMagic.valueOf(bis);
            try (POIFSFileSystem poifs = (fm == FileMagic.OLE2) ? new POIFSFileSystem(bis) : new POIFSFileSystem()) {
                if (fm != FileMagic.OLE2) {
                    poifs.createDocument(bis, md.getOleEntry());
                }
                baos.reset();

                Ole10Native.createOleMarkerEntry(poifs);

                poifs.getRoot().setStorageClsid(md.getClassID());

                int oid = getObjectID();
                if (oid == 0) {
                    // assign new embedding
                    oid = getSheet().getSlideShow().addEmbed(poifs);
                    setObjectID(oid);
                } else {
                    final HSLFObjectData od = getObjectData();
                    if (od != null) {
                        poifs.writeFilesystem(baos);
                        od.setData(baos.toByteArray());
                    }
                }

                setProgId(md.getProgId());
                setFullName(md.getObjectName());
            }
        }
    }
}
