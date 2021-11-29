package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import com.opencsv.CSVReader;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable{

    //COM3 Outgoing, COM4 Incoming

    private ScheduledExecutorService scheduledExecutorService;
    private final int WINDOW_SIZE = 10;
    @FXML
    private LineChart<?, ?> plot;

    @FXML
    private Button ignition;

    @FXML
    private Text burn_time;

    @FXML
    private Text thrust;

    @FXML
    private Button abort;


    @FXML
    private Button end_plot;

    @FXML
    private Text countDown;


    private int t = 0;
    private int abortFlag = 0;

//    private HCO5 hco5;
//
//    public Controller() throws IOException {
//        hco5 = new HCO5();
//        hco5.write("I");
//    }

    @FXML
    void ignition(ActionEvent event) throws InterruptedException {
//        try {
//            new HC05(1).go();
//        } catch (Exception ex) {
//            Logger.getLogger(HC05.class.getName()).log(Level.SEVERE, null, ex);
//        }
        ignition.setDisable(true);
        abort.setDisable(false);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        final int[] count = {10};
        scheduledExecutorService.scheduleAtFixedRate(()->{
            Platform.runLater(()->
            {
                abort.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        countDown.setText("ABORTED!!");
                        abortFlag = 1;
                        abort.setDisable(true);
                        Thread.currentThread().stop();
                        scheduledExecutorService.shutdownNow();
                    }
                });

                count[0] -=1;
                if(count[0] <= 0 && abortFlag == 0)
                {
                    countDown.setText("IGNITION!!!");
                    if(count[0] == 0) {
                        setThrust();
                    }
                    Thread.currentThread().stop();
                }
                else if(count[0] >0 && count[0]<=10 && abortFlag == 0)
                {
                    countDown.setText(Integer.toString(count[0]) + "s");
                }
                if(abortFlag == 1)
                {
                    countDown.setText("ABORTED!!");
                    Thread.currentThread().stop();
                    scheduledExecutorService.shutdownNow();
                }
            });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        },0,1,TimeUnit.SECONDS);
    }

    private void setThrust() {
        //reset.setDisable(false);
        abort.setDisable(true);
        end_plot.setDisable(false);
        XYChart.Series series = new XYChart.Series();
        plot.setTitle("Thrust vs Time Graph");

        plot.getData().add(series);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                CSVReader reader = new CSVReader(new FileReader("C:\\Users\\Jatin Dhall\\Desktop\\Desktop File\\VIT\\VIT\\CLUBS,CHAPTERS,TEAMS\\Team sammard\\CODES\\JAVA\\XBEE_COM_TEST\\data.csv"));
                String[] nextline;
                while ((nextline = reader.readNext()) != null) {
                    String[] columns = nextline;
                    write_into_csv(columns);
                    Platform.runLater(() -> {
                        end_plot.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                end_plot.setDisable(true);
//                                series.getData().clear();
                                scheduledExecutorService.shutdownNow();
                                return;

                            }
                        });
                        System.out.println("Thrust : " + columns[0]);
                        series.getData().add(new XYChart.Data<>(t, Float.parseFloat(columns[0])));

                        burn_time.setText(Integer.toString(t) + "s");
                        thrust.setText(columns[0] + "N");
                    });
                    TimeUnit.SECONDS.sleep(1);
                    t++;
                }
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        abort.setDisable(true);
        end_plot.setDisable(true);
    }
}
