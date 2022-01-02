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


