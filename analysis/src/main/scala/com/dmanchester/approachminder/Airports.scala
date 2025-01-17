package com.dmanchester.approachminder

import com.dmanchester.approachminder.Utils.feetToMetersConverter

object Airports {

  // Data from https://nfdc.faa.gov/webContent/28DaySub/extra/03_Nov_2022_APT_CSV.zip >
  // APT_BASE.csv, APT_RWY.csv, APT_RWY_END.csv.

  object sfoData {
    val referencePoint = LongLat(-122.375416666667, 37.6188055555556)
    val runwayWidthInFeet = 200 // all runways are same width
    val thresholdCenter01L = LongLat(-122.3829285, 37.6078978611111)
    val thresholdCenter19R = LongLat(-122.370609416667, 37.6264813611111)
    val thresholdCenter01R = LongLat(-122.38104075, 37.6063298888889)
    val thresholdCenter19L = LongLat(-122.367110833333, 37.6273421944444)
    val thresholdCenter10L = LongLat(-122.393391861111, 37.6287387222222)
    val thresholdCenter28R = LongLat(-122.357141111111, 37.6135336111111)
    val thresholdCenter10R = LongLat(-122.393105444444, 37.6262911111111)
    val thresholdCenter28L = LongLat(-122.358349166667, 37.6117119444444)
  }

  val sfo = {

    val runwayWidthInMeters = feetToMetersConverter.convert(sfoData.runwayWidthInFeet)

    Airport("KSFO", sfoData.referencePoint, Seq(
      RunwaySurfaceTemplate(runwayWidthInMeters, "01L", sfoData.thresholdCenter01L, "19R", sfoData.thresholdCenter19R),
      RunwaySurfaceTemplate(runwayWidthInMeters, "01R", sfoData.thresholdCenter01R, "19L", sfoData.thresholdCenter19L),
      RunwaySurfaceTemplate(runwayWidthInMeters, "10L", sfoData.thresholdCenter10L, "28R", sfoData.thresholdCenter28R),
      RunwaySurfaceTemplate(runwayWidthInMeters, "10R", sfoData.thresholdCenter10R, "28L", sfoData.thresholdCenter28L)
    ))
  }

  object oakData {
    val referencePoint = LongLat(-122.221138888889, 37.72125)
    val runwayWidthInFeetAllBut15_33 = 150
    val runwayWidthInFeet15_33 = 75
    val thresholdCenter10L = LongLat(-122.222180027778, 37.7304684722222)
    val thresholdCenter28R = LongLat(-122.204703583333, 37.7248124166667)
    val thresholdCenter10R = LongLat(-122.225903027778, 37.7287069444444)
    val thresholdCenter28L = LongLat(-122.206009472222, 37.7222716666667)
    val thresholdCenter12 = LongLat(-122.242114805556, 37.7200626666667)
    val thresholdCenter30 = LongLat(-122.214256972222, 37.7014926388889)
    val thresholdCenter15 = LongLat(-122.222807666667, 37.7402915833333)
    val thresholdCenter33 = LongLat(-122.219673888889, 37.73136125)
  }

  val oak = {

    val runwayWidthInMetersAllBut15_33 = feetToMetersConverter.convert(oakData.runwayWidthInFeetAllBut15_33)
    val runwayWidthInMeters15_33 = feetToMetersConverter.convert(oakData.runwayWidthInFeet15_33)

    Airport("KOAK", oakData.referencePoint, Seq(
      RunwaySurfaceTemplate(runwayWidthInMetersAllBut15_33, "10L", oakData.thresholdCenter10L, "28R", oakData.thresholdCenter28R),
      RunwaySurfaceTemplate(runwayWidthInMetersAllBut15_33, "10R", oakData.thresholdCenter10R, "28L", oakData.thresholdCenter28L),
      RunwaySurfaceTemplate(runwayWidthInMetersAllBut15_33, "12", oakData.thresholdCenter12, "30", oakData.thresholdCenter30),
      RunwaySurfaceTemplate(runwayWidthInMeters15_33, "15", oakData.thresholdCenter15, "33", oakData.thresholdCenter33)
    ))
  }
}
