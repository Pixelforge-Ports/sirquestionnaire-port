#!/bin/bash
# shellcheck source-path=SCRIPTDIR
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."
# shellcheck source=../package/sirquestionnaire/display.inc
source package/sirquestionnaire/display.inc
GAMEDIR="$(mktemp -d)"
trap 'rmdir "$GAMEDIR"' EXIT
for size in 640x480 720x480 720x720 1024x768 1280x720 960x544 320x240; do
    DISPLAY_WIDTH="${size%x*}" DISPLAY_HEIGHT="${size#*x}"
    SIRQUESTIONNAIRE_RESOLUTION=auto
    sirquestionnaire_display_setup
    [[ "${display_env[0]}" == "WESTON_HEADLESS_WIDTH=$DISPLAY_WIDTH" ]]
    [[ "${display_env[1]}" == "WESTON_HEADLESS_HEIGHT=$DISPLAY_HEIGHT" ]]
    [[ "${display_java[0]}" == "-Dsirquestionnaire.width=$DISPLAY_WIDTH" ]]
    [[ "${display_java[1]}" == "-Dsirquestionnaire.height=$DISPLAY_HEIGHT" ]]
done
DISPLAY_WIDTH=0 DISPLAY_HEIGHT=0
sirquestionnaire_display_setup
[[ "${#display_env[@]}" == 0 && "${#display_java[@]}" == 0 ]]
for bad in 0x480 640x0 100x100 9999x720 640x480oops '640x480;exit' 640X480; do
    SIRQUESTIONNAIRE_RESOLUTION="$bad"
    if sirquestionnaire_display_setup; then echo "Accepted invalid size: $bad"; exit 1; fi
done
SIRQUESTIONNAIRE_RESOLUTION=0720x0480
sirquestionnaire_display_setup
[[ "${display_java[0]}" == '-Dsirquestionnaire.width=720' ]]
[[ "${display_java[1]}" == '-Dsirquestionnaire.height=480' ]]
printf '720x720\r\n' > "$GAMEDIR/resolution.txt"
SIRQUESTIONNAIRE_RESOLUTION=""
sirquestionnaire_display_setup
[[ "${display_java[1]}" == '-Dsirquestionnaire.height=720' ]]
SIRQUESTIONNAIRE_RESOLUTION=1280x720
sirquestionnaire_display_setup
[[ "${display_java[0]}" == '-Dsirquestionnaire.width=1280' ]]
rm -- "$GAMEDIR/resolution.txt"
echo 'DISPLAY_CHECKS_OK: sizes, override precedence, CRLF, invalid inputs, automatic fallback'
