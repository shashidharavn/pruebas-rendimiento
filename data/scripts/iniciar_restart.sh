#!/bin/sh
#Inicio de los microservicios
sshpass -p 'official north beauty game' ssh -tt thoughtworks@10.0.9.212 'bash -s' < restart_microservices.sh
