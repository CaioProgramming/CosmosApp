DEPENDENCY=$1
VERSION=$2
GROUP_PATH=$3
tempFolder="./.github/workflows/.temp"
mavenUrl="https://repo1.maven.org/maven2/org"
echo requested dependency: $DEPENDENCY version: $VERSION
mkdir -p tempFolder
echo "Temp dir created"
dependencyURL="$mavenUrl/$GROUP_PATH/$DEPENDENCY/$VERSION/$DEPENDENCY-$VERSION.jar"
echo "Downloading $DEPENDENCY-$VERSION on $dependencyURL"

curl -L -o $tempFolder/$DEPENDENCY-$VERSION.jar $dependencyURL

# Check if the file exists
if [ -f "$tempFolder/$DEPENDENCY-$VERSION.jar" ]; then
    echo "Downloaded $DEPENDENCY-$VERSION.jar successfully."
else
    echo "Failed to download $DEPENDENCY-$VERSION.jar."
fi

echo "Current dependencies on $tempFolder"
ls $tempFolder
 echo "Downloaded $DEPENDENCY-$VERSION.jar"