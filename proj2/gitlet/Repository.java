package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *  This class has many functions related to the creation & manipulation of the repo.
 *  And it is also the bridge connecting Main.java and lots of objects in Gitlet.
 *  @author WEI Xize
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The 'commits' directory. */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    /** The 'blobs' directory. Storing the contents of files. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The HEAD file. */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /** The 'branches' directory. */
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");
    /** Staging area. */
    public static final File STAGED_FOR_ADDITIONS = join(GITLET_DIR, "staged_for_Additions");
    /** Removal area. */
    public static final File STAGED_FOR_REMOVAL = join(GITLET_DIR, "staged_for_removal");

    /* TODO: fill in the rest of this class. */

    /**
     * Initialize the Gitlet repo.
     * Invoked by Main.java case "init"
     */
    public static void init() throws IOException {
        /* 1st step of gitlet-design.md. */
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        /* 2nd step of gitlet-design.md. */
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        HEAD.createNewFile();
        BRANCHES_DIR.mkdir();
        STAGED_FOR_ADDITIONS.createNewFile();
        STAGED_FOR_REMOVAL.createNewFile();
        resetStagingArea();

        /* 3rd step of gitlet-design.md. */
        Commit initialCommit = new Commit();
        initialCommit.saveCommit();

        /* 4th step of gitlet-design.md. */
        String commitUID = sha1(serialize(initialCommit));
        createBranch("master", commitUID);
        writeContents(HEAD, "master");
    }

    /**
     * A helper method, create a new branch.
     * @param name the name of the new branch
     * @param commitUID the UID of the commit the branch points at
     * @throws IOException
     */
    private static void createBranch(String name, String commitUID) throws IOException {
        File branchFile = join(BRANCHES_DIR, name);
        branchFile.createNewFile();
        writeContents(branchFile, commitUID);
    }

    /**
     * Stage files, invoked by Main.java.
     * @param fileName the name of the file to be staged
     */
    public static void add(String fileName) throws IOException {
        /* 1st step of gitlet-design.md. */
        checkInitialized();
        File file = join(CWD, fileName);
        if (!file.exists()) {
            message("File does not exist.");
            System.exit(0);
        }

        /* 2nd step of gitlet-design.md. */
        removeSomethingInRemovalArea(file);

        /* 3rd step of gitlet-design.md. */
        HashMap<File, String> currentStagingArea;
        currentStagingArea = readObject(STAGED_FOR_ADDITIONS, HashMap.class);

        /* 4th step of gitlet-design.md. */
        Commit currentCommit = searchCurrentCommit();
        HashMap<File, String> trackedFiles = currentCommit.getTrackedFiles();
        if (Objects.equals(trackedFiles.get(file), sha1(readContents(file)))) {
            removeSomethingInStagingArea(file, currentStagingArea);
            return;
        }

        /* 5th step of gitlet-design.md. */
        addSomethingInStagingArea(file, currentStagingArea);
        createBlob(file);
    }

    /**
     * Used to get the current commit info.
     * @return HEAD Commit object
     */
    private static Commit searchCurrentCommit() {
        return readObject(join(COMMITS_DIR, readContentsAsString(join(BRANCHES_DIR, readContentsAsString(HEAD)))), Commit.class);
    }

    /**
     * I/O helper function.
     * @param file
     * @param currentStagingArea
     */
    private static void removeSomethingInStagingArea(File file, HashMap<File, String> currentStagingArea) {
        if (currentStagingArea.remove(file) != null) {
            writeObject(STAGED_FOR_ADDITIONS, currentStagingArea);
        }
    }

    /**
     * I/O helper function.
     * @param file
     */
    private static void removeSomethingInRemovalArea(File file) {
        HashSet<File> currentRemovalArea = readObject(STAGED_FOR_REMOVAL, HashSet.class);
        if (currentRemovalArea.remove(file)) {
            writeObject(STAGED_FOR_REMOVAL, currentRemovalArea);
        }
    }

    /**
     * I/O helper function.
     * @param file
     * @param currentStagingArea
     * @throws IOException
     */
    private static void addSomethingInStagingArea(File file, HashMap<File, String> currentStagingArea) throws IOException {
        currentStagingArea.put(file, sha1(readContents(file)));
        writeObject(STAGED_FOR_ADDITIONS, currentStagingArea);
    }

    /**
     * Create a blob for FILE if it does not exist.
     * @param file
     * @throws IOException
     */
    private static void createBlob(File file) throws IOException {
        File blob = join(BLOBS_DIR, sha1(readContents(file)));
        if (!blob.exists()) {
            blob.createNewFile();
            writeContents(blob, readContents(file));
        }
    }

    /**
     * Check if .gitlet exists.
     * If not, print out error message.
     * Should be used in front of every command, except for init command.
     */
    private static void checkInitialized() {
        if (!GITLET_DIR.exists()) {
            message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /**
     * Create a commit, invoked by Main.java.
     * @param msg the message of a commit
     */
    public static void commit(String msg) throws IOException {
        checkInitialized();

        /* 1st step of gitlet-design.md. */
        HashMap<File, String> currentStagingArea = readObject(STAGED_FOR_ADDITIONS, HashMap.class);
        HashSet<File> currentRemovalArea = readObject(STAGED_FOR_REMOVAL, HashSet.class);
        if (currentRemovalArea.isEmpty() && currentStagingArea.isEmpty()) {
            message("No changes added to the commit.");
            System.exit(0);
        }
        if (msg.isBlank()) {
            message("Please enter a commit message.");
            System.exit(0);
        }

        /* 2nd step of gitlet-design.md. */
        Commit lastestCommit = searchCurrentCommit();
        HashMap<File, String> lastestTrackedFiles = new HashMap<>(lastestCommit.getTrackedFiles());
        for (File file : currentRemovalArea) {
            lastestTrackedFiles.remove(file);
        }
        lastestTrackedFiles.putAll(currentStagingArea);
        resetStagingArea();

        /* 3rd step of gitlet-design.md. */
        Commit newCommit = new Commit(msg, sha1(serialize(lastestCommit)), lastestTrackedFiles);
        newCommit.saveCommit();

        /* 4th step of gitlet-design.md. */
        moveHEADTo(sha1(serialize(newCommit)));
    }

    /**
     * Overwrite the HEAD branch with the UID of the latest commit.
     * @param UID the UID of the latest commit
     */
    private static void moveHEADTo(String UID) {
        File HEADBranchFile = join(BRANCHES_DIR, readContentsAsString(HEAD));
        writeContents(HEADBranchFile, UID);
    }

    /**
     * Clear the Staging Area.
     */
    private static void resetStagingArea() {
        writeObject(STAGED_FOR_ADDITIONS, new HashMap<File, String>());
        writeObject(STAGED_FOR_REMOVAL, new HashSet<File>());
    }
}
