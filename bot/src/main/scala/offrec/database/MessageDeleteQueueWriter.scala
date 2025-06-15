package offrec.database

import scalikejdbc._

import java.time.OffsetDateTime

object MessageDeleteQueueWriter {
  def create(guildId: String, channelId: String, messageId: String, ttl: Int)(implicit session: DBSession): Long = {
    val now = OffsetDateTime.now()

    sql"""
      INSERT INTO message_delete_queue (guild_id, channel_id, message_id, ttl, status, created_at, updated_at)
      VALUES (${guildId}, ${channelId}, ${messageId}, ${ttl}, 0, ${now}, ${now})
    """.updateAndReturnGeneratedKey.apply()
  }
}
