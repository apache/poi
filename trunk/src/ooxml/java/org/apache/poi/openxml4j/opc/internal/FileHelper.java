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

package org.apache.poi.openxml4j.opc.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Provide useful method to manage file.
 *
 * @author Julien Chable
 * @version 0.1
 */
public final class FileHelper {

	/**
	 * Get the directory part of the specified file path.
	 *
	 * @param f
	 *            File to process.
	 * @return The directory path from the specified
	 */
	public static File getDirectory(File f) {
		if (f != null) {
			String path = f.getPath();
			int len = path.length();
			int num2 = len;
			while (--num2 >= 0) {
				char ch1 = path.charAt(num2);
				if (ch1 == File.separatorChar) {
					return new File(path.substring(0, num2));
				}
			}
		}
		return null;
	}

	/**
	 * Copy a file.
	 *
	 * @param in
	 *            The source file.
	 * @param out
	 *            The target location.
	 * @throws IOException
	 *             If an I/O error occur.
	 */
	public static void copyFile(File in, File out) throws IOException {
		FileChannel sourceChannel = new FileInputStream(in).getChannel();
		FileChannel destinationChannel = new FileOutputStream(out).getChannel();
		sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		sourceChannel.close();
		destinationChannel.close();
	}

	/**
	 * Get file name from the specified File object.
	 */
	public static String getFilename(File file) {
		if (file != null) {
			String path = file.getPath();
			int len = path.length();
			int num2 = len;
			while (--num2 >= 0) {
				char ch1 = path.charAt(num2);
				if (ch1 == File.separatorChar)
					return path.substring(num2 + 1, len);
			}
		}
		return "";
	}

}
