param([string]$JdkHome='C:\Users\HK\Documents\ChatGPT\7X\.build-tools\java\jdk-17.0.20.1+1')
$ErrorActionPreference='Stop'
$root7x=Split-Path $PSScriptRoot -Parent
$tools7x='C:\Users\HK\Documents\ChatGPT\7X\.build-tools\android-build\android-16'
$platform7x='C:\Users\HK\Documents\ChatGPT\7X\.build-tools\android-platform\android-36\android.jar'
$appBuild7x=Get-ChildItem -LiteralPath "$root7x\build" -Directory -Filter 'run-*' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
$out7x=Join-Path $root7x ('build\device-tests-'+[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path "$out7x\classes","$out7x\dex" | Out-Null
& "$JdkHome\bin\javac.exe" -encoding UTF-8 -source 8 -target 8 -classpath "$platform7x;$($appBuild7x.FullName)\classes" -d "$out7x\classes" "$PSScriptRoot\DeviceTests.java" "$PSScriptRoot\CoreTests.java"
if ($LASTEXITCODE) {throw 'Device test compilation failed'}
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory("$out7x\classes","$out7x\classes.jar")
& "$JdkHome\bin\java.exe" -cp "$tools7x\lib\d8.jar" com.android.tools.r8.D8 --min-api 28 --lib $platform7x --classpath "$($appBuild7x.FullName)\classes.jar" --output "$out7x\dex" "$out7x\classes.jar"
if ($LASTEXITCODE) {throw 'Device test DEX failed'}
& "$tools7x\aapt2.exe" link -I $platform7x --manifest "$PSScriptRoot\AndroidManifest.xml" --min-sdk-version 28 --target-sdk-version 28 -o "$out7x\unsigned.apk"
if ($LASTEXITCODE) {throw 'Device test package failed'}
$zip7x=[System.IO.Compression.ZipFile]::Open("$out7x\unsigned.apk",[System.IO.Compression.ZipArchiveMode]::Update)
try{[System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip7x,"$out7x\dex\classes.dex",'classes.dex')|Out-Null}finally{$zip7x.Dispose()}
& "$tools7x\zipalign.exe" -f 4 "$out7x\unsigned.apk" "$out7x\aligned.apk"
& "$JdkHome\bin\java.exe" -jar "$tools7x\lib\apksigner.jar" sign --ks "$root7x\signing\7x-engine.p12" --ks-key-alias engine --ks-pass pass:local-development-only --out "$root7x\build\device-tests.apk" "$out7x\aligned.apk"
if ($LASTEXITCODE) {throw 'Device test signing failed'}
Write-Output "$root7x\build\device-tests.apk"
