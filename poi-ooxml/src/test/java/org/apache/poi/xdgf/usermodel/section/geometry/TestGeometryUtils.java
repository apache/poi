package org.apache.poi.xdgf.usermodel.section.geometry;

import com.microsoft.schemas.office.visio.x2012.main.CellType;
import com.microsoft.schemas.office.visio.x2012.main.RowType;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Assertions;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TestGeometryUtils {

    private static final double EPS = 1e-6;

    private TestGeometryUtils() {
    }

    public static RowType createRow(long index, Map<String, Object> cells) {
        RowType row = RowType.Factory.newInstance();
        row.setIX(index);
        row.setDel(false);

        CellType[] cellsArray = cells
                .entrySet()
                .stream()
                .map(entry -> createCell(entry.getKey(), entry.getValue().toString()))
                .toArray(CellType[]::new);
        row.setCellArray(cellsArray);

        return row;
    }

    private static CellType createCell(String name, String value) {
        CellType cell = CellType.Factory.newInstance();
        cell.setN(name);
        cell.setV(value);
        return cell;
    }

    public static void assertPath(Path2D expected, Path2D actual) {
        PathIterator expectedIterator = expected.getPathIterator(null);
        PathIterator actualIterator = actual.getPathIterator(null);

        double[] expectedCoordinates = new double[6];
        double[] actualCoordinates = new double[6];
        while (!expectedIterator.isDone() && !actualIterator.isDone()) {
            int expectedSegmentType = expectedIterator.currentSegment(expectedCoordinates);
            int actualSegmentType = actualIterator.currentSegment(actualCoordinates);

            assertEquals(expectedSegmentType, actualSegmentType);
            assertCoordinates(expectedCoordinates, actualCoordinates);

            expectedIterator.next();
            actualIterator.next();
        }

        if (!expectedIterator.isDone() || !actualIterator.isDone()) {
            Assertions.fail("Path iterators have different number of segments");
        }
    }

    private static void assertCoordinates(double[] expected, double[] actual) {
        if (expected.length != actual.length) {
            Assertions.fail(String.format(
                    LocaleUtil.getUserLocale(),
                    "Given coordinates arrays have different length: expected=%s, actual=%s",
                    Arrays.toString(expected), Arrays.toString(actual)));
        }
        for (int i = 0; i < expected.length; i++) {
            double e = expected[i];
            double a = actual[i];

            if (Math.abs(e - a) > EPS) {
                Assertions.fail(String.format(
                        LocaleUtil.getUserLocale(),
                        "expected <%f> but found <%f>", e, a));
            }
        }
    }

}
