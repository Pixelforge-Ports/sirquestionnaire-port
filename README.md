## Notes

Thanks to [Orangepixel](https://orangepixel.net/) for creating **Sir Questionnaire**. Explore a dungeon through pairs of choices, collect equipment and learn how to defeat its creatures. PortMaster adaptation by **Pixelforge Ports (Ronax)**.

## Get SirQuestionnaire.jar from GOG

1. Open [Sir Questionnaire on GOG](https://www.gog.com/en/game/sir_questionnaire) in your owned library and download the **full Windows offline backup installer** for the supported build (1.8.5b). Download every accompanying `.bin` part, if listed, and keep them beside the `.exe`. Use the full installer, not a patch or the Galaxy installer.
2. Run the installer on Windows and open the installed game directory. Find `SirQuestionnaire.jar` beside the game executable. Enable file extensions in Explorer so its name is visible.
3. Copy that file unchanged into the installed port at `<ports directory>/sirquestionnaire/gamedata/SirQuestionnaire.jar`. Keep its exact name, capitalization and spaces. The Windows EXE and bundled Windows Java runtime are not needed.

Alternatively, extract your full offline installer using [innoextract](https://constexpr.org/innoextract/).
Run `innoextract -d extracted "your-full-offline-installer.exe"`, then locate `SirQuestionnaire.jar`
inside the extracted files (usually `extracted/app/`) and copy it to the same destination.
Installer layouts vary; use Windows installation if your extractor cannot read that installer.
Do not unpack or rename the game archive itself.

No game-data conversion is required on a PC or handheld. The handheld verifies the supplied
archive and creates its save folders. It cannot generate the purchased game data from nothing.

Supported archive SHA-256 (`SirQuestionnaire.jar`):

```text
f922d60b5dbaa977321f72868cdd7d38c7f4db6e1fe638c693c2cc1ae0804597
```

Compare it with `Get-FileHash -Algorithm SHA256 "SirQuestionnaire.jar"` in PowerShell,
or `sha256sum "SirQuestionnaire.jar"` on Linux. A different build needs a compatibility check.

## Installation

1. Update PortMaster. Put **sirquestionnaire.zip** in PortMaster's `autoinstall/` directory, then open PortMaster to install it. Connect to the network to download Java 17 and Westonpack if they are not installed yet.
2. Copy the owned file to **`<ports directory>/sirquestionnaire/gamedata/SirQuestionnaire.jar`**.
3. Launch **Sir Questionnaire** from your firmware's ports menu.

The launcher detects display size and uses the native viewport: **4:3** at **640x480** and **1024x768**, **3:2** at **720x480**, **1:1** at **720x720**, and **16:9** at **1280x720**. Other valid PortMaster dimensions follow the same screen-matching layout. Combat messages, Floor/Level/XP labels and item descriptions use full-width lettering with capital heights of **17 pixels at 480p**, **19 pixels at 720x720**, and **21 pixels at 1024x768 and 1280x720**. Combat messages stay in a fixed header beside the top icon, showing at most three complete lines per page so long messages do not expand over the floor or character. Floor and Level/XP share a row below the equipment icons during gameplay. Item details wrap in a separate panel below the inventory grid. Long combat messages and item descriptions advance between pages of complete lines every 3.5 seconds; dots indicate the current page. Letters are never clipped by a scrolling window. Other interface text uses the existing handheld sizing; large headings keep their original size. Square screens use a separate inventory view. If detection is wrong, put the actual size, such as `720x480`, in `sirquestionnaire/resolution.txt`; use `auto` or remove the file to restore automatic detection.

Back up **`sirquestionnaire/saves/`** before updating. If startup fails, check **`sirquestionnaire/log.txt`**. When reporting a problem, include the device, firmware version, resolution, reproduction steps, and log. Keep purchased game files private.

## Controls

| Control | Keyboard input / action |
|---|---|
| D-pad up | UP / Up / navigate |
| D-pad down | DOWN / Down / navigate |
| D-pad left | LEFT / Left / navigate |
| D-pad right | RIGHT / Right / navigate |
| A | X / Action / confirm |
| B | ESC / Back / pause |
| X | Z |
| Y | TAB |
| L1 | LEFT / Left / navigate |
| R1 | RIGHT / Right / navigate |
| L2 | F1 |
| R2 | X / Action / confirm |
| Start | O / Options |
| Select | ESC / Back / pause |
| Left stick | Same directions as the D-pad |
| Select + Start | Exit; save through the game first |

Use the game's default keyboard bindings. Start sends **O** for options.

## Build the PortMaster package

Requires Python 3.9+ and JDK 17 or newer. **No purchased JAR or DAT is required to compile
the host or build the ZIP.** From this source directory, on Windows:

```bat
python tools/build.py --jdk "C:\Program Files\Java\jdk-17"
```

Replace the quoted path with your installed JDK directory, for example `jdk-26.0.2.1`.
Use double quotes in Windows Command Prompt. On Linux:

```sh
python3 tools/build.py --jdk "/path/to/installed/jdk-17"
```

The first build downloads checksum-pinned public compile dependencies. Later builds may add
`--offline` to use the cache. Handwritten `compile-api/` declarations are compile-only;
only `org/portmaster/sirquestionnaire/` host classes go into `sirquestionnaire-host.jar`.


The only release artifact is **`dist/sirquestionnaire.zip`**, a universal BYO-data ZIP.
The build also prepares **`ports/sirquestionnaire/`** in the PortMaster source submission layout.
It never packages the owned game archive, Windows runtimes or personal saves.
Each full build compiles a fresh host under `build/artifacts/`; `package/` files remain unchanged.
The ZIP keeps `README.md` as supplied.
After editing package documentation or controls, rebuild with:

```sh
python tools/build.py --package-only
python tools/verify_package.py
```

`--package-only` requires a previously built host. The optional `--game-jar` argument checks
a supplied archive's fingerprint; it does not participate in compilation. Downloading a
public compile dependency does not supply the commercial game. Copy the owned files after installing.

Run `bash tests/verify_display.sh` for display-helper checks. Run `python tests/verify_launcher.py` for lifecycle checks. These tests use
mock runtimes and do not mount or run games. See `testing_thread.txt` for the Discord testing post. Upload source files using Git;
`build/`, `dist/`, generated `ports/` and owned data are excluded by `.gitignore`.

The Discord draft stays in source `testing_thread.txt`; it is not installed by the ZIP.

To check menus, quest text, combat messages, stats and inventory with your owned game JAR after building, run:

```sh
python tools/verify_resolutions.py --java "/path/to/java17/bin/java" --jdk "/path/to/installed/jdk" --game-jar "/path/to/SirQuestionnaire.jar" --interface
```

Use quoted Windows paths and `java.exe` on Windows. This runs desktop checks at the five
listed resolutions using separate test saves; handheld performance and controls still need device testing.
