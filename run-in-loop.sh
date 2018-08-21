#!/usr/bin/env bash

for i in `seq 1 100`;
do
    java -javaagent:build/libs/callspy-0.0.1-SNAPSHOT.jar Main 2>error.txt 1>all_methods_called.txt
    node split.js all_methods_called.txt $i
done

node stats.js first_call > stats_first_call.txt

node stats.js second_call > stats_second_call.txt
