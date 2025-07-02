package tech.cspioneer.backend.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class CopyTools {
    public static <T, S> List<T> copyList(List<S> sList, Class<T> classz) {
        if (sList == null) {
            throw new IllegalArgumentException("sList must not be null");
        }
        if (classz == null) {
            throw new IllegalArgumentException("classz must not be null");
        }
        List<T> list = new ArrayList<T>();
        for (S s : sList) {
            T t = null;
            try {
                t = classz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            BeanUtils.copyProperties(s, t);
            list.add(t);
        }
        return list;
    }

    public static <T, S> T copy(S s, Class<T> classz) {
        if (s == null) {
            throw new IllegalArgumentException("s must not be null");
        }
        if (classz == null) {
            throw new IllegalArgumentException("classz must not be null");
        }
        T t = null;
        try {
            t = classz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BeanUtils.copyProperties(s, t);
        return t;
    }
}