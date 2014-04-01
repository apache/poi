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

import java.io.ByteArrayOutputStream;

import org.apache.poi.ddf.EscherClientDataRecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.record.*;
import org.apache.poi.hslf.usermodel.SlideShow;

/**
 * Represents a movie in a PowerPoint document.
 *
 * @author Yegor Kozlov
 */
public final class MovieShape extends Picture {
    public static final int DEFAULT_MOVIE_THUMBNAIL = -1;

    public static final int MOVIE_MPEG = 1;
    public static final int MOVIE_AVI  = 2;

    /**
     * Create a new <code>Picture</code>
     *
    * @param pictureIdx the index of the picture
     */
    public MovieShape(int movieIdx, int pictureIdx){
        super(pictureIdx, null);
        setMovieIndex(movieIdx);
        setAutoPlay(true);
    }

    /**
     * Create a new <code>Picture</code>
     *
     * @param idx the index of the picture
     * @param parent the parent shape
     */
    public MovieShape(int movieIdx, int idx, Shape parent) {
        super(idx, parent);
        setMovieIndex(movieIdx);
    }

    /**
      * Create a <code>Picture</code> object
      *
      * @param escherRecord the <code>EscherSpContainer</code> record which holds information about
      *        this picture in the <code>Slide</code>
      * @param parent the parent shape of this picture
      */
     protected MovieShape(EscherContainerRecord escherRecord, Shape parent){
        super(escherRecord, parent);
    }

    /**
     * Create a new Placeholder and initialize internal structures
     *
     * @return the created <code>EscherContainerRecord</code> which holds shape data
     */
    protected EscherContainerRecord createSpContainer(int idx, boolean isChild) {
        _escherContainer = super.createSpContainer(idx, isChild);

        setEscherProperty(EscherProperties.PROTECTION__LOCKAGAINSTGROUPING, 0x1000100);
        setEscherProperty(EscherProperties.FILL__NOFILLHITTEST, 0x10001);

        EscherClientDataRecord cldata = new EscherClientDataRecord();
        cldata.setOptions((short)0xF);
        _escherContainer.addChildRecord(cldata);

        OEShapeAtom oe = new OEShapeAtom();
        InteractiveInfo info = new InteractiveInfo();
        InteractiveInfoAtom infoAtom = info.getInteractiveInfoAtom();
        infoAtom.setAction(InteractiveInfoAtom.ACTION_MEDIA);
        infoAtom.setHyperlinkType(InteractiveInfoAtom.LINK_NULL);

        AnimationInfo an = new AnimationInfo();
        AnimationInfoAtom anAtom = an.getAnimationInfoAtom();
        anAtom.setFlag(AnimationInfoAtom.Automatic, true);

        //convert hslf into ddf
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            oe.writeOut(out);
            an.writeOut(out);
            info.writeOut(out);
        } catch(Exception e){
            throw new HSLFException(e);
        }
        cldata.setRemainingData(out.toByteArray());

        return _escherContainer;
    }

    /**
     * Assign a movie to this shape
     *
     * @see org.apache.poi.hslf.usermodel.SlideShow#addMovie(String, int)
     * @param idx  the index of the movie
     */
    public void setMovieIndex(int idx){
        OEShapeAtom oe = (OEShapeAtom)getClientDataRecord(RecordTypes.OEShapeAtom.typeID);
        oe.setOptions(idx);

        AnimationInfo an = (AnimationInfo)getClientDataRecord(RecordTypes.AnimationInfo.typeID);
        if(an != null) {
            AnimationInfoAtom ai = an.getAnimationInfoAtom();
            ai.setDimColor(0x07000000);
            ai.setFlag(AnimationInfoAtom.Automatic, true);
            ai.setFlag(AnimationInfoAtom.Play, true);
            ai.setFlag(AnimationInfoAtom.Synchronous, true);
            ai.setOrderID(idx + 1);
        }
    }

    public void setAutoPlay(boolean flag){
        AnimationInfo an = (AnimationInfo)getClientDataRecord(RecordTypes.AnimationInfo.typeID);
        if(an != null){
            an.getAnimationInfoAtom().setFlag(AnimationInfoAtom.Automatic, flag);
            updateClientData();
        }
    }

    public boolean  isAutoPlay(){
        AnimationInfo an = (AnimationInfo)getClientDataRecord(RecordTypes.AnimationInfo.typeID);
        if(an != null){
            return an.getAnimationInfoAtom().getFlag(AnimationInfoAtom.Automatic);
        }
        return false;
    }

    /**
     * @return UNC or local path to a video file
     */
    public String getPath(){
        OEShapeAtom oe = (OEShapeAtom)getClientDataRecord(RecordTypes.OEShapeAtom.typeID);
        int idx = oe.getOptions();

        SlideShow ppt = getSheet().getSlideShow();
        ExObjList lst = (ExObjList)ppt.getDocumentRecord().findFirstOfType(RecordTypes.ExObjList.typeID);
        if(lst == null) return null;

        Record[]  r = lst.getChildRecords();
        for (int i = 0; i < r.length; i++) {
            if(r[i] instanceof ExMCIMovie){
                ExMCIMovie mci = (ExMCIMovie)r[i];
                ExVideoContainer exVideo = mci.getExVideo();
                int objectId = exVideo.getExMediaAtom().getObjectId();
                if(objectId == idx){
                    return exVideo.getPathAtom().getText();
                }
            }

        }
        return null;
    }
}
