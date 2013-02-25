#!/bin/bash

COMMAND=$1
PORT=9000

if [ ! -z "$2" ]; then
	PORT=$2;
fi

echo com $COMMAND
echo port $PORT

function debug_port {
	DEBUG_PORT=$(($PORT+1000))
	export SBT_EXTRA_PARAMS="-Xdebug -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=n"
}

function start_app {
	debug_port
	mkdir -p ./logs

	if [ -f "./logs/application.log"  ]; then
		kill_app
	fi
  echo " Staging application"
  ./sbt stage
	echo " Starting application "
	nohup ./target/start -Dhttp.port=$PORT > "./logs/application.log" 2>&1 &
}

function kill_app {
	if [ -f ./RUNNING_PID ]; then
		echo " Killing running application "
		kill `cat ./RUNNING_PID`
		`rm -f "./logs/application.log"`
		`rm -f "./RUNNING_PID"`
	fi
	sleep 2
}

case "$COMMAND" in
	'start')
		start_app
		;;
	'stop')
		kill_app
		;;
	*)
		echo "Usage $0 {app_ROOT_DIR app_name | [start|stop] [port] }"
		;;
esac
exit 0

