import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Raman Varma
 */
public class Main {

    /** Gitlet hidden directory. */
    static final File REPO = new File(".gitlet");

    /** Staging area directory containing an addition and removal stage. */
    static final File STAGING = new File(REPO + "/staging");

    /** Addition stage directory. */
    static final File STAGING_ADD = new File(STAGING + "/addition");

    /** Removal stage directory. */
    static final File STAGING_REMOVE = new File(STAGING + "/removal");

    /** Commit directory containing all commits (immutable). */
    static final File COMMITS = new File(REPO + "/commits");

    /** A file that points to the head of the current branch. */
    static final File HEAD = new File(REPO + "/HEAD");

    /** Branches directory containing all branches stored as files. */
    static final File BRANCHES = new File(REPO + "/branches");

    /** Files directory containing all committed files. */
    static final File FILES = new File(REPO + "/files");

    /** Remotes directory containing a HashMap of all remote name to directory
     *  mappings.*/
    static final File REMOTES_MAP = new File(REPO + "/remotes");

    /** The default length of a full SHA1 ID. */
    static final int FULL_SHA1_LENGTH = 40;

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        initialChecks(args);
        String command = args[0];
        switch (command) {
        case "init":
            checkInit(args);
            break;
        case "add":
            checkAdd(args);
            break;
        case "commit":
            checkCommit(args);
            break;
        case "rm":
            checkRm(args);
            break;
        case "log":
            checkLog(args);
            break;
        case "global-log":
            checkGlobalLog(args);
            break;
        case "find":
            checkFind(args);
            break;
        case "status":
            checkStatus(args);
            break;
        case "checkout":
            checkCheckout(args);
            break;
        case "branch":
            checkBranch(args);
            break;
        case "rm-branch":
            checkRmBranch(args);
            break;
        case "reset":
            checkReset(args);
            break;
        case "merge":
            checkMerge(args);
            break;
        default:
            commandDoesNotExist();
            break;
        }
    }

    /** Checks for no command error case and not in an initialized
     *  gitlet directory error case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void initialChecks(String... args) {
        if (args.length == 0) {
            noCommand();
        }
        HashSet<String> commands = requireGitletDir();
        String command = args[0];
        if (!command.equals("init") && commands.contains(command)
                && !REPO.exists()) {
            noGitletDir();
        }
    }

    /** Checks init command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkInit(String... args) throws IOException {
        if (args.length != 1) {
            incorrectOperands();
        }
        init();
    }

    /** Checks add command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkAdd(String... args) {
        if (args.length != 2) {
            incorrectOperands();
        }
        add(args[1]);
    }

    /** Checks commit command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkCommit(String... args) throws IOException {
        if (args.length != 2) {
            incorrectOperands();
        } else if (args[1].equals("")) {
            noCommitMessage();
        } else {
            commit(args[1], null);
        }
    }

    /** Checks rm command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkRm(String... args) {
        if (args.length != 2) {
            incorrectOperands();
        }
        boolean isStaged = isStaged(args[1]);
        boolean isTracked = isTracked(args[1]);
        if (isStaged && isTracked) {
            rm(args[1], true, true);
        } else if (isStaged) {
            rm(args[1], true, false);
        } else if (isTracked) {
            rm(args[1], false, true);
        } else {
            noReasonToRm();
        }
    }

    /** Checks log command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkLog(String... args) {
        if (args.length != 1) {
            incorrectOperands();
        }
        log();
    }

    /** Checks global-log command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkGlobalLog(String... args) {
        if (args.length != 1) {
            incorrectOperands();
        }
        globalLog();
    }

    /** Checks find command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkFind(String... args) {
        if (args.length != 2) {
            incorrectOperands();
        }
        find(args[1]);
    }

    /** Checks status command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkStatus(String... args) {
        if (args.length != 1) {
            incorrectOperands();
        }
        status();
    }

    /** Checks checkout command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkCheckout(String... args) {
        if (args.length != 2 && args.length != 3 && args.length != 4) {
            incorrectOperands();
        }
        if (args.length == 2) {
            checkout3(args[1]);
        } else if (args[1].equals("--")) {
            checkout1(args[2]);
        } else if (args[2].equals("--")) {
            checkout2(args[1], args[3]);
        } else {
            incorrectOperands();
        }
    }

    /** Checks branch command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkBranch(String... args) {
        if (args.length != 2) {
            incorrectOperands();
        }
        branch(args[1]);
    }

    /** Checks rm-branch command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkRmBranch(String... args) {
        if (args.length != 2) {
            incorrectOperands();
        }
        rmBranch(args[1]);
    }

    /** Checks reset command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkReset(String... args) {
        if (args.length != 2) {
            incorrectOperands();
        }
        reset(args[1]);
    }

    /** Checks merge command input case.
     *  @param args - ARGS contains <COMMAND> <OPERAND> .... */
    public static void checkMerge(String... args) throws IOException {
        if (args.length != 2) {
            incorrectOperands();
        }
        merge(args[1]);
    }

    /** @return - Returns a HashSet of all the gitlet commands that require
     *  an initialized gitlet directory (all commands but init). */
    public static HashSet<String> requireGitletDir() {
        return new HashSet<>(Arrays.asList("add", "commit", "rm",
                "log", "global-log", "find", "status", "checkout", "branch",
                "rm-branch", "reset", "merge"));
    }

    /** Determines if a file with name fileName is staged for addition.
     *  @param fileName - The name of the file.
     *  @return - Whether file with name fileName is staged. */
    public static boolean isStaged(String fileName) {
        boolean isStaged = false;
        File[] stagedForAdd = STAGING_ADD.listFiles();
        if (stagedForAdd != null) {
            for (File f : stagedForAdd) {
                if (f.getName().equals(fileName)) {
                    isStaged = true;
                    break;
                }
            }
        }
        return isStaged;
    }

    /** Determines if a file with name fileName is tracked in the
     *  current commit (the head commit of the current branch).
     *  @param fileName - The name of the file.
     *  @return - Whether file with name fileName is tracked. */
    public static boolean isTracked(String fileName) {
        boolean isTracked = false;
        Commit headCommit = getHeadCommit();
        HashMap<String, String> trackedFiles = headCommit.getTrackedFiles();
        if (trackedFiles != null && trackedFiles.containsKey(fileName)) {
            isTracked = true;
        }
        return isTracked;
    }

    /** No command entered error case. */
    public static void noCommand() {
        System.out.println("Please enter a command.");
        System.exit(0);
    }

    /** No commit message error case for commit command. */
    public static void noCommitMessage() {
        System.out.println("Please enter a commit message.");
        System.exit(0);
    }

    /** Command requiring initialized gitlet directory error case. */
    public static void noGitletDir() {
        System.out.println("Not in an initialized Gitlet directory.");
        System.exit(0);
    }

    /** Incorrect operands error case. */
    public static void incorrectOperands() {
        System.out.println("Incorrect operands.");
        System.exit(0);
    }

    /** No reason to remove file error case for rm command. */
    public static void noReasonToRm() {
        System.out.println("No reason to remove the file.");
        System.exit(0);
    }

    /** Inputted command does not exist error case. */
    public static void commandDoesNotExist() {
        System.out.println("No command with that name exists.");
        System.exit(0);
    }

    /** Creates a new Gitlet version-control system in the current directory.
     *  This system will automatically start with one commit: a commit that
     *  contains no files and has the commit message "initial commit". It will
     *  have a single branch: master, which initially points to this initial
     *  commit, and master will be the current branch. The timestamp for this
     *  initial commit will be 00:00:00 UTC, Thursday, 1 January 1970. */
    public static void init() throws IOException {
        if (REPO.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        } else {
            REPO.mkdir();
            STAGING.mkdir();
            STAGING_ADD.mkdir();
            STAGING_REMOVE.mkdir();
            COMMITS.mkdir();
            BRANCHES.mkdir();
            HEAD.createNewFile();
            FILES.mkdir();
            REMOTES_MAP.mkdir();

            Date initialDate = new Date(0);
            String initialMessage = "initial commit";
            String parent = null;
            HashMap<String, String> trackedFiles = null;
            String mergedInParent = null;
            String cSHA1 = Utils.sha1(initialMessage, initialDate.toString());
            Commit initial = new Commit(parent, initialMessage, initialDate,
                    trackedFiles, mergedInParent, cSHA1);
            File initialFile = new File(COMMITS + "/" + cSHA1);
            Utils.writeObject(initialFile, initial);

            String pathToMaster = BRANCHES + "/master";
            File master = new File(pathToMaster);
            Utils.writeContents(master, cSHA1);
            File head = new File(String.valueOf(HEAD));
            Utils.writeContents(head, pathToMaster);
        }
    }

    /** Adds a copy of the file as it currently exists to the staging area.
     *  Staging an already-staged file overwrites the previous entry in the
     *  staging area with the new contents. If the current working version
     *  of the file is identical to the version in the current commit, do not
     *  stage it to be added, and remove it from the staging area if it is
     *  already there. The file will no longer be staged for removal, if it
     *  was at the time of the command.
     *  @param fileName - The name of the file to add. */
    public static void add(String fileName) {
        File addFile = new File(fileName);
        if (!addFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            byte[] addFileContents = Utils.readContents(addFile);
            String addFileSHA1 = Utils.sha1((Object) addFileContents);
            File copyFile = new File(STAGING_ADD + "/" + fileName);
            Commit headCommit = getHeadCommit();
            HashMap<String, String> headTrackedFiles =
                    headCommit.getTrackedFiles();
            if (headTrackedFiles != null
                    && headTrackedFiles.containsKey(fileName)
                    && headTrackedFiles.get(fileName).equals(addFileSHA1)) {
                if (copyFile.exists()) {
                    copyFile.delete();
                }
            } else {
                Utils.writeContents(copyFile, (Object) addFileContents);
            }
            File stagedForRemoval = new File(STAGING_REMOVE + "/" + fileName);
            if (stagedForRemoval.exists()) {
                stagedForRemoval.delete();
            }
        }
    }

    /** Saves a snapshot of tracked files in the current commit and staging
     *  area so they can be restored at a later time, creating a new commit.
     *  By default, each commit's snapshot of files will be exactly the same
     *  as its parent commit's snapshot of files; it will keep versions of
     *  files exactly as they are, and not update them. A commit will only
     *  update the contents of files it is tracking that have been staged for
     *  addition at the time of commit, in which case the commit will now
     *  include the version of the file that was staged instead of the version
     *  it got from its parent. A commit will save and start tracking any
     *  files that were staged for addition but weren't tracked by its parent.
     *  Files tracked in the current commit may be untracked in the new commit
     *  as a result being staged for removal by the rm command.
     *  @param message - The commit's log message.
     *  @param mergedInParent - The merged-in parent of the commit. Null if
     *  the commit does not have a merged-in parent. */
    public static void commit(String message, String mergedInParent)
            throws IOException {
        Date timestamp = new Date();
        if (Objects.requireNonNull(STAGING_ADD.listFiles()).length == 0
                && Objects.requireNonNull(STAGING_REMOVE.listFiles()).length == 0) {
            noChanges();
        } else {
            Commit headCommit = getHeadCommit();
            HashMap<String, String> trackedFilesHead = headCommit.getTrackedFiles();
            HashMap<String, String> clonedTrackedFiles = new HashMap<>();
            String parentSHA1;
            if (headCommit.getTrackedFiles() != null) {
                parentSHA1 = Utils.sha1(headCommit.getParent(),
                        headCommit.getMessage(),
                        headCommit.getTimestamp().toString(),
                        convertMapToByte(headCommit.getTrackedFiles()));
            } else {
                parentSHA1 = Utils.sha1(headCommit.getMessage(),
                        headCommit.getTimestamp().toString());
            }
            if (trackedFilesHead != null) {
                for (String fileName : trackedFilesHead.keySet()) {
                    String fSHA1 = trackedFilesHead.get(fileName);
                    clonedTrackedFiles.put(fileName, fSHA1);
                }
            }

            File[] stagedForAdd = STAGING_ADD.listFiles();
            if (stagedForAdd != null) {
                for (File add : stagedForAdd) {
                    String fileName = add.getName();
                    byte[] addsContent = Utils.readContents(add);
                    String fSHA1 = Utils.sha1((Object) addsContent);
                    if (clonedTrackedFiles.containsKey(fileName)) {
                        clonedTrackedFiles.replace(fileName, fSHA1);
                    } else {
                        clonedTrackedFiles.put(fileName, fSHA1);
                    }
                }
            }
            File[] stagedForRm = STAGING_REMOVE.listFiles();
            if (stagedForRm != null) {
                for (File rm: stagedForRm) {
                    String fileName = rm.getName();
                    clonedTrackedFiles.remove(fileName);
                }
            }

            String commitSHA1 = Utils.sha1(parentSHA1, message,
                    timestamp.toString(), convertMapToByte(clonedTrackedFiles));
            Commit commit = new Commit(parentSHA1, message, timestamp,
                    clonedTrackedFiles, mergedInParent, commitSHA1);
            File commitFile = new File(COMMITS + "/" + commitSHA1);
            Utils.writeObject(commitFile, commit);

            File[] currFiles = FILES.listFiles();
            for (String fileName: clonedTrackedFiles.keySet()) {
                String fileNameSHA1 = clonedTrackedFiles.get(fileName);
                boolean alreadyInFiles = false;
                if (currFiles != null) {
                    for (File f : currFiles) {
                        if (fileNameSHA1.equals(f.getName())) {
                            alreadyInFiles = true;
                            break;
                        }
                    }
                    if (!alreadyInFiles) {
                        File addNewCommitFile =
                                new File(FILES + "/" + fileNameSHA1);
                        if (stagedForAdd != null) {
                            for (File f : stagedForAdd) {
                                if (f.getName().equals(fileName)) {
                                    byte[] fileNameContents =
                                            Utils.readContents(f);
                                    Utils.writeContents(addNewCommitFile,
                                            (Object) fileNameContents);
                                }
                            }
                        }
                    }
                } else {
                    File addNewCommitFile =
                            new File(FILES + "/" + fileNameSHA1);
                    if (stagedForAdd != null) {
                        for (File f : stagedForAdd) {
                            if (f.getName().equals(fileName)) {
                                byte[] fileNameContents = Utils.readContents(f);
                                Utils.writeContents(addNewCommitFile,
                                        (Object) fileNameContents);
                            }
                        }
                    }
                }
            }
            if (stagedForAdd != null) {
                for (File fileInAdd : stagedForAdd) {
                    fileInAdd.delete();
                }
            }
            if (stagedForRm != null) {
                for (File fileInRm : stagedForRm) {
                    fileInRm.delete();
                }
            }
            String pathToHead = Utils.readContentsAsString(HEAD);
            File currHead = new File(pathToHead);
            Utils.writeContents(currHead, commitSHA1);
        }
    }

    /** No changes added to commit error case. */
    public static void noChanges() {
        System.out.println("No changes added to the commit.");
        System.exit(0);
    }

    /** Unstage the file if it is currently staged for addition. If the
     *  file is tracked in the current commit, stage it for removal and
     *  remove the file from the working directory if the user has not
     *  already done so. Do not remove it unless it is tracked in the
     *  current commit.
     *  @param fileName - The name of the file to remove.
     *  @param isStaged - Whether the file is staged.
     *  @param isTracked - Whether the file is tracked. */
    public static void rm(String fileName, boolean isStaged, boolean isTracked) {
        if (isStaged) {
            File rmStaged = new File(STAGING_ADD + "/" + fileName);
            rmStaged.delete();
        }
        if (isTracked) {
            String fileSHA1 = getHeadCommit().getTrackedFiles().get(fileName);
            File rmFile = new File(FILES + "/" + fileSHA1);
            byte[] fileToRmContents = Utils.readContents(rmFile);
            File stageForRm = new File(STAGING_REMOVE + "/" + fileName);
            Utils.writeContents(stageForRm, (Object) fileToRmContents);
            File removedFile = new File(fileName);
            if (removedFile.exists()) {
                Utils.restrictedDelete(removedFile);
            }
        }
    }

    /** Starting at the current head commit, display information about each
     *  commit backwards along the commit tree until the initial commit,
     *  following the first parent commit links, ignoring any second parents
     *  found in merge commits. For every node in this history, the
     *  information it should display is the commit id, the time the commit
     *  was made, and the commit message. */
    public static void log() {
        Commit c = getHeadCommit();
        while (c != null) {
            displayInfo(c, c.getMergedInParent() != null);
            String parentSHA1 = c.getParent();
            File parentCommitFile = new File(COMMITS + "/" + parentSHA1);
            if (parentCommitFile.exists()) {
                c = Utils.readObject(parentCommitFile, Commit.class);
            } else {
                c = null;
            }
        }
    }

    /** Like log, except displays information about all commits ever made.
     *  The order of the commits does not matter. */
    public static void globalLog() {
        File commitsDir = new File(COMMITS.toString());
        List<String> allCommits = Utils.plainFilenamesIn(commitsDir);
        if (allCommits != null) {
            for (String commitSHA1 : allCommits) {
                File commitFile = new File(COMMITS + "/" + commitSHA1);
                Commit c = Utils.readObject(commitFile, Commit.class);
                displayInfo(c, c.getMergedInParent() != null);
            }
        }
    }

    /** Displays the following information about commit C: the commit id,
     *  the time the commit was made, and the commit message. If the commit
     *  has a merged-in parent, also display the first 7 characters of its
     *  two parents' IDs on the second line.
     *  @param c - The commit object whose information is displayed.
     *  @param mergedParentExists - Whether the commit has a merged-in
     *  parent. */
    public static void displayInfo(Commit c, boolean mergedParentExists) {
        System.out.println("===");
        String commitSHA1;
        if (c.getTrackedFiles() != null) {
            commitSHA1 = Utils.sha1(c.getParent(), c.getMessage(),
                    c.getTimestamp().toString(),
                    convertMapToByte(c.getTrackedFiles()));
        } else {
            commitSHA1 = Utils.sha1(c.getMessage(),
                    c.getTimestamp().toString());
        }
        System.out.println("commit " + commitSHA1);
        if (mergedParentExists) {
            String firstParent = c.getParent().subSequence(0, 7).toString();
            String mergedInParent = c.getMergedInParent().subSequence(0, 7).toString();
            System.out.println("Merge: " + firstParent + " " + mergedInParent);
        }
        Date timestamp = c.getTimestamp();
        SimpleDateFormat formatTimestamp =
                new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        String requiredFormat = formatTimestamp.format(timestamp);
        System.out.println("Date: " + requiredFormat);
        System.out.println(c.getMessage() + System.lineSeparator());
    }

    /** Prints out the IDs of all commits that have the given commit
     *  message, one per line. If there are multiple such commits,
     *  it prints the ids out on separate lines.
     *  @param commitMessage - The given commit message. */
    public static void find(String commitMessage) {
        File commitsDir = new File(COMMITS.toString());
        File[] allCommitFiles = commitsDir.listFiles();
        boolean commitExists = false;
        if (allCommitFiles != null) {
            for (File f : allCommitFiles) {
                Commit c = Utils.readObject(f, Commit.class);
                String cCommitMessage = c.getMessage();
                if (commitMessage.equals(cCommitMessage)) {
                    if (!commitExists) {
                        commitExists = true;
                    }
                    System.out.println(f.getName());
                }
            }
            if (!commitExists) {
                System.out.println("Found no commit with that message.");
                System.exit(0);
            }
        } else {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** Displays what branches currently exist, and marks the current
     *  branch with a *. Also displays what files have been staged for
     *  addition or removal. */
    public static void status() {
        List<String> stagedFiles = Utils.plainFilenamesIn(STAGING_ADD);
        List<String> removedFiles = Utils.plainFilenamesIn(STAGING_REMOVE);
        List<String> untrackedFiles = new ArrayList<>();
        List<String> modifiedNotStaged = new ArrayList<>();

        Commit headCommit = getHeadCommit();
        HashMap<String, String> trackedFilesHead
                = headCommit.getTrackedFiles();
        Set<String> fileNamesHead = null;
        if (trackedFilesHead != null) {
            fileNamesHead = trackedFilesHead.keySet();
        }
        File cwd = new File(".");
        File[] workingFiles = cwd.listFiles();
        if (workingFiles != null) {
            for (File f : workingFiles) {
                if (f.isFile()) {
                    String fileName = f.getName();
                    boolean notStaged = stagedFiles != null
                            && !stagedFiles.contains(fileName);
                    boolean emptyOrNotInHead = fileNamesHead == null
                            || !fileNamesHead.contains(fileName);
                    boolean fileRemoved = removedFiles != null
                            && removedFiles.contains(fileName);
                    boolean emptyOrNotStaged = stagedFiles == null
                            || !stagedFiles.contains(fileName);
                    if ((notStaged && emptyOrNotInHead) ||
                            (fileRemoved && emptyOrNotStaged)) {
                        untrackedFiles.add(fileName);
                    }
                    byte[] fsContents = Utils.readContents(f);
                    String fSHA1 = Utils.sha1((Object) fsContents);
                    boolean notInHead = fileNamesHead != null
                            && fileNamesHead.contains(fileName);
                    if (notInHead && emptyOrNotStaged) {
                        String headFileSHA1 = trackedFilesHead.get(fileName);
                        if (!fSHA1.equals(headFileSHA1)) {
                            modifiedNotStaged.add(fileName + " (modified)");
                        }
                    }
                    if (!modifiedNotStaged.contains(fileName) && stagedFiles != null
                            && stagedFiles.contains(fileName)) {
                        File stagedFile = new File(STAGING_ADD + "/" + fileName);
                        byte[] stagedFileContents = Utils.readContents(stagedFile);
                        String stagedFileSHA1 = Utils.sha1((Object) stagedFileContents);
                        if (!fSHA1.equals(stagedFileSHA1)) {
                            modifiedNotStaged.add(fileName + " (modified)");
                        }
                    }
                }
            }
        }
        List<String> allBranches = Utils.plainFilenamesIn(BRANCHES);
        String currentBranchName = getCurrentBranch();
        if (allBranches != null) {
            for (int i = 0; i < allBranches.size(); i += 1) {
                if (allBranches.get(i).equals(currentBranchName)) {
                    allBranches.set(i, "*" + currentBranchName);
                    break;
                }
            }
        }
        if (workingFiles != null && stagedFiles != null) {
            List<String> stagedFilesCopy = new ArrayList<>(stagedFiles);
            for (File f : workingFiles) {
                String fileName = f.getName();
                stagedFilesCopy.remove(fileName);
            }
            for (String stagedFileName : stagedFilesCopy) {
                if (!modifiedNotStaged.contains(stagedFileName)) {
                    modifiedNotStaged.add(stagedFileName + " (deleted)");
                }
            }
        } else if (stagedFiles != null) {
            for (String stagedFileName: stagedFiles) {
                modifiedNotStaged.add(stagedFileName + " (deleted)");
            }
        }
        if (fileNamesHead != null) {
            if (workingFiles != null) {
                for (File f : workingFiles) {
                    fileNamesHead.remove(f.getName());
                }
            }
            for (String fileNameHead : fileNamesHead) {
                if (removedFiles == null
                        || !removedFiles.contains(fileNameHead)) {
                    modifiedNotStaged.add(fileNameHead + " (deleted)");
                }
            }
        }
        Collections.sort(untrackedFiles);
        Collections.sort(modifiedNotStaged);
        displayStatus(allBranches, stagedFiles, removedFiles,
                untrackedFiles, modifiedNotStaged);
    }

    /** Displays information about branches collected by the STATUS method.
     *  @param allBranches - A list of all of the branches.
     *  @param stagedFiles - A list of all of the files staged for addition.
     *  @param removedFiles - A list of all of the files staged for removal.
     *  @param untrackedFiles - A list of all of the untracked files.
     *  @param modifiedNotStaged - A list of all of the files that have been
     *  modified but not staged for commit. */
    public static void displayStatus(List<String> allBranches,
                                     List<String> stagedFiles,
                                     List<String> removedFiles,
                                     List<String> untrackedFiles,
                                     List<String> modifiedNotStaged) {
        System.out.println("=== Branches ===");
        displaySection(allBranches);
        System.out.println("=== Staged Files ===");
        displaySection(stagedFiles);
        System.out.println("=== Removed Files ===");
        displaySection(removedFiles);
        System.out.println("=== Modifications Not Staged For Commit ===");
        displaySection(modifiedNotStaged);
        System.out.println("=== Untracked Files ===");
        displaySection(untrackedFiles);
    }

    /** Prints out each individual section of the STATUS method. Each section
     *  is separated by a new line.
     *  @param section - The section of status to display. */
    public static void displaySection(List<String> section) {
        for (String s : section) {
            System.out.println(s);
        }
        System.out.print(System.lineSeparator());
    }

    /** File does not exist error case. */
    public static void noFile() {
        System.out.println("File does not exist in that commit.");
        System.exit(0);
    }

    /** Takes the version of the file as it exists in the head commit, the
     *  front of the current branch, and puts it in the working directory,
     *  overwriting the version of the file that's already there if there is
     *  one. The new version of the file is not staged.
     *  @param fileName - The name of the file to checkout. */
    public static void checkout1(String fileName) {
        Commit headCommit = getHeadCommit();
        HashMap<String, String> trackedFiles = headCommit.getTrackedFiles();
        if (trackedFiles.containsKey(fileName)) {
            String fileNameSHA1 = trackedFiles.get(fileName);
            File[] committedFiles = FILES.listFiles();
            if (committedFiles != null) {
                for (File f : committedFiles) {
                    if (fileNameSHA1.equals(f.getName())) {
                        byte[] fsContents = Utils.readContents(f);
                        File addToCWD = new File(fileName);
                        Utils.writeContents(addToCWD, (Object) fsContents);
                    }
                }
            }
        } else {
            noFile();
        }
    }

    /** Commit does not exist error case. */
    public static void noCommit() {
        System.out.println("No commit with that id exists.");
        System.exit(0);
    }

    /** Takes the version of the file as it exists in the commit with the
     *  given id, and puts it in the working directory, overwriting the
     *  version of the file that's already there if there is one. The new
     *  version of the file is not staged.
     *  @param fileName - The name of the file to checkout.
     *  @param commitID - The ID of the commit from which the file is taken. */
    public static void checkout2(String commitID, String fileName) {
        File[] allCommits = COMMITS.listFiles();
        if (allCommits != null) {
            boolean noCommitWithID = true;
            for (File commitFile : allCommits) {
                Commit commit = Utils.readObject(commitFile, Commit.class);
                String commitSHA1;
                if (commit.getTrackedFiles() != null) {
                    commitSHA1 = Utils.sha1(commit.getParent(),
                            commit.getMessage(),
                            commit.getTimestamp().toString(),
                            convertMapToByte(commit.getTrackedFiles()));
                } else {
                    commitSHA1 = Utils.sha1(commit.getMessage(),
                            commit.getTimestamp().toString());
                }
                String commitSHA1Shorten = "";
                if (commitID.length() < FULL_SHA1_LENGTH) {
                    commitSHA1Shorten
                            = (String) commitSHA1.subSequence(0, commitID.length());
                }
                boolean matchesShortenedID = !commitSHA1Shorten.isEmpty()
                        && commitSHA1Shorten.equals(commitID);
                if (commitSHA1.equals(commitID) || matchesShortenedID) {
                    noCommitWithID = false;
                    HashMap<String, String> trackedFiles = commit.getTrackedFiles();
                    if (trackedFiles.containsKey(fileName)) {
                        String fileNameSHA1 = trackedFiles.get(fileName);
                        File[] committedFiles = FILES.listFiles();
                        if (committedFiles != null) {
                            for (File f : committedFiles) {
                                if (fileNameSHA1.equals(f.getName())) {
                                    byte[] fsContents = Utils.readContents(f);
                                    File addToCWD = new File(fileName);
                                    Utils.writeContents(addToCWD, (Object) fsContents);
                                }
                            }
                        }
                    } else {
                        noFile();
                    }
                }
            }
            if (noCommitWithID) {
                noCommit();
            }
        } else {
            noCommit();
        }
    }

    /** Takes all files in the commit at the head of the given branch, and
     *  puts them in the working directory, overwriting the versions of the
     *  files that are already there if they exist. Also, at the end of this
     *  command, the given branch will now be considered the current branch
     *  (HEAD). Any files that are tracked in the current branch but are not
     *  present in the checked-out branch are deleted. The staging area is
     *  cleared, unless the checked-out branch is the current branch.
     *  @param branchName - The name of the branch being checked-out. */
    public static void checkout3(String branchName) {
        handleErrorsCheckout3(branchName);
        String pathToCheckedOutBranch = BRANCHES + "/" + branchName;
        File checkedOutBranch = new File(pathToCheckedOutBranch);
        String checkedOutCommitSHA1
                = Utils.readContentsAsString(checkedOutBranch);
        File[] allCommits = COMMITS.listFiles();
        Commit checkedOutCommit = null;
        if (allCommits != null) {
            for (File f : allCommits) {
                if (checkedOutCommitSHA1.equals(f.getName())) {
                    checkedOutCommit = Utils.readObject(f, Commit.class);
                    break;
                }
            }
            Commit headCommit = getHeadCommit();
            HashMap<String, String> trackedFilesHead
                    = headCommit.getTrackedFiles();
            Set<String> headFiles;
            if (trackedFilesHead == null) {
                headFiles = new HashSet<>();
            } else {
                headFiles = trackedFilesHead.keySet();
            }
            if (checkedOutCommit != null) {
                HashMap<String, String> trackedFilesCheckedOut
                        = checkedOutCommit.getTrackedFiles();
                Set<String> checkedOutFiles;
                if (trackedFilesCheckedOut != null) {
                    checkedOutFiles = trackedFilesCheckedOut.keySet();
                } else {
                    checkedOutFiles = new HashSet<>();
                }
                for (String fileName : checkedOutFiles) {
                    String fileNameSHA1 = trackedFilesCheckedOut.get(fileName);
                    File f = new File(FILES + "/" + fileNameSHA1);
                    byte[] fsContents = Utils.readContents(f);
                    File fInCWD = new File(fileName);
                    Utils.writeContents(fInCWD, fsContents);
                }
                for (String fileName : headFiles) {
                    if (!checkedOutFiles.contains(fileName)) {
                        File f = new File(fileName);
                        f.delete();
                    }
                }
            } else {
                for (String fileName : headFiles) {
                    File f = new File(fileName);
                    f.delete();
                }
            }
        }
        clearStagingArea();
        Utils.writeContents(HEAD, pathToCheckedOutBranch);
    }

    /** Clear the staging area (addition stage and removal stage). */
    public static void clearStagingArea() {
        File[] stagedForAdd = STAGING_ADD.listFiles();
        if (stagedForAdd != null) {
            for (File f : stagedForAdd) {
                f.delete();
            }
        }
        File[] stagedForRemoval = STAGING_REMOVE.listFiles();
        if (stagedForRemoval != null) {
            for (File f : stagedForRemoval) {
                f.delete();
            }
        }
    }

    /** Handles the following error cases for CHECKOUT3: If no branch with
     *  that name exists, print "No such branch exists". If that branch is
     *  the current branch, print "No need to checkout the current branch."
     *  If a working file is untracked in the current branch and would be
     *  overwritten by the checkout, print "There is an untracked file in
     *  the way; delete it, or add and commit it first." and exit.
     *  @param branchName - The name of the branch. */
    public static void handleErrorsCheckout3(String branchName) {
        File branchesDir = new File(BRANCHES.toString());
        List<String> allBranchNames = Utils.plainFilenamesIn(branchesDir);
        if (allBranchNames != null && !allBranchNames.contains(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        File head = new File(HEAD.toString());
        String pathToBranch = Utils.readContentsAsString(head);
        File currBranch = new File(pathToBranch);
        if (branchName.equals(currBranch.getName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        File checkedOutBranch = new File(BRANCHES + "/" + branchName);
        String checkedOutCommitSHA1 = Utils.readContentsAsString(checkedOutBranch);
        File checkedOutCommitFile
                = new File(COMMITS + "/" + checkedOutCommitSHA1);
        Commit checkedOutCommit
                = Utils.readObject(checkedOutCommitFile, Commit.class);
        if (checkedOutCommit != null) {
            Commit headCommit = getHeadCommit();
            HashMap<String, String> trackedFilesHead = headCommit.getTrackedFiles();
            HashMap<String, String> trackedFilesCheckedOut
                    = checkedOutCommit.getTrackedFiles();
            File cwd = new File(".");
            File[] workingFiles = cwd.listFiles();
            if (workingFiles != null) {
                for (File f : workingFiles) {
                    String fileName = f.getName();
                    boolean notInHead = trackedFilesHead == null
                            || !trackedFilesHead.containsKey(fileName);
                    boolean inCheckedOut = trackedFilesCheckedOut != null
                            && trackedFilesCheckedOut.containsKey(fileName);
                    if (notInHead && inCheckedOut) {
                        System.out.println("There is an untracked file in the"
                                + " way; delete it, or add "
                                + "and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
    }

    /** Creates a new branch with the given name, and points it at the
     *  current head node. This command does NOT immediately switch to
     *  the newly created branch.
     *  @param branchName - The name of the new branch. */
    public static void branch(String branchName) {
        File branchesDir = new File(BRANCHES.toString());
        List<String> allBranchNames = Utils.plainFilenamesIn(branchesDir);
        if (allBranchNames != null && allBranchNames.contains(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File head = new File(String.valueOf(HEAD));
        String pathToHeadCommit = Utils.readContentsAsString(head);
        File branchOfHead = new File(pathToHeadCommit);
        String headSHA1 = Utils.readContentsAsString(branchOfHead);

        String pathToNewBranch = BRANCHES + "/" + branchName;
        File newBranch = new File(pathToNewBranch);
        Utils.writeContents(newBranch, headSHA1);
    }

    /** Deletes the branch with the given name. This means to delete
     *  the pointer associated with the branch; it does not mean to
     *  delete all commits that were created under the branch.
     *  @param branchName - The name of the branch to remove. */
    public static void rmBranch(String branchName) {
        File removeBranch = new File(BRANCHES + "/" + branchName);
        if (!removeBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String pathToCurrBranch = Utils.readContentsAsString(HEAD);
        File currBranch = new File(pathToCurrBranch);
        if (branchName.equals(currBranch.getName())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        removeBranch.delete();
    }

    /** Checks out all the files tracked by the given commit. Removes
     *  tracked files that are not present in that commit. Also moves
     *  the current branch's head to that commit node. The commit id
     *  may be abbreviated as for checkout. The staging area is
     *  cleared.
     *  @param commitID - The ID of the given commit. */
    public static void reset(String commitID) {
        String fullSHA1 = "";
        if (commitID.length() < FULL_SHA1_LENGTH) {
            File[] allCommits = COMMITS.listFiles();
            if (allCommits != null) {
                for (File commit : allCommits) {
                    String commitName = commit.getName();
                    if (commitName.contains(commitID)) {
                        fullSHA1 = commitName;
                        break;
                    }
                }
            }
        } else {
            File commitIDFile = new File(COMMITS + "/" + commitID);
            if (commitIDFile.exists()) {
                fullSHA1 = commitID;
            }
        }
        if (fullSHA1.isEmpty()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        File checkedOutCommitFile = new File(COMMITS + "/" + fullSHA1);
        Commit checkedOutCommit
                = Utils.readObject(checkedOutCommitFile, Commit.class);
        handleError2Reset(checkedOutCommit);

        HashMap<String, String> trackedFilesCheckedOut
                = checkedOutCommit.getTrackedFiles();
        Set<String> checkedOutFiles = trackedFilesCheckedOut.keySet();
        for (String fileName : checkedOutFiles) {
            String fileNameSHA1 = trackedFilesCheckedOut.get(fileName);
            File f = new File(FILES + "/" + fileNameSHA1);
            byte[] fsContents = Utils.readContents(f);
            File fInCWD = new File(fileName);
            Utils.writeContents(fInCWD, (Object) fsContents);
        }
        Commit headCommit = getHeadCommit();
        HashMap<String, String> trackedFilesHead = headCommit.getTrackedFiles();
        Set<String> headFiles = trackedFilesHead.keySet();
        for (String fileName : headFiles) {
            if (!checkedOutFiles.contains(fileName)) {
                File f = new File(fileName);
                f.delete();
            }
        }
        String currBranchName = getCurrentBranch();
        File currBranch = new File(BRANCHES + "/" + currBranchName);
        Utils.writeContents(currBranch, fullSHA1);
        clearStagingArea();
    }

    /** If a working file is untracked in the current branch and would
     *  be overwritten by the reset, print "There is an untracked file
     *  in the way; delete it, or add and commit it first." and exit.
     *  @param checkedOutCommit - The checked-out commit object. */
    public static void handleError2Reset(Commit checkedOutCommit) {
        Commit headCommit = getHeadCommit();
        HashMap<String, String> trackedFilesHead = headCommit.getTrackedFiles();
        HashMap<String, String> trackedFilesCheckedOut
                = checkedOutCommit.getTrackedFiles();
        File cwd = new File(".");
        File[] workingFiles = cwd.listFiles();
        if (workingFiles != null) {
            for (File f : workingFiles) {
                if (f.isFile()) {
                    String fileName = f.getName();
                    boolean trackedByHead = trackedFilesHead.containsKey(fileName);
                    boolean fInCheckedOutCommit
                            = trackedFilesCheckedOut.containsKey(fileName);
                    File stagedForAdd
                            = new File(STAGING_ADD + "/" + fileName);
                    boolean isStagedForAdd = stagedForAdd.exists();
                    if (!trackedByHead && fInCheckedOutCommit && !isStagedForAdd) {
                        System.out.println("There is an untracked file in the "
                                + "way; delete it, or add "
                                + "and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
    }

    /** Merges files from the given branch into the current branch.
     *  @param branchName - The name of the given branch. */
    public static void merge(String branchName) throws IOException {
        handleErrorsMerge(branchName);
        Commit head = getHeadCommit();
        File headCommitFile = new File(BRANCHES + "/" + getCurrentBranch());
        String headSHA1 = Utils.readContentsAsString(headCommitFile);

        File branchFile = new File(BRANCHES + "/" + branchName);
        String branchSHA1 = Utils.readContentsAsString(branchFile);
        File branchHeadCommitFile = new File(COMMITS + "/" + branchSHA1);
        Commit branch = Utils.readObject(branchHeadCommitFile, Commit.class);

        HashMap<Commit, Integer> markedByHead = getMarkedCommits(head);
        HashMap<Commit, Integer> markedByBranch = getMarkedCommits(branch);
        Commit splitPoint = findUnion(markedByHead, markedByBranch);
        String splitPointSHA1 = splitPoint.getCommitSHA1();
        if (splitPointSHA1.equals(branchSHA1)) {
            printAncestorMessage();
        } else if (splitPointSHA1.equals(headSHA1)) {
            checkout3(branchName);
            System.out.println("Current branch fast-forwarded.");
        } else {
            HashSet<String> modInBranchSinceSplit = new HashSet<>();
            HashMap<String, String> trackedFilesBranch = branch.getTrackedFiles();
            Set<String> trackedFilesBranchNames = trackedFilesBranch.keySet();
            HashMap<String, String> trackedFilesSplitPoint
                    = splitPoint.getTrackedFiles();
            HashMap<String, String> trackedFilesHead = head.getTrackedFiles();
            mergeCase1(branchSHA1, trackedFilesSplitPoint, trackedFilesBranch,
                    trackedFilesBranchNames, trackedFilesHead,
                    modInBranchSinceSplit);
            mergeCase2(trackedFilesBranchNames, trackedFilesHead,
                    trackedFilesSplitPoint, branchSHA1);
            boolean encounteredConflict = mergeCase3And4(false,
                    trackedFilesSplitPoint, trackedFilesHead,
                    trackedFilesBranch);
            for (String fileName : trackedFilesBranchNames) {
                boolean notAtSplitPoint = trackedFilesSplitPoint == null
                        || !trackedFilesSplitPoint.containsKey(fileName);
                if (notAtSplitPoint
                        && trackedFilesHead.containsKey(fileName)) {
                    String fileSHA1AtHead = trackedFilesHead.get(fileName);
                    String fileSHA1AtBranch = trackedFilesBranch.get(fileName);
                    if (!fileSHA1AtHead.equals(fileSHA1AtBranch)) {
                        replaceContents(fileName, fileSHA1AtHead,
                                fileSHA1AtBranch);
                        add(fileName);
                        encounteredConflict = true;
                    }
                }
            }
            String logMessage = "Merged " + branchName + " into "
                    + headCommitFile.getName() + ".";
            commit(logMessage, branchSHA1);
            if (encounteredConflict) {
                System.out.println("Encountered a merge conflict.");
            }
        }
    }

    /** Print message for merge method. */
    public static void printAncestorMessage() {
        System.out.println("Given branch is an "
                + "ancestor of the current branch.");
    }

    /** Any files that have been modified in the given branch since the split
     *  point, but not modified in the current branch since the split point
     *  should be changed to their versions in the given branch (checked out
     *  from the commit at the front of the given branch). These files should
     *  then all be automatically staged.
     *  @param branchSHA1 - The SHA1 ID of the given branch.
     *  @param trackedFilesSplitPoint - A HashMap of the files tracked by the
     *  commit at the split point, where the keys are file names and values are
     *  file IDs.
     *  @param trackedFilesBranch - A HashMap of the files tracked by the
     *  commit at the given branch, where the keys are file names and values are
     *  file IDs.
     *  @param trackedFilesBranchNames - The keyset of TRACKEDFILESBRANCH.
     *  @param trackedFilesHead - A HashMap of the files tracked by the head
     *  commit, where the keys are file names and values are file IDs.
     *  @param modInBranchSinceSplit - A HashSet of the files modified in the
     *  given branch since the split point. */
    public static void mergeCase1(String branchSHA1,
                                  HashMap<String, String> trackedFilesSplitPoint,
                                  HashMap<String, String> trackedFilesBranch,
                                  Set<String> trackedFilesBranchNames,
                                  HashMap<String, String> trackedFilesHead,
                                  HashSet<String> modInBranchSinceSplit) {
        if (trackedFilesSplitPoint != null) {
            for (String fileName : trackedFilesBranchNames) {
                if (trackedFilesSplitPoint.containsKey(fileName)) {
                    String fileContentBranch = trackedFilesBranch.get(fileName);
                    String fileContentSplitPoint
                            = trackedFilesSplitPoint.get(fileName);
                    if (!fileContentBranch.equals(fileContentSplitPoint)) {
                        modInBranchSinceSplit.add(fileName);
                    }
                }
            }
        }
        HashSet<String> notModInHeadSinceSplit = new HashSet<>();
        for (String fileName : modInBranchSinceSplit) {
            if (trackedFilesHead.containsKey(fileName)) {
                String fileContentHead = trackedFilesHead.get(fileName);
                String fileContentSplitPoint
                        = trackedFilesSplitPoint.get(fileName);
                if (fileContentHead.equals(fileContentSplitPoint)) {
                    notModInHeadSinceSplit.add(fileName);
                }
            }
        }
        for (String fileName : notModInHeadSinceSplit) {
            checkout2(branchSHA1, fileName);
            add(fileName);
        }
    }

    /** Any files that were not present at the split point and are present
     *  only in the given branch should be checked out and staged.
     *  @param trackedFilesBranchNames - The names of the files tracked by
     *  the given branch.
     *  @param trackedFilesHead - A HashMap of the files tracked by the head
     *  commit, where the keys are file names and values are file IDs.
     *  @param trackedFilesSplitPoint - A HashMap of the files tracked by the
     *  commit at the split point, where the keys are file names and values are
     *  file IDs.
     *  @param branchSHA1 - The SHA1 ID of the given branch. */
    public static void mergeCase2(Set<String> trackedFilesBranchNames,
                                  HashMap<String, String> trackedFilesHead,
                                  HashMap<String, String> trackedFilesSplitPoint,
                                  String branchSHA1) {
        for (String fileName : trackedFilesBranchNames) {
            boolean notAtSplitPoint = trackedFilesSplitPoint == null
                    || !trackedFilesSplitPoint.containsKey(fileName);
            if (!trackedFilesHead.containsKey(fileName) && notAtSplitPoint) {
                checkout2(branchSHA1, fileName);
                add(fileName);
            }
        }
    }

    /** Takes care of the following possible cases of merging: Any files
     *  present at the split point, unmodified in the current branch,
     *  and absent in the given branch should be removed (and untracked);
     *  Any files in the current and given branches that are changed (with
     *  regards to the split point) and are different from one another,
     *  or the contents of one are changed and the other file is deleted,
     *  should have its contents replaced in the working directory and staged
     *  for addition.
     *  @param encounteredConflict - Whether a merge conflict has been
     *  encountered: Initialized to false.
     *  @param trackedFilesSplitPoint - A HashMap of the files tracked by the
     *  commit at the split point, where the keys are file names and values are
     *  file IDs.
     *  @param trackedFilesHead - A HashMap of the files tracked by the head
     *  commit, where the keys are file names and values are file IDs.
     *  @param trackedFilesBranch - A HashMap of the files tracked by the
     *  commit at the given branch, where the keys are file names and values are
     *  file IDs.
     *  @return Whether a merge conflict was encountered. */
    public static boolean mergeCase3And4(boolean encounteredConflict,
                                         HashMap<String, String> trackedFilesSplitPoint,
                                         HashMap<String, String> trackedFilesHead,
                                         HashMap<String, String> trackedFilesBranch) {
        if (trackedFilesSplitPoint != null) {
            Set<String> trackedFileNamesSplitPoint
                    = trackedFilesSplitPoint.keySet();
            for (String fileName : trackedFileNamesSplitPoint) {
                String fileSHA1AtSplit = trackedFilesSplitPoint.get(fileName);
                if (trackedFilesHead.containsKey(fileName)
                        && !trackedFilesBranch.containsKey(fileName)) {
                    String fileSHA1AtHead = trackedFilesHead.get(fileName);
                    if (fileSHA1AtSplit.equals(fileSHA1AtHead)) {
                        File isStaged
                                = new File(STAGING_ADD + "/" + fileName);
                        rm(fileName, isStaged.exists(), true);
                    }
                }
            }
            for (String fileNameAtSplit : trackedFileNamesSplitPoint) {
                String fileSHA1AtSplit = trackedFilesSplitPoint.get(fileNameAtSplit);
                boolean atHeadCommit = trackedFilesHead.containsKey(fileNameAtSplit);
                boolean atBranchCommit
                        = trackedFilesBranch.containsKey(fileNameAtSplit);
                if (atHeadCommit && atBranchCommit) {
                    String fileSHA1AtHead = trackedFilesHead.get(fileNameAtSplit);
                    String fileSHA1AtBranch = trackedFilesBranch.get(fileNameAtSplit);
                    if (!fileSHA1AtSplit.equals(fileSHA1AtHead)
                            && !fileSHA1AtSplit.equals(fileSHA1AtBranch)) {
                        if (!fileSHA1AtHead.equals(fileSHA1AtBranch)) {
                            replaceContents(fileNameAtSplit, fileSHA1AtHead,
                                    fileSHA1AtBranch);
                            add(fileNameAtSplit);
                            encounteredConflict = true;
                        }
                    }
                } else if (atHeadCommit) {
                    String fSHA1AtHead = trackedFilesHead.get(fileNameAtSplit);
                    if (!fileSHA1AtSplit.equals(fSHA1AtHead)) {
                        replaceContents(fileNameAtSplit,
                                fSHA1AtHead, null);
                        add(fileNameAtSplit);
                        encounteredConflict = true;
                    }
                } else if (atBranchCommit) {
                    String fileSHA1AtBranch = trackedFilesBranch.get(fileNameAtSplit);
                    if (!fileSHA1AtSplit.equals(fileSHA1AtBranch)) {
                        replaceContents(fileNameAtSplit, null,
                                fileSHA1AtBranch);
                        add(fileNameAtSplit);
                        encounteredConflict = true;
                    }
                }
            }
        }
        return encounteredConflict;
    }

    /** Replaces the contents of conflicting files, which are any files
     *  modified in different ways in the current and given branches.
     *  @param fileName - The name of the conflicting file whose contents
     *  should be replaced.
     *  @param fileSHA1AtHead - The SHA1 ID of the file at the head commit
     *  of the current branch.
     *  @param fileSHA1AtBranch - The SHA1 ID of the file at the head commit
     *  of the given branch. */
    public static void replaceContents(String fileName, String fileSHA1AtHead,
                                       String fileSHA1AtBranch) {
        String contentsAtHead;
        if (fileSHA1AtHead == null) {
            contentsAtHead = "";
        } else {
            File fileAtHead = new File(FILES + "/" + fileSHA1AtHead);
            contentsAtHead = Utils.readContentsAsString(fileAtHead);
        }
        String contentsAtBranch;
        if (fileSHA1AtBranch == null) {
            contentsAtBranch = "";
        } else {
            File fileAtBranch = new File(FILES + "/" + fileSHA1AtBranch);
            contentsAtBranch = Utils.readContentsAsString(fileAtBranch);
        }
        String concatenatedContents = "<<<<<<< HEAD" + System.lineSeparator()
                + contentsAtHead + "=======" + System.lineSeparator()
                + contentsAtBranch + ">>>>>>>" + System.lineSeparator();
        File fileToReplace = new File(fileName);
        Utils.writeContents(fileToReplace, concatenatedContents);
    }

    /** Performs a depth-first traversal of the directed acyclic graph
     *  of commits, starting at commit node C.
     *  @return A HashMap containing every commit node reachable from
     *  commit C as keys and their distances from commit C as values.
     *  @param c - The starting commit node of the traversal. */
    public static HashMap<Commit, Integer> getMarkedCommits(Commit c) {
        Stack<ArrayList<Object>> fringe = new Stack<>();
        ArrayList<Object> first = new ArrayList<>();
        first.add(c);
        first.add(0);
        fringe.push(first);
        HashMap<Commit, Integer> markedCommits = new HashMap<>();
        ArrayList<Commit> edges;
        int count;

        while (!fringe.isEmpty()) {
            ArrayList<Object> commitCountPair = fringe.pop();
            Commit v = (Commit) commitCountPair.get(0);
            count = (int) commitCountPair.get(1);
            if (!markedCommits.containsKey(v)) {
                markedCommits.put(v, count);
                edges = getEdges(v);
                if (!edges.isEmpty()) {
                    count += 1;
                }
                for (Commit w : edges) {
                    if (!markedCommits.containsKey(w)) {
                        ArrayList<Object> addToFringe = new ArrayList<>();
                        addToFringe.add(w);
                        addToFringe.add(count);
                        fringe.push(addToFringe);
                    }
                }
            }
        }
        return markedCommits;
    }

    /** Finds all of the edges (parents) of commit V. If commit V has
     *  a merged-in parent, it will have two edges; otherwise, V will
     *  have a single edge.
     *  @param v - The commit object whose edges are to be found.
     *  @return An ArrayList containing all of the edges of commit V. */
    public static ArrayList<Commit> getEdges(Commit v) {
        ArrayList<Commit> edges = new ArrayList<>();
        if (v != null) {
            if (v.getParent() != null) {
                String vParentSHA1 = v.getParent();
                File vParentFile = new File(COMMITS + "/" + vParentSHA1);
                Commit vParent = Utils.readObject(vParentFile, Commit.class);
                edges.add(vParent);
            }
            if (v.getMergedInParent() != null) {
                String vMergedInParentSHA1 = v.getMergedInParent();
                File vMergedInParentFile
                        = new File(COMMITS + "/" + vMergedInParentSHA1);
                Commit vMergedInParent
                        = Utils.readObject(vMergedInParentFile, Commit.class);
                edges.add(vMergedInParent);
            }
        }
        return edges;
    }

    /** Finds all of the commit nodes that are reachable from both the
     *  head commit of the current branch and the head commit of the
     *  given branch. Returns the commit that is closest to the head
     *  commit of the current branch.
     *  @param headMarked - A HashMap containing commit nodes reachable
     *  from the head commit of the current branch as keys and their
     *  distances from the head commit of the current branch as values.
     *  @param branchMarked - A HashMap containing commit nodes reachable
     *  from the head commit of the given branch as keys. Its values are
     *  ignored. */
    public static Commit findUnion(HashMap<Commit, Integer> headMarked,
                                 HashMap<Commit, Integer> branchMarked) {
        Set<Commit> headMarkedCommits = new HashSet<>(headMarked.keySet());
        Set<String> branchMarkedSHA1s = new HashSet<>();
        Set<Commit> unionMarkedCommits = new HashSet<>();
        Set<Commit> branchMarkedCommits = new HashSet<>(branchMarked.keySet());

        for (Commit c : branchMarkedCommits) {
            branchMarkedSHA1s.add(c.getCommitSHA1());
        }
        for (Commit c : headMarkedCommits) {
            String cSHA1 = c.getCommitSHA1();
            if (branchMarkedSHA1s.contains(cSHA1)) {
                unionMarkedCommits.add(c);
            }
        }
        int minVal = Integer.MAX_VALUE;
        Commit minCommit = null;
        for (Commit c : unionMarkedCommits) {
            int distance = headMarked.get(c);
            if (distance < minVal) {
                minVal = distance;
                minCommit = c;
            }
        }
        return minCommit;
    }

    /** If there are staged additions or removals present, print the
     *  error message "You have uncommitted changes." and exit. If a
     *  branch with the given name does not exist, print the error
     *  message "A branch with that name does not exist." If
     *  attempting to merge a branch with itself, print the error
     *  message "Cannot merge a branch with itself."
     *  @param branchName - The name of the given branch. */
    public static void handleErrorsMerge(String branchName) {
        boolean nonEmptyAddStage
                = Objects.requireNonNull(STAGING_ADD.listFiles()).length != 0;
        boolean nonEmptyRemoveStage
                = Objects.requireNonNull(STAGING_REMOVE.listFiles()).length != 0;
        if (nonEmptyAddStage || nonEmptyRemoveStage) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        File givenBranch = new File(BRANCHES + "/" + branchName);
        if (!givenBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(getCurrentBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String branchCommitSHA1 = Utils.readContentsAsString(givenBranch);
        File branchCommitFile = new File(COMMITS + "/" + branchCommitSHA1);
        Commit branch = Utils.readObject(branchCommitFile, Commit.class);
        if (branch != null) {
            Commit headCommit = getHeadCommit();
            HashMap<String, String> trackedFilesHead = headCommit.getTrackedFiles();
            HashMap<String, String> trackedFilesBranch = branch.getTrackedFiles();
            File cwd = new File(".");
            File[] workingFiles = cwd.listFiles();
            if (workingFiles != null) {
                for (File f : workingFiles) {
                    String fileName = f.getName();
                    boolean notInHead = trackedFilesHead == null
                            || !trackedFilesHead.containsKey(fileName);
                    boolean inBranch = trackedFilesBranch != null
                            && trackedFilesBranch.containsKey(fileName);
                    if (notInHead && inBranch) {
                        System.out.println("There is an untracked file in the w"
                                + "ay; delete it, or add and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
    }

    /** @param trackedFiles - The HashMap to be converted to a byte
     *  array.
     *  @return The byte array representation of TRACKEDFILES. */
    public static byte[] convertMapToByte(HashMap<String, String> trackedFiles) {
        File containHashMap = new File(REPO + "/hashMapConvert");
        Utils.writeObject(containHashMap, trackedFiles);
        return Utils.readContents(containHashMap);
    }

    /** @return The head commit of the current branch. */
    public static Commit getHeadCommit() {
        File pathToHeadFile = new File(String.valueOf(HEAD));
        String pathToHeadStr = Utils.readContentsAsString(pathToHeadFile);
        File headCommitBranchFile = new File(pathToHeadStr);
        String headCommitSHA1 = Utils.readContentsAsString(headCommitBranchFile);
        File headCommitFile = new File(COMMITS + "/" + headCommitSHA1);
        return Utils.readObject(headCommitFile, Commit.class);
    }

    /** @return The name of the current branch. */
    public static String getCurrentBranch() {
        File pathToHeadFile = new File(String.valueOf(HEAD));
        String pathToHeadStr = Utils.readContentsAsString(pathToHeadFile);
        File headCommitBranchFile = new File(pathToHeadStr);
        return headCommitBranchFile.getName();
    }
}
