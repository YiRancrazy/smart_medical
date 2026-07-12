package com.yirancrazy.smartmedical.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CollectionUtils 单测
 */
class CollectionUtilsTest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Item {
        private Long id;
        private String name;
    }

    @Test
    void findOne_returnsMatch() {
        List<Item> items = Arrays.asList(
                new Item(1L, "a"),
                new Item(2L, "b"),
                new Item(3L, "c")
        );
        Item hit = CollectionUtils.findOne(items, 2L, Item::getId);
        assertNotNull(hit);
        assertEquals("b", hit.getName());
    }

    @Test
    void findOne_returnsNullWhenMiss() {
        List<Item> items = Arrays.asList(new Item(1L, "a"));
        Item hit = CollectionUtils.findOne(items, 99L, Item::getId);
        assertNull(hit);
    }

    @Test
    void findOne_returnsNullOnEmptyCollection() {
        Item hit = CollectionUtils.findOne(Collections.emptyList(), 1L, Item::getId);
        assertNull(hit);
    }

    @Test
    void findOne_returnsNullOnNullCollection() {
        Item hit = CollectionUtils.findOne(null, 1L, Item::getId);
        assertNull(hit);
    }

    @Test
    void findOne_returnsNullOnNullKey() {
        List<Item> items = Arrays.asList(new Item(1L, "a"));
        Item hit = CollectionUtils.findOne(items, null, Item::getId);
        assertNull(hit);
    }

    @Test
    void findOne_returnsFirstMatch() {
        List<Item> items = Arrays.asList(
                new Item(1L, "first"),
                new Item(1L, "second")
        );
        Item hit = CollectionUtils.findOne(items, 1L, Item::getId);
        assertNotNull(hit);
        assertEquals("first", hit.getName());
    }

    @Test
    void indexBy_buildsMap() {
        List<Item> items = Arrays.asList(
                new Item(1L, "a"),
                new Item(2L, "b"),
                new Item(3L, "c")
        );
        Map<Long, Item> index = CollectionUtils.indexBy(items, Item::getId);
        assertEquals(3, index.size());
        assertEquals("b", index.get(2L).getName());
    }

    @Test
    void indexBy_skipsNullKey() {
        List<Item> items = Arrays.asList(
                new Item(1L, "a"),
                new Item(null, "skip-me"),
                new Item(3L, "c")
        );
        Map<Long, Item> index = CollectionUtils.indexBy(items, Item::getId);
        assertEquals(2, index.size());
        assertTrue(index.containsKey(1L));
        assertTrue(index.containsKey(3L));
    }

    @Test
    void indexBy_emptyCollectionReturnsEmptyMap() {
        Map<Long, Item> index = CollectionUtils.indexBy(Collections.emptyList(), Item::getId);
        assertNotNull(index);
        assertTrue(index.isEmpty());
    }

    @Test
    void indexBy_nullCollectionReturnsEmptyMap() {
        Map<Long, Item> index = CollectionUtils.indexBy(null, Item::getId);
        assertNotNull(index);
        assertTrue(index.isEmpty());
    }

    @Test
    void indexBy_lastValueWinsForDuplicateKeys() {
        List<Item> items = Arrays.asList(
                new Item(1L, "first"),
                new Item(1L, "second")
        );
        Map<Long, Item> index = CollectionUtils.indexBy(items, Item::getId);
        assertEquals(1, index.size());
        assertSame(items.get(1), index.get(1L));
    }
}