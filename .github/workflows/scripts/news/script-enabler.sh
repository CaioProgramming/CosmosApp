mappers_dir="./.github/workflows/scripts/news/mapper"

required_scripts=(
    "get-news-reference.sh"
    "get-news-pages.sh"
    "issue-body-mapper.sh",
    format-page.sh
)

for script in "${required_scripts[@]}"; do
    echo "enabling script $script"
    chmod u+r+x "$mappers_dir/$script"
    git update-index --chmod=+x "$mappers_dir/$script"
done
echo "News Scripts allowed"
ls -l