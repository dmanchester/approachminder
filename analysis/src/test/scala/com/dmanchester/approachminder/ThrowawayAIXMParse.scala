package com.dmanchester.approachminder

import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple3Semigroupal, catsSyntaxTuple4Semigroupal}
import io.dylemma.spac
import io.dylemma.spac.Splitter
import io.dylemma.spac.xml.JavaxQName.javaxQNameAsQName
import io.dylemma.spac.xml.{JavaxSource, XmlParser, XmlParserApplyOps, XmlSplitterApplyOps, XmlSplitterOps, elem}

import javax.xml.namespace.QName

object ThrowawayAIXMParse {

  def main(args: Array[String]): Unit = {

    val GMLNamespaceUri = "http://www.opengis.net/gml/3.2"
    val XlinkNamespaceUri = "http://www.w3.org/1999/xlink"

    case class AIXMLongLat(longitude: BigDecimal, latitude: BigDecimal)
    case class AIXMAirportHeliport(gmlId: String, icaoId: String, longLat: AIXMLongLat)

    val gmlIdParser: XmlParser[String] = Splitter.xml(spac.xml.*).joinBy(XmlParser.attr(new QName(GMLNamespaceUri, "id"))).parseFirst

    // TODO Rather than "spac.xml.*", for readability, make this specific to "AirportHeliport"? (But would that harm composability?)
    val icaoIdParser: XmlParser[String] = Splitter.xml(spac.xml.* \ "timeSlice" \ "AirportHeliportTimeSlice" \ "locationIndicatorICAO").text.parseFirst

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

    val airports = airportsParser.parse(JavaxSource.fromInputStream { getClass.getResourceAsStream("resources/APT_AIXM_truncated.xml") })
    println(airports)


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


    case class AIXMRunwayDirection(gmlId: String, usedRunwayGmlId: String, runwayEnd: AIXMLongLat)

    val usedRunwayGmlIdParser: XmlParser[String] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayDirectionTimeSlice" \ "usedRunway").joinBy(XmlParser.attr(new QName(XlinkNamespaceUri, "href"))).parseFirst.map(extractGmlIdFromHref)

    val runwayEndParser: XmlParser[AIXMLongLat] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayDirectionTimeSlice" \ "extension" \ "RunwayDirectionExtension" \ "ElevatedPoint" \ "pos").text.parseFirst.map(convertLongLatString)

    val runwayDirectionParser: XmlParser[AIXMRunwayDirection] = (
      gmlIdParser,
      usedRunwayGmlIdParser,
      runwayEndParser
    ).mapN(AIXMRunwayDirection.apply)

    val runwayDirectionsParser: XmlParser[List[AIXMRunwayDirection]] = Splitter.xml("SubscriberFile" \ "Member" \ "RunwayDirection").joinBy(runwayDirectionParser).parseToList

    val runwayDirections = runwayDirectionsParser.parse(JavaxSource.fromInputStream { getClass.getResourceAsStream("resources/APT_AIXM_truncated.xml") })
    println(runwayDirections)

  }
}
