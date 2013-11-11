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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * @deprecated (name clash with {@link java.lang.Package} use {@link OPCPackage} instead.
 */
@Deprecated
public abstract class Package extends OPCPackage {
	/**
	 * @deprecated use {@link OPCPackage} 
	 */
    @Deprecated
    protected Package(PackageAccess access) {
		super(access);
	}

	/**
	 * @deprecated use {@link OPCPackage#open(String)} 
	 */
    @Deprecated
	public static Package open(String path) throws InvalidFormatException {
		return open(path, defaultPackageAccess);
	}

	/**
	 * @deprecated use {@link OPCPackage#open(String,PackageAccess)} 
	 */
    @Deprecated
	public static Package open(String path, PackageAccess access)
			throws InvalidFormatException {
		return (Package)OPCPackage.open(path, access);
	}

	/**
	 * @deprecated use {@link OPCPackage#open(InputStream)} 
	 */
    @Deprecated
	public static Package open(InputStream in) throws InvalidFormatException,
			IOException {
		return (Package)OPCPackage.open(in);
	}

	/**
	 * @deprecated use {@link OPCPackage#openOrCreate(java.io.File)}  
	 */
    @Deprecated
	public static Package openOrCreate(File file) throws InvalidFormatException {
		return (Package)OPCPackage.openOrCreate(file);
	}

	/**
	 * @deprecated use {@link OPCPackage#create(String)} 
	 */
    @Deprecated
	public static Package create(String path) {
		return (Package)OPCPackage.create(path);
	}

	/**
	 * @deprecated use {@link OPCPackage#create(File)} 
	 */
    @Deprecated
	public static Package create(File file) {
		return (Package)OPCPackage.create(file);
	}

	/**
	 * @deprecated use {@link OPCPackage#create(OutputStream)} 
	 */
    @Deprecated
	public static Package create(OutputStream output) {
		return (Package)OPCPackage.create(output);
	}
}
