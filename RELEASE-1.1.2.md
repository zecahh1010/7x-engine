# 7X Engine 1.1.2

2026-11-19, Hong Kong. `hk.local.enginex7`, version code 112. Existing signing certificate and overwrite installation retained.

Subaru now has an explicit, prominent low-RPM boxer burble: a strong rounded pulse, smaller uneven companion and clear gap between groups, shaping the existing WRX-derived waveform. The 0.78 playback cadence and low-frequency body remain. The added shaping fades out by 3600 RPM rather than imposing an idle pulse on the high-RPM exhaust.

The user's final BOV threshold is **4000 RPM**, superseding 3500 and the fixed short tail. A fresh pedal lift at or above 4000 triggers Subaru release, including light-pedal release at road speed. The event captures pre-lift RPM so a rapid return to idle does not remove or shorten it. Higher release RPM gives a longer decay; stale input creates no release. Existing cooldown prevents duplicate/repeated releases. Turbo upshift releases obey the same 4000-RPM threshold.

Software difference-signal tails above 0.001 RMS:

| Pre-lift RPM | Tail |
|---|---|
| 4000 | 470ms |
| 5000 | 730ms |
| 6000 | 1010ms |
| 7000 | 1270ms |

These are digital estimates, not physical speaker measurements. Backfire/rev-match sound parameters and the raised/hysteretic gearbox decisions are retained. Six non-Subaru presets remain byte-identical at fixed inputs. UI/ECARX/audio routing and permissions are unchanged apart from version text.

Preview files are generated locally by the shipping DSP with synthetic inputs:

- `dist/7X-Engine-1.1.2-Burble.wav`: 12-second low-RPM burble, immediately audible without ignition delay.
- `dist/7X-Engine-1.1.2-BOV-RPM.wav`: isolated releases at 4000/5000/6000/7000 RPM, starting at 0/4/8/12 seconds.
- `dist/7X-Engine-1.1.2-Subaru.wav`: 40-second ignition/driving/braking simulation.

Native emulator PCM/playback progress is verified separately from physical-car speaker audibility and subjective sound approval.
