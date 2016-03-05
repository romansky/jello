# jello [![Build Status](https://travis-ci.org/uniformlyrandom/jello.png)](https://travis-ci.org/uniformlyrandom/jello) [![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.6.svg)](https://www.scala-js.org)

Scala.js & JVM JSON marshalling library with straightforward format.

## Why did I create yet another JSON library?

Scala.js prohibits dynamically invoked code, so one has to use macros where one would otherwise use mechanisms such as reflection etc.

At the same time, the Scala.js community created some idiomatic marshaling/pickling libraries which were a divergence from the common. (Play Framework etc..) 
As it turned out, if one wants to use one of these generic libraries on the back-end he could not use any of the existing Scala-js compatible JSON libraries.

Thus Jello was born.

# Usage

## Installation

Edit `build.sbt` and add the following `libraryDependencies ++= Seq("com.uniformlyrandom" %%% "jello" % "0.3.0)`

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
  