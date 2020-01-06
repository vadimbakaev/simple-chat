# simple-chat
> Simple chat implemented with akka

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7720263821584b5cb0415c65048f575e)](https://www.codacy.com/manual/vadimbakaev/simple-chat?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=vadimbakaev/simple-chat&amp;utm_campaign=Badge_Grade)

## Run

To run all test and generate coverage report run:
```shell
sbt clean coverage test coverageReport coverageAggregate
```

To start application use:
```shell
sbt run
```

## Join to chat

To connect to chat run:
```shell
telnet localhost 9000
```

## Licensing

One really important part: Give your project a proper license. Here you should
state what the license is and how to find the text version of the license.
Something like:

"The code in this project is licensed under MIT license."