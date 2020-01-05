# simple-chat
> Simple chat implemented with akka

## Run

To run all test and generate coverage report run:
```sbtshell
clean coverage test it:test coverageReport coverageAggregate
```

To start application use:
```sbtshell
run
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
