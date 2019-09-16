<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.faendir</groupId>
  <artifactId>parent</artifactId>
  <version>0.9.1-SNAPSHOT</version>
  <packaging>pom</packaging>
  <modules>
    <module>message-generator-maven-plugin</module>
    <module>acrarium</module>
  </modules>
  <scm>
    <connection>scm:git:git://github.com:F43nd1r/Acrarium.git</connection>
    <developerConnection>scm:git:git@github.com:F43nd1r/Acrarium.git</developerConnection>
    <url>https://github.com/F43nd1r/Acrarium</url>
    <tag>HEAD</tag>
  </scm>
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-release-plugin</artifactId>
        <version>2.5.3</version>
        <configuration>
          <autoVersionSubModules />
          <tagNameFormat>v@{project.version}</tagNameFormat>
          <releaseProfile>release</releaseProfile>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
