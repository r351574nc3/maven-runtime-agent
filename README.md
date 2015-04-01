# maven-runtime-agent
Java agent that periodically fetches and updates dependencies at runtime. It is mostly for applications that you want to stay
running continuously (even through releases), but you would like it to still fetch updates from dependencies while running. Not
only do you want them to be fetched while running, but you'd like them to load in dependent order properly. This is not always completely
possible because even classes within the same jar are dependent upon each other. One would hope that this is only useful for
SNAPSHOT dependencies that are frequently changing. Usually, this is the case of the application itself.

I developed this mostly for Kuali applications. The idea is that institutions can add patches, fixes, and customizations to the software
they are running without having to restart. This is also useful for loading updates to libraries they use like Rice or KC/KFS while
running.

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
Add the following to your `pom.xml`
```
...
        <plugin>
          <groupId>org.apache.tomcat.maven</groupId>
          <artifactId>tomcat7-maven-plugin</artifactId>
          <version>${plugin.tomcat.version}</version>
          <configuration>
            <path>/${project.artifactId}-${build.environment}</path>
            <systemProperties>
              <org.apache.el.parser.SKIP_IDENTIFIER_CHECK>true</org.apache.el.parser.SKIP_IDENTIFIER_CHECK>
            </systemProperties>
          </configuration>
          <dependencies>
            <dependency>
              <groupId>mysql</groupId>
              <artifactId>mysql-connector-java</artifactId>
              <version>${mysql.version}</version>
              <scope>provided</scope>
            </dependency>
          </dependencies>
        </plugin>
...
```