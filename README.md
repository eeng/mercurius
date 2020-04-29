# Mercurius

This project is a toy application developed with the purpose of exploring [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) concepts in the context of functional programming languages, specifically Clojure.

[![Build Status](https://travis-ci.org/eeng/mercurius.svg?branch=master)](https://travis-ci.org/eeng/mercurius)

## Problem Domain

We'll try to mimic a very basic **cryptocurrency trading platform**, similar to the one you'd find on exchanges like Bitfinex and Binance.

Users will make deposits in different currencies, and then place orders to buy or sell some pair, like BTCUSD. Orders will be arranged in an order book showing the best ones (_bid_ and _ask_) on top, and it'll allow users to aggregate orders at different levels. At the same time, the system will be continuously monitoring the order book trying to match orders in search of possible trades. Finally, when a trade is executed (bid price ≥ ask price), the system will notify the user.

## Architecture

As it was previously mentioned, Clean Architecture principles were applied to structure the application in a loosely-coupled manner. Some terminology was slightly changed to match Domain-Driven Design names, which I prefer (i.e. _repositotories_ instead of _gateways_ or _use cases_ instead of _interactors_), but the main ideas like the Dependency Rule were fully adopted. In addition to this, [Domain Events](https://martinfowler.com/eaaDev/DomainEvent.html) were used throughout the system to integrate different components without them depending on each other.

### Entities

The core of the architecture contains the domain **entities** (wallets, orders, trades, etc.) that, being Clojure a functional language, consists of a set data structures and pure functions implementing the business logic. No side-effects are allowed in this layer, so its extremely easy to test.

You may find the code for this layer in the `mercurius.*.domain.entities` namespaces.

### Use Cases

Surrounding the entities we have the **use cases** layer. They will be in charge of coordinating all the steps required to fulfill the use case, e.g. fetching an entity from the database, calling the business logic, persisting the changes, and publishing the events. This layer does have side-effects, however, everything is done through protocols, and concrete implementations are injected from the outer layer, so it doesn't depend on any specific database or framework.

All use cases will be in the form of a high-order _builder_ function, i.e., a function that receives its dependencies and returns another function that finally executes the logic. This pattern allows us to treat all use cases uniformly, so we can apply cross-cutting concerns (like logging or transaction management) without repeating the same code everywhere. This is done in the _mediator_, which is the entry point to the domain.

The code for this layer is the `mercurius.*.domain.use-cases.*` namespaces.

### Adapters

Moving up, we have the interface adapters. Here there are repository implementations, controllers, and background processes. Regarding the repositories, simple in-memory implementations are provided (making use of the excellent Clojure STM), although, because the use cases depend exclusively on protocols, it should be fairly simple to swap them with real database implementations.

Also in this layer, there is a core.async implementation of an event bus that it's used to broadcast the domain events.

The code is in the `mercurius.*.adapters.(repositories|controllers|processes).*` namespaces.

### Infrastructure

## Implementation

The system consists of a backend written in [Clojure](https://clojure.org/) and a frontend SPA built with [ClojureScript](https://clojurescript.org/). In addition, the following libraries assisted in the project:

- [Integrant](https://github.com/weavejester/integrant) was used to wire all the component's dependencies and manage the stateful resources.
- [core.async] TODO PubSub EventBus
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

I'm by no means an expert in any of the principles or technologies used in this project, so feel free to share your thoughts, submit bug reports, pull requests, etc.
