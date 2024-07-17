DEPENDENCY=$1
VERSION=$2

tempFolder="./.github/workflows/.temp"
dependencyFile="$DEPENDENCY-$VERSION.jar"

# Find the file and copy it to the current directory if it exists
echo "Searching for $dependencyFile in $tempFolder"
echo -e "\n"
echo "Listing all files in $tempFolder:"
ls -l $tempFolder
echo -e "\n"
foundFiles=$(find $tempFolder -type f -name "$dependencyFile")

if [ -n "$foundFiles" ]; then
    echo "Found file: $foundFiles"
    echo -e "\n"
    echo $foundFiles
else
    echo "File not found."
    echo -e "\n"
    exit 1
fi