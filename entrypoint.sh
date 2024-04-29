#!/bin/sh

cd /home
exec java -Xmx128m -jar -Dfile.encoding=UTF-8 nginxWebUI.jar ${BOOT_OPTIONS} > /dev/null
