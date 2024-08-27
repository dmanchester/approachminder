package com.dmanchester.approachminder

import org.specs2.mutable._

class TimeOrderedDataSpec extends Specification {

  private val time06 = TimeWithSomeText(6)
  private val time10 = TimeWithSomeText(10)
  private val time15Winner = TimeWithSomeText(15, "winner")
  private val time15Loser1 = TimeWithSomeText(15, "loser 1")
  private val time15Loser2 = TimeWithSomeText(15, "loser 2")
  private val time18 = TimeWithSomeText(18)
  private val time21 = TimeWithSomeText(21)
  private val time27 = TimeWithSomeText(27)

  "create" should {

    "handle an empty Seq" in {
      val times = Seq.empty[TimeWithSomeText]
      val timeOrderedData = TimeOrderedData.create(times)
      timeOrderedData.seq must empty
    }

    "demonstrate at compile time it's retaining the subtypes of Seq and HasTime it receives" in {
      val times: IndexedSeq[TimeWithSomeText] = IndexedSeq(time10, time21)
      val timeOrderedData: TimeOrderedData[TimeWithSomeText, IndexedSeq] = TimeOrderedData.create(times)
      true must beTrue  // nothing to do at runtime
    }

    "time-order the data" in {
      val times = Seq(time18, time10, time21)
      val timeOrderedData = TimeOrderedData.create(times)
      timeOrderedData.seq mustEqual IndexedSeq(time10, time18, time21)
    }

    "resolve time-conflicting elements, picking the positionally last element with a given time as the winner" in {
      val times = Seq(time21, time15Loser1, time10, time15Loser2, time15Winner, time18)
      val timeOrderedData = TimeOrderedData.create(times)
      timeOrderedData.seq mustEqual Seq(time10, time15Winner, time18, time21)
    }
  }

  "splitOnGaps" should {

    "handle an empty Seq" in {
      val times = Seq.empty[TimeWithSomeText]
      val timeOrderedData = TimeOrderedData.create(times)
      val seqTimeOrderedData = timeOrderedData.splitOnGaps(1)
      seqTimeOrderedData.length mustEqual 1
      seqTimeOrderedData.head.seq must empty
    }

    "split on gaps while retaining Seq subtype" in {
      val times = IndexedSeq(time18, time06, time10, time27, time21, time15Winner)
      val timeOrderedData = TimeOrderedData.create(times)
      val seqTimeOrderedData = timeOrderedData.splitOnGaps(5)

      val timeOrderedDataAfterSplit0: TimeOrderedData[TimeWithSomeText, IndexedSeq] = seqTimeOrderedData(0)  // using explicit type to trigger a compile-time error if subtypes not retained
      timeOrderedDataAfterSplit0.seq mustEqual Seq(time06, time10)

      val timeOrderedDataAfterSplit1 = seqTimeOrderedData(1)
      timeOrderedDataAfterSplit1.seq mustEqual Seq(time15Winner, time18, time21)

      val timeOrderedDataAfterSplit2 = seqTimeOrderedData(2)
      timeOrderedDataAfterSplit2.seq mustEqual Seq(time27)
    }
  }
}