package com.directorytree;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * DirectoryTree generates a tree-like representation of a directory structure.
 * This utility provides a visual representation of files and directories
 * hierarchy,
 * similar to the 'tree' command in Unix-like systems.
 * 
 * Features:
 * - Handles symbolic links and prevents infinite loops
 * - Customizable file/directory filtering
 * - Colored output for different file types
 * - Supports both interactive mode and command-line arguments
 * - Configurable maximum depth
 * - Uses Unicode characters for visual tree structure
 * 
 * Usage examples:
 * 1. Interactive: java -jar directory-tree.jar
 * 2. With arguments: java -jar directory-tree.jar -p /path/to/directory -d 3 -c
 * true
 *
 * @author EmadBytes
 * @version 2.0
 * @since 2024-11-24
 */
public class DirectoryTree {
    // Tree structure characters
    private static final String BRANCH = "├── ";
    private static final String LAST_BRANCH = "└── ";
    private static final String VERTICAL = "│   ";
    private static final String SPACE = "    ";

    // ANSI color codes for terminal output
    private static final String RESET = "\u001B[0m";
    private static final String DIRECTORY_COLOR = "\u001B[1;34m"; // Bold blue
    private static final String EXECUTABLE_COLOR = "\u001B[1;32m"; // Bold green
    private static final String IMAGE_COLOR = "\u001B[1;35m"; // Bold magenta
    private static final String TEXT_COLOR = "\u001B[1;33m"; // Bold yellow

    // Configuration options
    private boolean useColors = true;
    private int maxDepth = -1; // -1 means no limit
    private Set<String> excludedDirs = new HashSet<>(Arrays.asList(
            "node_modules", "target", ".git", "build", "dist"));
    private boolean showHidden = false;

    /**
     * Main entry point of the application.
     * Handles command-line arguments or runs in interactive mode.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        DirectoryTree app = new DirectoryTree();

        if (args.length > 0) {
            // Parse command-line arguments
            app.parseArguments(args);
        } else {
            // Run in interactive mode
            app.runInteractive();
        }
    }

    /**
     * Parses command-line arguments to configure the application.
     *
     * @param args Command-line arguments
     */
    private void parseArguments(String[] args) {
        String path = ".";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-p", "--path" -> {
                    if (i + 1 < args.length) {
                        path = args[++i];
                    }
                }
                case "-d", "--depth" -> {
                    if (i + 1 < args.length) {
                        try {
                            maxDepth = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid depth value: " + args[i]);
                        }
                    }
                }
                case "-c", "--color" -> {
                    if (i + 1 < args.length) {
                        useColors = Boolean.parseBoolean(args[++i]);
                    }
                }
                case "-h", "--show-hidden" -> showHidden = true;
                case "--help" -> {
                    printHelp();
                    return;
                }
                case "-e", "--exclude" -> {
                    if (i + 1 < args.length) {
                        String[] dirs = args[++i].split(",");
                        excludedDirs.addAll(Arrays.asList(dirs));
                    }
                }
            }
        }

        processDirectory(path);
    }

    /**
     * Runs the application in interactive mode with user input.
     */
    private void runInteractive() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Directory Tree Generator v2.0");
            System.out.println("------------------------------");

            // Get path
            System.out.println("Enter directory path (e.g., C:\\Users\\YourName\\Documents or .):");
            String path = scanner.nextLine();

            // Get max depth
            System.out.println("Enter maximum depth (-1 for unlimited):");
            try {
                maxDepth = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input, using default: no limit");
                maxDepth = -1;
            }

            // Ask for colors
            System.out.println("Use colors? (y/n):");
            useColors = scanner.nextLine().toLowerCase().startsWith("y");

            // Ask for hidden files
            System.out.println("Show hidden files? (y/n):");
            showHidden = scanner.nextLine().toLowerCase().startsWith("y");

            // Ask for additional directories to exclude
            System.out.println("Enter additional directories to exclude (comma-separated, or press Enter to skip):");
            String excludeInput = scanner.nextLine();
            if (!excludeInput.isEmpty()) {
                String[] dirs = excludeInput.split(",");
                for (String dir : dirs) {
                    excludedDirs.add(dir.trim());
                }
            }

            processDirectory(path);
        }
    }

    /**
     * Processes the specified directory and prints its tree structure.
     *
     * @param path Path to the directory to process
     */
    private void processDirectory(String path) {
        File directory = new File(path);

        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Error: Invalid directory path: " + path);
            return;
        }

        System.out.println("\nDirectory Tree for: " + directory.getAbsolutePath());
        System.out.println("Maximum depth: " + (maxDepth == -1 ? "unlimited" : maxDepth));
        System.out.println("Excluded directories: " + excludedDirs);
        System.out.println();

        // Print the root directory name with appropriate color
        if (useColors) {
            System.out.println(DIRECTORY_COLOR + directory.getName() + RESET);
        } else {
            System.out.println(directory.getName());
        }

        // Start recursive traversal
        Set<String> visitedPaths = new HashSet<>();
        printDirectoryTree(directory, "", true, visitedPaths, 0);
    }

    /**
     * Recursively traverses and prints the directory tree structure.
     *
     * @param directory    Current directory being processed
     * @param prefix       Current line prefix for proper indentation
     * @param isRoot       Flag indicating if this is the root directory
     * @param visitedPaths Set of paths already visited to prevent cycles
     * @param depth        Current depth in the directory tree
     */
    private void printDirectoryTree(File directory, String prefix, boolean isRoot,
            Set<String> visitedPaths, int depth) {
        // Check depth limit
        if (maxDepth != -1 && depth > maxDepth) {
            return;
        }

        try {
            // Get canonical path to handle symbolic links properly
            String canonicalPath = directory.getCanonicalPath();

            // Check for circular references
            if (!visitedPaths.add(canonicalPath)) {
                printWithColor(prefix + BRANCH + directory.getName() + " (symbolic link)", null);
                return;
            }

            // Get list of files and directories
            File[] files = directory.listFiles();
            if (files == null) {
                return;
            }

            // Filter and sort files
            List<File> fileList = filterFiles(files);
            int total = fileList.size();

            // Process each file/directory
            for (int i = 0; i < total; i++) {
                File file = fileList.get(i);
                boolean isLast = (i == total - 1);

                printFile(file, prefix, isLast);

                // Recursively process subdirectories
                if (file.isDirectory()) {
                    String newPrefix = prefix + (isLast ? SPACE : VERTICAL);
                    printDirectoryTree(file, newPrefix, false, visitedPaths, depth + 1);
                }
            }
        } catch (IOException e) {
            System.out.println(prefix + BRANCH + "Error accessing: " + directory.getName());
        }
    }

    /**
     * Filters files according to configuration settings.
     *
     * @param files Array of files to filter
     * @return List of files after filtering
     */
    private List<File> filterFiles(File[] files) {
        List<File> fileList = new ArrayList<>();

        for (File file : files) {
            String fileName = file.getName();

            // Skip based on configuration
            if ((!showHidden && (file.isHidden() || fileName.startsWith("."))) ||
                    (file.isDirectory() && excludedDirs.contains(fileName))) {
                continue;
            }

            fileList.add(file);
        }

        // Sort files: directories first, then files alphabetically
        fileList.sort((f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            } else if (!f1.isDirectory() && f2.isDirectory()) {
                return 1;
            } else {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });

        return fileList;
    }

    /**
     * Prints a single file or directory entry in the tree with appropriate color.
     *
     * @param file   The file or directory to print
     * @param prefix The prefix for indentation
     * @param isLast Whether this is the last item at current level
     */
    private void printFile(File file, String prefix, boolean isLast) {
        String connector = isLast ? LAST_BRANCH : BRANCH;
        String coloredName = getColoredFileName(file);

        printWithColor(prefix + connector + coloredName, file);
    }

    /**
     * Prints text with color if enabled.
     *
     * @param text Text to print
     * @param file Associated file for determining color
     */
    private void printWithColor(String text, File file) {
        if (!useColors || file == null) {
            System.out.println(text);
            return;
        }

        System.out.println(text + RESET);
    }

    /**
     * Returns a file name with appropriate color based on file type.
     *
     * @param file File to get colored name for
     * @return Colored file name or plain name if colors disabled
     */
    private String getColoredFileName(File file) {
        if (!useColors) {
            return file.getName();
        }

        String name = file.getName();

        if (file.isDirectory()) {
            return DIRECTORY_COLOR + name;
        }

        String lowerName = name.toLowerCase();
        if (isExecutable(file)) {
            return EXECUTABLE_COLOR + name;
        } else if (lowerName.matches(".*\\.(jpg|jpeg|png|gif|bmp|svg)$")) {
            return IMAGE_COLOR + name;
        } else if (lowerName.matches(".*\\.(txt|md|java|c|cpp|py|js|html|css|xml|json)$")) {
            return TEXT_COLOR + name;
        }

        return name;
    }

    /**
     * Determines if a file is executable.
     *
     * @param file File to check
     * @return True if file is executable
     */
    private boolean isExecutable(File file) {
        if (file.isDirectory()) {
            return false;
        }

        String name = file.getName().toLowerCase();
        return file.canExecute() ||
                name.endsWith(".exe") ||
                name.endsWith(".bat") ||
                name.endsWith(".sh");
    }

    /**
     * Prints help information for command-line usage.
     */
    private void printHelp() {
        System.out.println("Directory Tree Generator - Help");
        System.out.println("-------------------------------");
        System.out.println("Usage: java -jar directory-tree.jar [options]");
        System.out.println("Options:");
        System.out.println("  -p, --path <path>       Directory path to display (default: current directory)");
        System.out.println("  -d, --depth <number>    Maximum depth to display (default: unlimited)");
        System.out.println("  -c, --color <true|false> Use colored output (default: true)");
        System.out.println("  -h, --show-hidden       Show hidden files and directories");
        System.out.println("  -e, --exclude <dirs>    Additional directories to exclude (comma-separated)");
        System.out.println("  --help                  Display this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar directory-tree.jar -p /home/user/projects -d 2");
        System.out.println("  java -jar directory-tree.jar --path . --color false");
    }
}