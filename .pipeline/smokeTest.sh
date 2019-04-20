#!/usr/bin/env bash
# this is simply testing if the application root returns HTTP 200
echo "curl -so /dev/null -w '%{response_code}' https://$1/actuator/health | grep 200"
curl -so /dev/null -w '%{response_code}' https://$1/actuator/health | grep 200
