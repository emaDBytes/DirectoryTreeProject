package com.directorytree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * DirectoryTree generates a tree-like representation of a directory structure.
 * This utility provides a visual representation of files and directories
 * hierarchy,
 * similar to the 'tree' command in Unix-like systems.
 * 
 * Features:
 * - Handles symbolic links and prevents infinite loops
 * - Skips hidden files and system directories
 * - Uses Unicode characters for visual tree structure
 * - Provides clear visualization of directory hierarchies
 * 
 * Usage example:
 * Run the program and enter a directory path when prompted.
 * The output will show the directory structure like this:
 * MyDirectory
 * ├── file1.txt
 * ├── folder1
 * │ ├── subfile1.txt
 * │ └── subfile2.txt
 * └── folder2
 * └── subfile3.txt
 *
 * @author EmadBytes
 * @version 1.0
 * @since 2024-11-24
 */
public class DirectoryTree {
    /**
     * Unicode character for drawing a non-last item branch connector.
     * Used for items that have siblings following them.
     */
    private static final String BRANCH = "├── ";

    /**
     * Unicode character for drawing the last item branch connector.
     * Used for the last item in each directory level.
     */
    private static final String LAST_BRANCH = "└── ";

    /**
     * Unicode character for drawing vertical continuation lines.
     * Used to connect items across different levels.
     */
    private static final String VERTICAL = "│   ";

    /**
     * Spacing used for indentation where no vertical line is needed.
     * Used after last items in a directory level.
     */
    private static final String SPACE = "    ";

    /**
     * Main entry point of the application.
     * Prompts user for a directory path and displays its tree structure.
     * Handles invalid input and directories gracefully.
     *
     * @param args Command line arguments (not used in current implementation)
     */
    public static void main(String[] args) {
        // Use try-with-resources to ensure Scanner is properly closed
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter below the directory path (e.g., C:\\Users\\YourName\\Documents or .):");
            String path = scanner.nextLine();

            // Validate the provided path
            File directory = new File(path);
            if (!directory.exists() || !directory.isDirectory()) {
                System.out.println("Invalid directory path");
                return;
            }

            // Initialize visited paths set for cycle detection
            Set<String> visitedPaths = new HashSet<>();

            // Print header and initial directory name
            System.out.println("\nDirectory Tree for: " + directory.getAbsolutePath());
            System.out.println();
            System.out.println(directory.getName());

            // Start recursive directory traversal
            printDirectoryTree(directory, "", true, visitedPaths);
        }
    }

    /**
     * Recursively traverses and prints the directory tree structure.
     * Handles symbolic links and circular references by tracking visited paths.
     * Implements depth-first traversal of the directory structure.
     *
     * @param directory    Current directory being processed
     * @param prefix       Current line prefix for proper indentation and tree
     *                     structure
     * @param isRoot       Flag indicating if this is the root directory
     * @param visitedPaths Set of canonical paths already visited to prevent cycles
     */
    private static void printDirectoryTree(File directory, String prefix, boolean isRoot, Set<String> visitedPaths) {
        try {
            // Get canonical path to handle symbolic links properly
            String canonicalPath = directory.getCanonicalPath();

            // Check for circular references
            if (!visitedPaths.add(canonicalPath)) {
                System.out.println(prefix + "├── " + directory.getName() + " (symbolic link)");
                return;
            }

            // Get list of files and directories
            File[] files = directory.listFiles();
            if (files == null) {
                // Directory is empty or inaccessible
                return;
            }

            // Filter and process directory contents
            List<File> fileList = filterHiddenFiles(files);
            int total = fileList.size();

            // Process each file/directory
            for (int i = 0; i < total; i++) {
                File file = fileList.get(i);
                boolean isLast = (i == total - 1);
                printFile(file, prefix, isLast);

                // Recursively process subdirectories
                if (file.isDirectory()) {
                    // Adjust prefix for subdirectories based on whether this is the last item
                    String newPrefix = prefix + (isLast ? SPACE : VERTICAL);
                    printDirectoryTree(file, newPrefix, false, visitedPaths);
                }
            }
        } catch (IOException e) {
            // Handle IO errors gracefully
            System.out.println(prefix + "├── Error accessing: " + directory.getName());
        }
    }

    /**
     * Filters out system and hidden files from the directory listing.
     * Removes:
     * - Hidden files (starting with .)
     * - System-marked hidden files
     * - node_modules directories (common in JavaScript projects)
     * - target directories (common in Java projects)
     *
     * @param files Array of files to filter
     * @return List of visible files and directories
     */
    private static List<File> filterHiddenFiles(File[] files) {
        List<File> fileList = new ArrayList<>();
        for (File file : files) {
            // Skip files that match any exclusion criteria
            if (!file.isHidden() &&
                    !file.getName().startsWith(".") &&
                    !file.getName().equals("node_modules") &&
                    !file.getName().equals("target")) {
                fileList.add(file);
            }
        }
        return fileList;
    }

    /**
     * Prints a single file or directory entry in the tree.
     * Handles the visual formatting of each tree item.
     *
     * @param file   The file or directory to print
     * @param prefix The prefix to use for the current level (determines indentation
     *               and tree structure)
     * @param isLast Boolean indicating if this is the last item in its containing
     *               directory
     */
    private static void printFile(File file, String prefix, boolean isLast) {
        // Use different branch characters based on whether this is the last item
        System.out.println(prefix + (isLast ? LAST_BRANCH : BRANCH) + file.getName());
    }
}