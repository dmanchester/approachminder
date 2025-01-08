package com.dmanchester.approachminder

import cats.implicits.{catsSyntaxTuple2Semigroupal, catsSyntaxTuple3Semigroupal, catsSyntaxTuple4Semigroupal, catsSyntaxTuple5Semigroupal}
import io.dylemma.spac
import io.dylemma.spac.{Parser, Source, Splitter}
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

  private val nameParser: XmlParser[String] = Splitter.xml(spac.xml.* \ "timeSlice" \ "AirportHeliportTimeSlice" \ "name").text.parseFirst

  // See https://aixm.aero/sites/default/files/imce/AIXM511HTML/AIXM/DataType_CodeAirportHeliportBaseType.html
  private val typeParser: XmlParser[String] = Splitter.xml(spac.xml.* \ "timeSlice" \ "AirportHeliportTimeSlice" \ "type").text.parseFirst

  // TODO Rather than "spac.xml.*", for readability, make this specific to "AirportHeliport"? (But would that harm composability?)
  private val icaoIdParser: XmlParser[Option[String]] = Splitter.xml(spac.xml.* \ "timeSlice" \ "AirportHeliportTimeSlice" \ "locationIndicatorICAO").text.parseFirstOpt

  private def convertLongLatString(longLat: String): AIXMLongLat = {
    val values = longLat.split(" ")
    AIXMLongLat(values(0).toDouble, values(1).toDouble)
  }

  // TODO As with icaoIdParser and "spac.xml.*"
  private val referencePointParser: XmlParser[AIXMLongLat] = Splitter.xml(spac.xml.* \ "timeSlice" \ "AirportHeliportTimeSlice" \ "ARP" \ "ElevatedPoint" \ "pos").text.parseFirst.map(convertLongLatString)

  /*TODO Keep non-implicit? implicit*/ private val airportParser: XmlParser[AIXMAirportHeliport] = Splitter.xml(spac.xml.* \ "AirportHeliport").joinBy((
    gmlIdParser,
    nameParser,
    typeParser,
    icaoIdParser,
    referencePointParser
  ).mapN(AIXMAirportHeliport.apply)).parseFirst

  private val gmlIdExtractor = "gml:id='([^']+)'".r.unanchored  // regex is externalized from extractGmlIdFromHref so the pattern needn't be recompiled on each invocation of that method

  private def extractGmlIdFromHref(href: String): String = {
    href match {
      case gmlIdExtractor(gmlId) => gmlId
    }
  }

  private val associatedAirportHeliportGmlIdParser: XmlParser[String] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayTimeSlice" \ "associatedAirportHeliport").joinBy(XmlParser.attr(new QName(XlinkNamespaceUri, "href"))).parseFirst.map(extractGmlIdFromHref)

  private val designatorParser: XmlParser[String] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayTimeSlice" \ "designator").text.parseFirst

  private val widthStripParser: XmlParser[Option[AIXMWidthStrip]] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayTimeSlice" \ "widthStrip").joinBy(
    (XmlParser.forText.map(_.toInt), XmlParser.attr("uom")).mapN(AIXMWidthStrip.apply)
  ).parseFirstOpt

  private val runwayParser: XmlParser[AIXMRunway] = Splitter.xml(spac.xml.* \ "Runway").joinBy((
    gmlIdParser,
    associatedAirportHeliportGmlIdParser,
    designatorParser,
    widthStripParser
  ).mapN(AIXMRunway.apply)).parseFirst

  private val usedRunwayGmlIdParser: XmlParser[String] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayDirectionTimeSlice" \ "usedRunway").joinBy(XmlParser.attr(new QName(XlinkNamespaceUri, "href"))).parseFirst.map(extractGmlIdFromHref)

  private val runwayEndParser: XmlParser[Option[AIXMLongLat]] = Splitter.xml(spac.xml.* \ "timeSlice" \ "RunwayDirectionTimeSlice" \ "extension" \ "RunwayDirectionExtension" \ "ElevatedPoint" \ "pos").text.parseFirstOpt.map(_.flatMap { longLat =>
    Option.unless(longLat.isEmpty)(convertLongLatString(longLat))
  })

  private val runwayDirectionParser: XmlParser[AIXMRunwayDirection] = Splitter.xml(spac.xml.* \ "RunwayDirection").joinBy((
    gmlIdParser,
    usedRunwayGmlIdParser,
    runwayEndParser
  ).mapN(AIXMRunwayDirection.apply)).parseFirst

  private sealed trait AIXMWrapper
  private case class AirportHeliportWrapper(airportHeliport: AIXMAirportHeliport) extends AIXMWrapper
  private case class RunwayWrapper(runway: AIXMRunway) extends AIXMWrapper
  private case class RunwayDirectionWrapper(runwayDirection: AIXMRunwayDirection) extends AIXMWrapper

  private val multiTypeParser: Parser[XmlEvent, List[AIXMWrapper]] = Splitter.xml("SubscriberFile" \ "Member").joinBy(
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
