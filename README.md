# Mercurius

[WIP]

[![Build Status](https://travis-ci.org/eeng/mercurius.svg?branch=master)](https://travis-ci.org/eeng/mercurius)

## TODO README

- Handle market orders
- Store some trx-id in deposits/withdraws related to transfers
- Simulator, place orders around current price
- Add simulation test invariant: Users without pending orders should have reserved 0 in their wallets
- Change the in-memory-wallet-repo to be indexed by [user-id, currency]
