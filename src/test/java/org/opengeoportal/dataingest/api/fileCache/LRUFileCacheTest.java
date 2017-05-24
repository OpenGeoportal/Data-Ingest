package org.opengeoportal.dataingest.api.fileCache;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.opengeoportal.dataingest.utils.GeoServerUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by joana on 01/03/17.
 */
public class LRUFileCacheTest {

    /**
     * Mockup cache capacity.
     */
    private long CAPACITY = 300000;
    /**
     * Mockup cache name.
     */
    private String CACHENAME = "testcache";
    /**
     * Mockup cache path.
     */
    private String PATH = "";
    private long MIN = 11;
    private long MAX = 1000;
    private int ITERATIONS = 50;

    /**
     * Mockup dataset names: from docker container names :-) .
     */
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

    /**
     * Test the correctness of the set and get methods.
     *
     * @throws Exception
     */
    @Test
    public void set_get() throws Exception {

        LRUFileCache fileCache = new LRUFileCache();
        fileCache.createCacheDir(CAPACITY, CACHENAME, PATH);

        int acc = 0;
        for (int i = 0; i < ITERATIONS; ++i) {

            int idx = addCacheEntry(fileCache);
            System.out.println("Inserting " + names.get(idx) + " key");

            if (fileCache.get(names.get(idx)).getKey().equals(names.get(idx))) {
                acc++;
                System.out.println("Getting " + names.get(idx) + ": ok");
            }
        }
        assertTrue(acc == ITERATIONS);
    }

    /**
     * Test if the cache is being correctly used.
     * Test if mutliple insertions of the same key do not add new cache entries.
     *
     * @throws Exception
     */
    @Test
    public void setSameKeyMultipleTimes() throws Exception {

        LRUFileCache fileCache = new LRUFileCache();
        fileCache.createCacheDir(CAPACITY, CACHENAME, PATH);

        int idx = addCacheEntry(fileCache);

        int oldSize = fileCache.map.size();
        for (int i = 0; i < ITERATIONS; ++i) {
            fileCache.set(names.get(idx), MIN + (int) (Math.random() * MAX));
            if (fileCache.map.size() == oldSize) {
                System.out.println("Key " + names.get(idx) + " found on the cache");
            }
        }
        assertTrue(fileCache.map.size() == oldSize);
    }

    /**
     * Test if eviction is triggered when the cache limit was reached.
     * We add two files to the cache, the second one above the cache capacity.
     * We test that the eviction was performed, by testing if there is only one item in the cache, and that the item
     * is the last to be inserted.
     *
     * @throws Exception
     */
    @Test
    public void evictByCapacity() throws Exception {

        LRUFileCache fileCache = new LRUFileCache();
        fileCache.createCacheDir(500, CACHENAME, PATH);

        String key0 = names.get(0);
        String key1 = names.get(1);

        fileCache.set(key0, 400);
        if (!mockupFile(key0)) throw new java.io.EOFException("Could not mockup file " + key0);
        fileCache.set(key1, 200);

        assertTrue(fileCache.map.size() == 1 && fileCache.get(names.get(1)) != null);
    }

    /**
     * Tests the correctness of the LRU algorithm.
     * We add 5 entries, just bellow the cache capacity.
     * We use all entries, except one. We add another entry, which triggers the LRU eviction.
     * We test that the non-used entry was removed from the cache.
     *
     * @throws Exception
     */
    @Test
    public void LRU() throws Exception {

        LRUFileCache fileCache = new LRUFileCache();
        fileCache.createCacheDir(510, CACHENAME, PATH);

        int lru = 0 + (int) (Math.random() * 5);

        // Setup
        for (int i = 0; i <= 5; i++) {
            fileCache.set(names.get(i), 100);
            if (!mockupFile(names.get(i))) throw new java.io.EOFException("Could not mockup file " + names.get(i));
        }

        // Use all values but lru
        for (int i = 0; i <= 5; ++i) if (i != lru) fileCache.get(names.get(i));

        fileCache.set(names.get(6), 100);

        assertTrue(fileCache.get(names.get(lru)) == null);
    }

    /**
     * Tests the remove() method on the LRUFileCache.
     * We add an entry to the cache, remove it and check if the cache is empty.
     *
     * @throws Exception
     */
    @Test
    public void remove() throws Exception {

        // Setup
        LRUFileCache fileCache = new LRUFileCache();
        fileCache.createCacheDir(CAPACITY, CACHENAME, PATH);
        int idx = addCacheEntry(fileCache);
        String key = names.get(idx);
        if (!mockupFile(key)) throw new java.io.EOFException("Could not mockup file " + key);

        fileCache.remove(fileCache.map.get(key));

        assertTrue(fileCache.map.size() == 0 && fileCache.get(key) == null);
    }

    /**
     * After each test, empty the cache dir, in order to avoid conflicts with filenames.
     */
    @After
    public void cleanup() throws IOException {
        final File dir = new File(FileNameUtils.getCachePath(PATH, CACHENAME));
        FileUtils.deleteDirectory(dir);
    }

    /**
     * Mockup a file on disk, so we can use it in the filecache.
     *
     * @param key dataset name.
     * @return true, if succeeded; zero if it fails.
     * @throws IOException
     */
    private boolean mockupFile(String key) throws IOException {
        String workspace = GeoServerUtils.getWorkspace(key);
        String dataset = GeoServerUtils.getDataset(key);
        String fileName = FileNameUtils.getFullPathZipFile(
            FileNameUtils.getCachePath(PATH, CACHENAME),
            dataset);
        File f = new File(fileName);
        return f.createNewFile();
    }

    /**
     * Adds a cache entry, with some randomness in it.
     *
     * @param fileCache a file cache
     * @return the index of the new entry on the names array.
     * @throws Exception
     */
    private int addCacheEntry(FileCache fileCache) throws Exception {
        int idx = 0 + (int) (Math.random() * names.size());
        fileCache.set(names.get(idx), MIN + (int) (Math.random() * MAX));
        return idx;
    }

    /**
     * Init a file cache with some randmoness in it
     *
     * @param cache a filecache
     * @return a filecache
     */
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


}
