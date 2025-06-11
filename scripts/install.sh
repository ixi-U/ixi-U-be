#!/bin/bash

echo "install phase"

# 앱 프로세스 종료
APP_NAME="app.jar"
APP_DIR="/home/ubuntu/app"
PID=$(pgrep -f $APP_NAME)

sudo rm -f "$APP_DIR/$APP_NAME"

if [ -n "$PID" ]; then
  echo "Stopping existing application (PID: $PID)"
  sudo kill -9 "$PID"
  rm -f "$APP_DIR/$APP_NAME"
else
  echo "No application to stop"
fi

# 앱 디렉토리 생성
mkdir -p "$APP_DIR"
