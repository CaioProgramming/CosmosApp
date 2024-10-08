DEPENDENCY=$1
VERSION=$2

tempFolder="./.github/workflows/.temp"
dependencyFile="$DEPENDENCY-$VERSION.jar"

# Find the file and copy it to the current directory if it exists
foundFiles=$(find $tempFolder -type f -name "$dependencyFile")

if [ -n "$foundFiles" ]; then
    echo "$foundFiles" | head -n 1
else
    exit 1
fi