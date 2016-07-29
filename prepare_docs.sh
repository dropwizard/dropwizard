#!/usr/bin/env bash
#
# Builds the documentation of the Dropwizard project for the specified
# release, copies and commits it to the local gh-pages branch.
#
# Usage: ./prepare_docs.sh v1.0.1
#

set -e

[[ "$#" -eq 0 ]] && { echo "No release branch is specified"; exit 1; }

release_branch="$1"
release_number="${release_branch:1}"

echo -e "\nGenerating Dropwizard documentation"
echo "Release branch: $release_branch"
echo "Release number: $release_number"

echo -e "\n-------------------------------"
echo "Moving to $release_branch branch"
echo "-------------------------------"

git checkout "$release_branch"

echo -e "\n-------------------------------"
echo "Generating documentation"
echo -e "-------------------------------\n"

mvn clean site

echo -e "\n-------------------------------"
echo "Staging documentation"
echo "-------------------------------"
mvn site:stage

echo -e "\n-------------------------------"
echo "Moving to the gh-pages branch"
echo -e "-------------------------------\n"
git checkout gh-pages

echo -e "\n-------------------------------"
echo "Creating a directory for documentation"
echo -e "-------------------------------\n"
mkdir "$release_number"

echo -e "\n-------------------------------"
echo "Copy documentation"
echo -e "-------------------------------\n"
cp -r target/staging/* "${release_number}"/

echo -e "\n-------------------------------"
echo "Add and commit changes to the repository"
echo -e "-------------------------------\n"
git add .
git commit -m "Add docs for Dropwizard $release_number"

echo -e "\nDone!"
echo "Please review changes and push them with if they look good"
exit $?
