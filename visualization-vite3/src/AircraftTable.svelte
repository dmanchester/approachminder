<script>
  export let observations;
  export let showApproachSegments;
  export let clickHandlerTrajectory;

  const numberFormat = new Intl.NumberFormat();
</script>

<table>
    <thead>
    <tr>
        <th>Callsign</th>
        <th>Category</th>
        <th>Latitude</th>
        <th>Longitude</th>
        <th>Altitude</th>
        {#if showApproachSegments}
            <th>Airport*</th>
            <th>Threshold*</th>
            <th>Dist. to Threshold*</th>
            <th>Vertical Dev.*</th>
            <th>Horizontal Dev.*</th>
            <th>Std. Devs.*</th>
        {/if}
        <th>On Ground?</th>
        <th>Velocity</th>
        <th>True Track</th>
        <th>Vertical Rate</th>
        <th>Squawk</th>
        <th>Age of Obs.</th>
    </tr>
    </thead>
    <tbody>
    {#each observations as observation (observation.trajectory.aircraftProfile.icao24)}
        <tr>
            <td>
                <button on:click={() => { clickHandlerTrajectory(observation.trajectory); }}>
                    {observation.trajectory.aircraftProfile.callsign}
                </button>
            </td>
            <!-- FIXNE Handle nulls better (for example, currently, squawk shows as "null" when not present. -->
            <!-- TODO Can we establish intermediate vars to make the following dot lookups shorter? -->
            <!-- TODO For fields with a decimal component, show trailing zeros (e.g., "1.0")  -->
            <td>{observation.trajectory.aircraftProfile.category}</td>
            <td>{observation.position.latitude}</td>
            <td>{observation.position.longitude}</td>
            <td>{numberFormat.format(observation.position.altitude)} m</td>  <!-- TODO Need to add in some factor to address "height above ellipsoid" vs. "height above geoid", get to a plausible height above MSL -->
            {#if showApproachSegments}
                <td>{observation.position.approachSegment.airport}</td>
                <td>{observation.position.approachSegment.threshold}</td>
                <td>{numberFormat.format(observation.position.approachSegment.thresholdDistanceMeters)} m</td>
                <td>{numberFormat.format(observation.position.approachSegment.verticalDevMeters)} m</td>
                <td>{numberFormat.format(observation.position.approachSegment.horizontalDevMeters)} m</td>
                <td>{observation.position.approachSegment.normalizedEuclideanDistance}</td>
            {/if}
            <td>{observation.position.onGround}</td>
            <td>{observation.position.velocity} m/s</td>
            <td>{observation.position.trueTrack}°</td>
            <td>{observation.position.verticalRate} m/s</td>
            <td>{observation.position.squawk}</td>
            <td>{observation.ageOfObservation} s</td>
        </tr>
    {/each}
    </tbody>
</table>