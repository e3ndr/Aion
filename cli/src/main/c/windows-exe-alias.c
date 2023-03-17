// All this file does is immediately call a sister bat file with the same input arguments.

#include <windows.h>
#include <stdio.h>
#include <string.h>

#pragma comment(lib, "SHELL32.LIB")

#define sizeofstr(s) (strlen(s) * sizeof(char))
#define sizeofch(n) (n * sizeof(char))

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

    STARTUPINFO si;
    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    si.hStdInput = GetStdHandle(STD_INPUT_HANDLE);
    si.hStdOutput = GetStdHandle(STD_OUTPUT_HANDLE);
    si.hStdError = GetStdHandle(STD_ERROR_HANDLE);
    // si.dwFlags |= STARTF_USESTDHANDLES;

    PROCESS_INFORMATION pi;
    ZeroMemory(&pi, sizeof(pi));

    // Start the child process.
    if (!CreateProcess(
            cmd,        // Use CMD
            to_execute, // Command line
            NULL,       // Process handle not inheritable
            NULL,       // Thread handle not inheritable
            FALSE,      // Set handle inheritance to FALSE
            0,          // No creation flags
            NULL,       // Use parent's environment block
            NULL,       // Use parent's starting directory
            &si,        // Pointer to STARTUPINFO structure
            &pi         // Pointer to PROCESS_INFORMATION structure
            ))
    {
        printf("CreateProcess failed (%d).\n", GetLastError());
        return -1;
    }

    // Wait until child process exits and cleanup.
    WaitForSingleObject(pi.hProcess, INFINITE);
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);

    // Get the exit code.
    DWORD exit_code = 0;
    GetExitCodeProcess(pi.hProcess, &exit_code);
    // printf("Exit code: %ld.\n", exit_code);

    return exit_code;
}