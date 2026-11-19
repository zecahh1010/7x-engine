# 7X Engine 1.1.3

Developer: **Zeca** · [GitHub repository](https://github.com/zecahh1010/7x-engine)

Zeekr 7X 車機用原生 Android 引擎聲浪 App。Android 9（API 28）以上，離線運作；只讀取車輛資料，不控制車輛。

## 安裝

直接覆蓋安裝 `dist/7X-Engine-1.1.3.apk`，不要先解除安裝，即可保留上次音色、內／外 dB 和其他設定。由較舊版本首次更新會把原來 GPS 模式切換為 **ECARX 實車**；之後可自行選回 GPS 或模擬。開 App 不會自動出聲，須按「啟動引擎」。

## 製作人員

開啟 **設定 → 製作人員**（English: **Settings → Credits**）可查看開發者 **Zeca**。返回會回到設定，再返回主畫面。

## 1.1.2 明顯煲水聲與轉數放氣

Subaru 低轉重新突出「卜卜卜」的強弱脈衝和間隔，保留較疏、低沉的節奏。放氣由**收油前 4000 轉**開始，轉數越高尾音越長；4000／5000／6000／7000 轉的軟件估計約為 0.47／0.73／1.01／1.27 秒。讀值過期不會觸發放氣。其他六款引擎、補油／回火效果及升降波規則保留。

`dist/7X-Engine-1.1.2-Burble.wav` 可立即試聽煲水聲；`7X-Engine-1.1.2-BOV-RPM.wav` 是四段轉數的獨立放氣聲比較。實際車內外喇叭仍須聽測。

## 1.1.1 修正

- 設定及音色選單改用固定深色底／淺色字，避免車機的系統選單主題令文字消失。
- 七款引擎全部保留，主畫面新增「上一款聲效／下一款聲效」，可直接輪流選擇。
- Subaru 節奏放慢、低頻加厚、放氣尾音縮短。Downshift 補油加強；高轉補油後有短回火，慢車低轉沒有回火。
- 升波轉速提高；收油鎖定升波，重新加油才解除。加入穩定判斷、轉波間隔及較平滑的降波過程。

七款為 Subaru WRX、美式 V8、意式 V8、直六 Turbo、V10、V12 GT、經典 Boxer Turbo。新增效果仍屬虛擬引擎模型；音色喜好與實際喇叭效果須車上聽測。

## 實車油門與腳煞

1. 選 **ECARX 實車**，毋須 GPS 權限。主畫面的油門讀值應隨踏板改變；沒有有效讀值會顯示 `—` 與等候狀態。
2. 選音色，再按「啟動引擎」。點火後，油門影響虛擬轉速與負載；停車踩油門亦有反應。車速由 ECARX 讀取，虛擬換檔仍是聲浪模型。
3. 有效腳煞開關會優先收油。腳煞深度顯示原始值，尚未作完整行程校準。
4. 如讀不到，開「設定」的實車輸入區，可看到各訊號讀值、讀取時間和 SDK 錯誤。超過 1.5 秒沒有有效油門讀取會回怠速。

訊號：油門 `0x00101400`、腳煞深度 `0x00101300`、腳煞開關 `0x20317A00`、車速 `0x00100100`（m/s，畫面乘 3.6）。油門 0–100 依使用者照片所示的 0／61／100 使用；腳煞深度量程仍待確認。這版 Engine 套件的實車 SDK 存取仍需車上確認，不能以 Explorer 的成功代替。

## 喇叭試音與無聲排查

1. 先熄匙，開「設定」，按 **車內試音**。會送出兩聲提示音，毋須踩踏板或取得 GPS。
2. 檢查 **App 音量**與**系統媒體音量**均大於 0。車內／車外各有 `−1 dB`、`+1 dB` 及重設，範圍 −36 至 +18 dB。
3. 車內自動模式跟隨車機正常媒體路由；車外自動找 `BUS12_OUTER_NOTIFY`。可手動對應裝置；「重新列出裝置」會刷新清單。
4. 若仍無聲，可在熄匙時切換 **44.1 kHz／48 kHz 立體聲**再試音。主畫面與設定會顯示錯誤；設定另列實際裝置、PCM 送出量與播放進度。
5. **車外試音**可獨立測試 BUS12。找不到或路由不符會報錯。車內＋車外若實際對到同一裝置，不會宣稱雙路成功。

「PCM 已送出」及播放進度表示 Android 接收了數位音訊，不代表功放和實際喇叭必定有聲。實車出聲仍須聽測。系統音量只在使用者操作滑桿時調整；App 不會自行把系統音量推高。dB 是相對 App 增益，並非實測聲壓。

## 模擬、GPS、浮窗

- **模擬駕駛**：拖油門滑桿、按住剎車、收油或啟用自動示範。數字始終標示為模擬。
- **GPS 實車**：獨立的 GPS／加速度估算負載模式；需精確定位，無有效定位時保持怠速。這不是讀取實際踏板。
- **浮窗**：首次授權顯示於其他 App 上層。大畫面採滿高雙欄，窄視窗以單欄捲動呈現；比例參照 Outer Voice。
- **收起**：變為右上角還原／退出面板，聲浪及已啟用的輸入繼續。
- **退出**：停止引擎、音訊、GPS、ECARX 讀取執行緒、浮窗及服務。通知亦有還原、熄匙、退出。
- **繁體中文／English**及外觀可在設定選擇。設定下方列出實際視窗像素、density 和 UI 比例，供車機尺寸排查。

## 音色

Subaru WRX GT1.0Z＋BOV、V8、意式 V8、直六 Turbo、V10、V12、經典 Boxer Turbo。第一段使用者 WRX 錄音提供 Subaru 排氣循環，兩段新增 Subaru 只作聲底對照，GR86 已排除。現行調音保留明顯煲水聲及較疏節奏；放氣由收油前 4000 轉開始，轉數越高尾音越長。詳見 `RELEASE-1.1.2.md` 及 `research/kakimoto/REFERENCES.md`。

## 建置與證據

```powershell
.\build.ps1 -Test
.\tests\build-device-tests.ps1
.\start-emulator.ps1
```

PowerShell 7+、JDK 17／Android compile API 36、min/target API 28、Java 8。本機腳本預設重用相鄰 7X 工作區工具；新電腦可明確指定安裝路徑：

```powershell
.\build.ps1 -JdkHome 'C:\tools\jdk-17' -BuildTools 'C:\Android\Sdk\build-tools\36.0.0' -PlatformJar 'C:\Android\Sdk\platforms\android-36\android.jar' -Test
```

亦可透過 Android Studio 開啟本目錄，或以已安裝的 Gradle 使用 `gradle :app:assembleDebug`；本倉庫沒有附 Gradle wrapper。`tests/build-device-tests.ps1` 和 `start-emulator.ps1` 是本機 QA 工具，內有本機 SDK/AVD 路徑，換電腦須先調整。

簽署金鑰只保留在本機 `signing/`，不在 Git 倉庫或原始碼包內。沒有原有金鑰時，建置腳本會產生新的本機金鑰，不能用來覆蓋已安裝且以舊金鑰簽署的版本。`dist/`、建置快取、模擬器及原始錄音均不提交；已生成的 `WrxCycles.java` 與測試基準已包含，正常建置毋須原始 MP3。

`package-source.ps1` 使用倉庫內的需求文件及研究摘要，可從獨立 checkout 產生原始碼 ZIP。可用 `-OutputPath` 另存，避免覆蓋現有交付包。

套件 `hk.local.enginex7`。權限為前景服務、浮窗、定位、wake lock 及一般 `MODIFY_AUDIO_SETTINGS`；沒有網絡、麥克風、root 或車輛寫入權限。測試用 instrumentation APK 獨立建置，不包含在正式 APK。

`VALIDATION.md` 分開記錄程式測試、原生模擬器與尚待實車確認的範圍。新版讀取、音訊和來源見 `research/VEHICLE-AUDIO-1.1.0.md`；製作人員頁見 `RELEASE-1.1.3.md`。介面操作機制最初參考 [Teslun Engine](https://teslun.hk/rev)，本次原生介面及輸出參考本機 Outer Voice，ECARX 訊號另核對 [7X-U](https://github.com/jlunnn/7X-U/)。
