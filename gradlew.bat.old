@if "%DEBUG%" == "" @echo off
@rem 
@rem Copyright 2015 the original author or authors.
@rem 
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem 
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem 
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem 

@if "%~dp0%" == "" @set "APP_HOME=%~dp0%"
@if "%APP_HOME%" == "" @set "APP_HOME=%CD%"

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
@set DEFAULT_JVM_OPTS=-Xmx64m -Dfile.encoding=UTF-8

@rem Find the java binary
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
exit /b 1

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%\\bin\\java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
exit /b 2

:init
@rem Get command-line arguments, handling Windows variants
if not "%OS%" == "Windows_NT" goto win9xME_args
if "%~@argc0"=="1" goto win9xME_args
:nt_args
if "%~1"=="" goto skipInit
set "MY_APP_OPTS=%*"
:win9xME_args
@rem Slurp the zero and special arguments
shift
shift
if not "%~1"=="" goto nt_args
:skipInit
set "GRADLE_CMD_LINE_ARGS=%*"

set "APP_HOME=%APP_HOME:\=\\%"

set WRAPPER_JAR_PATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
if not exist "%WRAPPER_JAR_PATH%" (
    echo.
    echo FATAL ERROR: Could not find %WRAPPER_JAR_PATH%
    echo.
    exit /b 1
)

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -classpath "%WRAPPER_JAR_PATH%" org.gradle.wrapper.GradleWrapperMain %GRADLE_CMD_LINE_ARGS%
endlocal
exit /b %ERRORLEVEL%