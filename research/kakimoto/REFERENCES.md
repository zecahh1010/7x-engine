# Subaru GT1.0Z reference analysis

2026-11-18, Hong Kong. Local analysis only; no user audio uploaded to external services.

The user's final direction is to keep the exhaust rhythm from the first WRX recording. The GR86 reference was explicitly withdrawn. Two later Subaru GDB recordings are supporting tonal comparisons, not replacement timing sources.

| Recording | Duration | SHA-256 | Use |
|---|---:|---|---|
| 02 Subaru WRX Kakimoto GT1.0Z Exhaust - (original Kbps).mp3 | 63.762 s | `3D297F42D250CC32CA4A58E2947AF6223DD72596A5182FCB5E31ECCE5324FD48` | Sole waveform and exhaust timing source |
| GDB マフラーサウンド 柿本 GT1.0Z Racing(競技用) - GRANTURISMOIMPREZA.mp3 | 100.200 s | `CAAB76C8E351BB380BE2587A2E197FABF81012632260328330D0A397F1650069` | Supporting spectrum/low-frequency-body comparison |
| SUBARU インプレッサ WRX STI GDB-E 柿本改 GT1.0Z Racing 排気音 - Pihya ppr.mp3 | 54.144 s | `826B1FAE54DAE354B3E47C2200C6F16DFD4A4A00E9EE51D2AF00E5E5817C9094` | Supporting spectrum/rev comparison |

The supplied names identify the claimed vehicles/exhausts; no independent authentication of their recording provenance is claimed. Durations, spectra and waveform measurements are verified locally. This workflow analyzes the files numerically and visually; it is not a human listening verdict.

## Findings and implementation

The first WRX has particularly repeatable windows around 11.65 s, 15.35 s, 44.50 s and 32.00 s. Local period-correlation estimates are 0.939–0.981. Their measured periodicities are approximately 12.28, 14.59, 21.39 and 50.86 Hz. At 15 s the strong measured spectral peaks include about 116/131 Hz; at 44 s about 86/108/152 Hz. These are recorded acoustic components, not ECU RPM measurements.

The GDB recording has dominant peaks near 103/52/155 Hz at 13 s, 102/51/152 Hz at 20 s, and 148/99/49 Hz at 30 s. The GDB-E recording has strong peaks near 121/90/60 Hz at 8 s and 123/99/74 Hz at 18 s, followed by moving harmonic bands during revs. These support the choice of a strong low-frequency body from the first recording; they do not justify replacing its cadence. Absolute recording amplitudes are not used to choose app gain: the GDB file has much higher decoded float levels and a different noise floor.

Only the first WRX file enters `build-wrx-tables.cjs`. It extracts four periodic waves by Hann-weighted Fourier projection, retaining measured harmonic phase relationships and within-cycle exhaust shape. Circular alignment permits smooth RPM interpolation. Each wave is normalized to the same RMS before PCM quantization; high orders taper and source-Nyquist frequencies are excluded. The artificial pitched drum layer from 1.0.1 is removed for this profile.

The runtime continuously advances these cycles with virtual RPM, morphs between adjacent tables, applies the load envelope and smooth ignition-cut gain, then mixes the existing turbo/BOV layer and output limiter. There is no playback of a fixed recording timeline. BOV remains an additional synthesized effect with the 1.0.1 amplitude/hold/decay settings; it is not presented as audio extracted from these exhaust recordings.

RPM anchors are a model assumption (`period Hz × 60`), because the references contain no synchronized ECU RPM trace. Above the highest captured anchor the top cycle is pitched up; exact high-RPM/exhaust likeness is unverified. The wavetable average removes nonperiodic wind and some fine acoustic variation. Subjective likeness and actual vehicle speaker output require listening.

## Reproduction

Decode the first original MP3, using FFmpeg, to mono 12 kHz float PCM:

```powershell
ffmpeg -nostdin -i '<original WRX MP3 path>' -map 0:a:0 -ac 1 -ar 12000 -f f32le -y reference.f32
node build-wrx-tables.cjs
```

Run these commands in this directory. The generator checks the decoded input SHA-256 before extraction, records all anchors and the generated Java hash in `wrx-extraction.json`, and writes `WrxCycles.java`. The generated Java tables are sufficient to build the app. Original MP3s/decoded float caches are retained only in the local research folder and excluded from the distributable source ZIP; regeneration needs the original WRX file. `analyze.cjs` supplies 1.365 s FFT windows and 1 s normalized autocorrelation diagnostics. Supporting spectra are read as broad tonal comparisons, not sample-accurate matching scores.
