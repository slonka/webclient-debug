#!/usr/bin/env bash

for i in `seq 1 100`;
do
    java -javaagent:build/libs/webclientdebug-0.0.1-SNAPSHOT.jar net.slonka.webclientdebug.Main 2>error.txt 1>all_methods_called.txt
    node split.js all_methods_called.txt $i
    perl -n -e'/\s+<.*- took (\d+\.\d+) ms.*/ && print $1 . "\n"' < output/first_call_$i.txt > output/first_call_stripped_$i.txt
    perl -n -e'/\s+<.*- took (\d+\.\d+) ms.*/ && print $1 . "\n"' < output/second_call_$i.txt > output/second_call_stripped_$i.txt
done

node stats.js first_call_stripped > stats_first_call.txt

node stats.js second_call_stripped > stats_second_call.txt
