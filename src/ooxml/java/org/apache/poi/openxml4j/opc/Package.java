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
import java.util.Date;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.opc.internal.ZipContentTypeManager;
import org.apache.poi.openxml4j.util.Nullable;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * @deprecated (name clash with {@link java.lang.Package} use {@link OPCPackage} instead.
 * 
 * @author Julien Chable, CDubet
 * 
 */
@Deprecated
public abstract class Package extends OPCPackage {

	/**
	 * Logger.
	 */
    private static POILogger logger = POILogFactory.getLogger(Package.class);


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
		if (path == null || "".equals(path.trim())
				|| (new File(path).exists() && new File(path).isDirectory()))
			throw new IllegalArgumentException("path");

		Package pack = new ZipPackage(path, access);
		if (pack.partList == null && access != PackageAccess.WRITE) {
			pack.getParts();
		}
		pack.originalPackagePath = new File(path).getAbsolutePath();
		return pack;
	}

	/**
	 * @deprecated use {@link OPCPackage#open(InputStream)} 
	 */
    @Deprecated
	public static Package open(InputStream in) throws InvalidFormatException,
			IOException {
		Package pack = new ZipPackage(in, PackageAccess.READ);
		if (pack.partList == null) {
			pack.getParts();
		}
		return pack;
	}

	/**
	 * @deprecated use {@link OPCPackage#openOrCreate(java.io.File)}  
	 */
    @Deprecated
	public static Package openOrCreate(File file) throws InvalidFormatException {
		Package retPackage = null;
		if (file.exists()) {
			retPackage = open(file.getAbsolutePath());
		} else {
			retPackage = create(file);
		}
		return retPackage;
	}

	/**
	 * @deprecated use {@link OPCPackage#create(String)} 
	 */
    @Deprecated
	public static Package create(String path) {
		return create(new File(path));
	}

	/**
	 * @deprecated use {@link OPCPackage#create(File)} 
	 */
    @Deprecated
	public static Package create(File file) {
		if (file == null || (file.exists() && file.isDirectory()))
			throw new IllegalArgumentException("file");

		if (file.exists()) {
			throw new InvalidOperationException(
					"This package (or file) already exists : use the open() method or delete the file.");
		}

		// Creates a new package
		Package pkg = null;
		pkg = new ZipPackage();
		pkg.originalPackagePath = file.getAbsolutePath();

		configurePackage(pkg);
		return pkg;
	}

	/**
	 * @deprecated use {@link OPCPackage#create(OutputStream)} 
	 */
    @Deprecated
	public static Package create(OutputStream output) {
		Package pkg = null;
		pkg = new ZipPackage();
		pkg.originalPackagePath = null;
		pkg.output = output;

		configurePackage(pkg);
		return pkg;
	}

	/**
	 * Configure the package.
	 * 
	 * @param pkg
	 */
	private static void configurePackage(Package pkg) {
		try {
			// Content type manager
			pkg.contentTypeManager = new ZipContentTypeManager(null, pkg);
			// Add default content types for .xml and .rels
			pkg.contentTypeManager
					.addContentType(
							PackagingURIHelper
									.createPartName(PackagingURIHelper.PACKAGE_RELATIONSHIPS_ROOT_URI),
							ContentTypes.RELATIONSHIPS_PART);
			pkg.contentTypeManager
					.addContentType(PackagingURIHelper
							.createPartName("/default.xml"),
							ContentTypes.PLAIN_OLD_XML);

			// Init some Package properties
			pkg.packageProperties = new PackagePropertiesPart(pkg,
					PackagingURIHelper.CORE_PROPERTIES_PART_NAME);
			pkg.packageProperties.setCreatorProperty("Generated by OpenXML4J");
			pkg.packageProperties.setCreatedProperty(new Nullable<Date>(
					new Date()));
		} catch (InvalidFormatException e) {
			// Should never happen
			throw new IllegalStateException(e);
		}
	}


}
