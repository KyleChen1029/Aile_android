package tw.com.chainsea.android.common.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * current by evan on 2020-04-01
 *
 * @author Evan Wang
 * @date 2020-04-01
 */
public class CollectionHelper {

    private static final Integer INTEGER_ONE = 1;

    public static boolean isEqualCollection(Collection a, Collection b) {
        if (a.size() != b.size()) {  // size Is the simplest equality condition
            return false;
        }
        Map mapa = getCardinalityMap(a);
        Map mapb = getCardinalityMap(b);

        // After the map is converted, duplicates can be removed. At this time, size is a non-duplicate item, which is also a prerequisite
        if (mapa.size() != mapb.size()) {
            return false;
        }
        for (Object obj : mapa.keySet()) {
            // To query the same obj, first both sides must be present, and the number of repetitions must be verified, which is map.value
            if (getFreq(obj, mapa) != getFreq(obj, mapb)) {
                return false;
            }
        }
        return true;
    }

    public static Map getCardinalityMap(Collection coll) {
        Map count = new HashMap();
        for (Iterator it = coll.iterator(); it.hasNext(); ) {
            Object obj = it.next();
            Integer c = (Integer) count.get(obj);
            if (c == null)
                count.put(obj, INTEGER_ONE);
            else {
                count.put(obj, Integer.valueOf(c.intValue() + 1));
            }
        }
        return count;
    }

    private static int getFreq(Object obj, Map freqMap) {
        Integer count = (Integer) freqMap.get(obj);
        if (count != null) {
            return count.intValue();
        }
        return 0;
    }

    public static <T> T findByElement(TreeSet<T> treeset, T key) {
        T ceil = treeset.ceiling(key); // least elt >= key
        T floor = treeset.floor(key);   // highest elt <= key
        return ceil == floor ? ceil : null;
    }
}
