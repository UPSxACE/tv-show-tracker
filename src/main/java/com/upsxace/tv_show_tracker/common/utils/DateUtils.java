package com.upsxace.tv_show_tracker.common.utils;

import java.time.LocalDate;

public class DateUtils {
    public static LocalDate safeDateParse(String dateStr){
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e){
            return null;
        }
    }
}
