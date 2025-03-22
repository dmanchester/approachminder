<script>
  export let observations;
  export let showApproachSegments;
  export let clickHandlerTrajectory;

  const numberFormat = new Intl.NumberFormat();
</script>

<table id={showApproachSegments ? "table-with-segments" : "table-without-segments"}>
    <thead>
    <tr>
        <th>Callsign</th>
<!--        <th>Category</th>-->
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
<!--        <th>On Ground?</th>-->
        <th>Velocity</th>
        <th>True Track</th>
        <th>Vertical Rate</th>
<!--        <th>Squawk</th>-->
        <th>Report Age</th>
    </tr>
    </thead>
    <tbody>
    {#each observations as observation (observation.trajectory.aircraftProfile.icao24)}
        <tr>
            <td class="align-center">
                <button on:click={() => { clickHandlerTrajectory(observation.trajectory); }}>
                    {observation.trajectory.aircraftProfile.callsign}
                </button>
            </td>
            <!-- FIXNE Handle nulls better (for example, currently, squawk shows as "null" when not present. -->
            <!-- TODO Can we establish intermediate vars to make the following dot lookups shorter? -->
            <!-- TODO For fields with a decimal component, show trailing zeros (e.g., "1.0")  -->
<!--            <td>{observation.trajectory.aircraftProfile.category}</td>-->
            <td>{observation.position.latitude}°</td>
            <td>{observation.position.longitude}°</td>
            <td>{numberFormat.format(observation.position.altitude)} m</td>  <!-- TODO Need to add in some factor to address "height above ellipsoid" vs. "height above geoid", get to a plausible height above MSL -->
            {#if showApproachSegments}
                <td class="td-emphasis">{observation.position.approachSegment.airport}</td>
                <td class="td-emphasis">{observation.position.approachSegment.threshold}</td>
                <td class="td-emphasis">{numberFormat.format(observation.position.approachSegment.thresholdDistanceMeters)} m</td>
                <td class="td-emphasis">{numberFormat.format(observation.position.approachSegment.verticalDevMeters)} m</td>
                <td class="td-emphasis">{numberFormat.format(observation.position.approachSegment.horizontalDevMeters)} m</td>
                <td class="td-emphasis">{observation.position.approachSegment.normalizedEuclideanDistance}</td>
            {/if}
<!--            <td>{observation.position.onGround}</td>-->
            <td>{observation.position.velocity} m/s</td>
            <td>{observation.position.trueTrack}°</td>
            <td>{observation.position.verticalRate} m/s</td>
<!--            <td>{observation.position.squawk}</td>-->
            <td>{observation.ageOfObservation} s.</td>
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
