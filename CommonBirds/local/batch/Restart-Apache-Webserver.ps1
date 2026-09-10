# Restart-Apache.ps1
param()

# Function to pause only when launched from GUI
function Pause-IfLaunchedFromExplorer {
    try {
        $parentProcess = (Get-Process -Id (Get-CimInstance Win32_Process -Filter "ProcessId = $PID" | 
            Select-Object -ExpandProperty ParentProcessId)).Name
        if ($parentProcess -eq 'explorer') {
            Write-Host "`nPress any key to continue..." -ForegroundColor Magenta
            $null = $Host.UI.RawUI.ReadKey('NoEcho,IncludeKeyDown')
        }
    }
    catch { <# Suppress errors #> }
}

# Check for administrator privileges
$currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
if (-not $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    # Relaunch with elevation if not admin
    $scriptPath = $MyInvocation.MyCommand.Path
    $scriptParams = $MyInvocation.UnboundArguments -join ' '
    
    Write-Host "Requesting administrator privileges..." -ForegroundColor Yellow
    
    try {
        # Detect if launched from Explorer to show proper UI
        $parentProcess = (Get-Process -Id (Get-CimInstance Win32_Process -Filter "ProcessId = $PID" | 
            Select-Object -ExpandProperty ParentProcessId)).Name
        
        $psi = New-Object System.Diagnostics.ProcessStartInfo
        $psi.FileName = "powershell.exe"
        $psi.Arguments = "-ExecutionPolicy Bypass -NoProfile -File `"$scriptPath`" $scriptParams"
        $psi.Verb = "RunAs"
        $psi.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Normal
        
        # Show UI differently based on launch context
        if ($parentProcess -eq 'explorer') {
            Write-Host "`nPlease approve UAC prompt to continue" -ForegroundColor Cyan
            Start-Sleep -Seconds 1  # Allow time to read message
            $psi.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Normal
        }
        else {
            $psi.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Hidden
        }
        
        [System.Diagnostics.Process]::Start($psi) | Out-Null
        exit 0
    }
    catch {
        Write-Host "`nERROR: Elevation failed - $_" -ForegroundColor Red
        Write-Host "Please run PowerShell as Administrator and execute manually:" -ForegroundColor Yellow
        Write-Host "PowerShell -ExecutionPolicy Bypass -File `"$scriptPath`"" -ForegroundColor Cyan
        Pause-IfLaunchedFromExplorer
        exit 1
    }
}

# Main script (running with admin privileges)
$serviceName = "Apache24"

try {
    # Check if service exists
    $service = Get-Service -Name $serviceName -ErrorAction Stop
    
    Write-Host "`nRestarting $($service.DisplayName)..." -ForegroundColor Cyan
    Restart-Service -Name $serviceName -Force
    
    # Wait and verify status
    Start-Sleep -Seconds 2
    $service.Refresh()
    
    if ($service.Status -eq 'Running') {
        Write-Host "`nSUCCESS: Service restarted" -ForegroundColor Green
        Write-Host "Status: $($service.Status)" -ForegroundColor Green
    }
    else {
        Write-Host "`nWARNING: Service may not have restarted properly" -ForegroundColor Yellow
        Write-Host "Current status: $($service.Status)" -ForegroundColor Yellow
        exit 2
    }
}
catch {
    Write-Host "`nERROR: $_" -ForegroundColor Red
    
    # Provide troubleshooting steps
    Write-Host "`nTroubleshooting steps:" -ForegroundColor Magenta
    Write-Host "1. Verify service name" -ForegroundColor Yellow
    $apacheServices = Get-Service | Where-Object {$_.Name -like '*apache*' -or $_.DisplayName -like '*apache*'}
    if ($apacheServices) {
        Write-Host "   Found Apache services: " -NoNewline -ForegroundColor Yellow
        $apacheServices | ForEach-Object { Write-Host "$($_.Name) " -NoNewline -ForegroundColor Cyan }
        Write-Host ""
    }
    else {
        Write-Host "   No Apache services found." -ForegroundColor Yellow
    }
    Write-Host "2. Check Apache logs in default location:" -ForegroundColor Yellow
    $defaultLogPath = Join-Path ${env:ProgramFiles} 'Apache Software Foundation\Apache24\logs'
    Write-Host "   $defaultLogPath" -ForegroundColor Cyan
    Write-Host "3. Test manual restart in an elevated PowerShell:" -ForegroundColor Yellow
    Write-Host "   Stop-Service $serviceName -Force; Start-Service $serviceName" -ForegroundColor Cyan
}
finally {
    Pause-IfLaunchedFromExplorer
}

exit 0