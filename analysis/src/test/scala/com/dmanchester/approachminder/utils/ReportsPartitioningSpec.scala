package com.dmanchester.approachminder.utils

import com.dmanchester.approachminder.testingtypes.CallsignAndTime
import org.specs2.mutable.*

class ReportsPartitioningSpec extends Specification {

  "partition()" should {

    val abcd = "abcd"
    val efgh = "efgh"
    def reportWithoutCallsign(timePosition: BigInt) = CallsignAndTime(None, timePosition)
    def reportWithCallsign(callsign: String, timePosition: BigInt) = CallsignAndTime(Some(callsign), timePosition)

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

      val partitions = ReportsPartitioning.partition(reports, 9)

      partitions mustEqual Seq(
        (None, Seq(withoutCallsignTime10, withoutCallsignTime15)),
        (None, Seq(withoutCallsignTime25))
      )
    }

    "partition reports without callsigns on time gaps *equal to* what the partitioner was initialized with" in {
      val reports = Seq(withoutCallsignTime10, withoutCallsignTime15, withoutCallsignTime25)

      val partitions = ReportsPartitioning.partition(reports, 10)

      partitions mustEqual Seq(
        (None, Seq(withoutCallsignTime10, withoutCallsignTime15)),
        (None, Seq(withoutCallsignTime25))
      )
    }

    "partition reports with callsigns on time gaps *larger than* what the partitioner was initialized with" in {
      val reports = Seq(withCallsignEFGHTime30, withCallsignEFGHTime35, withCallsignEFGHTime45)

      val partitions = ReportsPartitioning.partition(reports, 9)

      partitions mustEqual Seq(
        (Some(efgh), Seq(withCallsignEFGHTime30, withCallsignEFGHTime35)),
        (Some(efgh), Seq(withCallsignEFGHTime45))
      )
    }

    "partition reports with callsigns on time gaps *equal to* what the partitioner was initialized with" in {
      val reports = Seq(withCallsignEFGHTime30, withCallsignEFGHTime35, withCallsignEFGHTime45)

      val partitions = ReportsPartitioning.partition(reports, 10)

      partitions mustEqual Seq(
        (Some(efgh), Seq(withCallsignEFGHTime30, withCallsignEFGHTime35)),
        (Some(efgh), Seq(withCallsignEFGHTime45))
      )
    }

    "partition reports with callsigns when the callsign changes" in {
      val reports = Seq(withCallsignABCDTime20, withCallsignEFGHTime30, withCallsignEFGHTime35, withCallsignEFGHTime45)

      val partitions = ReportsPartitioning.partition(reports, 1000)

      partitions mustEqual Seq(
        (Some(abcd), Seq(withCallsignABCDTime20)),
        (Some(efgh), Seq(withCallsignEFGHTime30, withCallsignEFGHTime35, withCallsignEFGHTime45))
      )
    }

    "accommodate reports without callsigns before and after reports with the same callsign; but partition upon finding a different callsign" in {
      val reports = Seq(withoutCallsignTime10, withoutCallsignTime15, withCallsignABCDTime20, withoutCallsignTime25, withCallsignEFGHTime30, withCallsignEFGHTime35, withoutCallsignTime40)

      val partitions = ReportsPartitioning.partition(reports, 10)

      partitions mustEqual Seq(
        (Some(abcd), Seq(withoutCallsignTime10, withoutCallsignTime15, withCallsignABCDTime20, withoutCallsignTime25)),
        (Some(efgh), Seq(withCallsignEFGHTime30, withCallsignEFGHTime35, withoutCallsignTime40))
      )
    }
  }
}
