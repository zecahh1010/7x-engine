# Native selector verification, 1.1.1

2026-11-19. Narrow implementation and native visual verification by the implementing agent. This is not an independent full-product review.

Final APK SHA-256: `646D15F5FF3266F5DEC376109CC425AEA3AC84F511906AFDB58120D374616D53`.

Reviewed `v1.1.1-1024/settings-output-dropdown.png`, `seven-engine-selector.png`, `v1.1.1-phone/settings-output-dropdown.png` and `compact-settings-en-font130.png`. Actual popup text is visible on an explicit dark surface. Seven-preset count and Previous/Next access are present. 44 Android selector assertions passed at each genuine AVD size, including a light-parent-theme fixture and foreground/background checks for every adapter row. No generated image or web preview was used.

No further visual changes were needed after this inspection. Retain the existing proportional layout and scroll behavior. Real OEM popup behavior and physical speaker output remain unverified.
