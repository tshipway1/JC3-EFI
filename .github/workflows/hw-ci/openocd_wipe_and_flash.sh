#!/usr/bin/env bash

HW_SCRIPT=$1

set -e

# for instance
#
# .github/workflows/hw-ci/openocd_wipe_and_flash.sh ../.github/workflows/openocd_ci_f4_discovery.cfg
# .github/workflows/hw-ci/openocd_wipe_and_flash.sh ../.github/workflows/openocd_ci_proteus_f4.cfg
# .github/workflows/hw-ci/openocd_wipe_and_flash.sh ../.github/workflows/openocd_ci_proteus_f7.cfg

cd firmware

# works on F4 with ST-LINK/V3 does not seem to work using V2 :(
openocd -f "$HW_SCRIPT" -c init -c targets -c "reset halt" -c "flash erase_sector 0 0 last" -c "flash write_image "deliver/rusefi.bin" 0x08000000" -c "reset run" -c "shutdown"
