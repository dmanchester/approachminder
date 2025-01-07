package com.dmanchester.approachminder

import com.dmanchester.approachminder.AIXM.{AIXMAirportHeliport, AIXMLongLat, AIXMRunway, AIXMWidthStrip}
import io.dylemma.spac.xml.JavaxSource
import org.specs2.mutable.*

class AIXMSpec extends Specification {

  "parseAptXml" should {

    "correctly parse APT XML" in {

      val source = JavaxSource.fromInputStream { getClass.getResourceAsStream("resources/APT_AIXM_snippet.xml") }
      val (airportHeliports, runways) = AIXM.parseAptXml(source)

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

      runways.length must beEqualTo(9)
      // While the snippet includes four airports, it only includes the runway surfaces for the first three. Each runway
      // surface has three AIXMRunway instances. So, there are nine total.

      // AIXMRunway has one Option field: widthStrip. Examine an instance with a Some (#0); examine an instance
      // with a None (#1).

      runways(0) must beEqualTo(
        AIXMRunway(
          "RWY_0000001_1",
          "AH_0000001",
          "05/23",
          Some(
            AIXMWidthStrip(200, "FT")
          )
        )
      )

      runways(1) must beEqualTo(
        AIXMRunway(
          "RWY_BASE_END_0000001_1",
          "AH_0000001",
          "05",
          None
        )
      )
    }
  }
}
