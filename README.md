# maven-runtime-agent
Java agent that periodically fetches and updates dependencies at runtime. It is mostly for applications that you want to stay
running continuously (even through releases), but you would like it to still fetch updates from dependencies while running. Not
only do you want them to be fetched while running, but you'd like them to load in dependent order properly. This is not always completely
possible because even classes within the same jar are dependent upon each other. One would hope that this is only useful for
SNAPSHOT dependencies that are frequently changing. Usually, this is the case of the application itself.

I developed this mostly for Kuali applications. The idea is that institutions can add patches, fixes, and customizations to the software
they are running without having to restart. This is also useful for loading updates to libraries they use like Rice or KC/KFS while
running.

Before you use this agent, you should know that it:
* Replaces classes and their schemas in the current running JVM.
* Intended for use with applications that are actively developed and constantly running.
* Intended to help developers develop without having to stop the application.
* Embeds Maven in your application. This makes your application dependent upon Maven
and all its dependencies at delivery.

## Usage

### With Tomcat

Add the following to `CATALINA_OPTS`
```
export CATALINA_OPTS="-javaagent:path/to/maven-runtime-agent.jar"
bin/start_tomcat.sh && tail -f log/catalina.out
```

### With Java Commandline
```
java -javaagent:path/to/maven-runtime-agent.jar <class name>
```

... or ...

```
export CATALINA_OPTS="-javaagent:path/to/maven-runtime-agent.jar"
java <class name>
```

### With tomcat7-maven-plugin
Add the following to `MAVEN_OPTS`

```
export MAVEN_OPTS="-javaagent:path/to/maven-runtime-agent.jar"
mvn tomcat7:run
```