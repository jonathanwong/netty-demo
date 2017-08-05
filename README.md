Some examples with the [netty](https://netty.io) framework.

An example where one Netty server communicates to another Netty server.  IntegerClient passes an integer to PrimaryServer.  PrimaryServer then sends this integer to SecondaryServer.  SecondaryServer multiplies the integer by a factor of two and PrimaryServer receives the message.  Run the servers in this order:

`Run SecondaryServerBootstrap`

`Run PrimaryServerBootstrap`

`Run IntegerClient`



An example to show how ClientAuth works in Netty.  First

`Run CryptoUtils`

This will create _root.cert.pem_, _root.key.pem_, _client.jks_, _server.jks_, and _trustStore.jks_.

Then run:

`Run SslServerBootstrap`

`Run SslClientBootstrap`

