## Notes

Thanks to [Orangepixel](https://orangepixel.net/) for creating **Sir Questionnaire**. Explore a dungeon through pairs of choices, collect equipment and learn how to defeat its creatures. PortMaster adaptation by **Pixelforge Ports (Ronax)**.

## Get SirQuestionnaire.jar from GOG

Windows:

1. Open [Sir Questionnaire on GOG](https://www.gog.com/en/game/sir_questionnaire) in your owned library and download the **full Windows offline backup installer** for **1.8.5b**.
2. Run the installer on Windows and open the installed game directory. Find `SirQuestionnaire.jar` beside the game executable. Enable file extensions in Explorer so its name is visible.

Linux:

Alternatively, extract your full offline installer using [innoextract](https://constexpr.org/innoextract/).
Run `innoextract -d extracted "your-full-offline-installer.exe"`, then locate `SirQuestionnaire.jar`
inside the extracted files (usually `extracted/app/`).

Supported archive SHA-256 (`SirQuestionnaire.jar`):

```text
f922d60b5dbaa977321f72868cdd7d38c7f4db6e1fe638c693c2cc1ae0804597
```

Copy the owned file to **`<ports directory>/sirquestionnaire/gamedata/SirQuestionnaire.jar`**.
Launch **Sir Questionnaire** from your firmware's ports menu.

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
