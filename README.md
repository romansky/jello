# jello 

[![Build Status](https://travis-ci.org/romansky/jello.png)](https://travis-ci.org/romansky/jello) 
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.6.svg)](https://www.scala-js.org)
[![Maven Central](https://img.shields.io/maven-central/v/org.apache.maven/apache-maven.svg)](http://search.maven.org/#artifactdetails|com.uniformlyrandom|jello_2.11|0.3.0|jar)

http://search.maven.org/#artifactdetails|com.uniformlyrandom|jello_2.11|0.3.0|jar

Scala.js & JVM JSON marshalling library with straightforward format.

## Why did I create yet another JSON library?

Scala.js prohibits dynamically invoked code, so one has to use macros where one would otherwise use mechanisms such as reflection etc.

At the same time, the Scala.js community created some idiomatic marshaling/pickling libraries which were a divergence from the common. (Play Framework etc..) 
As it turned out, if one wants to use one of these generic libraries on the back-end he could not use any of the existing Scala-js compatible JSON libraries.

Thus Jello was born.

# Usage

## Installation

```
libraryDependencies ++= Seq("com.uniformlyrandom" %%% "jello" % "1.0.0)
```

## Overview

`Jello` takes inspiration from `Play Json`, the formatters need to be provided implicitly, it's recommended to have the companion object contain these formatters
 
```scala
case class A (
    m1: String,
    m2: Int
)

object A {
    implicit fmt: JelloFormat[A] = JelloFormat.format[A]
}

object App {

    import com.uniformlyrandom.jello.TypesLibrary._

    def main(args: Array[String]): Unit = {
        val a: A = A("value",1)
        val aJV: JelloValue = JelloJson.toJson(a)
        val aJson: String = JelloJson.toJsonString(aJV)
        //aJson == {"m1":"value","m2":1}
        val ajJV: JelloValue = JelloJson.parse(aJson)
        val aTry: Try[A] = JelloJson.fromJson(ajJV)
        
        assert(Try(a) == aTry)
    }
}
```

### Supported features

 * `Enumeration`s support
 * helper constructs to serialize `trait`s
  