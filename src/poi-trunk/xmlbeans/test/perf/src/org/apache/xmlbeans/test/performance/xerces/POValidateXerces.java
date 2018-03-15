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
package org.apache.xmlbeans.test.performance.xerces;

import org.apache.xerces.parsers.IntegratedParserConfiguration;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.apache.xmlbeans.test.performance.utils.Constants;
import org.apache.xmlbeans.test.performance.utils.PerfUtil;

public class POValidateXerces
{
  public static void main(String[] args) throws Exception
  {

    final int iterations = Constants.ITERATIONS;
    String filename;

    if(args.length == 0){
      filename = Constants.PO_INSTANCE_1;
    }
    else if(args[0].length() > 1){
      filename = Constants.XSD_DIR+Constants.P+args[0];
    }
    else{
      switch( Integer.parseInt(args[0]) )
      {
      case 1: filename = Constants.PO_INSTANCE_1; break;
      case 2: filename = Constants.PO_INSTANCE_2; break;  
      case 3: filename = Constants.PO_INSTANCE_3; break;
      case 4: filename = Constants.PO_INSTANCE_4; break;
      case 5: filename = Constants.PO_INSTANCE_5; break;
      case 6: filename = Constants.PO_INSTANCE_6; break;
      case 7: filename = Constants.PO_INSTANCE_7; break;
      default: filename = Constants.PO_INSTANCE_1; break;
      }
    }

    POValidateXerces test = new POValidateXerces();
    long cputime;
    int hash = 0;

    /////////////////////////////////
    // Xerces validating parser setup
    /////////////////////////////////
    /** Property identifier: symbol table. */
    final String SYMBOL_TABLE =
            org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX + org.apache.xerces.impl.Constants.SYMBOL_TABLE_PROPERTY;

    /** Property identifier: grammar pool. */
    final String GRAMMAR_POOL =
            org.apache.xerces.impl.Constants.XERCES_PROPERTY_PREFIX + org.apache.xerces.impl.Constants.XMLGRAMMAR_POOL_PROPERTY;

    // feature ids

    /** Namespaces feature id */
    final String NAMESPACES_FEATURE_ID =
            "http://xml.org/sax/features/namespaces";

    /** Validation feature id */
    final String VALIDATION_FEATURE_ID =
            "http://xml.org/sax/features/validation";

    /** Schema validation feature id */
    final String SCHEMA_VALIDATION_FEATURE_ID =
            "http://apache.org/xml/features/validation/schema";

    /** Schema full checking feature id */
    final String SCHEMA_FULL_CHECKING_FEATURE_ID =
            "http://apache.org/xml/features/validation/schema-full-checking";

    SymbolTable sym = new SymbolTable();
    XMLGrammarPreparser preparser = new XMLGrammarPreparser(sym);
    XMLGrammarPoolImpl grammarPool = new XMLGrammarPoolImpl();
 

    preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
    preparser.setProperty(GRAMMAR_POOL, grammarPool);
    preparser.setFeature(NAMESPACES_FEATURE_ID, true);
    preparser.setFeature(VALIDATION_FEATURE_ID, true);
    preparser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
    preparser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, true);
    
    // parse the schema
    XMLInputSource schemaIS = new XMLInputSource(null,Constants.PO_XSD,null);
    Grammar grammar = preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA,
                                                schemaIS);

    // create the parser configuration
    XMLParserConfiguration parserConfiguration = new IntegratedParserConfiguration(sym,grammarPool);
    // now reset the config according to xerces docs -- required b/c we preparsed???
    preparser.setProperty(GRAMMAR_POOL, grammarPool);
    preparser.setFeature(NAMESPACES_FEATURE_ID, true);
    preparser.setFeature(VALIDATION_FEATURE_ID, true);
    preparser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
    preparser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, true);
    
    /////////////////////////////////////
    // end Xerces validating parser setup
    /////////////////////////////////////

    // get the xmlinstance
    XMLInputSource instanceIS = new XMLInputSource(null,filename,null);
        
    // warm up the vm
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){     
      hash += test.run(parserConfiguration, instanceIS);
    }
    cputime = System.currentTimeMillis() - cputime;

    // run it again for the real measurement
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){     
      hash += test.run(parserConfiguration, instanceIS);
    }
    cputime = System.currentTimeMillis() - cputime;
    
    // print the results
    // use perf util to get file size (XMLInputSource doens't give an easy API).
    PerfUtil util = new PerfUtil();
    char[] chars = util.fileToChars(filename);
    // Class.getSimpleName() is only provided in jdk1.5, so have to trim package name off test name for logging to support 1.4
    System.out.print(Constants.DELIM+test.getClass().getName().substring(test.getClass().getName().lastIndexOf('.')+1)+" filesize="+chars.length+" ");
    System.out.print("hash "+hash+" ");
    System.out.print("time "+cputime+"\n");
  }

  private int run(XMLParserConfiguration p_parser, XMLInputSource p_input) throws Exception 
  {
    // parse with validating parser
    p_parser.parse(p_input);
    // return an int for the hash
    return 17;
  }
}
