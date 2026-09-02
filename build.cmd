@echo off
call .\gradlew clean buildPlugin
copy /-Y build\distributions\*.zip d:\tools\janitor-idea\
