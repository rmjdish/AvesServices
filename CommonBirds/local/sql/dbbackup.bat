echo off
setlocal EnableDelayedExpansion
echo Script to backup DBs
set db=%1
pushd \\file01.ucl.ac.uk\LHA2\
echo Backing up database %db%
set machine=Kiwi
set host=\san\backups\
set bkfile=%host%%machine%_%db%_backup.sql
set obfile1=%host%%machine%_%db%_previous_backup1.sql
set obfile2=%host%%machine%_%db%_previous_backup2.sql
set obfile3=%host%%machine%_%db%_previous_backup3.sql
set obfile4=%host%%machine%_%db%_previous_backup4.sql
set obfile5=%host%%machine%_%db%_previous_backup5.sql
set obfile6=%host%%machine%_%db%_previous_backup6.sql
set obfile7=%host%%machine%_%db%_previous_backup7.sql
if exist "%obfile6%" (
  call :shuffle "%obfile6%" "%obfile7%"
)
if exist "%obfile5%" (
   call :shuffle "%obfile5%" "%obfile6%"
) 
if exist "%obfile4%" (
   call :shuffle  "%obfile4%" "%obfile5%"
)
if exist "%obfile3%" (
   call :shuffle  "%obfile3%" "%obfile4%"
)
if exist "%obfile2%" (
   call :shuffle "%obfile2%" "%obfile3%"
)
if exist "%obfile1%" (
   call :shuffle "%obfile1%" "%obfile2%"
)
if exist "%bkfile%" (
   call :shuffle "%bkfile%" "%obfile1%"
)
if ERRORLEVEL 0 goto DUMP
echo Could not rename %bkfile%.  Perhaps it doesn't exist.
dir %host%*.sql
:DUMP
mysqldump --host=localhost --user=archive --password="Douglas1946" --dump-date --routines --triggers --databases %db% --result-file="%bkfile%"
echo Backup finished.
popd
echo on
exit /b

rem Subroutine Shuffle (who said Fortran was dead!)
:shuffle
echo Shuffle: %1 %2
if exist %1 (
   if exist %2 (
      echo %2 will be overwritten
      )
   copy /V /Y /A %1 /A %2
)
exit /b 
