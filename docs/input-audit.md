# Input inspection — Sir Questionnaire 1.8.5b

Inventoried all **154 files** in the supplied Windows/GOG installation, recording path, byte count, format signature and SHA256. Read and hashed all **3,340 entries** of `SirQuestionnaire.jar` and all **9 entries** of `webcache.zip`, with ZIP CRC validation. The Windows executable and installer were not run. Original files remain unchanged.

The JAR manifest uses `com.orangepixel.questionnaire.desktop.Main`; the replacement host extends `com.orangepixel.questionnaire.myCanvas`. The original launcher uses a 1280x720 desktop window, vsync and a 60 FPS ceiling. Selected launcher, engine, rendering, input, preferences and save classes were inspected privately. Inventorying every file does not mean every binary instruction was reverse engineered.

Supported data: `SirQuestionnaire.jar`, 71,482,334 bytes, SHA256 `f922d60b5dbaa977321f72868cdd7d38c7f4db6e1fe638c693c2cc1ae0804597`. No data conversion or extraction is required on the handheld.

## ARM64 dependencies

The archive includes AArch64 (ELF machine 183) libGDX/FreeType and LWJGL natives. GLFW and OpenAL need GLIBC 2.27. OpenAL additionally references GLIBCXX_3.4.22 and CXXABI_1.3.9. Jamepad needs GLIBC 2.29, but its native controller polling is disabled in favor of PortMaster keyboard mapping. Storefront and Twitch adapters are not constructed. The original bundled Windows runtime, EXEs, uninstaller, store metadata and web cache are not required in the Linux package.

## Adaptation

The Java host uses the unchanged owner JAR, an offline Social adapter and an overridden desktop preferences directory. Legacy Gdx preferences are isolated from newer per-profile files. Original game-state, turn and checkpoint rules are unchanged. The graphics bridge preserves the desktop 16:9 view at all requested physical sizes, maps only the default framebuffer viewport and is restored on each frame. It applies a 60 Hz ceiling without catch-up updates.

Keyboard defaults: arrows for choices/navigation; X use; Z drop; Escape back; Tab inventory; F1 codex. Left and right are gameplay decisions, not continuous character movement. Test code must distinguish actual room progression from inventory/outfit overlays that also use gameplay state 6.

Original `PlayerProfile.saveGame()`, `loadGame(false)` and `hasSaveGame()` provide checkpoint serialization and read-back checks. Full resume uses the original menu flow. Test-only calls do not change shipping checkpoint behavior.

Machine-readable input/member inventories and private inspection output remain under `build` and are excluded from source and BYO distributions. `tools/inspect_input.py` reproduces the inventories from an owned copy.
