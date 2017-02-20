package org.opengeoportal.dataingest.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import org.opengeoportal.dataingest.exception.FileLockedException;
import org.opengeoportal.dataingest.exception.FileNotReadyException;

/**
 * The Class FileManager.
 */
public class FileManager {

    /** The f. */
    private File f;

    /** The lock. */
    private File lock;

    /**
     * Max allowable lock time in seconds 1 hour is recommended for bigger
     * downloads.
     */
    private static long maxAllowableLockTime = 3600;

    /**
     * Instantiates a new file manager.
     *
     * @param path            the path
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FileNotReadyException the file not ready exception
     */
    public FileManager(String path) throws IOException, FileNotReadyException {
        f = new File(path);
        lock = new File(path + ".lock");
        if (!f.exists() || isLocked()) {
            throw new FileNotReadyException();
        }
    }

    /**
     * Instantiates a new file manager.
     *
     * @param path
     *            the path
     * @param newFile
     *            the new file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FileLockedException
     *             the file locked exception
     */
    public FileManager(String path, boolean newFile)
            throws IOException, FileLockedException {
        f = new File(path);
        lock = new File(path + ".lock");
        if (newFile) {
            if (f.exists() && !isLocked()) {
                f.delete();
            } else if (f.exists() && isLocked()) {
                throw new FileLockedException();
            }
            f.createNewFile();
        }
    }

    /**
     * Gets the file.
     *
     * @return the file
     */
    public File getFile() {
        return f;
    }

    /**
     * Lock the file.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void lock() throws IOException {
        synchronized (lock) {
            lock.createNewFile();
        }
    }

    /**
     * Unlock the file.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void unlock() throws IOException {
        synchronized (lock) {
            lock.delete();
        }
    }

    /**
     * Checks if is locked.
     *
     * @return true, if is locked
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public boolean isLocked() throws IOException {
        synchronized (lock) {
            if (lock.exists()) {

                // If some process holds the lock for too many time
                if (getLockAgeinSeconds() > maxAllowableLockTime) {
                    // release the lock
                    lock.delete();
                    return false;
                }

                return true;
            }

            return false;
        }

    }

    /**
     * Gets the file agein seconds.
     *
     * @return the file agein seconds
     * @throws NumberFormatException
     *             the number format exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public long getFileAgeinSeconds()
            throws NumberFormatException, IOException {
        Path path = f.toPath();
        BasicFileAttributes attr = Files.readAttributes(path,
                BasicFileAttributes.class);

        long current = System.currentTimeMillis() / 1000;
        long creationTime = attr.creationTime().toMillis() / 1000;

        return (current - creationTime);

    }

    /**
     * Gets the lock agein seconds.
     *
     * @return the lock agein seconds
     * @throws NumberFormatException
     *             the number format exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private long getLockAgeinSeconds()
            throws NumberFormatException, IOException {
        Path path = lock.toPath();
        BasicFileAttributes attr = Files.readAttributes(path,
                BasicFileAttributes.class);

        long current = System.currentTimeMillis() / 1000;
        long creationTime = attr.creationTime().toMillis() / 1000;

        return (current - creationTime);
    }

}
