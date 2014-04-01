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

package org.apache.poi.hslf;

import java.io.FileNotFoundException;

import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;
import org.apache.poi.hslf.record.CurrentUserAtom;
import org.apache.poi.hslf.record.DocumentEncryptionAtom;
import org.apache.poi.hslf.record.PersistPtrHolder;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.UserEditAtom;

/**
 * This class provides helper functions for determining if a
 *  PowerPoint document is Encrypted.
 * In future, it may also provide Encryption and Decryption
 *  functions, but first we'd need to figure out how
 *  PowerPoint encryption is really done!
 *
 * @author Nick Burch
 */

public final class EncryptedSlideShow
{
   /**
    * Check to see if a HSLFSlideShow represents an encrypted
    *  PowerPoint document, or not
    * @param hss The HSLFSlideShow to check
    * @return true if encrypted, otherwise false
    */
   public static boolean checkIfEncrypted(HSLFSlideShow hss) {
      // Easy way to check - contains a stream
      //  "EncryptedSummary"
      try {
         hss.getPOIFSDirectory().getEntry("EncryptedSummary");
         return true;
      } catch(FileNotFoundException fnfe) {
         // Doesn't have encrypted properties
      }

      // If they encrypted the document but not the properties,
      //  it's harder.
      // We need to see what the last record pointed to by the
      //  first PersistPrtHolder is - if it's a
      //  DocumentEncryptionAtom, then the file's Encrypted
      DocumentEncryptionAtom dea = fetchDocumentEncryptionAtom(hss);
      if(dea != null) {
         return true;
      }
      return false;
   }

	/**
	 * Return the DocumentEncryptionAtom for a HSLFSlideShow, or
	 *  null if there isn't one.
	 * @return a DocumentEncryptionAtom, or null if there isn't one
	 */
	public static DocumentEncryptionAtom fetchDocumentEncryptionAtom(HSLFSlideShow hss) {
		// Will be the last Record pointed to by the
		//  first PersistPrtHolder, if there is one

		CurrentUserAtom cua = hss.getCurrentUserAtom();
		if(cua.getCurrentEditOffset() != 0) {
			// Check it's not past the end of the file
			if(cua.getCurrentEditOffset() > hss.getUnderlyingBytes().length) {
				throw new CorruptPowerPointFileException("The CurrentUserAtom claims that the offset of last edit details are past the end of the file");
			}

			// Grab the details of the UserEditAtom there
			// If the record's messed up, we could AIOOB
			Record r = null;
			try {
				r = Record.buildRecordAtOffset(
						hss.getUnderlyingBytes(),
						(int)cua.getCurrentEditOffset()
				);
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
			if(r == null) { return null; }
			if(! (r instanceof UserEditAtom)) { return null; }
			UserEditAtom uea = (UserEditAtom)r;

			// Now get the PersistPtrHolder
			Record r2 = Record.buildRecordAtOffset(
					hss.getUnderlyingBytes(),
					uea.getPersistPointersOffset()
			);
			if(! (r2 instanceof PersistPtrHolder)) { return null; }
			PersistPtrHolder pph = (PersistPtrHolder)r2;

			// Now get the last record
			int[] slideIds = pph.getKnownSlideIDs();
			int maxSlideId = -1;
			for(int i=0; i<slideIds.length; i++) {
				if(slideIds[i] > maxSlideId) { maxSlideId = slideIds[i]; }
			}
			if(maxSlideId == -1) { return null; }

			int offset = (
					(Integer)pph.getSlideLocationsLookup().get(
							Integer.valueOf(maxSlideId)
					) ).intValue();
			Record r3 = Record.buildRecordAtOffset(
					hss.getUnderlyingBytes(),
					offset
			);

			// If we have a DocumentEncryptionAtom, it'll be this one
			if(r3 instanceof DocumentEncryptionAtom) {
				return (DocumentEncryptionAtom)r3;
			}
		}

		return null;
	}
}
