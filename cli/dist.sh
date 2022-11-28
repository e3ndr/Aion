#!/bin/bash

# +-------------------------------------------------------------------------------------------------------+
# | Requires zip to be installed:                                                                         |
# | Unix users can use a package manager to install it (ironic.)                                          |
# | Windows users can follow the steps on this StackOverflow answer: https://stackoverflow.com/a/55749636 |
# |                                                                                                       |
# | Also requires Java 11 to be installed on your machine.                                                |
# +-------------------------------------------------------------------------------------------------------+

# Dist dir.
rm -rf "dist"
mkdir "dist"

# Build the jar.
mvn install

JAVA_PACKAGE="java-temurin-jre"
JAVA_VERSION="11"

PLATFORMS=(
    # Windows
    "distribution='WINDOWS_NT';arch='X86_64'"
    "distribution='WINDOWS_NT';arch='X86'"

    # ---- TODO implement the decompression for .tar.gz so we can build for Linux and macOS. ----
    # Linux
    #"distribution='LINUX';arch='X86_64'"
    #"distribution='LINUX';arch='AARCH64'"
    #"distribution='LINUX';arch='ARM'"

    # macOS
    #"distribution='MACOS';arch='X86_64'"
    #"distribution='MACOS';arch='AARCH64'"
)

for entry in ${PLATFORMS[@]}; do
    echo ""

    declare distribution arch
    eval $entry # Creates `distribution` and `arch`.

    folder="dist/temp-$distribution-$arch"
    mkdir $folder

    echo "-- Building for: $distribution:$arch..."
    cp "target/Aion.jar" "$folder/Aion.jar"

    echo "-- Building the sources-cache..."
    java \
        -Daion.basedir=$folder \
        -jar "$folder/Aion.jar" \
        sources refresh

    # We actually use Aion to grab the Java runtime to be used. Kinda clever IMO.
    echo "-- Installing Java and building path..."
    java \
        -Daion.basedir=$folder \
        -jar "$folder/Aion.jar" \
        --override-architecture $arch \
        --override-distribution $distribution \
        install $JAVA_PACKAGE:$JAVA_VERSION -y

    echo "-- Cleaning up..."
    mv "$folder/packages/$JAVA_PACKAGE/$JAVA_VERSION/binary" "$folder/runtime"

    # Remove everything, but keep the path folder though!
    rm -rf $folder/download-cache
    rm -rf $folder/packages
    rm $folder/config.json
    rm $folder/sources-cache.json
    rm $folder/install-cache.json
    rm $folder/path/java
    rm $folder/path/java.bat # Added during the install/download of java for the runtime.

    echo "-- Compressing..."
    cd $folder
    archiveName="$distribution-$arch"

    if [ $distribution = "WINDOWS_NT" ]; then
        # We want to use zip on windows.
        zip -r "../$archiveName.zip" "."
    else
        tar czf "../$archiveName.tar.gz" "."
    fi

    cd ../..

    # Cleanup.
    rm -rf $folder
    echo "-- Done!"
done
