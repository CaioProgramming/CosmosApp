#!/bin/bash

resourcePath="resources/news.json"
lastElementTitle=$(jq '.news[-1].pages[0].title' "$jsonFilePath")
lastElementThumbnail=$(jq '.news[-1].pages[0].thumbnail' "$jsonFilePath")
lastElementAuthor=$(jq '.news[-1].pages[0].reference.author' "$jsonFilePath")
lastElementLink=$(jq '.news[-1].pages[0].reference.link' "$jsonFilePath")
descriptions=$(jq -r '.news[-1].pages | map(.description) | join(" ")' "$resourcePath")

ecat << 'EOF'
![${lastElementTitle}](${lastElementThumbnail})
# ${lastElementTitle}
  
_published by [${lastElementAuthor}](${lastElementLink})_

${descriptions}

EOF