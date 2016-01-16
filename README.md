# jello [![Build Status](https://travis-ci.org/uniformlyrandom/jello.png)](https://travis-ci.org/uniformlyrandom/jello)
pure macro based JSON serialization targeting mainly Scala-js and Play-json format compatibility

## Why did I create yet another JSON library?

Scala.js prohibits dynamically invoked code (it does static analasys to do dead code elimination–to minimize generated package size), so one has to use macros where one would otherwise use mechanizems such as reflection etc.

At the same time, the Scala.js community created some idiomatic JSON marshaling/pickling libraries which were a far cry from "generic" handling of back-end JSON parsing libraries (such as Play-json and the likes). As it turned out, if one wants to use one of these generic libraries on the back-end he could not use any of the existing Scala-js compatible JSON libraries.
