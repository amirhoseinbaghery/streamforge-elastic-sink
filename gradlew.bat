@echo off
setlocal
set GRADLE_VERSION=8.10.2
set DIST=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin
set INSTALL=%DIST%\gradle-%GRADLE_VERSION%
if not exist "%INSTALL%\bin\gradle.bat" (
  if not exist "%DIST%" mkdir "%DIST%"
  powershell -NoProfile -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%DIST%\gradle.zip'"
  powershell -NoProfile -Command "Expand-Archive -Force '%DIST%\gradle.zip' '%DIST%'"
)
call "%INSTALL%\bin\gradle.bat" %*
