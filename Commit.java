package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/** Commit class that creates Gitlet commit objects.
 *  @author Raman Varma
 */
public class Commit implements Serializable {

    /** Commit object constructor.
     *  @param parent - The SHA1 ID of the parent of this commit.
     *  @param message - The log message of this commit.
     *  @param timestamp - The timestamp of this commit.
     *  @param trackedFiles - The files tracked by this commit.
     *  @param mergedInParent - The SHA1 ID of the merged-in parent of
     *  this commit.
     *  @param commitSHA1 - The SHA1 ID of this commit. */
    public Commit(String parent, String message, Date timestamp,
                  HashMap<String, String> trackedFiles, String mergedInParent,
                  String commitSHA1) {
        _parent = parent;
        _message = message;
        _timestamp = timestamp;
        _trackedFiles = trackedFiles;
        _mergedInParent = mergedInParent;
        _commitSHA1 = commitSHA1;
    }

    /** @return The parent of this commit. */
    public String getParent() {
        return _parent;
    }

    /** @return The message of this commit. */
    public String getMessage() {
        return _message;
    }

    /** @return The timestamp of this commit. */
    public Date getTimestamp() {
        return _timestamp;
    }

    /** @return The files tracked by this commit. */
    public HashMap<String, String> getTrackedFiles() {
        return _trackedFiles;
    }

    /** @return The merged-in parent of this commit. */
    public String getMergedInParent() {
        return _mergedInParent;
    }

    /** @return The SHA1 ID of this commit. */
    public String getCommitSHA1() {
        return _commitSHA1;
    }

    /** The parent of this commit. */
    private final String _parent;

    /** The message of this commit. */
    private final String _message;

    /** The timestamp of this commit. */
    private final Date _timestamp;

    /** The files tracked by this commit. */
    private final HashMap<String, String> _trackedFiles;

    /** The merged-in parent of this commit. */
    private final String _mergedInParent;

    /** The SHA1 ID of this commit. */
    private String _commitSHA1;
}
