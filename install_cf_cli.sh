#!/bin/bash

echo "Installing cf cli"
if [[ ! -e ${CF_DIR}/cf ]]; then
    mkdir -p ${CF_DIR}
    cd ${CF_DIR}
    wget "https://cli.run.pivotal.io/stable?release=linux64-binary&source=github" -q -O cf.tgz && tar -zxvf cf.tgz && rm cf.tgz
    ./cf --version
    cd ..
    export PATH=${PATH}:${CF_DIR}
fi
