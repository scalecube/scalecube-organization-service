#!/usr/bin/env bash

# This script generates Release api documentation and saves it to develop branch

cd $TRAVIS_BUILD_DIR && git checkout master

SCALECUBE_ORG_SERVICE='/tmp/scalecube-org-service'
SCALECUBE_ORG_SERVICE_DOCS='/tmp/scalecube-repo/organization-service/'
RELEASE=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec)
RELEASE_TEMPLATE='0.0.0-CURRENT'

# clone needed repositories
git clone git@github.com:scalecube/scalecube.github.io.git /tmp/scalecube-repo
git clone git@github.com:scalecube/scalecube-organization-service.git $SCALECUBE_ORG_SERVICE
git clone https://github.com/apidoc/apidoc.git /tmp/apidoc-repo

# build apidoc docker image
docker build -t apidoc /tmp/apidoc-repo

mkdir -p /tmp/docs-generated $SCALECUBE_ORG_SERVICE_DOCS && rm -rf $SCALECUBE_ORG_SERVICE_DOCS*

# set release version to documentation
for apidocument in $(find $TRAVIS_BUILD_DIR/ApiDocs -regex '.*\.\(apidoc\|json\)$'); do
    sed -i "s/$RELEASE_TEMPLATE/$RELEASE/g" $apidocument
    cat $apidocument >> /tmp/scalecube-org-service/ApiDocs/_apidoc.js
done

# generate documentation
docker run -u $(id -u) \
   -v $TRAVIS_BUILD_DIR/ApiDocs:/apidoc/docs \
   -v /tmp/docs-generated:/apidoc/docs-generated \
   -it apidoc -f ".*\\.(apidoc|js)$" -i "/apidoc/docs" -v -o "docs-generated"

# copy newly created documentation to http://scalecube.io/
cp -R /tmp/docs-generated/* $SCALECUBE_ORG_SERVICE_DOCS

# commit documentation
for project in $SCALECUBE_ORG_SERVICE $SCALECUBE_ORG_SERVICE_DOCS; do
    cd $project && \
    git add . && \
    git commit -m "Feature: updated organization-service documentation" && \
    git push
done
