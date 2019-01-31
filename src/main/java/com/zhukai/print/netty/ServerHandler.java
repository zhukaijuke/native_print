package com.zhukai.print.netty;

import com.alibaba.fastjson.JSON;
import com.zhukai.print.dao.CommonDao;
import com.zhukai.print.model.Contants;
import com.zhukai.print.model.RequestModel;
import com.zhukai.print.model.ResMap;
import com.zhukai.print.util.AlertUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PrinterName;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

import static io.netty.handler.codec.http.HttpUtil.is100ContinueExpected;

/**
 * 用于处理http请求
 *
 * @author zhukai
 * @date 2019/1/28
 */
@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    /**
     * 忽略的url请求
     */
    public static Set<String> ignoreUrlSet = new HashSet<>();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // 获取请求的uri
        String uri = req.uri();
        // 过滤请求
        if (ignoreUrlSet.contains(uri)) {
            return;
        }
        // 100 Continue
        if (is100ContinueExpected(req)) {
            ctx.write(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.CONTINUE));
        }
        ResMap resMap = null;
        if (HttpMethod.GET.equals(req.method())) {
            resMap = ResMap.error("不支持GET请求!");
        } else if (HttpMethod.POST.equals(req.method())) {
            Map<String, String> parameterMap = getParameterMap(req);
            resMap = this.print(parameterMap);
        }

        // 创建http响应
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(JSON.toJSONString(resMap), CharsetUtil.UTF_8));
        // 设置头信息, 允许跨域访问
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Authorization, Content-Type, Accept");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        // 将html write到客户端
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    public Map<String, String> getParameterMap(FullHttpRequest req) {
        Map<String, String> map = new HashMap<>();
        ByteBuf content = req.content();
        String s = content.toString(CharsetUtil.UTF_8);
        if (StringUtils.isEmpty(s)) {
            return map;
        }
        String[] arr = s.split("&");
        for (String str : arr) {
            String[] split = str.split("=");
            if (split.length == 2) {
                map.put(split[0], split[1]);
            } else if (split.length == 1) {
                map.put(split[0], null);
            }
        }
        return map;
    }

    public ResMap print(Map<String, String> parameterMap) throws Exception {
        try {
            String url = parameterMap.get("url");
            if (StringUtils.isEmpty(url)) {
                String msg = "传入参数不正确! url为空!";
                log.error(msg);
                Platform.runLater(() -> AlertUtil.error(msg));
                return ResMap.error(msg);
            }
            String printerType = parameterMap.get("printerType");
            if (StringUtils.isEmpty(printerType)) {
                printerType = Contants.DEFAULT_PRINTER;
            }
            String docType = parameterMap.get("docType");
            if (StringUtils.isEmpty(printerType)) {
                docType = StringUtils.EMPTY;
            }
            RequestModel req = new RequestModel();
            req.setUrl(URLDecoder.decode(url, "UTF-8"));
            req.setDocType(URLDecoder.decode(docType, "UTF-8"));
            req.setPrinterType(URLDecoder.decode(printerType, "UTF-8"));
            return printByUrl(req);
        } catch (Exception e) {
            log.error("", e);
            Platform.runLater(() -> AlertUtil.error(e.getClass().getName()));
            return ResMap.error(e.getClass().getName());
        }

    }

    public static ResMap printByUrl(RequestModel req) throws JRException, MalformedURLException, UnsupportedEncodingException {
        String url = req.getUrl();
        String printerType = req.getPrinterType();
        String docType = req.getDocType();
        Map<String, String> configMap = CommonDao.getSysConfig();
        String printerName = configMap.get(printerType);
        if (StringUtils.isEmpty(printerName)) {
            String msg = "没有找到默认打印机配置, 打印参数:" + printerType;
            log.error(msg);
            Platform.runLater(() -> AlertUtil.error(msg));
            return ResMap.error("传入参数不正确!");
        }
        // 查找打印机
        HashAttributeSet hs = new HashAttributeSet();
        hs.add(new PrinterName(printerName, null));
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, hs);
        if (printServices == null || printServices.length == 0) {
            String msg = "无法找到名称为[" + printerName + "]的打印机!";
            log.error(msg);
            Platform.runLater(() -> AlertUtil.error(msg));
            return ResMap.error(msg);
        }
        boolean showPreview = BooleanUtils.toBoolean(configMap.get(Contants.SHOW_PERVIEW_DIALOG));
        boolean showPage = BooleanUtils.toBoolean(configMap.get(Contants.SHOW_PAGE_DIALOG));
        boolean showPrint = BooleanUtils.toBoolean(configMap.get(Contants.SHOW_PRINT_DIALOG));
        if (showPreview) {
            // 预览
            JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new URL(url));
            PropertyResourceBundle viewer = (PropertyResourceBundle) ResourceBundle.getBundle("bundle/viewer", Locale.CHINA);
            JasperViewer.viewReport(DefaultJasperReportsContext.getInstance(), jasperPrint, false, Locale.CHINA, viewer);
        } else {
            JRPrintServiceExporter exporter = new JRPrintServiceExporter();
            SimplePrintServiceExporterConfiguration cfg = new SimplePrintServiceExporterConfiguration();
            cfg.setPrintService(printServices[0]);
            cfg.setDisplayPageDialog(showPage);    // 页面设置
            cfg.setDisplayPrintDialog(showPrint);   // 打印机设置

            PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
            printRequestAttributeSet.add(new Copies(1));    // 打印张数
            cfg.setPrintRequestAttributeSet(printRequestAttributeSet);

            exporter.setConfiguration(cfg);
            exporter.setExporterInput(new SimpleExporterInput(new URL(url)));
            exporter.exportReport();
        }
        // 保存日志
        CommonDao.insertPrintLog(req);
        log.info("打印成功, 单据类型:{}, 打印机:{},打印类型:{}", docType, printerName, printerType);
        return ResMap.success();
    }

    public static boolean addIgnoreUrl(String ignoreUrl) {
        return ignoreUrlSet.add(ignoreUrl);
    }
}
