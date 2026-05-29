# Libere les ports utilises par HireHub (Windows).
# Usage : .\scripts\free-hirehub-ports.ps1

$ports = @(8761, 8888, 8089, 8081, 8092, 8083, 8084, 8085, 8086, 8093, 8090)

foreach ($port in $ports) {
    $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conns) {
        $pids = $conns | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($procId in $pids) {
            $name = (Get-Process -Id $procId -ErrorAction SilentlyContinue).ProcessName
            Write-Host "Port $port -> PID $procId ($name) : arret..."
            Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        }
    } else {
        Write-Host "Port $port : libre"
    }
}

Write-Host "Termine."
