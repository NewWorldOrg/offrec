package offrec.daemon.register_channel_command_daemon

import cats.effect.IO
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.{JDA, JDABuilder}
import offrec.feature.cleanup.CleanupHistoryListenerAdapter
import offrec.feature.init_slash_command.InitSlashCommandListenerAdapter
import offrec.feature.message_monitoring.MessageMonitoringListenerAdapter
import offrec.feature.register_channel.RegisterChannelListenerAdapter

object RegisterChannelCommandDaemon {
  def task(discordBotToken: String): IO[Unit] = {
    (for {
      jda <- preExecute(discordBotToken)
      _ <- execute(jda)
      _ <- postExecute(jda)
    } yield ()).foreverM
  }

  private def preExecute(discordBotToken: String): IO[JDA] = IO.blocking {
    JDABuilder
      .createDefault(discordBotToken)
      .enableIntents(GatewayIntent.MESSAGE_CONTENT)
      .enableIntents(GatewayIntent.GUILD_MESSAGES)
      .addEventListeners(new RegisterChannelListenerAdapter)
      .addEventListeners(new MessageMonitoringListenerAdapter)
      .addEventListeners(new CleanupHistoryListenerAdapter)
      .addEventListeners(new InitSlashCommandListenerAdapter) // 面倒なのでここで初期化する
      .build()
  }

  private def execute(jda: JDA): IO[Boolean] = {
    IO.blocking {
      jda.awaitShutdown()
    }.guarantee(IO.blocking {
      val client = jda.getHttpClient
      client.connectionPool.evictAll()
      client.dispatcher.executorService.shutdown()
    })
  }

  private def postExecute(jda: JDA): IO[Unit] = IO.blocking {
    jda.shutdown()
  }
}
