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

package org.apache.poi.hslf.dev;

import org.apache.poi.hslf.*;
import org.apache.poi.hslf.record.*;
import org.apache.poi.hslf.usermodel.SlideShow;

import org.apache.poi.util.LittleEndian;

import java.io.*;
import java.util.Hashtable;

/**
 * Gets all the different things that have Slide IDs (of sorts)
 *  in them, and displays them, so you can try to guess what they
 *  all mean
 */
public final class SlideIdListing {
	private static byte[] fileContents;

	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Need to give a filename");
			System.exit(1);
		}


		// Create the slideshow object, for normal working with
		HSLFSlideShow hss = new HSLFSlideShow(args[0]);
		SlideShow ss = new SlideShow(hss);

		// Grab the base contents
		fileContents = hss.getUnderlyingBytes();
		Record[] records = hss.getRecords();
		Record[] latestRecords = ss.getMostRecentCoreRecords();

		// Grab any records that interest us
		Document document = null;
		for(int i=0; i<latestRecords.length; i++) {
			if(latestRecords[i] instanceof Document) {
				document = (Document)latestRecords[i];
			}
		}

		System.out.println("");


		// Look for SlidePersistAtoms, and report what they have to
		//  say about possible slide IDs
		SlideListWithText[] slwts = document.getSlideListWithTexts();
		for(int i=0; i<slwts.length; i++) {
			Record[] cr = slwts[i].getChildRecords();
			for(int j=0; j<cr.length; j++) {
				if(cr[j] instanceof SlidePersistAtom) {
					SlidePersistAtom spa = (SlidePersistAtom)cr[j];
					System.out.println("SlidePersistAtom knows about slide:");
					System.out.println("\t" + spa.getRefID());
					System.out.println("\t" + spa.getSlideIdentifier());
				}
			}
		}

		System.out.println("");

		// Look for latest core records that are slides or notes
		for(int i=0; i<latestRecords.length; i++) {
			if(latestRecords[i] instanceof Slide) {
				Slide s = (Slide)latestRecords[i];
				SlideAtom sa = s.getSlideAtom();
				System.out.println("Found the latest version of a slide record:");
				System.out.println("\tCore ID is " + s.getSheetId());
				System.out.println("\t(Core Records count is " + i + ")");
				System.out.println("\tDisk Position is " + s.getLastOnDiskOffset());
				System.out.println("\tMaster ID is " + sa.getMasterID());
				System.out.println("\tNotes ID is " + sa.getNotesID());
			}
		}
		System.out.println("");
		for(int i=0; i<latestRecords.length; i++) {
			if(latestRecords[i] instanceof Notes) {
				Notes n = (Notes)latestRecords[i];
				NotesAtom na = n.getNotesAtom();
				System.out.println("Found the latest version of a notes record:");
				System.out.println("\tCore ID is " + n.getSheetId());
				System.out.println("\t(Core Records count is " + i + ")");
				System.out.println("\tDisk Position is " + n.getLastOnDiskOffset());
				System.out.println("\tMatching slide is " + na.getSlideID());
			}
		}

		System.out.println("");

		// Find any persist ones first
		int pos = 0;
		for(int i=0; i<records.length; i++) {
			Record r = records[i];

			if(r.getRecordType() == 6001l) {
				// PersistPtrFullBlock
				System.out.println("Found PersistPtrFullBlock at " + pos + " (" + Integer.toHexString(pos) + ")");
			}
			if(r.getRecordType() == 6002l) {
				// PersistPtrIncrementalBlock
				System.out.println("Found PersistPtrIncrementalBlock at " + pos + " (" + Integer.toHexString(pos) + ")");
				PersistPtrHolder pph = (PersistPtrHolder)r;

				// Check the sheet offsets
				int[] sheetIDs = pph.getKnownSlideIDs();
				Hashtable sheetOffsets = pph.getSlideLocationsLookup();
				for(int j=0; j<sheetIDs.length; j++) {
					Integer id = new Integer(sheetIDs[j]);
					Integer offset = (Integer)sheetOffsets.get(id);

					System.out.println("  Knows about sheet " + id);
					System.out.println("    That sheet lives at " + offset);

					Record atPos = findRecordAtPos(offset.intValue());
					System.out.println("    The record at that pos is of type " + atPos.getRecordType());
					System.out.println("    The record at that pos has class " + atPos.getClass().getName());

					if(! (atPos instanceof PositionDependentRecord)) {
						System.out.println("    ** The record class isn't position aware! **");
					}
				}
			}

			// Increase the position by the on disk size
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			r.writeOut(baos);
			pos += baos.size();
		}

		System.out.println("");
	}


	// Finds the record at a given position
	public static Record findRecordAtPos(int pos) {
		long type = LittleEndian.getUShort(fileContents, pos+2);
		long rlen = LittleEndian.getUInt(fileContents, pos+4);

		Record r = Record.createRecordForType(type,fileContents,pos,(int)rlen+8);

		return r;
	}
}
