DEPENDENCY=$1
VERSION=$2
GROUP_PATH=$3
tempFolder="./.github/workflows/.temp"
mavenUrl="https://repo1.maven.org/maven2/org"
echo requested dependency: $DEPENDENCY version: $VERSION

# Check if the directory already exists
if [ ! -d "$tempFolder" ]; then
    mkdir -p "$tempFolder"
    echo "Temp dir created"
    git add "$tempFolder/"
    git commit -m "Add new temp directory with dependencies"
else
    echo "Temp dir already exists"
fi

dependencyURL="$mavenUrl/$GROUP_PATH/$DEPENDENCY/$VERSION/$DEPENDENCY-$VERSION.jar"

echo "Downloading $DEPENDENCY-$VERSION on $dependencyURL"

curl -L -o $tempFolder/$DEPENDENCY-$VERSION.jar $dependencyURL

# Check if the file exists
tempFile="$tempFolder/$DEPENDENCY-$VERSION.jar"
if [ -f $tempfile ]; then
    echo "Downloaded $DEPENDENCY-$VERSION.jar successfully."
else
    echo "Failed to download $DEPENDENCY-$VERSION.jar."
    exit 1
fi

echo "Current dependencies on $tempFolder"
ls $tempFolder
 echo "Downloaded $DEPENDENCY-$VERSION.jar"