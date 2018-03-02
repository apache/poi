package misc.checkin;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.impl.regex.ParseException;
import org.apache.xmlbeans.impl.regex.RegularExpression;

import java.util.StringTokenizer;

public class XMLBEANS412Test extends TestCase
{
    static String PassedPosCharGroups = "-,\\-,--,\\--,---,\\---,--\\-,\\--\\-,-\\--,\\-\\--,-a,\\-a,a-,"+
            "a\\-,a-b,a\\-b,a\\--,-a-z,\\-a-z,a-z-,a-z\\-,a-z\\-0-9,a\\-z-,a\\-z\\-,a\\-z\\-0-9,"+
            "-0-9,0-9-,0-9aaa,0-9a-,a-z\\--/,A-F0-9.+-,-A-F0-9.+,A-F0-9.+\\-,\\-A-F0-9.+";

    static String FailedPosCharGroups =  "[a--],[a-z-0-9],[a\\-z-0-9],[0-9--],[0-9a--],[0-9-a],[0-9-a-z]";
    static String MiscPassedPatterns = "([\\.a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(([a-zA-Z0-9_-])*\\.([a-zA-Z0-9_-])+)+";

    public XMLBEANS412Test(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(XMLBEANS412Test.class);
    }

    public void testPassedPosCharGroupPatterns()
    {
        StringTokenizer tok = new StringTokenizer(PassedPosCharGroups,",");
        while (tok.hasMoreElements()) {
            String pattern = "[" + tok.nextToken() + "]";
            try {
                new RegularExpression(pattern, "X");
            } catch (ParseException e) {
                Assert.fail("Pattern " + pattern + " failed due to " + e.getMessage());
            }
        }
    }

    public void testNegatedPassedPosCharGroupPatterns()
    {
        StringTokenizer tok = new StringTokenizer(PassedPosCharGroups,",");
        while (tok.hasMoreElements()) {
            String pattern = "[^" + tok.nextToken() + "]";
            try {
                new RegularExpression(pattern, "X");
            } catch (ParseException e) {
                Assert.fail("Pattern " + pattern + " failed due to " + e.getMessage());
            }
        }


    }

    public void testFailedPosCharGroupPatterns()
    {
        StringTokenizer tok = new StringTokenizer(FailedPosCharGroups,",");
        while (tok.hasMoreElements()) {
            String pattern = "[" + tok.nextToken() + "]";
            try {
                new RegularExpression(pattern,"X");
            } catch (ParseException e) {
                continue;
            }
            Assert.fail("Pattern " + pattern + " did not fail.");
        }
    }

    public void testNegatedFailedPosCharGroupPatterns()
    {
        StringTokenizer tok = new StringTokenizer(FailedPosCharGroups,",");
        while (tok.hasMoreElements()) {
            String pattern = "[^" + tok.nextToken() + "]";
            try {
                new RegularExpression(pattern,"X");
            } catch (ParseException e) {
                continue;
            }
            Assert.fail("Pattern " + pattern + " did not fail.");
        }
    }

    public void testMiscPassedPatterns() {
        StringTokenizer tok = new StringTokenizer(MiscPassedPatterns,",");
        while (tok.hasMoreElements()) {
            String pattern = tok.nextToken();
            try {
                new RegularExpression(pattern, "X");
            } catch (ParseException e) {
                Assert.fail("Pattern " + pattern + " failed due to " + e.getMessage());
            }
        }

    }
}
