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

import org.apache.poi.hslf.record.*;

import java.util.ArrayList;

/**
 * A class that represents sound data embedded in a slide show.
 *
 * @author Yegor Kozlov
 */
public final class SoundData {
    /**
     * The record that contains the object data.
     */
    private Sound _container;

    /**
     * Creates the object data wrapping the record that contains the sound data.
     *
     * @param container the record that contains the sound data.
     */
    public SoundData(Sound container) {
        this._container = container;
    }

    /**
     * Name of the sound (e.g. "crash")
     *
     * @return name of the sound
     */
    public String getSoundName(){
        return _container.getSoundName();
    }

    /**
     * Type of the sound (e.g. ".wav")
     *
     * @return type of the sound
     */
    public String getSoundType(){
        return _container.getSoundType();
    }

    /**
     * Gets an input stream which returns the binary of the sound data.
     *
     * @return the input stream which will contain the binary of the sound data.
     */
    public byte[] getData() {
        return _container.getSoundData();
    }

    /**
     * Find all sound records in the supplied Document records
     *
     * @param document the document to find in
     * @return the array with the sound data
     */
    public static SoundData[] find(Document document){
        ArrayList<SoundData> lst = new ArrayList<SoundData>();
        Record[] ch = document.getChildRecords();
        for (int i = 0; i < ch.length; i++) {
            if(ch[i].getRecordType() == RecordTypes.SoundCollection.typeID){
                RecordContainer col = (RecordContainer)ch[i];
                Record[] sr = col.getChildRecords();
                for (int j = 0; j < sr.length; j++) {
                    if(sr[j] instanceof Sound){
                        lst.add(new SoundData((Sound)sr[j]));
                    }
                }
            }

        }
        return lst.toArray(new SoundData[lst.size()]);
    }
}
