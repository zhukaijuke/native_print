package com.zhukai.print;

import com.zhukai.print.netty.HttpServer;
import com.zhukai.print.netty.ServerHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.URL;

@Slf4j
public class Main extends Application {

    private int httpPort = 8086;

    private String title = "打印辅助程序";

    private TrayIcon trayIcon;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 最小化托盘
        this.enableTray(primaryStage);

        URL resource = Thread.currentThread().getContextClassLoader().getResource("fxml/main.fxml");
        Parent root = FXMLLoader.load(resource);
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(title);
        // 不能改变窗体大小
        primaryStage.setResizable(false);
        // 设置图标G
        primaryStage.getIcons().add(new Image("/icon/print.png"));
        // 关闭时监听事件
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            Platform.runLater(() -> primaryStage.hide());
        });
        primaryStage.show();

        // 开启一个Http服务
        this.startHttpServer();
    }

    private void startHttpServer() {
        new Thread(() -> {
            System.setProperty("io.netty.noUnsafe", "true");
            // 添加忽略地址
            ServerHandler.addIgnoreUrl("/favicon.ico");
            // 开启服务
            new HttpServer().bind(httpPort);
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }


    // 右下角, 最小化
    private void enableTray(final Stage stage) {
        PopupMenu popupMenu = new PopupMenu();
        java.awt.MenuItem openItem = new java.awt.MenuItem("显示");
        java.awt.MenuItem hideItem = new java.awt.MenuItem("最小化");
        java.awt.MenuItem quitItem = new java.awt.MenuItem("退出");

        Platform.setImplicitExit(false); // 多次使用显示和隐藏设置false

        ActionListener acl = e -> {
            java.awt.MenuItem item = (java.awt.MenuItem) e.getSource();
            if (item.getLabel().equals("退出")) {
                SystemTray.getSystemTray().remove(trayIcon);
                Platform.exit();
                System.exit(0);
                return;
            }
            if (item.getLabel().equals("显示")) {
                Platform.runLater(() -> stage.show());
            }
            if (item.getLabel().equals("最小化")) {
                Platform.runLater(() -> stage.hide());
            }
        };

        // 双击事件方法
        MouseListener sj = new MouseListener() {

            public void mouseReleased(MouseEvent e) {}

            public void mousePressed(MouseEvent e) {}

            public void mouseExited(MouseEvent e) {}

            public void mouseEntered(MouseEvent e) {}

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (stage.isShowing()) {
                        Platform.runLater(() -> stage.hide());
                    } else {
                        Platform.runLater(() -> stage.show());
                    }
                }
            }
        };


        openItem.addActionListener(acl);
        quitItem.addActionListener(acl);
        hideItem.addActionListener(acl);

        popupMenu.add(openItem);
        popupMenu.add(hideItem);
        popupMenu.add(quitItem);

        try {
            SystemTray tray = SystemTray.getSystemTray();
            BufferedImage image = ImageIO.read(Main.class.getResourceAsStream("/icon/print16.png"));
            trayIcon = new TrayIcon(image, title, popupMenu);
            trayIcon.setToolTip(title);
            tray.add(trayIcon);
            trayIcon.addMouseListener(sj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
