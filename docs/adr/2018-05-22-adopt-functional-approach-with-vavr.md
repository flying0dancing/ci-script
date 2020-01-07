# Implement Functional Programming approach and use io.vavr as preferred library 

## Status 
Accepted

## Context
The FCR project makes use of Apache Spark as its primary library. This library implements a functional approach using 
the Dataset object as a Functor/Monad with map/flatmap/etc methods.

Therefore it is suggested that the entire codebase adopt this functional approach so that the codebase is consistent.

#### Approach
- Use Monads/Embellishment instead of side effects in functions
    - This approach most obviously beneficial in the performance of dataset transformations
    - But this will also benefit the performance of the code base as a whole 
- Prefer return type embellishment over Exceptions 
- Use io.vavr to provide useful classes for this embelilshment 

#### Upgrading java.slang
The library java.slang was already used in the project but due to copyright issues the library [changed its name to
io.vavr](http://blog.vavr.io/javaslang-changes-name-to-vavr/), the code base needed to be upgraded accordingly.

The java.slang / [io.vavr](http://www.vavr.io/) library provides [AlgebraicDataTypes](https://en.wikipedia.org/wiki/Algebraic_data_type) such as Either/Option/Try that allow for a functional style of 
programming in Java. 
This marries well with the domain of the project as Spark requires a more function oriented programming approach when 
transforming data, as well as utilising the Scala classes for its encoding.

The library has other features such as
- Easy Functional composition support
- Pattern matching
- Jackson marshaling

## Decision
The io.vavr library is to be used as the go to choice for Functional Programming style objects.

Where possible Monads are preferred in place of Exceptions (using the io.vavr.control objects).
This should most obviously be done in Dataset functions but using this approach elsewhere has equal benefits.

## Consequences
- A standard has been set an all code should use io.vavr as the preferred choice (unless otherwise necessary)
- Easier functional composition support
- Richer array of Functor/Monadic objects
- Easier conversion of unwanted exceptions to Monads
- Hopefully with more exposure to the functional style the quality of "functional code" will improve
- Increased performance due to lack of excessive exception throwing