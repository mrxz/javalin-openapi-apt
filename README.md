# javalin-openapi-apt
Annotation processing to generate OpenAPI specification from source code using [Javalin](https://javalin.io).

This annotation processor uses [JavaParser](https://github.com/javaparser/javaparser) to analyze the way a project uses Javalin and to generate a corresponding OpenAPI v3 document.
It can derive the paths (including path parameters), query parameters, request body and the response body (with the appropiate schema).
Some significant limitations apply (see Limitations section below). This project is a proof of concept, so usage in production is _not_ recommended.

## Limitations
* The processor only works with `javac`.
* The `ApiBuilder` of Javalin must be used to construct the paths. Only a single method for the full routing tree is supported.
* Handlers must be passed as method references to the `ApiBuilder` methods.
* Polymorphic models will only have their base class in the resulting OpenAPI document.
* Required property of parameters or fields on a model are not supported.
* Handlers that conditionally respond with different types of bodies or requires different paremeters aren't supported.

The processor tries to gracefully handle unsupported constructions by producing a descriptive error.
