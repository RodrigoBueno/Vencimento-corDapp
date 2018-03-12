![Corda](https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png)

# CorDapp Template: Offline Version

This is a version of the V2 CorDapp template that has been adapted for use on networks with limited
connectivity to the outside world.

## Usage

This CorDapp template works identically to the online version, except that the dependencies must be gathered before use.

### Gathering the dependencies (REQUIRED)

You must complete this step before using the template in an offline environment. This step requires being online.

Once online, run the following command from the root of the project:
 
    ./gradlew gatherDependencies -Pdependencies.gather=true
    
This will gather all the dependencies required to run the template offline into the `lib/depdencies/` folder.
