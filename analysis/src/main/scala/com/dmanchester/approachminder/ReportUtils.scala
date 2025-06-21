package com.dmanchester.approachminder

object ReportUtils {

  /**
   * Partition reports from a single aircraft on a time gap or callsign change.
   *
   * @param reports The reports to partition.
   * @param timeGapSecs The time gap in seconds.
   * @tparam R The reports' type.
   * @return The partitioned reports.
   */
  def partition[R <: HasPositionReportIdentifiers](reports: Iterable[R], timeGapSecs: Int): Seq[(Option[String], Seq[R])] = {
    reports.foldLeft(PartitionerState.initial[R](timeGapSecs)) { (partitionerState, report) =>
      partitionerState.processReport(report)
    }.partitions
  }
}