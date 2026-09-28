package com.ctg.innovic.cmmpdi.dirsync.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListCompare {

    public static boolean equalsIgnoreOrder(List<String> list1, List<String> list2) {
        if (list1 == list2) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;

        List<String> sorted1 = new ArrayList<>(list1);
        List<String> sorted2 = new ArrayList<>(list2);

        Collections.sort(sorted1);
        Collections.sort(sorted2);

        return sorted1.equals(sorted2);
    }
}
