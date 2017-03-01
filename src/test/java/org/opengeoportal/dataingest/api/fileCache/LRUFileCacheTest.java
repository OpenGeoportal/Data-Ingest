package org.opengeoportal.dataingest.api.fileCache;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by joana on 01/03/17.
 */
public class LRUFileCacheTest {

    private long MIN = 11;
    private long MAX = 1000;
    // From docker container names :-)
    private List<String> names = Arrays.asList(
        "admiring",
        "adoring",
        "affectionate",
        "agitated",
        "amazing",
        "angry",
        "awesome",
        "blissful",
        "boring",
        "brave",
        "clever",
        "cocky",
        "compassionate",
        "competent",
        "condescending",
        "confident",
        "cranky",
        "dazzling",
        "determined",
        "distracted",
        "dreamy",
        "eager",
        "ecstatic",
        "elastic",
        "elated",
        "elegant",
        "eloquent",
        "epic",
        "fervent",
        "festive",
        "flamboyant",
        "focused",
        "friendly",
        "frosty",
        "gallant",
        "gifted",
        "goofy",
        "gracious",
        "happy",
        "hardcore",
        "heuristic",
        "hopeful",
        "hungry",
        "infallible",
        "inspiring",
        "jolly",
        "jovial",
        "keen",
        "kind",
        "laughing",
        "loving",
        "lucid",
        "mystifying",
        "modest",
        "musing",
        "naughty",
        "nervous",
        "nifty",
        "nostalgic",
        "objective",
        "optimistic",
        "peaceful",
        "pedantic",
        "pensive",
        "practical",
        "priceless",
        "quirky",
        "quizzical",
        "relaxed",
        "reverent",
        "romantic",
        "sad",
        "serene",
        "sharp",
        "silly",
        "sleepy",
        "stoic",
        "stupefied",
        "suspicious",
        "tender",
        "thirsty",
        "trusting",
        "unruffled",
        "upbeat",
        "vibrant",
        "vigilant",
        "vigorous",
        "wizardly",
        "wonderful",
        "xenodochial",
        "youthful",
        "zealous",
        "zen"
    );

    private LRUFileCache fileCache;
    private long CAPACITY = 300000;
    private int ITERATIONS = 50;

    private HashMap<String, Node> initMap() {
        HashMap<String, Node> map = new HashMap<String, Node>();
        names.stream().forEach(key -> map.put(key, new Node(key, MIN + (int) (Math.random() * MAX))));
        return map;
    }

    private LRUFileCache initCache(LRUFileCache cache) {
        names.stream().forEach(key -> {
            try {
                cache.set(key, MIN + (int) (Math.random() * MAX));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return cache;
    }

    @Test
    public void set_get() throws Exception {

        fileCache = new LRUFileCache();
        fileCache.setCapacity(CAPACITY);

        int acc = 0;
        for (int i = 0; i < ITERATIONS; ++i) {

            int idx = addCacheEntry();
            System.out.println("Inserting " + names.get(idx) + " key");

            if (fileCache.get(names.get(idx)).getKey().equals(names.get(idx))) {
                acc++;
                System.out.println("Getting " + names.get(idx) + ": ok");
            }
        }
        assertTrue(acc == ITERATIONS);
    }


    @Test
    public void setSameKeyMultipleTimes() throws Exception {

        fileCache = new LRUFileCache();
        fileCache.setCapacity(CAPACITY);
        int idx = addCacheEntry();

        int oldSize = fileCache.map.size();
        for (int i = 0; i < ITERATIONS; ++i) {
            fileCache.set(names.get(idx), MIN + (int) (Math.random() * MAX));
            if (fileCache.map.size() == oldSize) {
                System.out.println("Key " + names.get(idx) + " found on the cache");
            }
        }
        assertTrue(fileCache.map.size() == oldSize);
    }

    @Test
    public void evictByCapacity()  {

        fileCache = new LRUFileCache();
        fileCache.setCapacity(500);

        try {
            fileCache.set(names.get(0), 400);
            fileCache.set(names.get(1), 200);

        } catch(Exception ex){ // This exception is throw when it tries to remove a file on disk
            assertTrue(fileCache.map.size() == 1);
        }
    }

    //TODO: test LRU

    @Test
    public void remove() throws Exception {

    }

    private int addCacheEntry() throws Exception {
        int idx = 0 + (int) (Math.random() * names.size());
        fileCache.set(names.get(idx), MIN + (int) (Math.random() * MAX));
        return idx;
    }

}
