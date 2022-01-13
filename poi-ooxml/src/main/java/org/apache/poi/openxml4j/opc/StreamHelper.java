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

package org.apache.poi.openxml4j.opc;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.XMLHelper;
import org.w3c.dom.Document;

public final class StreamHelper {

    private StreamHelper() {
        // Do nothing
    }

    /**
     * Save the document object in the specified output stream.
     *
     * @param xmlContent
     *            The XML document.
     * @param outStream
     *            The OutputStream in which the XML document will be written.
     * @return <b>true</b> if the xml is successfully written in the stream,
     *         else <b>false</b>.
     */
    public static boolean saveXmlInStream(Document xmlContent,
                                          OutputStream outStream) {
        try {
            Transformer trans = XMLHelper.newTransformer();
            Source xmlSource = new DOMSource(xmlContent);
            // prevent close of stream by transformer:
            Result outputTarget = new StreamResult(new FilterOutputStream(
                    outStream) {
                @Override
                public void write(byte[] b, int off, int len)
                        throws IOException {
                    out.write(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    out.flush(); // only flush, don't close!
                }
            });
            // xmlContent.setXmlStandalone(true);
            trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            // don't indent xml documents, the indent will cause errors in calculating the xml signature
            // because of different handling of linebreaks in Windows/Unix
            trans.setOutputProperty(OutputKeys.INDENT, "no");
            trans.setOutputProperty(OutputKeys.STANDALONE, "yes");
            trans.transform(xmlSource, outputTarget);
        } catch (TransformerException e) {
            return false;
        }
        return true;
    }

    /**
     * Copy the input stream into the output stream.
     *
     * @param inStream
     *            The source stream.
     * @param outStream
     *            The destination stream.
     * @return <b>true</b> if the operation succeed, else return <b>false</b>.
     */
    public static boolean copyStream(InputStream inStream, OutputStream outStream) {
        try {
            IOUtils.copy(inStream, outStream);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
