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
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.usermodel.ObjectData;
import org.apache.poi.hslf.record.ExObjList;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.ExEmbed;
import org.apache.poi.util.POILogger;


/**
 * A shape representing embedded OLE obejct.
 *
 * @author Yegor Kozlov
 */
public final class OLEShape extends Picture {
    protected ExEmbed _exEmbed;

    /**
     * Create a new <code>OLEShape</code>
     *
    * @param idx the index of the picture
     */
    public OLEShape(int idx){
        super(idx);
    }

    /**
     * Create a new <code>OLEShape</code>
     *
     * @param idx the index of the picture
     * @param parent the parent shape
     */
    public OLEShape(int idx, Shape parent) {
        super(idx, parent);
    }

    /**
      * Create a <code>OLEShape</code> object
      *
      * @param escherRecord the <code>EscherSpContainer</code> record which holds information about
      *        this picture in the <code>Slide</code>
      * @param parent the parent shape of this picture
      */
     protected OLEShape(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);
    }

    /**
     * Returns unique identifier for the OLE object.
     *
     * @return the unique identifier for the OLE object
     */
    public int getObjectID(){
        return getEscherProperty(EscherProperties.BLIP__PICTUREID);
    }

    /**
     * Returns unique identifier for the OLE object.
     *
     * @return the unique identifier for the OLE object
     */
    public ObjectData getObjectData(){
        SlideShow ppt = getSheet().getSlideShow();
        ObjectData[] ole = ppt.getEmbeddedObjects();

        //persist reference
        int ref = getExEmbed().getExOleObjAtom().getObjStgDataRef();
        for (int i = 0; i < ole.length; i++) {
            if(ole[i].getExOleObjStg().getPersistId() == ref) return ole[i];

        }
        logger.log(POILogger.WARN, "OLE data not found");
        return null;
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
        if(_exEmbed == null){
            SlideShow ppt = getSheet().getSlideShow();

            ExObjList lst = ppt.getDocumentRecord().getExObjList();
            if(lst == null){
                logger.log(POILogger.WARN, "ExObjList not found");
                return null;
            }

            int id = getObjectID();
            Record[] ch = lst.getChildRecords();
            for (int i = 0; i < ch.length; i++) {
                if(ch[i] instanceof ExEmbed){
                    ExEmbed embd = (ExEmbed)ch[i];
                    if( embd.getExOleObjAtom().getObjID() == id) _exEmbed = embd;
                }
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
        return getExEmbed().getMenuName();
    }

    /**
     * Returns the full name of the embedded object,
     *  e.g. "Microsoft Word Document" or "Microsoft Office Excel Worksheet".
     *
     * @return the full name of the embedded object
     */
    public String getFullName(){
        return getExEmbed().getClipboardName();
    }

    /**
     * Returns the ProgID that stores the OLE Programmatic Identifier.
     * A ProgID is a string that uniquely identifies a given object, for example,
     * "Word.Document.8" or "Excel.Sheet.8".
     *
     * @return the ProgID
     */
    public String getProgID(){
        return getExEmbed().getProgId();
    }
}
