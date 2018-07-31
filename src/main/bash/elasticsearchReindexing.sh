#!/bin/bash
# getting complete index, here: "deletions", and index this again in a new index
# depends on https://github.com/moshe/elasticsearch_loader
# Should work with ES version >2.4
#
# author:dr0i
# date: 201808

# variables you should change to your needs:
ES_HOST=localhost
ES_INDEX=deletions
ES_TYPE=resource
ES_INDEX_CONFIG=index-config-deletions.json
# number of retrieving docs := SIZE * ITERATIONS - SIZE
SIZE=1000
ITERATIONS=33
OUT=deletions.json

##############
#  functions #

function initial() {
curl -XPOST  "http://$ES_HOST:9200/$ES_INDEX/$ES_TYPE/_search?scroll=1m"  -d"
{
 \"size\": $SIZE,
  \"sort\": [
    \"_doc\"
  ]
}"
}

function scrollWithId(){
curl -XPOST  "http://$ES_HOST:9200/_search/scroll"  -d"
{
    \"scroll\" : \"1m\",
    \"scroll_id\" : \"$SCROLLID\"
}"
}
#########
#  main #

initial > tmp_$OUT
SCROLLID=$(sed  's#.*scroll_id":"\(.*\)","took".*#\1#g'  tmp_$OUT | tail -n1)

i=0;
while [ $i -lt $ITERATIONS ]; do
	i=$( expr $i + 1 )
	scrollWithId >> tmp_$OUT
done

# format nice json for indexing
echo "[" > $OUT ;
sed  's#_source\":#\n#g' tmp_$OUT |sed  's#"sort":.*##g' |sed  's#.*_scroll_id.*##g'|grep  '{' >> $OUT
truncate -s-2 $OUT
echo "]" >> $OUT

# count if all is there:
echo "Got so much documents:"
grep -o '\<alephInternalSysnumber\>' $OUT| wc -l
echo "Good? Waiting 10 seconds before indexing so you can think about it ..."

sleep 10

# Finally indexing:
elasticsearch_loader --es-host http://$ES_HOST:9200 --index $ES_INDEX-$(date +%Y%m%d-%H%M) --type $ES_TYPE --index-settings-file $ES_INDEX_CONFIG json $OUT
