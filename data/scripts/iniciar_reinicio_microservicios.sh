#!/bin/sh
sshpass -e ssh -oStrictHostKeyChecking=no -tt $QA_USER@$QA_HOST 'bash -s' < restart_microservices.sh
