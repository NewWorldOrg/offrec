package offrec.database

import offrec.model.MessageDeleteQueue
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

  def create(guildId: String, channelId: String, messageIds: List[String], ttl: Int)(implicit session: DBSession): Unit = {
    val now = OffsetDateTime.now()

    val column = MessageDeleteQueue.column

    val builder = BatchParamsBuilder {
      messageIds.map { messageId =>
        Seq(
          column.guildId -> guildId,
          column.channelId -> channelId,
          column.messageId -> messageId,
          column.ttl -> ttl,
          column.status -> 0,
          column.createdAt -> now,
          column.updatedAt -> now
        )
      }
    }

    withSQL {
      insert.into(MessageDeleteQueue).namedValues(builder.columnsAndPlaceholders: _*)
    }.batch(builder.batchParams: _*).apply()
  }

  def markCompleted(queueIds: List[Long])(implicit session: DBSession): Int = {
    val now = OffsetDateTime.now()

    sql"""
      UPDATE message_delete_queue
      SET status = 1, updated_at = ${now}
      WHERE id IN (${queueIds})
    """.update.apply()
  }

  def markFailed(queueIds: List[Long])(implicit session: DBSession): Int = {
    val now = OffsetDateTime.now()

    sql"""
      UPDATE message_delete_queue
      SET status = 2, updated_at = ${now}
      WHERE id IN (${queueIds})
    """.update.apply()
  }
}
