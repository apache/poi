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

package org.apache.poi.hpsf.basic;

import org.apache.poi.poifs.filesystem.POIFSDocumentPath;

/**
 * <p>A POI file just for testing.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 */
public class POIFile
{

    private String name;
    private POIFSDocumentPath path;
    private byte[] bytes;


    /**
     * <p>Sets the POI file's name.</p>
     *
     * @param name The POI file's name.
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * <p>Returns the POI file's name.</p>
     *
     * @return The POI file's name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * <p>Sets the POI file's path.</p>
     *
     * @param path The POI file's path.
     */
    public void setPath(final POIFSDocumentPath path)
    {
        this.path = path;
    }

    /**
     * <p>Returns the POI file's path.</p>
     *
     * @return The POI file's path.
     */
    public POIFSDocumentPath getPath()
    {
        return path;
    }

    /**
     * <p>Sets the POI file's content bytes.</p>
     *
     * @param bytes The POI file's content bytes.
     */
    public void setBytes(final byte[] bytes)
    {
        this.bytes = bytes;
    }

    /**
     * <p>Returns the POI file's content bytes.</p>
     *
     * @return The POI file's content bytes.
     */
    public byte[] getBytes()
    {
        return bytes;
    }

}
