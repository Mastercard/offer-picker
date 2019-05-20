#!/bin/bash
echo $$ > /tmp/run_offer_picker.pid
rm offer_picker.log
rm /dev/shm/testFI/result/*.csv
nohup java -Xmx100G  -jar OfferPicker-jar-with-dependencies.jar OfferPicker.properties  > offer_picker.log &
pid=$!
echo "waiting OfferPicker applicaiton finish" $pid
wait $pid
echo "OfferPicker applicaiton finished"
echo "merge all csv files to one file"
awk 'FNR==1 && NR!=1{next;}{print}' /dev/shm/testFI/result/*.csv > /dev/shm/testFI/result/OfferPickerAllResu