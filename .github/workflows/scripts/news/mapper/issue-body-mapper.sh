BODY="$1"
FIELD="$2"
#echo "Getting $FIELD from Body => $BODY"
echo "$BODY" | grep "$FIELD" | cut -d ':' -f2 | xargs