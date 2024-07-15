BODY=$1

news_author=$(echo "$ISSUE_BODY" | grep "autor:" | cut -d ':' -f2 | xargs)
news_reference=$(echo "$ISSUE_BODY" | grep "reference:" | cut -d ':' -f2 | xargs)

json_output=$(jq -n \
    --arg author "$news_author" \
    --arg reference "$news_reference"\
    '{ "author": $author, "reference": $reference }'
    )

echo $json_output    