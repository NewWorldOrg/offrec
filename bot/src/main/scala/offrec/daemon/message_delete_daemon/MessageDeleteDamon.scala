package offrec.daemon.message_delete_daemon

import cats.effect.IO
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.{JDA, JDABuilder}
import offrec.database.{MessageDeleteQueueReader, MessageDeleteQueueWriter}
import offrec.logging.Logger
import scalikejdbc.DB

import scala.concurrent.duration._
import scala.util.control.Exception.allCatch

object MessageDeleteDamon extends Logger {
  def task(discordBotToken: String): IO[Unit] = {
    (for {
      jda <- preExecute(discordBotToken)
      _ <- execute(jda).handleErrorWith { e =>
        IO {
          logger.error("Error occurred during message deletion process", e)
        }
      }.foreverM
      _ <- postExecute(jda)
    } yield ()).foreverM
  }

  private def preExecute(discordBotToken: String): IO[JDA] = IO.blocking {
    JDABuilder.createDefault(discordBotToken).build().awaitReady()
  }

  private def execute(jda: JDA): IO[Unit] = {
    val deleteTask = IO.blocking {
      val rows = DB.readOnly { implicit s => MessageDeleteQueueReader.pendings(limit = 100) }

      if (rows.isEmpty) {
        logger.info("No pending messages to delete")
      } else {
        logger.info("Deletion target messages retrieved", kv("count", rows.size))

        val groupedRows = rows
          .groupBy(_.guildId)
          .map { case (guildId, queues) => (guildId, queues.groupBy(_.channelId)) }

        groupedRows.foreach { case (guildId, channels) =>
          channels.foreach { case (channelId, queues) =>
            val messageIds = queues.map(_.messageId)

            for {
              guild <- Option(jda.getGuildById(guildId))
              channel <- Option(guild.getChannelById(classOf[TextChannel], channelId))
            } yield {
              messageIds.foreach { messageId =>
                allCatch.either {
                  channel.deleteMessageById(messageId).complete()
                } match {
                  case Left(e) =>
                    logger.error("Failed to delete messages", kv("guildId", guildId), kv("channelId", channelId), kv("messageId", messageId), e)
                    DB.localTx { implicit s =>
                      MessageDeleteQueueWriter.markFailed(messageId)
                    }
                  case Right(_) =>
                    logger.info("Messages deleted", kv("guildId", guildId), kv("channelId", channelId), kv("messageId", messageId))
                    DB.localTx { implicit s =>
                      MessageDeleteQueueWriter.markCompleted(messageId)
                    }
                }
              }
            }
          }
        }
      }
    }
    val waitTask = IO.sleep(15.seconds)

    deleteTask *> waitTask

  }

  private def postExecute(jda: JDA): IO[Unit] = IO.blocking {
    jda.shutdown()
  }
}
