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

import java.io.InputStream;
import java.io.IOException;

import org.apache.poi.hslf.record.ExOleObjStg;

/**
 * A class that represents object data embedded in a slide show.
 *
 * @author Daniel Noll
 */
public class ObjectData {
    /**
     * The record that contains the object data.
     */
    private ExOleObjStg storage;

    /**
     * Creates the object data wrapping the record that contains the object data.
     *
     * @param storage the record that contains the object data.
     */
    public ObjectData(ExOleObjStg storage) {
        this.storage = storage;
    }

    /**
     * Gets an input stream which returns the binary of the embedded data.
     *
     * @return the input stream which will contain the binary of the embedded data.
     */
    public InputStream getData() {
        return storage.getData();
    }

    /**
     * Sets the embedded data.
     *
     * @param data the embedded data.
     */
     public void setData(byte[] data) throws IOException {
        storage.setData(data);    
    }

    /**
     * Return the record that contains the object data.
     *
     * @return the record that contains the object data.
     */
    public ExOleObjStg getExOleObjStg() {
        return storage;
    }
}
