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
package org.apache.poi.hslf.examples;
import org.apache.poi.ddf.*;
import org.apache.poi.hslf.model.*;
import org.apache.poi.hslf.record.InteractiveInfo;
import org.apache.poi.hslf.record.InteractiveInfoAtom;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.usermodel.*;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;

/**
 * For each slide iterate over shapes and found associated sound data.
 *
 * @author Yegor Kozlov
 */
public class SoundFinder {
    public static void main(String[] args) throws Exception {
        SlideShow ppt = new SlideShow(new FileInputStream(args[0]));
        SoundData[] sounds = ppt.getSoundData();

        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            Shape[] shape = slide[i].getShapes();
            for (int j = 0; j < shape.length; j++) {
                int soundRef = getSoundReference(shape[j]);
                if(soundRef != -1) {
                    System.out.println("Slide["+i+"], shape["+j+"], soundRef: "+soundRef);
                    System.out.println("  " + sounds[soundRef].getSoundName());
                    System.out.println("  " + sounds[soundRef].getSoundType());
                }
            }
        }
    }

    /**
     * Check if a given shape is associated with a sound.
     * @return 0-based reference to a sound in the sound collection
     * or -1 if the shape is not associated with a sound
     */
    protected static int getSoundReference(Shape shape){
        int soundRef = -1;
        //dive into the shape container and search for InteractiveInfoAtom
        EscherContainerRecord spContainer = shape.getSpContainer();
        List spchild = spContainer.getChildRecords();
        for (Iterator it = spchild.iterator(); it.hasNext();) {
            EscherRecord obj = (EscherRecord) it.next();
            if (obj.getRecordId() == EscherClientDataRecord.RECORD_ID) {
                byte[] data = obj.serialize();
                Record[] records = Record.findChildRecords(data, 8,
data.length - 8);
                for (int j = 0; j < records.length; j++) {
                    if (records[j] instanceof InteractiveInfo) {
                        InteractiveInfoAtom info = ((InteractiveInfo)records[j]).getInteractiveInfoAtom();
                        if (info.getAction() == InteractiveInfoAtom.ACTION_MEDIA) {
                            soundRef = info.getSoundRef();
                        }
                    }
                }
            }
        }
        return soundRef;
    }
}
