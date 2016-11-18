/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.groupon.seleniumgridextras.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author JamesFraser
 */
public final class QueueDataHolder {

    private long total_values = 0;
    private int over_target = 0;
    private int under_target = 0;
    private double average = 0.0;

    private final int TARGET = 60000;

    private boolean changed = false;

    public QueueDataHolder() {
    }

    public void addValue(int value) {
        total_values += value;
        if (value < TARGET) {
            under_target++;
        } else {
            over_target++;
        }

        changed = true;
    }

    public QueueDataHolder(int total, int over, int under) {
        total_values = total;
        over_target = over;
        under_target = under;

        changed = true;
        getAverage();
    }

    public long getTotal() {
        return total_values;
    }

    public int getOver() {
        return over_target;
    }

    public int getUnder() {
        return under_target;
    }

    public double getAverage() {
        if (over_target + under_target == 0) {
            return 0;
        }

        if (changed) {
            average = ((double) total_values) / ((double) (over_target + under_target));
            average = round(average/1000, 2);
            changed = false;
        }

        return average;
    }

    public double getTargetPercentage() {
        if (total_values == 0) {
            return 100;
        }

        double value = (100 * ((double) under_target) / ((double) (over_target + under_target)));
        return round(value, 2);
    }

    public void merge(QueueDataHolder anotherHolder) {
        total_values += anotherHolder.getTotal();
        over_target += anotherHolder.getOver();
        under_target += anotherHolder.getUnder();

        changed = true;
    }

    private static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
