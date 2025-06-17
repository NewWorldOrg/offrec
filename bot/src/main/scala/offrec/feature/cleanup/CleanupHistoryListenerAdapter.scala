package offrec.feature.cleanup

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import offrec.database.MessageDeleteQueueWriter
import offrec.logging.Logger
import scalikejdbc.DB

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class CleanupHistoryListenerAdapter extends ListenerAdapter with Logger {
  private val channelSelectCustomId = "offrec-cleanup-select-channel"

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    super.onSlashCommandInteraction(event)

    if (event.getName != CleanupHistoryListenerAdapter.slashCommandName) {
      return
    }

    val guild = event.getGuild
    if (guild == null) {
      event.reply("このコマンドはサーバー内でのみ使用できます。").setEphemeral(true).queue()
      return
    }

    val channels = guild.getTextChannels.asScala.toList
    if (channels.isEmpty) {
      event.reply("利用可能なテキストチャンネルが見つかりませんでした。").setEphemeral(true).queue()
      return
    }

    val selectMenuBuilder = StringSelectMenu
      .create(channelSelectCustomId)
      .setPlaceholder("チャンネルを選択してください")
      .setMinValues(1)
      .setMaxValues(1)

    channels.foreach { channel =>
      selectMenuBuilder.addOption(
        s"#${channel.getName}",
        channel.getId,
        channel.getTopic
      )
    }

    val selectMenu = selectMenuBuilder.build()
    event
      .reply("履歴を削除するチャンネルを選択してください:")
      .addComponents(ActionRow.of(selectMenu))
      .setEphemeral(true)
      .queue()
  }

  override def onStringSelectInteraction(event: StringSelectInteractionEvent): Unit = {
    super.onStringSelectInteraction(event)

    if (event.getComponentId != channelSelectCustomId) {
      return
    }

    event.getValues.asScala.toList match {
      case Nil =>
        event.reply("チャンネルが選択されていません。もう一度やり直してください。").setEphemeral(true).queue()
      case selectedChannelId :: Nil =>
        val guildId = event.getGuild.getId
        val selectedChannel = event.getGuild.getTextChannelById(selectedChannelId)
        val channelName = if (selectedChannel != null) s"#${selectedChannel.getName}" else "不明なチャンネル"

        if (selectedChannel == null) {
          event.reply("チャンネルが見つかりませんでした。").setEphemeral(true).queue()
          return
        }

        val messages = selectedChannel.getHistory.retrievePast(CleanupHistoryListenerAdapter.defaultLimit).complete().asScala.toList
        var queued = 0

        messages.foreach { msg =>
          Try {
            DB.localTx { implicit session =>
              MessageDeleteQueueWriter.create(guildId, selectedChannelId, msg.getId, 0)
            }
          } match {
            case Success(_) => queued += 1
            case Failure(exception) =>
              logger.error(
                "Failed to queue history message for deletion",
                kv("messageId", msg.getId),
                kv("guildId", guildId),
                kv("channelId", selectedChannelId),
                exception
              )
          }
        }

        logger.info(
          "Cleanup history queued",
          kv("guildId", guildId),
          kv("channelId", selectedChannelId),
          kv("count", queued)
        )
        event
          .reply(s"$channelName の最新 ${queued} 件のメッセージを削除対象に登録しました。")
          .setEphemeral(true)
          .queue()
      case _ =>
        event.reply("複数のチャンネルが選択されました。1つだけ選択してください。").setEphemeral(true).queue()
    }
  }
}

object CleanupHistoryListenerAdapter {
  val slashCommandName = "cleanup-history"
  val slashCommandDescription = "過去のメッセージを一括で削除キューに登録します。"
  val defaultLimit: Int = 100
}

