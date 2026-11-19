param([string]$OutputPath)
$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$rootEngine=$PSScriptRoot
New-Item -ItemType Directory -Force -Path (Join-Path $rootEngine 'dist') | Out-Null
$destinationEngine=if($OutputPath){[IO.Path]::GetFullPath($OutputPath)}else{Join-Path $rootEngine 'dist\7X-Engine-1.1.3-Source.zip'}
New-Item -ItemType Directory -Force -Path (Split-Path $destinationEngine -Parent) | Out-Null
$tempEngine=Join-Path $rootEngine ('dist\source-'+[guid]::NewGuid().ToString('N')+'.zip')
$archiveEngine=[System.IO.Compression.ZipFile]::Open($tempEngine,[System.IO.Compression.ZipArchiveMode]::Create)
$countEngine=0
try {
 $sourceEngine=@(Get-ChildItem -LiteralPath $rootEngine -File)
 foreach($folderEngine in @('app','tests','.impeccable')){
  $pathEngine=Join-Path $rootEngine $folderEngine
  if(Test-Path -LiteralPath $pathEngine){$sourceEngine+=Get-ChildItem -LiteralPath $pathEngine -File -Recurse}
 }
 foreach($fileEngine in $sourceEngine){
  $relativeEngine=[IO.Path]::GetRelativePath($rootEngine,$fileEngine.FullName).Replace('\','/')
  if($relativeEngine -match '^\.impeccable/review/(headunit1024|headunit2560|compact720)/|^\.impeccable/review/(initial-|native-now)'){continue}
  if($fileEngine.Extension -in @('.p12','.jks','.apk','.class','.dex')){throw "Unexpected binary/private file in source list: $relativeEngine"}
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archiveEngine,$fileEngine.FullName,('engine-app/'+$relativeEngine))|Out-Null
  $countEngine++
 }
 foreach($fileEngine in (Get-ChildItem -LiteralPath (Join-Path $rootEngine 'dist') -File | Where-Object {$_.Extension -eq '.txt' -or $_.Name -in @('audio-probe.json','audio-tuning-tests.json','wrx-stream-tests.json','vehicle-input-tests.json','credits-source-check-1.1.3.json','bov-rpm-tests-1.1.2.json','gearbox-tests-1.1.2.json','profile-sounds-1.1.2.json','gearbox-trace-1.1.2.csv')})){
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archiveEngine,$fileEngine.FullName,('engine-app/dist/'+$fileEngine.Name))|Out-Null
  $countEngine++
 }
 foreach($relativeEngine in @('research/VEHICLE-AUDIO-1.1.0.md','research/kakimoto/REFERENCES.md','research/kakimoto/build-wrx-tables.cjs','research/kakimoto/analyze.cjs','research/kakimoto/wrx-extraction.json','research/kakimoto/analysis.json','research/kakimoto/gdb-analysis.json','research/kakimoto/gdb-e-analysis.json','research/kakimoto/spectrum.png','research/kakimoto/gdb-spectrum.png','research/kakimoto/gdb-e-spectrum.png','research/teslun-rev/STUDY-2026-11-18.md','research/teslun-rev/driving.jpg','research/teslun-rev/braking.jpg','research/teslun-rev/downshift.jpg','research/teslun-rev/idle.jpg','research/teslun-rev/settings.jpg')){
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archiveEngine,(Join-Path $rootEngine $relativeEngine),('engine-app/'+$relativeEngine))|Out-Null
  $countEngine++
 }
} finally {$archiveEngine.Dispose()}
$verifyEngine=[IO.Compression.ZipFile]::OpenRead($tempEngine)
try {
 foreach($requiredEngine in @('engine-app/build.ps1','engine-app/app/src/main/AndroidManifest.xml','engine-app/app/src/main/java/hk/local/enginex7/EngineSynth.java','engine-app/tests/DeviceTests.java','engine-app/DESIGN.md','engine-app/.impeccable/design.json')){if(!$verifyEngine.GetEntry($requiredEngine)){throw "Missing $requiredEngine"}}
 if($verifyEngine.Entries.FullName -match '(^|/)signing/|\.p12$|\.jks$|\.emulator/'){throw 'Private or emulator content included'}
}finally{$verifyEngine.Dispose()}
Move-Item -LiteralPath $tempEngine -Destination $destinationEngine -Force
[pscustomobject]@{Path=$destinationEngine;Entries=$countEngine;Bytes=(Get-Item -LiteralPath $destinationEngine).Length;SHA256=(Get-FileHash -LiteralPath $destinationEngine -Algorithm SHA256).Hash}|ConvertTo-Json
