# Gitlet Design Document

**Name**: WEI Xize

## Classes and Data Structures

### Class 1: Repository.java

This class has many functions related to the creation and manipulation of the repo. And it is also the bridge connecting Main.java and lots of objects in Gitlet.
Contains only static variables and methods.

#### init()

Initialize the Gitlet repo. Invoked by Main.java case "init"
1. Judge if the order is legal or not.
2. Create all of needed folders and files. **For staging areas, we will create the relative files and write empty data structures on them.**
3. Create the first commit and serialize it into the 'commits' folder.
4. Setup master branch and the HEAD.

###### createbranch()

A helper method, create a new branch.

#### add(String fileName)

Stage files, invoked by Main.java.
1. Check Failure cases.
2. Check if the file is in the removal area.
3. Extract the current state of staging area.
4. Get the current commit info. If the file content remains the same as the commit's, then remove the file from staging area and then exit.
5. Add to the staging area. If the blob does not exist, then create a blob.

###### lots of helper methods

Used to manipulate the .gitlet folder. See docs in Repository.java.

#### commit(String msg)

Create a commit, invoked by Main.java.
1. Check Failure cases.
2. Add and remove files from the original tracked files HashMap.
3. Create and save a new commit.
4. Move the head and the branch to the current commit.

### Class 2: Commit.java

This class represents a commit. It should be serializable so that it can be put into the 'commits' folder.
This class is at the core of Gitlet.

#### Commit()

Initial commit. Invoked by Repository.init().

#### saveCommit()

Serialize the commit to the target directory.

#### Commit(String msg, String parentsUID1, HashMap<File, String> trackedFiles)

Constructor for not-initial commits.


## Algorithms

## Persistence

The .gitlet folder includes:

1. commits directory, which contains lots of serialized commit objects.
2. blobs directory, which contains copies of the contents of certain tracked files.
3. HEAD file, which is a pointer to the current branch.
4. branches directory, which is collection of all the branches.
5. staged_for_Additions file, which represents the staging area with a map.
6. staged_for_removal file, which represents the removal area with a set.

## Note
1. Serialize the object before sha1 it.
2. Make before testing.
3. HashSet implements Serializable while Set does not. So, we need use HashSet to readObject.
4. Break after cases, otherwise default case will be invoked.
5. **Pay attention to the passed-in parameters. Do not mess it up, which will cause tons of great pain to debug:)**