package offrec.database

import offrec.model.MessageDeleteQueue
import scalikejdbc._

import java.time.OffsetDateTime

object MessageDeleteQueueReader {
  def pendings(limit: Int)(implicit session: DBSession): List[MessageDeleteQueue] = {
    val mdq = MessageDeleteQueue.syntax("mdq")
    val now = OffsetDateTime.now()

    sql"""
      SELECT ${mdq.result.*}
      FROM ${MessageDeleteQueue.as(mdq)}
      WHERE ${mdq.status} = 0
        AND ${mdq.deletedAt} IS NULL
        AND ${mdq.createdAt} + INTERVAL ${mdq.ttl} SECOND <= ${now}
      ORDER BY ${mdq.createdAt} ASC
      LIMIT ${limit}
    """.map(MessageDeleteQueue(mdq.resultName)).list.apply()
  }
}
