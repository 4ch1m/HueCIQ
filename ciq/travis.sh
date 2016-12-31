#!/bin/bash

# Helper-script for 'Travis CI'-integration
#
# - d/l the CIQ-SDK
# - generate a dummy private-key for compilation
# - export the necessary MB-environment-variables
# - run the actual build-helper-script and package the app

###

SDK_URL="https://developer.garmin.com/downloads/connect-iq/sdks/connectiq-sdk-win-2.2.2.zip"

SDK_FILE="sdk.zip"
SDK_DIR="sdk"

PEM_FILE="/tmp/developer_key.pem"
DER_FILE="/tmp/developer_key.der"

###

cd ciq

wget -O "${SDK_FILE}" "${SDK_URL}"
unzip "${SDK_FILE}" "bin/*" -d "${SDK_DIR}"

openssl genrsa -out "${PEM_FILE}" 4096
openssl pkcs8 -topk8 -inform PEM -outform DER -in "${PEM_FILE}" -out "${DER_FILE}" -nocrypt

export MB_HOME="${SDK_DIR}"
export MB_PRIVATE_KEY="${DER_FILE}"

./mb_runner.sh package
