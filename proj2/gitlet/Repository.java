package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  does at a high level.
 *  This class has many functions related to the creation & manipulation of the repo.
 *  And it is also the bridge connecting Main.java and lots of objects in Gitlet.
 *  @author WEI Xize
 */
public class Repository {
    /**
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
    /** Remote repos. */
    public static final File REMOTES_DIR = join(GITLET_DIR, "remotes");


    /**
     * Initialize the Gitlet repo.
     * Invoked by Main.java case "init"
     */
    public static void init() {
        /* 1st step of gitlet-design.md. */
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        /* 2nd step of gitlet-design.md. */
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        BRANCHES_DIR.mkdir();
        resetStagingArea();
        REMOTES_DIR.mkdir();

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
     */
    private static void createBranch(String name, String commitUID) {
        File branchFile = join(BRANCHES_DIR, name);
        writeContents(branchFile, commitUID);
    }

    /**
     * Stage files, invoked by Main.java.
     * @param fileName the name of the file to be staged
     */
    public static void add(String fileName) {
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
        TreeMap<String, String> trackedFiles = currentCommit.getTrackedFiles();
        if (Objects.equals(trackedFiles.get(fileName), sha1(readContents(file)))) {
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
        return Commit.fromFile(readContentsAsString(join(BRANCHES_DIR,
                readContentsAsString(HEAD))));
    }

    /**
     * I/O helper function.
     * @param file file to be removed from staging area
     * @param currentStagingArea current staging area
     */
    private static void removeSomethingInStagingArea(File file,
                                                     HashMap<File, String> currentStagingArea) {
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
     */
    private static void addSomethingInStagingArea(File file,
                                                  HashMap<File, String> currentStagingArea) {
        currentStagingArea.put(file, sha1(readContents(file)));
        writeObject(STAGED_FOR_ADDITIONS, currentStagingArea);
    }

    /**
     * Create a blob for FILE if it does not exist.
     * @param file the file we create blob for
     */
    private static void createBlob(File file) {
        File blob = join(BLOBS_DIR, sha1(readContents(file)));
        if (!blob.exists()) {
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
     * @param merge whether this is a merge commit
     * @param givenBranchHEAD the given commit to merge
     */
    public static void commit(String msg, boolean merge, Commit givenBranchHEAD) {
        checkInitialized();

        /* 1st step of gitlet-design.md. */
        HashMap<File, String> currentStagingArea = readObject(STAGED_FOR_ADDITIONS,
                HashMap.class);
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
        TreeMap<String, String> lastestTrackedFiles =
                new TreeMap<>(lastestCommit.getTrackedFiles());
        for (File file : currentRemovalArea) {
            lastestTrackedFiles.remove(file.getName());
        }
        HashMap<String, String> currentStaging = new HashMap<>();
        for (File file : currentStagingArea.keySet()) {
            currentStaging.put(file.getName(), currentStagingArea.get(file));
        }
        lastestTrackedFiles.putAll(currentStaging);
        resetStagingArea();

        /* 3rd step of gitlet-design.md. */
        Commit newCommit;
        if (!merge) {
            newCommit = new Commit(msg,
                    sha1(serialize(lastestCommit)),
                    lastestTrackedFiles);
        } else {
            newCommit = new Commit(msg,
                    sha1(serialize(lastestCommit)),
                    sha1(serialize(givenBranchHEAD)),
                    lastestTrackedFiles);
        }
        newCommit.saveCommit();

        /* 4th step of gitlet-design.md. */
        moveHEADTo(sha1(serialize(newCommit)));
    }

    /**
     * Overwrite the HEAD branch with the uid of the latest commit.
     * @param uid the uid of the latest commit
     */
    private static void moveHEADTo(String uid) {
        File headBranchFile = join(BRANCHES_DIR, readContentsAsString(HEAD));
        writeContents(headBranchFile, uid);
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
        HashMap<File, String> currentStagingArea = readObject(STAGED_FOR_ADDITIONS,
                HashMap.class);
        Commit currentCommit = searchCurrentCommit();
        TreeMap<String, String> lastestTrackedFiles =
                new TreeMap<>(currentCommit.getTrackedFiles());
        boolean staged = currentStagingArea.get(fileToBeRemoved) != null;
        boolean trackedByHAEDCommit = lastestTrackedFiles.get(fileName) != null;
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
        String parentsUid1 = currentCommit.getParentsUID1();
        while (parentsUid1 != null) {
            currentCommit = Commit.fromFile(parentsUid1);
            parentsUid1 = currentCommit.getParentsUID1();
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
        String headBranch = readContentsAsString(HEAD);
        List<String> branchFileNames = plainFilenamesIn(BRANCHES_DIR);
        String[] branchFileNamesArray = branchFileNames.toArray(new String[0]);
        Arrays.sort(branchFileNamesArray);
        for (String branchFileName: branchFileNamesArray) {
            if (Objects.equals(branchFileName, headBranch)) {
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
        HashSet<File> removalArea = readObject(STAGED_FOR_REMOVAL, HashSet.class);
        String[] fileNames = new String[removalArea.size()];
        int j = 0;
        for (File file : removalArea) {
            fileNames[j] = file.getName();
            j += 1;
        }
        sortAndPrint(fileNames);
        /* Modifications Not Staged For Commit. */
        System.out.println("=== Modifications Not Staged For Commit ===");
        TreeMap<String, String> currentTrackedFiles = searchCurrentCommit().getTrackedFiles();
        HashSet<String> targetFileNames = new HashSet<>();
        for (Map.Entry<String, String> entry : currentTrackedFiles.entrySet()) {
            if (!removalArea.contains(join(CWD, entry.getKey()))
                    && !join(CWD, entry.getKey()).exists()) {
                targetFileNames.add(join(CWD, entry.getKey()).getName());
            }
            if (join(CWD, entry.getKey()).exists()) {
                if (!sha1(readContents(join(CWD, entry.getKey()))).equals(entry.getValue())
                        && !stagingArea.containsKey(join(CWD, entry.getKey()))
                        && !removalArea.contains(join(CWD, entry.getKey()))) {
                    targetFileNames.add(join(CWD, entry.getKey()).getName());
                }
            }
        }
        for (Map.Entry<File, String> entry : stagingArea.entrySet()) {
            if (!entry.getKey().exists()
                    || !Objects.equals(sha1(readContents(entry.getKey())), entry.getValue())) {
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
        String[] fileNamesArray = searchUntrackedFilesNames(stagingArea,
                currentTrackedFiles,
                removalArea);
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

    /**
     * Checkout file in the current HEAD commit, invoked by Main.java.
     * @param fileName the name of the to-be-checked file
     */
    public static void checkoutFileName(String fileName) {
        /* 1st step of gitlet-design.md. */
        checkInitialized();
        TreeMap<String, String> trackedFiles = searchCurrentCommit().getTrackedFiles();
        checkFileExists(fileName, trackedFiles);

        /* 2nd step of gitlet-design.md. */
        checkOut(fileName, trackedFiles);
    }

    /**
     * Checkout file in the COMMIT ID commit, invoked by Main.java.
     * @param commitId ID of the given commit
     * @param fileName the name of the to-be-checked file
     */
    public static void checkoutCommitIdFileName(String commitId, String fileName) {
        /* 1st step of gitlet-design.md. */
        checkInitialized();
        TreeMap<String, String> trackedFiles = searchCommitByUID(commitId).getTrackedFiles();
        checkFileExists(fileName, trackedFiles);

        /* 2nd step of gitlet-design.md. */
        checkOut(fileName, trackedFiles);
    }

    /**
     * Checkout all files based on the given branch, invoked by Main.java.
     * @param branchName the name of the given branch
     */
    public static void checkoutBranchName(String branchName) {
        /* 1st step of gitlet-design.md. */
        checkInitialized();
        checkBranch(branchName);

        /* 2nd step of gitlet-design.md. */
        TreeMap<String, String> trackedFilesInCheckedOutBranch =
                searchCommitByUID(readContentsAsString(join(BRANCHES_DIR, branchName)))
                        .getTrackedFiles();
        TreeMap<String, String> trackedFilesInCurrentBranch =
                searchCurrentCommit().getTrackedFiles();
        HashMap<File, String> stagingArea = readObject(STAGED_FOR_ADDITIONS, HashMap.class);
        HashSet<File> removalArea = readObject(STAGED_FOR_REMOVAL, HashSet.class);
        HashSet<String> untrackedFileNames =
                new HashSet<>(Arrays.asList(searchUntrackedFilesNames(stagingArea,
                        trackedFilesInCurrentBranch,
                        removalArea)));

        /* 3rd step of gitlet-design.md. */
        checkOutAllTheFilesTrackedByTheGivenCommit(trackedFilesInCheckedOutBranch,
                untrackedFileNames);

        /* 4th step of gitlet-design.md. */
        removesTrackedFilesThatAreNotPresentInThatCommit(trackedFilesInCurrentBranch,
                trackedFilesInCheckedOutBranch);

        /* 5th step of gitlet-design.md. */
        resetStagingArea();
        writeContents(HEAD, branchName);

    }

    /**
     * Check if file exists in CWD.
     * @param fileName the name of the given file
     * @param trackedFiles the files tracked in the given commit
     */
    private static void checkFileExists(String fileName, TreeMap<String, String> trackedFiles) {
        if (!trackedFiles.containsKey(fileName)) {
            message("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /**
     * Search commit based on the given UID.
     * @param uid the UID of the target Commit
     * @return the target Commit object
     */
    private static Commit searchCommitByUID(String uid) {
        if (uid.length() == 40) {
            File commitFile = join(COMMITS_DIR, uid);
            if (!commitFile.exists()) { //UID长度是40但是还是有可能没有这个commit.
                message("No commit with that id exists.");
                System.exit(0);
            }
            return readObject(commitFile, Commit.class);
        } else {
            List<String> commitsNames = plainFilenamesIn(COMMITS_DIR);
            for (String commitName : commitsNames) {
                if (commitName.startsWith(uid)) {
                    return readObject(join(COMMITS_DIR, commitName), Commit.class);
                }
            }
            message("No commit with that id exists.");
            System.exit(0);
        }
        return null;
    }

    /**
     * Check whether the user pass in a valid branch name.
     * @param branchName the name of the given branch
     */
    private static void checkBranch(String branchName) {
        if (Objects.equals(branchName, readContentsAsString(HEAD))) {
            message("No need to checkout the current branch.");
            System.exit(0);
        }
        File branchFile = join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            message("No such branch exists.");
            System.exit(0);
        }
    }

    /**
     * Create or overwrite the given file based on its tracked version in the given commit.
     * @param fileName the name of the to-be-checked-out file
     * @param trackedFiles the tracked files in given commit
     */
    private static void checkOut(String fileName, TreeMap<String, String> trackedFiles) {
        File file = join(CWD, fileName);
        writeContents(file, readContents(join(BLOBS_DIR, trackedFiles.get(fileName))));
    }

    /**
     * Search all untracked files in CWD.
     * @param stagingArea the staging area
     * @param currentTrackedFiles the tracked files in current commit
     * @param removalArea the removal area
     * @return a String array contains the names of all the untracked files in CWD
     */
    private static String[] searchUntrackedFilesNames(HashMap<File, String> stagingArea,
                                                      TreeMap<String, String>
                                                              currentTrackedFiles,
                                                      HashSet<File> removalArea) {
        LinkedList<String> untrackedFileNames = new LinkedList<>();
        String[] fileNamesInCWD = plainFilenamesIn(CWD).toArray(new String[0]);
        for (String fileNameInCWD : fileNamesInCWD) {
            if (!stagingArea.containsKey(join(CWD, fileNameInCWD))
                    && !currentTrackedFiles.containsKey(fileNameInCWD)) {
                untrackedFileNames.add(fileNameInCWD);
            }
            if (removalArea.contains(join(CWD, fileNameInCWD))
                    && join(CWD, fileNameInCWD).exists()) {
                untrackedFileNames.add(fileNameInCWD);
            }
        }
        return untrackedFileNames.toArray(new String[0]);
    }

    /**
     * Create a new branch, invoked by Main.java.
     * @param branchName the name of the new branch
     */
    public static void branch(String branchName) {
        checkInitialized();
        checkIfBranchAlreadyExists(branchName);

        /* 1st step of gitlet-design.md. */
        writeContents(join(BRANCHES_DIR, branchName), sha1(serialize(searchCurrentCommit())));
    }

    /**
     * Check if the branch name is available.
     * @param branchName the name of the new branch
     */
    private static void checkIfBranchAlreadyExists(String branchName) {
        if (join(BRANCHES_DIR, branchName).exists()) {
            message("A branch with that name already exists.");
            System.exit(0);
        }
    }

    /**
     * Remove the given branch, invoked by Main.java.
     * @param branchName the name of the given branch
     */
    public static void rmBranch(String branchName) {
        checkInitialized();
        checkBranchForRemoval(branchName);

        /* 1st step of gitlet-design.md. */
        join(BRANCHES_DIR, branchName).delete();
        //restrictedDelete() can not be used since there is no .gitlet aside the branch file.
    }

    /**
     * Check whether the user pass in a valid branch name.
     * @param branchName the name of the given branch
     */
    private static void checkBranchForRemoval(String branchName) {
        if (Objects.equals(branchName, readContentsAsString(HEAD))) {
            message("Cannot remove the current branch.");
            System.exit(0);
        }
        checkBranchExists(branchName);
    }

    /**
     * Checkout a specific commit, invoked by Main.java.
     * @param commitId the UID of the given commit
     */
    public static void reset(String commitId) {
        /* 1st step of gitlet-design.md. */
        checkInitialized();

        /* 2nd step of gitlet-design.md. */
        TreeMap<String, String> trackedFilesInCheckedOutCommit =
                searchCommitByUID(commitId).getTrackedFiles();
        TreeMap<String, String> trackedFilesInCurrentCommit =
                searchCurrentCommit().getTrackedFiles();
        HashMap<File, String> stagingArea =
                readObject(STAGED_FOR_ADDITIONS, HashMap.class);
        HashSet<File> removalArea =
                readObject(STAGED_FOR_REMOVAL, HashSet.class);
        HashSet<String> untrackedFileNames =
                new HashSet<>(Arrays.asList(searchUntrackedFilesNames(stagingArea,
                        trackedFilesInCurrentCommit, removalArea)));

        /* 3rd step of gitlet-design.md. */
        checkOutAllTheFilesTrackedByTheGivenCommit(trackedFilesInCheckedOutCommit,
                untrackedFileNames);

        /* 4th step of gitlet-design.md. */
        removesTrackedFilesThatAreNotPresentInThatCommit(trackedFilesInCurrentCommit,
                trackedFilesInCheckedOutCommit);

        /* 5th step of gitlet-design.md. */
        resetStagingArea();
        writeContents(join(BRANCHES_DIR, readContentsAsString(HEAD)),
                sha1(serialize(searchCommitByUID(commitId))));
        //Do not save commitId since it might be shortened.
    }

    /**
     * Checks out all the files tracked by the given commit.
     * @param trackedFilesInCheckedOutCommit tracked files in checked out commit
     * @param untrackedFileNames untracked file names
     */
    private static void checkOutAllTheFilesTrackedByTheGivenCommit(
            TreeMap<String, String> trackedFilesInCheckedOutCommit,
            HashSet<String> untrackedFileNames) {
        for (String checkedOutFileName : trackedFilesInCheckedOutCommit.keySet()) {
            File checkedOutFile = join(CWD, checkedOutFileName);
            checkUntrackedFile(checkedOutFile, untrackedFileNames);
        }
        for (String checkedOutFileName : trackedFilesInCheckedOutCommit.keySet()) {
            checkOut(checkedOutFileName, trackedFilesInCheckedOutCommit);
            //Should manipulate after making sure that all the error cases are impossible!
        }
    }

    /**
     * Removes tracked files that are not present in that commit.
     * @param trackedFilesInCurrentCommit tracked files in current commit
     * @param trackedFilesInCheckedOutCommit tracked files in checked out commit
     */
    private static void removesTrackedFilesThatAreNotPresentInThatCommit(
            TreeMap<String, String> trackedFilesInCurrentCommit,
            TreeMap<String, String> trackedFilesInCheckedOutCommit) {
        for (String currentTrackedFileName : trackedFilesInCurrentCommit.keySet()) {
            File currentTrackedFile = join(CWD, currentTrackedFileName);
            if (!trackedFilesInCheckedOutCommit.containsKey(currentTrackedFileName)) {
                restrictedDelete(currentTrackedFile);
            }
        }
    }

    /**
     * Merges files from the given branch into the current branch.
     * Invoked by Main.java.
     * @param branchName the name of the given branch
     */
    public static void merge(String branchName) {
        /* 1st step of gitlet-design.md. */
        checkInitialized();
        checkStagedAdditionsOrRemovals();
        checkBranchExists(branchName);
        checkMergeABranchWithItself(branchName);

        /* 2nd step of gitlet-design.md. */
        Commit currentCommit = searchCurrentCommit();
        Commit givenBranchHeads =
                searchCommitByUID(readContentsAsString(join(BRANCHES_DIR, branchName)));
        Commit splitPoint = getSplitPoint(givenBranchHeads, currentCommit);
        if (sha1(serialize(splitPoint)).equals(sha1(serialize(givenBranchHeads)))) {
            message("Given branch is an ancestor of the current branch.");
            return;
        } else if (sha1(serialize(splitPoint)).equals(sha1(serialize(currentCommit)))) {
            checkoutBranchName(branchName);
            message("Current branch fast-forwarded.");
            return;
        }

        /* 3rd step of gitlet-design.md. */
        TreeMap<String, String> currentCommitTrackedFiles = currentCommit.getTrackedFiles();
        TreeMap<String, String> givenBranchHeadsTrackedFiles =
                givenBranchHeads.getTrackedFiles();
        TreeMap<String, String> splitPointTrackedFiles = splitPoint.getTrackedFiles();
        checkUntrackedFile(currentCommitTrackedFiles,
                givenBranchHeadsTrackedFiles, splitPointTrackedFiles);
        boolean conflict = processMergingFiles(currentCommitTrackedFiles,
                givenBranchHeadsTrackedFiles, splitPointTrackedFiles);

        /* 4th step of gitlet-design.md. */
        commit("Merged " + branchName + " into "
                + readContentsAsString(HEAD) + ".", true,
                givenBranchHeads);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Check if there are staged files.
     */
    private static void checkStagedAdditionsOrRemovals() {
        if (!readObject(STAGED_FOR_ADDITIONS, HashMap.class).isEmpty()
                || !readObject(STAGED_FOR_REMOVAL, HashSet.class).isEmpty()) {
            message("You have uncommitted changes.");
            System.exit(0);
        }
    }

    /**
     * Check if the given branch exists.
     * @param branchName the name of the given branch
     */
    private static void checkBranchExists(String branchName) {
        File branchFile = join(BRANCHES_DIR, branchName);
        if (!branchFile.exists()) {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    /**
     * Check if the given branch is the same as the current branch.
     * @param branchName the name of the given branch
     */
    private static void checkMergeABranchWithItself(String branchName) {
        if (Objects.equals(readContentsAsString(HEAD), branchName)) {
            message("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /**
     * Get the split point.
     * Will break tie based on the distance from COMMIT2.
     * @param commit1 given commit
     * @param commit2 current HEAD commit
     * @return split point commit
     */
    private static Commit getSplitPoint(Commit commit1, Commit commit2) {
        HashSet<String> ancestorsOfCommit1 = new HashSet<>();
        addToSet(sha1(serialize(commit1)), ancestorsOfCommit1);
        return findSplitPointBasedOnAncestorsOfCommit1(sha1(serialize(commit2)),
                ancestorsOfCommit1);
    }

    /**
     * Mark all the ancestors of commit1.
     * @param commitUID commit1's UID
     * @param ancestors ancestors of commit1
     */
    private static void addToSet(String commitUID, HashSet<String> ancestors) {
        if (ancestors.contains(commitUID)) {
            return;
        }

        ancestors.add(commitUID);

        Commit currentCommit = searchCommitByUID(commitUID);
        if (currentCommit.getParentsUID1() == null) {
            return;
        } else {
            addToSet(currentCommit.getParentsUID1(), ancestors);
        }
        if (currentCommit.getParentsUID2() != null) {
            addToSet(currentCommit.getParentsUID2(), ancestors);
        }
    }

    /**
     * BFS commit1's nearest ancestor to commit2.
     * @param commitUID the UID of commit2
     * @param ancestorsOfCommit1 ancestors of commit1
     * @return commit1's nearest ancestor to commit2
     */
    private static Commit findSplitPointBasedOnAncestorsOfCommit1(
            String commitUID,
            HashSet<String> ancestorsOfCommit1) {
        HashSet<String> explored = new HashSet<>();
        Deque<String> fringe = new ArrayDeque<>();
        fringe.add(commitUID);
        while (!fringe.isEmpty()) {
            String currentCommitUID = fringe.remove();
            explored.add(currentCommitUID);
            Commit currentCommit = searchCommitByUID(currentCommitUID);
            if (ancestorsOfCommit1.contains(currentCommitUID)) {
                return currentCommit;
            }
            if (!explored.contains(currentCommit.getParentsUID1())) {
                fringe.add(currentCommit.getParentsUID1());
            }
            if (currentCommit.getParentsUID2() != null
                    && !explored.contains(currentCommit.getParentsUID2())) {
                fringe.add(currentCommit.getParentsUID2());
            }
        }
        return null;
    }

    /**
     * Process all the files in CWD.
     * @param currentCommitTrackedFiles current branch tracked files
     * @param givenBranchHeadsTrackedFiles given branch tracked files
     * @param splitPointTrackedFiles split point tracked files
     * @return whether there are conflicts
     */
    private static boolean processMergingFiles(TreeMap<String, String>
                                                       currentCommitTrackedFiles,
                                               TreeMap<String, String>
                                                       givenBranchHeadsTrackedFiles,
                                               TreeMap<String, String>
                                                       splitPointTrackedFiles) {

        boolean conflictFromCwdFiles = mergeCwdFiles(currentCommitTrackedFiles,
                splitPointTrackedFiles,
                givenBranchHeadsTrackedFiles);
        boolean conflictFromGivenBranchFiles = mergeGivenBranchFiles(currentCommitTrackedFiles,
                splitPointTrackedFiles,
                givenBranchHeadsTrackedFiles);
        return conflictFromCwdFiles || conflictFromGivenBranchFiles;
    }

    /**
     * Check untracked file for merging, if it is going to be manipulated, exit.
     * @param currentCommitTrackedFiles current commit tracked files
     * @param givenBranchHeadsTrackedFiles given branch heads tracked files
     * @param splitPointTrackedFiles split point tracked files
     */
    private static void checkUntrackedFile(TreeMap<String, String> currentCommitTrackedFiles,
                                           TreeMap<String, String> givenBranchHeadsTrackedFiles,
                                           TreeMap<String, String> splitPointTrackedFiles) {
        HashMap<File, String> stagingArea =
                readObject(STAGED_FOR_ADDITIONS, HashMap.class);
        HashSet<File> removalArea =
                readObject(STAGED_FOR_REMOVAL, HashSet.class);
        HashSet<String> untrackedFileNames =
                new HashSet<>(Arrays.asList(searchUntrackedFilesNames(stagingArea,
                        currentCommitTrackedFiles, removalArea)));

        List<String> filesInCWD = plainFilenamesIn(CWD);
        for (String file : filesInCWD) {
            boolean unModifiedInTheCurrentBranch =
                    currentCommitTrackedFiles.containsKey(file)
                            && splitPointTrackedFiles.containsKey(file)
                            && Objects.equals(currentCommitTrackedFiles.get(file),
                            splitPointTrackedFiles.get(file));
            boolean modifiedInTheCurrentBranch =
                    currentCommitTrackedFiles.containsKey(file)
                            && splitPointTrackedFiles.containsKey(file)
                            && !Objects.equals(currentCommitTrackedFiles.get(file),
                            splitPointTrackedFiles.get(file));
            boolean modifiedInTheGivenBranch =
                    givenBranchHeadsTrackedFiles.containsKey(file)
                            && splitPointTrackedFiles.containsKey(file)
                            && !Objects.equals(givenBranchHeadsTrackedFiles.get(file),
                            splitPointTrackedFiles.get(file));

            /* Case 1. */
            if (modifiedInTheGivenBranch && unModifiedInTheCurrentBranch) {
                checkUntrackedFile(join(CWD, file), untrackedFileNames);
            }

            /* Case 6. */
            if (unModifiedInTheCurrentBranch
                    && !givenBranchHeadsTrackedFiles.containsKey(file)) {
                checkUntrackedFile(join(CWD, file), untrackedFileNames);
            }

            /* Case 8. */
            boolean changedAndDifferentFromOther = modifiedInTheCurrentBranch
                    && modifiedInTheGivenBranch
                    && !Objects.equals(currentCommitTrackedFiles.get(file),
                    givenBranchHeadsTrackedFiles.get(file));
            boolean oneChangedTheOtherDeleted = modifiedInTheCurrentBranch
                    && !givenBranchHeadsTrackedFiles.containsKey(file);
            boolean fileAbsentAtTheSplitPointAndHasDifferentContents
                    = !splitPointTrackedFiles.containsKey(file)
                    && currentCommitTrackedFiles.containsKey(file)
                    && givenBranchHeadsTrackedFiles.containsKey(file)
                    && !Objects.equals(currentCommitTrackedFiles.get(file),
                    givenBranchHeadsTrackedFiles.get(file));
            boolean modifiedInDifferentWays = changedAndDifferentFromOther
                    || oneChangedTheOtherDeleted
                    || fileAbsentAtTheSplitPointAndHasDifferentContents;
            if (modifiedInDifferentWays) {
                checkUntrackedFile(join(CWD, file), untrackedFileNames);
            }
        }

        for (String file : givenBranchHeadsTrackedFiles.keySet()) {
            boolean modifiedInTheGivenBranch = givenBranchHeadsTrackedFiles.containsKey(file)
                    && splitPointTrackedFiles.containsKey(file)
                    && !Objects.equals(givenBranchHeadsTrackedFiles.get(file),
                    splitPointTrackedFiles.get(file));

            /* Case 5. */
            if (!splitPointTrackedFiles.containsKey(file)
                    && givenBranchHeadsTrackedFiles.containsKey(file)
                    && !currentCommitTrackedFiles.containsKey(file)) {
                checkUntrackedFile(join(CWD, file), untrackedFileNames);
            }

            /* Case 8. */
            if (modifiedInTheGivenBranch && !currentCommitTrackedFiles.containsKey(file)) {
                checkUntrackedFile(join(CWD, file), untrackedFileNames);
            }
        }
    }

    /**
     * Check untracked file, if it is going to be manipulated, exit.
     * @param checkedOutFile the being checked file
     * @param untrackedFileNames untracked files
     */
    private static void checkUntrackedFile(File checkedOutFile,
                                           HashSet<String> untrackedFileNames) {
        if (checkedOutFile.exists() && untrackedFileNames.contains(checkedOutFile.getName())) {
            message("There is an untracked file in the way; delete it, "
                    + "or add and commit it first.");
            System.exit(0);
        }
    }

    /**
     * Deal with conflicts in merging.
     * @param givenBranchHeadsTrackedFiles given branch HEAD's tracked files
     * @param file the file being processed
     * @param currentCommitTrackedFiles current commit tracked files
     */
    private static void dealWithConflict(TreeMap<String, String> givenBranchHeadsTrackedFiles,
                                         File file,
                                         TreeMap<String, String> currentCommitTrackedFiles) {
        if (!givenBranchHeadsTrackedFiles.containsKey(file.getName())) {
            writeContents(file, "<<<<<<< HEAD\n",
                    readContents(join(BLOBS_DIR,
                            currentCommitTrackedFiles.get(file.getName()))),
                    "=======\n", ">>>>>>>\n");
        } else if (!currentCommitTrackedFiles.containsKey(file.getName())) {
            writeContents(file, "<<<<<<< HEAD\n",
                    "=======\n",
                    readContents(join(BLOBS_DIR,
                            givenBranchHeadsTrackedFiles.get(file.getName()))),
                    ">>>>>>>\n");
        } else {
            writeContents(file, "<<<<<<< HEAD\n",
                    readContents(join(BLOBS_DIR,
                            currentCommitTrackedFiles.get(file.getName()))),
                    "=======\n", readContents(join(BLOBS_DIR,
                            givenBranchHeadsTrackedFiles.get(file.getName()))),
                    ">>>>>>>\n");
        }
    }

    /**
     * Merge CWD files.
     * @param currentCommitTrackedFiles current commit tracked files
     * @param splitPointTrackedFiles split point tracked files
     * @param givenBranchHeadsTrackedFiles given branch heads tracked files
     * @return  whether there are conflicts
     */
    private static boolean mergeCwdFiles(TreeMap<String, String> currentCommitTrackedFiles,
                                         TreeMap<String, String> splitPointTrackedFiles,
                                         TreeMap<String, String> givenBranchHeadsTrackedFiles) {
        boolean conflict = false;
        List<String> filesInCWD = plainFilenamesIn(CWD);
        for (String file : filesInCWD) {
            boolean unModifiedInTheCurrentBranch =
                    currentCommitTrackedFiles.containsKey(file)
                            && splitPointTrackedFiles.containsKey(file)
                            && Objects.equals(currentCommitTrackedFiles.get(file),
                            splitPointTrackedFiles.get(file));
            boolean modifiedInTheCurrentBranch =
                    currentCommitTrackedFiles.containsKey(file)
                            && splitPointTrackedFiles.containsKey(file)
                            && !Objects.equals(currentCommitTrackedFiles.get(file),
                            splitPointTrackedFiles.get(file));
            boolean modifiedInTheGivenBranch =
                    givenBranchHeadsTrackedFiles.containsKey(file)
                            && splitPointTrackedFiles.containsKey(file)
                            && !Objects.equals(givenBranchHeadsTrackedFiles.get(file),
                            splitPointTrackedFiles.get(file));

            /* Case 1. */
            if (modifiedInTheGivenBranch && unModifiedInTheCurrentBranch) {
                checkOut(file, givenBranchHeadsTrackedFiles);
                add(file);
                continue;
            }

            /* Case 6. */
            if (unModifiedInTheCurrentBranch
                    && !givenBranchHeadsTrackedFiles.containsKey(file)) {
                rm(file);
                continue;
            }

            /* Case 8. */
            boolean changedAndDifferentFromOther = modifiedInTheCurrentBranch
                    && modifiedInTheGivenBranch
                    && !Objects.equals(currentCommitTrackedFiles.get(file),
                    givenBranchHeadsTrackedFiles.get(file));
            boolean oneChangedTheOtherDeleted = modifiedInTheCurrentBranch
                    && !givenBranchHeadsTrackedFiles.containsKey(file);
            boolean fileAbsentAtTheSplitPointAndHasDifferentContents =
                    !splitPointTrackedFiles.containsKey(file)
                            && currentCommitTrackedFiles.containsKey(file)
                            && givenBranchHeadsTrackedFiles.containsKey(file)
                            && !Objects.equals(currentCommitTrackedFiles.get(file),
                            givenBranchHeadsTrackedFiles.get(file));
            boolean modifiedInDifferentWays = changedAndDifferentFromOther
                    || oneChangedTheOtherDeleted
                    || fileAbsentAtTheSplitPointAndHasDifferentContents;
            if (modifiedInDifferentWays) {
                conflict = true;
                dealWithConflict(givenBranchHeadsTrackedFiles, join(CWD, file),
                        currentCommitTrackedFiles);
                add(file);
            }
        }
        return conflict;
    }

    /**
     * Merge given branch files.
     * @param currentCommitTrackedFiles current commit tracked files
     * @param splitPointTrackedFiles split point tracked files
     * @param givenBranchHeadsTrackedFiles given branch heads tracked files
     * @return  whether there are conflicts
     */
    private static boolean mergeGivenBranchFiles(TreeMap<String, String>
                                                         currentCommitTrackedFiles,
                                                 TreeMap<String, String>
                                                         splitPointTrackedFiles,
                                                 TreeMap<String, String>
                                                         givenBranchHeadsTrackedFiles) {
        boolean conflict = false;
        for (String file : givenBranchHeadsTrackedFiles.keySet()) {
            boolean modifiedInTheGivenBranch =
                    givenBranchHeadsTrackedFiles.containsKey(file)
                            && splitPointTrackedFiles.containsKey(file)
                            && !Objects.equals(givenBranchHeadsTrackedFiles.get(file),
                            splitPointTrackedFiles.get(file));

            /* Case 5. */
            if (!splitPointTrackedFiles.containsKey(file)
                    && givenBranchHeadsTrackedFiles.containsKey(file)
                    && !currentCommitTrackedFiles.containsKey(file)) {
                checkOut(file, givenBranchHeadsTrackedFiles);
                add(file);
                continue;
            } // Should not in the loop above, while these files do not exist in CWD.

            /* Case 8. */
            if (modifiedInTheGivenBranch && !currentCommitTrackedFiles.containsKey(file)) {
                conflict = true;
                dealWithConflict(givenBranchHeadsTrackedFiles, join(CWD, file),
                        currentCommitTrackedFiles);
                add(file);
            }
        }
        return conflict;
    }

    /**
     * Add remote, invoked by Main.java.
     * @param remoteName the name of the remote
     * @param nameOfRemoteDirectory the path to the remote
     */
    public static void addRemote(String remoteName, String nameOfRemoteDirectory) {
        checkInitialized();
        if (join(REMOTES_DIR, remoteName).exists()) {
            message("A remote with that name already exists.");
            System.exit(0);
        }

        String path = nameOfRemoteDirectory.replace('/', File.separatorChar);
        writeContents(join(REMOTES_DIR, remoteName), path);
    }

    /**
     * Remove a remote, invoked by Main.java.
     * @param remoteName the name of the remote
     */
    public static void rmRemote(String remoteName) {
        checkInitialized();
        if (!join(REMOTES_DIR, remoteName).exists()) {
            message("A remote with that name does not exist.");
            System.exit(0);
        }

        join(REMOTES_DIR, remoteName).delete();
    }

    /**
     * Attempts to append the current branch’s commits
     * to the end of the given branch at the given remote.
     * Invoked by Main.java.
     * @param remoteName the name of the remote repo
     * @param remoteBranchName the name of the remote branch
     */
    public static void push(String remoteName, String remoteBranchName) {
        checkInitialized();

        File remoteGitletDir = getRemoteGitletDir(remoteName);
        checkRemoteDir(remoteGitletDir);

        /* If the Gitlet system on the remote machine exists
        but does not have the input branch,
        then simply add the branch to the remote Gitlet. */
        Commit currentCommit = searchCurrentCommit();
        if (!join(join(remoteGitletDir, "branches"), remoteBranchName).exists()) {
            addCommitToRemote(currentCommit, remoteGitletDir);
            addBlobsToRemote(currentCommit, remoteGitletDir);
            writeContents(join(join(remoteGitletDir, "branches"),
                    remoteBranchName),
                    sha1(serialize(currentCommit)));
            return;
        }

        String remoteBranchesHead = readContentsAsString(join(join(remoteGitletDir,
                "branches"), remoteBranchName));
        checkInTheHistoryOfLocalHead(currentCommit, remoteBranchesHead);

        while (!sha1(serialize(currentCommit)).equals(remoteBranchesHead)) {
            addCommitToRemote(currentCommit, remoteGitletDir);
            addBlobsToRemote(currentCommit, remoteGitletDir);
            currentCommit = Commit.fromFile(currentCommit.getParentsUID1());
            // Do not forget to move the pointer!!!
        }

        writeContents(join(join(remoteGitletDir, "branches"), remoteBranchName),
                sha1(serialize(searchCurrentCommit())));
    }

    private static File getRemoteGitletDir(String remoteName) {
        return join(CWD, readContentsAsString(join(REMOTES_DIR, remoteName)));
    }

    private static void checkInTheHistoryOfLocalHead(Commit currentLocalHead,
                                                     String remoteBranchesHead) {
        boolean isHistory = false;
        while (currentLocalHead != null) {
            if (sha1(serialize(currentLocalHead)).equals(remoteBranchesHead)) {
                isHistory = true;
                break;
            }
            currentLocalHead = Commit.fromFile(currentLocalHead.getParentsUID1());
            // fromFile can now deal with null parameter.
        }
        if (!isHistory) {
            message("Please pull down remote changes before pushing.");
            System.exit(0);
        }
    }

    private static void checkRemoteDir(File remoteGitletDir) {
        if (!remoteGitletDir.exists()) {
            message("Remote directory not found.");
            System.exit(0);
        }
    }

    private static void addCommitToRemote(Commit currentCommit, File remoteGitletDir) {
        writeObject(join(join(remoteGitletDir, "commits"),
                sha1(serialize(currentCommit))), currentCommit);
    }

    private static void addBlobsToRemote(Commit currentCommit, File remoteGitletDir) {
        TreeMap<String, String> currentTrackedFiles = currentCommit.getTrackedFiles();
        for (String fileName : currentTrackedFiles.keySet()) {
            String blobName = currentTrackedFiles.get(fileName);
            if (!join(join(remoteGitletDir, "blobs"), blobName).exists()) {
                writeContents(join(join(remoteGitletDir, "blobs"), blobName),
                        readContents(join(BLOBS_DIR, blobName)));
            }
        }
    }

    /**
     * Brings down commits from the remote Gitlet repository into the local Gitlet repository.
     * Invoked By Main.java.
     * @param remoteName the name of the remote
     * @param remoteBranchName the given remote branch name
     */
    public static void fetch(String remoteName, String remoteBranchName) {
        checkInitialized();
        File remoteGitletDir = getRemoteGitletDir(remoteName);
        checkRemoteDir(remoteGitletDir);
        checkRemoteBranch(remoteGitletDir, remoteBranchName);

        bfsFetch(remoteGitletDir, remoteBranchName);

        File remoteBranchesDir = join(BRANCHES_DIR, remoteName);
        if (!remoteBranchesDir.exists()) {
            remoteBranchesDir.mkdir();
            // Make sure remote branch directory exists.
        }
        writeContents(join(remoteBranchesDir, remoteBranchName),
                readContentsAsString(join(join(remoteGitletDir, "branches"),
                        remoteBranchName)));
    }

    private static void checkRemoteBranch(File remoteGitletDir, String remoteBranchName) {
        if (!join(join(remoteGitletDir, "branches"), remoteBranchName).exists()) {
            message("That remote does not have that branch.");
            System.exit(0);
        }
    }

    private static void bfsFetch(File remoteGitletDir, String remoteBranchName) {
        String currentCommitId = readContentsAsString(join(join(remoteGitletDir,
                "branches"), remoteBranchName));
        HashSet<String> explored = new HashSet<>();
        Queue<String> fringe = new ArrayDeque<>();
        fringe.add(currentCommitId);
        while (!fringe.isEmpty()) {
            currentCommitId = fringe.remove();
            explored.add(currentCommitId);

            File localCommitFile = join(COMMITS_DIR, currentCommitId);
            if (!localCommitFile.exists()) {
                writeContents(localCommitFile, readContents(join(join(remoteGitletDir,
                        "commits"), currentCommitId)));
            }

            Commit currentCommit = readObject(join(join(remoteGitletDir, "commits"),
                    currentCommitId), Commit.class);
            TreeMap<String, String> currentCommitTrackedFiles = currentCommit.getTrackedFiles();
            for (String fileName : currentCommitTrackedFiles.keySet()) {
                String blobName = currentCommitTrackedFiles.get(fileName);
                if (!join(BLOBS_DIR, blobName).exists()) {
                    writeContents(join(BLOBS_DIR, blobName),
                            readContents(join(join(remoteGitletDir, "blobs"),
                                    blobName)));
                }
            }

            String parent1 = currentCommit.getParentsUID1();
            if (parent1 != null && !explored.contains(parent1)) {
                fringe.add(parent1);
            }
            String parent2 = currentCommit.getParentsUID2();
            if (parent2 != null && !explored.contains(parent2)) {
                fringe.add(parent2);
            }
        }
    }

    /**
     * Fetches branch [remote name]/[remote branch name] as for the fetch command,
     * and then merges that fetch into the current branch.
     * Invoked by Main.java.
     * @param remoteName the name of the remote
     * @param remoteBranchName the given remote branch name
     */
    public static void pull(String remoteName, String remoteBranchName) {
        fetch(remoteName, remoteBranchName);
        merge(remoteName + File.separatorChar + remoteBranchName);
    }
}
