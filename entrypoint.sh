#!/bin/sh

cd /home
exec java -jar -Xmx64m nginxWebUI.jar "${BOOT_OPTIONS}" > /dev/null
