$BODY=$1
$FIELD=$2

$VALUE=$(echo "$BODY" | grep "$FIELD" | cut -d ':' -f2 | xargs)