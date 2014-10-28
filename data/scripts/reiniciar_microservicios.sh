#!/usr/bin/expect -f
spawn ssh $QA_USER@$QA_HOST
expect -re "assword: "
send "\$QA_PASSWORD\r"
send "sudo su -\r"
send "cd /etc/init.d\r"
send "for i in \$(ls -d servicio-*)\r"
send "do\r"
send "echo \"Reiniciando \$i...\"\r"
send "service \$i restart\r"
send "done\r"
send "exit\r"
send "exit\r"
interact
