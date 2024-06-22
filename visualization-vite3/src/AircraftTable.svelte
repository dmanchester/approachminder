<script>
  export let observations;
  export let showApproachSegments;
  export let clickHandlerTrajectory;
</script>

<table>
    <thead>
    <tr>
        <th>Callsign</th>
        <th>Category</th>
        <th>Longitude</th>
        <th>Latitude</th>
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
            <!-- TODO Can we establish intermediate vars to make the following dot lookups shorter? -->
            <td>{observation.trajectory.aircraftProfile.category}</td>
            <td>{observation.position.longitude}</td>
            <td>{observation.position.latitude}</td>
            <td>{observation.position.altitude}</td>  <!-- TODO Need to add in some factor to address "height above ellipsoid" vs. "height above geoid", get to a plausible height above MSL -->
            {#if showApproachSegments}
                <td>{observation.position.approachSegment.airport}</td>
                <td>{observation.position.approachSegment.threshold}</td>
                <td>{observation.position.approachSegment.thresholdDistanceMeters}</td>
                <td>{observation.position.approachSegment.verticalDevMeters}</td>
                <td>{observation.position.approachSegment.horizontalDevMeters}</td>
                <td>{observation.position.approachSegment.normalizedEuclideanDistance}</td>
            {/if}
            <td>{observation.position.onGround}</td>
            <td>{observation.position.velocity}</td>
            <td>{observation.position.trueTrack}</td>
            <td>{observation.position.verticalRate}</td>
            <td>{observation.position.squawk}</td>
            <td>{observation.ageOfObservation}</td>
        </tr>
    {/each}
    </tbody>
</table>