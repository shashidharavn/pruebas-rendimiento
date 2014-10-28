#!/bin/sh
ssh thoughtworks@181.112.147.247
expect "assword:"
send "official north beauty game"
interact
sudo su -
cd /etc/init.d
microservicios = ($(ls -d servicio*/))
for i in "${microservicios[@]}"
do
  #service $i restart
  echo $i
done
