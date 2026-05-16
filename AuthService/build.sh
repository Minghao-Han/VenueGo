#!/bin/bash
exec bash "$(dirname "$0")/../scripts/build-service.sh" AuthService minghaohan/venuego-auth v1
