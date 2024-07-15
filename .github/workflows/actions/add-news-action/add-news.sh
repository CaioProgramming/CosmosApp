#!/bin/bash

FILE_PATH=$1
FILE_NAME=$2
ISSUE_NUMBER=$3
ISSUE_BODY=$4

mappers_dir="./.github/workflows/scripts/news/mapper"
cd $mappers_dir


reference_data=$("$mappers_dir/get-news-reference.sh" "$ISSUE_BODY")
news_pages=$("$mappers_dir/get-news-pages.sh" "$ISSUE_BODY")
news_thumbnail=$("$mappers_dir/issue-body-mapper.sh" "$ISSUE_BODY" "thumbnail")

# Assuming you want to append the branch name to README.md

NEW_ITEM=$(
    jq -n \
    --arg id "$ISSUE_NUMBER"
    --arg reference "$reference_data"
    --arg pages "$news_pages"
    --arg thumbnailURL "$news_thumbnail" \
    '{id: $id, reference: $reference, pages: $pages, thumbnailURL: $thumbnailURL}'

)

newsPath="./resources$FILE_PATH"

jq --arg item "$NEW_ITEM" '.news += [$item]' "$newsPath" > news.json && mv news.json "$newsPath"
git add "$newsPath"
git commit -m "Add news item for issue # $ISSUE_NUMBER"
git push
echo "News item added successfully"