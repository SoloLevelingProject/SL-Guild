package com.banghoi.util;

import java.util.*;

public class HashMapUtil {

    public static List<String> sortFromGreatestToLowestI(HashMap<String, Integer> hashMap) {
        if (hashMap == null)
            return new ArrayList<>();
        List<String> list = new ArrayList<>();
        hashMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> list.add(entry.getKey()));
        return list;
    }

    public static List<String> sortFromGreatestToLowestL(HashMap<String, Long> hashMap) {
        if (hashMap == null)
            return new ArrayList<>();
        List<String> list = new ArrayList<>();
        hashMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> list.add(entry.getKey()));
        return list;
    }

    public static List<String> sortFromLowestToGreatestI(HashMap<String, Integer> hashMap) {
        if (hashMap == null)
            return new ArrayList<>();
        List<String> list = new ArrayList<>();
        hashMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> list.add(entry.getKey()));
        return list;
    }

    public static List<String> sortFromLowestToGreatestL(HashMap<String, Long> hashMap) {
        if (hashMap == null)
            return new ArrayList<>();
        List<String> list = new ArrayList<>();
        hashMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> list.add(entry.getKey()));
        return list;
    }

    public static List<String> sortFromGreatestToLowestD(HashMap<String, Double> hashMap) {
        if (hashMap == null)
            return new ArrayList<>();
        List<String> list = new ArrayList<>();
        hashMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> list.add(entry.getKey()));
        return list;
    }

}
