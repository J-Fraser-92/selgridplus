/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.utilities;

import java.util.Calendar;

/**
 *
 * @author JamesFraser
 */
public class TimeFrame {

    public enum Length {

        TODAY,
        YESTERDAY,
        PAST_SEVEN_DAYS,
        LAST_WEEK,
        WEEK_SO_FAR,
        PAST_TWENTY_EIGHT_DAYS,
        QUARTER,
        LAST_QUARTER
    }

    public static int offsetFor(Length length) {
        if (length.equals(Length.YESTERDAY)) {
            return -1;
        } else if (length.equals(Length.LAST_WEEK)) {
            Calendar cal = Calendar.getInstance();
            int days = 0;
            while (cal.get(Calendar.DAY_OF_WEEK) != 1) {
                days--;
                cal.add(Calendar.DATE, -1);
            }
            return days;
        } else if (length.equals(Length.LAST_QUARTER)) {
            return ((-1) - daysFor(Length.QUARTER));
        }
        return 0;
    }

    public static int daysFor(Length length) {
        int days = 1;
        Calendar cal;
        switch (length) {
            case PAST_SEVEN_DAYS:
                days = 7;
                break;
            case LAST_WEEK:
                days = 7;
                break;
            case WEEK_SO_FAR:
                cal = Calendar.getInstance();
                while (cal.get(Calendar.DAY_OF_WEEK) != 2) {
                    days++;
                    cal.add(Calendar.DATE, -1);
                }
                break;
            case PAST_TWENTY_EIGHT_DAYS:
                days = 28;
                break;
            case QUARTER:
                cal = Calendar.getInstance();
                days = cal.get(Calendar.DATE);

                switch (cal.get(Calendar.MONTH)) {
                    case 1:
                        days += 31;
                        break;
                    case 2:
                        days += 59;
                        if ((cal.get(Calendar.YEAR) % 4 == 0)) {
                            days++;
                        }
                        break;
                    case 4:
                        days += 30;
                        break;
                    case 5:
                        days += 61;
                        break;
                    case 7:
                        days += 31;
                        break;
                    case 8:
                        days += 62;
                        break;
                    case 10:
                        days += 31;
                        break;
                    case 11:
                        days += 61;
                        break;
                }
                break;
            case LAST_QUARTER:
                cal = Calendar.getInstance();
                if(cal.get(Calendar.MONTH) < 3) {
                    //Q4
                    //OCT:31, NOV:30, DEC:31
                    days = 92;
                } else if(cal.get(Calendar.MONTH) < 6) {
                    //Q1
                    //JAN:31, FEB:28, MAR:31
                    days = 90;
                    if ((cal.get(Calendar.YEAR) % 4 == 0)) {
                        days++;
                    }
                } else if(cal.get(Calendar.MONTH) < 9) {
                    //Q2
                    //APR:30, MAY:31, JUN:30
                    days = 91;
                } else {
                    //Q3
                    //JUL:31, AUG:31, SEP:30
                    days = 92;
                }
                break;
        }
        return days;
    }
}
