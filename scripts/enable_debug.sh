#!/bin/bash -e

curl -u admin -v -X PUT -d "" -H "Content-Type: application/json" http://localhost:7990/bitbucket/rest/api/latest/logs/logger/com.isroot/debug
