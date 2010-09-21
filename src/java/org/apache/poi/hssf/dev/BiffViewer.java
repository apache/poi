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

package org.apache.poi.hssf.dev;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.RecordInputStream.LeftoverDataException;
import org.apache.poi.hssf.record.chart.*;
import org.apache.poi.hssf.record.pivottable.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;

/**
 *  Utillity for reading in BIFF8 records and displaying data from them.
 *
 *@author     Andrew C. Oliver (acoliver at apache dot org)
 *@author     Glen Stampoultzis (glens at apache.org)
 *@see        #main
 */
public final class BiffViewer {
	static final char[] NEW_LINE_CHARS = System.getProperty("line.separator").toCharArray();

    private BiffViewer() {
		// no instances of this class
	}

	/**
	 *  Create an array of records from an input stream
	 *
	 *@param  is the InputStream from which the records will be obtained
	 *@return an array of Records created from the InputStream
	 *@exception  RecordFormatException  on error processing the InputStream
	 */
	public static Record[] createRecords(InputStream is, PrintStream ps, BiffRecordListener recListener, boolean dumpInterpretedRecords)
			throws RecordFormatException {
		List<Record> temp = new ArrayList<Record>();

		RecordInputStream recStream = new RecordInputStream(is);
		while (true) {
			boolean hasNext;
			try {
				hasNext = recStream.hasNextRecord();
			} catch (LeftoverDataException e) {
				e.printStackTrace();
				System.err.println("Discarding " + recStream.remaining() + " bytes and continuing");
				recStream.readRemainder();
				hasNext = recStream.hasNextRecord();
			}
			if (!hasNext) {
				break;
			}
			recStream.nextRecord();
			if (recStream.getSid() == 0) {
				continue;
			}
			Record record;
			if (dumpInterpretedRecords) {
				record = createRecord (recStream);
				if (record.getSid() == ContinueRecord.sid) {
					continue;
				}
				temp.add(record);

				if (dumpInterpretedRecords) {
					String[] headers = recListener.getRecentHeaders();
					for (int i = 0; i < headers.length; i++) {
						ps.println(headers[i]);
					}
					ps.print(record.toString());
				}
			} else {
				recStream.readRemainder();
			}
			ps.println();
		}
		Record[] result = new Record[temp.size()];
		temp.toArray(result);
		return result;
	}


	/**
	 *  Essentially a duplicate of RecordFactory. Kept separate as not to screw
	 *  up non-debug operations.
	 *
	 */
	private static Record createRecord(RecordInputStream in) {
		switch (in.getSid()) {
			case AreaFormatRecord.sid:     return new AreaFormatRecord(in);
			case AreaRecord.sid:           return new AreaRecord(in);
			case ArrayRecord.sid:          return new ArrayRecord(in);
			case AxisLineFormatRecord.sid: return new AxisLineFormatRecord(in);
			case AxisOptionsRecord.sid:    return new AxisOptionsRecord(in);
			case AxisParentRecord.sid:     return new AxisParentRecord(in);
			case AxisRecord.sid:           return new AxisRecord(in);
			case AxisUsedRecord.sid:       return new AxisUsedRecord(in);
			case AutoFilterInfoRecord.sid: return new AutoFilterInfoRecord(in);
			case BOFRecord.sid:            return new BOFRecord(in);
			case BackupRecord.sid:         return new BackupRecord(in);
			case BarRecord.sid:            return new BarRecord(in);
			case BeginRecord.sid:          return new BeginRecord(in);
			case BlankRecord.sid:          return new BlankRecord(in);
			case BookBoolRecord.sid:       return new BookBoolRecord(in);
			case BoolErrRecord.sid:        return new BoolErrRecord(in);
			case BottomMarginRecord.sid:   return new BottomMarginRecord(in);
			case BoundSheetRecord.sid:     return new BoundSheetRecord(in);
			case CFHeaderRecord.sid:       return new CFHeaderRecord(in);
			case CFRuleRecord.sid:         return new CFRuleRecord(in);
			case CalcCountRecord.sid:      return new CalcCountRecord(in);
			case CalcModeRecord.sid:       return new CalcModeRecord(in);
			case CategorySeriesAxisRecord.sid: return new CategorySeriesAxisRecord(in);
			case ChartFormatRecord.sid:    return new ChartFormatRecord(in);
			case ChartRecord.sid:          return new ChartRecord(in);
			case CodepageRecord.sid:       return new CodepageRecord(in);
			case ColumnInfoRecord.sid:     return new ColumnInfoRecord(in);
			case ContinueRecord.sid:       return new ContinueRecord(in);
			case CountryRecord.sid:        return new CountryRecord(in);
			case DBCellRecord.sid:         return new DBCellRecord(in);
			case DSFRecord.sid:            return new DSFRecord(in);
			case DatRecord.sid:            return new DatRecord(in);
			case DataFormatRecord.sid:     return new DataFormatRecord(in);
			case DateWindow1904Record.sid: return new DateWindow1904Record(in);
			case DefaultColWidthRecord.sid:return new DefaultColWidthRecord(in);
			case DefaultDataLabelTextPropertiesRecord.sid: return new DefaultDataLabelTextPropertiesRecord(in);
			case DefaultRowHeightRecord.sid: return new DefaultRowHeightRecord(in);
			case DeltaRecord.sid:          return new DeltaRecord(in);
			case DimensionsRecord.sid:     return new DimensionsRecord(in);
			case DrawingGroupRecord.sid:   return new DrawingGroupRecord(in);
			case DrawingRecordForBiffViewer.sid: return new DrawingRecordForBiffViewer(in);
			case DrawingSelectionRecord.sid: return new DrawingSelectionRecord(in);
			case DVRecord.sid:             return new DVRecord(in);
			case DVALRecord.sid:           return new DVALRecord(in);
			case EOFRecord.sid:            return new EOFRecord(in);
			case EndRecord.sid:            return new EndRecord(in);
			case ExtSSTRecord.sid:         return new ExtSSTRecord(in);
			case ExtendedFormatRecord.sid: return new ExtendedFormatRecord(in);
			case ExternSheetRecord.sid:    return new ExternSheetRecord(in);
			case ExternalNameRecord.sid:   return new ExternalNameRecord(in);
			case FeatRecord.sid:           return new FeatRecord(in);
			case FeatHdrRecord.sid:        return new FeatHdrRecord(in);
			case FilePassRecord.sid:       return new FilePassRecord(in);
			case FileSharingRecord.sid:    return new FileSharingRecord(in);
			case FnGroupCountRecord.sid:   return new FnGroupCountRecord(in);
			case FontBasisRecord.sid:      return new FontBasisRecord(in);
			case FontIndexRecord.sid:      return new FontIndexRecord(in);
			case FontRecord.sid:           return new FontRecord(in);
			case FooterRecord.sid:         return new FooterRecord(in);
			case FormatRecord.sid:         return new FormatRecord(in);
			case FormulaRecord.sid:        return new FormulaRecord(in);
			case FrameRecord.sid:          return new FrameRecord(in);
			case GridsetRecord.sid:        return new GridsetRecord(in);
			case GutsRecord.sid:           return new GutsRecord(in);
			case HCenterRecord.sid:        return new HCenterRecord(in);
			case HeaderRecord.sid:         return new HeaderRecord(in);
			case HideObjRecord.sid:        return new HideObjRecord(in);
			case HorizontalPageBreakRecord.sid: return new HorizontalPageBreakRecord(in);
			case HyperlinkRecord.sid:      return new HyperlinkRecord(in);
			case IndexRecord.sid:          return new IndexRecord(in);
			case InterfaceEndRecord.sid:   return InterfaceEndRecord.create(in);
			case InterfaceHdrRecord.sid:   return new InterfaceHdrRecord(in);
			case IterationRecord.sid:      return new IterationRecord(in);
			case LabelRecord.sid:          return new LabelRecord(in);
			case LabelSSTRecord.sid:       return new LabelSSTRecord(in);
			case LeftMarginRecord.sid:     return new LeftMarginRecord(in);
			case LegendRecord.sid:         return new LegendRecord(in);
			case LineFormatRecord.sid:     return new LineFormatRecord(in);
			case LinkedDataRecord.sid:     return new LinkedDataRecord(in);
			case MMSRecord.sid:            return new MMSRecord(in);
			case MergeCellsRecord.sid:     return new MergeCellsRecord(in);
			case MulBlankRecord.sid:       return new MulBlankRecord(in);
			case MulRKRecord.sid:          return new MulRKRecord(in);
			case NameRecord.sid:           return new NameRecord(in);
			case NameCommentRecord.sid:    return new NameCommentRecord(in);
			case NoteRecord.sid:           return new NoteRecord(in);
			case NumberRecord.sid:         return new NumberRecord(in);
			case ObjRecord.sid:            return new ObjRecord(in);
			case ObjectLinkRecord.sid:     return new ObjectLinkRecord(in);
			case PaletteRecord.sid:        return new PaletteRecord(in);
			case PaneRecord.sid:           return new PaneRecord(in);
			case PasswordRecord.sid:       return new PasswordRecord(in);
			case PasswordRev4Record.sid:   return new PasswordRev4Record(in);
			case PlotAreaRecord.sid:       return new PlotAreaRecord(in);
			case PlotGrowthRecord.sid:     return new PlotGrowthRecord(in);
			case PrecisionRecord.sid:      return new PrecisionRecord(in);
			case PrintGridlinesRecord.sid: return new PrintGridlinesRecord(in);
			case PrintHeadersRecord.sid:   return new PrintHeadersRecord(in);
			case PrintSetupRecord.sid:     return new PrintSetupRecord(in);
			case ProtectRecord.sid:        return new ProtectRecord(in);
			case ProtectionRev4Record.sid: return new ProtectionRev4Record(in);
			case RKRecord.sid:             return new RKRecord(in);
			case RecalcIdRecord.sid:       return new RecalcIdRecord(in);
			case RefModeRecord.sid:        return new RefModeRecord(in);
			case RefreshAllRecord.sid:     return new RefreshAllRecord(in);
			case RightMarginRecord.sid:    return new RightMarginRecord(in);
			case RowRecord.sid:            return new RowRecord(in);
			case SCLRecord.sid:            return new SCLRecord(in);
			case SSTRecord.sid:            return new SSTRecord(in);
			case SaveRecalcRecord.sid:     return new SaveRecalcRecord(in);
			case SelectionRecord.sid:      return new SelectionRecord(in);
			case SeriesIndexRecord.sid:    return new SeriesIndexRecord(in);
			case SeriesListRecord.sid:     return new SeriesListRecord(in);
			case SeriesRecord.sid:         return new SeriesRecord(in);
			case SeriesTextRecord.sid:     return new SeriesTextRecord(in);
			case SeriesToChartGroupRecord.sid: return new SeriesToChartGroupRecord(in);
			case SharedFormulaRecord.sid:  return new SharedFormulaRecord(in);
			case SheetPropertiesRecord.sid:return new SheetPropertiesRecord(in);
			case StringRecord.sid:         return new StringRecord(in);
			case StyleRecord.sid:          return new StyleRecord(in);
			case SupBookRecord.sid:        return new SupBookRecord(in);
			case TabIdRecord.sid:          return new TabIdRecord(in);
			case TableStylesRecord.sid:    return new TableStylesRecord(in);
			case TableRecord.sid:          return new TableRecord(in);
			case TextObjectRecord.sid:     return new TextObjectRecord(in);
			case TextRecord.sid:           return new TextRecord(in);
			case TickRecord.sid:           return new TickRecord(in);
			case TopMarginRecord.sid:      return new TopMarginRecord(in);
			case UnitsRecord.sid:          return new UnitsRecord(in);
			case UseSelFSRecord.sid:       return new UseSelFSRecord(in);
			case VCenterRecord.sid:        return new VCenterRecord(in);
			case ValueRangeRecord.sid:     return new ValueRangeRecord(in);
			case VerticalPageBreakRecord.sid: return new VerticalPageBreakRecord(in);
			case WSBoolRecord.sid:         return new WSBoolRecord(in);
			case WindowOneRecord.sid:      return new WindowOneRecord(in);
			case WindowProtectRecord.sid:  return new WindowProtectRecord(in);
			case WindowTwoRecord.sid:      return new WindowTwoRecord(in);
			case WriteAccessRecord.sid:    return new WriteAccessRecord(in);
			case WriteProtectRecord.sid:   return new WriteProtectRecord(in);

			// chart
			case CatLabRecord.sid:         return new CatLabRecord(in);
			case ChartEndBlockRecord.sid:  return new ChartEndBlockRecord(in);
			case ChartEndObjectRecord.sid: return new ChartEndObjectRecord(in);
			case ChartFRTInfoRecord.sid:   return new ChartFRTInfoRecord(in);
			case ChartStartBlockRecord.sid: return new ChartStartBlockRecord(in);
			case ChartStartObjectRecord.sid: return new ChartStartObjectRecord(in);

			// pivot table
			case StreamIDRecord.sid:        return new StreamIDRecord(in);
			case ViewSourceRecord.sid:      return new ViewSourceRecord(in);
			case PageItemRecord.sid:        return new PageItemRecord(in);
			case ViewDefinitionRecord.sid:  return new ViewDefinitionRecord(in);
			case ViewFieldsRecord.sid:      return new ViewFieldsRecord(in);
			case DataItemRecord.sid:        return new DataItemRecord(in);
			case ExtendedPivotTableViewFieldsRecord.sid: return new ExtendedPivotTableViewFieldsRecord(in);
		}
		return new UnknownRecord(in);
	}

	private static final class CommandArgs {

		private final boolean _biffhex;
		private final boolean _noint;
		private final boolean _out;
		private final boolean _rawhex;
		private final boolean _noHeader;
		private final File _file;

		private CommandArgs(boolean biffhex, boolean noint, boolean out, boolean rawhex, boolean noHeader, File file) {
			_biffhex = biffhex;
			_noint = noint;
			_out = out;
			_rawhex = rawhex;
			_file = file;
			_noHeader = noHeader;
		}

		public static CommandArgs parse(String[] args) throws CommandParseException {
			int nArgs = args.length;
			boolean biffhex = false;
			boolean noint = false;
			boolean out = false;
			boolean rawhex = false;
			boolean noheader = false;
			File file = null;
			for (int i=0; i<nArgs; i++) {
				String arg = args[i];
				if (arg.startsWith("--")) {
					if ("--biffhex".equals(arg)) {
						biffhex = true;
					} else if ("--noint".equals(arg)) {
						noint = true;
					} else if ("--out".equals(arg)) {
						out = true;
					} else if ("--escher".equals(arg)) {
						System.setProperty("poi.deserialize.escher", "true");
					} else if ("--rawhex".equals(arg)) {
						rawhex = true;
					} else if ("--noheader".equals(arg)) {
						noheader = true;
					} else {
						throw new CommandParseException("Unexpected option '" + arg + "'");
					}
					continue;
				}
				file = new File(arg);
				if (!file.exists()) {
					throw new CommandParseException("Specified file '" + arg + "' does not exist");
				}
				if (i+1<nArgs) {
					throw new CommandParseException("File name must be the last arg");
				}
			}
			if (file == null) {
				throw new CommandParseException("Biff viewer needs a filename");
			}
			return new CommandArgs(biffhex, noint, out, rawhex, noheader, file);
		}
		public boolean shouldDumpBiffHex() {
			return _biffhex;
		}
		public boolean shouldDumpRecordInterpretations() {
			return !_noint;
		}
		public boolean shouldOutputToFile() {
			return _out;
		}
		public boolean shouldOutputRawHexOnly() {
			return _rawhex;
		}
		public boolean suppressHeader() {
			return _noHeader;
		}
		public File getFile() {
			return _file;
		}
	}
	private static final class CommandParseException extends Exception {
		public CommandParseException(String msg) {
			super(msg);
		}
	}

	/**
	 * Method main with 1 argument just run straight biffview against given
	 * file<P>
	 *
	 * <b>Usage</b>:<br/>
	 *
	 * BiffViewer [--biffhex] [--noint] [--noescher] [--out] &lt;fileName&gt; <br/>
	 * BiffViewer --rawhex  [--out] &lt;fileName&gt; <br/>
	 * <br/>
	 *
	 * <table>
	 * <tr><td>--biffhex</td><td>show hex dump of each BIFF record</td></tr>
	 * <tr><td>--noint</td><td>do not output interpretation of BIFF records</td></tr>
	 * <tr><td>--out</td><td>send output to &lt;fileName&gt;.out</td></tr>
	 * <tr><td>--rawhex</td><td>output raw hex dump of whole workbook stream</td></tr>
	 * <tr><td>--escher</td><td>turn on deserialization of escher records (default is off)</td></tr>
	 * <tr><td>--noheader</td><td>do not print record header (default is on)</td></tr>
	 * </table>
	 *
	 */
	public static void main(String[] args) {
		// args = new String[] { "--out", "", };
		CommandArgs cmdArgs;
		try {
			cmdArgs = CommandArgs.parse(args);
		} catch (CommandParseException e) {
			e.printStackTrace();
			return;
		}

		try {

			PrintStream ps;
			if (cmdArgs.shouldOutputToFile()) {
				OutputStream os = new FileOutputStream(cmdArgs.getFile().getAbsolutePath() + ".out");
				ps = new PrintStream(os);
			} else {
				ps = System.out;
			}

			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(cmdArgs.getFile()));
			InputStream is = fs.createDocumentInputStream("Workbook");

			if (cmdArgs.shouldOutputRawHexOnly()) {
				int size = is.available();
				byte[] data = new byte[size];

				is.read(data);
				HexDump.dump(data, 0, System.out, 0);
			} else {
				boolean dumpInterpretedRecords = cmdArgs.shouldDumpRecordInterpretations();
				boolean dumpHex = cmdArgs.shouldDumpBiffHex();
				boolean zeroAlignHexDump = dumpInterpretedRecords;  // TODO - fix non-zeroAlign
				BiffRecordListener recListener = new BiffRecordListener(dumpHex ? new OutputStreamWriter(ps) : null, zeroAlignHexDump, cmdArgs.suppressHeader());
				is = new BiffDumpingStream(is, recListener);
				createRecords(is, ps, recListener, dumpInterpretedRecords);
			}
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final class BiffRecordListener implements IBiffRecordListener {
		private final Writer _hexDumpWriter;
		private final List<String> _headers;
		private final boolean _zeroAlignEachRecord;
		private final boolean _noHeader;
		public BiffRecordListener(Writer hexDumpWriter, boolean zeroAlignEachRecord, boolean noHeader) {
			_hexDumpWriter = hexDumpWriter;
			_zeroAlignEachRecord = zeroAlignEachRecord;
			_noHeader = noHeader;
			_headers = new ArrayList<String>();
		}

		public void processRecord(int globalOffset, int recordCounter, int sid, int dataSize,
				byte[] data) {
			String header = formatRecordDetails(globalOffset, sid, dataSize, recordCounter);
			if(!_noHeader) _headers.add(header);
			Writer w = _hexDumpWriter;
			if (w != null) {
				try {
					w.write(header);
					w.write(NEW_LINE_CHARS);
					hexDumpAligned(w, data, dataSize+4, globalOffset, _zeroAlignEachRecord);
					w.flush();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		public String[] getRecentHeaders() {
			String[] result = new String[_headers.size()];
			_headers.toArray(result);
			_headers.clear();
			return result;
		}
		private static String formatRecordDetails(int globalOffset, int sid, int size, int recordCounter) {
			StringBuffer sb = new StringBuffer(64);
			sb.append("Offset=").append(HexDump.intToHex(globalOffset)).append("(").append(globalOffset).append(")");
			sb.append(" recno=").append(recordCounter);
			sb.append(  " sid=").append(HexDump.shortToHex(sid));
			sb.append( " size=").append(HexDump.shortToHex(size)).append("(").append(size).append(")");
			return sb.toString();
		}
	}

	private static interface IBiffRecordListener {

		void processRecord(int globalOffset, int recordCounter, int sid, int dataSize, byte[] data);

	}

	/**
	 * Wraps a plain {@link InputStream} and allows BIFF record information to be tapped off
	 *
	 */
	private static final class BiffDumpingStream extends InputStream {
		private final DataInputStream _is;
		private final IBiffRecordListener _listener;
		private final byte[] _data;
		private int _recordCounter;
		private int _overallStreamPos;
		private int _currentPos;
		private int _currentSize;
		private boolean _innerHasReachedEOF;

		public BiffDumpingStream(InputStream is, IBiffRecordListener listener) {
			_is = new DataInputStream(is);
			_listener = listener;
			_data = new byte[RecordInputStream.MAX_RECORD_DATA_SIZE + 4];
			_recordCounter = 0;
			_overallStreamPos = 0;
			_currentSize = 0;
			_currentPos = 0;
		}

		public int read() throws IOException {
			if (_currentPos >= _currentSize) {
				fillNextBuffer();
			}
			if (_currentPos >= _currentSize) {
				return -1;
			}
			int result = _data[_currentPos] & 0x00FF;
			_currentPos ++;
			_overallStreamPos ++;
			formatBufferIfAtEndOfRec();
			return result;
		}
		public int read(byte[] b, int off, int len) throws IOException {
			if (_currentPos >= _currentSize) {
				fillNextBuffer();
			}
			if (_currentPos >= _currentSize) {
				return -1;
			}
			int availSize = _currentSize - _currentPos;
			int result;
			if (len > availSize) {
				System.err.println("Unexpected request to read past end of current biff record");
				result = availSize;
			} else {
				result = len;
			}
			System.arraycopy(_data, _currentPos, b, off, result);
			_currentPos += result;
			_overallStreamPos += result;
			formatBufferIfAtEndOfRec();
			return result;
		}

		public int available() throws IOException {
			return _currentSize - _currentPos + _is.available();
		}
		private void fillNextBuffer() throws IOException {
			if (_innerHasReachedEOF) {
				return;
			}
			int b0 = _is.read();
			if (b0 == -1) {
				_innerHasReachedEOF = true;
				return;
			}
			_data[0] = (byte) b0;
			_is.readFully(_data, 1, 3);
			int len = LittleEndian.getShort(_data, 2);
			_is.readFully(_data, 4, len);
			_currentPos = 0;
			_currentSize = len + 4;
			_recordCounter++;
		}
		private void formatBufferIfAtEndOfRec() {
			if (_currentPos != _currentSize) {
				return;
			}
			int dataSize = _currentSize-4;
			int sid = LittleEndian.getShort(_data, 0);
			int globalOffset = _overallStreamPos-_currentSize;
			_listener.processRecord(globalOffset, _recordCounter, sid, dataSize, _data);
		}
		public void close() throws IOException {
			_is.close();
		}
	}

	private static final int DUMP_LINE_LEN = 16;
	private static final char[] COLUMN_SEPARATOR = " | ".toCharArray();
	/**
	 * Hex-dumps a portion of a byte array in typical format, also preserving dump-line alignment
	 * @param globalOffset (somewhat arbitrary) used to calculate the addresses printed at the
	 * start of each line
	 */
	static void hexDumpAligned(Writer w, byte[] data, int dumpLen, int globalOffset,
			boolean zeroAlignEachRecord) {
		int baseDataOffset = 0;

		// perhaps this code should be moved to HexDump
		int globalStart = globalOffset + baseDataOffset;
		int globalEnd = globalOffset + baseDataOffset + dumpLen;
		int startDelta = globalStart % DUMP_LINE_LEN;
		int endDelta = globalEnd % DUMP_LINE_LEN;
		if (zeroAlignEachRecord) {
			endDelta -= startDelta;
			if (endDelta < 0) {
				endDelta += DUMP_LINE_LEN;
			}
			startDelta = 0;
		}
		int startLineAddr;
		int endLineAddr;
		if (zeroAlignEachRecord) {
			endLineAddr = globalEnd - endDelta - (globalStart - startDelta);
			startLineAddr = 0;
		} else {
			startLineAddr = globalStart - startDelta;
			endLineAddr = globalEnd - endDelta;
		}

		int lineDataOffset = baseDataOffset - startDelta;
		int lineAddr = startLineAddr;

		// output (possibly incomplete) first line
		if (startLineAddr == endLineAddr) {
			hexDumpLine(w, data, lineAddr, lineDataOffset, startDelta, endDelta);
			return;
		}
		hexDumpLine(w, data, lineAddr, lineDataOffset, startDelta, DUMP_LINE_LEN);

		// output all full lines in the middle
		while (true) {
			lineAddr += DUMP_LINE_LEN;
			lineDataOffset += DUMP_LINE_LEN;
			if (lineAddr >= endLineAddr) {
				break;
			}
			hexDumpLine(w, data, lineAddr, lineDataOffset, 0, DUMP_LINE_LEN);
		}


		// output (possibly incomplete) last line
		if (endDelta != 0) {
			hexDumpLine(w, data, lineAddr, lineDataOffset, 0, endDelta);
		}
	}

	private static void hexDumpLine(Writer w, byte[] data, int lineStartAddress, int lineDataOffset, int startDelta, int endDelta) {
		if (startDelta >= endDelta) {
			throw new IllegalArgumentException("Bad start/end delta");
		}
		try {
			writeHex(w, lineStartAddress, 8);
			w.write(COLUMN_SEPARATOR);
			// raw hex data
			for (int i=0; i< DUMP_LINE_LEN; i++) {
				if (i>0) {
					w.write(" ");
				}
				if (i >= startDelta && i < endDelta) {
					writeHex(w, data[lineDataOffset+i], 2);
				} else {
					w.write("  ");
				}
			}
			w.write(COLUMN_SEPARATOR);

			// interpreted ascii
			for (int i=0; i< DUMP_LINE_LEN; i++) {
				if (i >= startDelta && i < endDelta) {
					w.write(getPrintableChar(data[lineDataOffset+i]));
				} else {
					w.write(" ");
				}
			}
			w.write(NEW_LINE_CHARS);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static char getPrintableChar(byte b) {
		char ib = (char) (b & 0x00FF);
		if (ib < 32 || ib > 126) {
			return '.';
		}
		return ib;
	}

	private static void writeHex(Writer w, int value, int nDigits) throws IOException {
		char[] buf = new char[nDigits];
		int acc = value;
		for(int i=nDigits-1; i>=0; i--) {
			int digit = acc & 0x0F;
			buf[i] = (char) (digit < 10 ? ('0' + digit) : ('A' + digit - 10));
			acc >>= 4;
		}
		w.write(buf);
	}
}
