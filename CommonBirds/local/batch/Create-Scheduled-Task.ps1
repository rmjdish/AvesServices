# First, set execution policy to allow the script to run
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope LocalMachine -Force

# Create the action (fix the quotes and path)
$Action = New-ScheduledTaskAction `
    -Execute "PowerShell.exe" `
    -Argument "-ExecutionPolicy Bypass -File `"C:\Scripts\Restart-Services.ps1`""

# Create the trigger for 3 AM daily
$Trigger = New-ScheduledTaskTrigger -Daily -At "3:00 AM"

# Create settings
$Settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -RestartCount 3 `
    -RestartInterval (New-TimeSpan -Minutes 5)

# Create principal (run as SYSTEM)
$Principal = New-ScheduledTaskPrincipal `
    -UserId "SYSTEM" `
    -LogonType ServiceAccount `
    -RunLevel Highest

# Register the task
Register-ScheduledTask `
    -TaskName "Restart Web Services" `
    -Action $Action `
    -Trigger $Trigger `
    -Settings $Settings `
    -Principal $Principal `
    -Description "Restarts Apache and MySQL services daily at 3 AM"

# Verify the task was created
Get-ScheduledTask -TaskName "Restart Web Services"
