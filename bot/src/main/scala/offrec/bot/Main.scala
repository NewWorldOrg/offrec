package offrec.bot

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxParallelSequence_
import com.typesafe.config.ConfigFactory
import offrec.daemon.message_delete_daemon.MessageDeleteDamon
import offrec.daemon.ping_daemon.PingDaemon
import offrec.daemon.register_channel_command_daemon.RegisterChannelCommandDaemon
import offrec.logging.Logger
import scalikejdbc.config.DBs

object Main extends IOApp with Logger {
  override def run(args: List[String]): IO[ExitCode] = {
    val loadConfig = IO {
      val config = ConfigFactory.load()
      config.getString("discord.token")
    }
    val setupDB = IO {
      DBs.setupAll()
    }
    val closeDB = IO {
      DBs.closeAll()
    }

    (for {
      discordBotToken <- loadConfig
      _ <- setupDB
      registerChannelCommandDaemonFiber <- RegisterChannelCommandDaemon.task(discordBotToken).start
      messageDeleteDamonFiber <- MessageDeleteDamon.task(discordBotToken).start
      pingDaemonFiber <- PingDaemon.task(discordBotToken).start
      _ <- List(
        registerChannelCommandDaemonFiber.join.guarantee(IO(logger.info("Shutting down register channel command daemon..."))),
        messageDeleteDamonFiber.join.guarantee(IO(logger.info("Shutting down message delete daemon..."))),
        pingDaemonFiber.join.guarantee(IO(logger.info("Shutting down ping daemon...")))
      ).parSequence_
    } yield ()).guarantee(closeDB).as(ExitCode.Success)
  }
}
