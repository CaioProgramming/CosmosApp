$BODY=$1


NEWS_PAGE=$(
    jq -n \
    --arg title "$BODY" \
    '{title: $title, description: '', thumbnailURL: '' }'
)

echo $NEWS_PAGE
