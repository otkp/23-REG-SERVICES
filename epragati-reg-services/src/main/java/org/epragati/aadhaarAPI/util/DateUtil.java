package org.epragati.aadhaarAPI.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
/**
 * this util class only use Date Related
 * @author naga.pulaparthi 
 */

public class DateUtil {
	
    public final static String DATE_PATTERN = "dd MMM yyyy HH:mm:ss";
    
    
    public final static String DATE_PATTERN_MISSEC = "MM/dd/yyyy hh:mm:ss.SSS a";
    public final static String DATE_PATTERN_3 ="yyyy-MM-dd hh:mm:ss";
    public final static String DATE_PATTERN_4 ="yyyyMMdd";
    //2018/11/02 17:50:6.367 crt format  for aadhaar 
    public final static String CRT_DATE_PATTERN="yyyy/MM/dd hh:mm:ss.SSS";
  

    public final static DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);

    /**
     * this is convert date in long type </br>
     * 
     * @param date
     * @return date in long
     */
    public static Long getDateInLong(Date date) {

        return date.getTime();
    }

    /**
     * convert from Current Date to UTC TimeStamp </br>
     * 
     * @return TimeStamp
     */
    public static Long toCurrentUTCTimeStamp() {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        return calendar.getTime().getTime() / 1000;
    }

    /**
     * convert from Date to UTC TimeStamp </br>
     * 
     * @param date
     * @return TimeStamp
     */
    public static Long toUTCTimeStamp(Date date) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(date.getTime());
        return calendar.getTime().getTime();
    }

    /**
     * convert from UTC TimeStamp to IST Date </br>
     * 
     * @param Long utcTimeStamp
     * @return Date
     */
    public static Date fromUTCTimeStamp(Long utcTimeStamp) {
        TimeZone timeZone = TimeZone.getTimeZone("IST");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(utcTimeStamp);
        return calendar.getTime();
    }

    public static String extractDateAsString(Long timestamp) {
        Date date = new Date(timestamp * 1000);
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    public static String extractDateAsStringWithHyphen(Long timestamp) {
        Date date = new Date(timestamp * 1000);
        return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }

    /**
     * to get date as String format dd-MM-yyyy
     * 
     * @param dateTime
     * @return
     */
    public static String getDateAsString(Long dateTime) {
        Date date = new Date(dateTime * 1000);
        return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }

    public static String extractTimeAsString(Long timestamp) {
        Date date = new Date(timestamp * 1000);
        return new SimpleDateFormat("hh:mm:ss a").format(date);
    }

    public static Long addYears(Long date, int year) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(date * 1000);
        calendar.add(Calendar.YEAR, year);
        Long addyeardate = calendar.getTime().getTime() / 1000;
        return reduceDays(addyeardate, 1);
    }

    public static Long addMonths(Long date, int month) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(date * 1000);
        calendar.add(Calendar.MONTH, month - 1);
        Long addMonthDate = calendar.getTime().getTime() / 1000;
        return reduceDays(addMonthDate, 1);
    }
    
    public static Long addMonthsNew(Long date, int month) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(date * 1000);
        calendar.add(Calendar.MONTH, month);
        Long addMonthDate = calendar.getTime().getTime() / 1000;
        return reduceDays(addMonthDate, 1);
    }
    
    public static void main(String[] args) {
        System.out.println(getFitnessExpiryDate(new Date(1463291655000L)));
//        System.out.println(createDate(6, 5, 2017));
        
    }

    public static Long reduceDays(Long date, int days) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis((date) - (days * 24 * 60 * 60));
        return calendar.getTime().getTime();
    }

    public static Long addDays(Long date, int days) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis((date) + (days * 24 * 60 * 60));
        return calendar.getTime().getTime();
    }

    public static Long dateFormater(String inputeDate) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        try {
            date = df.parse(inputeDate);
        } catch (ParseException e) {
            return null;
        }
        return date.getTime() / 1000;
    }
    
    public static Long dateSeconds(Long inputeDate) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = df.format(inputeDate);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date;
        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
        return date.getTime() / 1000;
    }

    public static Long getDateInLong(String inputDate, String time) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date();
        try {
            date = df.parse(inputDate + " " + time);
        } catch (ParseException e) {
            return null;
        }
        return date.getTime() / 1000;
    }

    /**
     * To Get Long Date Input with Indian standard format
     * 
     * @param inputeDate
     * @param time
     * @return
     */
    public static Long getLongDate(String inputeDate, String time) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Date date = new Date();
        try {
            date = df.parse(inputeDate + " " + time);
        } catch (ParseException e) {
            return null;
        }
        return date.getTime() / 1000;
    }

    /**
     * isSameOrGreaterDate compare date between two dates
     * 
     * @param currentDateTime The high value
     * @param oldDateTime The low value
     * @return
     */

    public static Boolean isSameOrGreaterDate(Long currentDateTime, Long oldDateTime) {
        Date currentDate = Date.from(Instant.ofEpochSecond(currentDateTime));
        Date oldDate = Date.from(Instant.ofEpochSecond(oldDateTime));
        return currentDate.compareTo(oldDate) >= 0 ? true : false;

    }

    public static Long toLastDayOfMonth(int monthType) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.add(Calendar.MONTH, monthType + 1);
        calendar.set(Calendar.DAY_OF_MONTH, monthType + 1);
        calendar.add(Calendar.DATE, -(monthType + 1));
        return calendar.getTime().getTime() / 1000;
    }

    public static String getYear(Long dateTime) {
        if (Objects.isNull(dateTime))
            return null;
        Date date = new Date(dateTime * 1000);
        return new SimpleDateFormat("yyyy").format(date);
    }

    public static Boolean isPastCalendarDay(Long inputeDate) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(inputeDate * 1000);
        Boolean pastDay = false;
        int dayDiff = c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
        if (dayDiff > 0)
            pastDay = true;
        return pastDay;
    }

    public static Boolean isPastCalendarMonth(Long inputeDate) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(inputeDate * 1000);
        Boolean pastMonth = false;
        int monthDiff = c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
        if (monthDiff > 0)
            pastMonth = true;
        return pastMonth;
    }

    public static int getMonth(Long inputeDate) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(inputeDate * 1000);
        int month = c1.get(Calendar.MONTH);
        return month;
    }

    public static Integer getCurrentAge(String dateOfBirth) throws Exception {
        String dateOfBirthAME = null;
        String[] strIND = dateOfBirth.split("-");
        String[] strAME = dateOfBirth.split("/");
        if (strIND.length == 3) {
            dateOfBirthAME = strIND[2] + "-" + strIND[1] + "-" + strIND[0];
        } else {
            dateOfBirthAME = strAME[2] + "-" + strAME[0] + "-" + strAME[1];
        }
        LocalDate sinceGraduation = LocalDate.parse(dateOfBirthAME);
        LocalDate currentDate = LocalDate.now(ZoneId.of("UTC"));
        Period betweenDates = Period.between(sinceGraduation, currentDate);
        return betweenDates.getYears();
    }

    public static String getDate(String format, String timeZone, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        return sdf.format(date);
    }

    public static Date addYearToDate(Date date, int n) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.YEAR, n);
        return c.getTime();
    }
    
    public static Long convertToRunningTimeStamp(Date date){
        if(Objects.isNull(date)){
            return null;
        }
        Long lDate = date.getTime()/1000;
        return lDate;
    }
    
    /**
     * return diff number of days date2 - date1
     * @param date1
     * @param date2
     * @return
     */
    public static Long getDateDiff(Date date1, Date date2){
        if(Objects.isNull(date1) || Objects.isNull(date2)){
            return null;
        }
        long diff = date2.getTime() - date1.getTime();
        Long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return days;
    }
    
    /**
     * return Date add some months in a particular date </br>
     * 
     * @param date
     * @param months
     * @return
     */
    public static Date currentDate(Date date, int months){

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, months);
        c.add(Calendar.DATE, -1);
   	return c.getTime();
   }
   
    /**
     * convert from Current Date to UTC Date </br>
     * 
     * @return Current Date
     */
    public static Date toCurrentUTCDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return calendar.getTime();
    }
    public static Date convertMMYYYYtoDate(String inputFormat){
    	Date date = new Date();
    	try {
        String str_date = inputFormat;
        DateFormat formatter;
        formatter = new SimpleDateFormat("MM/yyyy");
        date = (Date) formatter.parse(str_date);
        } catch (ParseException e) {
	        System.out.println("Exception :" + e);
	    }
    	return date;
    }
    
    public static boolean compareInBetween(Date from , Date to , Date between){
    	return from.compareTo(between) * between.compareTo(to) > 0; 
    }
    
    public static String getYear(Date date) {
        if (Objects.isNull(date))
            return null;
        return new SimpleDateFormat("yyyy").format(date);
    }
    
    public static Date addDaysToDate(Date date, int n) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, n);
        return c.getTime();
    }
    
    public static Integer extractDate(Date date) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar c = Calendar.getInstance(timeZone);
        c.setTime(date);
        return c.get(Calendar.DATE);
    }
    
    public static Integer extractYear(Date date) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar c = Calendar.getInstance(timeZone);
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }
    
    public static Integer extractMonth(Date date) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar c = Calendar.getInstance(timeZone);
        c.setTime(date);
        return c.get(Calendar.MONTH) + 1;
    }
    
    public static Date createDate(Integer date, Integer month, Integer year) {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        Calendar c = Calendar.getInstance(timeZone);
        c.set(Calendar.DAY_OF_MONTH, date);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.YEAR, year);
        return c.getTime();
    }
    
    public static Date getFitnessExpiryDate(Date issueDate) {
        Integer month = DateUtil.extractMonth(issueDate);
        Integer year;
        Integer date;
        if (month.intValue() == 5) {
            date = DateUtil.extractDate(issueDate);
            if (date.intValue() == 15) {
                date = 14;
                year = DateUtil.extractYear(issueDate) + 1;
            } else if (date.intValue() > 15) {
                date = 15;
                year = DateUtil.extractYear(issueDate) + 1;
            } else {
                date = 15;
                year = DateUtil.extractYear(issueDate);
            }
        } else {
            date = 15;
            if (month.intValue() > 5) {
                year = DateUtil.extractYear(issueDate) + 1;
            } else {
                year = DateUtil.extractYear(issueDate);
            }
        }
        month = 5;
        return DateUtil.createDate(date, month, year);
    }
    
}


