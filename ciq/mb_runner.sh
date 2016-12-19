#!/bin/bash

if [[ ! ${MB_HOME} ]]; then
    echo "MB_HOME not set!"
    exit 1
fi

if [[ ! ${MB_PRIVATE_KEY} ]]; then
    echo "MB_PRIVATE_KEY not set!"
    exit 1
fi

###

case "${1}" in
   build)
      ;;
   package)
      ;;
   *)
      echo "Usage: `basename ${0}` {build|package}"
      exit 1
      ;;
esac

###

PROJECT_HOME="`dirname $(readlink -f \"${0}\")`"

###

APP_NAME="HueCIQ"
MANIFEST_FILE="${PROJECT_HOME}/manifest.xml"
API_DB="${MB_HOME}/bin/api.db"
PROJECT_INFO="${MB_HOME}/bin/projectInfo.xml"
API_DEBUG="${MB_HOME}/bin/api.debug.xml"
DEVICES="${MB_HOME}/bin/devices.xml"

###

RESOURCES="`cd /; find \"${PROJECT_HOME}/resources\"* -iname '*.xml' | tr '\n' ':'`"
SOURCES="`cd /; find \"${PROJECT_HOME}/source\" -iname '*.mc' | tr '\n' ' '`"

###

# possible parameters ...

#PARAMS+="--apidb \"${API_DB}\" "
#PARAMS+="--buildapi "
#PARAMS+="--configs-dir <arg> "
#PARAMS+="--device <arg> "
#PARAMS+="--package-app "
#PARAMS+="--debug "
#PARAMS+="--excludes-map-file <arg> "
#PARAMS+="--import-dbg \"${API_DEBUG}\" "
#PARAMS+="--write-db "
#PARAMS+="--manifest ${MANIFEST_FILE} "
#PARAMS+="--api-version <arg> "
#PARAMS+="--output ${APP_NAME}.prg "
#PARAMS+="--project-info \"${PROJECT_INFO}\" "
#PARAMS+="--release "
#PARAMS+="--sdk-version <arg> "
#PARAMS+="--unit-test "
#PARAMS+="--devices \"${DEVICES}\" "
#PARAMS+="--version "
#PARAMS+="--warn "
#PARAMS+="--excludes <arg> "
#PARAMS+="--private-key ${MB_PRIVATE_KEY} "
#PARAMS+="--rez ${RESOURCES} "

function concat_params_for_build
{
    PARAMS+="--apidb \"${API_DB}\" "
    PARAMS+="--import-dbg \"${API_DEBUG}\" "
    PARAMS+="--manifest ${MANIFEST_FILE} "
    PARAMS+="--output ${APP_NAME}.prg "
    PARAMS+="--project-info \"${PROJECT_INFO}\" "
    PARAMS+="--unit-test "
    PARAMS+="--devices \"${DEVICES}\" "
    PARAMS+="--warn "
    PARAMS+="--private-key ${MB_PRIVATE_KEY} "
    PARAMS+="--rez ${RESOURCES} "
}

function concat_params_for_package
{
    PARAMS+="--package-app "
    PARAMS+="--manifest ${MANIFEST_FILE} "
    PARAMS+="--output ${APP_NAME}.iq "
    PARAMS+="--release "
    PARAMS+="--warn "
    PARAMS+="--private-key ${MB_PRIVATE_KEY} "
    PARAMS+="--rez ${RESOURCES} "
}

###

case "${1}" in
   build)
        concat_params_for_build
        ;;
   package)
        concat_params_for_package
        ;;
esac

###

cd ${PROJECT_HOME}

rm -f "${PROJECT_HOME}/${APP_NAME}"*.prg*
rm -f "${PROJECT_HOME}/${APP_NAME}"*.iq

java -jar "${MB_HOME}/bin/monkeybrains.jar" ${PARAMS} ${SOURCES}
