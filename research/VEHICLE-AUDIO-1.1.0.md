# Vehicle and audio reference evidence

2026-11-18, Hong Kong.

## User physical-car evidence

Eight user-supplied car photographs show the old Engine in GPS mode, waiting for GPS, with a fixed-height small gauge and large blank lower region. Its "audio playing" text conflicted with the user's report of no audible output. The same user's ECARX Explorer photographs show throttle `0`, `61`, `100` at `0x00101400`, brake `0` and `14.84375` at `0x00101300`, and brake switch `1` at `0x20317A00`. This establishes useful input observations on the user's vehicle, not successful access from the new Engine package or speaker audibility.

## Read path

The implementation reuses this workspace's own Explorer `EcarxReader.java`, changing only its Java package. It resolves public interfaces even when the manager implementation class is not public. Call path: `com.ecarx.xui.adaptapi.car.Car.create(Context)` → `getSensorManager()` → `getSensorLatestValue(int)` for speed/throttle/brake depth; `getICarFunction()` → `getFunctionValue(int)` for the brake switch. No setters, vehicle commands, CAN writes or SDK binary imports.

The user-provided [7X-U repository](https://github.com/jlunnn/7X-U/) was fetched read-only at commit `5bfad5b33f2ca80d3eddd0be1249076ddab14c70`:

- [Signal.java](https://github.com/jlunnn/7X-U/blob/5bfad5b33f2ca80d3eddd0be1249076ddab14c70/app/src/main/java/com/sevenxu/eq/Signal.java): IDs and m/s speed unit.
- [EcarxSource.java](https://github.com/jlunnn/7X-U/blob/5bfad5b33f2ca80d3eddd0be1249076ddab14c70/app/src/main/java/com/sevenxu/eq/EcarxSource.java): the reflection manager/getter path.
- [VehicleStateMapper.java](https://github.com/jlunnn/7X-U/blob/5bfad5b33f2ca80d3eddd0be1249076ddab14c70/app/src/main/java/com/sevenxu/eq/VehicleStateMapper.java): m/s conversion and percent convention; its documentation still treats brake/throttle calibration as provisional. User photographs add throttle endpoint observations, but not a full brake calibration.

Only IDs, API contracts and observations are referenced; no upstream telemetry, EQ or control implementation is imported. Polling runs on an owned HandlerThread at nominal 100 ms intervals, serializing SDK reads. A successful scalar read gets its own monotonic timestamp and is published immediately. Input expires after 1.5 s without a successful read. This age is **application read age**, not guaranteed ECU sample age: `getSensorLatestValue` can itself return cached data. SDK calls may delay a poll; no measured vehicle latency is claimed. Reconnection is retried, and the owned thread is stopped on Exit.

At zero vehicle speed, fresh throttle controls a virtual free-rev target. At speed, measured ECARX speed drives the virtual gearing while throttle controls load. A fresh brake switch overrides depth for pressed/released determination; depth is the fallback if the switch is missing. GPS is a separate explicitly selected estimated-load mode, never silently substituted for ECARX. All RPM/gears are virtual; the app never changes the car's real drivetrain.

The model runs on its own approximately 20 ms HandlerThread, separate from the main UI thread, ECARX reader and PCM producer. Start/stop/source transitions are serialized; model controls are synchronized or volatile, immutable frames are published through a volatile reference, and GPS state is synchronized across callbacks and the model worker. A dedicated Android regression blocks the UI thread for 600 ms and checks that both model ticks and PCM delivery continue. This validates thread separation, not physical pedal-to-speaker latency.

## Outer Voice references

Read local `C:/Users/HK/Documents/ChatGPT/7X/OuterVoice`: `MainActivity.java` proportional 1024×600 scaling, `qa/v141-reopened-screen-edge.png` and home captures for the native two-column visual language, `AudioRoutes.java` for exact `BUS12_OUTER_NOTIFY`, `WavPlayer.java` for AudioTrack route validation. Earlier 44.1 kHz recording/playback supports offering that format. It does not prove 48 kHz was the cause of silence on this car.

Automatic cabin output now leaves the preferred device unset so normal OEM media policy selects it, rather than forcing the generic builtin speaker. Explicit saved device mappings still resolve by type/address/name. An exterior route must be explicit BUS12 or the user's manual choice. Both may be refused if the actual tracks converge onto one device. No global audio policy, OEM audio effect, volume group or privileged routing API is modified.

Stereo PCM duplicates the mono engine source into both channels. Format is 44.1 kHz by default, with 48 kHz selectable while stopped. Speaker tests emit two low-level tones without running the virtual engine or GPS. System media gain/mute, app gain, actual routed device, submitted PCM and playback-head progress are shown separately. None of these measurements alone proves physical amplifier output.

## Verification boundary

Pure tests cover observed 0/61/100 throttle, 14.84375 brake, sentinel/NaN rejection, public-interface reflection, input expiry, measured m/s, stationary revs, release BOV and brake precedence. Android tests use clearly labelled **TEST DATA / 非實車** snapshots, exercise both AudioTrack formats and retain the baseline overlay/background/Exit checks. Images of those fixtures must not be described as car capture evidence. Physical SDK access from `hk.local.enginex7`, OEM overlay insets, internal/external/Both audibility and amplifier latency remain target-car retests.
