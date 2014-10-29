#!/bin/sh
sshpass -p 'official north beauty game' ssh -oStrictHostKeyChecking=no -tt thoughtworks@10.0.9.212 'bash -s' < restart_microservices.sh
