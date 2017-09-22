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
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hslf.record.InteractiveInfoAtom;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSoundData;

/**
 * For each slide iterate over shapes and found associated sound data.
 */
public class SoundFinder {
    public static void main(String[] args) throws IOException {
        try (FileInputStream fis = new FileInputStream(args[0])) {
            try (HSLFSlideShow ppt = new HSLFSlideShow(fis)) {
                HSLFSoundData[] sounds = ppt.getSoundData();

                for (HSLFSlide slide : ppt.getSlides()) {
                    for (HSLFShape shape : slide.getShapes()) {
                        int soundRef = getSoundReference(shape);
                        if (soundRef == -1) continue;


                        System.out.println("Slide[" + slide.getSlideNumber() + "], shape[" + shape.getShapeId() + "], soundRef: " + soundRef);
                        System.out.println("  " + sounds[soundRef].getSoundName());
                        System.out.println("  " + sounds[soundRef].getSoundType());
                    }
                }
            }
        }
    }

    /**
     * Check if a given shape is associated with a sound.
     * @return 0-based reference to a sound in the sound collection
     * or -1 if the shape is not associated with a sound
     */
    protected static int getSoundReference(HSLFShape shape){
        int soundRef = -1;
        //dive into the shape container and search for InteractiveInfoAtom
        InteractiveInfoAtom info = shape.getClientDataRecord(RecordTypes.InteractiveInfo.typeID);
        if (info != null && info.getAction() == InteractiveInfoAtom.ACTION_MEDIA) {
            soundRef = info.getSoundRef();
        }
        return soundRef;
    }
}
