# 7X Engine 1.1.1

2026-11-19, Hong Kong. `hk.local.enginex7`, version code 111. Signed with the existing certificate for overwrite installation.

- Settings and engine selectors use explicitly styled TextViews and a dark native dropdown popup, with a drawn chevron. Selected, popup and disabled text colors do not depend on an OEM spinner/dialog layout or light parent theme.
- All seven sound presets remain available: Subaru WRX, American V8, Italian V8, inline-six turbo, V10, V12 GT and classic Boxer turbo. Previous/Next sound controls provide direct access and wrap through all seven. Their saved keys are retained.
- Subaru uses the same WRX-derived cycle tables at 78% of the previous cadence, with a darker filter and stronger low-frequency body. Other six presets preserve identical PCM at fixed DSP inputs; driving sample WAVs can differ because the shared gearbox changed.
- Subaru BOV retains its initial pressure-release effect but has a short tail. The software difference-signal tail above 0.001 RMS is about 660ms, previously 2670ms. This is not an acoustic duration measured in the car.
- Raised upshift points, a persistent throttle-lift lock until pedal reapplication, 200ms stable demand, 1.2s shift spacing and hysteresis prevent early/coasting upshifts and gear hunting. A rolling start selects an appropriate initial virtual gear.
- Downshifts use a 520ms smooth rev match with a pronounced breath. A high-RPM Subaru downshift schedules a short backfire after the blip; low-RPM downshifts have no backfire. Braking and coasting cannot initiate upshifts.

All RPM, gears and effects are virtual sound modeling. No vehicle writes or permissions were added. Desktop/native emulator checks do not establish physical-car SDK access, OEM popup behavior, actual speaker audibility or subjective sound approval. See VALIDATION.md for exact final artifact evidence.

Listen to `dist/7X-Engine-1.1.1-Subaru.wav`: 40 seconds of ignition, acceleration, lift, braking/downshifts and idle, rendered by the shipping model/DSP with synthetic input. GR86 remains excluded.
