#!/bin/bash

BRANCH_NAME=$1
ISSUE_NUMBER=$2
ISSUE_NAME=$3
ISSUE_BODY=$4

echo "Edited by Issue $ISSUE_NAME including $ISSUE_BODY" >> README.md

reference_data=$(./get-news-reference.sh "$ISSUE_BODY")
news_pages=$(./get-news-pages.sh "$ISSUE_BODY")
news_thumbnail=$(./issue-body-mapper.sh "$ISSUE_BODY" "thumbnail")

# Assuming you want to append the branch name to README.md

NEW_ITEM=$(
    jq -n \
    --arg id "$ISSUE_NUMBER"
    --arg reference "$reference_data"
    --arg pages "$news_pages"
    --arg thumbnailURL "$news_thumbnail" \
    '{id: $id, reference: $reference, pages: $pages, thumbnailURL: $thumbnailURL}'

)

newsPath="resources/news.json"

jq --argjson item "$NEW_ITEM" '.newsItems += [$item]' "$newsPath" > temp.json && mv temp.json "$newsPath"
git add "$newsPath"
git commit -m "Add news item for issue #$ISSUE_NUMBER"
git config --global user.name 'github-actions[bot]'
git config --global user.email 'github-actions[bot]@users.noreply.github.com'
git commit -am "Edit README.md to include branch name"
git push origin "$BRANCH_NAME"