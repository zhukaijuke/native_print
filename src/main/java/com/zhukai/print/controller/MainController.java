package com.zhukai.print.controller;

import com.zhukai.print.dao.CommonDao;
import com.zhukai.print.listener.MyChangeListener;
import com.zhukai.print.model.Contants;
import com.zhukai.print.model.PrinterDTO;
import com.zhukai.print.model.RequestModel;
import com.zhukai.print.netty.ServerHandler;
import com.zhukai.print.util.AlertUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.print.PrintService;
import java.awt.print.PrinterJob;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Slf4j
public class MainController implements Initializable {

    @FXML
    private ChoiceBox<String> defaultPrinterChoiceBox;
    @FXML
    private ChoiceBox<String> labelPrinterChoiceBox;
    @FXML
    private ChoiceBox<String> logisticsPrinterChoiceBox;
    @FXML
    private ChoiceBox<String> packPrinterChoiceBox;
    @FXML
    private ChoiceBox<String> a4PrinterChoiceBox;
    @FXML
    private TableView<PrinterDTO> printerTableView;
    @FXML
    private TableColumn<PrinterDTO, String> seqNumColumn;
    @FXML
    private TableColumn<PrinterDTO, String> printNameColumn;
    @FXML
    private CheckBox showPageDialogCheckBox;
    @FXML
    private CheckBox showPrintDialogCheckBox;
    @FXML
    private CheckBox showPerviewDialogCheckBox;
    @FXML
    private TextArea logArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // 获取打印机列表
        PrintService[] printers = PrinterJob.lookupPrintServices();

        // 绑定打印机列表数据
        seqNumColumn.setCellValueFactory(new PropertyValueFactory<>("seqNum")); // 映射
        printNameColumn.setCellValueFactory(new PropertyValueFactory<>("printerName")); // 映射
        List<PrinterDTO> printerDTOList = this.getPrinterDTOList(printers);
        printerTableView.setItems(FXCollections.observableArrayList(printerDTOList));

        // 绑定打印机下拉框数据
        List<String> printStrList = printerDTOList.stream().map(PrinterDTO::getPrinterName).collect(Collectors.toList());
        defaultPrinterChoiceBox.setItems(FXCollections.observableArrayList(printStrList));
        labelPrinterChoiceBox.setItems(FXCollections.observableArrayList(printStrList));
        logisticsPrinterChoiceBox.setItems(FXCollections.observableArrayList(printStrList));
        packPrinterChoiceBox.setItems(FXCollections.observableArrayList(printStrList));
        a4PrinterChoiceBox.setItems(FXCollections.observableArrayList(printStrList));

        // 初始化下拉框选中数据
        Map<String, String> map = CommonDao.getSysConfig();
        this.initChoiceBoxValue(map, printStrList);

        // 初始化CheckBox数据
        showPageDialogCheckBox.setSelected(BooleanUtils.toBoolean(map.get(Contants.SHOW_PAGE_DIALOG)));
        showPrintDialogCheckBox.setSelected(BooleanUtils.toBoolean(map.get(Contants.SHOW_PRINT_DIALOG)));
        showPerviewDialogCheckBox.setSelected(BooleanUtils.toBoolean(map.get(Contants.SHOW_PERVIEW_DIALOG)));

        // 绑定下拉框值变更的监听
        defaultPrinterChoiceBox.getSelectionModel().selectedItemProperty().addListener(new MyChangeListener<>(Contants.DEFAULT_PRINTER));
        labelPrinterChoiceBox.getSelectionModel().selectedItemProperty().addListener(new MyChangeListener<>(Contants.LABEL_PRINTER));
        logisticsPrinterChoiceBox.getSelectionModel().selectedItemProperty().addListener(new MyChangeListener<>(Contants.LOGISTICS_PRINTER));
        packPrinterChoiceBox.getSelectionModel().selectedItemProperty().addListener(new MyChangeListener<>(Contants.PACK_PRINTER));
        a4PrinterChoiceBox.getSelectionModel().selectedItemProperty().addListener(new MyChangeListener<>(Contants.A4_PRINTER));

        // 绑定CheckBox变更的监听
        showPageDialogCheckBox.selectedProperty().addListener(new MyChangeListener<>(Contants.SHOW_PAGE_DIALOG));
        showPrintDialogCheckBox.selectedProperty().addListener(new MyChangeListener<>(Contants.SHOW_PRINT_DIALOG));
        showPerviewDialogCheckBox.selectedProperty().addListener(new MyChangeListener<>(Contants.SHOW_PERVIEW_DIALOG));

        // 重定向System.out输出到logArea控件中
        this.setSystemOut();
        log.info("获取打印机成功!");
        log.info("初始化绑定数据成功!");
    }

    /**
     * 重定向System.out输出到logArea控件中
     */
    private void setSystemOut() {
        PrintStream printStream = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                String text = String.valueOf((char) b);
                Platform.runLater(() -> {
                    int length = logArea.getLength();
                    if (length > 10000) {
                        logArea.deleteText(0, length / 4);
                    }
                    logArea.appendText(text);
                });
            }

            @Override
            public void write(byte[] b, int off, int len) {
                String text = new String(b, off, len);
                Platform.runLater(() -> {
                    int length = logArea.getLength();
                    if (length > 10000) {
                        logArea.deleteText(0, length / 4);
                    }
                    logArea.appendText(text);
                });
            }
        }, true);
        System.setOut(printStream);
        System.setErr(printStream);
    }

    private List<PrinterDTO> getPrinterDTOList(PrintService[] printers) {
        List<PrinterDTO> printerDTOList = new ArrayList<>();
        int i = 1;
        for (PrintService printer : printers) {
            PrinterDTO dto = new PrinterDTO();
            dto.setSeqNum(i++);
            dto.setPrinterName(printer.getName());
            printerDTOList.add(dto);
        }
        return printerDTOList;
    }

    /**
     * 重新加载打印机列表
     */
    public void reloadPrint(ActionEvent event) {
        PrintService[] printers = PrinterJob.lookupPrintServices();
        List<PrinterDTO> printerDTOList = this.getPrinterDTOList(printers);
        printerTableView.getItems().setAll(printerDTOList);

        // 绑定打印机下拉框数据
        List<String> printStrList = printerDTOList.stream().map(PrinterDTO::getPrinterName).collect(Collectors.toList());
        defaultPrinterChoiceBox.getItems().setAll(printStrList);
        labelPrinterChoiceBox.getItems().setAll(printStrList);
        logisticsPrinterChoiceBox.getItems().setAll(printStrList);
        packPrinterChoiceBox.getItems().setAll(printStrList);
        a4PrinterChoiceBox.getItems().setAll(printStrList);

        Map<String, String> map = CommonDao.getSysConfig();
        this.initChoiceBoxValue(map, printStrList);
    }

    private void initChoiceBoxValue(Map<String, String> map, List<String> printStrList) {
        // 默认打印机下拉框初始值
        String defaultValue = map.get(Contants.DEFAULT_PRINTER);
        if (!StringUtils.isEmpty(defaultValue) && printStrList.contains(defaultValue)) {
            defaultPrinterChoiceBox.setValue(defaultValue);
        } else {
            CommonDao.deleteSysConfig(Contants.DEFAULT_PRINTER);
        }
        // 标签打印机下拉框初始值
        String labelValue = map.get(Contants.LABEL_PRINTER);
        if (!StringUtils.isEmpty(labelValue) && printStrList.contains(labelValue)) {
            labelPrinterChoiceBox.setValue(labelValue);
        } else {
            CommonDao.deleteSysConfig(Contants.LABEL_PRINTER);
        }
        // 物流面单打印机下拉框初始值
        String logisticsValue = map.get(Contants.LOGISTICS_PRINTER);
        if (!StringUtils.isEmpty(logisticsValue) && printStrList.contains(logisticsValue)) {
            logisticsPrinterChoiceBox.setValue(logisticsValue);
        } else {
            CommonDao.deleteSysConfig(Contants.LOGISTICS_PRINTER);
        }
        // 装箱清单打印机下拉框初始值
        String packValue = map.get(Contants.PACK_PRINTER);
        if (!StringUtils.isEmpty(packValue) && printStrList.contains(packValue)) {
            packPrinterChoiceBox.setValue(packValue);
        } else {
            CommonDao.deleteSysConfig(Contants.PACK_PRINTER);
        }
        // A4打印机下拉框初始值
        String a4Value = map.get(Contants.A4_PRINTER);
        if (!StringUtils.isEmpty(a4Value) && printStrList.contains(a4Value)) {
            a4PrinterChoiceBox.setValue(a4Value);
        } else {
            CommonDao.deleteSysConfig(Contants.A4_PRINTER);
        }
    }

    /**
     * 清空日志
     */
    public void clearLog(ActionEvent event) {
        Platform.runLater(() -> logArea.setText(""));
    }

    /**
     * 重打上一次请求的单据
     */
    public void rePrint(ActionEvent event) {
        RequestModel lastReq = CommonDao.getLastPrintLog();
        if (lastReq == null) {
            String msg = "没有查询到上次的打印记录!";
            log.error(msg);
            Platform.runLater(() -> AlertUtil.error(msg));
            return;
        }
        try {
            ServerHandler.printByUrl(lastReq);
        } catch (Exception e) {
            log.error("打印失败!", e);
        }
    }

}
