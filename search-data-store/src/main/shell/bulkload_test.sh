#!/bin/bash


WORK_DIR=${PWD}


java_cp=search-data-store-1.5.2-SNAPSHOT.jar
for jf in $WORK_DIR/lib/*.jar ;
do
        java_cp=$jf:$java_cp;
done

java -cp $java_cp com.yihaodian.store.bulkload.HBaseRecordBulkLoader



