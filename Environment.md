Use the AI Assistant if needed to configure a maven build environment.

In the File list, click the vertical ... and choose Show Hidden Files.

.replit:

entrypoint = "Main.java"
hidden = ["**/*.class", "javafx-sdk-18.0.2"]

[unitTest]
language = "java"

[nix]
channel = "stable-22_11"

[debugger]
support = true

[debugger.compile]
command = "javac -classpath .:/run_dir/junit-4.12.jar:target/dependency/* -g -d . $(find . -type f -name '*.java')"

[debugger.interactive]
transport = "localhost:0"
connectTimeout = 60
startCommand = "java-debug"

[debugger.interactive.initializeMessage]
command = "initialize"
type = "request"

[debugger.interactive.initializeMessage.arguments]
adapterID = "cppdbg"
clientID = "replit"
clientName = "replit.com"
columnsStartAt1 = true
linesStartAt1 = true
locale = "en-us"
pathFormat = "path"
supportsInvalidatedEvent = true
supportsProgressReporting = true
supportsRunInTerminalRequest = true
supportsVariablePaging = true
supportsVariableType = true

[debugger.interactive.launchMessage]
command = "launch"
type = "request"

[debugger.interactive.launchMessage.arguments]
classPaths = ["."]
mainClass = "Main"

[workflows]
runButton = "Run JavaFX App"

[[workflows.workflow]]
name = "Run Tests"
mode = "sequential"

[[workflows.workflow.tasks]]
task = "shell.exec"
args = "find . -name \"*.class\" -delete"

[[workflows.workflow.tasks]]
task = "shell.exec"
args = "mvn test-compile"

[[workflows.workflow.tasks]]
task = "shell.exec"
args = "mvn test"

[[workflows.workflow]]
name = "Run JavaFX App"
author = 32876875
mode = "sequential"

[[workflows.workflow.tasks]]
task = "shell.exec"
args = "find . -name \"*.class\" -delete && javac --module-path javafx-sdk-18.0.2/lib --add-modules javafx.controls Main.java && java --module-path javafx-sdk-18.0.2/lib --add-modules javafx.controls Main"



pom.xml:


<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>blackjack</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>18.0.2</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>18.0.2</version>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>.</sourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>Main</mainClass>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>0.0.8</version>
                <configuration>
                    <mainClass>Main</mainClass>
                    <args>
                        <arg>-Djava.awt.headless=false</arg>
                        <arg>-Dprism.order=sw</arg>
                        <arg>-Dprism.verbose=true</arg>
                        <arg>-Dglass.platform=gtk</arg>
                        <arg>-Dmonocle.platform=Headless</arg>
                        <arg>-DDISPLAY=:0</arg>
                    </args>
                    <environmentVariables>
                        <DISPLAY>:0</DISPLAY>
                    </environmentVariables>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

