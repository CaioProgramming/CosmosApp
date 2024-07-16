BODY=$1

pages_formatter="./.github/workflows/scripts/news/mapper/format-page.sh"
pages=()
for i in {1..5}
do 
  pages+=$("$pages_formatter" "$BODY" "page_$i")
done

pages_json=$(printf '%s\n' "${pages[@]}" | jq -R . | jq -s .)

echo "${#pages[@]} pages: $pages_json"
echo $pages_json