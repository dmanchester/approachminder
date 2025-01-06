package com.dmanchester.approachminder

import io.dylemma.spac.xml.JavaxSource
import org.specs2.mutable.*

class AIXMSpec extends Specification {

  "parseAptXml" should {

    "correctly parse APT XML" in {

      val source = JavaxSource.fromInputStream { getClass.getResourceAsStream("resources/APT_AIXM_truncated.xml") }
      val (airportHeliports, runways, runwayDirections) = AIXM.parseAptXml(source)

      // FIXME Obviously, move away from Seq.empty to true expected values
      airportHeliports must beEqualTo(Seq.empty)
      runways must beEqualTo(Seq.empty)
      runwayDirections must beEqualTo(Seq.empty)
    }
  }
}
