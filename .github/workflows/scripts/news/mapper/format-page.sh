BODY="$1"
FIELD="$2"
echo "Mapping Body => $BODY"
mappers_dir= "./.github/workflows/scripts/news/mapper"
mapper_script= "$mappers_dir/issue-body-mapper.sh"
DESCRIPTION= $($mapper_script "$BODY" "$FIELD")
NEWS_PAGE=$(
    jq -n \
    --arg title "$DESCRIPTION" \
    '{title: '', description: '$title', thumbnailURL: '' }'
)

echo $NEWS_PAGE
