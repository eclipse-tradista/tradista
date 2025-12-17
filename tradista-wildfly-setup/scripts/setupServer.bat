xcopy ..\modules\org "%WILDFLY_HOME%\modules\org" /E /I /Y
%WILDFLY_HOME%\bin\jboss-cli.bat --connect --file=./commands.cli