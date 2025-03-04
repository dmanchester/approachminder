package com.dmanchester.approachminder

import com.dmanchester.approachminder.AIXM.{AIXMAirportHeliport, AIXMLongLat, AIXMRunway, AIXMRunwayDirection, AIXMValueWithUOM}
import io.dylemma.spac.xml.JavaxSource
import org.specs2.mutable.*

class AIXMSpec extends Specification {

  "parseAptXml" should {

    "correctly parse APT XML" in {

      val source = JavaxSource.fromInputStream { getClass.getResourceAsStream("resources/APT_AIXM_snippet.xml") }
      val (airportHeliports, runways, runwayDirections) = AIXM.parseAptXml(source)

      airportHeliports.length must beEqualTo(4)

      // AIXMAirportHeliport has one Option field: icaoId. Examine an instance with a Some (#0); examine an instance
      // with a None (#3).

      airportHeliports(0) must beEqualTo(
        AIXMAirportHeliport(
          "AH_0000001",
          "ADAK",
          "AH",
          Some("PADK"),
          AIXMLongLat(
            BigDecimal("-176.642482"),
            BigDecimal("51.883583")
          )
        )
      )

      airportHeliports(3) must beEqualTo(
        AIXMAirportHeliport(
          "AH_0000004",
          "AKIACHAK",
          "OTHER",
          None,
          AIXMLongLat(
            BigDecimal("-161.435077"),
            BigDecimal("60.907865")
          )
        )
      )

      runways.length must beEqualTo(11)

      // AIXMRunway has one Option field: widthStrip. Examine an instance with a Some (#0); examine an instance
      // with a None (#1).

      // FIXME Commented out cuz lengthStrip
//      runways(0) must beEqualTo(
//        AIXMRunway(
//          "RWY_0000001_1",
//          "AH_0000001",
//          "05/23",
//          Some(
//            AIXMValueWithUOM(200, "FT")
//          )
//        )
//      )
//
//      runways(1) must beEqualTo(
//        AIXMRunway(
//          "RWY_BASE_END_0000001_1",
//          "AH_0000001",
//          "05",
//          None
//        )
//      )

      runwayDirections.length must beEqualTo(7)

      // AIXMRunwayDirection has one Option field: runwayEnd. Examine an instance with a Some (#0); examine an instance
      // with a None (#6).

      runwayDirections(0) must beEqualTo(
        AIXMRunwayDirection(
          "RWY_DIRECTION_BASE_END_0000001_1",
          "RWY_BASE_END_0000001_1",
          Some(AIXMLongLat(
            BigDecimal("-176.657497"),
            BigDecimal("51.878339")
          ))
        )
      )

      runwayDirections(6) must beEqualTo(
        AIXMRunwayDirection(
          "RWY_DIRECTION_BASE_END_0000001_4",
          "RWY_BASE_END_0000001_4",
          None
        )
      )
    }
  }
}
