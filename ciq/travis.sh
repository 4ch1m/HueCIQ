#!/bin/bash

# Helper-script for 'Travis CI'-integration
# (simply download the CIQ-SDK and compile/package the sources)

SDK_URL="https://developer.garmin.com/downloads/connect-iq/sdks/connectiq-sdk-win-2.2.2.zip"

###

# change to the ciq-subfolder
cd ciq

# d/l and unzip the SDK
wget -O sdk.zip "${SDK_URL}"
unzip sdk.zip -d "sdk"

# generate a dummy-private-key for compilation
PEM_FILE="/tmp/developer_key.pem"
DER_FILE="/tmp/developer_key.der"
openssl genrsa -out "${PEM_FILE}" 4096
openssl pkcs8 -topk8 -inform PEM -outform DER -in "${PEM_FILE}" -out "${DER_FILE}" -nocrypt

# export the env-vars
export MB_HOME="sdk"
export MB_PRIVATE_KEY="/tmp/developer_key.der"

# run the actual build-helper-script
./mb_runner.sh package
