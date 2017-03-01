/*
 * @author Antonio
 */
package org.opengeoportal.dataingest.api.fileCache;

import org.opengeoportal.dataingest.exception.FileLockedException;
import org.opengeoportal.dataingest.exception.FileNotReadyException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * The Class FileManager.
 */
public class FileManager {

    /** The f. */
    private final File f;

    /** The lock. */
    private final File lock;

    /**
     * Max allowable lock time in seconds 1 hour is recommended for bigger
     * downloads.
     */
    private long maxAllowableLockTime;

    /**
     * Instantiates a new file manager.
     *
     * @param path
     *            the path
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FileNotReadyException
     *             the file not ready exception
     */
    public FileManager(final String path, long aMaxAllowableLockTime)
            throws IOException, FileNotReadyException {
        f = new File(path);
        this.maxAllowableLockTime = aMaxAllowableLockTime;
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
    public FileManager(final String path, final boolean newFile, long aMaxAllowableLockTime)
            throws IOException, FileLockedException {
        f = new File(path);
        this.maxAllowableLockTime = aMaxAllowableLockTime;
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
     * Removes the file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws FileLockedException the file locked exception
     */
    public void removeFile() throws IOException, FileLockedException {
        if (f.exists() && !isLocked()) {
            f.delete();
        } else if (f.exists() && isLocked()) {
            throw new FileLockedException();
        }
    }

    /**
     * Lock the file.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void lock() throws IOException {
        synchronized (lock) {
            if(!lock.exists()) {
                lock.createNewFile();
            } else {
                throw new IOException("Lock already exists");
            }
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
            if(lock.exists()) {
                lock.delete();
            } else {
                throw new IOException("Lock does not exists");
            }
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

                // If some process holds the lock for too much time
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
        final Path path = f.toPath();
        final BasicFileAttributes attr = Files.readAttributes(path,
                BasicFileAttributes.class);

        final long current = System.currentTimeMillis() / 1000;
        final long creationTime = attr.creationTime().toMillis() / 1000;

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
        final Path path = lock.toPath();
        final BasicFileAttributes attr = Files.readAttributes(path,
                BasicFileAttributes.class);

        final long current = System.currentTimeMillis() / 1000;
        final long creationTime = attr.creationTime().toMillis() / 1000;

        return (current - creationTime);
    }

}
