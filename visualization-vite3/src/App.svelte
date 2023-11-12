<script>
  import { onMount } from "svelte";
  import { Viewer, Ion } from "cesium";
  import "../node_modules/cesium/Source/Widgets/widgets.css";

  window['CESIUM_BASE_URL'] = './node_modules/cesium/Source/'

  // TODO Externalize this.
  Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI2MzQ3MmQ0ZC1mNWU1LTQ2YzItYTRjMS01NGIxYzRjMGIwZTUiLCJpZCI6MTMyMTg5LCJpYXQiOjE2ODA2NTY4ODN9.CSgIJqm0gEDGCXXvbuW932tn04Q1m8Y_AmssiRXgR8Y';


  let viewer;
  onMount(async () => {
    viewer = new Viewer('cesiumContainer');
  });
</script>

<div id="cesiumContainer">
</div>
