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

import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherPropertyTypes;
import org.apache.poi.hslf.record.AnimationInfo;
import org.apache.poi.hslf.record.AnimationInfoAtom;
import org.apache.poi.hslf.record.ExMCIMovie;
import org.apache.poi.hslf.record.ExObjList;
import org.apache.poi.hslf.record.ExObjRefAtom;
import org.apache.poi.hslf.record.ExVideoContainer;
import org.apache.poi.hslf.record.HSLFEscherClientDataRecord;
import org.apache.poi.hslf.record.InteractiveInfo;
import org.apache.poi.hslf.record.InteractiveInfoAtom;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFPictureShape;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.sl.usermodel.ShapeContainer;

/**
 * Represents a movie in a PowerPoint document.
 *
 * @author Yegor Kozlov
 */
public final class MovieShape extends HSLFPictureShape {
    public static final int DEFAULT_MOVIE_THUMBNAIL = -1;

    public static final int MOVIE_MPEG = 1;
    public static final int MOVIE_AVI  = 2;

    /**
     * Create a new <code>Picture</code>
     *
    * @param pictureData the picture data
     */
    public MovieShape(int movieIdx, HSLFPictureData pictureData){
        super(pictureData, null);
        setMovieIndex(movieIdx);
        setAutoPlay(true);
    }

    /**
     * Create a new <code>Picture</code>
     *
     * @param pictureData the picture data
     * @param parent the parent shape
     */
    public MovieShape(int movieIdx, HSLFPictureData pictureData, ShapeContainer<HSLFShape,HSLFTextParagraph> parent) {
        super(pictureData, parent);
        setMovieIndex(movieIdx);
    }

    /**
      * Create a <code>Picture</code> object
      *
      * @param escherRecord the <code>EscherSpContainer</code> record which holds information about
      *        this picture in the <code>Slide</code>
      * @param parent the parent shape of this picture
      */
    public MovieShape(EscherContainerRecord escherRecord, ShapeContainer<HSLFShape,HSLFTextParagraph> parent){
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

        setEscherProperty(EscherPropertyTypes.PROTECTION__LOCKAGAINSTGROUPING, 0x1000100);
        setEscherProperty(EscherPropertyTypes.FILL__NOFILLHITTEST, 0x10001);

        ExObjRefAtom oe = new ExObjRefAtom();
        InteractiveInfo info = new InteractiveInfo();
        InteractiveInfoAtom infoAtom = info.getInteractiveInfoAtom();
        infoAtom.setAction(InteractiveInfoAtom.ACTION_MEDIA);
        infoAtom.setHyperlinkType(InteractiveInfoAtom.LINK_NULL);

        AnimationInfo an = new AnimationInfo();
        AnimationInfoAtom anAtom = an.getAnimationInfoAtom();
        anAtom.setFlag(AnimationInfoAtom.Automatic, true);

        HSLFEscherClientDataRecord cldata = getClientData(true);
        cldata.addChild(oe);
        cldata.addChild(an);
        cldata.addChild(info);

        return ecr;
    }

    /**
     * Assign a movie to this shape
     *
     * @see org.apache.poi.hslf.usermodel.HSLFSlideShow#addMovie(String, int)
     * @param idx  the index of the movie
     */
    public void setMovieIndex(int idx){
        ExObjRefAtom oe = getClientDataRecord(RecordTypes.ExObjRefAtom.typeID);
        oe.setExObjIdRef(idx);

        AnimationInfo an = getClientDataRecord(RecordTypes.AnimationInfo.typeID);
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
        AnimationInfo an = getClientDataRecord(RecordTypes.AnimationInfo.typeID);
        if(an != null){
            an.getAnimationInfoAtom().setFlag(AnimationInfoAtom.Automatic, flag);
        }
    }

    public boolean  isAutoPlay(){
        AnimationInfo an = getClientDataRecord(RecordTypes.AnimationInfo.typeID);
        if(an != null){
            return an.getAnimationInfoAtom().getFlag(AnimationInfoAtom.Automatic);
        }
        return false;
    }

    /**
     * @return UNC or local path to a video file
     */
    @SuppressWarnings("resource")
    public String getPath(){
        ExObjRefAtom oe = getClientDataRecord(RecordTypes.ExObjRefAtom.typeID);
        int idx = oe.getExObjIdRef();

        HSLFSlideShow ppt = getSheet().getSlideShow();
        ExObjList lst = (ExObjList)ppt.getDocumentRecord().findFirstOfType(RecordTypes.ExObjList.typeID);
        if(lst == null) {
            return null;
        }

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
