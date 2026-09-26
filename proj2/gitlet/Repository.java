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
        TreeMap<File, String> trackedFiles = currentCommit.getTrackedFiles();
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
        return Commit.fromFile(readContentsAsString(join(BRANCHES_DIR, readContentsAsString(HEAD))));
    }

    /**
     * I/O helper function.
     * @param file file to be removed from staging area
     * @param currentStagingArea current staging area
     */
    private static void removeSomethingInStagingArea(File file, HashMap<File, String> currentStagingArea) {
        if (currentStagingArea.remove(file) != null) {
            writeObject(STAGED_FOR_ADDITIONS, currentStagingArea);
        }
    }

    /**
     * I/O helper function.
     * @param file file that should be removed from the removal area
     */
    private static void removeSomethingInRemovalArea(File file) {
        HashSet<File> currentRemovalArea = readObject(STAGED_FOR_REMOVAL, HashSet.class);
        if (currentRemovalArea.remove(file)) {
            writeObject(STAGED_FOR_REMOVAL, currentRemovalArea);
        }
    }

    /**
     * I/O helper function.
     * @param file file to be staged
     * @param currentStagingArea current staging area
     * @throws IOException
     */
    private static void addSomethingInStagingArea(File file, HashMap<File, String> currentStagingArea) throws IOException {
        currentStagingArea.put(file, sha1(readContents(file)));
        writeObject(STAGED_FOR_ADDITIONS, currentStagingArea);
    }

    /**
     * Create a blob for FILE if it does not exist.
     * @param file the file we create blob for
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
        TreeMap<File, String> lastestTrackedFiles = new TreeMap<>(lastestCommit.getTrackedFiles());
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

    /**
     * Stop tracking a certain file, invoked by Main.java.
     * @param fileName the name of the to be removed file
     */
    public static void rm(String fileName) {
        checkInitialized();

        /* 1st step of gitlet-design.md. */
        File fileToBeRemoved = join(CWD, fileName);
        HashMap<File, String> currentStagingArea = readObject(STAGED_FOR_ADDITIONS, HashMap.class);
        Commit currentCommit = searchCurrentCommit();
        TreeMap<File, String> lastestTrackedFiles = new TreeMap<>(currentCommit.getTrackedFiles());
        boolean staged = currentStagingArea.get(fileToBeRemoved) != null;
        boolean trackedByHAEDCommit = lastestTrackedFiles.get(fileToBeRemoved) != null;
        if (!staged && !trackedByHAEDCommit) {
            message("No reason to remove the file.");
            System.exit(0);
        }

        /* 2nd step of gitlet-design.md. */
        if (staged) {
            removeSomethingInStagingArea(fileToBeRemoved, currentStagingArea);
        }

        /* 3rd step of gitlet-design.md. */
        if (trackedByHAEDCommit) {
            addSomethingInRemovalArea(fileToBeRemoved);
            restrictedDelete(fileToBeRemoved);
        }
    }

    /**
     * I/O helper method.
     * @param fileToBeRemoved the to be removed file
     */
    private static void addSomethingInRemovalArea(File fileToBeRemoved) {
        HashSet<File> currentRemovalArea = readObject(STAGED_FOR_REMOVAL, HashSet.class);
        currentRemovalArea.add(fileToBeRemoved);
        writeObject(STAGED_FOR_REMOVAL, currentRemovalArea);
    }

    /**
     * Print out the history, invoked by Main.java.
     */
    public static void log() {
        checkInitialized();

        /* 1st step of gitlet-design.md. */
        Commit currentCommit = searchCurrentCommit();
        print(currentCommit);
        String ParentsUID1 = currentCommit.getParentsUID1();
        while (ParentsUID1 != null) {
            currentCommit = Commit.fromFile(ParentsUID1);
            ParentsUID1 = currentCommit.getParentsUID1();
            print(currentCommit);
        }
    }

    /**
     * Print out the data of a certain commit.
     * @param commit commit to be printed out
     */
    private static void print(Commit commit) {
        System.out.println("===");
        System.out.print("commit ");
        System.out.println(sha1(serialize(commit)));
        String parentsUID2 = commit.getParentsUID2();
        if (parentsUID2 != null) {
            System.out.print("Merge: ");
            System.out.print(commit.getParentsUID1().substring(0, 7));
            System.out.print(" ");
            System.out.println(parentsUID2.substring(0, 7));
        }


        /* @DeepSeek V4.1 Flash */
        System.out.print("Date: ");
        Date date = commit.getDate();
        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(date);

        Formatter formatter = new Formatter();
        formatter.format(Locale.US,
                "%1$ta %1$tb %1$td %1$tT %1$tY %1$tz",
                cal);

        String result = formatter.toString();
        System.out.println(result);
        // Wed Dec 31 16:00:00 1969 -0800


        System.out.println(commit.getMessage());
        System.out.println();
    }

    /**
     * Print out every commit. Invoked by Main.java.
     */
    public static void globalLog() {
        checkInitialized();

        List<String> fileNames = plainFilenamesIn(COMMITS_DIR);
        for (String fileName : fileNames) {
            print(readObject(join(COMMITS_DIR, fileName), Commit.class));
        }
    }

    /**
     * Find the commit with a certain message, then print out its UID. Invoked by Main.java.
     * @param msg message of a commit
     */
    public static void find(String msg) {
        checkInitialized();

        List<String> fileNames = plainFilenamesIn(COMMITS_DIR);
        boolean found = false;
        for (String fileName : fileNames) {
            Commit commit = readObject(join(COMMITS_DIR, fileName), Commit.class);
            if (Objects.equals(commit.getMessage(), msg)) {
                System.out.println(sha1(serialize(commit)));
                found = true;
            }
        }
        if (!found) {
            message("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**
     * Give some useful information to the user, invoked by Main.java.
     */
    public static void status() {
        checkInitialized();

        /* Branches. */
        System.out.println("=== Branches ===");
        String HEADBranch = readContentsAsString(HEAD);
        List<String> branchFileNames = plainFilenamesIn(BRANCHES_DIR);
        String[] branchFileNamesArray = branchFileNames.toArray(new String[0]);
        Arrays.sort(branchFileNamesArray);
        for (String branchFileName: branchFileNamesArray) {
            if (Objects.equals(branchFileName, HEADBranch)) {
                System.out.print("*");
            }
            System.out.println(branchFileName);
        }
        System.out.println();

        /* Staged Files. */
        System.out.println("=== Staged Files ===");
        HashMap<File, String> stagingArea = readObject(STAGED_FOR_ADDITIONS, HashMap.class);
        String[] stagedFileNames = new String[stagingArea.size()];
        int i = 0;
        for (File stagedFile : stagingArea.keySet()) {
            stagedFileNames[i] = stagedFile.getName();
            i += 1;
        }
        sortAndPrint(stagedFileNames);

        /* Removed Files. */
        System.out.println("=== Removed Files ===");
        HashSet<File> files = readObject(STAGED_FOR_REMOVAL, HashSet.class);
        String[] fileNames = new String[files.size()];
        int j = 0;
        for (File file : files) {
            fileNames[j] = file.getName();
            j += 1;
        }
        sortAndPrint(fileNames);

        /* Modifications Not Staged For Commit. */
        System.out.println("=== Modifications Not Staged For Commit ===");
        TreeMap<File, String> currentTrackedFiles = searchCurrentCommit().getTrackedFiles();
        HashSet<String> targetFileNames = new HashSet<>();
        for (Map.Entry<File, String> entry : currentTrackedFiles.entrySet()) {
            if (!files.contains(entry.getKey()) && !entry.getKey().exists()) {
                targetFileNames.add(entry.getKey().getName());
            }
            if (entry.getKey().exists()) {
                if (!sha1(readContents(entry.getKey())).equals(entry.getValue()) && !stagingArea.containsKey(entry.getKey()) && !files.contains(entry.getKey())) {
                    targetFileNames.add(entry.getKey().getName());
                }
            }
        }
        for (Map.Entry<File, String> entry : stagingArea.entrySet()) {
            if (!entry.getKey().exists() || !Objects.equals(sha1(readContents(entry.getKey())), entry.getValue())) {
                targetFileNames.add(entry.getKey().getName());
            }
        }
        String[] targetNamesArray = targetFileNames.toArray(new String[0]);
        Arrays.sort(targetNamesArray);
        for (String fileName : targetNamesArray) {
            System.out.print(fileName);
            if (join(CWD, fileName).exists()) {
                System.out.println(" (modified)");
            } else {
                System.out.println(" (deleted)");
            }
        }
        System.out.println();

        /* Untracked Files. */
        System.out.println("=== Untracked Files ===");
        LinkedList<String> untrackedFileNames = new LinkedList<>();
        String[] fileNamesInCWD = plainFilenamesIn(CWD).toArray(new String[0]);
        for (String fileNameInCWD : fileNamesInCWD) {
            if (!stagingArea.containsKey(join(CWD, fileNameInCWD)) && !currentTrackedFiles.containsKey(join(CWD, fileNameInCWD))) {
                untrackedFileNames.add(fileNameInCWD);
            }
            if (files.contains(join(CWD, fileNameInCWD)) && join(CWD, fileNameInCWD).exists()) {
                untrackedFileNames.add(fileNameInCWD);
            }
        }
        String[] fileNamesArray = untrackedFileNames.toArray(new String[0]);
        sortAndPrint(fileNamesArray);
    }

    /**
     * Print out the files' name based on FILENAMES.
     * @param fileNames a string array which contains the names of the to be printed files
     */
    private static void sortAndPrint(String[] fileNames) {
        Arrays.sort(fileNames);
        for (String fileName : fileNames) {
            System.out.println(fileName);
        }
        System.out.println();
    }
}
