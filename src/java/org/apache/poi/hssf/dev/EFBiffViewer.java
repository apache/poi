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

package org.apache.poi.hssf.dev;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 *
 * @author  andy
 */

public class EFBiffViewer
{
    String file;

    /** Creates a new instance of EFBiffViewer */

    @SuppressWarnings("WeakerAccess")
    public EFBiffViewer() {
    }

    public void run() throws IOException {
        try (POIFSFileSystem fs = new POIFSFileSystem(new File(file), true);
         InputStream din = BiffViewer.getPOIFSInputStream(fs)) {
            HSSFRequest req = new HSSFRequest();

            req.addListenerForAllRecords(System.out::println);
            HSSFEventFactory factory = new HSSFEventFactory();

            factory.processEvents(req, din);
        }
    }

    public void setFile(String file)
    {
        this.file = file;
    }

    public static void main(String [] args) throws IOException
    {
        if ((args.length == 1) && !args[ 0 ].equals("--help"))
        {
            EFBiffViewer viewer = new EFBiffViewer();

            viewer.setFile(args[ 0 ]);
            viewer.run();
        }
        else
        {
            System.out.println("EFBiffViewer");
            System.out.println(
                "Outputs biffview of records based on HSSFEventFactory");
            System.out
                .println("usage: java org.apache.poi.hssf.dev.EBBiffViewer "
                         + "filename");
        }
    }
}
