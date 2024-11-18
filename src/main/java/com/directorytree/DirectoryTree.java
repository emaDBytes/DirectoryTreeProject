package com.directorytree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DirectoryTree {
    private static final String BRANCH = "├── ";
    private static final String LAST_BRANCH = "└── ";
    private static final String VERTICAL = "│   ";
    private static final String SPACE = "    ";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter the directory path (e.g., C:\\Users\\YourName\\Documents or .):");
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

    private static void printDirectoryTree(File directory, String prefix, boolean isRoot) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        List<File> fileList = filterHiddenFiles(files);
        int total = fileList.size();

        for (int i = 0; i < total; i++) {
            File file = fileList.get(i);
            boolean isLast = (i == total - 1);
            printFile(file, prefix, isLast);

            if (file.isDirectory()) {
                String newPrefix = prefix + (isLast ? SPACE : VERTICAL);
                printDirectoryTree(file, newPrefix, false);
            }
        }
    }

    private static List<File> filterHiddenFiles(File[] files) {
        List<File> fileList = new ArrayList<>();
        for (File file : files) {
            if (!file.isHidden() && !file.getName().startsWith(".")) {
                fileList.add(file);
            }
        }
        return fileList;
    }

    private static void printFile(File file, String prefix, boolean isLast) {
        System.out.println(prefix + (isLast ? LAST_BRANCH : BRANCH) + file.getName());
    }
}