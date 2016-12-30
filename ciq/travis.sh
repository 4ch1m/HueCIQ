#!/bin/bash

# Helper-script for 'Travis CI'-integration
# (simply download the CIQ-SDK and compile the sources)

SDK_URL="https://developer.garmin.com/downloads/connect-iq/sdks/connectiq-sdk-win-2.2.2.zip"

cd ciq

wget -O sdk.zip "${SDK_URL}"
unzip sdk.zip -d "sdk"

export MB_HOME="sdk"
export MB_PRIVATE_KEY="n/a"

./mb_runner.sh build
