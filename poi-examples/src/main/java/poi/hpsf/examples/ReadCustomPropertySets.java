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

package org.apache.poi.hpsf.examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hpsf.NoPropertySetStreamException;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.PropertySetFactory;
import org.apache.poi.hpsf.Section;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.util.HexDump;

/**
 * <p>Sample application showing how to read a document's custom property set.
 * Call it with the document's file name as command-line parameter.</p>
 *
 * <p>Explanations can be found in the HPSF HOW-TO.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class ReadCustomPropertySets
{

    /**
     * <p>Runs the example program.</p>
     *
     * @param args Command-line arguments (unused).
     * @throws IOException if any I/O exception occurs.
     */
    public static void main(final String[] args)
        throws IOException
    {
        final String filename = args[0];
        POIFSReader r = new POIFSReader();

        /* Register a listener for *all* documents. */
        r.registerListener(new MyPOIFSReaderListener());
        r.read(new FileInputStream(filename));
    }


    static class MyPOIFSReaderListener implements POIFSReaderListener
    {
        public void processPOIFSReaderEvent(final POIFSReaderEvent event)
        {
            PropertySet ps = null;
            try
            {
                ps = PropertySetFactory.create(event.getStream());
            }
            catch (NoPropertySetStreamException ex)
            {
                out("No property set stream: \"" + event.getPath() +
                    event.getName() + "\"");
                return;
            }
            catch (Exception ex)
            {
                throw new RuntimeException
                    ("Property set stream \"" +
                     event.getPath() + event.getName() + "\": " + ex);
            }

            /* Print the name of the property set stream: */
            out("Property set stream \"" + event.getPath() +
                event.getName() + "\":");

            /* Print the number of sections: */
            final long sectionCount = ps.getSectionCount();
            out("   No. of sections: " + sectionCount);

            /* Print the list of sections: */
            List sections = ps.getSections();
            int nr = 0;
            for (Iterator i = sections.iterator(); i.hasNext();)
            {
                /* Print a single section: */
                Section sec = (Section) i.next();
                out("   Section " + nr++ + ":");
                String s = hex(sec.getFormatID().getBytes());
                s = s.substring(0, s.length() - 1);
                out("      Format ID: " + s);

                /* Print the number of properties in this section. */
                int propertyCount = sec.getPropertyCount();
                out("      No. of properties: " + propertyCount);

                /* Print the properties: */
                Property[] properties = sec.getProperties();
                for (int i2 = 0; i2 < properties.length; i2++)
                {
                    /* Print a single property: */
                    Property p = properties[i2];
                    long id = p.getID();
                    long type = p.getType();
                    Object value = p.getValue();
                    out("      Property ID: " + id + ", type: " + type +
                        ", value: " + value);
                }
            }
        }
    }

    static void out(final String msg)
    {
        System.out.println(msg);
    }

    static String hex(final byte[] bytes)
    {
        return HexDump.dump(bytes, 0L, 0);
    }

}
