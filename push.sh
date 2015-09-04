source ./api_token.sh

ssh kidder@thekidder.com  << EOF
  # stop existing slackers-quest processes
  docker ps | grep --color=never slackers-quest | awk '{ print \$1 }' | xargs docker stop
  # get the latest source and build
  cd slackers-quest
  git pull
  sbt docker:publishLocal
  # run, using the API_TOKEN sourced at the beginning of this script
  docker run -d -e API_TOKEN=$API_TOKEN slackers-quest
EOF
