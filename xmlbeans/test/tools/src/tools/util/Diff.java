package tools.util;

// Diff -- text file difference utility.
// See full docu-comment at beginning of Diff class.

// $Id$

import java.io.*;
import java.util.Vector;

/**
 * This is the info kept per-file.
 */
class fileInfo
{

    static final int MAXLINECOUNT = 20000;

    //DataInputStream file;	/* File handle that is open for read.  */
    File file;
    public int maxLine;	/* After input done, # lines in file.  */
    node symbol[]; /* The symtab handle of each line. */
    int other[]; /* Map of line# to line# in other file */
    /* ( -1 means don't-know ).            */
    /* Allocated AFTER the lines are read. */

    /**
     * Normal constructor with one filename
     * Tests whether file actually exists
     */
    fileInfo(String filename)
    {
        symbol = new node[MAXLINECOUNT + 2];
        other = null;		// allocated later!
        file = new File(filename);
    }

    // This is done late, to be same size as # lines in input file.
    void alloc()
    {
        other = new int[symbol.length + 2];
    }
}

;

/**
 * diff         Text file difference utility.
 * ----         Copyright 1987, 1989 by Donald C. Lindsay,
 * School of Computer Science,  Carnegie Mellon University.
 * Copyright 1982 by Symbionics.
 * Use without fee is permitted when not for direct commercial
 * advantage, and when credit to the source is given. Other uses
 * require specific permission.
 *
 * Converted from C to Java by Ian F. Darwin, ian@darwinsys.com, January, 1997.
 * Copyright 1997, Ian F. Darwin.
 *
 * Conversion is NOT FULLY TESTED.
 *
 * USAGE:      diff oldfile newfile
 *
 * This program assumes that "oldfile" and "newfile" are text files.
 * The program writes to stdout a description of the changes which would
 * transform "oldfile" into "newfile".
 *
 * The printout is in the form of commands, each followed by a block of
 * text. The text is delimited by the commands, which are:
 *
 * DELETE AT n
 * ..deleted lines
 *
 * INSERT BEFORE n
 * ..inserted lines
 *
 * n MOVED TO BEFORE n
 * ..moved lines
 *
 * n CHANGED FROM
 * ..old lines
 * CHANGED TO
 * ..newer lines
 *
 * The line numbers all refer to the lines of the oldfile, as they are
 * numbered before any commands are applied.
 * The text lines are printed as-is, without indentation or prefixing. The
 * commands are printed in upper case, with a prefix of ">>>>", so that
 * they will stand out. Other schemes may be preferred.
 * Files which contain more than MAXLINECOUNT lines cannot be processed.
 * This can be fixed by changing "symbol" to a Vector.
 * The algorithm is taken from Communications of the ACM, Apr78 (21, 4, 264-),
 * "A Technique for Isolating Differences Between Files."
 * Ignoring I/O, and ignoring the symbol table, it should take O(N) time.
 * This implementation takes fixed space, plus O(U) space for the symbol
 * table (where U is the number of unique lines). Methods exist to change
 * the fixed space to O(N) space.
 * Note that this is not the only interesting file-difference algorithm. In
 * general, different algorithms draw different conclusions about the
 * changes that have been made to the oldfile. This algorithm is sometimes
 * "more right", particularly since it does not consider a block move to be
 * an insertion and a (separate) deletion. However, on some files it will be
 * "less right". This is a consequence of the fact that files may contain
 * many identical lines (particularly if they are program source). Each
 * algorithm resolves the ambiguity in its own way, and the resolution
 * is never guaranteed to be "right". However, it is often excellent.
 * This program is intended to be pedagogic.  Specifically, this program was
 * the basis of the Literate Programming column which appeared in the
 * Communications of the ACM (CACM), in the June 1989 issue (32, 6,
 * 740-755).
 * By "pedagogic", I do not mean that the program is gracefully worded, or
 * that it showcases language features or its algorithm. I also do not mean
 * that it is highly accessible to beginners, or that it is intended to be
 * read in full, or in a particular order. Rather, this program is an
 * example of one professional's style of keeping things organized and
 * maintainable.
 * The program would be better if the "print" variables were wrapped into
 * a struct. In general, grouping related variables in this way improves
 * documentation, and adds the ability to pass the group in argument lists.
 * This program is a de-engineered version of a program which uses less
 * memory and less time.  The article points out that the "symbol" arrays
 * can be implemented as arrays of pointers to arrays, with dynamic
 * allocation of the subarrays.  (In C, macros are very useful for hiding
 * the two-level accesses.) In Java, a Vector would be used. This allows an
 * extremely large value for MAXLINECOUNT, without dedicating fixed arrays.
 * (The "other" array can be allocated after the input phase, when the exact
 * sizes are known.) The only slow piece of code is the "strcmp" in the tree
 * descent: it can be speeded up by keeping a hash in the tree node, and
 * only using "strcmp" when two hashes happen to be equal.
 *
 * Change Log
 * ----------
 * 1Jan97 Ian F. Darwin: first working rewrite in Java, based entirely on
 * D.C.Lindsay's reasonable C version.
 * Changed comments from /***************** to /**, shortened, added
 * whitespace, used tabs more, etc.
 * 6jul89 D.C.Lindsay, CMU: fixed portability bug. Thanks, Gregg Wonderly.
 * Just changed "char ch" to "int ch".
 * Also added comment about way to improve code.
 * 10jun89 D.C.Lindsay, CMU: posted version created.
 * Copyright notice changed to ACM style, and Dept. is now School.
 * ACM article referenced in docn.
 * 26sep87 D.C.Lindsay, CMU: publication version created.
 * Condensed all 1982/83 change log entries.
 * Removed all command line options, and supporting code. This
 * simplified the input code (no case reduction etc). It also
 * simplified the symbol table, which was capable of remembering
 * offsets into files (instead of strings), and trusting (!) hash
 * values to be unique.
 * Removed dynamic allocation of arrays: now fixed static arrays.
 * Removed speed optimizations in symtab package.
 * Removed string compression/decompression code.
 * Recoded to Unix standards from old Lattice/MSDOS standards.
 * (This affected only the #include's and the IO.)
 * Some renaming of variables, and rewording of comments.
 * 1982/83 D.C.Lindsay, Symbionics: created.
 *
 * @author	Ian F. Darwin, Java version
 * @version	Java version 0.9, 1997
 * @author	D. C. Lindsay, C version (1982-1987)
 */
public class Diff
{

    /**
     * block len > any possible real block len
     */
    final int UNREAL = Integer.MAX_VALUE;

    /**
     * Keeps track of information about file1 and file2
     */
    fileInfo oldinfo, newinfo;

    /**
     * blocklen is the info about found blocks. It will be set to 0, except
     * at the line#s where blocks start in the old file. At these places it
     * will be set to the # of lines in the block. During printout ,
     * this # will be reset to -1 if the block is printed as a MOVE block
     * (because the printout phase will encounter the block twice, but
     * must only print it once.)
     * The array declarations are to MAXLINECOUNT+2 so that we can have two
     * extra lines (pseudolines) at line# 0 and line# MAXLINECOUNT+1
     * (or less).
     */
    int blocklen[];

    /**
     * Controls how trailing whitespace lines is handled
     */
    boolean trimLines;

    /**
     * main - entry point when used standalone.
     * NOTE: no routines return error codes or throw any local
     * exceptions. Instead, any routine may complain
     * to stderr and then exit with error to the system.
     */
    public static void main(String argstrings[])
    {
        if (argstrings.length != 2)
        {
            System.err.println("Usage: diff oldfile newfile");
            System.exit(1);
        }
        Diff d = new Diff(false);
        d.doDiff(argstrings[0], argstrings[1]);
        return;
    }

    /**
     * Construct a Diff object.
     */
    public Diff(boolean trimLines)
    {
        this.trimLines = trimLines;
    }

    /**
     * Construct a Diff object.
     */
    public Diff()
    {
        this.trimLines = false;
    }

    /**
     * Do one file comparison. Called with both filenames.
     */
    public void doDiff(String oldFile, String newFile)
    {
        println(">>>> Difference of file \"" + oldFile +
                "\" and file \"" + newFile + "\".\n");
        oldinfo = new fileInfo(oldFile);
        newinfo = new fileInfo(newFile);
        /* we don't process until we know both files really do exist. */
        try
        {
            inputscan(oldinfo);
            inputscan(newinfo);
        }
        catch (IOException e)
        {
            System.err.println("Read error: " + e);
        }

        /* Now that we've read all the lines, allocate some arrays.
         */
        blocklen = new int[(oldinfo.maxLine > newinfo.maxLine ?
                oldinfo.maxLine : newinfo.maxLine) + 2];
        oldinfo.alloc();
        newinfo.alloc();

        /* Now do the work, and print the results. */
        transform();
        printout();
    }

    public String compareFiles(String oldFile, String newFile)
        throws IOException
    {
        //println("Diff of \"" + oldFile + "\" and \"" + newFile);
        oldinfo = new fileInfo(oldFile);
        newinfo = new fileInfo(newFile);
        /* we don't process until we know both files really do exist. */
        inputscan(oldinfo);
        inputscan(newinfo);

        /* Now that we've read all the lines, allocate some arrays.
         */
        blocklen = new int[(oldinfo.maxLine > newinfo.maxLine ?
                                        oldinfo.maxLine : newinfo.maxLine) + 2];
        oldinfo.alloc();
        newinfo.alloc();

        /* Now do the work, and print the results. */
        transform();
        return printout();
    }


    /**
     * inputscan    Reads the file specified by pinfo.file.
     * ---------    Places the lines of that file in the symbol table.
     * Sets pinfo.maxLine to the number of lines found.
     */
    void inputscan(fileInfo pinfo)
            throws IOException
    {
        pinfo.maxLine = 0;

        BufferedReader reader = new BufferedReader(new FileReader(pinfo.file));
        Vector v = new Vector();

        while (true)
        {
            String s = reader.readLine();
            if (s == null) break;
            v.addElement(s);
        }
        reader.close();
        // Discard all trailing lines that are only whitespaces..
        if (trimLines)
        {
            int i = v.size();
            while (--i >= 0)
                if (Util.isWhiteSpace((String) v.get(i)))
                    v.removeElementAt(i);
        }

        int i = 0;
        while (i < v.size())
            storeline((String) v.get(i++), pinfo);

    }

    /**
     * storeline    Places line into symbol table.
     * ---------    Expects pinfo.maxLine initted: increments.
     * Places symbol table handle in pinfo.ymbol.
     * Expects pinfo is either oldinfo or newinfo.
     */
    void storeline(String linebuffer, fileInfo pinfo)
    {
        int linenum = ++pinfo.maxLine;    /* note, no line zero */
        if (linenum > fileInfo.MAXLINECOUNT)
        {
            System.err.println("MAXLINECOUNT exceeded, must stop.");
            System.exit(1);
        }
        pinfo.symbol[linenum] =
                node.addSymbol(linebuffer, pinfo == oldinfo, linenum);
    }

    /*
     * transform
     * Analyzes the file differences and leaves its findings in
     * the global arrays oldinfo.other, newinfo.other, and blocklen.
     * Expects both files in symtab.
     * Expects valid "maxLine" and "symbol" in oldinfo and newinfo.
     */
    void transform()
    {
        int oldline, newline;
        int oldmax = oldinfo.maxLine + 2;  /* Count pseudolines at  */
        int newmax = newinfo.maxLine + 2;  /* ..front and rear of file */

        for (oldline = 0; oldline < oldmax; oldline++)
            oldinfo.other[oldline] = -1;
        for (newline = 0; newline < newmax; newline++)
            newinfo.other[newline] = -1;

        scanunique();  /* scan for lines used once in both files */
        scanafter();   /* scan past sure-matches for non-unique blocks */
        scanbefore();  /* scan backwards from sure-matches */
        scanblocks();  /* find the fronts and lengths of blocks */
    }

    /*
     * scanunique
     * Scans for lines which are used exactly once in each file.
     * Expects both files in symtab, and oldinfo and newinfo valid.
     * The appropriate "other" array entries are set to the line# in
     * the other file.
     * Claims pseudo-lines at 0 and XXXinfo.maxLine+1 are unique.
     */
    void scanunique()
    {
        int oldline, newline;
        node psymbol;

        for (newline = 1; newline <= newinfo.maxLine; newline++)
        {
            psymbol = newinfo.symbol[newline];
            if (psymbol.symbolIsUnique())
            {        // 1 use in each file
                oldline = psymbol.linenum;
                newinfo.other[newline] = oldline; // record 1-1 map
                oldinfo.other[oldline] = newline;
            }
        }
        newinfo.other[0] = 0;
        oldinfo.other[0] = 0;
        newinfo.other[newinfo.maxLine + 1] = oldinfo.maxLine + 1;
        oldinfo.other[oldinfo.maxLine + 1] = newinfo.maxLine + 1;
    }

    /*
     * scanafter
     * Expects both files in symtab, and oldinfo and newinfo valid.
     * Expects the "other" arrays contain positive #s to indicate
     * lines that are unique in both files.
     * For each such pair of places, scans past in each file.
     * Contiguous groups of lines that match non-uniquely are
     * taken to be good-enough matches, and so marked in "other".
     * Assumes each other[0] is 0.
     */
    void scanafter()
    {
        int oldline, newline;

        for (newline = 0; newline <= newinfo.maxLine; newline++)
        {
            oldline = newinfo.other[newline];
            if (oldline >= 0)
            {	/* is unique in old & new */
                for (; ;)
                {	/* scan after there in both files */
                    if (++oldline > oldinfo.maxLine) break;
                    if (oldinfo.other[oldline] >= 0) break;
                    if (++newline > newinfo.maxLine) break;
                    if (newinfo.other[newline] >= 0) break;

                    /* oldline & newline exist, and
                    aren't already matched */

                    if (newinfo.symbol[newline] !=
                            oldinfo.symbol[oldline])
                        break;  // not same

                    newinfo.other[newline] = oldline; // record a match
                    oldinfo.other[oldline] = newline;
                }
            }
        }
    }

    /**
     * scanbefore
     * As scanafter, except scans towards file fronts.
     * Assumes the off-end lines have been marked as a match.
     */
    void scanbefore()
    {
        int oldline, newline;

        for (newline = newinfo.maxLine + 1; newline > 0; newline--)
        {
            oldline = newinfo.other[newline];
            if (oldline >= 0)
            {               /* unique in each */
                for (; ;)
                {
                    if (--oldline <= 0) break;
                    if (oldinfo.other[oldline] >= 0) break;
                    if (--newline <= 0) break;
                    if (newinfo.other[newline] >= 0) break;

                    /* oldline and newline exist,
                    and aren't marked yet */

                    if (newinfo.symbol[newline] !=
                            oldinfo.symbol[oldline])
                        break;  // not same

                    newinfo.other[newline] = oldline; // record a match
                    oldinfo.other[oldline] = newline;
                }
            }
        }
    }

    /**
     * scanblocks - Finds the beginnings and lengths of blocks of matches.
     * Sets the blocklen array (see definition).
     * Expects oldinfo valid.
     */
    void scanblocks()
    {
        int oldline, newline;
        int oldfront = 0;      // line# of front of a block in old, or 0
        int newlast = -1;      // newline's value during prev. iteration

        for (oldline = 1; oldline <= oldinfo.maxLine; oldline++)
            blocklen[oldline] = 0;
        blocklen[oldinfo.maxLine + 1] = UNREAL; // starts a mythical blk

        for (oldline = 1; oldline <= oldinfo.maxLine; oldline++)
        {
            newline = oldinfo.other[oldline];
            if (newline < 0)
                oldfront = 0;  /* no match: not in block */
            else
            {                                   /* match. */
                if (oldfront == 0) oldfront = oldline;
                if (newline != (newlast + 1)) oldfront = oldline;
                ++blocklen[oldfront];
            }
            newlast = newline;
        }
    }

    /* The following are global to printout's subsidiary routines */
    // enum{ idle, delete, insert, movenew, moveold,
    // same, change } printstatus;
    public static final int
            idle = 0, delete = 1, insert = 2, movenew = 3, moveold = 4,
    same = 5, change = 6;
    int printstatus;
    boolean anyprinted;
    int printoldline, printnewline;     // line numbers in old & new file

    /**
     * printout - Prints summary to stdout.
     * Expects all data structures have been filled out.
     */
    String printout()
    {
        printstatus = idle;
        anyprinted = false;
        PrintStream _sysout = System.out;
        _sysout.flush();
        String ret;
        try
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            System.setOut(new PrintStream(bout));
            for (printoldline = printnewline = 1; ;)
            {
                if (printoldline > oldinfo.maxLine)
                {
                    newconsume();
                    break;
                }
                if (printnewline > newinfo.maxLine)
                {
                    oldconsume();
                    break;
                }
                if (newinfo.other[printnewline] < 0)
                {
                    if (oldinfo.other[printoldline] < 0)
                        showchange();
                    else
                        showinsert();
                } else if (oldinfo.other[printoldline] < 0)
                    showdelete();
                else if (blocklen[printoldline] < 0)
                    skipold();
                else if (oldinfo.other[printoldline] == printnewline)
                    showsame();
                else
                    showmove();
            }

            if (anyprinted)
                ret = bout.toString();
            else
                ret = null;
        }
        finally
        {
            System.setOut(_sysout);
        }

        return ret;
    }

    /*
     * newconsume        Part of printout. Have run out of old file.
     * Print the rest of the new file, as inserts and/or moves.
     */
    void newconsume()
    {
        for (; ;)
        {
            if (printnewline > newinfo.maxLine)
                break;        /* end of file */
            if (newinfo.other[printnewline] < 0)
                showinsert();
            else
                showmove();
        }
    }

    /**
     * oldconsume        Part of printout. Have run out of new file.
     * Process the rest of the old file, printing any
     * parts which were deletes or moves.
     */
    void oldconsume()
    {
        for (; ;)
        {
            if (printoldline > oldinfo.maxLine)
                break;       /* end of file */
            printnewline = oldinfo.other[printoldline];
            if (printnewline < 0)
                showdelete();
            else if (blocklen[printoldline] < 0)
                skipold();
            else
                showmove();
        }
    }

    /**
     * showdelete        Part of printout.
     * Expects printoldline is at a deletion.
     */
    void showdelete()
    {
        if (printstatus != delete)
            println(">>>> DELETE AT " + printoldline);
        printstatus = delete;
        oldinfo.symbol[printoldline].showSymbol();
        anyprinted = true;
        printoldline++;
    }

    /*
     * showinsert        Part of printout.
     * Expects printnewline is at an insertion.
     */
    void showinsert()
    {
        if (printstatus == change)
            println(">>>>     CHANGED TO");
        else if (printstatus != insert)
            println(">>>> INSERT BEFORE " + printoldline);
        printstatus = insert;
        newinfo.symbol[printnewline].showSymbol();
        anyprinted = true;
        printnewline++;
    }

    /**
     * showchange        Part of printout.
     * Expects printnewline is an insertion.
     * Expects printoldline is a deletion.
     */
    void showchange()
    {
        if (printstatus != change)
            println(">>>> " + printoldline + " CHANGED FROM");
        printstatus = change;
        oldinfo.symbol[printoldline].showSymbol();
        anyprinted = true;
        printoldline++;
    }

    /**
     * skipold           Part of printout.
     * Expects printoldline at start of an old block that has
     * already been announced as a move.
     * Skips over the old block.
     */
    void skipold()
    {
        printstatus = idle;
        for (; ;)
        {
            if (++printoldline > oldinfo.maxLine)
                break;     /* end of file  */
            if (oldinfo.other[printoldline] < 0)
                break;    /* end of block */
            if (blocklen[printoldline] != 0)
                break;          /* start of another */
        }
    }

    /**
     * skipnew           Part of printout.
     * Expects printnewline is at start of a new block that has
     * already been announced as a move.
     * Skips over the new block.
     */
    void skipnew()
    {
        int oldline;
        printstatus = idle;
        for (; ;)
        {
            if (++printnewline > newinfo.maxLine)
                break;    /* end of file  */
            oldline = newinfo.other[printnewline];
            if (oldline < 0)
                break;                         /* end of block */
            if (blocklen[oldline] != 0)
                break;              /* start of another */
        }
    }

    /**
     * showsame          Part of printout.
     * Expects printnewline and printoldline at start of
     * two blocks that aren't to be displayed.
     */
    void showsame()
    {
        int count;
        printstatus = idle;
        if (newinfo.other[printnewline] != printoldline)
        {
            System.err.println("BUG IN LINE REFERENCING");
            System.exit(1);
        }
        count = blocklen[printoldline];
        printoldline += count;
        printnewline += count;
    }

    /**
     * showmove          Part of printout.
     * Expects printoldline, printnewline at start of
     * two different blocks ( a move was done).
     */
    void showmove()
    {
        int oldblock = blocklen[printoldline];
        int newother = newinfo.other[printnewline];
        int newblock = blocklen[newother];

        if (newblock < 0)
            skipnew();         // already printed.
        else if (oldblock >= newblock)
        {     // assume new's blk moved.
            blocklen[newother] = -1;         // stamp block as "printed".
            println(">>>> " + newother +
                    " THRU " + (newother + newblock - 1) +
                    " MOVED TO BEFORE " + printoldline);
            for (; newblock > 0; newblock--, printnewline++)
                newinfo.symbol[printnewline].showSymbol();
            anyprinted = true;
            printstatus = idle;

        } else                /* assume old's block moved */
            skipold();      /* target line# not known, display later */
    }

    /**
     * Convenience wrapper for println
     */
    public void println(String s)
    {
        System.out.println(s);
    }
}

;				// end of main class!

/**
 * Class "node". The symbol table routines in this class all
 * understand the symbol table format, which is a binary tree.
 * The methods are: addSymbol, symbolIsUnique, showSymbol.
 */
class node
{                       /* the tree is made up of these nodes */
    node pleft, pright;
    int linenum;

    static final int freshnode = 0,
    oldonce = 1, newonce = 2, bothonce = 3, other = 4;

    int /* enum linestates */ linestate;
    String line;

    static node panchor = null;    /* symtab is a tree hung from this */

    /**
     * Construct a new symbol table node and fill in its fields.
     *
     * @param string A line of the text file
     */
    node(String pline)
    {
        pleft = pright = null;
        linestate = freshnode;
        /* linenum field is not always valid */
        line = pline;
    }

    /**
     * matchsymbol       Searches tree for a match to the line.
     *
     * @param	String	pline, a line of text
     * If node's linestate == freshnode, then created the node.
     */
    static node matchsymbol(String pline)
    {
        int comparison;
        node pnode = panchor;
        if (panchor == null) return panchor = new node(pline);
        for (; ;)
        {
            comparison = pnode.line.compareTo(pline);
            if (comparison == 0) return pnode;          /* found */

            if (comparison < 0)
            {
                if (pnode.pleft == null)
                {
                    pnode.pleft = new node(pline);
                    return pnode.pleft;
                }
                pnode = pnode.pleft;
            }
            if (comparison > 0)
            {
                if (pnode.pright == null)
                {
                    pnode.pright = new node(pline);
                    return pnode.pright;
                }
                pnode = pnode.pright;
            }
        }
        /* NOTE: There are return stmts, so control does not get here. */
    }

    /**
     * addSymbol(String pline) - Saves line into the symbol table.
     * Returns a handle to the symtab entry for that unique line.
     * If inoldfile nonzero, then linenum is remembered.
     */
    static node addSymbol(String pline, boolean inoldfile, int linenum)
    {
        node pnode;
        pnode = matchsymbol(pline);  /* find the node in the tree */
        if (pnode.linestate == freshnode)
        {
            pnode.linestate = inoldfile ? oldonce : newonce;
        } else
        {
            if ((pnode.linestate == oldonce && !inoldfile) ||
                    (pnode.linestate == newonce && inoldfile))
                pnode.linestate = bothonce;
            else
                pnode.linestate = other;
        }
        if (inoldfile) pnode.linenum = linenum;
        return pnode;
    }

    /**
     * symbolIsUnique    Arg is a ptr previously returned by addSymbol.
     * --------------    Returns true if the line was added to the
     * symbol table exactly once with inoldfile true,
     * and exactly once with inoldfile false.
     */
    boolean symbolIsUnique()
    {
        return (linestate == bothonce);
    }

    /**
     * showSymbol        Prints the line to stdout.
     */
    void showSymbol()
    {
        System.out.println(line);
    }
}

