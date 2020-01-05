package vbakaev.app.config

final case class AppConfig(
    tcp: ServerConfig
)

final case class ServerConfig(
    interface: String,
    port: Int
)
