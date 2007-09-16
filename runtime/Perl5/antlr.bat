@echo off

IF "%ANTLR_HOME%" == "" SET ANTLR_HOME=%~p0..\..

"%JAVA_HOME%\bin\java" ^
    -Dfile.encoding=windows-1252 ^
    -classpath "%~p0..\..\build\classes;%~p0..\..\build\rtclasses;%ANTLR_HOME%\lib\antlr-3.0.jar;%ANTLR_HOME%\lib\antlr-2.7.7.jar;%ANTLR_HOME%\lib\stringtemplate-3.0.jar" ^
    org.antlr.Tool ^
    %*
