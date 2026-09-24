package gitlet;

// TODO: any imports you need here
import static gitlet.Utils.*;
import static gitlet.Repository.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.HashMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *  This class represents a commit.
 *  It should be serializable so that it can be put into the 'commits' folder.
 *  This class is at the core of Gitlet.
 *  @author WEI Xize
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /** The date of this Commit. */
    private Date date;
    /**
     * The parent Commits of this Commit.
     * Needs to set the transient fields to appropriate values when deserialized
     */
    private transient Commit parent1;
    private transient Commit parent2;
    /** The parent Commits' UID of this commit. */
    private String parentsUID1;
    private String parentsUID2;
    /** The contents of this commit, represented by a map. */
    private HashMap<File, String> trackedFiles;



    /* TODO: fill in the rest of this class. */
    /**
     * Initial commit
     * Invoked by Repository.init()
     */
    public Commit() {
        message = "initial commit";
        date = new Date(0);
        parent1 = null;
        parent2 = null;
        parentsUID1 = null;
        parentsUID2 = null;
        trackedFiles = new HashMap<>();
    }

    /**
     * Serialize itself to the target place.
     * @throws IOException
     */
    public void saveCommit() throws IOException {
        File commitFile = join(COMMITS_DIR, sha1(serialize(this)));
        commitFile.createNewFile();
        writeObject(commitFile, this);
    }

    /**
     * An interface.
     * @return trackedFiles.
     */
    public HashMap<File, String> getTrackedFiles() {
        return trackedFiles;
    }

    /**
     * Constructor for not-initial commits.
     * @param msg message
     * @param parentsUID1 UID of parent commit I.
     * @param trackedFiles non-metadata.
     */
    public Commit(String msg, String parentsUID1, HashMap<File, String> trackedFiles) {
        message = msg;
        date = new Date();
        parent1 = null;
        parent2 = null;
        this.parentsUID1 = parentsUID1;
        parentsUID2 = null;
        this.trackedFiles = trackedFiles;
    }
}
