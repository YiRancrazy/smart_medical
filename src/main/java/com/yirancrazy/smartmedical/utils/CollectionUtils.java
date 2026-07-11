package com.yirancrazy.smartmedical.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * 集合工具
 * <p>
 * 替换项目内散落的 {@code list.stream().filter(...).findFirst().orElse(null)}
 * 与 {@code list.stream().collect(Collectors.toMap(...))} 内联调用。
 *
 * @Author: YiRanCrazy@gmail.com
 * @Description: 集合工具
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */
public final class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * 按 key 查找集合中第一个匹配元素
     *
     * @param collection 源集合
     * @param key        待匹配 key
     * @param keyFn      元素 → key 的映射
     * @return 首个匹配元素；未命中返回 {@code null}
     */
    public static <T, K> T findOne(Collection<T> collection, K key, Function<T, K> keyFn) {
        if (collection == null || collection.isEmpty() || key == null) {
            return null;
        }
        for (T item : collection) {
            if (Objects.equals(key, keyFn.apply(item))) {
                return item;
            }
        }
        return null;
    }

    /**
     * 按 keyFn 将集合索引为 Map
     *
     * @param collection 源集合
     * @param keyFn      元素 → key 的映射
     * @return key → element 的索引；空集合返回空 Map
     */
    public static <T, K> Map<K, T> indexBy(Collection<T> collection, Function<T, K> keyFn) {
        if (collection == null || collection.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<K, T> index = new HashMap<>(collection.size());
        for (T item : collection) {
            K key = keyFn.apply(item);
            if (key != null) {
                index.put(key, item);
            }
        }
        return index;
    }
}