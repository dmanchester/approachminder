package com.dmanchester.approachminder

import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple3Semigroupal, catsSyntaxTuple4Semigroupal, catsSyntaxTuple5Semigroupal}
import io.dylemma.spac
import io.dylemma.spac.{ContextMatcher, Parser, Source, Splitter}
import io.dylemma.spac.xml.JavaxQName.javaxQNameAsQName
import io.dylemma.spac.xml.{JavaxSource, XmlEvent, XmlParser, XmlParserApplyOps, XmlSplitterApplyOps, XmlSplitterOps, elem}

import java.io.File
import javax.xml.namespace.QName

object AIXM {

  case class AIXMLongLat(longitude: BigDecimal, latitude: BigDecimal)
  case class AIXMAirportHeliport(gmlId: String, name: String, aixmType: String, icaoId: Option[String], longLat: AIXMLongLat)

  case class AIXMWidthStrip(value: Int, uom: String)
  case class AIXMRunway(gmlId: String, associatedAirportHeliportGmlId: String, designator: String, widthStrip: Option[AIXMWidthStrip])

  case class AIXMRunwayDirection(gmlId: String, usedRunwayGmlId: String, runwayEnd: Option[AIXMLongLat])

  private val GMLNamespaceUri = "http://www.opengis.net/gml/3.2"
  private val XlinkNamespaceUri = "http://www.w3.org/1999/xlink"

  private val gmlIdParser: XmlParser[String] = Splitter.xml(spac.xml.*).joinBy(XmlParser.attr(new QName(GMLNamespaceUri, "id"))).parseFirst

  private val airportHeliportTimeSliceMatcher: ContextMatcher[XmlEvent.ElemStart, Unit] = "AirportHeliport" \ "timeSlice" \ "AirportHeliportTimeSlice"

  private def convertLongLatString(longLat: String): AIXMLongLat = {
    val values = longLat.split(" ")
    AIXMLongLat(values(0).toDouble, values(1).toDouble)
  }
  /*TODO Keep non-implicit? implicit*/ private val airportParser: XmlParser[AIXMAirportHeliport] = Splitter.xml(spac.xml.* \ "AirportHeliport").joinBy((
    gmlIdParser,
    Splitter.xml(airportHeliportTimeSliceMatcher \ "name").text.parseFirst,
    Splitter.xml(airportHeliportTimeSliceMatcher \ "type").text.parseFirst,  // See https://aixm.aero/sites/default/files/imce/AIXM511HTML/AIXM/DataType_CodeAirportHeliportBaseType.html
    Splitter.xml(airportHeliportTimeSliceMatcher \ "locationIndicatorICAO").text.parseFirstOpt,
    Splitter.xml(airportHeliportTimeSliceMatcher \ "ARP" \ "ElevatedPoint" \ "pos").text.parseFirst.map(convertLongLatString)
  ).mapN(AIXMAirportHeliport.apply)).parseFirst

  private val runwayTimeSliceMatcher: ContextMatcher[XmlEvent.ElemStart, Unit] = "Runway" \ "timeSlice" \ "RunwayTimeSlice"

  private val gmlIdExtractor = "gml:id='([^']+)'".r.unanchored  // regex is externalized from extractGmlIdFromHref so the pattern needn't be recompiled on each invocation of that method

  private def extractGmlIdFromHref(href: String): String = {
    href match {
      case gmlIdExtractor(gmlId) => gmlId
    }
  }

  private val widthStripParser: XmlParser[Option[AIXMWidthStrip]] = Splitter.xml(runwayTimeSliceMatcher \ "widthStrip").joinBy(
    (XmlParser.forText.map(_.toInt), XmlParser.attr("uom")).mapN(AIXMWidthStrip.apply)
  ).parseFirstOpt

  private val runwayParser: XmlParser[AIXMRunway] = Splitter.xml(spac.xml.* \ "Runway").joinBy((
    gmlIdParser,
    Splitter.xml(runwayTimeSliceMatcher \ "associatedAirportHeliport").joinBy(XmlParser.attr(new QName(XlinkNamespaceUri, "href"))).parseFirst.map(extractGmlIdFromHref),
    Splitter.xml(runwayTimeSliceMatcher \ "designator").text.parseFirst,
    widthStripParser
  ).mapN(AIXMRunway.apply)).parseFirst

  private val runwayDirectionTimeSliceMatcher: ContextMatcher[XmlEvent.ElemStart, Unit] = "RunwayDirection" \ "timeSlice" \ "RunwayDirectionTimeSlice"

  private val runwayDirectionParser: XmlParser[AIXMRunwayDirection] = Splitter.xml(spac.xml.* \ "RunwayDirection").joinBy((
    gmlIdParser,
    Splitter.xml(runwayDirectionTimeSliceMatcher \ "usedRunway").joinBy(XmlParser.attr(new QName(XlinkNamespaceUri, "href"))).parseFirst.map(extractGmlIdFromHref),
    Splitter.xml(runwayDirectionTimeSliceMatcher \ "extension" \ "RunwayDirectionExtension" \ "ElevatedPoint" \ "pos").text.parseFirstOpt.map(_.flatMap { longLat =>
      Option.unless(longLat.isEmpty)(convertLongLatString(longLat))
    })
  ).mapN(AIXMRunwayDirection.apply)).parseFirst

  private sealed trait AIXMWrapper
  private case class AirportHeliportWrapper(airportHeliport: AIXMAirportHeliport) extends AIXMWrapper
  private case class RunwayWrapper(runway: AIXMRunway) extends AIXMWrapper
  private case class RunwayDirectionWrapper(runwayDirection: AIXMRunwayDirection) extends AIXMWrapper

  private val multiTypeParser: XmlParser[List[AIXMWrapper]] = Splitter.xml("SubscriberFile" \ "Member").joinBy(
    Parser.oneOf(
      airportParser.map(AirportHeliportWrapper.apply),
      runwayParser.map(RunwayWrapper.apply),
      runwayDirectionParser.map(RunwayDirectionWrapper.apply),
    ).wrapSafe.map(_.toOption)
  ).parseToList.map(_.flatten)

  def parseAptXml(source: Source[XmlEvent]): (Seq[AIXMAirportHeliport], Seq[AIXMRunway], Seq[AIXMRunwayDirection]) = {

    val parserOutput = multiTypeParser.parse(source)

    (
      parserOutput.collect { case AirportHeliportWrapper(airportHeliport) => airportHeliport },
      parserOutput.collect { case RunwayWrapper(runway) => runway },
      parserOutput.collect { case RunwayDirectionWrapper(runwayDirection) => runwayDirection }
    )
  }

  def parseAptXml(file: String): (Seq[AIXMAirportHeliport], Seq[AIXMRunway], Seq[AIXMRunwayDirection]) = {
    parseAptXml(JavaxSource.fromFile(new File(file)))
  }
}
