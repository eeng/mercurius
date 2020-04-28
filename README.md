# Mercurius

This is a demo application written with the purpose of exploring [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) concepts in the context of functional programming languages.

[![Build Status](https://travis-ci.org/eeng/mercurius.svg?branch=master)](https://travis-ci.org/eeng/mercurius)

## Implementation

The application consist of a backend written in [Clojure](https://clojure.org/) and a frontend SPA written in [ClojureScript](https://clojurescript.org/). In addition, the following libraries assisted the project:

- [Integrant](https://github.com/weavejester/integrant) was used to wire all the component's dependencies and manage the stateful resources.
- [Sente](https://github.com/ptaoussanis/sente) provides realtime communication between frontend and backend through Web Sockets.
- [Reagent](https://reagent-project.github.io/) was used to build the user interface.
- [re-frame](https://github.com/day8/re-frame/) cleanly separated the views from the state and event management in the frontend.
- [shadow-cljs](http://shadow-cljs.org/) simplified the process of compiling ClojureScript.

## Running with Docker

If you have Docker installed, you can try out the application with:

```
docker-compose up
```

After a while, you should be able to access http://localhost:5000 and log in with username `user1` or `user2` and password `secret`.

## Contributing

I'm by no means an expert in any of the technologies used in this project, so feel free to share your thoughts, submit bug reports, pull requests, etc.
