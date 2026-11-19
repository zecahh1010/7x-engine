param([string]$JdkHome='C:\Users\HK\Documents\ChatGPT\7X\.build-tools\java\jdk-17.0.20.1+1')
$ErrorActionPreference='Stop'
$rootEngine=Split-Path $PSScriptRoot -Parent
$outEngine=Join-Path $rootEngine 'build\core-tests'
New-Item -ItemType Directory -Force -Path $outEngine | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $rootEngine 'dist') | Out-Null
$coreEngine=Join-Path $rootEngine 'app\src\main\java\hk\local\enginex7'
& "$JdkHome\bin\javac.exe" -encoding UTF-8 -d $outEngine "$coreEngine\Profile.java" "$coreEngine\EcarxReader.java" "$coreEngine\VehicleSnapshot.java" "$coreEngine\EngineModel.java" "$coreEngine\WrxCycles.java" "$coreEngine\EngineSynth.java" "$PSScriptRoot\CoreTests.java" "$PSScriptRoot\VehicleInputTests.java" "$PSScriptRoot\GearboxTests.java" "$PSScriptRoot\ProfileSoundProbe.java"
if($LASTEXITCODE){throw 'Core compilation failed'}
& "$JdkHome\bin\java.exe" -cp $outEngine hk.local.enginex7.CoreTests "$rootEngine\dist\samples" | Tee-Object -FilePath "$rootEngine\dist\core-tests.txt"
if($LASTEXITCODE){throw 'Core tests failed'}
& "$JdkHome\bin\java.exe" -cp $outEngine hk.local.enginex7.VehicleInputTests | Tee-Object -FilePath "$rootEngine\dist\vehicle-input-tests.json"
if($LASTEXITCODE){throw 'Vehicle input regression failed'}
& "$JdkHome\bin\java.exe" -cp $outEngine hk.local.enginex7.GearboxTests "$rootEngine\dist\gearbox-trace-1.1.2.csv" | Tee-Object -FilePath "$rootEngine\dist\gearbox-tests-1.1.2.json"
if($LASTEXITCODE){throw 'Gearbox trace regression failed'}
& "$JdkHome\bin\java.exe" -cp $outEngine hk.local.enginex7.ProfileSoundProbe | Set-Content -Encoding UTF8 "$rootEngine\dist\profile-sounds-1.1.2.json"
if($LASTEXITCODE){throw 'Preset sound probe failed'}
