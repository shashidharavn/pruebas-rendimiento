#!/bin/sh
sudo su -
cd /etc/init.d
for i in $(ls -d servicio-*)
do
echo "Reiniciando $i..."
service $i restart
done
exit
exit
