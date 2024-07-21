#!/bin/bash
ITEM=$1

jsonContent=$(cat "$ITEM")

lastElementTitle=$(echo "$jsonContent" | jq '.news[-1].pages[0].title')
lastElementThumbnail=$(echo "$jsonContent" | jq '.news[-1].pages[0].thumbnailURL')
lastElementAuthor=$(echo "$jsonContent" | jq '.news[-1].reference.author')
lastElementLink=$(echo "$jsonContent" | jq '.news[-1].reference.reference')
descriptions=$(echo "$jsonContent" | jq -r '.news[-1].pages | map(.description) | join("\n\n")')

printf "![${lastElementTitle}](${lastElementThumbnail})\n# ${lastElementTitle}\n_published by [${lastElementAuthor}](${lastElementLink})_\n${descriptions}"