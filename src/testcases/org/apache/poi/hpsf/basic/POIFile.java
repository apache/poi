
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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
 * @since 2002-07-20
 * @version $Id$
 */
public class POIFile
{

    private String name;
    private POIFSDocumentPath path;
    private byte[] bytes;

    public void setName(final String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setPath(final POIFSDocumentPath path)
    {
        this.path = path;
    }

    public POIFSDocumentPath getPath()
    {
        return path;
    }

    public void setBytes(final byte[] bytes)
    {
        this.bytes = bytes;
    }

    public byte[] getBytes()
    {
        return bytes;
    }

}
