# hlml.java

Compiler for the [HLML](https://github.com/calestialgem/hlml) written in Java.

## Requirements

Requires a JVM for **Java 20** with preview features enabled to run.

Optionally for Windows, the JVM should be on the path to use the released
`hlml.bat` as is. If that is not the case, edit the released `hlml.bat` by
replacing the first word (which is `java`) with the path to the JVM you want to
use.

## Installation

1. Download the released `hlml.jar` (the compiled Java code) and the released
   `hlml.hlml` (the standard library implementation).
2. Optionally for Windows, download the released `hlml.bat` (the invocation
   script) if you want to use it. Edit this file if there is no JVM in the path
   or the JVM you want to use is another one.
3. Create a directory anywhere you want, and put the downloaded files there.
4. Optionally for Windows, add the directory to the path such that you can use
   the released `hlml.bat` from anywhere without mentioning its path every time.

## Uninstallation

1. For Windows, if you added the released `hlml.bat` to the path, remove it.
2. Delete the directory you have created and the downloaded files.

## Invocation

Invoke the JVM by passing the released `hlml.jar` with `--enable-preview` flag.
Command line arguments can be passed to the compiler through that same command.

Assuming `my_downloads` is the path to the downloaded files and `java` is the
path to the installed Java 20 JVM executable:

`java -jar --enable-preview my_downloads/hlml.jar [OPTIONS] <argument>`

For Windows, there is the released `hlml.bat`, which can be used to invoke (if
`java` can be invoked as a command to reach the installed Java 20 JVM):

`my_downloads/hlml [OPTIONS] <argument>`

For Windows, if the directory holding the released `hlml.bat` is added to the
path:

`hlml [OPTIONS] <argument>`

## Usage

A source name (without the file extension) must be passed to the compiler. Then
it will search in the list of include directories in the order they were given
and compile the source to the given output file.

To set the path to the output file, use the `-o <path>` option. If an output is
not given, the compiler only validates the target source's semantics. If an
output path is given, the source must have an `entrypoint` declaration.

To add the path to an include directory, use the `-I <path>` option, which can
be given multiple times to create a list of include directories. By default this
would be just `.` (the directory the compiler was invoked from), and the given
paths are added on top of this.

This list of directories must include the standard library if the code uses the
symbols in the standard library. You can use the released `hlml.hlml` or write
your own.

Parameters and options can be in any order. The order is only important for the
directories added to the include path because it dictates the order in which
they are searched through.

## Example

Assuming the standard library and other required libraries are in the
`my_hlml_libraries` directory and the code you want to compile is in the current
working directory named `my_code.hlml`:

`hlml -I my_hlml_libraries my_code`

Assuming the working directory is the directory you put the downloaded files and
all your libraries and the code you want to compile is also here:

`hlml my_code -o my_code.mlog`

## Suggestion

You can just have single directory for HLML in your computer, and you can put
the compiler and all the source files in this directory. Then, you can create a
script that will invoke the JVM with the compiler's JAR. (This kind of script is
provided for Windows.)

Then you can just compile the code directly into that folder as it was done in
the last example. Or, you can create a sub-directory called `bin`, `artifacts`
or `mlog`, and invoke the compiler like
`hlml my_code -o artifacts/my_code.mlog`, and the compiled Mindustry Logic code
would be cleanly separated into that directory.

And libraries or code you get from others can be put here and simply used by
your code.

Also you can setup VS Code to show you error locations directly in the source
file. Look at
[my Mindustry logic setup](https://github.com/calestialgem/mindustry_logic) for
an example.

## License

Licensed under GPL 3.0 or later.

---

Copyright (C) 2023 Cem Geçgel <gecgelcem@outlook.com>
