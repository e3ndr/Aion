@ECHO OFF

SET AION_BASE_DIR=%~dp0..\

%~dp0..\runtime\bin\java -jar %~dp0..\aion.jar %*
