/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package drt.drtcases;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xml.xmlbeans.bindingConfig.BindingConfigDocument;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.impl.binding.bts.BindingFile;
import org.apache.xmlbeans.impl.binding.bts.BindingLoader;
import org.apache.xmlbeans.impl.binding.bts.BindingType;
import org.apache.xmlbeans.impl.binding.bts.BindingTypeName;
import org.apache.xmlbeans.impl.binding.bts.BuiltinBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.ByNameBean;
import org.apache.xmlbeans.impl.binding.bts.CompositeBindingLoader;
import org.apache.xmlbeans.impl.binding.bts.JavaTypeName;
import org.apache.xmlbeans.impl.binding.bts.MethodName;
import org.apache.xmlbeans.impl.binding.bts.QNameProperty;
import org.apache.xmlbeans.impl.binding.bts.SimpleBindingType;
import org.apache.xmlbeans.impl.binding.bts.XmlTypeName;
import org.apache.xmlbeans.impl.binding.bts.BindingFileUtils;
import org.apache.xmlbeans.impl.binding.compile.BindingCompilerTask;
import org.apache.xmlbeans.impl.binding.compile.Schema2Java;
import org.apache.xmlbeans.impl.binding.joust.JavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.SourceJavaOutputStream;
import org.apache.xmlbeans.impl.binding.joust.WriterFactory;
import org.apache.xmlbeans.impl.binding.tylar.TylarWriter;
import org.w3.x2001.xmlSchema.SchemaDocument;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class BindingTests extends TestCase
{
    public BindingTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(BindingTests.class); }
    
    public static boolean verbose = false;
    
    public void testJAXRPCBinding() throws Exception
    {
        // bind
        File typesonlyfile = TestEnv.xbeanCase("schema/typesonly/typesonly.xsd");
        File[] xsds = new File[] {typesonlyfile};
        Schema2Java compiler = new Schema2Java(
          //FIXME please dont use product internals in a test
                BindingCompilerTask.createSchemaTypeSystem(xsds));

        final JavaOutputStream joust = createJoust();
        BindingFile bindingFile = null;
        TylarWriter twriter = new TylarWriter() {
          public JavaOutputStream getJavaOutputStream() { return joust; }
          public void writeBindingFile(BindingFile bf) throws IOException {
            if (verbose) BindingFileUtils.write(bf).save(System.out);
          }
          public void writeSchema(SchemaDocument xsd, String filepath) {}
          public void writeSchemaTypeSystem(SchemaTypeSystem sts) {}
          public void close() {}
        };
        //FIXME this is kinda dumb, just emulating current behavior.
        //real test should create a tylar on disk  -pcal
        compiler.bind(twriter);

        // now compile
        // SimpleSchemaToJavaResultCompiler.Params params = new SimpleSchemaToJavaResultCompiler.Params();
        // File theJar = TestEnv.xbeanOutput("schema/binding/typesonly.jar");
        // params.setOutputJar(theJar);
        // to test later
        //SimpleSchemaToJavaResultCompiler.compile(result, params);
    }
//
//    public void testIsSetAnnotation()
//        throws Exception
//    {
//        final File sourcefile =
//            TestEnv.xbeanCase("marshal/com/mytest/MySubClass.java");
//        Assert.assertTrue(sourcefile.exists());
//
//        File sourcepath =
//            TestEnv.xbeanCase("marshal");
//        Assert.assertTrue(sourcepath.exists());
//        Assert.assertTrue(sourcepath.isDirectory());
//
//        JamServiceFactory jf = JamServiceFactory.getInstance();
//        JamServiceParams params = jf.createServiceParams();
//
//        params.includeSourceFile(new File[]{sourcepath}, sourcefile);
//
//        JamService service = jf.createService(params);
//
//        final Java2Schema j2s = new Java2Schema(service.getAllClasses());
//
////        j2s.setVerbose(true);
//
//
//        final File tmpfile = File.createTempFile("bindingTests", "-tylar");
//        if (!tmpfile.delete()) {
//            throw new AssertionError("delete failed on " + tmpfile);
//        }
//        final boolean ok = tmpfile.mkdirs();
//        Assert.assertTrue("mkdir" + tmpfile + " failed", ok);
//
//        j2s.bindAsExplodedTylar(tmpfile);
//
//    }

  //creates a JavaOutputStream that either spits out to System.out
  //or swallows the source output entirely.  This is temporary.  -pcal
  private JavaOutputStream createJoust() {
    final PrintWriter sourceOut;
    if (verbose) {
      sourceOut = new PrintWriter(System.out);
    } else {
      sourceOut = new PrintWriter(new Writer() { //null output
        public void write(char cbuf[], int off, int len) {}
        public void close() {};
        public void flush() {};
      });
    }
    return new SourceJavaOutputStream
            (new WriterFactory() {
              public Writer createWriter(String x, String y) {
                sourceOut.println("=======================");
                return sourceOut;
              }
            });
  }



    public void testBindingFile() throws Exception
    {
        BindingFile bf = new BindingFile();
        BindingLoader builtins = BuiltinBindingLoader.getInstance();

        // some complex types
        ByNameBean bnb = new ByNameBean(BindingTypeName.forPair(JavaTypeName.forString("com.mytest.MyClass"), XmlTypeName.forString("t=my-type@http://www.mytest.com/")));
        bf.addBindingType(bnb, true, true);
        ByNameBean bnb2 = new ByNameBean(BindingTypeName.forPair(JavaTypeName.forString("com.mytest.YourClass"), XmlTypeName.forString("t=your-type@http://www.mytest.com/")));
        bf.addBindingType(bnb2, true, true);

        // a custom simple type
        SimpleBindingType sbt = new SimpleBindingType(BindingTypeName.forPair(JavaTypeName.forString("java.lang.String"), XmlTypeName.forString("t=custom-string@http://www.mytest.com/")));
        bf.addBindingType(sbt, false, true); // note not from-java-default for String


        // bnb

        QNameProperty prop = new QNameProperty();
        prop.setQName(new QName("http://www.mytest.com/", "myelt"));
        prop.setSetterName(createSetterName("setMyelt",bnb2.getName()));
        prop.setGetterName(createGetterName("getMyelt"));
        prop.setBindingType(bnb2);
        bnb.addProperty(prop);

        prop = new QNameProperty();
        prop.setQName(new QName("http://www.mytest.com/", "myelt2"));
        prop.setSetterName(createSetterName("setMyelt2",bnb.getName()));
        prop.setGetterName(createGetterName("getMyelt2"));
        prop.setBindingType(bnb);
        bnb.addProperty(prop);

        prop = new QNameProperty();
        prop.setQName(new QName("http://www.mytest.com/", "myatt"));
        prop.setSetterName(createSetterName("setMyatt",sbt.getName()));
        prop.setGetterName(createGetterName("getMyatt"));
        prop.setBindingType(sbt);
        bnb.addProperty(prop);

        // now bnb2

        prop = new QNameProperty();
        prop.setQName(new QName("http://www.mytest.com/", "yourelt"));
        prop.setSetterName(createSetterName("setYourelt",bnb2.getName()));
        prop.setGetterName(createGetterName("getYourelt"));
        prop.setBindingType(bnb2);
        bnb2.addProperty(prop);

        prop = new QNameProperty();
        prop.setQName(new QName("http://www.mytest.com/", "yourelt2"));
        prop.setSetterName(createSetterName("setYourelt2",bnb.getName()));
        prop.setGetterName(createGetterName("getYourelt2"));
        prop.setBindingType(bnb);
        bnb2.addProperty(prop);

        // sbt
        sbt.setAsIfXmlType(XmlTypeName.forString("t=string@http://www.w3.org/2001/XMLSchema"));

        // now serialize
        BindingConfigDocument doc = BindingFileUtils.write(bf);
        if (verbose)
            System.out.println(doc.toString());

        // now load
        BindingFile bfc = BindingFileUtils.forDoc(doc);
        BindingLoader lc = CompositeBindingLoader.forPath(new BindingLoader[] {builtins, bfc});
        ByNameBean bnbc = (ByNameBean)bfc.getBindingType(BindingTypeName.forPair(JavaTypeName.forString("com.mytest.MyClass"), XmlTypeName.forString("t=my-type@http://www.mytest.com/")));
        ByNameBean bnb2c = (ByNameBean)bfc.getBindingType(BindingTypeName.forPair(JavaTypeName.forString("com.mytest.YourClass"), XmlTypeName.forString("t=your-type@http://www.mytest.com/")));
        SimpleBindingType sbtc = (SimpleBindingType)bfc.getBindingType(BindingTypeName.forPair(JavaTypeName.forString("java.lang.String"), XmlTypeName.forString("t=custom-string@http://www.mytest.com/")));

        // check loading xsd:float
        {
            QName qn = new QName("http://www.w3.org/2001/XMLSchema", "float");
            XmlTypeName xn = XmlTypeName.forTypeNamed(qn);
            XmlTypeName xns = XmlTypeName.forString("t=float@http://www.w3.org/2001/XMLSchema");
            Assert.assertEquals(xn, xns);
            Assert.assertEquals(xn.hashCode(), xns.hashCode());
            BindingType btype = lc.getBindingType(lc.lookupPojoFor(xn));
            Assert.assertNotNull(btype);
        }

        // check loading xsd:string
        {
            QName qn = new QName("http://www.w3.org/2001/XMLSchema", "string");
            XmlTypeName xn = XmlTypeName.forTypeNamed(qn);
            XmlTypeName xns = XmlTypeName.forString("t=string@http://www.w3.org/2001/XMLSchema");
            Assert.assertEquals(xn, xns);
            Assert.assertEquals(xn.hashCode(), xns.hashCode());
            BindingType btype = lc.getBindingType(lc.lookupPojoFor(xn));
            Assert.assertNotNull(btype);
        }

        // check bnb
        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myelt"));
        Assert.assertEquals(createSetterName("setMyelt",prop.getTypeName()),
                            prop.getSetterName());
        Assert.assertEquals(createGetterName("getMyelt"),
                            prop.getGetterName());
        Assert.assertEquals(bnb2c, lc.getBindingType(prop.getTypeName()));

        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myelt2"));
        Assert.assertEquals(createSetterName("setMyelt2",prop.getTypeName()),
                            prop.getSetterName());
        Assert.assertEquals(createGetterName("getMyelt2"), prop.getGetterName());
        Assert.assertEquals(bnbc, lc.getBindingType(prop.getTypeName()));

        prop = bnbc.getPropertyForElement(new QName("http://www.mytest.com/", "myatt"));
        Assert.assertEquals(createSetterName("setMyatt",prop.getTypeName()),
                            prop.getSetterName());
        Assert.assertEquals(createGetterName("getMyatt"), prop.getGetterName());
        Assert.assertEquals(sbtc, lc.getBindingType(prop.getTypeName()));

        // check bnb2
        prop = bnb2c.getPropertyForElement(new QName("http://www.mytest.com/", "yourelt"));
        Assert.assertEquals(createSetterName("setYourelt", prop.getTypeName()),
                                             prop.getSetterName());
        Assert.assertEquals(createGetterName("getYourelt"), prop.getGetterName());
        Assert.assertEquals(bnb2c, lc.getBindingType(prop.getTypeName()));

        prop = bnb2c.getPropertyForElement(new QName("http://www.mytest.com/", "yourelt2"));
        Assert.assertEquals(createSetterName("setYourelt2",prop.getTypeName()),
                            prop.getSetterName());
        Assert.assertEquals(createGetterName("getYourelt2"), prop.getGetterName());
        Assert.assertEquals(bnbc, lc.getBindingType(prop.getTypeName()));

        // check sbtc
        Assert.assertEquals(XmlTypeName.forString("t=string@http://www.w3.org/2001/XMLSchema"), sbtc.getAsIfXmlType());
    }

  /**
   * Utility method for building up method names in the binding file.
   */
  private static MethodName createGetterName(String methodName) {
    return MethodName.create(methodName);
  }

  /**
   * Utility method for building up method names in the binding file.
   */
  private static MethodName createSetterName(String methodName, BindingTypeName bt) {
    return MethodName.create(methodName,
                             bt.getJavaName());
  }
}
