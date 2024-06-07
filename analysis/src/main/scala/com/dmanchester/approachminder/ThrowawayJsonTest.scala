package com.dmanchester.approachminder

import play.api.libs.json.Json

object ThrowawayJsonTest {

  def main(args: Array[String]): Unit = {

    val tbp0 = TimeBasedPosition(1662044820, 2, 3, 4, null)
    val tbp1 = TimeBasedPosition(1662044825, 5, 6, 7, null)

//    val json = Json.toJson(Seq(tbp0, tbp1))(IO.multiplePositionWithApproachSegmentWrites)
//    println(json)
  }
}
