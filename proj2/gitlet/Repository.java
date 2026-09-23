package gitlet;

import java.io.File;
import java.io.IOException;

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
     * initialize the Gitlet repo.
     * invoked by Main.java case "init"
     */
    public static void init() throws IOException {
        /* 1st step of gitlet-design.md. */
        if (GITLET_DIR.exists()) {
            throw error("A Gitlet version-control system already exists in the current directory.");
        }

        /* 2nd step of gitlet-design.md. */
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        HEAD.createNewFile();
        BRANCHES_DIR.mkdir();

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
}
