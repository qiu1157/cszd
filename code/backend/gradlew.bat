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
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem --------------------------------------------------------------------------
@rem Ensure a JDK is available (javac required for compilation).
@rem If JAVA_HOME is not set or points to a JRE, auto-download Temurin JDK 17 zip
@rem via Adoptium API into .gradle\jdks\temurin-17 and set JAVA_HOME accordingly.
@rem --------------------------------------------------------------------------
set JDK_CACHE_DIR=%APP_HOME%\.gradle\jdks\temurin-17
set JDK_HOME_FILE=%JDK_CACHE_DIR%\JAVA_HOME.txt

if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\javac.exe" (
    goto findJavaFromJavaHome
  )
)

@rem Try to detect javac from PATH (??????? JDK ???? JAVA_HOME)
set JAVAC_PATH=
for /f "delims=" %%i in ('where javac 2^>NUL') do (
  set JAVAC_PATH=%%i
  goto _found_javac
)
:_found_javac
if defined JAVAC_PATH (
  for %%j in ("%JAVAC_PATH%") do set JAVA_HOME=%%~dpj..
)

@rem Try common installation directory (Program Files\Java\jdk-*)
if not defined JAVA_HOME (
  for /d %%d in ("%ProgramFiles%\Java\jdk-*") do (
    if exist "%%d\bin\javac.exe" (
      set JAVA_HOME=%%d
      goto _jdk_from_common_dir
    )
  )
)
:_jdk_from_common_dir

if exist "%JDK_HOME_FILE%" (
  for /f "usebackq delims=" %%i in ("%JDK_HOME_FILE%") do set JAVA_HOME=%%i
)

if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\javac.exe" (
    goto findJavaFromJavaHome
  )
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $dest='%JDK_CACHE_DIR%'; New-Item -ItemType Directory -Force -Path $dest | Out-Null; $zip=Join-Path $dest 'jdk.zip'; if(!(Test-Path $zip)){ Write-Host 'Downloading Temurin JDK 17 ...'; Invoke-WebRequest -TimeoutSec 300 -Uri 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk' -OutFile $zip }; Expand-Archive -Force -Path $zip -DestinationPath $dest; $jdk=(Get-ChildItem $dest -Directory | Sort-Object Name | Select-Object -First 1).FullName; Set-Content -Path (Join-Path $dest 'JAVA_HOME.txt') -Value $jdk -Encoding ASCII"
if %ERRORLEVEL% neq 0 goto fail

for /f "usebackq delims=" %%i in ("%JDK_HOME_FILE%") do set JAVA_HOME=%%i
if not exist "%JAVA_HOME%\bin\javac.exe" goto fail

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega




