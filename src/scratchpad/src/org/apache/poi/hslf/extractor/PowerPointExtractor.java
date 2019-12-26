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

package org.apache.poi.hslf.extractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.hslf.usermodel.HSLFObjectShape;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFSlideShowImpl;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.util.Removal;

/**
 * This class can be used to extract text from a PowerPoint file. Can optionally
 * also get the notes from one.
 *
 * @deprecated in POI 4.0.0, use {@link SlideShowExtractor} instead
 */
@SuppressWarnings("WeakerAccess")
@Deprecated
@Removal(version="5.0.0")
public final class PowerPointExtractor extends POIOLE2TextExtractor {
   	private final SlideShowExtractor<HSLFShape,HSLFTextParagraph> delegate;

	private boolean slidesByDefault = true;
	private boolean notesByDefault;
	private boolean commentsByDefault;
	private boolean masterByDefault;

	/**
	 * Basic extractor. Returns all the text, and optionally all the notes
	 */
	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("Usage:");
			System.err.println("\tPowerPointExtractor [-notes] <file>");
			System.exit(1);
		}

		boolean notes = false;
		boolean comments = false;
        boolean master = true;
        
		String file;
		if (args.length > 1) {
			notes = true;
			file = args[1];
			if (args.length > 2) {
				comments = true;
			}
		} else {
			file = args[0];
		}

		try (PowerPointExtractor ppe = new PowerPointExtractor(file)) {
			System.out.println(ppe.getText(true, notes, comments, master));
		}
	}

	public PowerPointExtractor(final HSLFSlideShow slideShow) {
		super(slideShow.getSlideShowImpl());
		setFilesystem(slideShow);
		delegate = new SlideShowExtractor<>(slideShow);
	}

	/**
	 * Creates a PowerPointExtractor, from a file
	 *
	 * @param fileName The name of the file to extract from
	 */
	public PowerPointExtractor(String fileName) throws IOException {
		this(createHSLF(new File(fileName), Biff8EncryptionKey.getCurrentUserPassword(), true));
	}

	/**
	 * Creates a PowerPointExtractor, from an Input Stream
	 *
	 * @param iStream The input stream containing the PowerPoint document
	 */
	public PowerPointExtractor(InputStream iStream) throws IOException {
		this(createHSLF(iStream, Biff8EncryptionKey.getCurrentUserPassword()));
	}

	/**
	 * Creates a PowerPointExtractor, from an open POIFSFileSystem
	 *
	 * @param fs the POIFSFileSystem containing the PowerPoint document
	 */
	public PowerPointExtractor(POIFSFileSystem fs) throws IOException {
		this(createHSLF(fs, Biff8EncryptionKey.getCurrentUserPassword()));
	}

   /**
    * Creates a PowerPointExtractor, from a specific place
    *  inside an open {@link POIFSFileSystem}
    *
    * @param dir the POIFS Directory containing the PowerPoint document
    */
   public PowerPointExtractor(DirectoryNode dir) throws IOException {
      this(new HSLFSlideShow(dir));
   }

	/**
	 * Creates a PowerPointExtractor, from a HSLFSlideShow
	 *
	 * @param ss the HSLFSlideShow to extract text from
	 */
	public PowerPointExtractor(HSLFSlideShowImpl ss) {
		this(new HSLFSlideShow(ss));
	}

	/**
	 * Should a call to getText() return slide text? Default is yes
	 */
	public void setSlidesByDefault(final boolean slidesByDefault) {
		this.slidesByDefault = slidesByDefault;
		delegate.setSlidesByDefault(slidesByDefault);
	}

	/**
	 * Should a call to getText() return notes text? Default is no
	 */
	public void setNotesByDefault(final boolean notesByDefault) {
		this.notesByDefault = notesByDefault;
		delegate.setNotesByDefault(notesByDefault);
	}

	/**
	 * Should a call to getText() return comments text? Default is no
	 */
	public void setCommentsByDefault(final boolean commentsByDefault) {
		this.commentsByDefault = commentsByDefault;
		delegate.setCommentsByDefault(commentsByDefault);
	}

    /**
     * Should a call to getText() return text from master? Default is no
     */
    public void setMasterByDefault(final boolean masterByDefault) {
    	this.masterByDefault = masterByDefault;
    	delegate.setMasterByDefault(masterByDefault);
    }

	/**
	 * Fetches all the slide text from the slideshow, but not the notes, unless
	 * you've called setSlidesByDefault() and setNotesByDefault() to change this
	 */
	@Override
    public String getText() {
		return delegate.getText();
	}

	/**
	 * Fetches text from the slideshow, be it slide text or note text. Because
	 * the final block of text in a TextRun normally have their last \n
	 * stripped, we add it back
	 *
	 * @param getSlideText fetch slide text
	 * @param getNoteText fetch note text
	 */
	public String getText(boolean getSlideText, boolean getNoteText) {
		return getText(getSlideText,getNoteText,commentsByDefault,masterByDefault);
	}

	public String getText(boolean getSlideText, boolean getNoteText, boolean getCommentText, boolean getMasterText) {
		delegate.setSlidesByDefault(getSlideText);
		delegate.setNotesByDefault(getNoteText);
		delegate.setCommentsByDefault(getCommentText);
		delegate.setMasterByDefault(getMasterText);
		try {
			return delegate.getText();
		} finally {
			delegate.setSlidesByDefault(slidesByDefault);
			delegate.setNotesByDefault(notesByDefault);
			delegate.setCommentsByDefault(commentsByDefault);
			delegate.setMasterByDefault(masterByDefault);
		}
	}

	/**
	 * Fetches all the notes text from the slideshow, but not the slide text
	 */
	public String getNotes() {
		return getText(false, true, false, false);
	}

	@SuppressWarnings("unchecked")
	public List<HSLFObjectShape> getOLEShapes() {
		return (List<HSLFObjectShape>)delegate.getOLEShapes();
	}

	/**
	 * Helper method to avoid problems with compiling code in Eclipse
	 *
	 * Eclipse javac has some bugs with complex casts, this method tries
	 * to work around this.
	 *
	 * @param fs The {@link POIFSFileSystem} to read the document from
	 * @param password The password that should be used or null if no password is necessary.
	 *
	 * @return The created SlideShow
	 *
	 * @throws IOException if an error occurs while reading the data
	 */
	private static HSLFSlideShow createHSLF(POIFSFileSystem fs, String password) throws IOException, EncryptedDocumentException {
		// Note: don't change the code here, it is required for Eclipse to compile the code
		SlideShow slideShowOrig = SlideShowFactory.create(fs, password);
		return (HSLFSlideShow)slideShowOrig;
	}

	/**
	 * Helper method to avoid problems with compiling code in Eclipse
	 *
	 * Eclipse javac has some bugs with complex casts, this method tries
	 * to work around this.
	 *
	 *  @param inp The {@link InputStream} to read data from.
	 *  @param password The password that should be used or null if no password is necessary.
	 *
	 *  @return The created SlideShow
	 *
	 *  @throws IOException if an error occurs while reading the data
	 *  @throws EncryptedDocumentException If the wrong password is given for a protected file
	 */
	private static HSLFSlideShow createHSLF(InputStream inp, String password) throws IOException, EncryptedDocumentException {
		// Note: don't change the code here, it is required for Eclipse to compile the code
		SlideShow slideShowOrig = SlideShowFactory.create(inp, password);
		return (HSLFSlideShow)slideShowOrig;
	}

	/**
	 * Helper method to avoid problems with compiling code in Eclipse
	 *
	 * Eclipse javac has some bugs with complex casts, this method tries
	 * to work around this.
	 *
	 *  @param file The file to read data from.
	 *  @param password The password that should be used or null if no password is necessary.
	 *  @param readOnly If the SlideShow should be opened in read-only mode to avoid writing back
	 *      changes when the document is closed.
	 *
	 *  @return The created SlideShow
	 *
	 *  @throws IOException if an error occurs while reading the data
	 *  @throws EncryptedDocumentException If the wrong password is given for a protected file
	 */
	private static HSLFSlideShow createHSLF(File file, String password, boolean readOnly) throws IOException, EncryptedDocumentException {
		// Note: don't change the code here, it is required for Eclipse to compile the code
		SlideShow slideShowOrig = SlideShowFactory.create(file, password, readOnly);
		return (HSLFSlideShow)slideShowOrig;
	}
}
