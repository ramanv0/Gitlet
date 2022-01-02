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
Gitlet distinguishes several different kinds of objects. The important ones are:
- blobs: Essentially the contents of files.
- trees: Directory structures mapping names to references to blobs and other trees (subdirectories).
- commits: Combinations of log messages, other metadata (commit date, author, etc.), a reference to a tree, and references to parent commits. The repository also maintains a mapping from branch heads (in this course, we've used names like master, proj2, etc.) to references to commits, so that certain important commits have symbolic names.

Gitlet further simplifies Git by:
- Incorporating trees into commits and not dealing with subdirectories (so there will be one "flat" directory of plain files for each repository).
- Limiting to merges that reference two parents (in real Git, there can be any number of parents).
- Having metadata consist only of a timestamp and log message. A commit, therefore, will consist of a log message, timestamp, a mapping of file names to blob references, a parent reference, and (for merges) a second parent reference.

Every object—every blob and every commit—has a unique integer id that serves as a reference to the object. Two objects with exactly the same content will have the same id on all systems. In the case of blobs, "same content" means the same file contents. In the case of commits, it means the same metadata, the same mapping of names to references, and the same parent reference. Gitlet accomplishes this the same way as Git: by using a cryptographic hash function called SHA-1 (Secure Hash 1), which produces a 160-bit integer hash from any sequence of bytes.

Here is an example illustrating the structures used in Gitlet. As can be seen in the diagram below, each commit (rectangle) points to some blobs (circles), which contain file contents. The commits contain the file names and references to these blobs, as well as a parent link. These references, depicted as arrows, are represented in the .gitlet directory using their SHA-1 hash values (the small hexadecimal numerals above the commits and below the blobs). The newer commit contains an updated version of wug1.txt, but shares the same version of wug2.txt as the older commit.

![commits-and-blobs](https://user-images.githubusercontent.com/76065183/147873738-5ab89393-6b31-4279-a983-e8578fba6711.png)

## General Failure Cases
There are some failure cases Gitlet handles that don't apply to a particular command. They are:
- If a user doesn't input any arguments, print the message "Please enter a command." and exit.
- If a user inputs a command that doesn't exist, print the message "No command with that name exists." and exit.
- If a user inputs a command with the wrong number or format of operands, print the message "Incorrect operands." and exit.
- If a user inputs a command that requires being in an initialized Gitlet working directory (i.e., one containing a .gitlet subdirectory), but is not in such a directory, print the message "Not in an initialized Gitlet directory." and exit.

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
