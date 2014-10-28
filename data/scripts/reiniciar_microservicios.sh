#!/bin/sh
sudo su -
cd /etc/init.d
microservicios = ($(ls -d servicio*/))
for i in "${microservicios[@]}"
do
  #service $i restart
  echo $i
done
