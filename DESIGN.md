---
name: 7X Engine
description: A proportional native Android engine instrument following Outer Voice.
colors:
  background: "#0e181c"
  surface: "#19262c"
  text: "#edf3f3"
  muted: "#aebfc6"
  track: "#34464e"
  redline-error: "#ffaaa4"
  warning-amber: "#edc282"
  accent-cyan: "#00bcc4"
  accent-magenta: "#ffa3ce"
  accent-lime: "#c4dc8c"
  accent-violet: "#c5b8f3"
  accent-ice: "#dce7ef"
  native-theme-accent: "#88d8cd"
  native-window-background: "#10171b"
  native-secondary-text: "#b0c4ca"
  ripple: "#6fe0e433"
typography:
  title:
    fontFamily: "sans"
    fontSize: "26u × fontScale"
    fontWeight: 700
  heading:
    fontFamily: "sans"
    fontSize: "20u × fontScale"
    fontWeight: 700
  ignition:
    fontFamily: "sans"
    fontSize: "25u × fontScale"
    fontWeight: 700
  gain:
    fontFamily: "sans"
    fontSize: "25u × fontScale"
  throttle:
    fontFamily: "sans"
    fontSize: "22u × fontScale"
  spinner:
    fontFamily: "sans"
    fontSize: "20u × fontScale"
  control:
    fontFamily: "sans"
    fontSize: "18u × fontScale"
  slider-label:
    fontFamily: "sans"
    fontSize: "17u × fontScale"
  status:
    fontFamily: "sans"
    fontSize: "16u × fontScale"
  body:
    fontFamily: "sans"
    fontSize: "15u × fontScale"
  detail:
    fontFamily: "sans"
    fontSize: "14u × fontScale"
  display-gear:
    fontFamily: "sans-serif-medium"
    fontSize: "54gu × fontScale"
  display-rpm:
    fontFamily: "sans-serif-medium"
    fontSize: "30gu × fontScale"
  display-speed:
    fontFamily: "sans-serif-medium"
    fontSize: "27gu × fontScale"
  gauge-tick:
    fontFamily: "sans-serif-medium"
    fontSize: "16gu × fontScale"
  gauge-detail:
    fontFamily: "sans-serif"
    fontSize: "13gu × fontScale"
  gauge-unit:
    fontFamily: "sans-serif"
    fontSize: "12gu × fontScale"
rounded:
  control: "10u"
spacing:
  label-vertical: "4u"
  minimized-horizontal: "6u"
  control-gap: "8u"
  panel-vertical: "10u"
  section-gap: "12u"
  dropdown-horizontal: "16u"
  panel-horizontal: "20u"
  home-column-gap: "24u"
  settings-column-gap: "28u"
components:
  button-default:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.text}"
    typography: "{typography.control}"
    rounded: "{rounded.control}"
    padding: "8u 10u"
  button-selected:
    backgroundColor: "{colors.accent-cyan}"
    textColor: "{colors.background}"
    typography: "{typography.control}"
    rounded: "{rounded.control}"
    padding: "8u 10u"
  ignition:
    typography: "{typography.ignition}"
    rounded: "{rounded.control}"
  panel:
    backgroundColor: "{colors.background}"
    padding: "10u 20u"
  minimized-panel:
    backgroundColor: "{colors.surface}"
    padding: "4u 6u"
    width: "272u"
    height: "68u"
---

# Design System: 7X Engine

## Overview

**Creative North Star: "One working instrument"**

The user-pinned Outer Voice reference governs this dark native instrument: a charcoal field, cyan operational accent, white system sans and generous direct controls. One large tachometer and one obvious ignition action share the usable car window with immediate pedal feedback. Traditional Chinese is the default; English is a persistent option.

Java Android Views and authored Canvas drawing provide the interface under `android:style/Theme.Material.NoActionBar`. Measured-window proportions replace the rejected fixed-height composition. Native screenshot fixtures provide review evidence; there is no HTML detector, generated image or shipping raster.

**Key Characteristics:**

- Full-height two-pane operation on wide windows, stacked scrolling on compact windows.
- Proportional controls and separate system font-scale support.
- Explicit actual, estimated, simulated, stale and unavailable input states.
- Independent cabin/exterior gain and nearby speaker-test feedback.
- Shared Activity and overlay panel with distinct Minimize and Exit behavior.

Source authority: `PanelView.java`, `GaugeView.java`, `EngineService.java`, `AudioOutput.java`, `VehicleSnapshot.java`, `Config.java` and `res/values/styles.xml`. This refresh supersedes the former fixed-layout documentation and its emulator-only approval. The 1.1.0 bounded finish verdict is historical; see `.impeccable/review/FINISH-1.1.0.md`. Version 1.1.1 received narrow selector/native verification in `SELECTORS-1.1.1.md`, not a new independent full-product review. Review directories `.impeccable/review/physical-1024`, `physical-2560` and `physical-phone` contain native **emulator fixtures**, despite their names. They are not physical-car evidence. Real head-unit SDK access, speaker audibility/routing, OEM overlay behavior and system insets remain unverified.

## Colors

### Primary

**Operational Cyan** (`accent-cyan`) is the default selected-mode, available ignition and gauge accent. Amber, Magenta, Lime, Violet and Ice are saved alternatives. Amber shares `warning-amber`; these choices are operating accents rather than complete Android theme replacements.

### Secondary

**Warning Amber** identifies simulation and unavailable/stale vehicle throttle. **Soft Red** (`redline-error`) marks redline and recoverable audio errors. Color is always accompanied by written state.

### Neutral

**Charcoal Field** (`background`) covers the authored panel; **Raised Charcoal** (`surface`) fills controls and the minimized bar. **Frost Text** carries primary values, **Muted Blue Grey** supporting copy, and **Instrument Track** unfilled arcs, progress and control borders.

The `native-theme-accent`, `native-window-background` and `native-secondary-text` tokens record the distinct values still present in `styles.xml`. Platform widgets may inherit these; saved accent changes do not retheme every native control. `ripple` is the authored translucent touch feedback.

**The State Color Rule.** Preserve written warning and error states independently of the chosen operational accent.

## Typography

System `sans` supplies TextViews and Chinese fallback. Canvas uses `sans-serif-medium` for gear, RPM, speed and ticks, and `sans-serif` for detail. No font asset, custom line-height, letter spacing or tabular-numeral feature is authored.

Frontmatter sizes are reference units, not fixed dp/sp. In the panel, `u = max(displayDensity, min(measuredWidthPx / 1024, measuredHeightPx / 600))` pixels per unit. Text size is the role size multiplied by `u` and `fontScale = scaledDensity / density`, passed to TextView in pixels. Gauge text uses its own `gu = max(density, min(gaugeWidthPx / 430, gaugeHeightPx / 445))`, with the same font-scale multiplier.

The hierarchy moves from gear/RPM/speed to title, ignition and gain, throttle, spinner/heading, ordinary controls, status and supporting detail. Navigation and mode buttons use the status size; brake uses the control size. Multiline spinner labels remain enabled.

**The Scaling Rule.** Preserve both measured-window scaling and the independent font-scale multiplier. Review compact scrolling and large text in native captures; proportional Canvas coordinates do not guarantee text reflow.

## Layout

The panel recalculates from its measured window on size changes, with display dimensions as an initial fallback. It uses the frontmatter padding. Wide means `measuredWidthPx / u >= 800`, not Android configuration width. In wide home, the toolbar remains above a remaining-height horizontal body: instrument/control weights are 0.46/0.54, with the home-column gap. The gauge consumes remaining instrument height; the right control pane scrolls when needed. Compact home places the gauge (310u high) above controls in one scrolling body. This compact gauge height is not a fixed car-wide composition.

Wide navigation shares the title row; actions are 90u wide with control gaps. Compact navigation moves to its own equal-weight row. Settings always scroll, with equal columns and the settings-column gap when wide, stacking when compact. Equal-weight row children have an 8u leading margin after the first.

Ordinary buttons have a 52u minimum height, ignition 80u, spinner/dropdown rows 56u, and sliders 48u. These minima scale with `u >= density`. Retain at least 48dp touch targets and 8dp neighboring target gaps in review; text wrapping may increase heights.

The full application overlay uses MATCH_PARENT width/height at zero offsets. It remains non-focusable and not touch-modal, with Android overlay permission required. The minimized overlay stays top-right at 12u/28u, sized by the display-based version of the same proportional formula. It is 272u × 68u, with Restore and a 72u-wide Exit, each 56u high. Exit has an explicit 8u leading margin, at least 8dp. Measured usable bounds and OEM status/navigation insets require real-head-unit validation.

**The Window Rule.** Use measured panel bounds for wide/compact composition; do not restore the rejected configuration-width breakpoint or fixed wide gauge height.

## Elevation & Depth

Authored depth comes from tonal fields, outlined rounded controls and brighter instrument marks. Custom panel buttons explicitly use zero elevation and a native RippleDrawable. Other native widgets and minimized buttons retain platform feedback and may carry theme elevation; there is no project shadow scale. No decorative imagery or depth effects compete with values.

## Shapes

Authored panel controls use the frontmatter gentle corner radius and a track-colored stroke of `max(1px, 1u)`. The panel and minimized background remain rectangular fields; spinners and sliders retain Android geometry.

The Canvas signature is a 270-degree arc starting at 135 degrees, centered at half width/44% height. Radius is `min(width × 0.41, height × 0.35)`; track stroke is 6.5% of radius. Ticks occur every 500 RPM with labels every 1,000. Needle and tick strokes remain raw Canvas pixels (3px needle, 2px major/1px minor ticks), a source limitation rather than a proportional stroke token.

## Components

### Navigation and ignition

Version 1.1.3 adds Settings → Credits / 設定 → 製作人員. It reuses the panel toolbar, palette, native scroll container and measured-window scale. The page centers the Developer/開發者 label at 18u, **Zeca** at bold 48u in the saved accent, and app name at 20u. Back returns to Settings, then Home; size changes preserve the Credits page. It does not change playback state.

Version 1.1.1 refines selectors without changing the proportional layout or palette. All collapsed and popup rows are authored native TextViews with explicit Frost Text on Raised Charcoal, independent of OEM layouts or light parent themes. Dropdown mode is explicit, with a drawn chevron and explicit popup surface. Home also offers Previous/Next sound controls and a seven-preset count. The 1.1.0 independent review remains historical; 1.1.1 received its own narrow selector/native visual verification, recorded in VALIDATION.md and `.impeccable/review/SELECTORS-1.1.1.md`; this is not a new independent full-product review.

Settings/Back, Float, Minimize and Exit are direct native actions. Ignition presents Start engine, Preparing audio, Igniting, Stop engine or Testing. It is disabled during preparation/ignition; selected fill marks available Start. Mode, output, format and device mappings lock while running, preparing or testing. Sound selection remains available. Home Back backgrounds the Activity and requests the minimized overlay when permitted; Settings Back restores home.

### Instrument and ECARX input

The tachometer displays virtual engine RPM/gear and independently labelled vehicle, simulated or GPS speed. Vehicle throttle uses the observed 0–100 range. Fresh values show percent; stale/missing values show an em dash and warning description. ECARX speed also shows an em dash when stale.

Brake depth remains visibly labelled **raw depth** without a percentage claim. The fresh brake switch takes priority in braking detection; otherwise fresh raw brake depth above 0.2 is the implemented fallback. Signal freshness is independent per channel with a 1,500ms window. Settings show identifiers, raw readings and age/stale/unavailable text. A zeroed progress fill beside an em dash represents unavailable data, not a measured zero reading. Do not infer brake-depth calibration from bar length.

GPS explicitly labels estimated load. Simulation explicitly labels synthetic pedal/speed and offers pedal, hold-brake, release and automatic demo. Manual input cancels demo; brake clears pedal; panel detach clears brake.

The gauge smooths displayed RPM by 0.28 per draw and requests visible redraw every 33ms. Panel state refreshes every 100ms. Gauge accessibility description refreshes at roughly one-second intervals. These are requested timings, not measured performance guarantees.

### Speaker tests and output settings

Cabin and exterior test actions have nearby polite live-region status and a Stop test action while busy. Feedback distinguishes choose-test, engine-must-stop, preparing, playing with elapsed/2.2s, finished, stopped and error. Busy test actions disable and dim. Finished copy asks the user to confirm both tones were heard; completion is not proof of physical audibility.

Test status and route summary use `AudioOutput.activeOutput`, the attempted test target, rather than the saved engine output. Settings keep actual cabin/exterior device descriptions, PCM/played frames and active sample rate visible. Output selection remains Internal, External or Both; missing mappings and routing failures remain explicit. Automatic exterior mapping names BUS12; a saved disconnected device remains a visible offline option.

Independent internal/external gains have signed dB, ±1dB steps and Reset 0dB; persisted range clamps to −36…+18dB. App volume and system media volume are separate sliders. Sample rate (44.1/48kHz stereo) and cabin/exterior mappings are independent settings. Gain is relative app gain, not measured sound pressure. Zero/muted system or app volume has visible feedback. GPS intensity and delay remain separate controls.

### Persistence and lifecycle

Defaults are Subaru, ECARX vehicle mode, Internal output, automatic device mappings, 0dB channel gains, 60% app volume, 44.1kHz stereo, 50% intensity, 120ms compensation, Traditional Chinese and cyan. Preferences persist those choices, language and appearance; running state does not persist.

Minimize preserves service/audio. Restore returns to the full panel. The foreground notification offers Restore, Stop and Exit. Stopping resets the engine model to neutral values and stops location/audio. Explicit Exit stops model/audio/location/vehicle reader, removes overlay and Activity task, releases focus/wake lock and stops the foreground service.

## Do's and Don'ts

### Do:

- **Do** follow Outer Voice's native proportional control language and measured-window scaling.
- **Do** keep stale/unavailable ECARX values, raw brake depth and simulated/estimated input explicitly labelled.
- **Do** preserve nearby test feedback, Stop test and the actual attempted speaker target.
- **Do** keep independent dB, app/system volume, sample rate and device mappings.
- **Do** review native wide, compact, large-text and minimized captures with 48dp targets and at least 8dp gaps.

### Don't:

- **Don't** restore the rejected fixed-height wide layout or configuration-width breakpoint.
- **Don't** claim real-car SDK access, calibrated brake depth, audible output or OEM overlay/insets from emulator fixtures, or a final ship verdict before finish review.
- **Don't** substitute HTML previews, generated screenshots or shipping raster artwork for native evidence.
- **Don't** conceal routing errors, equate completed test tones with heard sound, or make Exit leave playback alive.
- **Don't** imply saved accent selection rethemes every platform widget or claim Compose/Material 3 conformance.
