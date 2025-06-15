package offrec.model

import scalikejdbc._

import java.time.OffsetDateTime

case class MessageDeleteQueue(
    id: Long,
    guildId: String, // サーバーID
    channelId: String, // チャンネルID
    messageId: String, // メッセージID
    ttl: Int, // メッセージ削除までの時間（秒）
    status: Int, // 0: pending, 1: completed, 2: failed
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime,
    deletedAt: Option[OffsetDateTime]
)

object MessageDeleteQueue extends SQLSyntaxSupport[MessageDeleteQueue] {
  override def tableName: String = "message_delete_queue"

  def apply(rn: ResultName[MessageDeleteQueue])(rs: WrappedResultSet): MessageDeleteQueue = autoConstruct(rs, rn)
}
