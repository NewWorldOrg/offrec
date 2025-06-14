package offrec.feature.register_channel

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.{TextInput, TextInputStyle}
import net.dv8tion.jda.api.interactions.modals.Modal
import offrec.logging.Logger

class RegisterChannelListenerAdapter extends ListenerAdapter with Logger {
  private val modalCustomId = "offrec-register-channel"
  private val modalKey = "key"

  override def onSlashCommandInteraction(event: SlashCommandInteractionEvent): Unit = {
    if (event.getName != RegisterChannelListenerAdapter.slashCommandName) {
      // do nothing
    } else {
      // チャンネルを取得

      // 一覧から選択する

      val inputKeyPem = TextInput
        .create(modalKey, "PEM/OpenSSH", TextInputStyle.PARAGRAPH)
        .setRequired(true)
        .setMinLength(1)
        .build()

      val modal = Modal
        .create(modalCustomId, "Register Key")
        .addComponents(ActionRow.of(inputKeyPem))
        .build()

      event.replyModal(modal).queue()
    }
  }

  override def onModalInteraction(event: ModalInteractionEvent): Unit = {
    if (event.getModalId != modalCustomId) {
      // do nothing
    } else {

      // チャンネルの登録処理

    }
  }
}

object RegisterChannelListenerAdapter {
  val slashCommandName = "register-channel"
  val slashCommandDescription = ""
}
