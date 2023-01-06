# [JourneyMap WebMap for Minecraft][1]

Source code and build resources for [JourneyMap WebMap][2] ([http://journeymap.info][2])

### Downloads
[CurseForge][6]

[Modrinth][7]

### Support
[Discord][5]

## Details
JourenyMap Client WebMap is an addon mod for JourneyMap that allows you to view your map in a local web browser. 
Main uses are displaying your map on another monitor, on a tablet, or any device on your local network. 

### What it is Not! 
It is not a server sided map. It will do nothing when added to the server. 

### Web Content
The project [WebMap Client][4] contains the javascript resources used by this project. 

## Requirements

* IntelliJ IDEA
* OpenJDK 1.17

## Environment Setup

### 1. Git the JourneyMap WebMap source

Check out a branch of the JourneyMap WebMap GIT repo to a directory called journeymap-webmap.  For example:

```sh
    git clone git@github.com:TeamJM/journeymap-webmap.git   
    cd journeymap-webmap-client
    git fetch && git checkout (branchname)
```

### 2. Setup JourneyMap WebMap with MultiLoader Template for IntelliJ IDEA

## IntelliJ IDEA
#### JourneyMap WebMap uses the [MultiLoader Template][3] for combining sources of Fabric and Forge.


1. If your default JVM/JDK is not Java 17 you will encounter an error when opening the project. This error is fixed by going to `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM`and changing the value to a valid Java 17 JVM. You will also need to set the Project SDK to Java 17. This can be done by going to `File > Project Structure > Project SDK`. Once both have been set open the Gradle tab in IDEA and click the refresh button to reload the project.
2. Open the Gradle tab in IDEA if it has not already been opened. Navigate to `Your Project > Common > Tasks > vanilla gradle > decompile`. Run this task to decompile Minecraft.
3. Open the Gradle tab in IDEA if it has not already been opened. Navigate to `Your Project > Forge > Tasks > forgegradle runs > genIntellijRuns`. Run this task to set up run configurations for Forge.
4. Open your Run/Debug Configurations. Under the Application category there should now be options to run Forge and Fabric projects. Select one of the client options and try to run it.
5. Assuming you were able to run the game in step 7 your workspace should now be set up.


#### Notes for IntelliJ 2020+:

To enable HotSwap:

1. Go to File > Settings... > Build, Execution, Deployment > Build Tools > Gradle
2. For the journeymap-webmap project, change "Build and run using" to "IntelliJ IDEA"

If builds begin failing with the error "java.lang.UnsupportedClassVersionError:
org/intellij/erlang/jps/model/JpsErlangModelSerializerExtension", you'll need to use a newer project JDK or simply
disable the Erlang plugin:

1. Go to File > Settings... > Plugins
2. Select the Erlang plugin and uncheck it to disable.
3. Press Apply and follow the prompts to restart IntelliJ

### 3. Build the jars

* Update `project.properties` version info
* Build using Gradle panel > journeymap-webmap > Tasks > build > build
* The end result will be in `build/libs/journeymap-webmap*.jar`

#### JourneyMap WebMap includes and/or makes use of the following software under their respective licenses:
[Javalin][8]:
[LICENSE][9] - [Apache License Version 2.0][10]

[1]: https://github.com:TeamJM/journeymap-webmap

[2]: http://journeymap.info

[3]: https://github.com/jaredlll08/MultiLoader-Template

[4]: https://github.com/TeamJM/webmap-client

[5]: https://discord.com/invite/eP8gE69

[6]: https://www.curseforge.com/minecraft/mc-mods/journeymap-web-map

[7]: https://modrinth.com/mod/journeymap-web-map

[8]: https://github.com/javalin/javalin

[9]: https://github.com/javalin/javalin/blob/master/LICENSE

[10]: http://www.apache.org/licenses/
