/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2000 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  nor may "Apache" appear in their name, without prior written
 *  permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 */
package org.apache.poi.hpsf.examples;

import java.io.*;
import java.util.*;
import org.apache.poi.hpsf.*;
import org.apache.poi.poifs.eventfilesystem.*;
import org.apache.poi.util.HexDump;

/**
 * <p>Sample application showing how to read a custom property set of
 * a document. Call it with the document's file name as command line
 * parameter.</p>
 *
 * <p>Explanations can be found in the HPSF HOW-TO.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2003-02-01
 */
public class ReadCustomPropertySets
{

    public static void main(String[] args)
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
        public void processPOIFSReaderEvent(POIFSReaderEvent event)
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
                    int id = p.getID();
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

    static String hex(byte[] bytes)
    {
        return HexDump.dump(bytes, 0L, 0);
    }

}
