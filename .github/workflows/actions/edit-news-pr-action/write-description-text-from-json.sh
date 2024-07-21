#!/bin/bash
cd ./resources
ls -a
jsonFilePath="./news.json"

jsonContent=$(cat "$jsonFilePath")

lastElementTitle=$(echo "$jsonContent" | jq '.news[-1].pages[0].title')
lastElementThumbnail=$(echo "$jsonContent" | jq '.news[-1].pages[0].thumbnailURL')
lastElementAuthor=$(echo "$jsonContent" | jq '.news[-1].reference.author')
lastElementLink=$(echo "$jsonContent" | jq '.news[-1].reference.reference')
descriptions=$(echo "$jsonContent" | jq -r '.news[-1].pages | map(.description) | join("\n\n")')

cat << EOF
![${lastElementTitle}](${lastElementThumbnail})
# ${lastElementTitle}
  
_published by [${lastElementAuthor}](${lastElementLink})_

${descriptions}

EOF