@echo off
echo APP_HOME=%~dp0
set "APP_HOME=%~dp0"
if "%APP_HOME%" == "" set "APP_HOME=%CD%"
echo APP_HOME after=%APP_HOME%
set "WRAPPER_JAR_PATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
echo WRAPPER_JAR_PATH=%WRAPPER_JAR_PATH%
if exist "%WRAPPER_JAR_PATH%" (
    echo Wrapper JAR found
) else (
    echo Wrapper JAR NOT found
)
echo JAVA_HOME=%JAVA_HOME%
if defined JAVA_HOME (
    echo JAVA_HOME is set
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    echo JAVA_EXE=%JAVA_EXE%
    if exist "%JAVA_EXE%" (
        echo Java EXE found
    ) else (
        echo Java EXE NOT found
    )
) else (
    echo JAVA_HOME is NOT set
)