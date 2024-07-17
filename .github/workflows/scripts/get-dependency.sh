DEPENDENCY=$1
VERSION=$2

tempFolder="./.github/workflows/.temp"
dependencyFile="$DEPENDENCY-$VERSION.jar"

# Find the file and copy it to the current directory if it exists
echo "Searching for $dependencyFile in $tempFolder"
echo "Listing all files in $tempFolder:"
ls -l $tempFolder

foundFiles=$(find $tempFolder -type f -name "$dependencyFile")

if [ -n "$foundFiles" ]; then
    echo "Found file: $foundFiles"
    echo $foundFiles
else
    echo "File not found."
    exit 1
fi