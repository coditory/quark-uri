# Quark URI
[![Build](https://github.com/coditory/quark-uri/actions/workflows/build.yml/badge.svg)](https://github.com/coditory/quark-uri/actions/workflows/build.yml)
[![Coverage](https://codecov.io/github/coditory/quark-uri/branch/master/graph/badge.svg?token=L6IOC9EBGO)](https://codecov.io/github/coditory/quark-uri)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.quark/quark-uri/badge.svg)](https://mvnrepository.com/artifact/com.coditory.quark/quark-uri)

**🚧 This library as under heavy development until release of version `1.x.x` 🚧**

> Just a URI manipulation library for Java. Provides URI builder and parser.

- lightweight, exactly 1 minimalistic dependency
- single purpose, not part of a framework
- does percent encoding and decoding
- adds option to encode/decode `+` character as a space

## Installation

Add to your `build.gradle`:

```gradle
dependencies {
    implementation "com.coditory.quark:quark-uri:0.0.3"
}
```

## Basic usage

Build uri string
```java
UriBuilder.fromUri("https://coditory.com?w=W&a=A")
    .addPathSegment("about")
    .addQueryParam("a", "X")
    .addQueryParam("a", "X")
    .addQueryParam("a", "Y")
    .addQueryParam("b", "Y")
    .addQueryParam("e", "")
    .buildUriString();

// Result:
// https://coditory.com/about?w=W&a=A&a=X&a=X&a=Y&b=Y&e=
```

Build uri components
```java
// uriComponents is a value object, that provides access to all parts of parsed uri
UriComponents uriComponents = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
    .addPathSegment("about")
    .addQueryParam("a", "X")
    .addQueryParam("a", "X")
    .addQueryParam("a", "Y")
    .addQueryParam("b", "Y")
    .addQueryParam("e", "")
    .buildUriComponents();

// Result:
// UriComponents{scheme="https", host="coditory.com", pathSegments=[about], queryParams={w=[W], a=[A, X, X, Y], b=[Y], e=[]}}
```

Parses encoded spaces and pluses:
```java
UriComponents plusesAsSpaces = UriBuilder.fromUri("/abc?a+b=A+B").buildUriComponents();
UriComponents encodedSpaces = UriBuilder.fromUri("/abc?a%20b=A%20B").buildUriComponents();
assert plusesAsSpaces == encodedSpaces

// Result:
// UriComponents{rootPath=true, pathSegments=[abc], queryParams={a b=[A B]}}
```

By default encodes spaces as `%20`
```java
UriBuilder.fromUri("https://coditory.com/a+bc/d%20ef/")
    .addPathSegment("x y ")
    .setFragment("frag ment")
    .addQueryParam("f oo", "b ar")
    .addQueryParam("x", "y+z")
    .buildUriString();

// Result:
// https://coditory.com/a+bc/d%20ef/x%20y%20?f%20oo=b%20ar&x=y%2Bz#frag%20ment
```