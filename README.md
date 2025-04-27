# pintu-api-test JAVA

Repository for test automation scripts

Supported Features :

1REST API using rest-assured


## Prerequisites

* [JDK 11](https://jdk.java.net/11/) or later version
* [Git](https://git-scm.com/downloads)
* [Intellij IDEA](https://www.jetbrains.com/idea/download/)

Optional:

* [Allure](https://docs.qameta.io/allure/#_installing_a_commandline) for reports

## Getting started

1. Clone the test repo, and some prerequisites:

    - make sure you use JDK 11 or above
    - make sure you have the necessary `configs` file, ask the team!

2. Add IntelliJ [Lombok][1] plugin:
    * Open `Preferences` (⌘ + ,)
    * Go to `Plugins` > `Marketplace`
    * Search for `Lombok` and Install

3. Compile the project
    * run `./gradlew clean assemble` in the terminal

## Testing

### Command-line

Use following command-line to execute the test.

    ./gradlew test -DtestEnv=dev -Ddebug=true

Use following command for spesific class

    ./gradlew test "rest.testWebSocketTrade.testSubscribeToTradeStream" -Ddebug=true -DtestEnv=dev

This will run the example test suite. Change the config and suiteXmlFile as needed.

- note: `configfailurepolicy` needs to be set to `continue` so one error does not propagate to other unrelated tests.

### Intellij IDEA

* Open `Run > Edit Configurations`
* Pick `Templates > TestNG / Gradle` from the list
* Add `-Dconfig="config/dev.yaml"` to VM Options

### Reporting

* Follow this for the installation https://allurereport.org/docs/install/
* After each test execution there will be new file created `allure-results`
* once allure already installed you can run on your prompt `allure serve {allure-results-directory}` to get the allure reporting opened

## Helpful Links

* https://www.baeldung.com/install-maven-on-windows-linux-mac
* https://installvirtual.com/how-to-install-openjdk-13-on-mac-using-brew/

## Notes

Feel free to add more stuff as necessary, just try to keep this clean.


[1]: https://projectlombok.org/