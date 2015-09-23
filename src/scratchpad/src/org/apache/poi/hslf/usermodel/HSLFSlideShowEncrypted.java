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

package org.apache.poi.hslf.usermodel;

import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;
import org.apache.poi.hslf.exceptions.EncryptedPowerPointFileException;
import org.apache.poi.hslf.record.DocumentEncryptionAtom;
import org.apache.poi.hslf.record.PersistPtrHolder;
import org.apache.poi.hslf.record.PositionDependentRecord;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.UserEditAtom;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIDecryptor;
import org.apache.poi.poifs.crypt.cryptoapi.CryptoAPIEncryptor;
import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * This class provides helper functions for encrypted PowerPoint documents.
 */
@Internal
public class HSLFSlideShowEncrypted {
    DocumentEncryptionAtom dea;
    CryptoAPIEncryptor enc = null;
    CryptoAPIDecryptor dec = null;
    Cipher cipher = null;
    CipherOutputStream cyos = null;

    private static final BitField fieldRecInst = new BitField(0xFFF0);
    
    protected HSLFSlideShowEncrypted(DocumentEncryptionAtom dea) {
        this.dea = dea;
    }

    protected HSLFSlideShowEncrypted(byte[] docstream, NavigableMap<Integer,Record> recordMap) {
        // check for DocumentEncryptionAtom, which would be at the last offset
        // need to ignore already set UserEdit and PersistAtoms
        UserEditAtom userEditAtomWithEncryption = null;
        for (Map.Entry<Integer, Record> me : recordMap.descendingMap().entrySet()) {
            Record r = me.getValue();
            if (!(r instanceof UserEditAtom)) continue;
            UserEditAtom uea = (UserEditAtom)r;
            if (uea.getEncryptSessionPersistIdRef() != -1) {
                userEditAtomWithEncryption = uea;
                break;
            }
        }

        if (userEditAtomWithEncryption == null) {
            dea = null;
            return;
        }

        Record r = recordMap.get(userEditAtomWithEncryption.getPersistPointersOffset());
        assert(r instanceof PersistPtrHolder);
        PersistPtrHolder ptr = (PersistPtrHolder)r;
        
        Integer encOffset = ptr.getSlideLocationsLookup().get(userEditAtomWithEncryption.getEncryptSessionPersistIdRef());
        assert(encOffset != null);
        
        r = recordMap.get(encOffset);
        if (r == null) {
            r = Record.buildRecordAtOffset(docstream, encOffset);
            recordMap.put(encOffset, r);
        }
        assert(r instanceof DocumentEncryptionAtom);
        this.dea = (DocumentEncryptionAtom)r;
        decryptInit();
        
        String pass = Biff8EncryptionKey.getCurrentUserPassword();
        if(!dec.verifyPassword(pass != null ? pass : Decryptor.DEFAULT_PASSWORD)) {
            throw new EncryptedPowerPointFileException("PowerPoint file is encrypted. The correct password needs to be set via Biff8EncryptionKey.setCurrentUserPassword()");
        }
     }

    public DocumentEncryptionAtom getDocumentEncryptionAtom() {
        return dea;
    }
    
    protected void setPersistId(int persistId) {
        if (enc != null && dec != null) {
            throw new EncryptedPowerPointFileException("Use instance either for en- or decryption");
        }
        
        try {
            if (enc != null) cipher = enc.initCipherForBlock(cipher, persistId);
            if (dec != null) cipher = dec.initCipherForBlock(cipher, persistId);
        } catch (GeneralSecurityException e) {
            throw new EncryptedPowerPointFileException(e);
        }
    }
    
    protected void decryptInit() {
        if (dec != null) return;
        EncryptionInfo ei = dea.getEncryptionInfo();
        dec = (CryptoAPIDecryptor)ei.getDecryptor();
    }
    
    protected void encryptInit() {
        if (enc != null) return;
        EncryptionInfo ei = dea.getEncryptionInfo();
        enc = (CryptoAPIEncryptor)ei.getEncryptor();
    }
    

    
    protected OutputStream encryptRecord(OutputStream plainStream, int persistId, Record record) {
        boolean isPlain = (dea == null 
            || record instanceof UserEditAtom
            || record instanceof PersistPtrHolder
            || record instanceof DocumentEncryptionAtom
        );
        if (isPlain) return plainStream;

        encryptInit();
        setPersistId(persistId);
        
        if (cyos == null) {
            cyos = new CipherOutputStream(plainStream, cipher);
        }
        return cyos;
    }

    protected void decryptRecord(byte[] docstream, int persistId, int offset) {
        if (dea == null) return;

        decryptInit();
        setPersistId(persistId);
        
        try {
            // decrypt header and read length to be decrypted
            cipher.update(docstream, offset, 8, docstream, offset);
            // decrypt the rest of the record
            int rlen = (int)LittleEndian.getUInt(docstream, offset+4);
            cipher.update(docstream, offset+8, rlen, docstream, offset+8);
        } catch (GeneralSecurityException e) {
            throw new CorruptPowerPointFileException(e);
        }       
    }        
    
    protected void decryptPicture(byte[] pictstream, int offset) {
        if (dea == null) return;
        
        decryptInit();
        setPersistId(0);
        
        try {
            // decrypt header and read length to be decrypted
            cipher.doFinal(pictstream, offset, 8, pictstream, offset);
            int recInst = fieldRecInst.getValue(LittleEndian.getUShort(pictstream, offset));
            int recType = LittleEndian.getUShort(pictstream, offset+2);
            int rlen = (int)LittleEndian.getUInt(pictstream, offset+4);
            offset += 8;
            int endOffset = offset + rlen; 

            if (recType == 0xF007) {
                // TOOD: get a real example file ... to actual test the FBSE entry
                // not sure where the foDelay block is
                
                // File BLIP Store Entry (FBSE)
                cipher.doFinal(pictstream, offset, 1, pictstream, offset); // btWin32
                offset++;
                cipher.doFinal(pictstream, offset, 1, pictstream, offset); // btMacOS
                offset++;
                cipher.doFinal(pictstream, offset, 16, pictstream, offset); // rgbUid
                offset += 16;
                cipher.doFinal(pictstream, offset, 2, pictstream, offset); // tag
                offset += 2;
                cipher.doFinal(pictstream, offset, 4, pictstream, offset); // size
                offset += 4;
                cipher.doFinal(pictstream, offset, 4, pictstream, offset); // cRef
                offset += 4;
                cipher.doFinal(pictstream, offset, 4, pictstream, offset); // foDelay
                offset += 4;
                cipher.doFinal(pictstream, offset+0, 1, pictstream, offset+0); // unused1
                cipher.doFinal(pictstream, offset+1, 1, pictstream, offset+1); // cbName
                cipher.doFinal(pictstream, offset+2, 1, pictstream, offset+2); // unused2
                cipher.doFinal(pictstream, offset+3, 1, pictstream, offset+3); // unused3
                int cbName = LittleEndian.getUShort(pictstream, offset+1);
                offset += 4;
                if (cbName > 0) {
                    cipher.doFinal(pictstream, offset, cbName, pictstream, offset); // nameData
                    offset += cbName;
                }
                if (offset == endOffset) {
                    return; // no embedded blip
                }
                // fall through, read embedded blip now

                // update header data
                cipher.doFinal(pictstream, offset, 8, pictstream, offset);
                recInst = fieldRecInst.getValue(LittleEndian.getUShort(pictstream, offset));
                recType = LittleEndian.getUShort(pictstream, offset+2);
                // rlen = (int)LittleEndian.getUInt(pictstream, offset+4);
                offset += 8;
            }

            int rgbUidCnt = (recInst == 0x217 || recInst == 0x3D5 || recInst == 0x46B || recInst == 0x543 ||
                recInst == 0x6E1 || recInst == 0x6E3 || recInst == 0x6E5 || recInst == 0x7A9) ? 2 : 1;
            
            for (int i=0; i<rgbUidCnt; i++) {
                cipher.doFinal(pictstream, offset, 16, pictstream, offset); // rgbUid 1/2
                offset += 16;
            }
            
            if (recType == 0xF01A || recType == 0XF01B || recType == 0XF01C) {
                cipher.doFinal(pictstream, offset, 34, pictstream, offset); // metafileHeader
                offset += 34;
            } else {
                cipher.doFinal(pictstream, offset, 1, pictstream, offset); // tag
                offset += 1;
            }
            
            int blipLen = endOffset - offset;
            cipher.doFinal(pictstream, offset, blipLen, pictstream, offset);
        } catch (GeneralSecurityException e) {
            throw new CorruptPowerPointFileException(e);
        }       
    }

    protected void encryptPicture(byte[] pictstream, int offset) {
        if (dea == null) return;
        
        encryptInit();
        setPersistId(0);

        try {
            int recInst = fieldRecInst.getValue(LittleEndian.getUShort(pictstream, offset));
            int recType = LittleEndian.getUShort(pictstream, offset+2);
            int rlen = (int)LittleEndian.getUInt(pictstream, offset+4);
            cipher.doFinal(pictstream, offset, 8, pictstream, offset);
            offset += 8;
            int endOffset = offset + rlen; 

            if (recType == 0xF007) {
                // TOOD: get a real example file ... to actual test the FBSE entry
                // not sure where the foDelay block is
                
                // File BLIP Store Entry (FBSE)
                cipher.doFinal(pictstream, offset, 1, pictstream, offset); // btWin32
                offset++;
                cipher.doFinal(pictstream, offset, 1, pictstream, offset); // btMacOS
                offset++;
                cipher.doFinal(pictstream, offset, 16, pictstream, offset); // rgbUid
                offset += 16;
                cipher.doFinal(pictstream, offset, 2, pictstream, offset); // tag
                offset += 2;
                cipher.doFinal(pictstream, offset, 4, pictstream, offset); // size
                offset += 4;
                cipher.doFinal(pictstream, offset, 4, pictstream, offset); // cRef
                offset += 4;
                cipher.doFinal(pictstream, offset, 4, pictstream, offset); // foDelay
                offset += 4;
                int cbName = LittleEndian.getUShort(pictstream, offset+1);
                cipher.doFinal(pictstream, offset+0, 1, pictstream, offset+0); // unused1
                cipher.doFinal(pictstream, offset+1, 1, pictstream, offset+1); // cbName
                cipher.doFinal(pictstream, offset+2, 1, pictstream, offset+2); // unused2
                cipher.doFinal(pictstream, offset+3, 1, pictstream, offset+3); // unused3
                offset += 4;
                if (cbName > 0) {
                    cipher.doFinal(pictstream, offset, cbName, pictstream, offset); // nameData
                    offset += cbName;
                }
                if (offset == endOffset) {
                    return; // no embedded blip
                }
                // fall through, read embedded blip now

                // update header data
                recInst = fieldRecInst.getValue(LittleEndian.getUShort(pictstream, offset));
                recType = LittleEndian.getUShort(pictstream, offset+2);
                // rlen = (int) LittleEndian.getUInt(pictstream, offset+4);
                cipher.doFinal(pictstream, offset, 8, pictstream, offset);
                offset += 8;
            }
            
            int rgbUidCnt = (recInst == 0x217 || recInst == 0x3D5 || recInst == 0x46B || recInst == 0x543 ||
                recInst == 0x6E1 || recInst == 0x6E3 || recInst == 0x6E5 || recInst == 0x7A9) ? 2 : 1;
                
            for (int i=0; i<rgbUidCnt; i++) {
                cipher.doFinal(pictstream, offset, 16, pictstream, offset); // rgbUid 1/2
                offset += 16;
            }
            
            if (recType == 0xF01A || recType == 0XF01B || recType == 0XF01C) {
                cipher.doFinal(pictstream, offset, 34, pictstream, offset); // metafileHeader
                offset += 34;
            } else {
                cipher.doFinal(pictstream, offset, 1, pictstream, offset); // tag
                offset += 1;
            }
            
            int blipLen = endOffset - offset;
            cipher.doFinal(pictstream, offset, blipLen, pictstream, offset);
        } catch (GeneralSecurityException e) {
            throw new CorruptPowerPointFileException(e);
        }       
    }

    protected Record[] updateEncryptionRecord(Record records[]) {
        String password = Biff8EncryptionKey.getCurrentUserPassword();
        if (password == null) {
            if (dea == null) {
                // no password given, no encryption record exits -> done
                return records;
            } else {
                // need to remove password data
                dea = null;
                return removeEncryptionRecord(records);
            }
        } else {
            // create password record
            if (dea == null) {
                dea = new DocumentEncryptionAtom();
                enc = null;
            }
            encryptInit();
            EncryptionInfo ei = dea.getEncryptionInfo();
            byte salt[] = ei.getVerifier().getSalt();
            if (salt == null) {
                enc.confirmPassword(password);
            } else {
                byte verifier[] = ei.getDecryptor().getVerifier();
                enc.confirmPassword(password, null, null, verifier, salt, null);
            }

            // move EncryptionRecord to last slide position
            records = normalizeRecords(records);
            return addEncryptionRecord(records, dea);
        }
    }

    /**
     * remove duplicated UserEditAtoms and merge PersistPtrHolder.
     * Before this method is called, make sure that the offsets are correct,
     * i.e. call {@link HSLFSlideShowImpl#updateAndWriteDependantRecords(OutputStream, Map)}
     */
    protected static Record[] normalizeRecords(Record records[]) {
        // http://msdn.microsoft.com/en-us/library/office/gg615594(v=office.14).aspx
        // repeated slideIds can be overwritten, i.e. ignored
        
        UserEditAtom uea = null;
        PersistPtrHolder pph = null;
        TreeMap<Integer,Integer> slideLocations = new TreeMap<Integer,Integer>();
        TreeMap<Integer,Record> recordMap = new TreeMap<Integer,Record>();
        List<Integer> obsoleteOffsets = new ArrayList<Integer>();
        int duplicatedCount = 0;
        for (Record r : records) {
            assert(r instanceof PositionDependentRecord);
            PositionDependentRecord pdr = (PositionDependentRecord)r;
            if (pdr instanceof UserEditAtom) {
                uea = (UserEditAtom)pdr;
                continue;
            }
            
            if (pdr instanceof PersistPtrHolder) {
                if (pph != null) {
                    duplicatedCount++;
                }
                pph = (PersistPtrHolder)pdr;
                for (Map.Entry<Integer,Integer> me : pph.getSlideLocationsLookup().entrySet()) {
                    Integer oldOffset = slideLocations.put(me.getKey(), me.getValue());
                    if (oldOffset != null) obsoleteOffsets.add(oldOffset);
                }
                continue;
            }
            
            recordMap.put(pdr.getLastOnDiskOffset(), r);
        }
        
        assert(uea != null && pph != null && uea.getPersistPointersOffset() == pph.getLastOnDiskOffset());
        
        recordMap.put(pph.getLastOnDiskOffset(), pph);
        recordMap.put(uea.getLastOnDiskOffset(), uea);

        if (duplicatedCount == 0 && obsoleteOffsets.isEmpty()) {
            return records;
        }

        uea.setLastUserEditAtomOffset(0);
        pph.clear();
        for (Map.Entry<Integer,Integer> me : slideLocations.entrySet()) {
            pph.addSlideLookup(me.getKey(), me.getValue());
        }
        
        for (Integer oldOffset : obsoleteOffsets) {
            recordMap.remove(oldOffset);
        }
        
        return recordMap.values().toArray(new Record[recordMap.size()]);
    }
     
    
    protected static Record[] removeEncryptionRecord(Record records[]) {
        int deaSlideId = -1;
        int deaOffset = -1;
        PersistPtrHolder ptr = null;
        UserEditAtom uea = null;
        List<Record> recordList = new ArrayList<Record>();
        for (Record r : records) {
            if (r instanceof DocumentEncryptionAtom) {
                deaOffset = ((DocumentEncryptionAtom)r).getLastOnDiskOffset();
                continue;
            } else if (r instanceof UserEditAtom) {
                uea = (UserEditAtom)r;
                deaSlideId = uea.getEncryptSessionPersistIdRef();
                uea.setEncryptSessionPersistIdRef(-1);
            } else if (r instanceof PersistPtrHolder) {
                ptr = (PersistPtrHolder)r;
            }
            recordList.add(r);
        }
        
        assert(ptr != null);
        if (deaSlideId == -1 && deaOffset == -1) return records;
        
        TreeMap<Integer,Integer> tm = new TreeMap<Integer,Integer>(ptr.getSlideLocationsLookup());
        ptr.clear();
        int maxSlideId = -1;
        for (Map.Entry<Integer,Integer> me : tm.entrySet()) {
            if (me.getKey() == deaSlideId || me.getValue() == deaOffset) continue;
            ptr.addSlideLookup(me.getKey(), me.getValue());
            maxSlideId = Math.max(me.getKey(), maxSlideId);
        }
        
        uea.setMaxPersistWritten(maxSlideId);

        records = recordList.toArray(new Record[recordList.size()]);
        
        return records;
    }


    protected static Record[] addEncryptionRecord(Record records[], DocumentEncryptionAtom dea) {
        assert(dea != null);
        int ueaIdx = -1, ptrIdx = -1, deaIdx = -1, idx = -1;
        for (Record r : records) {
            idx++;
            if (r instanceof UserEditAtom) ueaIdx = idx;
            else if (r instanceof PersistPtrHolder) ptrIdx = idx;
            else if (r instanceof DocumentEncryptionAtom) deaIdx = idx;
        }
        assert(ueaIdx != -1 && ptrIdx != -1 && ptrIdx < ueaIdx);
        if (deaIdx != -1) {
            DocumentEncryptionAtom deaOld = (DocumentEncryptionAtom)records[deaIdx];
            dea.setLastOnDiskOffset(deaOld.getLastOnDiskOffset());
            records[deaIdx] = dea;
            return records;
        } else {
            PersistPtrHolder ptr = (PersistPtrHolder)records[ptrIdx];
            UserEditAtom uea = ((UserEditAtom)records[ueaIdx]);
            dea.setLastOnDiskOffset(ptr.getLastOnDiskOffset()-1);
            int nextSlideId = uea.getMaxPersistWritten()+1;
            ptr.addSlideLookup(nextSlideId, ptr.getLastOnDiskOffset()-1);
            uea.setEncryptSessionPersistIdRef(nextSlideId);
            uea.setMaxPersistWritten(nextSlideId);
            
            Record newRecords[] = new Record[records.length+1];
            if (ptrIdx > 0) System.arraycopy(records, 0, newRecords, 0, ptrIdx);
            if (ptrIdx < records.length-1) System.arraycopy(records, ptrIdx, newRecords, ptrIdx+1, records.length-ptrIdx);
            newRecords[ptrIdx] = dea;
            return newRecords;
        }
    }

}
