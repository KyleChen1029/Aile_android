package tw.com.chainsea.ce.sdk.service.type;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * current by evan on 2020-03-13
 * 刷新來源
 */
public enum RefreshSource {
    ALL,
    LOCAL,
    REMOTE,
    ROBOT;


    public static Set<RefreshSource> ALL_or_LOCAL = Sets.newHashSet(ALL, LOCAL);
    public static Set<RefreshSource> ALL_or_REMOTE = Sets.newHashSet(ALL, REMOTE);

}
