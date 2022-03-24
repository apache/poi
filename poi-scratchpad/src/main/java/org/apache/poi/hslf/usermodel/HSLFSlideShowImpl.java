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

import static org.apache.logging.log4j.util.Unbox.box;
import static org.apache.poi.hslf.usermodel.HSLFSlideShow.POWERPOINT_DOCUMENT;
import static org.apache.poi.hslf.usermodel.HSLFSlideShow.PP95_DOCUMENT;
import static org.apache.poi.hslf.usermodel.HSLFSlideShow.PP97_DOCUMENT;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.POIDocument;
import org.apache.poi.ddf.EscherBSERecord;
import org.apache.poi.ddf.EscherContainerRecord;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hslf.exceptions.CorruptPowerPointFileException;
import org.apache.poi.hslf.exceptions.HSLFException;
import org.apache.poi.hslf.exceptions.OldPowerPointFormatException;
import org.apache.poi.hslf.record.CurrentUserAtom;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.DocumentEncryptionAtom;
import org.apache.poi.hslf.record.ExOleObjStg;
import org.apache.poi.hslf.record.PersistPtrHolder;
import org.apache.poi.hslf.record.PersistRecord;
import org.apache.poi.hslf.record.PositionDependentRecord;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.RecordTypes;
import org.apache.poi.hslf.record.UserEditAtom;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.EntryUtils;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.PictureData.PictureType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * This class contains the main functionality for the Powerpoint file
 * "reader". It is only a very basic class for now
 */
public final class HSLFSlideShowImpl extends POIDocument implements Closeable {
    private static final Logger LOG = LogManager.getLogger(HSLFSlideShowImpl.class);

    static final int UNSET_OFFSET = -1;

    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 200_000_000;
    private static final int MAX_DOCUMENT_SIZE = 100_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    // Holds metadata on where things are in our document
    private CurrentUserAtom currentUser;

    // Low level contents of the file
    private byte[] _docstream;

    // Low level contents
    private Record[] _records;

    // Raw Pictures contained in the pictures stream
    private List<HSLFPictureData> _pictures;

    // Embedded objects stored in storage records in the document stream, lazily populated.
    private HSLFObjectData[] _objects;

    /**
     * @param length the max record length allowed for HSLFSlideShowImpl
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for HSLFSlideShowImpl
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    /**
     * Constructs a Powerpoint document from fileName. Parses the document
     * and places all the important stuff into data structures.
     *
     * @param fileName The name of the file to read.
     * @throws IOException if there is a problem while parsing the document.
     */
    @SuppressWarnings("resource")
    public HSLFSlideShowImpl(String fileName) throws IOException {
        this(new POIFSFileSystem(new File(fileName)));
    }

    /**
     * Constructs a Powerpoint document from an input stream. Parses the
     * document and places all the important stuff into data structures.
     *
     * @param inputStream the source of the data
     * @throws IOException if there is a problem while parsing the document.
     */
    @SuppressWarnings("resource")
    public HSLFSlideShowImpl(InputStream inputStream) throws IOException {
        //do Ole stuff
        this(new POIFSFileSystem(inputStream));
    }

    /**
     * Constructs a Powerpoint document from a POIFS Filesystem. Parses the
     * document and places all the important stuff into data structures.
     *
     * @param filesystem the POIFS FileSystem to read from
     * @throws IOException if there is a problem while parsing the document.
     */
    public HSLFSlideShowImpl(POIFSFileSystem filesystem) throws IOException {
        this(filesystem.getRoot());
    }

    /**
     * Constructs a Powerpoint document from a specific point in a
     * POIFS Filesystem. Parses the document and places all the
     * important stuff into data structures.
     *
     * @param dir the POIFS directory to read from
     * @throws IOException if there is a problem while parsing the document.
     */
    public HSLFSlideShowImpl(DirectoryNode dir) throws IOException {
        super(handleDualStorage(dir));

        try {
            // First up, grab the "Current User" stream
            // We need this before we can detect Encrypted Documents
            readCurrentUserStream();

            // Next up, grab the data that makes up the
            //  PowerPoint stream
            readPowerPointStream();

            // Now, build records based on the PowerPoint stream
            buildRecords();

            // Look for any other streams
            readOtherStreams();
        } catch (RuntimeException | IOException e) {
            // clean up the filesystem when we cannot read it here to avoid
            // leaking file handles
            dir.getFileSystem().close();

            throw e;
        }
    }

    private static DirectoryNode handleDualStorage(DirectoryNode dir) throws IOException {
        // when there's a dual storage entry, use it, as the outer document can't be read quite probably ...
        if (!dir.hasEntry(PP97_DOCUMENT)) {
            return dir;
        }
        return (DirectoryNode) dir.getEntry(PP97_DOCUMENT);
    }

    /**
     * Constructs a new, empty, Powerpoint document.
     */
    public static HSLFSlideShowImpl create() {
        try (InputStream is = HSLFSlideShowImpl.class.getResourceAsStream("/org/apache/poi/hslf/data/empty.ppt")) {
            if (is == null) {
                throw new HSLFException("Missing resource 'empty.ppt'");
            }
            return new HSLFSlideShowImpl(is);
        } catch (IOException e) {
            throw new HSLFException(e);
        }
    }

    /**
     * Extracts the main PowerPoint document stream from the
     * POI file, ready to be passed
     *
     * @throws IOException when the powerpoint can't be read
     */
    private void readPowerPointStream() throws IOException {
        final DirectoryNode dir = getDirectory();

        if (!dir.hasEntry(POWERPOINT_DOCUMENT) && dir.hasEntry(PP95_DOCUMENT)) {
            throw new OldPowerPointFormatException("You seem to have supplied a PowerPoint95 file, which isn't supported");
        }

        // Get the main document stream
        DocumentEntry docProps = (DocumentEntry)dir.getEntry(POWERPOINT_DOCUMENT);

        // Grab the document stream
        int len = docProps.getSize();
        try (InputStream is = dir.createDocumentInputStream(docProps)) {
            _docstream = IOUtils.toByteArray(is, len, MAX_DOCUMENT_SIZE);
        }
    }

    /**
     * Builds the list of records, based on the contents
     * of the PowerPoint stream
     */
    private void buildRecords() throws IOException {
        // The format of records in a powerpoint file are:
        //   <little endian 2 byte "info">
        //   <little endian 2 byte "type">
        //   <little endian 4 byte "length">
        // If it has a zero length, following it will be another record
        //      <xx xx yy yy 00 00 00 00> <xx xx yy yy zz zz zz zz>
        // If it has a length, depending on its type it may have children or data
        // If it has children, these will follow straight away
        //      <xx xx yy yy zz zz zz zz <xx xx yy yy zz zz zz zz>>
        // If it has data, this will come straigh after, and run for the length
        //      <xx xx yy yy zz zz zz zz dd dd dd dd dd dd dd>
        // All lengths given exclude the 8 byte record header
        // (Data records are known as Atoms)

        // Document should start with:
        //   0F 00 E8 03 ## ## ## ##
        //     (type 1000 = document, info 00 0f is normal, rest is document length)
        //   01 00 E9 03 28 00 00 00
        //     (type 1001 = document atom, info 00 01 normal, 28 bytes long)
        //   80 16 00 00 E0 10 00 00 xx xx xx xx xx xx xx xx
        //   05 00 00 00 0A 00 00 00 xx xx xx
        //     (the contents of the document atom, not sure what it means yet)
        //   (records then follow)

        // When parsing a document, look to see if you know about that type
        //  of the current record. If you know it's a type that has children,
        //  process the record's data area looking for more records
        // If you know about the type and it doesn't have children, either do
        //  something with the data (eg TextRun) or skip over it
        // If you don't know about the type, play safe and skip over it (using
        //  its length to know where the next record will start)
        //

        _records = read(_docstream, (int) currentUser.getCurrentEditOffset());
    }

    private Record[] read(byte[] docstream, int usrOffset) throws IOException {
        //sort found records by offset.
        //(it is not necessary but SlideShow.findMostRecentCoreRecords() expects them sorted)
        NavigableMap<Integer, Record> records = new TreeMap<>(); // offset -> record
        Map<Integer, Integer> persistIds = new HashMap<>(); // offset -> persistId
        initRecordOffsets(docstream, usrOffset, records, persistIds);
        HSLFSlideShowEncrypted decryptData = new HSLFSlideShowEncrypted(docstream, records);

        for (Map.Entry<Integer, Record> entry : records.entrySet()) {
            Integer offset = entry.getKey();
            Record record = entry.getValue();
            Integer persistId = persistIds.get(offset);
            if (record == null) {
                // all plain records have been already added,
                // only new records need to be decrypted (tbd #35897)
                decryptData.decryptRecord(docstream, persistId, offset);
                record = Record.buildRecordAtOffset(docstream, offset);
                entry.setValue(record);
            }

            if (record instanceof PersistRecord) {
                ((PersistRecord) record).setPersistId(persistId);
            }
        }

        decryptData.close();
        return records.values().toArray(new Record[0]);
    }

    private void initRecordOffsets(byte[] docstream, int usrOffset, NavigableMap<Integer, Record> recordMap, Map<Integer, Integer> offset2id) {
        while (usrOffset != 0) {
            UserEditAtom usr = (UserEditAtom) Record.buildRecordAtOffset(docstream, usrOffset);
            if (usr == null) {
                throw new CorruptPowerPointFileException("Powerpoint document contains no user edit atom");
            }

            recordMap.put(usrOffset, usr);

            int psrOffset = usr.getPersistPointersOffset();
            PersistPtrHolder ptr = (PersistPtrHolder) Record.buildRecordAtOffset(docstream, psrOffset);
            if (ptr == null) {
                throw new CorruptPowerPointFileException("Powerpoint document is missing a PersistPtrHolder at " + psrOffset);
            }
            recordMap.put(psrOffset, ptr);

            for (Map.Entry<Integer, Integer> entry : ptr.getSlideLocationsLookup().entrySet()) {
                Integer offset = entry.getValue();
                Integer id = entry.getKey();
                recordMap.put(offset, null); // reserve a slot for the record
                offset2id.put(offset, id);
            }

            usrOffset = usr.getLastUserEditAtomOffset();

            // check for corrupted user edit atom and try to repair it
            // if the next user edit atom offset is already known, we would go into an endless loop
            if (usrOffset > 0 && recordMap.containsKey(usrOffset)) {
                // a user edit atom is usually located 36 byte before the smallest known record offset
                usrOffset = recordMap.firstKey() - 36;
                // check that we really are located on a user edit atom
                int ver_inst = LittleEndian.getUShort(docstream, usrOffset);
                int type = LittleEndian.getUShort(docstream, usrOffset + 2);
                int len = LittleEndian.getInt(docstream, usrOffset + 4);
                if (ver_inst == 0 && type == 4085 && (len == 0x1C || len == 0x20)) {
                    LOG.atWarn().log("Repairing invalid user edit atom");
                    usr.setLastUserEditAtomOffset(usrOffset);
                } else {
                    throw new CorruptPowerPointFileException("Powerpoint document contains invalid user edit atom");
                }
            }
        }
    }

    public DocumentEncryptionAtom getDocumentEncryptionAtom() {
        for (Record r : _records) {
            if (r instanceof DocumentEncryptionAtom) {
                return (DocumentEncryptionAtom) r;
            }
        }
        return null;
    }


    /**
     * Find the "Current User" stream, and load it
     */
    private void readCurrentUserStream() {
        try {
            currentUser = new CurrentUserAtom(getDirectory());
        } catch (IOException ie) {
            LOG.atError().withThrowable(ie).log("Error finding Current User Atom");
            currentUser = new CurrentUserAtom();
        }
    }

    /**
     * Find any other streams from the filesystem, and load them
     */
    private void readOtherStreams() {
        // Currently, there aren't any
    }

    /**
     * Find and read in pictures contained in this presentation.
     * This is lazily called as and when we want to touch pictures.
     */
    private void readPictures() throws IOException {

        // if the presentation doesn't contain pictures, will use an empty collection instead
        if (!getDirectory().hasEntry("Pictures")) {
            _pictures = new ArrayList<>();
            return;
        }

        DocumentEntry entry = (DocumentEntry) getDirectory().getEntry("Pictures");
        EscherContainerRecord blipStore = getBlipStore();
        byte[] pictstream;
        try (DocumentInputStream is = getDirectory().createDocumentInputStream(entry)) {
            pictstream = IOUtils.toByteArray(is, entry.getSize());
        }

        List<PictureFactory> factories = new ArrayList<>();
        try (HSLFSlideShowEncrypted decryptData = new HSLFSlideShowEncrypted(getDocumentEncryptionAtom())) {

            int pos = 0;
            // An empty picture record (length 0) will take up 8 bytes
            while (pos <= (pictstream.length - HSLFPictureData.PREAMBLE_SIZE)) {
                int offset = pos;

                decryptData.decryptPicture(pictstream, offset);

                // Image signature
                int signature = LittleEndian.getUShort(pictstream, pos);
                pos += LittleEndianConsts.SHORT_SIZE;
                // Image type + 0xF018
                int type = LittleEndian.getUShort(pictstream, pos);
                pos += LittleEndianConsts.SHORT_SIZE;
                // Image size (excluding the 8 byte header)
                int imgsize = LittleEndian.getInt(pictstream, pos);
                pos += LittleEndianConsts.INT_SIZE;

                // When parsing the BStoreDelay stream, [MS-ODRAW] says that we
                //  should terminate if the type isn't 0xf007 or 0xf018->0xf117
                if (!((type == 0xf007) || (type >= 0xf018 && type <= 0xf117))) {
                    break;
                }

                // The image size must be 0 or greater
                // (0 is allowed, but odd, since we do wind on by the header each
                //  time, so we won't get stuck)
                if (imgsize < 0) {
                    throw new CorruptPowerPointFileException("The file contains a picture, at position " + factories.size() + ", which has a negatively sized data length, so we can't trust any of the picture data");
                }

                // If the type (including the bonus 0xF018) is 0, skip it
                PictureType pt = PictureType.forNativeID(type - 0xF018);
                if (pt == null) {
                    LOG.atError().log("Problem reading picture: Invalid image type 0, on picture with length {}.\nYour document will probably become corrupted if you save it! Position: {}", box(imgsize),box(pos));
                } else {
                    //The pictstream can be truncated halfway through a picture.
                    //This is not a problem if the pictstream contains extra pictures
                    //that are not used in any slide -- BUG-60305
                    if (pos + imgsize > pictstream.length) {
                        LOG.atWarn().log("\"Pictures\" stream may have ended early. In some circumstances, this is not a problem; " +
                                "in others, this could indicate a corrupt file");
                        break;
                    }

                    // Copy the data, ready to pass to PictureData
                    byte[] imgdata = IOUtils.safelyClone(pictstream, pos, imgsize, MAX_RECORD_LENGTH);

                    factories.add(new PictureFactory(blipStore, pt, imgdata, offset, signature));
                }

                pos += imgsize;
            }
        }

        matchPicturesAndRecords(factories, blipStore);

        List<HSLFPictureData> pictures = new ArrayList<>();
        for (PictureFactory it : factories)      {
            try {
                HSLFPictureData pict = it.build();

                pict.setIndex(pictures.size() + 1);        // index is 1-based
                pictures.add(pict);
            } catch (IllegalArgumentException e) {
                LOG.atError().withThrowable(e).log("Problem reading picture. Your document will probably become corrupted if you save it!");
            }
        }

        _pictures = pictures;
    }

    /**
     * Matches all of the {@link PictureFactory PictureFactories} for a slideshow with {@link EscherBSERecord}s in the
     * Blip Store for the slideshow.
     * <p>
     * When reading a slideshow into memory, we have to match the records in the Blip Store with the factories
     * representing picture in the pictures stream. This can be difficult, as presentations might have incorrectly
     * formatted data. This function attempts to perform matching using multiple heuristics to increase the likelihood
     * of finding all pairs, while aiming to reduce the likelihood of associating incorrect pairs.
     *
     * @param factories Factories for creating {@link HSLFPictureData} out of the pictures stream.
     * @param blipStore Blip Store of the presentation being loaded.
     */
    private static void matchPicturesAndRecords(List<PictureFactory> factories, EscherContainerRecord blipStore) {
        // LinkedList because we're sorting and removing.
        LinkedList<PictureFactory> unmatchedFactories = new LinkedList<>(factories);
        unmatchedFactories.sort(Comparator.comparingInt(PictureFactory::getOffset));

        // Arrange records by offset. In the common case of a well-formed slideshow, where every factory has a
        //  matching record, this is somewhat wasteful, but is necessary to handle the uncommon case where multiple
        //  records share an offset.
        Map<Integer, List<EscherBSERecord>> unmatchedRecords = new HashMap<>();
        for (EscherRecord child : blipStore) {
            EscherBSERecord record = (EscherBSERecord) child;
            unmatchedRecords.computeIfAbsent(record.getOffset(), k -> new ArrayList<>()).add(record);
        }

        // The first pass through the factories only pairs a factory with a record if we're very confident that they
        //  are a match. Confidence comes from a perfect match on the offset, and if necessary, the UID. Matched
        //  factories and records are removed from the unmatched collections.
        for (Iterator<PictureFactory> iterator = unmatchedFactories.iterator(); iterator.hasNext(); ) {
            PictureFactory factory = iterator.next();
            int physicalOffset = factory.getOffset();
            List<EscherBSERecord> recordsAtOffset = unmatchedRecords.get(physicalOffset);

            if (recordsAtOffset == null || recordsAtOffset.isEmpty()) {
                // There are no records that have an offset matching the physical offset in the stream. We'll do
                //  more complicated and less reliable matching for this factory after all "well known"
                //  image <-> record pairs have been found.
                LOG.atDebug().log("No records with offset {}", box(physicalOffset));
            } else if (recordsAtOffset.size() == 1) {
                // Only 1 record has the same offset as the target image. Assume these are a pair.
                factory.setRecord(recordsAtOffset.get(0));
                unmatchedRecords.remove(physicalOffset);
                iterator.remove();
            } else {

                // Multiple records share an offset. Perform additional matching based on UID.
                for (int i = 0; i < recordsAtOffset.size(); i++) {
                    EscherBSERecord record = recordsAtOffset.get(i);
                    byte[] recordUid = record.getUid();
                    byte[] imageHeader = Arrays.copyOf(factory.imageData, HSLFPictureData.CHECKSUM_SIZE);
                    if (Arrays.equals(recordUid, imageHeader)) {
                        factory.setRecord(record);
                        recordsAtOffset.remove(i);
                        iterator.remove();
                        break;
                    }
                }
            }
        }

        // At this point, any factories remaining didn't have a record with a matching offset. The second pass
        // through the factories pairs based on the UID. Factories for which a record with a matching UID cannot be
        // found will get a new record.
        List<EscherBSERecord> remainingRecords = unmatchedRecords.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        for (PictureFactory factory : unmatchedFactories) {

            boolean matched = false;
            for (int i = remainingRecords.size() - 1; i >= 0; i--) {
                EscherBSERecord record = remainingRecords.get(i);
                byte[] recordUid = record.getUid();
                byte[] imageHeader = Arrays.copyOf(factory.imageData, HSLFPictureData.CHECKSUM_SIZE);
                if (Arrays.equals(recordUid, imageHeader)) {
                    remainingRecords.remove(i);
                    factory.setRecord(record);
                    record.setOffset(factory.getOffset());
                    matched = true;
                }
            }

            if (!matched) {
                // Synthesize a new record
                LOG.atDebug().log("No record found for picture at offset {}", box(factory.offset));
                EscherBSERecord record = HSLFSlideShow.addNewEscherBseRecord(blipStore, factory.type, factory.imageData, factory.offset);
                factory.setRecord(record);
            }
        }

        LOG.atDebug().log("Found {} unmatched records.", box(remainingRecords.size()));
    }

    /**
     * remove duplicated UserEditAtoms and merge PersistPtrHolder, i.e.
     * remove document edit history
     */
    public void normalizeRecords() {
        try {
            updateAndWriteDependantRecords(null, null);
        } catch (IOException e) {
            throw new CorruptPowerPointFileException(e);
        }
        _records = HSLFSlideShowEncrypted.normalizeRecords(_records);
    }


    /**
     * This is a helper functions, which is needed for adding new position dependent records
     * or finally write the slideshow to a file.
     *
     * @param os                 the stream to write to, if null only the references are updated
     * @param interestingRecords a map of interesting records (PersistPtrHolder and UserEditAtom)
     *                           referenced by their RecordType. Only the very last of each type will be saved to the map.
     *                           May be null, if not needed.
     */
    @SuppressWarnings("WeakerAccess")
    public void updateAndWriteDependantRecords(OutputStream os, Map<RecordTypes, PositionDependentRecord> interestingRecords)
            throws IOException {
        // For position dependent records, hold where they were and now are
        // As we go along, update, and hand over, to any Position Dependent
        //  records we happen across
        Map<Integer, Integer> oldToNewPositions = new HashMap<>();

        // First pass - figure out where all the position dependent
        //   records are going to end up, in the new scheme
        // (Annoyingly, some powerpoint files have PersistPtrHolders
        //  that reference slides after the PersistPtrHolder)
        UserEditAtom usr = null;
        PersistPtrHolder ptr = null;
        CountingOS cos = new CountingOS();
        for (Record record : _records) {
            // all top level records are position dependent
            PositionDependentRecord pdr = (PositionDependentRecord) record;
            int oldPos = pdr.getLastOnDiskOffset();
            int newPos = cos.size();
            pdr.setLastOnDiskOffset(newPos);
            if (oldPos != UNSET_OFFSET) {
                // new records don't need a mapping, as they aren't in a relation yet
                oldToNewPositions.put(oldPos, newPos);
            }

            // Grab interesting records as they come past
            // this will only save the very last record of each type
            RecordTypes saveme = null;
            int recordType = (int) record.getRecordType();
            if (recordType == RecordTypes.PersistPtrIncrementalBlock.typeID) {
                saveme = RecordTypes.PersistPtrIncrementalBlock;
                ptr = (PersistPtrHolder) pdr;
            } else if (recordType == RecordTypes.UserEditAtom.typeID) {
                saveme = RecordTypes.UserEditAtom;
                usr = (UserEditAtom) pdr;
            }
            if (interestingRecords != null && saveme != null) {
                interestingRecords.put(saveme, pdr);
            }

            // Dummy write out, so the position winds on properly
            record.writeOut(cos);
        }
        cos.close();

        if (usr == null || ptr == null) {
            throw new HSLFException("UserEditAtom or PersistPtr can't be determined.");
        }

        Map<Integer, Integer> persistIds = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : ptr.getSlideLocationsLookup().entrySet()) {
            persistIds.put(oldToNewPositions.get(entry.getValue()), entry.getKey());
        }

        try (HSLFSlideShowEncrypted encData = new HSLFSlideShowEncrypted(getDocumentEncryptionAtom())) {
            for (Record record : _records) {
                // We've already figured out their new location, and
                // told them that
                // Tell them of the positions of the other records though
                PositionDependentRecord pdr = (PositionDependentRecord) record;
                Integer persistId = persistIds.get(pdr.getLastOnDiskOffset());
                if (persistId == null) {
                    persistId = 0;
                }

                // For now, we're only handling PositionDependentRecord's that
                // happen at the top level.
                // In future, we'll need the handle them everywhere, but that's
                // a bit trickier
                pdr.updateOtherRecordReferences(oldToNewPositions);

                // Whatever happens, write out that record tree
                if (os != null) {
                    record.writeOut(encData.encryptRecord(os, persistId, record));
                }
            }
        }

        // Update and write out the Current User atom
        int oldLastUserEditAtomPos = (int) currentUser.getCurrentEditOffset();
        Integer newLastUserEditAtomPos = oldToNewPositions.get(oldLastUserEditAtomPos);
        if (newLastUserEditAtomPos == null || usr.getLastOnDiskOffset() != newLastUserEditAtomPos) {
            throw new HSLFException("Couldn't find the new location of the last UserEditAtom that used to be at " + oldLastUserEditAtomPos);
        }
        currentUser.setCurrentEditOffset(usr.getLastOnDiskOffset());
    }

    /**
     * Writes out the slideshow to the currently open file.
     * <p>
     * This will fail (with an {@link IllegalStateException} if the
     * slideshow was opened read-only, opened from an {@link InputStream}
     * instead of a File, or if this is not the root document. For those cases,
     * you must use {@link #write(OutputStream)} or {@link #write(File)} to
     * write to a brand new document.
     *
     * @throws IOException           thrown on errors writing to the file
     * @throws IllegalStateException if this isn't from a writable File
     * @since POI 3.15 beta 3
     */
    @Override
    public void write() throws IOException {
        validateInPlaceWritePossible();

        // Write the PowerPoint streams to the current FileSystem
        // No need to do anything to other streams, already there!
        write(getDirectory().getFileSystem(), false);

        // Sync with the File on disk
        getDirectory().getFileSystem().writeFilesystem();
    }

    /**
     * Writes out the slideshow file the is represented by an instance
     * of this class.
     * <p>This will write out only the common OLE2 streams. If you require all
     * streams to be written out, use {@link #write(File, boolean)}
     * with {@code preserveNodes} set to {@code true}.
     *
     * @param newFile The File to write to.
     * @throws IOException If there is an unexpected IOException from writing to the File
     */
    @Override
    public void write(File newFile) throws IOException {
        // Write out, but only the common streams
        write(newFile, false);
    }

    /**
     * Writes out the slideshow file the is represented by an instance
     * of this class.
     * If you require all streams to be written out (eg Marcos, embedded
     * documents), then set {@code preserveNodes} set to {@code true}
     *
     * @param newFile       The File to write to.
     * @param preserveNodes Should all OLE2 streams be written back out, or only the common ones?
     * @throws IOException If there is an unexpected IOException from writing to the File
     */
    public void write(File newFile, boolean preserveNodes) throws IOException {
        // Get a new FileSystem to write into

        try (POIFSFileSystem outFS = POIFSFileSystem.create(newFile)) {
            // Write into the new FileSystem
            write(outFS, preserveNodes);

            // Send the POIFSFileSystem object out to the underlying stream
            outFS.writeFilesystem();
        }
    }

    /**
     * Writes out the slideshow file the is represented by an instance
     * of this class.
     * <p>This will write out only the common OLE2 streams. If you require all
     * streams to be written out, use {@link #write(OutputStream, boolean)}
     * with {@code preserveNodes} set to {@code true}.
     *
     * @param out The OutputStream to write to.
     * @throws IOException If there is an unexpected IOException from
     *                     the passed in OutputStream
     */
    @Override
    public void write(OutputStream out) throws IOException {
        // Write out, but only the common streams
        write(out, false);
    }

    /**
     * Writes out the slideshow file the is represented by an instance
     * of this class.
     * If you require all streams to be written out (eg Macros, embedded
     * documents), then set {@code preserveNodes} set to {@code true}
     *
     * @param out           The OutputStream to write to.
     * @param preserveNodes Should all OLE2 streams be written back out, or only the common ones?
     * @throws IOException If there is an unexpected IOException from
     *                     the passed in OutputStream
     */
    public void write(OutputStream out, boolean preserveNodes) throws IOException {
        // Get a new FileSystem to write into

        try (POIFSFileSystem outFS = new POIFSFileSystem()) {
            // Write into the new FileSystem
            write(outFS, preserveNodes);

            // Send the POIFSFileSystem object out to the underlying stream
            outFS.writeFilesystem(out);
        }
    }

    private void write(POIFSFileSystem outFS, boolean copyAllOtherNodes) throws IOException {
        // read properties and pictures, with old encryption settings where appropriate
        if (_pictures == null) {
            readPictures();
        }
        getDocumentSummaryInformation();

        // The list of entries we've written out
        final List<String> writtenEntries = new ArrayList<>(1);

        // set new encryption settings
        try (HSLFSlideShowEncrypted encryptedSS = new HSLFSlideShowEncrypted(getDocumentEncryptionAtom())) {
            _records = encryptedSS.updateEncryptionRecord(_records);

            // Write out the Property Streams
            writeProperties(outFS, writtenEntries);

            try (UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream()) {

                // For position dependent records, hold where they were and now are
                // As we go along, update, and hand over, to any Position Dependent
                // records we happen across
                updateAndWriteDependantRecords(baos, null);

                // Update our cached copy of the bytes that make up the PPT stream
                _docstream = baos.toByteArray();
            }

            // Write the PPT stream into the POIFS layer
            ByteArrayInputStream bais = new ByteArrayInputStream(_docstream);
            outFS.createOrUpdateDocument(bais, POWERPOINT_DOCUMENT);
            writtenEntries.add(POWERPOINT_DOCUMENT);

            currentUser.setEncrypted(encryptedSS.getDocumentEncryptionAtom() != null);
            currentUser.writeToFS(outFS);
            writtenEntries.add("Current User");

            if (!_pictures.isEmpty()) {
                Enumeration<InputStream> pictEnum = IteratorUtils.asEnumeration(
                    _pictures.stream().map(data -> encryptOnePicture(encryptedSS, data)).iterator()
                );

                try (SequenceInputStream sis = new SequenceInputStream(pictEnum)) {
                    outFS.createOrUpdateDocument(sis, "Pictures");
                    writtenEntries.add("Pictures");
                } catch (IllegalStateException e) {
                    throw (IOException)e.getCause();
                }
            }
        }

        // If requested, copy over any other streams we spot, eg Macros
        if (copyAllOtherNodes) {
            EntryUtils.copyNodes(getDirectory().getFileSystem(), outFS, writtenEntries);
        }
    }

    private static InputStream encryptOnePicture(HSLFSlideShowEncrypted encryptedSS, HSLFPictureData data) {
        try (UnsynchronizedByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream()) {
            data.write(baos);
            byte[] pictBytes = baos.toByteArray();
            encryptedSS.encryptPicture(pictBytes, 0);
            return new ByteArrayInputStream(pictBytes);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public EncryptionInfo getEncryptionInfo() {
        DocumentEncryptionAtom dea = getDocumentEncryptionAtom();
        return (dea != null) ? dea.getEncryptionInfo() : null;
    }


    /* ******************* adding methods follow ********************* */

    /**
     * Adds a new root level record, at the end, but before the last
     * PersistPtrIncrementalBlock.
     */
    @SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
    public synchronized int appendRootLevelRecord(Record newRecord) {
        int addedAt = -1;
        Record[] r = new Record[_records.length + 1];
        boolean added = false;
        for (int i = (_records.length - 1); i >= 0; i--) {
            if (added) {
                // Just copy over
                r[i] = _records[i];
            } else {
                r[(i + 1)] = _records[i];
                if (_records[i] instanceof PersistPtrHolder) {
                    r[i] = newRecord;
                    added = true;
                    addedAt = i;
                }
            }
        }
        _records = r;
        return addedAt;
    }

    /**
     * Add a new picture to this presentation.
     *
     * @return offset of this picture in the Pictures stream
     */
    public int addPicture(HSLFPictureData img) {
        // Process any existing pictures if we haven't yet
        if (_pictures == null) {
            try {
                readPictures();
            } catch (IOException e) {
                throw new CorruptPowerPointFileException(e.getMessage());
            }
        }

        // Add the new picture in
        int offset = 0;
        if (!_pictures.isEmpty()) {
            HSLFPictureData prev = _pictures.get(_pictures.size() - 1);
            offset = prev.getOffset() + prev.getBseSize();
        }
        img.setIndex(_pictures.size() + 1);        // index is 1-based
        _pictures.add(img);
        return offset;
    }

    /* ******************* fetching methods follow ********************* */


    /**
     * Returns an array of all the records found in the slideshow
     */
    public Record[] getRecords() {
        return _records;
    }

    /**
     * Returns an array of the bytes of the file. Only correct after a
     * call to open or write - at all other times might be wrong!
     */
    public byte[] getUnderlyingBytes() {
        return _docstream;
    }

    /**
     * Fetch the Current User Atom of the document
     */
    public CurrentUserAtom getCurrentUserAtom() {
        return currentUser;
    }

    /**
     * Return list of pictures contained in this presentation
     *
     * @return list with the read pictures or an empty list if the
     * presentation doesn't contain pictures.
     */
    public List<HSLFPictureData> getPictureData() {
        if (_pictures == null) {
            try {
                readPictures();
            } catch (IOException e) {
                throw new CorruptPowerPointFileException(e.getMessage());
            }
        }

        return Collections.unmodifiableList(_pictures);
    }

    /**
     * Gets embedded object data from the slide show.
     *
     * @return the embedded objects.
     */
    public HSLFObjectData[] getEmbeddedObjects() {
        if (_objects == null) {
            List<HSLFObjectData> objects = new ArrayList<>();
            for (Record r : _records) {
                if (r instanceof ExOleObjStg) {
                    objects.add(new HSLFObjectData((ExOleObjStg) r));
                }
            }
            _objects = objects.toArray(new HSLFObjectData[0]);
        }
        return _objects;
    }

    private EscherContainerRecord getBlipStore() {
        Document documentRecord = null;
        for (Record record : _records) {
            if (record.getRecordType() == RecordTypes.Document.typeID) {
                documentRecord = (Document) record;
                break;
            }
        }

        if (documentRecord == null) {
            throw new CorruptPowerPointFileException("Document record is missing");
        }

        EscherContainerRecord blipStore;

        EscherContainerRecord dggContainer = documentRecord.getPPDrawingGroup().getDggContainer();
        blipStore = HSLFShape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);
        if (blipStore == null) {
            blipStore = new EscherContainerRecord();
            blipStore.setRecordId(EscherContainerRecord.BSTORE_CONTAINER);

            dggContainer.addChildBefore(blipStore, EscherOptRecord.RECORD_ID);
        }
        return blipStore;
    }

    @Override
    public void close() throws IOException {
        // only close the filesystem, if we are based on the root node.
        // embedded documents/slideshows shouldn't close the parent container
        if (getDirectory().getParent() == null ||
            PP97_DOCUMENT.equals(getDirectory().getName())) {
            POIFSFileSystem fs = getDirectory().getFileSystem();
            if (fs != null) {
                fs.close();
            }
        }
    }

    @Override
    protected String getEncryptedPropertyStreamName() {
        return "EncryptedSummary";
    }

    void writePropertiesImpl() throws IOException {
        super.writeProperties();
    }

    PropertySet getPropertySetImpl(String setName) throws IOException {
        return super.getPropertySet(setName);
    }

    PropertySet getPropertySetImpl(String setName, EncryptionInfo encryptionInfo) throws IOException {
        return super.getPropertySet(setName, encryptionInfo);
    }

    void writePropertiesImpl(POIFSFileSystem outFS, List<String> writtenEntries) throws IOException {
        super.writeProperties(outFS, writtenEntries);
    }

    void validateInPlaceWritePossibleImpl() throws IllegalStateException {
        super.validateInPlaceWritePossible();
    }

    void clearDirectoryImpl() {
        super.clearDirectory();
    }

    boolean initDirectoryImpl() {
        return super.initDirectory();
    }

    void replaceDirectoryImpl(DirectoryNode newDirectory) throws IOException {
        super.replaceDirectory(newDirectory);
    }

    private static class CountingOS extends OutputStream {
        int count;

        @Override
        public void write(int b) throws IOException {
            count++;
        }

        @Override
        public void write(byte[] b) throws IOException {
            count += b.length;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            count += len;
        }

        public int size() {
            return count;
        }
    }

    /**
     * Assists in creating {@link HSLFPictureData} when parsing a slideshow.
     *
     * This class is relied upon heavily by {@link #matchPicturesAndRecords(List, EscherContainerRecord)}.
     */
    static final class PictureFactory {
        final byte[] imageData;

        private final EscherContainerRecord recordContainer;
        private final PictureData.PictureType type;
        private final int offset;
        private final int signature;
        private EscherBSERecord record;

        PictureFactory(
                EscherContainerRecord recordContainer,
                PictureData.PictureType type,
                byte[] imageData,
                int offset,
                int signature
        ) {
            this.recordContainer = Objects.requireNonNull(recordContainer);
            this.type = Objects.requireNonNull(type);
            this.imageData = Objects.requireNonNull(imageData);
            this.offset = offset;
            this.signature = signature;
        }

        int getOffset() {
            return offset;
        }

        /**
         * Constructs a new {@link HSLFPictureData}.
         * <p>
         * The {@link EscherBSERecord} must have been set via {@link #setRecord(EscherBSERecord)} prior to invocation.
         */
        HSLFPictureData build() {
            Objects.requireNonNull(record, "Can't build an instance until the record has been assigned.");
            return HSLFPictureData.createFromSlideshowData(type, recordContainer, record, imageData, signature);
        }

        /**
         * Sets the {@link EscherBSERecord} with which this factory should create a {@link HSLFPictureData}.
         */
        PictureFactory setRecord(EscherBSERecord bse) {
            record = bse;
            return this;
        }
    }
}
