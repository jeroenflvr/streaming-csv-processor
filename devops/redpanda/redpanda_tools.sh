#!/usr/bin/env bash

REDPANDA_PATH="/Users/jeroen/projects/ks-example/devops/redpanda"

start_redpanda() {
    echo "starting redpanda ..."
    cwd=$(pwd)
    cd $REDPANDA_PATH
    docker compose up -d
    cd $cwd
 
}
 
stop_redpanda() {
    echo "stopping redpanda ..."
    cwd=$(pwd)
    cd $REDPANDA_PATH
    docker compose down
    cd $cwd
}
 

create_topics(){
    rpk topic create local-stream-topic --brokers localhost:9093 -X tls.enabled=true --tls-truststore=/Users/jeroen/projects/ks-example/devops/redpanda/certs/ca.crt
    rpk topic create local-input-topic --brokers localhost:9093 -X tls.enabled=true --tls-truststore=/Users/jeroen/projects/ks-example/devops/redpanda/certs/ca.crt
    rpk topic create local-update-topic --brokers localhost:9093 -c cleanup.policy=compact -X tls.enabled=true --tls-truststore=/Users/jeroen/projects/ks-example/devops/redpanda/certs/ca.crt
    rpk topic create local-state-topic --brokers localhost:9093 -c cleanup.policy=compact -X tls.enabled=true --tls-truststore=/Users/jeroen/projects/ks-example/devops/redpanda/certs/ca.crt
    rpk topic create local-updates-only-topic --brokers localhost:9093 -X tls.enabled=true --tls-truststore=/Users/jeroen/projects/ks-example/devops/redpanda/certs/ca.crt
}
 