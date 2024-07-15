BODY=$1

mappers_dir="./.github/workflows/scripts/news/mapper"
pages=()
for in in {1..5}
do 
  echo "Mapping page $i"
  page=$($("$mappers_dir/format-page.sh" "$BODY" "page_$i"))
  pages+=($page)
done
echo "Pages: ${pages[@]}"

pages_json=$(jq -n \
                  --arg pages "$pages" \
                  '[$pages]')

echo $pages_json