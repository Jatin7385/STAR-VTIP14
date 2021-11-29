package sample;

import com.opencsv.CSVReader;
import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.offline.OfflineCache;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller {
    @FXML
    private LineChart<?, ?> plot;

    @FXML
    private MapView mapView;

    @FXML
    private Button telemetry;

    private int t = 0;

    private ScheduledExecutorService scheduledExecutorService;
    private final int WINDOW_SIZE = 10;

    private static final int ZOOM_DEFAULT = 14;

    private CoordinateLine CarrierTrack;

    private static final Coordinate carrierCoordinates = new Coordinate(12.975555555555555, 79.16055555555556);
    private final List<Coordinate> coordinates = new ArrayList<>();

    private final Marker carrierMarker;
    private final MapLabel carrierLabel;

    private WMSParam wmsParam = new WMSParam()
            .setUrl("http://ows.terrestris.de/osm/service?")
            .addParam("layers", "OSM-WMS");

    private XYZParam xyzParams = new XYZParam()
            .withUrl("https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x})")
            .withAttributions(
                    "'Tiles &copy; <a href=\"https://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer\">ArcGIS</a>'");
    private int timeFlag = 0;


    public Controller() {
        carrierMarker = Marker.createProvided(Marker.Provided.RED).setPosition(carrierCoordinates).setVisible(
                true);
        carrierLabel = new MapLabel("RRS").setPosition(carrierCoordinates).setVisible(true);
        carrierMarker.attachLabel(carrierLabel);
    }

    public void initMapAndControls(Projection projection) {
        // init MapView-Cache
        final OfflineCache offlineCache = mapView.getOfflineCache();
        final String cacheDir = System.getProperty("java.io.tmpdir") + "/mapjfx-cache";

        // set the custom css file for the MapView
        mapView.setCustomMapviewCssURL(getClass().getResource("/custom_mapview.css"));

        // watch the MapView's initialized property to finish initialization
        mapView.initializedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                afterMapIsInitialized();
            }
        });

        // load two coordinate lines
        CarrierTrack = new CoordinateLine().setColor(Color.MAGENTA).setVisible(true);
        MapType mapType=MapType.OSM;;
        mapView.setMapType(mapType);

        // finally initialize the map view
        mapView.initialize(Configuration.builder()
                .projection(projection)
                .showZoomControls(false)
                .build());
    }

    //Function for making the Coordinate Line

    private Optional<CoordinateLine> loadCoordinateLine(URL url) {
        try (
                Stream<String> lines = new BufferedReader(
                        new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)).lines()
        ) {
            return Optional.of(new CoordinateLine(
                    lines.map(line -> line.split(";")).filter(array -> array.length == 2)
                            .map(values -> new Coordinate(Double.valueOf(values[0]), Double.valueOf(values[1])))
                            .collect(Collectors.toList())));
        } catch (IOException | NumberFormatException e) {
            System.out.println(e);
        }
        return Optional.empty();
    }

    private void afterMapIsInitialized() {
        mapView.setZoom(ZOOM_DEFAULT);
        mapView.setCenter(carrierCoordinates);
        // add the markers to the map - they are still invisible
        mapView.addMarker(carrierMarker);

        // add the fix label, the other's are attached to markers.
        mapView.addLabel(carrierLabel);

        // add the tracks
        mapView.addCoordinateLine(CarrierTrack);
    }

    private void setAltitude() {
        XYChart.Series series = new XYChart.Series();
        plot.setTitle("Altitude vs Time Graph");

        plot.getData().add(series);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                CSVReader reader = new CSVReader(new FileReader("C:\\Users\\Jatin Dhall\\Desktop\\Desktop File\\VIT\\VIT\\CLUBS,CHAPTERS,TEAMS\\Team sammard\\CODES\\JAVA\\GMAPSTEST\\data.csv"));
                String[] nextline;
                while ((nextline = reader.readNext()) != null) {
                    String[] columns = nextline;
                    write_into_csv(columns);
                    Platform.runLater(() -> {
                        series.getData().add(new XYChart.Data<>(t, Float.parseFloat(columns[0])));

                        Coordinate newcarrierCoordinates = new Coordinate(Double.parseDouble(columns[1]),Double.parseDouble(columns[2]));
                        carrierMarker.setPosition(newcarrierCoordinates).setVisible(true);
                        mapView.setCenter(newcarrierCoordinates);
                        coordinates.add(new Coordinate(Double.parseDouble(columns[1]),Double.parseDouble(columns[2])));
                        CarrierTrack = new CoordinateLine(coordinates)
                                .setColor(Color.RED)
                                .setFillColor(Color.web("lawngreen", 0.4))
                                .setClosed(true);
                        mapView.addCoordinateLine(CarrierTrack);
                        CarrierTrack.setVisible(true);
                    });
                    TimeUnit.SECONDS.sleep(1);
                        t++;
                }
                timeFlag = 1;
                Thread.currentThread().stop();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


    public void write_into_csv(String[] data_array)
    {
        try {
            String data="";
            for(int i=0;i< data_array.length;i++)
            {
                if(i!= data_array.length-1) {
                    data += data_array[i] + ",";
                }
                else{
                    data+=data_array[i];
                }
            }
            FileWriter fw = new FileWriter("log.csv", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            pw.println(data);
            pw.flush();
            pw.close();

        } catch (Exception e) {

        }
    }

    @FXML
    public void telemetry(ActionEvent event) throws InterruptedException
    {
        telemetry.setDisable(true);
        setAltitude();
    }
}

