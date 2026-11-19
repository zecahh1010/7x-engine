param([string]$AvdName='EngineQA',[int]$Port=5582,[int]$Width=1280,[int]$Height=720,[int]$Density=160)
$ErrorActionPreference='Stop'
$project7x=$PSScriptRoot
$qa7x=Join-Path $project7x '.emulator'
$sdk7x='C:\Users\HK\Documents\ChatGPT\7X\.build-tools'
New-Item -ItemType Directory -Force -Path "$qa7x\avd\$AvdName.avd", "$qa7x\home" | Out-Null
$env:ANDROID_AVD_HOME="$qa7x\avd"
$env:ANDROID_USER_HOME="$qa7x\home"
$env:ANDROID_EMULATOR_HOME="$qa7x\home"
$env:ANDROID_SDK_ROOT=$sdk7x
@"
avd.ini.encoding=UTF-8
path=$qa7x\avd\$AvdName.avd
target=android-28
"@ | Set-Content -LiteralPath "$qa7x\avd\$AvdName.ini"
@"
avd.ini.encoding=UTF-8
hw.cpu.arch=x86
hw.cpu.ncore=2
hw.ramSize=1536
hw.lcd.width=$Width
hw.lcd.height=$Height
hw.lcd.density=$Density
hw.keyboard=yes
hw.gpu.enabled=yes
hw.gpu.mode=swiftshader
image.sysdir.1=$sdk7x\system-images\x86\
disk.dataPartition.size=2147483648
vm.heapSize=256
showDeviceFrame=no
"@ | Set-Content -LiteralPath "$qa7x\avd\$AvdName.avd\config.ini"
Start-Process -FilePath "$sdk7x\emulator-package\emulator\emulator.exe" -ArgumentList @('-avd',$AvdName,'-port',"$Port",'-no-window','-no-audio','-no-snapshot','-gpu','software','-feature','-Vulkan','-accel','auto','-no-boot-anim') -WindowStyle Hidden -RedirectStandardOutput "$qa7x\$AvdName-stdout.log" -RedirectStandardError "$qa7x\$AvdName-stderr.log"
Write-Output "Started isolated API 28 emulator on emulator-$Port; user data is inside engine-app workspace."
