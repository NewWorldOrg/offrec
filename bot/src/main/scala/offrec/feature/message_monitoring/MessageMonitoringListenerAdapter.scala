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
            logger.info(s"Message queued for deletion: queueId=$queueId, messageId=$messageId, guildId=$guildId, channelId=$channelId, ttl=$ttl")
          case Failure(exception) =>
            logger.error(s"Failed to queue message for deletion: messageId=$messageId, guildId=$guildId, channelId=$channelId", exception)
        }
      case Success(None) =>
        // チャンネルが監視対象でない場合は何もしない
        
      case Failure(exception) =>
        logger.error(s"Failed to check if channel is monitored: guildId=$guildId, channelId=$channelId", exception)
    }
  }
}

object MessageMonitoringListenerAdapter {
  val defaultTtl: Int = 86400 // 1day
}
