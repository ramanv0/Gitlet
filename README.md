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

Every object—–every blob and every commit—–has a unique integer id that serves as a reference to the object. Two objects with exactly the same content will have the same id on all systems. In the case of blobs, "same content" means the same file contents. In the case of commits, it means the same metadata, the same mapping of names to references, and the same parent reference. Gitlet accomplishes this the same way as Git: by using a cryptographic hash function called SHA-1 (Secure Hash 1), which produces a 160-bit integer hash from any sequence of bytes.

Here is an example illustrating the structures used in Gitlet. As can be seen in the diagram below, each commit (rectangle) points to some blobs (circles), which contain file contents. The commits contain the file names and references to these blobs, as well as a parent link. These references, depicted as arrows, are represented in the .gitlet directory using their SHA-1 hash values (the small hexadecimal numerals above the commits and below the blobs). The newer commit contains an updated version of wug1.txt, but shares the same version of wug2.txt as the older commit.

![commits-and-blobs](https://user-images.githubusercontent.com/76065183/147873738-5ab89393-6b31-4279-a983-e8578fba6711.png)

## General Failure Cases
There are some failure cases Gitlet handles that don't apply to a particular command. They are:
- If a user doesn't input any arguments, print the message "Please enter a command." and exit.
- If a user inputs a command that doesn't exist, print the message "No command with that name exists." and exit.
- If a user inputs a command with the wrong number or format of operands, print the message "Incorrect operands." and exit.
- If a user inputs a command that requires being in an initialized Gitlet working directory (i.e., one containing a .gitlet subdirectory), but is not in such a directory, print the message "Not in an initialized Gitlet directory." and exit.

