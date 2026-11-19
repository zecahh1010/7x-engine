param(
 [string]$JdkHome = $env:JAVA_HOME,
 [string]$BuildTools,
 [string]$PlatformJar,
 [string]$KeyStore,
 [switch]$Test
)
$ErrorActionPreference = 'Stop'
$projectPath7x = $PSScriptRoot
$knownTools7x = 'C:\Users\HK\Documents\ChatGPT\7X\.build-tools'
if (!$JdkHome -and (Test-Path -LiteralPath "$knownTools7x\java")) { $JdkHome = (Get-ChildItem -LiteralPath "$knownTools7x\java" -Directory | Select-Object -First 1).FullName }
if (!$BuildTools -and (Test-Path -LiteralPath "$knownTools7x\android-build\android-16")) { $BuildTools = "$knownTools7x\android-build\android-16" }
if (!$PlatformJar -and (Test-Path -LiteralPath "$knownTools7x\android-platform\android-36\android.jar")) { $PlatformJar = "$knownTools7x\android-platform\android-36\android.jar" }
if (!(Test-Path -LiteralPath "$JdkHome\bin\javac.exe") -or !(Test-Path -LiteralPath "$BuildTools\aapt2.exe") -or !(Test-Path -LiteralPath $PlatformJar)) { throw 'Supply -JdkHome JDK17 -BuildTools SDK-build-tools-directory -PlatformJar android.jar (API 35 or 36).' }
$buildDir7x = Join-Path $projectPath7x ('build\run-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path "$buildDir7x\classes", "$buildDir7x\dex", "$projectPath7x\dist", "$projectPath7x\signing" | Out-Null
& "$BuildTools\aapt2.exe" compile --dir "$projectPath7x\app\src\main\res" -o "$buildDir7x\resources.zip"
if ($LASTEXITCODE) { throw 'Resource compilation failed' }
& "$BuildTools\aapt2.exe" link -I $PlatformJar --manifest "$projectPath7x\app\src\main\AndroidManifest.xml" --min-sdk-version 28 --target-sdk-version 28 -o "$buildDir7x\unsigned.apk" "$buildDir7x\resources.zip"
if ($LASTEXITCODE) { throw 'Manifest/resource linking failed' }
$sources7x = @(Get-ChildItem -LiteralPath "$projectPath7x\app\src\main\java" -Filter '*.java' -Recurse | ForEach-Object FullName)
& "$JdkHome\bin\javac.exe" -encoding UTF-8 -source 8 -target 8 -classpath $PlatformJar -d "$buildDir7x\classes" @sources7x
if ($LASTEXITCODE) { throw 'Java compilation failed' }
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory("$buildDir7x\classes", "$buildDir7x\classes.jar")
& "$JdkHome\bin\java.exe" -cp "$BuildTools\lib\d8.jar" com.android.tools.r8.D8 --min-api 28 --lib $PlatformJar --output "$buildDir7x\dex" "$buildDir7x\classes.jar"
if ($LASTEXITCODE) { throw 'DEX compilation failed' }
$archive7x = [System.IO.Compression.ZipFile]::Open("$buildDir7x\unsigned.apk", [System.IO.Compression.ZipArchiveMode]::Update)
try { [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive7x,"$buildDir7x\dex\classes.dex",'classes.dex') | Out-Null } finally { $archive7x.Dispose() }
& "$BuildTools\zipalign.exe" -f -p 4 "$buildDir7x\unsigned.apk" "$buildDir7x\aligned.apk"
if ($LASTEXITCODE) { throw 'Alignment failed' }
if (!$KeyStore) { $KeyStore = "$projectPath7x\signing\7x-engine.p12" }
$password7x = $env:ENGINE_KEYSTORE_PASSWORD
if (!$password7x) { $password7x = 'local-development-only' }
if (!(Test-Path -LiteralPath $KeyStore)) {
 & "$JdkHome\bin\keytool.exe" -genkeypair -keystore $KeyStore -storetype PKCS12 -alias engine -keyalg RSA -keysize 3072 -validity 3650 -storepass $password7x -keypass $password7x -dname 'CN=7X Engine Local Development, O=Local Offline Audio'
 if ($LASTEXITCODE) { throw 'Local signing key creation failed' }
}
$apk7x = "$projectPath7x\dist\7X-Engine-1.1.3.apk"
& "$JdkHome\bin\java.exe" -jar "$BuildTools\lib\apksigner.jar" sign --ks $KeyStore --ks-key-alias engine --ks-pass "pass:$password7x" --key-pass "pass:$password7x" --out $apk7x "$buildDir7x\aligned.apk"
if ($LASTEXITCODE) { throw 'Signing failed' }
& "$JdkHome\bin\java.exe" -jar "$BuildTools\lib\apksigner.jar" verify --verbose --print-certs $apk7x | Tee-Object -FilePath "$projectPath7x\dist\signature.txt"
if ($LASTEXITCODE) { throw 'Signature verification failed' }
& "$BuildTools\zipalign.exe" -c 4 $apk7x
if ($LASTEXITCODE) { throw 'Final alignment verification failed' }
& "$BuildTools\aapt.exe" dump badging $apk7x | Set-Content -LiteralPath "$projectPath7x\dist\apk-badging.txt"
if ($LASTEXITCODE) { throw 'Package inspection failed' }
& "$BuildTools\aapt.exe" dump permissions $apk7x | Tee-Object -FilePath "$projectPath7x\dist\permissions.txt"
if ($LASTEXITCODE) { throw 'Permission inspection failed' }
if ((Get-Content -LiteralPath "$projectPath7x\dist\permissions.txt" -Raw) -match 'android.permission.INTERNET') { throw 'Unexpected network permission' }
$hash7x = Get-FileHash -LiteralPath $apk7x -Algorithm SHA256
"$($hash7x.Hash)  7X-Engine-1.1.3.apk" | Set-Content -LiteralPath "$projectPath7x\dist\SHA256SUMS.txt"
$hash7x
if ($Test) { & "$projectPath7x\tests\test.ps1" -JdkHome $JdkHome; & "$projectPath7x\tests\test-audio-tuning.ps1" -JdkHome $JdkHome }
