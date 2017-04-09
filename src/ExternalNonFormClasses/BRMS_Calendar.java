/*
 /*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002.
 * All rights reserved. Software written by Ian F. Darwin and others.
 * $Id: LICENSE,v 1.8 2004/02/09 03:33:38 ian Exp $
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
 * pioneering role in inventing and promulgating (and standardizing) the Java 
 * language and environment is gratefully acknowledged.
 * 
 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
 * inventing predecessor languages C and C++ is also gratefully acknowledged.
 */

package ExternalNonFormClasses;

import brms_v2.Main;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;


public class BRMS_Calendar {

    /**
     * A Calendar object used throughout
     */
    Calendar calendar = new GregorianCalendar();
    /**
     * The currently-interesting year (not modulo 1900!)
     */
    protected int yy;

    /**
     * Currently-interesting month and day
     */
    protected int mm,

    /**
     * Currently-interesting month and day
     */

    /**
     * Currently-interesting month and day
     */
    dd;
    /**
     * The number of day squares to leave blank at the start of this month
     */
    protected int leadGap = 0;

    /**
     * Today's year
     */
    protected final int thisYear = calendar.get(Calendar.YEAR);

    /**
     * Today's month
     */
    protected final int thisMonth = calendar.get(Calendar.MONTH);

    private final JButton days[];
    private final JComboBox monthChoice, yearChoice;
    private final JPanel panel;
    
    private String daySelected;
    
    Main nm;

    public BRMS_Calendar(Main nm, JPanel jP, JButton newJ[], JComboBox jC1, JComboBox jC2) {
        this.days = newJ;
        this.monthChoice = jC1;
        this.yearChoice = jC2;
        this.panel = jP;
        this.nm = nm;
        setYYMMDD(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        nm.getCalendarDaySelected(String.valueOf(dd));
        buildGUI();
        recompute();
    }

    private void setYYMMDD(int year, int month, int today) {
        yy = year;
        mm = month;
        dd = today;
    }
    
    private void dateSet(String i){
        daySelected = i;
        System.out.println("Day: "+daySelected);
    }
    
    public String getDaySelected(){
        return daySelected;
    }
    
    public String getMonthSelected(){
        return months[mm];
    }
    
    public String getYearSelected(){
        return String.valueOf(yy);
    }

    String[] months = {"January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"};

    private void buildGUI() {

        for (String month : months) {
            monthChoice.addItem(month);
        }
        monthChoice.setSelectedItem(months[mm]);
        monthChoice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int i = monthChoice.getSelectedIndex();
                if (i >= 0) {
                    mm = i;
                    System.out.println("Month=" + mm);
                    recompute();
                }
            }
        });

        monthChoice.getAccessibleContext().setAccessibleName("Months");
        monthChoice.getAccessibleContext().setAccessibleDescription(
                "Choose a month of the year");

        yearChoice.setEditable(true);
        for (int i = yy - 10; i < yy + 10; i++) {
            yearChoice.addItem(Integer.toString(i));
        }
        yearChoice.setSelectedItem(Integer.toString(yy));
        yearChoice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int i = yearChoice.getSelectedIndex();
                if (i >= 0) {
                    yy = Integer.parseInt(yearChoice.getSelectedItem()
                            .toString());
                    // System.out.println("Year=" + yy);
                    recompute();
                }
            }
        });

        ActionListener dateSetter = new ActionListener() { //SELECT A DAY
            @Override
            public void actionPerformed(ActionEvent e) {
                String num = e.getActionCommand();
                if (!num.equals("")) {
                    System.out.println("" + num);
                    // set the current day highlighted
                    setDayActive(Integer.parseInt(num));
                    // When this becomes a Bean, you can
                    // fire some kind of DateChanged event here.
                    // Also, build a similar daySetter for day-of-week btns.
                    nm.getCalendarDaySelected(num);
                }
            }
        };
        

        for (JButton day : days) {
            day.addActionListener(dateSetter);
        }

    }

    public final static int dom[] = {31, 28, 31, 30, /* jan feb mar apr */
        31, 30, 31, 31, /* may jun jul aug */
        30, 31, 30, 31 /* sep oct nov dec */};

    /**
     * Compute which days to put where, in the Cal panel
     */
    private void recompute() {
        // System.out.println("Cal::recompute: " + yy + ":" + mm + ":" + dd);
        if (mm < 0 || mm > 11) {
            throw new IllegalArgumentException("Month " + mm
                    + " bad, must be 0-11");
        }
        clearDayActive();
        calendar = new GregorianCalendar(yy, mm, dd);

        // Compute how much to leave before the first.
        // getDay() returns 0 for Sunday, which is just right.
        leadGap = new GregorianCalendar(yy, mm, 1).get(Calendar.DAY_OF_WEEK) - 1;
        // System.out.println("leadGap = " + leadGap);

        int daysInMonth = dom[mm];
        if (isLeap(calendar.get(Calendar.YEAR)) && mm == 1) //    if (isLeap(calendar.get(Calendar.YEAR)) && mm > 1)
        {
            ++daysInMonth;
        }

        // Blank out the labels before 1st day of month
        for (int i = 0; i < leadGap; i++) {
            days[i].setText("");
            days[i].setEnabled(false);
        }

        int daysWithNum = 0;

        // Fill in numbers for the day of month.
        for (int i = 1; i <= daysInMonth; i++) {
            JButton b = days[daysWithNum =(leadGap + i - 1)];
            b.setText(Integer.toString(i));
            b.setEnabled(true);
        }

        // 7 days/week * up to 6 rows
        for (int i = daysWithNum+1; i < days.length; i++) {
            days[i].setText("");
            days[i].setEnabled(false);
        }

        // Shade current day, only if current month
        if (thisYear == yy && mm == thisMonth) {
            setDayActive(dd); // shade the box for today
        }
        // Say we need to be drawn on the screen
        panel.repaint();
    }

    /**
     * isLeap() returns true if the given year is a Leap Year.
     *
     * "a year is a leap year if it is divisible by 4 but not by 100, except
     * that years divisible by 400 *are* leap years." -- Kernighan & Ritchie,
     * _The C Programming Language_, p 37.
     *
     * @param year
     * @return
     */
    public boolean isLeap(int year) {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
    }

    /**
     * Set the year, month, and day
     *
     * @param yy
     * @param mm
     * @param dd
     */
    public void setDate(int yy, int mm, int dd) {
        // System.out.println("Cal::setDate");
        this.yy = yy;
        this.mm = mm; // starts at 0, like Date
        this.dd = dd;
        recompute();
    }

    /**
     * Unset any previously highlighted day
     */
    private void clearDayActive() {
        JButton b;

        // First un-shade the previously-selected square, if any
        if (activeDay > 0) {
            b = days[(leadGap + activeDay - 1)];
            b.setBackground(new JButton().getBackground());
            b.repaint();
            activeDay = -1;
        }
    }

    private int activeDay = -1;

    /**
     * Set just the day, on the current month
     *
     * @param newDay
     */
    public void setDayActive(int newDay) {

        clearDayActive();

        // Set the new one
        if (newDay <= 0) {
            dd = new GregorianCalendar().get(Calendar.DAY_OF_MONTH);
        } else {
            dd = newDay;
        }
        // Now shade the correct square
        Component square = days[(leadGap + newDay - 1)];
        square.setBackground(Color.red);
        square.repaint();
        activeDay = newDay;
    }
}
