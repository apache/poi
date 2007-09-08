
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

import java.io.*;

import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.ObjectData;
import org.apache.poi.hslf.usermodel.PictureData;

import junit.framework.TestCase;

public class TestOleEmbedding extends TestCase
{
    /**
     * Tests support for OLE objects.
     *
     * @throws Exception if an error occurs.
     */
    public void testOleEmbedding2003() throws Exception
    {
        String dirname = System.getProperty("HSLF.testdata.path");
        File file = new File(dirname, "ole2-embedding-2003.ppt");
        HSLFSlideShow slideShow = new HSLFSlideShow(new FileInputStream(file));
        try
        {
            // Placeholder EMFs for clients that don't support the OLE components.
            PictureData[] pictures = slideShow.getPictures();
            assertEquals("Should be two pictures", 2, pictures.length);
            //assertDigestEquals("Wrong data for picture 1", "8d1fbadf4814f321bb1ccdd056e3c788", pictures[0].getData());
            //assertDigestEquals("Wrong data for picture 2", "987a698e83559cf3d38a0deeba1cc63b", pictures[1].getData());

            // Actual embedded objects.
            ObjectData[] objects = slideShow.getEmbeddedObjects();
            assertEquals("Should be two objects", 2, objects.length);
            //assertDigestEquals("Wrong data for objecs 1", "0d1fcc61a83de5c4894dc0c88e9a019d", objects[0].getData());
            //assertDigestEquals("Wrong data for object 2", "b323604b2003a7299c77c2693b641495", objects[1].getData());
        }
        finally
        {
            slideShow.close();
        }
    }
}
