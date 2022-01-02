# Gitlet
A version-control system that mimics the basic features of the popular system Git.

## Overview of Gitlet
The main functionality that Gitlet supports is:
- Saving the contents of entire directories of files (command: commit).
- Restoring a version of one or more files or entire commits (command: checkout).
- Viewing the history of your backups (command: log).
- Maintaining related sequences of commits, called branches (command: branch).
- Merging changes made in one branch into another (command: merge).

## Internal Structures
Gitlet distinguishes several different kinds of objects. The main ones are:
- **blobs**: Essentially the contents of files.
- **trees**: Directory structures mapping names to references to blobs and other trees (subdirectories).
- **commits**: Combinations of log messages, other metadata (commit date, author, etc.), a reference to a tree, and references to parent commits. The repository also maintains a mapping from branch heads to references to commits, so that certain important commits have symbolic names.

Gitlet further simplifies Git by:
- Incorporating trees into commits and not dealing with subdirectories (so there will be one "flat" directory of plain files for each repository).
- Limiting to merges that reference two parents (in real Git, there can be any number of parents).
- Having metadata consist only of a timestamp and log message. A commit, therefore, will consist of a log message, timestamp, a mapping of file names to blob references, a parent reference, and (for merges) a second parent reference.

Every object—every blob and every commit—has a unique integer id that serves as a reference to the object. Two objects with exactly the same content will have the same id on all systems. In the case of blobs, "same content" means the same file contents. In the case of commits, it means the same metadata, the same mapping of names to references, and the same parent reference. Gitlet accomplishes this the same way as Git: by using a cryptographic hash function called SHA-1 (Secure Hash 1), which produces a 160-bit integer hash from any sequence of bytes.

Here is an example illustrating the structures used in Gitlet. As can be seen in the diagram below, each commit (rectangle) points to some blobs (circles), which contain file contents. The commits contain the file names and references to these blobs, as well as a parent link. These references, depicted as arrows, are represented in the .gitlet directory using their SHA-1 hash values (the small hexadecimal numerals above the commits and below the blobs). The newer commit contains an updated version of wug1.txt, but shares the same version of wug2.txt as the older commit.

![commits-and-blobs](https://user-images.githubusercontent.com/76065183/147873738-5ab89393-6b31-4279-a983-e8578fba6711.png)

## General Failure Cases
There are some failure cases Gitlet handles that don't apply to a particular command. These are:
- If a user doesn't input any arguments, Gitlet prints the message "Please enter a command." and exits.
- If a user inputs a command that doesn't exist, Gitlet prints the message "No command with that name exists." and exits.
- If a user inputs a command with the wrong number or format of operands, Gitlet prints the message "Incorrect operands." and exits.
- If a user inputs a command that requires being in an initialized Gitlet working directory (i.e., one containing a .gitlet subdirectory), but is not in such a directory, Gitlet prints the message "Not in an initialized Gitlet directory." and exits.

## Commands
### init
- **Usage**: java gitlet.Main init
- **Description**: Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit: a commit that contains no files and has the commit message "initial commit". It will have a single branch: master, which initially points to this initial commit, and master will be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 ("The (Unix) Epoch"). Since the initial commit in all repositories created by Gitlet will have exactly the same content, it follows that all repositories will automatically share this commit (they will all have the same UID) and all commits in all repositories will trace back to it.
- **Failure cases**: If there is already a Gitlet version-control system in the current directory, it should abort. It should NOT overwrite the existing system with a new one. In this case, prints the error message "A Gitlet version-control system already exists in the current directory." and exits.

### add
- **Usage**: java gitlet.Main add [file name]
- **Description**: Adds a copy of the file as it currently exists to the staging area (see the description of the **commit** command). Staging an already-staged file overwrites the previous entry in the staging area with the new contents. The staging area is contained within the .gitlet directory. If the current working version of the file is identical to the version in the current commit, does not stage it to be added, and removes it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back). The file will no longer be staged for removal (see the **rm** command), if it was at the time of the command.
- **Failure cases**: If the file does not exist, prints the error message "File does not exist." and exits without changing anything.

### commit
- **Usage**: java gitlet.Main commit [message]
- **Description**: Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit. By default, each commit's snapshot of files will be exactly the same as its parent commit's snapshot of files; it will keep versions of files exactly as they are, and not update them. A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent. A commit will save and start tracking any files that were staged for addition but weren't tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result of being staged for removal by the **rm** command (below).
- **Failure cases**: If no files have been staged, aborts. Prints the message "No changes added to the commit." Every commit must have a non-blank message. If it doesn't, prints the error message "Please enter a commit message." It is not a failure for tracked files to be missing from the working directory or changed in the working directory.

  Some additional points about **commit**:
  - The staging area is cleared after a commit.
  - The commit command never adds, changes, or removes files in the working directory (other than those in the .gitlet directory). The **rm** command will remove such files, as well as staging them for removal, so that they will be untracked after a commit.
  - Any changes made to files after staging for addition or removal are ignored by the **commit** command, which only modifies the contents of the .gitlet directory.
  - After the commit command, the new commit is added as a new node in the commit tree.
  - The commit just made becomes the "current commit", and the head pointer now points to it. The previous head commit is this commit's parent commit.
  - Each commit contains the date and time it was made.
  - Each commit has a log message associated with it that describes the changes to the files in the commit (specified by the user). The entire message should take up only one entry in the args array that is passed to main. To include multiword messages, surround them in quotes.
  - Each commit is identified by its SHA-1 id, which includes the file (blob) references of its files, parent reference, log message, and commit time.

### rm
- **Usage**: java gitlet.Main rm [file name]
- **Description**: Unstages the file if it is currently staged for addition. If the file is tracked in the current commit, stages it for removal and removes the file from the working directory if the user has not already done so (does not remove it unless it is tracked in the current commit).
- **Failure cases**: If the file is neither staged nor tracked by the head commit, prints the error message "No reason to remove the file." and exits.

### log
- **Usage**: java gitlet.Main log
- **Description**: Starting at the current head commit, displays information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits i.e. displays the head commit's history. (Same as the *git log --first-parent* command in regular Git). For every commit node in this history, the information displayed is the commit id, the time the commit was made, and the commit message. Here is an example of the exact format the **log** command follows:

       ===  
       commit a0da1ea5a15ab613bf9961fd86f010cf74c7ee48  
       Date: Thu Nov 9 20:00:05 2017 -0800  
       A commit message.  

       ===  
       commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff  
       Date: Thu Nov 9 17:01:33 2017 -0800  
       Another commit message.  

       ===  
       commit e881c9575d180a215d1a636545b8fd9abfb1d2bb  
       Date: Wed Dec 31 16:00:00 1969 -0800  
       initial commit  
   
  There is a === before each commit and an empty line after it. As in real Git, each entry displays the unique SHA-1 id of the commit object. The timestamps displayed in the commits reflect the current timezone, not UTC; as a result, the timestamp for the initial commit in the above example does not read Thursday, January 1st, 1970, 00:00:00, but rather the equivalent Pacific Standard Time.
  
  For merge commits (those that have two parent commits), **log** adds a line just below the first, as in:
  
       ===  
       commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff  
       Merge: 4975af1 2c1ead1  
       Date: Sat Nov 11 12:30:00 2017 -0800  
       Merged development into master.
   
  where the two hexadecimal numerals following "Merge:" consist of the first seven digits of the first and second parents' commit ids, in that order. The first parent is the current branch when the **merge** command was executed; the second is that of the merged-in branch (as in regular Git).

- **Failure cases**: None

### global-log
- **Usage**: java gitlet.Main global-log
- **Description**: Like log, except displays information about all commits ever made, in no specific order.
- **Failure cases**: None

### find
- **Usage**: java gitlet.Main find [commit message]
- **Description**: Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the **commit** command above.
- **Failure cases**: If no such commit exists, prints the error message "Found no commit with that message." and exits.
- **Differences from Git**: Doesn't exist in real Git. Similar effects can be achieved by grepping the output of log.

### status
- **Usage**: java gitlet.Main status
- **Description**: Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged for addition or removal. The following is an example of the exact format the **status** command follows:

      === Branches ===
      *master
      other-branch

      === Staged Files ===
      wug.txt
      wug2.txt

      === Removed Files ===
      goodbye.txt

      === Modifications Not Staged For Commit ===
      junk.txt (deleted)
      wug3.txt (modified)

      === Untracked Files ===
      random.stuff

  There is an empty line between sections. Entries are listed in lexicographic order, using the Java string-comparison order (the asterisk does not count). A file in the working directory is "modified but not staged" if it is:
    - Tracked in the current commit, changed in the working directory, but not staged; or
    - Staged for addition, but with different contents than in the working directory; or
    - Staged for addition, but deleted in the working directory; or
    - Not staged for removal, but tracked in the current commit and deleted from the working directory.

  The final category ("Untracked Files") is for files present in the working directory but neither staged for addition nor tracked. This includes files that have been staged for removal, but then re-created without Gitlet's knowledge. Any subdirectories that may have been introduced are ignored, since Gitlet does not deal with them.

- **Failure cases**: None

### checkout
Checkout is a command that can do different things depending on what its arguments are. There are 3 possible use cases (each bullet point below corresponds to a use case):

- **Usages**:
  - java gitlet.Main checkout -- [file name]
  - java gitlet.Main checkout [commit id] -- [file name]
  - java gitlet.Main checkout [branch name]

- **Descriptions**:
  - Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
  - Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
  - Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).

- **Failure cases**:
  - If the file does not exist in the previous commit, aborts and prints the error message "File does not exist in that commit."
  - If no commit with the given id exists, prints "No commit with that id exists." Otherwise, if the file does not exist in the given commit, prints the same message as for the first failure case.
  - If no branch with that name exists, prints "No such branch exists." If that branch is the current branch, prints "No need to checkout the current branch." If a working file is untracked in the current branch and would be overwritten by the checkout, prints "There is an untracked file in the way; delete it, or add and commit it first." and exits.

- **Differences from Git**: Git does not clear the staging area and stages the file that is checked out. Also, Git will not do a checkout that would overwrite or undo changes (additions or removals) that have been staged.
