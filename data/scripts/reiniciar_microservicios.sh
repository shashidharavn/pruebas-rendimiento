#!/usr/bin/expect -f
spawn ssh $env(QA_USER)@$env(QA_HOST)
expect -re "assword: "
send "$env(QA_PASSWORD)\r"
send "sudo su -\r"
send "cd /etc/init.d\r"
send "for i in \$(ls -d servicio-*)\r"
send "do\r"
send "echo \"Reiniciando \$i...\"\r"
send "service \$i start\r"
send "done\r"
send "exit\r"
send "exit\r"
interact
