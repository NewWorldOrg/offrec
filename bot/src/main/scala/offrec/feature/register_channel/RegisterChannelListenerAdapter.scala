package offrec.feature.register_channel

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import offrec.database.{ChannelReader, ChannelWriter}
import offrec.logging.Logger
import scalikejdbc.DB

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

class RegisterChannelListenerAdapter extends ListenerAdapter with Logger {
  private val channelSelectCustomId = "offrec-select-channel"

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    super.onSlashCommandInteraction(event)

    if (event.getName != RegisterChannelListenerAdapter.slashCommandName) {
      // do nothing
    } else {
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
        .reply("チャンネルを選択してください:")
        .addComponents(ActionRow.of(selectMenu))
        .setEphemeral(true)
        .queue()
    }
  }

  override def onStringSelectInteraction(event: StringSelectInteractionEvent): Unit = {
    super.onStringSelectInteraction(event)

    if (event.getComponentId != channelSelectCustomId) {
      // do nothing
    } else {
      event.getValues.asScala.toList match {
        case Nil =>
          event.reply("チャンネルが選択されていません。もう一度やり直してください。").setEphemeral(true).queue()
        case selectedChannelId :: Nil =>
          val guildId = event.getGuild.getId
          val selectedChannel = event.getGuild.getTextChannelById(selectedChannelId)
          val channelName = if (selectedChannel != null) s"#${selectedChannel.getName}" else "不明なチャンネル"

          Try {
            DB.localTx { implicit session =>
              ChannelReader.findByGuildAndChannel(guildId, selectedChannelId) match {
                case Some(_) =>
                  logger.info(s"Channel already exists: guildId=$guildId, channelId=$selectedChannelId")
                  "既に登録済みのチャンネルです。"
                case None =>
                  ChannelWriter.create(guildId, selectedChannelId)
                  logger.info(s"Channel registered: guildId=$guildId, channelId=$selectedChannelId")
                  s"$channelName にキーが登録されました。"
              }
            }
          } match {
            case Success(message) =>
              event.reply(message).setEphemeral(true).queue()
            case Failure(exception) =>
              logger.error(s"Failed to register channel: ${exception.getMessage}", exception)
              event.reply("チャンネル登録に失敗しました。").setEphemeral(true).queue()
          }

        case _ =>
          event.reply("複数のチャンネルが選択されました。1つだけ選択してください。").setEphemeral(true).queue()
      }
    }
  }
}

object RegisterChannelListenerAdapter {
  val slashCommandName = "register-channel"
  val slashCommandDescription = "監視対象のチャンネルを登録します。"
}
