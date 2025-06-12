#!/bin/bash

echo "==== APPLICATION START ===="

APP_NAME="app.jar"
APP_DIR="/home/ubuntu/app"
LOG_DIR="/home/ubuntu/app"
LOG_FILE="$LOG_DIR/app.log"

# 로그 디렉토리 생성
mkdir -p "$LOG_DIR"

# 앱 실행
echo "Starting $APP_NAME with prod profile..."
nohup java -jar "$APP_DIR/$APP_NAME" --spring.profiles.active=prod > "$LOG_FILE" 2>&1 &

CLOUDWATCH_AGENT_BIN="/opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl"
CLOUDWATCH_CONFIG="/opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json"

# cloudwatch Agent 설치 확인 및 설치
if [ ! -x "$CLOUDWATCH_AGENT_BIN" ]; then
  echo "CloudWatch Agent not found. Installing..."
  sudo apt update -y
  sudo apt install -y wget
  sudo wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
  sudo dpkg -i -E ./amazon-cloudwatch-agent.deb
else
  echo "CloudWatch Agent is already installed."
fi

## 설정 파일 생성
#echo "Generating CloudWatch Agent configuration..."
#sudo mkdir -p "$(dirname "$CLOUDWATCH_CONFIG")"
#
#cat <<EOF | sudo tee "$CLOUDWATCH_CONFIG" > /dev/null
#{
#  "logs": {
#    "logs_collected": {
#      "files": {
#        "collect_list": [
#          {
#            "file_path": "$LOG_FILE",
#            "log_group_name": "MyAppLogs",
#            "log_stream_name": "{instance_id}",
#            "timestamp_format": "%Y-%m-%d %H:%M:%S"
#          }
#        ]
#      }
#    }
#  }
#}
#EOF
#
## 설정 파일 존재 여부 확인 후 agent 시작
#if [ -f "$CLOUDWATCH_CONFIG" ]; then
#  echo "Starting CloudWatch Agent..."
#  sudo "$CLOUDWATCH_AGENT_BIN" \
#    -a fetch-config \
#    -m ec2 \
#    -c file:"$CLOUDWATCH_CONFIG" \
#    -s
#else
#  echo "CloudWatch configuration file not found: $CLOUDWATCH_CONFIG"
#  exit 1
#fi
