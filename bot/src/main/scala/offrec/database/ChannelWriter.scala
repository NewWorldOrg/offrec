package offrec.database

import scalikejdbc._

import java.time.OffsetDateTime

object ChannelWriter {
  def create(guildId: String, channelId: String)(implicit session: DBSession): Long = {
    val now = OffsetDateTime.now()

    sql"""
      INSERT INTO channel (guild_id, channel_id, created_at, updated_at)
      VALUES (${guildId}, ${channelId}, ${now}, ${now})
    """.updateAndReturnGeneratedKey.apply()
  }
}
