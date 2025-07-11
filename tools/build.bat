@echo off

setlocal enabledelayedexpansion

set projectDirectory=%~dp0..
set valueDirectory=%projectDirectory%\values
set sourceDirectory=%projectDirectory%\hlml\src
set buildDirectory=%projectDirectory%\build
set assetDirectory=%projectDirectory%\assets
set releaseDirectory=%projectDirectory%\releases
set valueSourceFile=%sourceDirectory%\hlml\Values.java

for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do (
    for /f "tokens=1 delims=." %%b in ("%%a") do (
        set timestamp=%%b
    )
)

set /p version=< "%valueDirectory%\VERSION"

echo package hlml;>%valueSourceFile%
echo;>>%valueSourceFile%
echo /** This class is auto-generated at build-time. */>>%valueSourceFile%
echo public final class Values {>>%valueSourceFile%
echo   /** Version of the compiler. */>>%valueSourceFile%
echo   public static final String VERSION = %version%;>>%valueSourceFile%
echo;>>%valueSourceFile%
echo   /** Build timestamp of the compiler. */>>%valueSourceFile%
echo   public static final String TIMESTAMP = "%timestamp%";>>%valueSourceFile%
echo;>>%valueSourceFile%
echo   /** Prevents instantiating this class. */>>%valueSourceFile%
echo   private Values() {}>>%valueSourceFile%
echo }>>%valueSourceFile%

mkdir "%buildDirectory%"
for /r %sourceDirectory% %%i in (*) do (
    set sourceFiles=!sourceFiles! %%i
)
javac -d "%buildDirectory%" --release 21 %sourceFiles%

rmdir /s /q "%releaseDirectory%\latest"
mkdir "%releaseDirectory%\latest"
xcopy /v /e "%assetDirectory%" "%releaseDirectory%\latest"
cd "%buildDirectory%"
jar cfe "%releaseDirectory%\latest\hlml.jar" hlml.launcher.Launcher *

cd "%releaseDirectory%\latest"
7z a -tzip "..\hlml_v%version%_b%timestamp%.zip" "*"
cd "..\.."
