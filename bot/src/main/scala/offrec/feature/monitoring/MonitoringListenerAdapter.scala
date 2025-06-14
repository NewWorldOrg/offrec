package offrec.feature.hub

import offrec.database.{HubMessageDeleteQueueWriter, HubMessageMappingReader, HubMessageMappingWriter}
import offrec.logging.Logger
import offrec.model.hub.{HubMessageDeleteQueue, HubMessageMapping}
import club.minnced.discord.webhook.external.JDAWebhookClient
import club.minnced.discord.webhook.send.{WebhookEmbedBuilder, WebhookMessageBuilder}
import net.dv8tion.jda.api.entities.Mentions
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent
import net.dv8tion.jda.api.events.message.{GenericMessageEvent, MessageDeleteEvent, MessageReceivedEvent, MessageUpdateEvent}
import net.dv8tion.jda.api.hooks.ListenerAdapter
import scalikejdbc.DB

import java.time.OffsetDateTime
import scala.jdk.CollectionConverters._
import scala.util.control.Exception._

class MonitoringListenerAdapter extends ListenerAdapter with Logger {
  override def onMessageReceived(event: MessageReceivedEvent): Unit = {
    // チャンネルを取得

    // 監視対象であるか？（DBに問い合わせ）

    // 監視対象の場合、削除キューにキューイングする
  }
}
