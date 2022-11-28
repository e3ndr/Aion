@ECHO OFF

SET AION_BASE_DIR=%~dp0..\

%~dp0..\packages\{package}\{version}\commands\{command} %*
