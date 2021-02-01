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

package org.apache.poi.hwmf.usermodel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.hwmf.record.HwmfEscape;
import org.apache.poi.hwmf.record.HwmfEscape.EscapeFunction;
import org.apache.poi.hwmf.record.HwmfEscape.WmfEscapeEMF;
import org.apache.poi.hwmf.record.HwmfFill.HwmfImageRecord;
import org.apache.poi.hwmf.record.HwmfRecord;

public class HwmfEmbeddedIterator implements Iterator<HwmfEmbedded> {

    private final Deque<Iterator<?>> iterStack = new ArrayDeque<>();
    private Object current;

    public HwmfEmbeddedIterator(HwmfPicture wmf) {
        this(wmf.getRecords().iterator());
    }

    public HwmfEmbeddedIterator(Iterator<HwmfRecord> recordIterator) {
        iterStack.add(recordIterator);
    }

    @Override
    public boolean hasNext() {
        if (iterStack.isEmpty()) {
            return false;
        }

        if (current != null) {
            // don't search twice and potentially skip items
            return true;
        }

        Iterator<?> iter;
        do {
            iter = iterStack.peek();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (obj instanceof HwmfImageRecord) {
                    current = obj;
                    return true;
                }
                if (obj instanceof HwmfEscape && ((HwmfEscape)obj).getEscapeFunction() == EscapeFunction.META_ESCAPE_ENHANCED_METAFILE) {
                    WmfEscapeEMF emfData = ((HwmfEscape)obj).getEscapeData();
                    if (emfData.isValid()) {
                        current = obj;
                        return true;
                    }
                }
            }
            iterStack.pop();
        } while (!iterStack.isEmpty());

        return false;
    }

    @Override
    public HwmfEmbedded next() {
        HwmfEmbedded emb;
        if ((emb = checkHwmfImageRecord()) != null) {
            return emb;
        }
        if ((emb = checkHwmfEscapeRecord()) != null) {
            return emb;
        }

        throw new NoSuchElementException("no further embedded emf records found.");
    }

    private HwmfEmbedded checkHwmfImageRecord() {
        if (!(current instanceof HwmfImageRecord)) {
            return null;
        }

        HwmfImageRecord hir = (HwmfImageRecord)current;
        current = null;

        HwmfEmbedded emb = new HwmfEmbedded();
        emb.setEmbeddedType(HwmfEmbeddedType.BMP);
        emb.setData(hir.getBMPData());

        return emb;
    }


    private HwmfEmbedded checkHwmfEscapeRecord() {
        if (!(current instanceof HwmfEscape)) {
            return null;
        }
        final HwmfEscape esc = (HwmfEscape)current;
        assert(esc.getEscapeFunction() == EscapeFunction.META_ESCAPE_ENHANCED_METAFILE);

        WmfEscapeEMF img = esc.getEscapeData();
        assert(img.isValid());
        current = null;

        final HwmfEmbedded emb = new HwmfEmbedded();
        emb.setEmbeddedType(HwmfEmbeddedType.EMF);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (;;) {
                bos.write(img.getEmfData());

                current = null;
                if (img.getRemainingBytes() > 0 && hasNext() && (current instanceof HwmfEscape)) {
                    img = ((HwmfEscape)current).getEscapeData();
                } else {
                    return emb;
                }
            }
        } catch (IOException e) {
            // ByteArrayOutputStream doesn't throw IOException
            return null;
        } finally {
            emb.setData(bos.toByteArray());
        }
    }
}
