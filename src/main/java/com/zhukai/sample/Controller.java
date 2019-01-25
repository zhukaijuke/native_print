package com.zhukai.sample;

import com.zhukai.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSizeName;
import java.awt.print.PrinterJob;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private Label myLabel;
    @FXML
    private TextField myInput;
    @FXML
    private Button myBtn;
    @FXML
    private ChoiceBox<PrintService> printerList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PrintService[] printers = PrinterJob.lookupPrintServices();
        printerList.setItems(FXCollections.observableArrayList(printers));
        printerList.setConverter(new StringConverter<PrintService>() {
            @Override
            public String toString(PrintService ps) {
                return ps.getName();
            }

            @Override
            public PrintService fromString(String str) {
                return null;
            }
        });
    }

    // When user click on myButton
    // this method will be called.
    public void showDateTime(ActionEvent event) {
        System.out.println("Button showDateTime!");

        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
        String dateTimeString = df.format(now);
        // Show in VIEW
        myInput.setText(dateTimeString);
        myLabel.setText(dateTimeString);
    }

    public void printFile(ActionEvent event) throws Exception {
        System.out.println("Button printFile!");
        PrintService ps = printerList.getValue();
        if (ps == null) {
            AlertUtil.error("请选择打印机!");
        }
        URL url = new URL("http://localhost/print/test2");
        // Object obj = JRLoader.loadObject(url);
        // JasperPrint jasperPrint = (JasperPrint);

        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        SimplePrintServiceExporterConfiguration cfg = new SimplePrintServiceExporterConfiguration();
        cfg.setPrintService(ps);
        cfg.setDisplayPageDialog(false);
        cfg.setDisplayPrintDialog(false);

        PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
        printRequestAttributeSet.add(new Copies(1));

        cfg.setPrintRequestAttributeSet(printRequestAttributeSet);
        exporter.setConfiguration(cfg);
        exporter.setExporterInput(new SimpleExporterInput(url));
        exporter.exportReport();
    }
}
