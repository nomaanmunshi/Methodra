@echo off
setlocal
set GRADLE_VERSION=8.13
set BASE=%USERPROFILE%\.gradle\methodra-distributions
set DIST=%BASE%\gradle-%GRADLE_VERSION%
set ZIP=%BASE%\gradle-%GRADLE_VERSION%-bin.zip
if not exist "%DIST%\bin\gradle.bat" (
  if not exist "%BASE%" mkdir "%BASE%"
  if not exist "%ZIP%" powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP%'"
  powershell -NoProfile -Command "Expand-Archive -Force '%ZIP%' '%BASE%'"
)
call "%DIST%\bin\gradle.bat" %*
