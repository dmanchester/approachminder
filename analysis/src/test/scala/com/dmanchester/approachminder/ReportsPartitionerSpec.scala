package com.dmanchester.approachminder

import org.specs2.mutable.*

class ReportsPartitionerSpec extends Specification {

  "a partitioner should" should {

    val icao24 = "anICAO24Value"
    val category = Light
    val abcd = "abcd"
    val efgh = "efgh"
    def reportWithoutCallsign(timePosition: BigInt) = PositionReportIdentifiers(icao24, None, timePosition, Light)
    def reportWithCallsign(callsign: String, timePosition: BigInt) = PositionReportIdentifiers(icao24, Some(callsign), timePosition, Light)

    val withoutCallsignTime10 = reportWithoutCallsign(10)
    val withoutCallsignTime15 = reportWithoutCallsign(15)
    val withCallsignABCDTime20 = reportWithCallsign(abcd, 20)
    val withoutCallsignTime25 = reportWithoutCallsign(25)
    val withCallsignEFGHTime30 = reportWithCallsign(efgh, 30)
    val withCallsignEFGHTime35 = reportWithCallsign(efgh, 35)
    val withoutCallsignTime40 = reportWithoutCallsign(40)
    val withCallsignEFGHTime45 = reportWithCallsign(efgh, 45)

    "partition reports without callsigns on time gaps *larger than* what the partitioner was initialized with" in {
      val reports = Seq(withoutCallsignTime10, withoutCallsignTime15, withoutCallsignTime25)

      val partitionedReports = ReportsPartitioner.partition(reports, 9)

      partitionedReports mustEqual(Seq(
        (None, Seq(withoutCallsignTime10, withoutCallsignTime15)),
        (None, Seq(withoutCallsignTime25))
      ))
    }

    "partition reports without callsigns on time gaps *equal to* what the partitioner was initialized with" in {
      val reports = Seq(withoutCallsignTime10, withoutCallsignTime15, withoutCallsignTime25)

      val partitionedReports = ReportsPartitioner.partition(reports, 10)

      partitionedReports mustEqual(Seq(
        (None, Seq(withoutCallsignTime10, withoutCallsignTime15)),
        (None, Seq(withoutCallsignTime25))
      ))
    }

    "partition reports with callsigns on time gaps *larger than* what the partitioner was initialized with" in {
      val reports = Seq(withCallsignEFGHTime30, withCallsignEFGHTime35, withCallsignEFGHTime45)

      val partitionedReports = ReportsPartitioner.partition(reports, 9)

      partitionedReports mustEqual(Seq(
        (Some(efgh), Seq(withCallsignEFGHTime30, withCallsignEFGHTime35)),
        (Some(efgh), Seq(withCallsignEFGHTime45))
      ))
    }

    "partition reports with callsigns on time gaps *equal to* what the partitioner was initialized with" in {
      val reports = Seq(withCallsignEFGHTime30, withCallsignEFGHTime35, withCallsignEFGHTime45)

      val partitionedReports = ReportsPartitioner.partition(reports, 10)

      partitionedReports mustEqual(Seq(
        (Some(efgh), Seq(withCallsignEFGHTime30, withCallsignEFGHTime35)),
        (Some(efgh), Seq(withCallsignEFGHTime45))
      ))
    }

    "partition reports with callsigns when the callsign changes" in {
      val reports = Seq(withCallsignABCDTime20, withCallsignEFGHTime30, withCallsignEFGHTime35, withCallsignEFGHTime45)

      val partitionedReports = ReportsPartitioner.partition(reports, 1000)

      partitionedReports mustEqual(Seq(
        (Some(abcd), Seq(withCallsignABCDTime20)),
        (Some(efgh), Seq(withCallsignEFGHTime30, withCallsignEFGHTime35, withCallsignEFGHTime45))
      ))
    }

    "accommodate reports without callsigns before and after reports with the same callsign; but partition upon finding a different callsign" in {
      val reports = Seq(withoutCallsignTime10, withoutCallsignTime15, withCallsignABCDTime20, withoutCallsignTime25, withCallsignEFGHTime30, withCallsignEFGHTime35, withoutCallsignTime40)

      val partitionedReports = ReportsPartitioner.partition(reports, 10)

      partitionedReports mustEqual(Seq(
        (Some(abcd), Seq(withoutCallsignTime10, withoutCallsignTime15, withCallsignABCDTime20, withoutCallsignTime25)),
        (Some(efgh), Seq(withCallsignEFGHTime30, withCallsignEFGHTime35, withoutCallsignTime40))
      ))
    }
  }
}
