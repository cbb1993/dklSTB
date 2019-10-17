package com.huanhong.decathlonstb.util;

public class StoreAmout {
    public int year_count;
    public int year_count_score;
    public int to_days_count_score;
    public int to_days_count;

    @Override
    public String toString() {
        return "yc: " + year_count + " ycs: " + year_count_score + " dc: " + to_days_count + " dcs: " + to_days_count_score;
    }
}