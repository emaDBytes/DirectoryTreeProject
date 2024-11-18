// src\main\java\com\directorytree\DirectoryTree.java
package com.directorytree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * DirectoryTree generates a tree-like representation of a directory structure.
 * This utility allows users to visualize the hierarchy of files and directories
 * in a given path, similar to the tree command in Unix-like systems.
 *
 * @author EmadBytes
 * @version 1.0
 */
public class DirectoryTree {
    /** Unicode character for drawing a branch connector in the tree */
    private static final String BRANCH = "├── ";
    /** Unicode character for drawing the last branch connector in the tree */
    private static final String LAST_BRANCH = "└── ";
    /** Unicode character for drawing vertical lines in the tree */
    private static final String VERTICAL = "│   ";
    /** Spacing used for indentation in the tree structure */
    private static final String SPACE = "    ";

    /**
     * Main entry point of the application.
     * Prompts user for a directory path and displays its tree structure.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Use try-with-resources to ensure proper closure of Scanner
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter below the directory path (e.g., C:\\Users\\YourName\\Documents or .):");
            String path = scanner.nextLine();

            File directory = new File(path);
            if (!directory.exists() || !directory.isDirectory()) {
                System.out.println("Invalid directory path");
                return;
            }

            System.out.println("\nDirectory Tree for: " + directory.getAbsolutePath());
            System.out.println();
            System.out.println(directory.getName());
            printDirectoryTree(directory, "", true);
        }
    }

    /**
     * Recursively prints the directory tree structure.
     *
     * @param directory The current directory being processed
     * @param prefix    The prefix to use for the current level of the tree
     * @param isRoot    Boolean flag indicating if this is the root directory
     */
    private static void printDirectoryTree(File directory, String prefix, boolean isRoot) {
        File[] files = directory.listFiles();
        if (files == null) {
            return; // Return if directory is inaccessible
        }

        // Get filtered list of non-hidden files and directories
        List<File> fileList = filterHiddenFiles(files);
        int total = fileList.size();

        // Iterate through all files/directories in the current directory
        for (int i = 0; i < total; i++) {
            File file = fileList.get(i);
            boolean isLast = (i == total - 1);
            printFile(file, prefix, isLast);

            // Recursively process subdirectories
            if (file.isDirectory()) {
                String newPrefix = prefix + (isLast ? SPACE : VERTICAL);
                printDirectoryTree(file, newPrefix, false);
            }
        }
    }

    /**
     * Filters out hidden files and directories from the given array.
     * Hidden files are those that are marked as hidden by the OS or
     * start with a dot (.).
     *
     * @param files Array of files to filter
     * @return List of non-hidden files and directories
     */
    private static List<File> filterHiddenFiles(File[] files) {
        List<File> fileList = new ArrayList<>();
        for (File file : files) {
            // Only add files that are neither hidden nor start with a dot
            if (!file.isHidden() && !file.getName().startsWith(".")) {
                fileList.add(file);
            }
        }
        return fileList;
    }

    /**
     * Prints a single file or directory entry in the tree.
     *
     * @param file   The file or directory to print
     * @param prefix The prefix to use for the current level
     * @param isLast Boolean indicating if this is the last entry in the current
     *               directory
     */
    private static void printFile(File file, String prefix, boolean isLast) {
        System.out.println(prefix + (isLast ? LAST_BRANCH : BRANCH) + file.getName());
    }
}