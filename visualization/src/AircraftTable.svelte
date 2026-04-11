<script lang="ts">
  import type { PositionWrapper } from "../lib/UI";

  import { sortBy } from 'lodash';

  interface Props {
    posWrappers: Array<PositionWrapper>;
    showApproachSegments: boolean;
  }

  let { posWrappers, showApproachSegments }: Props = $props();

  let posWrappersSorted = $derived.by(() => {

    const fieldAccessor: (wrapper: PositionWrapper) => any = showApproachSegments ?
      wrapper => wrapper.position.approachSegment!.thresholdDistanceMeters :
      wrapper => wrapper.position.trajectory.callsign;

    return sortBy(posWrappers, fieldAccessor);
  });

  const numberFormat = new Intl.NumberFormat();
</script>

<table id={showApproachSegments ? "table-with-segments" : "table-without-segments"}>
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
    {#each posWrappersSorted as wrapper (wrapper.position.trajectory.icao24)}
        <tr>
            <td class="align-center">
                <button onclick={wrapper.trackEntityFunc}>
                    {wrapper.position.trajectory.callsign}
                </button>
            </td>
            <!-- FIXME Handle nulls better (for example, currently, squawk shows as "null" when not present. -->
            <!-- TODO Can we establish intermediate vars to make the following dot lookups shorter? -->
            <!-- TODO For fields with a decimal component, show trailing zeros (e.g., "1.0")  -->
            <td>{wrapper.position.latitude}°</td>
            <td>{wrapper.position.longitude}°</td>
            <td>{numberFormat.format(wrapper.position.altitude)} m</td>  <!-- TODO Need to add in some factor to address "height above ellipsoid" vs. "height above geoid", get to a plausible height above MSL -->
            {#if showApproachSegments}  <!-- TODO Better option than using "?" on each line below? -->
                <td class="td-emphasis">{wrapper.position.approachSegment?.airport}</td>
                <td class="td-emphasis">{wrapper.position.approachSegment?.threshold}</td>
                <td class="td-emphasis">{numberFormat.format(wrapper.position.approachSegment?.thresholdDistanceMeters ?? -1)} m</td>  <!-- TODO Here and below, "?? -1" definitely not optimal. Better option? -->
                <td class="td-emphasis">{numberFormat.format(wrapper.position.approachSegment?.verticalDevMeters ?? -1)} m</td>
                <td class="td-emphasis">{numberFormat.format(wrapper.position.approachSegment?.horizontalDevMeters ?? -1)} m</td>
                <td class="td-emphasis">{wrapper.position.approachSegment?.normalizedEuclideanDistance}</td>
            {/if}
            <td>{wrapper.position.velocity} m/s</td>
            <td>{wrapper.position.trueTrack}°</td>
            <td>{wrapper.position.verticalRate} m/s</td>
            <td>{wrapper.ageSecs} s.</td>
        </tr>
    {/each}
    </tbody>
</table>

<style>
  table {
    border-collapse: collapse;
  }

  #table-without-segments {
    width: 57.14%;  /* 8/14 */
  }

  th, td {
    padding: 4px;
    border: 1px solid #ddd;
  }

  th {
    text-align: center;
  }

  #table-with-segments th {
    width: 7.1429%;  /* 100%/14 */
  }

  #table-without-segments th {
    width: 12.5%;  /* 100%/8 */
  }

  thead tr {
    background-color: #f0f0f0;
  }

  tbody tr:nth-child(even) {
    background-color: #f9f9f9;
  }

  tbody tr:nth-child(odd) {
    background-color: #ffffff;
  }

  td {
    text-align: right;
  }

  .td-emphasis {
    background-color: #ffa5002f;
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
