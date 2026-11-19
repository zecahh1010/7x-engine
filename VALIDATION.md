# 7X Engine 1.1.3 validation

2026-11-19, Hong Kong. Package `hk.local.enginex7`, version code **113**. Final APK SHA-256: `046D6C45394BCF61B8E3766595839C667F9429D9FF8D4DF6B1B3E4AF1B333B39`. Existing signing certificate, v3 signature and alignment verified. Permissions unchanged.

Credits is reachable through Settings → Credits / 設定 → 製作人員 and displays **Developer / 開發者 — Zeca**. Native screenshots were visually checked on genuine 1024×600/density160 and 720×1280/density240 AVDs, with 130% font size on the compact device. Chinese credits were inspected at both sizes; English credits and returning to Settings were inspected at wide size using actual native taps. Layout/text are readable and Back returns to Settings. Evidence: `.impeccable/review/v1.1.3-credits`.

This is a narrow manual native UI verification, not a new independent full-product review. No new unit-test suite was added for the low-impact page. Seven source hashes confirm EngineSynth, EngineModel, Profile, AudioOutput, VehicleInput, Config and WrxCycles are unchanged; prior sound/BOV/gearbox tests below are historical evidence for that unchanged implementation. The 4000-RPM BOV rule is retained. Hardware/system Back behavior is implemented in MainActivity but was not separately exercised on a physical car. No physical-car validation is claimed.

## 1.1.2 historical validation

2026-11-19, Hong Kong. Package `hk.local.enginex7`, version code **112**, min/target API28. Final APK SHA-256: `BFD427E9D797C1CA2CCDBFE1A9B5202ED16FE46E9438C56CBA4890A7B8F10C72`. Signature and alignment passed with the existing certificate; permissions/read-only ECARX APIs are unchanged.

- **33 core, 31 vehicle-input and 61 gearbox trace checks passed.** The raised/hysteretic shift decisions, rev-match and post-blip backfire are retained. Subaru release event scheduling now uses pre-lift RPM and the user-selected 4000-RPM threshold.
- **38 audio checks passed**, including 15 focused RPM-dependent BOV checks and six streaming regressions. Below-4000 events produce no BOV PCM; 4000 is the audible boundary. Light-pedal release above 4000 is not blocked by an unrelated load/pressure threshold; stale input produces no event; stored event RPM survives tachometer return to idle. Tail measurements increase monotonically: **470/730/1010/1270ms at 4000/5000/6000/7000 RPM**. See `dist/bov-rpm-tests-1.1.2.json`.
- A short-window low/mid pulse-separation metric rises from **0.489145 to 0.844875** versus 1.1.1. This quantifies stronger amplitude contrast, not perceived loudness or user approval. The slower idle cadence remains **10.65956Hz**; 55–220Hz body remains 1.424× at idle and 1.451× at 1800RPM versus 1.1.0. Source WRX cycle-table hash/provenance is retained; no GR86 data. Six other presets remain byte-identical at fixed inputs. Existing rev-match/backfire difference signals are unchanged. See `dist/audio-probe.json`, `dist/audio-tuning-tests.json`, `tests/fixtures/audio-1.1.1.json`.
- Streaming checks cover 17-second buffer-partition/phase continuity, 30-second RPM sweep, finite output, no interpolation holes, stop and restart. The sweep maximum float is below 1; headroom and adjacent-sample checks passed. These are software metrics, not an ARM64/head-unit timing or acoustic benchmark.
- **29 Android vehicle/audio checks passed on the exact final APK** at genuine 1024×600/density160 API28 x86 AVD size. They cover stationary pedal-driven revs, real-speed units, BOV release, brake/stale fallback, model/PCM continuing during a 600ms UI block, stereo output and speaker-test stop/failure states. The fixture now explicitly selects Subaru, independently of a persisted V10 upgrade-test selection. See `dist/android-vehicle-1.1.2.txt`.
- **Direct 1.1.1-to-final-1.1.2 upgrade passed: 2 setup and 4 verification checks.** Last sound and separate gains survive; relaunch stays silent. `dist/upgrade-before-1.1.2.txt`, `dist/android-persist-check-1.1.2.txt`.

Scope: audio/event tuning; selector/layout/routing unchanged except displayed version. Prior 1.1.1 native UI evidence remains historical, not a new full UI review. Pre-final/native fixture failures are not final artifact evidence. Current previews are synthetic shipping-DSP renders: 12-second burble, 16-second isolated BOV comparison (4000/5000/6000/7000 starts at 0/4/8/12 seconds), and 40-second driving simulation. Physical car SDK, ARM64 playback, speaker audibility/route and subjective sound quality remain unverified.

## 1.1.1 historical validation

2026-11-19, Hong Kong. `hk.local.enginex7`, version code **111**, min/target API 28, compile API 36. Final signed APK SHA-256: `646D15F5FF3266F5DEC376109CC425AEA3AC84F511906AFDB58120D374616D53`.

- **33 core and 31 ECARX/reflection checks passed.** Seven distinct nonzero engine outputs remain present; signal IDs, live pedal, brake priority and stale-input fallback are retained.
- **61 virtual gearbox trace checks passed.** Continuous fresh synthetic vehicle input covers raised upshift RPM, lift/no upshift, partial-lift lock, brake-only downshifts, dwell/hysteresis, low-RPM coast without backfire, high-RPM post-blip backfire, rolling attach at 100km/h and small alternating speed samples. Maximum RPM step during the braking trace was 77.02 RPM per 20ms tick. See `dist/gearbox-tests-1.1.1.json` and `dist/gearbox-trace-1.1.1.csv`. These are modeled traces, not physical drivetrain measurements.
- **20 audio checks passed**, including six streaming regressions: continuous RPM sweep, buffer-size invariance, phase wrap, finite signal/headroom, stop and restart. The original WRX cycle-table provenance/hash remains unchanged; GR86 is excluded. New fixed-input fingerprints confirm all six non-Subaru presets are byte-identical to 1.1.0. Shared gearbox scheduling deliberately changes dynamic driving samples. See `tests/fixtures/profile-sounds-1.1.0.json` and `dist/profile-sounds-1.1.1.json`.
- Subaru software metrics: idle 55–220Hz body **1.461×**, 1800-RPM body **1.488×** versus 1.1.0; idle cadence **10.660Hz**; BOV difference-signal tail above 0.001 RMS **660ms**, formerly 2670ms. Rev-match and scheduled-backfire difference RMS are 0.1871 and 0.1627. Sweep maximum float 0.8162, maximum adjacent delta 0.04656. These are software signal measurements, not SPL, physical sound quality or subjective approval.
- **45 Android operation checks passed** on the exact final APK, including dropdown selection, gains, PCM, background/minimize/restore, GPS expiry and Exit. `dist/android-suite-1.1.1.txt`.
- **44 selector checks passed on each of two genuine AVD sizes:** 1024×600/density160 and 720×1280/density240/fontScale1.3. All seven presets cycle in both directions; every selected and dropdown row has explicit foreground/background colors; a light parent theme does not replace them; the actual settings popup exposes readable options. Native screenshots were visually inspected. `dist/android-dropdown-emulator-5584-1.1.1.txt`, `dist/android-dropdown-emulator-5588-1.1.1.txt`.
- **28 vehicle/audio Android checks passed** at 1024×600: fresh pedal/speed, stationary revving/BOV, braking, stale fallback, independent background model/PCM during a blocked UI, test formats/Stop/route failure. Stale-input verification allows the existing smooth RPM ramp after the 1500ms freshness deadline, with a bounded 2600ms wait. `dist/android-vehicle-emulator-5584-1.1.1.txt`.
- **4 compact capture checks passed** with 130% font size, Traditional Chinese and English settings. `dist/android-shots-emulator-5588-1.1.1.txt`.
- **Direct 1.1.0-to-final-1.1.1 upgrade passed:** 2 baseline setup and 4 verification checks. V10 selection and +2/−3dB gains survive; relaunch is silent. `dist/upgrade-before-1.1.1.txt`, `dist/upgrade-after-1.1.1.txt`. Tests start independent modes in a fresh process to avoid reusing a service still exiting from the preceding mode.
- Signature/alignment passed with the existing certificate `22E4080F0334D2923587A05F2500B5670A4C3D0742851C3F389BD1C412E9FD2B`. Permissions and read-only ECARX APIs are unchanged.

Current captures: `.impeccable/review/v1.1.1-1024` and `v1.1.1-phone`. They are native **emulator** fixtures. The 1.1.0 independent review below is historical; this release received a narrow implementation/selector visual check, not a new independent full-product review. No physical head-unit SDK, OEM popup, ARM64, speaker-route/audibility or subjective audio test was possible. A 40-second preview rendered by the shipping model/DSP uses labelled synthetic input: `dist/7X-Engine-1.1.1-Subaru.wav`.

## 1.1.0 historical validation

2026-11-18, Hong Kong. Package `hk.local.enginex7`, version code **110**, min/target API 28, compile API 36. Exact final APK SHA-256: `1C45FB0C181008B5510054AB36D1868528C995D74D6E8F644AFA264BABAD9ACA`.

- **33 core, 31 vehicle-input/reflection and 20 audio checks passed.** ECARX fixtures cover the photographed throttle 0/61/100 and brake raw 14.84375, exact read-only signal IDs, m/s conversion, brake-switch priority, invalid/stale inputs, stationary revving and release BOV. Subaru recording-derived cycles and the other six profiles remain unchanged.
- **45 Android checks passed on the final APK**, including native controls, dropdown, independent persisted gains, background PCM, minimize/restore, 8dp minimized-button gap, GPS expiry, explicit routing failure and active-playback Exit. See `dist/device-tests-1.1.0.txt`.
- **28 vehicle/audio Android checks passed separately at 1024×600/density160 and 2560×1600/density160.** These cover actual model consumption of labelled synthetic ECARX snapshots, no GPS in vehicle mode, brake/stale idle fallback, PCM/model progress while deliberately blocking the UI thread for 600ms, 44.1/48kHz stereo tests, nearby test target/status, visible Stop test, and explicit missing BUS12. See `dist/vehicle-device-tests-1.1.0.txt` and `dist/large-vehicle-tests-1.1.0.txt`.
- **4 compact capture checks passed** at 720×1280/density240 with system font scale 1.3, including Traditional Chinese home/settings and English settings. These are three genuine AVD physical framebuffer sizes, without `wm size`/density overrides. `dist/physical-*-metrics.txt` records geometry.
- **Final signed reinstall persistence passed: 2 setup and 4 verification checks.** Last V10 selection and +2/−3dB gains survived `install -r`; relaunch stayed silent. This is an exact-final-version reinstall/process test, not a direct 1.0.2-to-final upgrade test. See `dist/reinstall-before-1.1.0.txt` and `dist/reinstall-after-1.1.0.txt`. Earlier upgrade logs are historical.
- **Bounded independent native UI finish review: ship within emulator scope.** The verdict pass resolved the four listed issues: nearby speaker-test feedback/Stop, attempted output identity, minimized control gap and current design documentation. See `.impeccable/review/FINISH-1.1.0.md`. No broader new physical-device review is claimed.
- APK v3 signature and alignment passed with the existing certificate `22E4080F0334D2923587A05F2500B5670A4C3D0742851C3F389BD1C412E9FD2B`. `MODIFY_AUDIO_SETTINGS` supports the explicit user-operated system media-volume slider. No INTERNET, microphone or vehicle-write permissions/API calls were added.

ECARX scalar reads use the same reflected APIs as the existing Explorer and the pinned `jlunnn/7X-U` commit `5bfad5b33f2ca80d3eddd0be1249076ddab14c70`. Each channel has an independent app-read timestamp and 1500ms freshness window; these are not ECU sample timestamps. Model physics, SDK polling and PCM run independently of the UI. Source references and limitations are in `research/VEHICLE-AUDIO-1.1.0.md`.

All current `physical-1024`, `physical-2560` and `physical-phone` capture directories contain **emulator fixtures**, not physical-car evidence. Failed/preliminary `wm` override captures are excluded from the source package. Final preview explicitly labels test data. Emulator PCM/playback-head progress cannot prove physical audibility or an amplifier route. Actual car SDK/classloader/permissions, cabin/exterior speaker audibility, ARM64 execution and OEM overlay/system insets remain unverified. Brake depth is raw and uncalibrated; the fresh brake switch takes priority. RPM/gear remain virtual engine modeling, not ECU RPM or vehicle control.

## 1.0.2 historical validation

2026-11-18, Hong Kong. Package `hk.local.enginex7`, version code **102**, min/target API 28. APK SHA-256: `BDDA6A512191F710094464CD061E3223D5FB89DE0F32F208C2E01C0F7C71B241`.

Current release evidence:

- **33 core assertions passed** with the recording-derived Subaru exhaust cycles.
- **20 audio checks passed**: finite/headroom checks, retained stronger/longer BOV, reference/hash provenance, other six profiles byte-identical to 1.0.0, and six streaming regressions. The latter include a 17-second buffer-partition comparison across phase wrap, a 30-second bidirectional RPM sweep with cut transitions, no interpolation silence, and fade/restart checks. Desktop rendering of the 30-second sweep took 0.595 s; this is not a head-unit performance benchmark. See `dist/audio-tuning-tests.json`, `dist/wrx-stream-tests.json`.
- **44 Android checks passed** on the exact installed 1.0.2 APK, including actual PCM streaming, selection of the renamed Subaru profile, floating panel, minimize/background/restore, independent gains, language controls, GPS fixtures/expiry and active-playback Exit. `dist/device-tests-1.0.2.txt`. No AndroidRuntime crash was logged during the run.
- **Upgrade from 1.0.1 passed**: 2 old-version setup and 4 new-version verification checks. V10 selection and +2/−3 dB independent gains survived `install -r`; relaunch stayed silent. The profile key `subaru` is retained despite its new display name. `dist/upgrade-before-1.0.2.txt`, `dist/upgrade-after-1.0.2.txt`.
- APK v3 signature and final alignment passed, with the same certificate as previous releases: `22E4080F0334D2923587A05F2500B5670A4C3D0742851C3F389BD1C412E9FD2B`. Permissions are unchanged; no INTERNET or microphone permission.

The only exhaust waveform/timing source is the user's **first WRX GT1.0Z MP3**. Its SHA-256 and four extraction windows are recorded in `research/kakimoto/wrx-extraction.json`. The two later Subaru GDB recordings were analyzed as supporting spectrum comparisons. **GR86 is excluded**. Method, source identities, reproduction and limitations are in `research/kakimoto/REFERENCES.md`.

The generated preview is `dist/7X-Engine-1.0.2-Subaru.wav` (same Java renderer). At fixed test inputs, the 55–220 Hz body estimator is 0.12567 at 820 RPM and 0.13311 at 1,800 RPM. BOV difference-signal RMS in its first 200 ms is 0.20447; its >0.001 RMS 10 ms-window tail lasts about 2.67 s, retaining the 1.0.1 effect. These are software measurements, not SPL or perceptual similarity scores. Sweep maximum float is 0.653 and maximum adjacent delta 0.0714.

Physical Zeekr audio, external/Both speaker output, ARM64 execution and subjective exhaust likeness remain unverified. There is no synchronized ECU RPM trace in the supplied recordings: the cycle-to-virtual-RPM mapping is a modeling assumption, with pitch extension beyond the highest extracted anchor. The existing UI design review remains the 1.0.0 baseline; this update changes audio, version text and the Subaru display name.

## 1.0.1 historical validation

2026-11-18. Package `hk.local.enginex7`, code **101**, same local signing certificate and permissions as 1.0.0. APK SHA-256: `7B82FF91647D6F31698EF1A011F4BC5177455A4646690C49E2F4491B20314858`.

1.0.1 verification (historical):

- **33 core assertions passed** with the revised synthesis.
- **11 audio-tuning checks passed**: stronger Subaru idle/low-mid body, louder/longer BOV, finite audio with headroom, and byte-identical fourteen-second WAVs for all six other profiles. `dist/audio-tuning-tests.json` and `tests/fixtures/audio-1.0.0.json`.
- **44 Android checks passed** on the exact 1.0.1 APK, including real PCM streaming, native engine selection, background playback, minimize/restore and active-playback Exit. `dist/device-tests-1.0.1.txt`.
- Upgrade tested from installed 1.0.0 to 1.0.1 using `install -r`: **2 setup and 4 verification checks passed**. V10 choice and separate +2/−3 dB settings remained, and relaunch stayed silent. `dist/upgrade-before-1.0.1.txt`, `dist/upgrade-after-1.0.1.txt`.
- Signature verification and final APK alignment passed. No additional runtime permission was introduced.

Subaru low-frequency body RMS is 1.94× at 820 RPM and 1.49× at 1,800 RPM, measured by the same 55–220 Hz difference-of-lowpass estimator. The BOV difference signal's first 200 ms RMS increased 5.91 dB. Ten-millisecond windows above RMS 0.001 (approximately −60 dBFS in the float-domain difference signal) persist for 2.68 s versus 1.12 s. These are deterministic software comparisons, not measured speaker SPL or a subjective sound-quality verdict. Probe maximum absolute float sample: 0.604. The preview WAV is generated from the same updated Java synthesis/model.

This is an audio-only update plus the version label. The seven-screen native UI review below belongs to the 1.0.0 baseline; no new whole-interface review or physical-car verification is claimed for 1.0.1. Actual Subaru likeness, exterior-speaker loudness and sustained target-car performance require listening on the car.

## 1.0.0 baseline evidence

Release date: 2026-11-18, Hong Kong. Package `hk.local.enginex7`, version 1.0.0 / code 100, min/target API 28, compile API 36. No native libraries; physical ARM64 execution remains untested.

## Build and signing

The offline PowerShell build compiled native Java sources, packaged DEX/resources, aligned the APK and verified its APK Signature Scheme v3 signature. APK SHA-256: `E4E0E7576D9274C516184E24BC50C1E9D00279EB99E30350C67DB35FF0D7393D`. Certificate SHA-256: `22E4080F0334D2923587A05F2500B5670A4C3D0742851C3F389BD1C412E9FD2B`. The private signing key is retained locally and excluded from source exports.

The production manifest requests foreground service, application overlay, fine/coarse location and wake lock. No INTERNET, microphone, raw CAN, vehicle setter, root or test-only exported control permission is requested.

## Automated evidence

- Desktop pure Java tests: **33 assertions passed**, plus finite-sample checks throughout seven synthesis outputs. Covers startup stages, zero-pedal idle, acceleration/upshift, boost, throttle-release BOV and cooldown, braking/downshift, stopped idle, shutdown, moving GPS inputs, GPS expiry, dB conversion, peak limiting, distinct nonzero PCM for all seven profiles and a BOV-triggered PCM difference.
- Android 9 / API 28 x86 head-unit suite: **44 checks passed** on the exact release APK; `dist/device-tests.txt`. It exercises the installed app, not a web imitation. Scope includes native overlay, actual touchscreen selection in the popup engine menu, actual route streaming, gain controls, language, output-unavailable handling, minimized background frame advancement, restore, GPS Location fixtures/expiry and Exit while actively playing.
- Genuine 720×1280, density 240 compact emulator at **130% system font**: **4 checks passed**, including three native captures in Traditional Chinese and English. `dist/compact-tests.txt`.
- Forced process restart: **2 setup checks and 4 verification checks passed**. V10 selection and independent +2 / −3 dB settings survived force-stop/relaunch; relaunch remained silent. `dist/persist-set.txt`, `dist/persist-check.txt`.
- Android GPS-provider check: **7 checks passed** on the exact release APK; `dist/gps-tests.txt`. Uses emulator console coordinates, separately from in-process Location fixtures, and checks that opening Android Location Settings minimizes the large overlay. It is not a physical satellite-reception or road-driving test.

The test-only instrumentation APK is built separately and is not included in the production APK. QA overlay/location permissions were granted only inside task-created emulators. Debug ADB root was used only on those emulators to retrieve private screenshots, not by the app.

## Native visual evidence

`.impeccable/review/` contains head-unit home, driving, settings, minimized overlay, emulator GPS and compact Traditional Chinese/English large-text captures. They are actual installed-app screenshots. The pink launcher/background outside the overlay belongs to the stock emulator.

An initial attempt to resize the head-unit emulator produced cropped/letterboxed screenshots; those were rejected and replaced by a separate genuine portrait AVD. Concurrent emulators also exposed a host rendering stall: thread diagnostics stopped in the emulator GLES pipe / `RenderThread`, with instrumentation waiting in `startActivitySync`. The emulator launcher now uses software graphics with Vulkan disabled, and final runs use one emulator at a time. These failures are not reported as passed checks.

The first API 28 run caught a newer `SeekBar.setMinHeight` method call; it was replaced with the API-compatible `View.setMinimumHeight` before successful device checks. No crash from that initial build is treated as final-release evidence. Popup automation initially matched invisible accessibility nodes or checked asynchronous selection too early; final tests filter visible nonempty bounds, inject properly timed touchscreen events, and wait for the selected engine. Normal ADB touch also independently selected V10 and saved it. These harness failures are not counted as passed product checks.

## Audio evidence and limits

`dist/samples/` contains fourteen-second WAV previews generated by the same original Java model/synth. Timeline: ignition, idle, acceleration and release; turbo profiles can generate BOV after boost. These are software-generated PCM, not microphone recordings or proof of physical amplifier output. Peak limiting and nonfinite rejection are checked; subjective similarity to an actual Subaru exhaust requires listening.

Actual internal AudioTrack routing and PCM streaming are tested on the emulator. The emulator has no BUS12 exterior speaker: external/Both requests must fail clearly. Exterior routing, two simultaneous physical outputs, audible output, dB loudness changes, actual synchronization, target-car permission policy and long-duration performance remain unverified.

This release uses GPS/motion-estimated load; no calibrated ECARX pedal signal is connected. Tachometer, RPM, boost and gears are virtual and do not actuate the EV. Application overlays remain subject to Android and OEM restrictions; background playback is not a guarantee against force-stop, process termination or vehicle sleep.

## Finish review

Independent native finish reviewer returned **ship** for seven named native screenshots (head-unit home/driving/settings/minimized and three compact large-text screens), with no material UI corrections. Review scope is screenshots and relevant UI source, not independently rerun tests or hardware; the final backend-only OFF reset is covered by the core and release Android tests. The newly refreshed GPS capture is additional evidence, outside that seven-screen review. See `.impeccable/review/finish-review.md`.

Independent documenter produced and rechecked token-bearing `DESIGN.md` and `.impeccable/design.json`; JSON parses and 38 token references resolve. YAML received structural checks; a separate YAML parser was unavailable. No HTML/CSS design detector is applicable to native Java Views/Canvas.
