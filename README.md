# Mercurius

This project is a toy application developed with the purpose of exploring [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) concepts in the context of functional programming languages, specifically [Clojure](https://clojure.org/).

[![Build Status](https://travis-ci.org/eeng/mercurius.svg?branch=master)](https://travis-ci.org/eeng/mercurius)

## Problem Domain

We'll try to mimic a very basic **cryptocurrency trading platform**, similar to the one you'd find on exchanges like Bitfinex and Binance.

Users will make deposits in different currencies, and then place orders to buy or sell some pair, like BTCUSD. Orders will be arranged in an order book showing the best ones (_bid_ and _ask_) on top, and it'll allow users to aggregate orders at different levels. At the same time, the order book will be continuously monitored in search of possible trades. Finally, when a trade is executed (bid price ≥ ask price), the system will notify the users involved and update the order book.

## Architecture Description

As it was previously mentioned, Clean Architecture principles were applied to structure the application in a loosely-coupled manner. Some terminology was slightly changed to match Domain-Driven Design names, which I prefer (i.e. _repositories_ instead of _gateways_ or _use cases_ instead of _interactors_), but the main ideas like the Dependency Rule were fully adopted. In addition to this, [Event Collaboration](https://martinfowler.com/eaaDev/EventCollaboration.html) was used throughout the system to integrate different components with a better separation of concerns.

The application was separated into a backend written in [Clojure](https://clojure.org/) and a frontend built with [ClojureScript](https://clojurescript.org/), with all the communication between them going through Web Sockets.

### Entities

The core of the architecture contains the domain **entities** (wallets, orders, trades, etc.) that, being Clojure a functional language, consists of a set of data structures and pure functions implementing the business logic. No side-effects nor dependencies on any framework are allowed in this layer, so its extremely easy to test.

### Use Cases

Surrounding the entities we have the **use cases** layer. They will be in charge of coordinating all the steps required to fulfill some command or query, e.g. fetching an entity from the database, calling the business logic, persisting the changes, and publishing the events. This layer does have side-effects, however, everything is done through protocols, with concrete implementations being injected from outer layers, so it doesn't depend on any database library or framework.

All use cases will be in the form of a high-order _builder_ function, i.e., a function that receives its dependencies and returns another function that finally executes the logic. This pattern allows us to treat all use cases uniformly, so we can apply cross-cutting concerns (like logging or transaction management) without repeating the same code everywhere. This is done in the _mediator_, which is the entry point to the domain.

### Adapters

Moving up outside the domain layers, we have the interface adapters. Here there are repository implementations, controllers, and background processes. Regarding the repositories, simple in-memory implementations are provided (making use of the excellent Clojure STM), although, because the use cases depend exclusively on protocols, it should be fairly simple to swap them with real database implementations later.

### Infrastructure

In the outer layer we have the "details" (as Uncle Bob likes to call them): The Web Server ([HTTP Kit](http://http-kit.github.io/)), the real-time communications framework ([Sente](https://github.com/ptaoussanis/sente)), and there is also a basic [core.async](https://github.com/clojure/core.async) implementation of a PubSub that it's used by the background processes to notify users when certain events occurred. It's based on protocols as well, so we can swap it with a Redis implementation, for example, if needed.

### Configuration

This is the place where the entire backend is assembled. All the components are built and wired together with [Integrant](https://github.com/weavejester/integrant). This allows us to start/stop the whole system from the REPL.

### UI

Finally, the frontend side consists of a SPA written in [ClojureScript](https://clojurescript.org/), with the help of the amazing [reagent](https://reagent-project.github.io/) and [re-frame](https://github.com/day8/re-frame/) libraries. To compile and build the release, [shadow-cljs](http://shadow-cljs.org/) was used.

## Code Structure

A deliberate attempt was made to try to organize the code in a way that [clearly expresses our architecture](https://blog.cleancoder.com/uncle-bob/2011/09/30/Screaming-Architecture.html).

At the first level, the code is subdivided by feature:

```sh
src/mercurius
├── core         # Functionality shared between all the features.
├── accounts     # Deals with user management, authentication, etc.
├── wallets      # Handles deposits, withdraws, transfers.
├── trading      # The core domain, order management and the trade finding algorithm reside here.
└── simulation   # Simulates multiple users placing orders and executing trades.
```

Within each of the above sub-folders, the code is subdivided by architectural layer. For example:

```sh
src/mercurius/trading
├── adapters          # Every namespace in this layer points downward, to the domain (Dependency Rule).
│   ├── presenters
│   ├── processes     # Background processes. They are kind of like controllers as they both call use cases.
│   └── repositories  # Repository concrete implementations.
├── domain
│   ├── entities
│   ├── repositories  # Repository protocols used by the use cases.
│   └── use_cases
└── presentation      # The reagent/re-frame UI code.
```

Other features have additional layers.

## Running with Docker

If you have Docker installed, you can try out the application with:

```
docker-compose up
```

After a while, you should be able to access http://localhost:5000 and log in with username `user1` or `user2` and password `secret`. I would recommend to open up two browser windows, one with each user, and place some orders so a trade is executed and you can observe the real-time comms feature.

## Contributing

I'm by no means an expert in any of the principles or technologies used in this project, so feel free to share your thoughts, submit bug reports, pull requests, etc.
