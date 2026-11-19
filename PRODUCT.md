# 7X Engine

Native Android app for a Zeekr 7X head unit. User-authorized implementation, 2026-11-18. Canonical requirements: 7X-ENGINE-REQUIREMENTS.md; reference study: research/teslun-rev/STUDY-2026-11-18.md.

2026-11-19 correction: keep all seven sound presets easy to select; protect dropdown text from OEM theme colors. Retune Subaru toward a lower, slower pulse and short BOV tail, stronger smooth downshift rev-match breath, and high-RPM-only post-blip backfire. Raise shift RPM, prevent upshifts while lifting/coasting, and remove gear hunting. All drivetrain changes remain virtual and read-only.

Later 2026-11-19 steering supersedes the fixed-tail requirement: the boxer burble must be prominent and clearly separated. Subaru BOV starts from **4000 RPM before pedal lift**, with progressively longer tails at higher RPM. Preserve the remembered event RPM after the tachometer falls, suppress stale-input events, and keep the existing rev-match/backfire and gearbox decisions.

Build a working offline APK: engine startup/idle/virtual automatic gearbox and seven locally synthesized engine characters including a pronounced unequal-header Subaru Boxer with boost-release BOV. Default ECARX vehicle mode, optional GPS estimated-load mode and labelled simulation mode. Do not actuate the car. User physical screenshots show throttle 0, 61 and 100 at ECARX 0x00101400, plus brake depth 14.84375 at 0x00101300 and brake switch 0x20317A00. Implement read-only live ECARX input by the existing Explorer reader. Treat throttle as the observed 0–100 range; keep brake depth raw and prefer the fresh brake switch. Missing/stale input must never become a false zero-percent reading or latch acceleration. Actual Engine app read access still needs car validation.

Near-fullscreen overlay above ordinary apps, top-right minimized control, explicit Exit, background foreground-service playback/location. Internal, external and Both outputs, independent +/- dB gains in Settings. Missing external output and dual-routing failures must be visible, not silently rerouted.

Traditional Chinese default and English option; dark low-glare, large touch controls, simple hierarchy. User now explicitly requests Outer Voice native UI and proportional screen sizing, retaining Teslun mechanics. Physical screenshot rejection supersedes old emulator-only UI approval. No visual options exploration or mockup approval gate requested. Native Java Views/Canvas, no webview or runtime network dependency. Emulator evidence must remain separate from actual car routing, audibility and overlay OEM behavior.
