package offrec.feature.init_slash_command

import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import offrec.feature.register_channel.RegisterChannelListenerAdapter

class InitSlashCommandListenerAdapter extends ListenerAdapter {
  override def onReady(event: ReadyEvent): Unit = {
    super.onReady(event)

    val jda = event.getJDA

    jda
      .updateCommands()
      .addCommands(
        Commands.slash(RegisterChannelListenerAdapter.slashCommandName, RegisterChannelListenerAdapter.slashCommandDescription)
      )
      .queue()
  }

}
