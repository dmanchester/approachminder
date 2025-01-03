package com.dmanchester.approachminder

import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple3Semigroupal, catsSyntaxTuple4Semigroupal}
import io.dylemma.spac
import io.dylemma.spac.{Parser, Splitter}
import io.dylemma.spac.xml.JavaxQName.javaxQNameAsQName
import io.dylemma.spac.xml.{JavaxSource, XmlEvent, XmlParser, XmlParserApplyOps, XmlSplitterApplyOps, XmlSplitterOps, elem, extractElemName}

import java.io.File
import javax.xml.namespace.QName

object ThrowawayAIXMParse {

  def main(args: Array[String]): Unit = {

    val GMLNamespaceUri = "http://www.opengis.net/gml/3.2"
    val XlinkNamespaceUri = "http://www.w3.org/1999/xlink"

    case class AIXMLongLat(longitude: BigDecimal, latitude: BigDecimal)
    case class AIXMAirportHeliport(gmlId: String, icaoId: Option[String], longLat: AIXMLongLat)

    val gmlIdParser: XmlParser[String] = Splitter.xml(spac.xml.*).joinBy(XmlParser.attr(new QName(GMLNamespaceUri, "id"))).parseFirst

    // TODO Rather than "spac.xml.*", for readability, make this specific to "AirportHeliport"? (But would that harm composability?)
    val icaoIdParser: XmlParser[Option[String]] = Splitter.xml(spac.xml.* \ "timeSlice" \ "AirportHeliportTimeSlice" \ "locationIndicatorICAO").text.parseFirstOpt

    def convertLongLatString(longLat: String): AIXMLongLat = {
      val values = longLat.split(" ")
      AIXMLongLat(values(0).toDouble, values(1).toDouble)
    }

    // TODO As with icaoIdParser and "spac.xml.*"
    val referencePointParser: XmlParser[AIXMLongLat] = Splitter.xml(spac.xml.* \ "timeSlice" \ "AirportHeliportTimeSlice" \ "ARP" \ "ElevatedPoint" \ "pos").text.parseFirst.map(convertLongLatString)

    /*TODO Keep non-implicit? implicit*/ val airportParser: XmlParser[AIXMAirportHeliport] = (
      gmlIdParser,
      icaoIdParser,
      referencePointParser
    ).mapN(AIXMAirportHeliport.apply)

    val airportsParser: XmlParser[List[AIXMAirportHeliport]] = Splitter.xml("SubscriberFile" \ "Member" \ "AirportHeliport").joinBy(airportParser).parseToList

//    val airports = airportsParser.parse(JavaxSource.fromInputStream { getClass.getResourceAsStream("resources/APT_AIXM_truncated.xml") })
//    println(airports)


    case class AIXMWidthStrip(value: Int, uom: String)
    case class AIXMRunway(gmlId: String, associatedAirportHeliportGmlId: String, designator: String, widthStrip: Option[AIXMWidthStrip])

    def extractGmlIdFromHref(href: String): String = {
      val regex = "gml:id='([^']+)'".r.unanchored  // FIXME Externalize for performance?

      href match {
        case regex(gmlId) => gmlId
      }
    }

    val associatedAirportHeliportGmlIdParser: XmlParser[String] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayTimeSlice" \ "associatedAirportHeliport").joinBy(XmlParser.attr(new QName(XlinkNamespaceUri, "href"))).parseFirst.map(extractGmlIdFromHref)

    val designatorParser: XmlParser[String] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayTimeSlice" \ "designator").text.parseFirst

    val widthStripParser: XmlParser[Option[AIXMWidthStrip]] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayTimeSlice" \ "widthStrip").joinBy(
      (XmlParser.forText.map(_.toInt), XmlParser.attr("uom")).mapN(AIXMWidthStrip.apply)
    ).parseFirstOpt

    val runwayParser: XmlParser[AIXMRunway] = (
      gmlIdParser,
      associatedAirportHeliportGmlIdParser,
      designatorParser,
      widthStripParser
    ).mapN(AIXMRunway.apply)

    val runwaysParser: XmlParser[List[AIXMRunway]] = Splitter.xml("SubscriberFile" \ "Member" \ "Runway").joinBy(runwayParser).parseToList

//    val runways = runwaysParser.parse(JavaxSource.fromInputStream { getClass.getResourceAsStream("resources/APT_AIXM_truncated.xml") })
//    println(runways)


    case class AIXMRunwayDirection(gmlId: String, usedRunwayGmlId: String, runwayEnd: Option[AIXMLongLat])

    val usedRunwayGmlIdParser: XmlParser[String] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayDirectionTimeSlice" \ "usedRunway").joinBy(XmlParser.attr(new QName(XlinkNamespaceUri, "href"))).parseFirst.map(extractGmlIdFromHref)
//    val usedRunwayGmlIdParser: XmlParser[Option[String]] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayDirectionTimeSlice" \ "usedRunway").joinBy(XmlParser.attr(new QName(XlinkNamespaceUri, "href"))).parseFirstOpt.map(_.map(extractGmlIdFromHref))

    val runwayEndParser: XmlParser[Option[AIXMLongLat]] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayDirectionTimeSlice" \ "extension" \ "RunwayDirectionExtension" \ "ElevatedPoint" \ "pos").text.parseFirstOpt.map(_.flatMap { longLat =>
      Option.unless(longLat.isEmpty)(convertLongLatString(longLat))
    })

    val runwayDirectionParser: XmlParser[AIXMRunwayDirection] = (
      gmlIdParser,
      usedRunwayGmlIdParser,
      runwayEndParser
    ).mapN(AIXMRunwayDirection.apply)

    sealed trait AIXMWrapper
    case class AirportHeliportWrapper(airportHeliport: AIXMAirportHeliport) extends AIXMWrapper
    case class RunwayWrapper(runway: AIXMRunway) extends AIXMWrapper
    case class RunwayDirectionWrapper(runwayDirection: AIXMRunwayDirection) extends AIXMWrapper


    val runwayDirectionsParser: XmlParser[List[AIXMRunwayDirection]] = Splitter.xml("SubscriberFile" \ "Member" \ "RunwayDirection").joinBy(runwayDirectionParser).parseToList

//    val runwayDirections = runwayDirectionsParser.parse(JavaxSource.fromInputStream { getClass.getResourceAsStream("resources/APT_AIXM_truncated.xml") })
//    println(runwayDirections)

    val threeTypeParser = Splitter.xml("SubscriberFile" \ "Member" \ extractElemName).map {
      case "AirportHeliport" => airportParser.map(airportHeliport => Some(AirportHeliportWrapper(airportHeliport)))
      case "Runway" => runwayParser.map(runway => Some(RunwayWrapper(runway)))
      case "RunwayDirection" => runwayDirectionParser.map(runwayDirection => Some(RunwayDirectionWrapper(runwayDirection)))
      case _ => Parser.pure(None)
    }.parseToList.map(_.flatten)

    val threeTypeParserAlt: Parser[XmlEvent, List[AIXMWrapper]] = Splitter.xml("SubscriberFile" \ "Member").joinBy(
      Parser.oneOf(
        Splitter.xml(spac.xml.* \ "AirportHeliport").joinBy(airportParser).parseFirst.map(airportHeliport => AirportHeliportWrapper(airportHeliport)),
        Splitter.xml(spac.xml.* \ "Runway").joinBy(runwayParser).parseFirst.map(runway => RunwayWrapper(runway)),
        Splitter.xml(spac.xml.* \ "RunwayDirection").joinBy(runwayDirectionParser).parseFirst.map(runwayDirection => RunwayDirectionWrapper(runwayDirection)),
      ).wrapSafe.map(_.toOption)
    ).parseToList.map(_.flatten)

    val t1 = System.nanoTime
//    val threeTypeParserOutput = threeTypeParser.parse(JavaxSource.fromInputStream { getClass.getResourceAsStream("resources/APT_AIXM_truncated.xml") })
    val threeTypeParserOutput = threeTypeParserAlt.parse(JavaxSource.fromFile(new File("/home/dan/APT_AIXM.xml")))
    val duration = (System.nanoTime - t1) / 1e9d

    val (airports, runways, runwayDirections) = (
      threeTypeParserOutput.collect { case AirportHeliportWrapper(x) => x },
      threeTypeParserOutput.collect { case RunwayWrapper(x) => x },
      threeTypeParserOutput.collect { case RunwayDirectionWrapper(x) => x }
    )

    println(s"${airports.length} airports, ${runways.length} runways, ${runwayDirections.length} runway directions")
    println(s"Elapsed time (seconds): ${duration}")
  }
}
