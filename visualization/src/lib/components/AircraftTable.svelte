<script lang="ts">
  import { formatNumber } from "../utils/ui";
  import { type PositionWrapper } from "../model/PositionWrapper";

  import { sortBy } from "lodash";

  interface Props {
    positionWrappers: Array<PositionWrapper>;
    showApproachSegments: boolean;
    icao24ToTrack: string;
  }

  let {
    positionWrappers,
    showApproachSegments,
    icao24ToTrack = $bindable(),
  }: Props = $props();

  let positionWrappersSorted = $derived.by(() => {
    const fieldAccessor: (wrapper: PositionWrapper) => number | string | null =
      showApproachSegments
        ? (wrapper) => wrapper.position.approachSegment!.thresholdDistanceMeters
        : (wrapper) => wrapper.position.trajectory.callsign;

    return sortBy(positionWrappers, fieldAccessor);
  });
</script>

<!--
@component

A table of aircraft and their latest positions.

In the basic case of, the aircraft are not on approach to a runway, the table shows callsign, latitude, longitude, etc.

In the advanced case of, the aircraft *are* on approach (and "true" is passed for showApproachSegments), the table
additionally shows information about the approach (runway, distance to threshold, vertical and horizontal deviation,
etc.).
-->
<table
  id={showApproachSegments ? "table-with-segments" : "table-without-segments"}
>
  <thead>
    <tr>
      <th>Callsign</th>
      <th>Latitude</th>
      <th>Longitude</th>
      <th>Altitude</th>
      {#if showApproachSegments}
        <th class="td-emphasis">Airport</th>
        <th class="td-emphasis">Runway</th>
        <th class="td-emphasis">Dist. to Threshold</th>
        <th class="td-emphasis">Vertical Deviation</th>
        <th class="td-emphasis">Horiz. Deviation</th>
        <th class="td-emphasis">Standard Devs.</th>
      {/if}
      <th>Velocity</th>
      <th>True Track</th>
      <th>Vertical Rate</th>
      <th>Report Age</th>
    </tr>
  </thead>
  <tbody>
    {#each positionWrappersSorted as wrapper, i (wrapper.position.trajectory.icao24)}
      {@const position = wrapper.position}
      <tr
        class={position.trajectory.icao24 === icao24ToTrack
          ? "tr-tracking"
          : i % 2 === 0
            ? "tr-even"
            : "tr-odd"}
      >
        <td class="align-center">
          <button
            onclick={() => {
              icao24ToTrack = position.trajectory.icao24;
            }}
          >
            {position.trajectory.callsign}
          </button>
        </td>
        <td>{formatNumber(position.latitude, 4)}°</td>
        <td>{formatNumber(position.longitude, 4)}°</td>
        <td>{formatNumber(position.altitude, 0)} m</td>
        <!-- TODO Need to add in some factor to address "height above ellipsoid" vs. "height above geoid", get to a plausible height above MSL -->
        {#if showApproachSegments && position.approachSegment}
          {@const approachSegment = position.approachSegment}
          <td class="td-emphasis">{approachSegment.airport}</td>
          <td class="td-emphasis">{approachSegment.threshold}</td>
          <td class="td-emphasis"
            >{formatNumber(approachSegment.thresholdDistanceMeters, 0)} m</td
          >
          <td class="td-emphasis"
            >{formatNumber(approachSegment.verticalDevMeters, 0)} m</td
          >
          <td class="td-emphasis"
            >{formatNumber(approachSegment.horizontalDevMeters, 0)} m</td
          >
          <td class="td-emphasis"
            >{formatNumber(approachSegment.normalizedEuclideanDistance, 1)}</td
          >
        {/if}
        <td>{formatNumber(position.velocity, 0)} m/s</td>
        <td>{formatNumber(position.trueTrack, 0)}°</td>
        <td>{formatNumber(position.verticalRate, 1)} m/s</td>
        <td>{formatNumber(wrapper.ageSecs, 0)} s.</td>
      </tr>
    {/each}
  </tbody>
</table>

<style>
  table {
    border-collapse: collapse;
  }

  #table-without-segments {
    width: 57.14%; /* 8/14 */
  }

  th,
  td {
    padding: 4px;
    border: 1px solid #ddd;
  }

  th {
    text-align: center;
  }

  #table-with-segments th {
    width: 7.1429%; /* 100%/14 */
  }

  #table-without-segments th {
    width: 12.5%; /* 100%/8 */
  }

  thead tr {
    background-color: #f0f0f0;
  }

  .tr-tracking {
    background-color: #ffffc0;
  }

  .tr-even {
    /* "even" and "odd" refer to JavaScript 0-based indexing, as opposed to CSS 1-based indexing */
    background-color: #ffffff;
  }

  .tr-odd {
    background-color: #f9f9f9;
  }

  td {
    text-align: right;
  }

  .td-emphasis {
    background-color: #007bff1f;
  }

  button {
    background-color: #007bff;
    color: #ffffff;
    border: none;
    border-radius: 6px;
  }

  button:hover {
    background-color: #0056b3; /* darker blue */
  }

  .align-center {
    text-align: center;
  }
</style>
