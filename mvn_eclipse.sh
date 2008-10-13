rm -f .classpath .project
./goal.sh clean
./goal.sh -DdownloadJavadocs=true -DdownloadSources=true eclipse:eclipse
