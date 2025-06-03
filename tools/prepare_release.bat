@echo off

setlocal enabledelayedexpansion

for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do (
    for /f "tokens=1 delims=." %%b in ("%%a") do (
        set timeStamp=%%b
    )
)

set projectDirectory=%~dp0..
set sourceDirectory=%projectDirectory%\hlml\src
set assetDirectory=%projectDirectory%\assets
set releaseDirectory=%projectDirectory%\releases

rmdir /s /q "%releaseDirectory%\build"
mkdir "%releaseDirectory%\build"
for /r %sourceDirectory% %%i in (*) do (
    set sourceFiles=!sourceFiles! %%i
)
javac -d "%releaseDirectory%\build" --release 21 %sourceFiles%

rmdir /s /q "%releaseDirectory%\latest"
mkdir "%releaseDirectory%\latest"
xcopy /v /e "%assetDirectory%" "%releaseDirectory%\latest"
cd "%releaseDirectory%\build"
jar cfe "%releaseDirectory%\latest\hlml.jar" hlml.launcher.Launcher *

cd "%releaseDirectory%\latest"
7z a -tzip "..\hlml-%timeStamp%.zip" "*"
cd "..\.."
