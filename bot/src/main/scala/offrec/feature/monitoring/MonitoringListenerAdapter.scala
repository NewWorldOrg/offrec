package offrec.feature.monitoring

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import offrec.logging.Logger

class MonitoringListenerAdapter extends ListenerAdapter with Logger {
  override def onMessageReceived(event: MessageReceivedEvent): Unit = {
    // チャンネルを取得

    // 監視対象であるか？（DBに問い合わせ）

    // 監視対象の場合、削除キューにキューイングする
  }
}
