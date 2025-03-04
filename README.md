# Directory Tree Generator

A Java application that generates a tree-like structure of directories and files, similar to the `tree` command in Unix-like systems.

## Features

- Colored output for different file types
- Customizable depth limit
- File and directory filtering options
- Hidden file handling
- Symbolic link detection to prevent infinite loops
- Both interactive and command-line modes

## Prerequisites

- Java 21 or higher
- Maven

## Building the Project

```bash
mvn clean package
```

## Running the Application

### Using the executable JAR:

```bash
java -jar target/directory-tree-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Interactive Mode:

Simply run the JAR without arguments to enter interactive mode.

### Command-Line Options:

```bash
java -jar directory-tree.jar [options]

Options:
  -p, --path <path>       Directory path to display (default: current directory)
  -d, --depth <number>    Maximum depth to display (default: unlimited)
  -c, --color <true|false> Use colored output (default: true)
  -h, --show-hidden       Show hidden files and directories
  -e, --exclude <dirs>    Additional directories to exclude (comma-separated)
  --help                  Display help message
```

## Examples

```bash
# Display current directory with 2 levels
java -jar directory-tree.jar -p . -d 2

# Display with hidden files and no colors
java -jar directory-tree.jar -p /home/user/projects -h -c false

# Exclude specific directories
java -jar directory-tree.jar -e "logs,temp,cache"
```
