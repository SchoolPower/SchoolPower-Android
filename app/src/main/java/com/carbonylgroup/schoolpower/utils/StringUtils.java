

package com.carbonylgroup.schoolpower.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * StringUtil
 * Created by ThirtyDegreesRay on 2016/7/14 16:18
 */
public class StringUtils {

    public static boolean isBlank(@Nullable String str) {
        return str == null || str.trim().equals("");
    }

    public static boolean isBlankList(@Nullable List list) {
        return list == null || list.size() == 0;
    }

    public static List<String> stringToList(@NonNull String str, @NonNull String separator){
        List<String> list = null;
        if(!str.contains(separator)){
            return list;
        }
        String[] strs = str.split(separator);
        list = Arrays.asList(strs);
        return list;
    }

    public static String listToString(@NonNull List<String> list, @NonNull String separator){
        StringBuilder stringBuilder = new StringBuilder("");
        if(list.size() == 0 || isBlank(separator)){
            return stringBuilder.toString();
        }
        for(int i = 0; i < list.size(); i++){
            stringBuilder.append(list.get(i));
            if(i != list.size() - 1){
                stringBuilder.append(separator);
            }
        }
        return stringBuilder.toString();
    }

    public static String getSizeString(long size){
        if(size < 1024){
            return String.format(Locale.getDefault(), "%d B", size);
        }else if(size < 1024 * 1024){
            float sizeK = size / 1024f;
            return String.format(Locale.getDefault(), "%.2f KB", sizeK);
        }else if(size < 1024 * 1024 * 1024){
            float sizeM = size / (1024f * 1024f);
            return String.format(Locale.getDefault(), "%.2f MB", sizeM);
        }
        return null;
    }

    public static String upCaseFirstChar(String str){
        if(isBlank(str)) return null;
        return str.substring(0, 1).toUpperCase().concat(str.substring(1));
    }

    @NonNull
    public static Date getDateByTime(@NonNull Date time){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    @NonNull
    public static Date getTodayDate(){
        return getDateByTime(new Date());
    }

}
