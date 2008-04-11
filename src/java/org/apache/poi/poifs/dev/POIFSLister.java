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

package org.apache.poi.poifs.dev;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * A lister of the entries in POIFS files.
 * 
 * Much simpler than {@link POIFSViewer}
 */
public class POIFSLister {
    /**
     * Display the entries of multiple POIFS files
     *
     * @param args the names of the files to be displayed
     */
    public static void main(final String args[]) throws IOException {
        if (args.length == 0)
        {
            System.err.println("Must specify at least one file to view");
            System.exit(1);
        }

        for (int j = 0; j < args.length; j++)
        {
            viewFile(args[ j ]);
        }
    }

    public static void viewFile(final String filename) throws IOException
    {
    	POIFSFileSystem fs = new POIFSFileSystem(
    			new FileInputStream(filename)
    	);
    	displayDirectory(fs.getRoot(), "");
    }
    
    public static void displayDirectory(DirectoryNode dir, String indent) {
    	System.out.println(indent + dir.getName() + " -");
    	String newIndent = indent + "  ";
    	
    	for(Iterator it = dir.getEntries(); it.hasNext(); ) {
    		Object entry = it.next();
    		if(entry instanceof DirectoryNode) {
    			displayDirectory((DirectoryNode)entry, newIndent);
    		} else {
    			DocumentNode doc = (DocumentNode)entry;
    			String name = doc.getName();
    			if(name.charAt(0) < 10) {
    				String altname = "(0x0" + (int)name.charAt(0) + ")" + name.substring(1);
    				name = name.substring(1) + " <" + altname + ">";
    			}
    			System.out.println(newIndent + name);
    		}
    	}
    }
}