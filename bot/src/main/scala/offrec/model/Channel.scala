package offrec.model

import scalikejdbc._

import java.time.OffsetDateTime

case class Channel(
    id: Long,
    guildId: String, // サーバーID
    channelId: String, // チャンネルID
    createdAt: OffsetDateTime,
    updatedAt: OffsetDateTime,
    deletedAt: Option[OffsetDateTime]
)

object Channel extends SQLSyntaxSupport[Channel] {
  override def tableName: String = "message_delete_queue"

  def apply(rn: ResultName[Channel])(rs: WrappedResultSet): Channel = autoConstruct(rs, rn)
}
