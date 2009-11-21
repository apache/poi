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

package org.apache.poi.hsmf.examples;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

/**
 * Reads one or several Outlook MSG files and for each of them creates
 * a text file from available chunks and a directory that contains
 * attachments.
 * 
 * @author Bruno Girin
 *
 */
public class Msg2txt {
	
	/**
	 * The stem used to create file names for the text file and the directory
	 * that contains the attachments.
	 */
	private String fileNameStem;
	
	/**
	 * The Outlook MSG file being processed.
	 */
	private MAPIMessage msg;
	
	public Msg2txt(String fileName) throws IOException {
		fileNameStem = fileName;
		if(fileNameStem.endsWith(".msg") || fileNameStem.endsWith(".MSG")) {
			fileNameStem = fileNameStem.substring(0, fileNameStem.length() - 4);
		}
		msg = new MAPIMessage(fileName);
	}
	
	/**
	 * Processes the message.
	 * 
	 * @throws IOException if an exception occurs while writing the message out
	 */
	public void processMessage() throws IOException {
		String txtFileName = fileNameStem + ".txt";
		String attDirName = fileNameStem + "-att";
		PrintWriter txtOut = null;
		try {
			txtOut = new PrintWriter(txtFileName);
			try {
				String displayFrom = msg.getDisplayFrom();
				txtOut.println("From: "+displayFrom);
			} catch (ChunkNotFoundException e) {
				// ignore
			}
			try {
				String displayTo = msg.getDisplayTo();
				txtOut.println("To: "+displayTo);
			} catch (ChunkNotFoundException e) {
				// ignore
			}
			try {
				String displayCC = msg.getDisplayCC();
				txtOut.println("CC: "+displayCC);
			} catch (ChunkNotFoundException e) {
				// ignore
			}
			try {
				String displayBCC = msg.getDisplayBCC();
				txtOut.println("BCC: "+displayBCC);
			} catch (ChunkNotFoundException e) {
				// ignore
			}
			try {
				String subject = msg.getSubject();
				txtOut.println("Subject: "+subject);
			} catch (ChunkNotFoundException e) {
				// ignore
			}
			try {
				String body = msg.getTextBody();
				txtOut.println(body);
			} catch (ChunkNotFoundException e) {
				System.err.println("No message body");
			}
			Map attachmentMap = msg.getAttachmentFiles();
			if(attachmentMap.size() > 0) {
				File d = new File(attDirName);
				if(d.mkdir()) {
					for(
							Iterator ii = attachmentMap.entrySet().iterator();
							ii.hasNext();
							) {
						Map.Entry entry = (Map.Entry)ii.next();
						processAttachment(d, entry.getKey().toString(),
								(ByteArrayInputStream)entry.getValue());
					}
				} else {
					System.err.println("Can't create directory "+attDirName);
				}
			}
		} finally {
			if(txtOut != null) {
				txtOut.close();
			}
		}
	}
	
	/**
	 * Processes a single attachment: reads it from the Outlook MSG file and
	 * writes it to disk as an individual file.
	 * 
	 * @param dir the directory in which to write the attachment file
	 * @param fileName the name of the attachment file
	 * @param fileIn the input stream that contains the attachment's data
	 * @throws IOException when any of the file operations fails
	 */
	public void processAttachment(File dir, String fileName,
			ByteArrayInputStream fileIn) throws IOException {
		File f = new File(dir, fileName);
		OutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(f);
			byte[] buffer = new byte[2048];
			int bNum = fileIn.read(buffer);
			while(bNum > 0) {
				fileOut.write(buffer);
				bNum = fileIn.read(buffer);
			}
		} finally {
			try {
				if(fileIn != null) {
					fileIn.close();
				}
			} finally {
				if(fileOut != null) {
					fileOut.close();
				}
			}
		}
	}
	
	/**
	 * Processes the list of arguments as a list of names of Outlook MSG files.
	 * 
	 * @param args the list of MSG files to process
	 */
	public static void main(String[] args) {
		if(args.length <= 0) {
			System.err.println("No files names provided");
		} else {
			for(int i = 0; i < args.length; i++) {
				try {
					Msg2txt processor = new Msg2txt(args[i]);
					processor.processMessage();
				} catch (IOException e) {
					System.err.println("Could not process "+args[i]+": "+e);
				}
			}
		}
	}

}
