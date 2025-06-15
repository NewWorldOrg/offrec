package offrec.feature.message_monitoring

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import offrec.database.{ChannelReader, MessageDeleteQueueWriter}
import offrec.logging.Logger
import scalikejdbc.DB

import scala.util.{Failure, Success, Try}

class MessageMonitoringListenerAdapter extends ListenerAdapter with Logger {

  override def onMessageReceived(event: MessageReceivedEvent): Unit = {
    if (!event.isFromGuild) {
      return
    }

    val guildId = event.getGuild.getId
    val channelId = event.getChannel.getId
    val messageId = event.getMessageId

    Try {
      DB.readOnly { implicit session =>
        ChannelReader.findByGuildAndChannel(guildId, channelId)
      }
    } match {
      case Success(Some(_)) =>
        val ttl = MessageMonitoringListenerAdapter.defaultTtl

        Try {
          DB.localTx { implicit session =>
            MessageDeleteQueueWriter.create(guildId, channelId, messageId, ttl)
          }
        } match {
          case Success(queueId) =>
            logger.info(
              "Message queued for deletion",
              kv("queueId", queueId),
              kv("messageId", messageId),
              kv("guildId", guildId),
              kv("channelId", channelId),
              kv("ttl", ttl)
            )
          case Failure(exception) =>
            logger.error(
              "Failed to queue message for deletion",
              kv("messageId", messageId),
              kv("guildId", guildId),
              kv("channelId", channelId),
              exception
            )
        }
      case Success(None) =>
      // チャンネルが監視対象でない場合は何もしない

      case Failure(exception) =>
        logger.error("Failed to check if channel is monitored", kv("guildId", guildId), kv("channelId", channelId), exception)
    }
  }
}

object MessageMonitoringListenerAdapter {
  val defaultTtl: Int = 86400 // 1day
}
