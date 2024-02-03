package com.dmanchester.approachminder

class RunwaySurfaceTemplate private(val widthInMeters: Double, val threshold0Name: String, val threshold0Center: HasLongLat, val threshold1Name: String, val threshold1Center: HasLongLat)

object RunwaySurfaceTemplate {
  def apply(widthInMeters: Double, threshold0Name: String, threshold0Center: HasLongLat, threshold1Name: String, threshold1Center: HasLongLat): RunwaySurfaceTemplate = new RunwaySurfaceTemplate(widthInMeters, threshold0Name, threshold0Center, threshold1Name, threshold1Center)
}