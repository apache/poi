/* Copyright 2003-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.xmlbeans.samples.vxsdb;

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

/**
 * @author Philip Mark Donaghy
 */
public class VelocityXmlBeansDB extends Task {

    private static final Log log = LogFactory.getLog(VelocityXmlBeansDB.class);

    private String template;

    private String output;

    private String schema;

    /**
     * @param output
     *            The output to set.
     */
    public void setOutput(String output) {
        this.output = output;
    }

    /**
     * @param schema
     *            The schema to set.
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * @param template
     *            The template to set.
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Default Constructor
     */
    public VelocityXmlBeansDB() {
    }

    /**
     * Puts the XmlBeans SchemaTypeSystem into the Velocity Context
     */
    public void execute() throws BuildException {

        // Create a Velocity Context and a Velocity Template
        VelocityContext ctx = new VelocityContext();
        Template template = null;

        // Output to a file
        FileWriter writer = null;

        // XmlBeans
        SchemaTypeSystem schemaTypeSystem = null;

        try {

            // Initialize Velocity
            Velocity.init();
            log.info("Using the Velocity template, " + this.template);
            template = Velocity.getTemplate(this.template);

            // Create Schema Type System
            log.info("Using the xml schema, " + this.schema);
            schemaTypeSystem = XmlBeans.compileXsd(
                    new XmlObject[] { XmlBeans.typeLoaderForClassLoader(this.getClass().getClassLoader()).
                            parse(new File(this.schema), null, null) },
					XmlBeans.getBuiltinTypeSystem(),
                    null);

            // Place SchemaTypeSystem in the Velocity Context
            ctx.put("xsd", schemaTypeSystem);

            // Place a exported key Map in the Velocity Context
            ctx.put("exportedKeyMap", createExportedKeyMap(schemaTypeSystem));

            // Write to the file
            log.info("Using the output file, " + this.output);
            writer = new FileWriter(new File(this.output));
            template.merge(ctx, writer);
            writer.close();

        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    /**
     * 
     * @param sts
     * @return
     */
    private Map createExportedKeyMap(SchemaTypeSystem sts) {
        
        // Map of exported keys (foreign keys)
        // The key is the name of the element exporting key(s)
        // The value is a List of tables importing this key
        Map exportedKeyMap = new HashMap();

        // For all global elements and all global types
        // Create a map of exported key lists
        SchemaGlobalElement[] globals = sts.globalElements();
        for (int i = 0; i < globals.length; i++) {
            processProperties(globals[i].getName().getLocalPart().toUpperCase(), globals[i].getType().getProperties(), exportedKeyMap);
        }
        return exportedKeyMap;
    }

    private void processProperties(String tableName, SchemaProperty[] properties, Map exportedKeyMap) {
        // For all properties
        for (int i = 0; i < properties.length; i++) {
            processProperty(tableName, properties[i], exportedKeyMap);
        }
    }

    private void processProperty(String tableName, SchemaProperty property, Map exportedKeyMap) {
        // If property maxOccurs is greater than one or unbounded (null)
        if (property.getMaxOccurs() == null || property.getMaxOccurs().compareTo(BigInteger.ONE) > 0) {
            
            // Tables that import this properties type (ex. line-item type exports a foreign key to purchase-order,
            // PURCHASE_ORDER is in the list of importers) 
            List importers = (List) exportedKeyMap.get(property.getType().getName().toString());
            if (importers == null) {
                importers = new ArrayList();
                exportedKeyMap.put(property.getType().getName().toString(), importers);
            }
            importers.add(tableName);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        VelocityXmlBeansDB beans = new VelocityXmlBeansDB();

        // Verify arguments
        if (args.length < 3) {
            log
                    .error("Usage : java org.apache.xmlbeans.samples.vxsdb.VelocityXmlBeansDB TEMPLATE OUTPUT SCHEMA1 [SCHEMA2] [...]");
            System.exit(1);
        }
        beans.setTemplate(args[0]);
        beans.setOutput(args[1]);
        beans.setSchema(args[2]);
        try {
            beans.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}