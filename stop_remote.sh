ssh kidder@thekidder.com  << EOF
  # stop existing slackers-quest processes
  docker ps | grep --color=never slackers-quest | awk '{ print \$1 }' | xargs docker stop
EOF
