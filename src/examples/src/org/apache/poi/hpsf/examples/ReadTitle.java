/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

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
package org.apache.poi.hpsf.examples;

import java.io.*;
import org.apache.poi.hpsf.*;
import org.apache.poi.poifs.eventfilesystem.*;

/**
 * <p>Sample application showing how to read a OLE 2 document's
 * title. Call it with the document's file name as command line
 * parameter.</p>
 *
 * <p>Explanations can be found in the HPSF HOW-TO.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2003-02-01
 */
public class ReadTitle
{

    public static void main(String[] args) throws IOException
    {
        final String filename = args[0];
        POIFSReader r = new POIFSReader();
        r.registerListener(new MyPOIFSReaderListener(),
                           "\005SummaryInformation");
        r.read(new FileInputStream(filename));
    }


    static class MyPOIFSReaderListener implements POIFSReaderListener
    {
        public void processPOIFSReaderEvent(POIFSReaderEvent event)
        {
            SummaryInformation si = null;
            try
            {
                si = (SummaryInformation)
                    PropertySetFactory.create(event.getStream());
            }
            catch (Exception ex)
            {
                throw new RuntimeException
                    ("Property set stream \"" +
                     event.getPath() + event.getName() + "\": " + ex);
            }
            final String title = si.getTitle();
            if (title != null)
                System.out.println("Title: \"" + title + "\"");
            else
                System.out.println("Document has no title.");
        }
    }

}
