/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.maven.project.Resource;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 *
 * */
public class SchemaCompilerWrapper {

    private String sourceDir;
    private String sourceSchemas;
    private String xmlConfigs;
    private String javaTargetDir;
    private String classTargetDir;
    private String catalogLocation;
    private String classPath;
    private List resources;
    private boolean buildSchemas;
    //this copy should not end in /
    private String baseSchemaLocation = "schemaorg_apache_xmlbeans/src";

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public String getSourceSchemas() {
        return sourceSchemas;
    }

    public void setSourceSchemas(String sourceSchemas) {
        this.sourceSchemas = sourceSchemas;
    }

    public String getXmlConfigs() {
        return xmlConfigs;
    }

    public void setXmlConfigs(String xmlConfigs) {
        this.xmlConfigs = xmlConfigs;
    }

    public String getJavaTargetDir() {
        return javaTargetDir;
    }

    public void setJavaTargetDir(String javaTargetDir) {
        this.javaTargetDir = javaTargetDir;
    }

    public String getClassTargetDir() {
        return classTargetDir;
    }

    public void setClassTargetDir(String classTargetDir) {
        this.classTargetDir = classTargetDir;
    }

    public String getCatalogLocation() {
        return catalogLocation;
    }

    public void setCatalogLocation(String catalogLocation) {
        this.catalogLocation = catalogLocation;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public List getResources() {
        return resources;
    }

    public void setResources(List resources) {
        this.resources = resources;
    }

    public boolean isBuildSchemas() {
        return buildSchemas;
    }

    public void setBuildSchemas(boolean buildSchemas) {
        this.buildSchemas = buildSchemas;
    }

    public String getBaseSchemaLocation() {
        return baseSchemaLocation;
    }

    public void setBaseSchemaLocation(String baseSchemaLocation) {
        if (baseSchemaLocation != null && !(baseSchemaLocation.length() == 0)) {
            this.baseSchemaLocation = baseSchemaLocation;
        }
    }

    public void compileSchemas() throws Exception {
        List schemas = new ArrayList();
        File base = new File(sourceDir);
        Resource resource = new Resource();
        resource.setDirectory(sourceDir);
        resource.setTargetPath(baseSchemaLocation);
        for (StringTokenizer st = new StringTokenizer(sourceSchemas, ","); st.hasMoreTokens();) {
            String schemaName = st.nextToken();
            schemas.add(new File(base, schemaName));
            resource.addInclude(schemaName);
        }
        resources.add(resource);
        if (buildSchemas) {
            List configs = new ArrayList();

            if (xmlConfigs != null) {
                for (StringTokenizer st = new StringTokenizer(xmlConfigs, ","); st.hasMoreTokens();) {
                    String configName = st.nextToken();
                    configs.add(new File(configName));
                }
            }
            List classPathList = new ArrayList();
            List urls = new ArrayList();
            if (classPath != null) {
                for (StringTokenizer st = new StringTokenizer(classPath, ","); st.hasMoreTokens();) {
                    String classpathElement = st.nextToken();
                    File file = new File(classpathElement);
                    classPathList.add(file);
                    urls.add(file.toURL());
                    System.out.println("Adding to classpath: " + file);
                }
            }
            ClassLoader cl = new URLClassLoader((URL[]) urls.toArray(new URL[] {}));
            EntityResolver entityResolver = null;
            if (catalogLocation != null) {
                CatalogManager catalogManager = CatalogManager.getStaticManager();
                catalogManager.setCatalogFiles(catalogLocation);
                entityResolver = new CatalogResolver();
            }
            URI sourceDirURI = new File(sourceDir).toURI();
            entityResolver = new PassThroughResolver(cl, entityResolver, sourceDirURI, baseSchemaLocation);

            SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
            params.setBaseDir(null);
            params.setXsdFiles((File[])schemas.toArray(new File[] {}));
            params.setWsdlFiles(new File[] {});
            params.setJavaFiles(new File[] {});
            params.setConfigFiles((File[])configs.toArray(new File[] {}));
            params.setClasspath((File[])classPathList.toArray(new File[] {}));
            params.setOutputJar(null);
            params.setName(null);
            params.setSrcDir(new File(javaTargetDir));
            params.setClassesDir(new File(classTargetDir));
            params.setCompiler(null);
            params.setMemoryInitialSize(null);
            params.setMemoryMaximumSize(null);
            params.setNojavac(true);
            params.setQuiet(false);
            params.setVerbose(true);
            params.setDownload(false);
            params.setNoUpa(false);
            params.setNoPvr(false);
            params.setDebug(true);
            params.setErrorListener(new ArrayList());
            params.setRepackage(null);
            params.setExtensions(null);
            params.setMdefNamespaces(null);
            params.setEntityResolver(entityResolver);

            boolean result = SchemaCompiler.compile(params);
            if (!result) {
                Collection errors = params.getErrorListener();
                for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
                    Object o = (Object) iterator.next();
                    System.out.println("xmlbeans error: " + o);
                }
                throw new Exception("Schema compilation failed");
            }
        }

    }

    private static class PassThroughResolver implements EntityResolver {
        private final ClassLoader cl;
        private final EntityResolver delegate;
        private final URI sourceDir;
        //this copy has an / appended
        private final String baseSchemaLocation;

        public PassThroughResolver(ClassLoader cl, EntityResolver delegate, URI sourceDir, String baseSchemaLocation) {
            this.cl = cl;
            this.delegate = delegate;
            this.sourceDir = sourceDir;
            this.baseSchemaLocation = baseSchemaLocation + "/";
        }
        public InputSource resolveEntity(String publicId,
                                         String systemId)
                throws SAXException, IOException {
            if (delegate != null) {
                InputSource is = delegate.resolveEntity(publicId, systemId);
                if (is != null) {
                    return is;
                }
            }
            System.out.println("Could not resolve publicId: " + publicId + ", systemId: " + systemId + " from catalog");
            String localSystemId;
            try {
                 localSystemId = sourceDir.relativize(new URI(systemId)).toString();
            } catch (URISyntaxException e) {
                throw (IOException)new IOException("Could not relativeize systemId").initCause(e);
            }
            InputStream in = cl.getResourceAsStream(localSystemId);
            if (in != null) {
                System.out.println("found in classpath at: " + localSystemId);
                return new InputSource(in);
            }
            in = cl.getResourceAsStream(baseSchemaLocation + localSystemId);
            if (in != null) {
                System.out.println("found in classpath at: META-INF/" + localSystemId);
                return new InputSource(in);
            }
            System.out.println("Not found in classpath, looking in current directory: " + systemId);
            return new InputSource(systemId);
        }

    }
}
