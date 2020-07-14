package com.streamxhub.flink.test

import java.util.Properties

import com.streamxhub.common.util.JsonUtils
import com.streamxhub.common.conf.ConfigConst._
import com.streamxhub.flink.core.request.MySQLRequest
import com.streamxhub.flink.core.source.MySQLSource
import com.streamxhub.flink.core.{FlinkStreaming, StreamingContext}
import org.apache.flink.streaming.api.scala._

object MySQLSourceApp extends FlinkStreaming {

  override def handler(context: StreamingContext): Unit = {
    implicit val prop = new Properties()
    prop.put(KEY_INSTANCE, "test")
    prop.put(KEY_JDBC_DRIVER, "com.mysql.jdbc.Driver")
    prop.put(KEY_JDBC_URL, "jdbc:mysql://localhost:3306/test?useSSL=false")
    prop.put(KEY_JDBC_USER, "root")
    prop.put(KEY_JDBC_PASSWORD, "123322242")
    prop.put("readOnly", "false")
    prop.put("idleTimeout", "20000")

    val mysqlSource = new MySQLSource(context)
    val ds = mysqlSource.getDataStream[Orders]({
      "select * from orders limit 10"
    }, r => {
      r.map(x => {
        val y = JsonUtils.read[Orders](x)
        println(y)
        y
      })
    })

    ds.print("ds----------->")

   val ds1 = MySQLRequest(ds).requestOrdered[Orders](
      x=>s"select * from orders where timestamp='${x.timestamp}'",
      x=> JsonUtils.read[Orders](x)
    )

    ds1.print()

  }

}

case class Orders(values: String, timestamp: Long)
