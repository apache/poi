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
package org.apache.poi.dev;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;

/**
 * Prints out the contents of a OOXML container.
 * Useful for seeing what parts are defined, and how
 *  they're all related to each other.
 */
public class OOXMLLister {
	private OPCPackage container;
	private PrintStream disp;
	
	public OOXMLLister(OPCPackage container) {
		this(container, System.out);
	}
	public OOXMLLister(OPCPackage container, PrintStream disp) {
		this.container = container;
		this.disp = disp;
	}
	
	/**
	 * Figures out how big a given PackagePart is.
	 */
	public static long getSize(PackagePart part) throws IOException {
		InputStream in = part.getInputStream();
		byte[] b = new byte[8192];
		long size = 0;
		int read = 0;
		
		while(read > -1) {
			read = in.read(b);
			if(read > 0) {
				size += read;
			}
		}
		
		return size;
	}
	
	/**
	 * Displays information on all the different
	 *  parts of the OOXML file container.
	 */
	public void displayParts() throws Exception {
		ArrayList<PackagePart> parts = container.getParts();
		for (PackagePart part : parts) {
			disp.println(part.getPartName());
			disp.println("\t" + part.getContentType());
			
			if(! part.getPartName().toString().equals("/docProps/core.xml")) {
				disp.println("\t" + getSize(part) + " bytes");
			}
			
			if(! part.isRelationshipPart()) {
				disp.println("\t" + part.getRelationships().size() + " relations");
				for(PackageRelationship rel : part.getRelationships()) {
					displayRelation(rel, "\t  ");
				}
			}
		}
	}
	/**
	 * Displays information on all the different
	 *  relationships between different parts
	 *  of the OOXML file container.
	 */
	public void displayRelations() throws Exception {
		PackageRelationshipCollection rels = 
			container.getRelationships();
		for (PackageRelationship rel : rels) {
			displayRelation(rel, "");
		}
	}
	private void displayRelation(PackageRelationship rel, String indent) {
		disp.println(indent+"Relationship:");
		disp.println(indent+"\tFrom: "+ rel.getSourceURI());
		disp.println(indent+"\tTo:   " + rel.getTargetURI());
		disp.println(indent+"\tID:   " + rel.getId());
		disp.println(indent+"\tMode: " + rel.getTargetMode());
		disp.println(indent+"\tType: " + rel.getRelationshipType());
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			System.err.println("Use:");
			System.err.println("\tjava HXFLister <filename>");
			System.exit(1);
		}
		
		File f = new File(args[0]);
		if(! f.exists()) {
			System.err.println("Error, file not found!");
			System.err.println("\t" + f.toString());
			System.exit(2);
		}
		
		OOXMLLister lister = new OOXMLLister(
				OPCPackage.open(f.toString(), PackageAccess.READ)
		);
		
		lister.disp.println(f.toString() + "\n");
		lister.displayParts();
		lister.disp.println();
		lister.displayRelations();
	}
}
