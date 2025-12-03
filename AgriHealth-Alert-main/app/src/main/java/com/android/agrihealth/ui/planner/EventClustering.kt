package com.android.agrihealth.ui.planner

import com.android.agrihealth.data.model.report.Report
import java.time.LocalTime

/**
 * Coded using chatGPT 5.0 data class representing a tha layout for a ReportCard
 *
 * @param report The Report represented by the LayoutItem
 * @param start the startTime of the report
 * @param end the end time of the report found using startTime and duration
 * @param lane the lane associated with the Layout item.
 * @param totalLanes the minimum amount of lanes need for the cluster this report is part of.
 */
data class ReportLayoutItem(
    val report: Report,
    val start: LocalTime,
    val end: LocalTime,
    var lane: Int = 0,
    var totalLanes: Int = 1
)

/**
 * Coded using ChatGPT 5.0 cluster events into group of overlapping clusters. This assures that a
 * cluster has minimum totalLanes after assignLane
 *
 * @param events list of ReportLayoutItem to split into cluster
 * @see ReportLayoutItem
 * @see assignLanes
 */
fun clusterEvents(events: List<ReportLayoutItem>): List<List<ReportLayoutItem>> {
  if (events.isEmpty()) return emptyList()

  val sorted = events.sortedBy { it.start }
  val clusters = mutableListOf<MutableList<ReportLayoutItem>>()

  var currentCluster = mutableListOf(sorted[0])
  var currentEnd = sorted[0].end

  for (i in 1 until sorted.size) {
    val item = sorted[i]

    // If event starts before or exactly at current cluster end → same cluster
    if (item.start < currentEnd) {
      currentCluster.add(item)
      if (item.end > currentEnd) currentEnd = item.end
    } else {
      // New cluster
      clusters.add(currentCluster)
      currentCluster = mutableListOf(item)
      currentEnd = item.end
    }
  }

  clusters.add(currentCluster)
  return clusters
}

/**
 * Coded using chatGPT 5.0 Split a cluster of overlapping Report to different lane to avoid
 * overlapping
 *
 * @param cluster list of ReportLayout item to attribute to lanes
 */
fun assignLanes(cluster: List<ReportLayoutItem>) {
  val lanes: MutableList<MutableList<ReportLayoutItem>> = mutableListOf()

  fun overlaps(a: ReportLayoutItem, b: ReportLayoutItem) = a.start < b.end && b.start < a.end

  for (event in cluster.sortedBy { it.start }) {
    var laneIndex = -1

    // find earliest lane that is free before this event starts
    for (i in lanes.indices) {
      if (lanes[i].isEmpty() || !overlaps(lanes[i].last(), event)) {
        laneIndex = i
        break
      }
    }

    if (laneIndex == -1) {
      // Need a new lane
      laneIndex = lanes.size
      lanes.add(mutableListOf())
    }

    lanes[laneIndex].add(event)
    event.lane = laneIndex
  }

  // set width data
  val maxLanes = lanes.size
  cluster.forEach { it.totalLanes = maxLanes }
}
