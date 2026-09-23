# Gitlet Design Document

**Name**: WEI Xize

## Classes and Data Structures

### Class 1: Repository.java

This class has many functions related to the creation and manipulation of the repo. And it is also the bridge connecting Main.java and lots of objects in Gitlet.
Contains only static variables and methods.

#### init()

Initialize the Gitlet repo. Invoked by Main.java case "init"
1. Judge if the order is legal or not.
2. Create all of needed folders and files.
3. Create the first commit and serialize it into the 'commits' folder.
4. Setup master branch and the HEAD.

#### createbranch()

A helper method, create a new branch.

### Class 2: Commit.java

This class represents a commit. It should be serializable so that it can be put into the 'commits' folder.
This class is at the core of Gitlet.

#### Commit()

Initial commit. Invoked by Repository.init()

#### saveCommit()

Serialize the commit to the target directory.


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