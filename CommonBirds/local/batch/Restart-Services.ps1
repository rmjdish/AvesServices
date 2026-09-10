# Restart-Services.ps1
try {
    Write-Output "Starting service restart at $(Get-Date)"
    
    # Find and restart Apache (common service names)
    $apacheServices = @("Apache2.4", "httpd", "Apache", "apache")
    $apacheFound = $false
    
    foreach ($serviceName in $apacheServices) {
        $service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
        if ($service) {
            Write-Output "Found Apache service: $($service.DisplayName)"
            Write-Output "Stopping $serviceName..."
            Stop-Service -Name $serviceName -Force
            Start-Sleep -Seconds 5
            Write-Output "Starting $serviceName..."
            Start-Service -Name $serviceName
            Start-Sleep -Seconds 5
            $service.Refresh()
            if ($service.Status -eq 'Running') {
                Write-Output "Apache service restarted successfully"
            } else {
                Write-Output "Warning: Apache service may not be running properly"
            }
            $apacheFound = $true
            break
        }
    }
    
    if (-not $apacheFound) {
        Write-Output "No Apache service found with common names"
    }
    
    # Find and restart MySQL (common service names)
    $mysqlServices = @("MySQL", "MySQL80", "MySQL57", "MySQLServer", "mysql")
    $mysqlFound = $false
    
    foreach ($serviceName in $mysqlServices) {
        $service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
        if ($service) {
            Write-Output "Found MySQL service: $($service.DisplayName)"
            Write-Output "Stopping $serviceName..."
            Stop-Service -Name $serviceName -Force
            Start-Sleep -Seconds 10  # MySQL needs more time to stop
            Write-Output "Starting $serviceName..."
            Start-Service -Name $serviceName
            Start-Sleep -Seconds 10
            $service.Refresh()
            if ($service.Status -eq 'Running') {
                Write-Output "MySQL service restarted successfully"
            } else {
                Write-Output "Warning: MySQL service may not be running properly"
            }
            $mysqlFound = $true
            break
        }
    }
    
    if (-not $mysqlFound) {
        Write-Output "No MySQL service found with common names"
    }
    
    Write-Output "Service restart completed at $(Get-Date)"
    
} catch {
    Write-Output "Error occurred: $($_.Exception.Message)"
    exit 1
}