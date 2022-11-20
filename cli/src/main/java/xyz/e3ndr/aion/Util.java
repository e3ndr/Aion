package xyz.e3ndr.aion;

import java.util.LinkedList;
import java.util.List;

public class Util {

    @SafeVarargs
    public static <T> List<T> concat(List<T>... lists) {
        List<T> list = new LinkedList<>();

        for (List<T> l : lists) {
            list.addAll(l);
        }

        return list;
    }

}