package ca.weblite.mvnconfig.util;

import org.json.JSONArray;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class JSONUtils {
    public static int indexOf(JSONArray haystack, Object needle) {
        int len = haystack.length();
        for (int i=0; i<len; i++) {
            if (Objects.equals(haystack.get(i), needle)) {
                return i;
            }
        }
        return -1;
    }

    public static <T> int indexOf(JSONArray haystack, Class<T> elementType, Predicate<T> predicate) {
        int len = haystack.length();
        for (int i=0; i<len; i++) {
            if (predicate.test((T)haystack.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static <T> List<T> toList(JSONArray array, List<T> outList) {
        outList.clear();
        int len = array.length();
        for (int i=0; i<len; i++) {
            outList.add((T)array.get(i));
        }
        return outList;
    }


}
