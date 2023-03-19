// All this file does is immediately call a sister bat file with the same input arguments.

#include <windows.h>
#include <stdio.h>
#include <string.h>
#include "helpers/run-command.h"
#include "helpers/string.h"

#pragma comment(lib, "SHELL32.LIB")

int main(int argc, char **argv)
{
    // Get the path and name of *THIS* executable.
    char *this_executable = argv[0];
    // printf("This executable: '%s'\n", this_executable);
    // printf("EN Len: %d\n", sizeofstr(this_executable));

    // Get the raw commandline args.
    char *raw_command_line = GetCommandLine();
    char *this_command_line = calloc(sizeofstr(raw_command_line), 1);
    memcpy( // Skip over the executable name.
        this_command_line,
        &raw_command_line[sizeofstr(this_executable)],
        sizeofstr(raw_command_line) - sizeofstr(this_executable));
    // printf("Args: '%s'\n", this_command_line);
    // printf("Raw commandline: '%s'\n", raw_command_line);

    // Format the command for cmd.
    char *to_execute = calloc(sizeofstr(this_executable) + sizeofstr(this_command_line) + sizeofch(11 /*"cmd /c " + ".bat"*/), 1);
    strcat(to_execute, "/c ");
    strcat(to_execute, this_executable);
    strcat(to_execute, ".bat");
    strcat(to_execute, this_command_line);
    // printf("To execute: %s\n", to_execute);

    char *cmd = getenv("COMSPEC");

    long long exit_code = RunCommand(cmd, to_execute);
    return exit_code;
}