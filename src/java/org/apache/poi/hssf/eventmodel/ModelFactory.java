/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.poi.hssf.eventmodel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.model.Model;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.EOFRecord;
import org.apache.poi.hssf.record.Record;


/**
 * ModelFactory creates workbook and sheet models based upon 
 * events thrown by them there events from the EventRecordFactory.
 * 
 * @see org.apache.poi.hssf.eventmodel.EventRecordFactory
 * @author Andrew C. Oliver acoliver@apache.org
 */
public class ModelFactory implements ERFListener
{

    List listeners;
    Model currentmodel;
    boolean lastEOF;
 
    /**
     * Constructor for ModelFactory.  Does practically nothing.
     */
    public ModelFactory()
    {
        super();
        listeners = new ArrayList(1);
    }
    
    /**
     * register a ModelFactoryListener so that it can receive 
     * Models as they are created.
     */
    public void registerListener(ModelFactoryListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Start processing the Workbook stream into Model events.
     */
    public void run(InputStream stream) {
        EventRecordFactory factory = new EventRecordFactory(true);
        factory.registerListener(this,null);
        lastEOF = true;
        factory.processRecords(stream);
    }

    //ERFListener
    public boolean processRecord(Record rec)
    {
       if (rec.getSid() == BOFRecord.sid) {
             if (lastEOF != true) {
              throw new RuntimeException("Not yet handled embedded models");  
             } else {
              BOFRecord bof = (BOFRecord)rec;
              switch (bof.getType()) {
               case BOFRecord.TYPE_WORKBOOK:
                 currentmodel = new Workbook();                 
               break;
               case BOFRecord.TYPE_WORKSHEET:
                 currentmodel = new Sheet();                                  
               break;
              default:
                   throw new RuntimeException("Unsupported model type "+bof.getType());
              }                
               
             }        
        }
        
        if (rec.getSid() == EOFRecord.sid) {
            lastEOF = true;
            throwEvent(currentmodel);
        } else {
            lastEOF = false;   
        }
        
 
        return true;
    }

    /**
     * Throws the model as an event to the listeners
     * @param model to be thrown
     */
    private void throwEvent(Model model)
    {
        Iterator i = listeners.iterator();
        while (i.hasNext()) {
          ModelFactoryListener mfl = (ModelFactoryListener) i.next();
          mfl.process(model);
        }
    }


}
