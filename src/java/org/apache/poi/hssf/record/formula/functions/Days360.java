package org.apache.poi.hssf.record.formula.functions;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * Calculates the number of days between two dates based on a 360-day year
 * (twelve 30-day months), which is used in some accounting calculations. Use
 * this function to help compute payments if your accounting system is based on
 * twelve 30-day months.
 * 
 * 
 * @author PUdalau
 */
public class Days360 extends NumericFunction.TwoArg {

    @Override
    protected double evaluate(double d0, double d1) throws EvaluationException {
        Calendar startingDate = getStartingDate(d0);
        Calendar endingDate = getEndingDateAccordingToStartingDate(d1, startingDate);
        long startingDay = startingDate.get(Calendar.MONTH) * 30 + startingDate.get(Calendar.DAY_OF_MONTH);
        long endingDay = (endingDate.get(Calendar.YEAR) - startingDate.get(Calendar.YEAR)) * 360
                + endingDate.get(Calendar.MONTH) * 30 + endingDate.get(Calendar.DAY_OF_MONTH);
        return endingDay - startingDay;
    }

    private Calendar getDate(double date) {
        Calendar processedDate = new GregorianCalendar();
        processedDate.setTime(DateUtil.getJavaDate(date, false));
        return processedDate;
    }

    private Calendar getStartingDate(double date) {
        Calendar startingDate = getDate(date);
        if (isLastDayOfMonth(startingDate)) {
            startingDate.set(Calendar.DAY_OF_MONTH, 30);
        }
        return startingDate;
    }

    private Calendar getEndingDateAccordingToStartingDate(double date, Calendar startingDate) {
        Calendar endingDate = getDate(date);
        endingDate.setTime(DateUtil.getJavaDate(date, false));
        if (isLastDayOfMonth(endingDate)) {
            if (startingDate.get(Calendar.DATE) < 30) {
                endingDate = getFirstDayOfNextMonth(endingDate);
            }
        }
        return endingDate;
    }

    private boolean isLastDayOfMonth(Calendar date) {
        Calendar clone = (Calendar) date.clone();
        clone.add(java.util.Calendar.MONTH, 1);
        clone.add(java.util.Calendar.DAY_OF_MONTH, -1);
        int lastDayOfMonth = clone.get(Calendar.DAY_OF_MONTH);
        return date.get(Calendar.DAY_OF_MONTH) == lastDayOfMonth;
    }

    private Calendar getFirstDayOfNextMonth(Calendar date) {
        Calendar newDate = (Calendar) date.clone();
        if (date.get(Calendar.MONTH) < Calendar.DECEMBER) {
            newDate.set(Calendar.MONTH, date.get(Calendar.MONTH) + 1);
        } else {
            newDate.set(Calendar.MONTH, 1);
            newDate.set(Calendar.YEAR, date.get(Calendar.YEAR) + 1);
        }
        newDate.set(Calendar.DATE, 1);
        return newDate;
    }
}
