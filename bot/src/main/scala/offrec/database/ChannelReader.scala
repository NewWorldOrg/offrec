package offrec.database

import offrec.model.Channel
import scalikejdbc._

object ChannelReader {
  def findByGuildAndChannel(guildId: String, channelId: String)(implicit session: DBSession): Option[Channel] = {
    val c = Channel.syntax("c")
    
    sql"""
      SELECT ${c.result.*}
      FROM ${Channel.as(c)}
      WHERE ${c.guildId} = ${guildId}
        AND ${c.channelId} = ${channelId}
        AND ${c.deletedAt} IS NULL
    """.map(Channel(c.resultName)).single.apply()
  }
}