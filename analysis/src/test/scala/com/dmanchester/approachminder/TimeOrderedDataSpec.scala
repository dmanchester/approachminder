package com.dmanchester.approachminder

import org.specs2.mutable._

class TimeOrderedDataSpec extends Specification {

  "create" should {

    val time10 = TimeWithSomeText(10)
    val time20 = TimeWithSomeText(20)
    val time21 = TimeWithSomeText(21)
    val time15Winner = TimeWithSomeText(15, "winner")
    val time15Loser1 = TimeWithSomeText(15, "loser 1")
    val time15Loser2 = TimeWithSomeText(15, "loser 2")

    "demonstrate at compile time it's retaining the subtypes of Seq and HasTime it receives" in {
      val times: IndexedSeq[TimeWithSomeText] = IndexedSeq(time10, time21)
      val timeOrderedData: TimeOrderedData[TimeWithSomeText, IndexedSeq[TimeWithSomeText]] = TimeOrderedData.create(times)
      true must beTrue  // nothing to do at runtime
    }

    "time-order the data" in {
      val times = Seq(time20, time10, time21)
      val timeOrderedData = TimeOrderedData.create(times)
      timeOrderedData.seq mustEqual IndexedSeq(time10, time20, time21)
    }

    "resolve time-conflicting elements, picking the positionally last element with a given time as the winner" in {
      val times = Seq(time21, time15Loser1, time10, time15Loser2, time15Winner, time20)
      val timeOrderedData = TimeOrderedData.create(times)
      timeOrderedData.seq mustEqual Seq(time10, time15Winner, time20, time21)
    }
  }
}