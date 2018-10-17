# UrbanCode uDepoyRestClient Project
---

### License
This project is protected under the [Apache Version 2.0 License](https://www.apache.org/licenses/LICENSE-2.0.txt)

### Compatibility
The uDeployRestClient is used to interact with UrbanCode Deploy v6.0 or later and generally implemented through a plug-in or script. It provides wrapper functionality, through the REST client, with various UCD objects such as: Applications, Components, and Processes.

### Releases
Our most recent releases can be found on the [UrbanCode's Public Maven Repository](https://public.dhe.ibm.com/software/products/UrbanCode/maven2/com/ibm/urbancode/commons/uDeployRestClient/).

### Repository Standards
This project is a clone of an internal repository with Gradle modifications. Pull requests and suggestions must be validated internally before they appear publicly. Please use the [Issues tab](https://github.com/IBM-UrbanCode/uDeployRestClient/issues) for general support.

### Compiling Open Source Project
`gradle`

The Gradle .jar file can be found in build/libs once the project has been built, under the Releases tab, or as an artifact on Maven Repository. To add the uDeployRestClient project as a dependency in another Gradle project, add the following lines:
```
repositories {
    maven {
        url "https://public.dhe.ibm.com/software/products/UrbanCode/maven2/"
    }
}
dependencies {
    compile 'com.ibm.urbancode.commons:uDeployRestClient:+'
}
```

### Generating Java Docs
`gradle createDocs`

Generate new Java Docs with the above command.
