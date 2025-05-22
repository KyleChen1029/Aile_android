package tw.com.chainsea.ce.sdk.bean.parameter;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * current by evan on 2020-03-13
 */
public enum Sort {
    ALL, ASC, DESC;

    public static Set<Sort> ALL_or_ASC = Sets.newHashSet(ALL, ASC);
    public static Set<Sort> ALL_or_DESC = Sets.newHashSet(ALL, DESC);
}
