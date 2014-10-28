#!/usr/bin/expect -f
spawn ssh thoughtworks@181.112.147.247
expect -re "assword: "
send "official north beauty game\r"
send "sudo su -\r"
send "cd /etc/init.d\r"
send "for i in \$(ls -d servicio-*)\r"
send "do\r"
send "echo \"Reiniciando \$i...\"\r"
# send "service \$i restart\r"
send "done\r"
send "exit\r"
send "exit\r"
