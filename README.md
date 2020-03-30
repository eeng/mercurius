# Mercurius

[WIP]

[![Build Status](https://travis-ci.org/eeng/mercurius.svg?branch=master)](https://travis-ci.org/eeng/mercurius)

## TODO README

- Handle market orders
- Trigger events, like when a trade is made. Then remove the "trade made" logging.
- Think about aggregate's invariants and transactions.
- Store some trx-id in deposits/withdraws related to transfers
- Simulator, place orders around current price
- Add simulation test invariant: Users without pending orders should have reserved 0 in their wallets
- Transaction middleware
- In the event handlers pass through the mediator so those use cases get logged and retried as well
