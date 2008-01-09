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

package org.apache.poi.hssf.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

import org.apache.xmlbeans.XmlException;
import org.openxml4j.opc.PackagePart;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.SstDocument;


public class SharedStringsTable extends LinkedList<String> {
    public static final String MAIN_SML_NS_URI = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    
    private SstDocument doc; 
    private PackagePart part;

    public SharedStringsTable(PackagePart part) throws IOException, XmlException {
        this.part = part;
    	doc = SstDocument.Factory.parse(
    			part.getInputStream()
    	);
    	read();
    }

    private void read() {
    	CTRst[] sts = doc.getSst().getSiArray();
    	for (int i = 0; i < sts.length; i++) {
			add(sts[i].getT());
		}
    }
    
    /**
     * Writes the current shared strings table into
     *  the associated OOXML PackagePart
     */
    public void write() throws IOException {
    	CTSst sst = doc.getSst();
    	
    	// Remove the old list
    	for(int i=sst.sizeOfSiArray() - 1; i>=0; i--) {
    		sst.removeSi(i);
    	}
    	
    	// Add the new one
    	for(String s : this) {
    		sst.addNewSi().setT(s);
    	}
    	
    	// Update the counts
    	sst.setCount(this.size());
    	sst.setUniqueCount(this.size());
    	
    	// Write out
    	OutputStream out = part.getOutputStream();
    	doc.save(out);
    	out.close();
    }
}
