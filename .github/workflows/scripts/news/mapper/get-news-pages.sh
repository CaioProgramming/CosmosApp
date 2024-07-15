BODY=$1


page_1=$(echo "$BODY" | grep "page_1:" | cut -d ':' -f2 | xargs)
page1_data=$(./get-news-pages.sh "$page_1")
page2_data=$(./get-news-pages.sh "$page_1")
page3_data=$(./get-news-pages.sh "$page_1")
page4_data=$(./get-news-pages.sh "$page_1")
page5_data=$(./get-news-pages.sh "$page_1")

pages_json=$(jq -n \
                  --arg p1 "$page1_data" \
                  --arg p2 "$page2_data" \
                  --arg p3 "$page3_data" \
                  --arg p4 "$page4_data" \
                  --arg p5 "$page5_data" \
                  '[$p1, $p2, $p3, $p4, $p5]')

echo $pages_json