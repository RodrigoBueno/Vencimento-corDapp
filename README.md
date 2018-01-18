![Corda](https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png)

# CorDapp Template: Offline Version

Version supported: `M14`

This is a version of the CorDapp template which has been adapted for use on networks with limited
connectivity to the outside World!

#### Pre-requisites:

* Oracle JDK (minimum version 1.8u131 at the time of writing)
* IntelliJ IDEA CE (minimum version 2017.1 at the time of writing)
* git

#### Changes made:

* Adapted `build.gradle` file to look for all the Corda dependencies in the `/lib/dependencies` folder
* Adapted `gradle-wrapper.properties` file to look for `gradle-3.4.1-all.zip` in the `/gradle` folder

**Note:** All the dependencies and Gradle are included in this repo (so it's big, ~300MB)

#### Update Instructions

To update this template you must;

1. Add the `upstream` template as a remote with: `git remote add upstream git@github.com:corda/cordapp-template-kotlin.git`
2. Fetch the latest updates from upstream: `git fetch upstream`
3. Rebase under the upstream master: `git rebase upstream/master`
4. Checkout the release branch you want to release: `git checkout upstream release-M<version>`
5. Cherry pick all commits between `upstream/master` and `master` with `git cherry-pick upstream/master..master`
6. Tag you release
7. Push your tag

You have now updated and released.

#### Release instructions

You must be online to create a release and then run;
 
 1. `git checkout .`  - this will erase any local changes (required)
 2. `git clean -df` - this will erase any IntelliJ information (optional)
 2. `./gradlew clean` - cleans build directores (required)
 1. `rm -rf lib/dependencies` - cleans previous dependencies (optional)
 2. `./gradlew gatherDependencies` - while _online_ (required) 
 3. zip this directory - it is fine to include the git history and other files.
 