BODY=$1
FIELD=$2
echo "Mapping Body => $BODY"
mappers_dir="./.github/workflows/scripts/news/mapper"
MAPPER_SCRIPT = "$mappers_dir/issue-body-mapper.sh"
DESCRIPTION= $($MAPPER_SCRIPT "$BODY" "$FIELD")
NEWS_PAGE=$(
    jq -n \
    --arg title "$DESCRIPTION" \
    '{title: '', description: '$title', thumbnailURL: '' }'
)

echo $NEWS_PAGE
