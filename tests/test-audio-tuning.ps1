param([string]$JdkHome='C:\Users\HK\Documents\ChatGPT\7X\.build-tools\java\jdk-17.0.20.1+1')
$ErrorActionPreference='Stop'
$rootEngine=Split-Path $PSScriptRoot -Parent
$classesEngine=Join-Path $rootEngine 'build\core-tests'
& "$JdkHome\bin\javac.exe" -encoding UTF-8 -cp $classesEngine -d $classesEngine "$PSScriptRoot\AudioTuningProbe.java" "$PSScriptRoot\WrxAudioTests.java" "$PSScriptRoot\BovRpmTests.java"
if($LASTEXITCODE){throw 'Audio probe compilation failed'}
$jsonEngine=& "$JdkHome\bin\java.exe" -cp $classesEngine hk.local.enginex7.AudioTuningProbe "$rootEngine\dist\audio-probe"
if($LASTEXITCODE){throw 'Audio probe failed'}
$jsonEngine|Set-Content -LiteralPath "$rootEngine\dist\audio-probe.json"
$newEngine=$jsonEngine|ConvertFrom-Json
$oldEngine=Get-Content "$PSScriptRoot\fixtures\audio-1.1.0.json" -Raw|ConvertFrom-Json
$checksEngine=0
$previousEngine=Get-Content "$PSScriptRoot\fixtures\audio-1.1.1.json" -Raw|ConvertFrom-Json
if($newEngine.burbleContrast -lt $previousEngine.burbleContrast*1.4){throw 'Requested distinct burble pulse separation is insufficient'};$checksEngine++
if([Math]::Abs($newEngine.idleCadenceHz-$previousEngine.idleCadenceHz) -gt .05){throw 'Slower burble cadence changed unexpectedly'};$checksEngine++
if([Math]::Abs($newEngine.revMatchDeltaRms-$previousEngine.revMatchDeltaRms) -gt .00001 -or [Math]::Abs($newEngine.backfireDeltaRms-$previousEngine.backfireDeltaRms) -gt .00001){throw 'Rev-match/backfire should be retained'};$checksEngine++
if($newEngine.idleBodyRms -lt $oldEngine.idleBodyRms*1.2 -or $newEngine.midBodyRms -lt $oldEngine.midBodyRms*1.2){throw 'Requested bass body increase is insufficient'};$checksEngine++
if($newEngine.idleCadenceHz -lt 10 -or $newEngine.idleCadenceHz -gt 11.5){throw 'Slower idle cadence is outside the intended range'};$checksEngine++
if($newEngine.bovAudibleTailMs -gt 850 -or $newEngine.bovAudibleTailMs -lt 250){throw 'Requested short BOV tail is outside 250-850ms'};$checksEngine++
if($newEngine.revMatchDeltaRms -lt .04){throw 'Rev-match breath has insufficient PCM energy'};$checksEngine++
if($newEngine.backfireDeltaRms -lt .03){throw 'Scheduled backfire has insufficient PCM energy'};$checksEngine++
if($newEngine.maxAbsoluteFloat -gt 1){throw 'Audio probe exceeds PCM headroom'};$checksEngine++
$beforeEngine=Get-Content "$PSScriptRoot\fixtures\profile-sounds-1.1.0.json" -Raw|ConvertFrom-Json
$afterEngine=Get-Content "$rootEngine\dist\profile-sounds-1.1.2.json" -Raw|ConvertFrom-Json
foreach($idEngine in @('v8','flat8','i6','v10','v12','boxer')){if($beforeEngine.$idEngine -ne $afterEngine.$idEngine){throw "Unexpected preset DSP change: $idEngine"};$checksEngine++}
$provenanceEngine=Get-Content "$rootEngine\research\kakimoto\wrx-extraction.json" -Raw|ConvertFrom-Json
if($provenanceEngine.gr86Used -or $provenanceEngine.referenceSha256 -ne '3D297F42D250CC32CA4A58E2947AF6223DD72596A5182FCB5E31ECCE5324FD48'){throw 'Wrong reference provenance'};$checksEngine++
if((Get-FileHash "$rootEngine\app\src\main\java\hk\local\enginex7\WrxCycles.java" -Algorithm SHA256).Hash -ne $provenanceEngine.javaSha256){throw 'Generated cycles do not match reference provenance'};$checksEngine++
$streamEngine=& "$JdkHome\bin\java.exe" -cp $classesEngine hk.local.enginex7.WrxAudioTests
if($LASTEXITCODE){throw 'WRX streaming regressions failed'}
$streamEngine|Set-Content "$rootEngine\dist\wrx-stream-tests.json"
$checksEngine+=($streamEngine|ConvertFrom-Json).assertions
$bovEngine=& "$JdkHome\bin\java.exe" -cp $classesEngine hk.local.enginex7.BovRpmTests "$rootEngine\dist\audio-probe"
if($LASTEXITCODE){throw 'RPM-dependent BOV regression failed'}
$bovEngine|Set-Content "$rootEngine\dist\bov-rpm-tests-1.1.2.json"
$checksEngine+=($bovEngine|ConvertFrom-Json).assertions
Copy-Item -LiteralPath "$rootEngine\dist\audio-probe\subaru-driving.wav" -Destination "$rootEngine\dist\7X-Engine-1.1.2-Subaru.wav" -Force
Copy-Item -LiteralPath "$rootEngine\dist\audio-probe\subaru-burble.wav" -Destination "$rootEngine\dist\7X-Engine-1.1.2-Burble.wav" -Force
Copy-Item -LiteralPath "$rootEngine\dist\audio-probe\bov-rpm-comparison.wav" -Destination "$rootEngine\dist\7X-Engine-1.1.2-BOV-RPM.wav" -Force
[ordered]@{result='PASS';assertions=$checksEngine;reference='First WRX GT1.0Z cycles, explicit low-RPM burble envelope';burbleContrastBefore=$previousEngine.burbleContrast;burbleContrastAfter=$newEngine.burbleContrast;gr86Used=$false;idleBodyRatioVs110=$newEngine.idleBodyRms/$oldEngine.idleBodyRms;midBodyRatioVs110=$newEngine.midBodyRms/$oldEngine.midBodyRms;idleCadenceHz=$newEngine.idleCadenceHz;bovThresholdRpm=4000;bovTailByRpm=($bovEngine|ConvertFrom-Json);bovTailBeforeMs=$oldEngine.bovAudibleTailMs;bovTailAfterMs=$newEngine.bovAudibleTailMs;revMatchDeltaRms=$newEngine.revMatchDeltaRms;backfireDeltaRms=$newEngine.backfireDeltaRms;maxAbsoluteFloat=$newEngine.maxAbsoluteFloat;otherSixProfilesFixedInputByteIdentical=$true;physicalSpeakerMeasured=$false}|ConvertTo-Json|Tee-Object -FilePath "$rootEngine\dist\audio-tuning-tests.json"
