BODY="$1"
FIELD="$2"
echo "Mapping $BODY for $FIELD"
echo "$BODY" | grep "$FIELD" | cut -d ':' -f2 | xargs