BODY="$1"
FIELD="$2"
#echo "Mapping Body => $BODY"
#echo "Requiring => $FIELD"
mapper_script="./.github/workflows/scripts/news/mapper/issue-body-mapper.sh"
description=$("$mapper_script" "$BODY" "$FIELD")
news_page=$(
    jq -n \
    --arg description "$description" \
    '{title: "", description: $description, thumbnailURL: "" }'
)
echo $news_page
