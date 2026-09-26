@echo off
setlocal
echo =======================================================
echo   StreamVault Android & Firestick TV - Push to GitHub
echo =======================================================
echo.
echo 1. Go to https://github.com/new and create a new repository
echo    (Can be Public or Private - name it anything, e.g. StreamVault-Android)
echo.
set /p REPO_URL="Enter your GitHub Repository URL (e.g. https://github.com/Caball232/StreamVault-Android.git): "

if "%REPO_URL%"=="" (
    echo No URL entered. Exiting.
    pause
    exit /b
)

echo.
echo Initializing git, staging files, and committing...
git init
git add .
git commit -m "StreamVault Android & Firestick TV native client v1.0.0" >nul 2>&1
git branch -M main
git remote remove origin >nul 2>&1
git remote add origin %REPO_URL%

echo.
echo Pushing to GitHub...
git push -u origin main

if %ERRORLEVEL% equ 0 (
    echo.
    echo =======================================================
    echo SUCCESS! Code pushed to GitHub!
    echo =======================================================
    echo.
    echo Now:
    echo 1. Go to your repo on GitHub: %REPO_URL%
    echo 2. Click on the "Actions" tab at the top.
    echo 3. You will see "Build StreamVault Firestick & Android APK" running.
    echo 4. In ~1 minute, it will finish and produce your downloadable:
    echo    "StreamVault-FireTV.apk" under Artifacts and GitHub Releases!
    echo.
    echo Firestick Downloader Instructions:
    echo  - In the "Downloader" app on Fire TV, type the direct URL or shortcode
    echo  - Click Go, and Fire OS will prompt "Install" automatically!
    echo.
) else (
    echo.
    echo Error pushing to GitHub. Please check your URL and GitHub credentials.
)

pause
