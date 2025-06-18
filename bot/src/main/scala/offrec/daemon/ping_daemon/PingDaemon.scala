package offrec.daemon.ping_daemon

import cats.effect.IO
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.{JDA, JDABuilder}
import offrec.feature.ping.PingListenerAdapter

object PingDaemon {
  def task(discordBotToken: String): IO[Unit] = {
    (for {
      jda <- preExecute(discordBotToken)
      _ <- execute(jda)
      _ <- postExecute(jda)
    } yield ()).foreverM
  }

  private def preExecute(discordBotToken: String): IO[JDA] = IO {
    JDABuilder
      .createDefault(discordBotToken)
      .enableIntents(GatewayIntent.MESSAGE_CONTENT)
      .enableIntents(GatewayIntent.GUILD_WEBHOOKS)
      .enableIntents(GatewayIntent.GUILD_MESSAGES)
      .addEventListeners(new PingListenerAdapter)
      .build()
  }

  private def execute(jda: JDA): IO[Boolean] = {
    IO {
      jda.awaitShutdown()
    }.guarantee(IO {
      val client = jda.getHttpClient
      client.connectionPool.evictAll()
      client.dispatcher.executorService.shutdown()
    })
  }

  private def postExecute(jda: JDA): IO[Unit] = IO {
    jda.shutdown()
  }
}
