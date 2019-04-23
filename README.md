# Scooby

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://travis-ci.com/d2a4u/scooby.svg?branch=master)](https://travis-ci.com/d2a4u/scooby)

[Doobie's](https://tpolecat.github.io/doobie/) helpers micro library

## Getting Started

This micro library's purpose is to achieve better syntax when working with Doobie. Sometimes when
using Doobie, we can end up with lots of raw SQL, I want to be able to write code and worry about
SQL later.

## Examples

### Using mixins

```scala
for {
  _ <- Customer.insert(customer1)
  _ <- Customer.insert(customer2)
  result <- Customer.find[Customer.FindByAge, List](query)
} yield result

```

### Using types

```scala
for {
  _ <- createTable
  _ <- Insert[Customer](customer1, Customer.insert).run
  _ <- Insert[Customer](customer2, Customer.insert).run
  result <- Find[Customer.FindByAge, Customer, List](query, Customer.fba).run
} yield result
```

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT)
