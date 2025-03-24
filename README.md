# ApproachMinder

ApproachMinder is a proof of concept for **detecting incorrectly flown aircraft approaches** in real time using [ADS-B](https://skybrary.aero/articles/automatic-dependent-surveillance-broadcast-ads-b) data.

Created by [Daniel Manchester](https://www.dmanchester.com/), ApproachMinder is still under development, but the initial results are encouraging.

<!-- To apply borders to images in GitHub Markdown, wrap in "kbd" tags. See:
     https://stackoverflow.com/questions/37349314/is-it-possible-to-add-border-to-image-in-github-markdown -->
<kbd>![DAL1152 about to cross the threshold of KSFO Runway 28L.](images/dal1152-at-185101.png)</kbd>

## Motivation

Incorrectly flown aircraft approaches are an ongoing problem in aviation. Under such an approach, a pilot seeking to land an aircraft deviates excessively from the intended runway's approach path.

**Incorrectly flown approaches can be deadly.** On 6 July 2013, while attempting to land at San Francisco International Airport, **Asiana Airlines Flight 214** descended too far as it neared Runway 28L. It struck the airport's seawall and crashed, killing three passengers and injuring many on board. ([NTSB report](https://www.ntsb.gov/investigations/accidentreports/reports/aar1401.pdf); [SKYbrary article](https://skybrary.aero/accidents-and-incidents/b772-san-francisco-ca-usa-2013))

Four years later, on 7 July 2017, a far larger tragedy was narrowly averted at the same airport. Intending to land on Runway 28R, **Air Canada Flight 759** instead lined up with a neighboring taxiway. Four aircraft loaded with passengers and fuel were positioned on the taxiway, awaiting their opportunity to take off. In the final moments before likely impact with the waiting aircraft, the Air Canada crew realized their mistake and aborted the landing attempt. ([NTSB report](https://www.ntsb.gov/investigations/accidentreports/reports/air1801.pdf); [SKYbrary article](https://skybrary.aero/accidents-and-incidents/a320-b789-a343-san-francisco-ca-usa-2017))

Incorrectly flown approaches are not unique to airports in the United States. On 23 May 2022, an **Airhub Airlines flight** sought to land in low-visibility conditions at Paris's Charles de Gaulle Airport. Due to an incorrectly set altimeter, the pilot descended excessively on approach. While still nearly one nautical mile from the runway, the pilot flew within six feet of the ground before aborting the landing attempt. ([BEA report](https://bea.aero/fileadmin/uploads/9HEMU/9H-EMU_EN.pdf); [SKYbrary article](https://skybrary.aero/accidents-and-incidents/a320-vicinity-paris-cdg-france-2022))

These incidents all relate to modern aircraft operating commercial flights to major airports. There have been other such incidents. And if one also considers [general aviation](https://skybrary.aero/articles/general-aviation-ga) flights, smaller airports, and less-advanced aircraft, **the problem of incorrectly flown approaches likely runs much deeper.**

## Concept

The wide availability of [ADS-B](https://skybrary.aero/articles/automatic-dependent-surveillance-broadcast-ads-b) data has opened new avenues for reducing the incidence of incorrectly flown aircraft approaches.

With ADS-B (Automatic Dependent Surveillance - Broadcast), aircraft continually broadcast their positions as determined by GPS (or other [GNSS](https://skybrary.aero/articles/global-navigation-satellite-system-gnss)).

**The ApproachMinder concept applies ADS-B to the problem of incorrectly flown approaches via a three-point strategy:**
1. Extract approaches and landings from historical ADS-B data.
2. Train runway-specific statistical models from those approaches and landings.
3. In real time, monitor the "live" ADS-B data from aircraft. Using the models from \#2, infer when an aircraft is on approach and its intended airport and runway, quantify its deviation from the applicable model, and raise an alert on excessive deviation.

Various stores of historical ADS-B data exist, as do multiple networks for collecting live data. Accordingly, **applying the ApproachMinder concept to an airport would not involve installing or operating additional equipment at that airport. The real-time monitoring and alerting could occur centrally.**

## Initial Results

As mentioned above, ApproachMinder is still under development, and the underlying software has not been refined. However, **work on ApproachMinder has progressed far enough to have produced encouraging initial results.**

The sections that follow walk through those initial results by ApproachMinder's two components: **analysis** and **visualization.**

### Analysis Component

For point #1 of the ApproachMinder concept (extracting approaches and landings from historical ADS-B data), using [OpenSky Network](https://opensky-network.org/) ADS-B data from the San Francisco Bay Area, as well as readily available geospatial information about the runways at San Francisco International Airport (KSFO) and Oakland International Airport (KOAK), **ApproachMinder's analysis component was able to extract approaches and landings for the airports' runways ([interactive map](https://www.google.com/maps/d/viewer?mid=19HSJa4v0cnq09HmCCfV7Ui6AVoEluMc&usp=sharing)):**

<kbd>![Approaches and landings extracted by ApproachMinder.](images/extracted-approaches-and-landings.png)</kbd>

### Visualization Component

**An interactive demo of ApproachMinder's visualization component is available.** It provides a perspective on the implementation of point #2 (training statistical models) and point #3 (inferring which aircraft are on approach and their intended runways; quantifying deviation from the models) of the ApproachMinder concept.

**[The demo is accessible here](https://www.dmanchester.com/approachminder-demo/?bing=true).** Due to its use of satellite imagery, the demo is fairly bandwidth-intensive: the initial download is approximately **50 MB,** and additional imagery is downloaded as the demo progresses.

#### Walkthrough of Demo

The demo shows the results of analyzing ADS-B data from 6 December 2022.

##### Initial View

The demo begins around 18:49:10. (All times are [UTC](https://en.wikipedia.org/wiki/Coordinated_Universal_Time).) It initially tracks SkyWest Airlines 4081 (callsign SKW4081):

<kbd>![SKW4081 at 9.2 km from KOAK Runway 30.](images/skw4081-at-184910-with-highlight.png)</kbd>

In the dashboard, the plain-colored columns are taken directly from ADS-B data. **The orange columns represent ApproachMinder's inferences and calculations.**

As can be seen, **ApproachMinder examines SKW4081's trajectory against its statistical models of runways and correctly infers that the aircraft is on approach to KOAK's Runway 30.** In drawing that inference, it relies on trajectory data only up to the point in time shown.

Further, **ApproachMinder calculates that:**

* 9,209 meters remain to the runway's threshold;
* SKW4081's current position deviates 3 meters vertically and less than 1 meter horizontally from a statistical mean of historical aircraft positions at that point in the approach; and
* the combined vertical and horizontal deviation is 0.1 [standard deviations](https://en.wikipedia.org/wiki/Standard_deviation) from that historical mean.

Standard deviation values put an aircraft's meters-based deviations in context. For example, early in an approach, a vertical deviation of 100 meters and a horizontal one of 50 meters could be acceptable and statistically common, and would thus lead to a small standard deviation. But that same magnitude of vertical and horizontal deviation late in an approach could signal a serious problem and be statistically rare, leading to a large standard deviation.

###### Alerts

ApproachMinder does not yet support alerts, but standard deviation values will likely be at their core: **If the standard deviation calculated for an aircraft's approach and position exceeds some threshold, ApproachMinder will raise an alert.**

##### Progression

As the demo progresses, **ApproachMinder's continuously re-examines SKW4081's trajectory against its statistical models.** It re-confirms that KOAK's Runway 30 is SKW4081's most likely target, and it re-calculates the vertical and horizontal deviations and the combined standard deviation.

**It also continuously re-analyzes the trajectories of the other aircraft it had inferred were on approach at the start of the demo** (FDX3809 and DAL1152), and **it draws on-approach inferences for other aircraft as appropriate** (for example, SWA1972 at 18:50:35).

At 18:51:17, ApproachMinder shows that SKW4081 will imminently cross the threshold of KOAK's Runway 30, the runway that—at 18:49:10 (over two minutes prior)—ApproachMinder had surmised the aircraft was targeting:

<kbd>![SKW4081 about to cross the threshold of KOAK Runway 30.](images/skw4081-at-185117-with-highlight.png)</kbd>

##### Interactivity

With the help of the [CesiumJS](https://cesium.com/platform/cesiumjs/) library, the ApproachMinder visualization offers multiple kinds of interactivity. A user can:

* Pan, zoom, and rotate the 3D view. (At the top-right corner of the visualization, see the "?" button for instructions.)
* Speed up or slow down the visualization, jump to arbitrary points in time, or play it in reverse.
* Click an aircraft's callsign on the dashboard to begin tracking it in the view.
* Click an aircraft in the view to confirm its callsign.
* Drag the divider between the view and the dashboard to allocate more space to one or the other.

#### Weaknesses

* The dashboard doesn't indicate which aircraft the 3D view is tracking.
* When an aircraft being tracked in the view is no longer visible, a new one isn't automatically selected.
* Clicking an aircraft in the view sometimes leads to occurrences in the Developer Tools console of a warning (`WebGL: INVALID_VALUE: uniform3fv: no array`).
* The same 3D model—a Boeing 737-800 with its landing gear extended—is used for all aircraft, regardless of their actual type and the status of their landing gear.

## Technical Architecture; Attributions

### Analysis Component
ApproachMinder's analysis component is written in [Scala](https://www.scala-lang.org/). It relies on various libraries in Scala and Java, employing them as follows:
* **[Play JSON](https://www.playframework.com/documentation/2.9.x/ScalaJson#The-Play-JSON-library):** Parse JSON-based ADS-B reports from the OpenSky Network. Produce JSON for the visualization component.
* **[GeoTools](https://geotools.org/):** Convert latitude and longitude positions to the meters-based [Universal Transverse Mercator](https://en.wikipedia.org/wiki/Universal_Transverse_Mercator_coordinate_system) (UTM) projection.
* **[JTS Topology Suite](https://github.com/locationtech/jts?tab=readme-ov-file#jts-topology-suite):** Perform calculations on UTM coordinates.
* **[Apache Commons Math](https://commons.apache.org/proper/commons-math/):** Calculate statistics.
* **[specs2](https://etorreborre.github.io/specs2/):** Automated testing.

### Visualization Component
ApproachMinder's visualization component is written in JavaScript. It employs libraries as follows:
* **[CesiumJS](https://cesium.com/platform/cesiumjs/):** Provide in-browser, three-dimensional visualizations of aircraft in flight. Offer various controls to the user.
* **[Svelte](https://svelte.dev/):** Display a dashboard of aircraft data, keeping it synchronized with the clock time as managed by CesiumJS.
* **[svelte-split-pane](https://www.npmjs.com/package/@rich_harris/svelte-split-pane):** Provide a draggable split pane between the CesiumJS-based view and the Svelte-based dashboard.
* **[B737-800 Model](https://skfb.ly/oSG9Q):** 3D model used in the view. (Created by [hikami3150](https://sketchfab.com/hikami3150). Licensed under the [Creative Commons Attribution](http://creativecommons.org/licenses/by/4.0/) license.)
* **[Jasmine](https://jasmine.github.io/):** Automated testing.

**[Vite](https://vite.dev/)** ([pronunciation](https://vite.dev/guide/#overview)) is the build tool of the ApproachMinder visualization and hosts it in development mode. Vite also produces production builds of the visualization and handles associated tasks, including bundling and minification.

ApproachMinder's Vite configuration is derived from the "[simply-cesium-vite-vue](https://github.com/s3xysteak/simply-cesium-vite-vue)" example.

## Future Development

Future ApproachMinder development is anticipated to address the weaknesses of the visualization component discussed above, and to implement the following items.

### More-Advanced Approach Models

ApproachMinder's current models support approaches beyond the simple, straight-in variety. However, they do not account well for distinct flows of aircraft before those flows converge for a given runway.

For example, referring to [this approach chart](https://aeronav.faa.gov/d-tpp/2502/00375I28RSAC1.PDF) for KSFO Runway 28R, ApproachMinder can accurately model the approach from waypoint CEPIN onward. But ApproachMinder does not handle well the fact that aircraft may arrive at CEPIN from either of two sub-approaches. (It calculates a mean path that lies between the sub-approaches that is likely flown by few aircraft, if any.)

A future version of ApproachMinder will rely on clustering logic to discern sub-approaches and will model them as a tree-like structure.

### Visualization of Approach Models

While the ApproachMinder visualization shows numerical results derived from approach models, it does not show the models themselves.

  A future version will display the models. The display will likely rely on a "crosshairs" metaphor, with vertical crosshairs placed at distance intervals leading to a runway threshold. Each crosshairs would be centered on the mean position a model has calculated from historical data for that distance, and crosshair markings would show the associated standard deviations.

### Alerting

As discussed above, ApproachMinder does not yet support alerts.

A future version will add them. If an aircraft deviates excessively from its inferred approach, ApproachMinder will show a warning within the visualization, even if the user hadn't previously been monitoring the aircraft's descent.

### Support for AIXM Data

Geospatial information about an airport's runways—their width; the coordinates of their thresholds' centers—must currently be input manually.

A future version of ApproachMinder will source that data from XML-based [AIXM](https://aixm.aero/) files; in the case of the United States, files from the [NASR data](https://www.faa.gov/air_traffic/flight_info/aeronav/aero_data/NASR_Subscription/) of the Federal Aviation Administration (FAA).

Initial development against FAA AIXM data with the [xml-spac](https://github.com/dylemma/xml-spac) parser has been successful.

### TypeScript Migration

A future version of ApproachMinder will migrate the visualization component to [TypeScript](https://www.typescriptlang.org/).
