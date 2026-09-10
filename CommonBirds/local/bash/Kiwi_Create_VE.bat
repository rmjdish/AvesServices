rem A Windows BAT file to create an all user Python VE
rem using virtualenvwrapper-win on a Windows 2022 server
set WORKON_HOME=D:\PyVEs
mkvirtualenv varsearch
icacls D:\PyVEs\varsearch /grant:r "Everyone":R
echo Done.  Try workon to test it.
echo Do the following to set the project directory
echo workon varsearch
echo setprojectdir D:\CLS_DDT
