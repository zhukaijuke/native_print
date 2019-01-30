package com.zhukai.print.netty;

import com.alibaba.fastjson.JSON;
import com.zhukai.print.model.ResMap;
import com.zhukai.print.util.AlertUtil;
import com.zhukai.print.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.standard.PrinterName;
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

        Map<String, String> parameterMap = getParameterMap(req);

        ResMap resMap = this.print(parameterMap);
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
                return ResMap.error("传入参数不正确!");
            }
            // 查找打印机
            HashAttributeSet hs = new HashAttributeSet();
            String printerName = "TSC TTP-244 Pro";
            hs.add(new PrinterName(printerName, null));
            PrintService[] printServices = PrintServiceLookup.lookupPrintServices(DocFlavor.SERVICE_FORMATTED.PAGEABLE, hs);
            if (printServices == null || printServices.length == 0) {
                String msg = "无法找到打印机!";
                AlertUtil.error(msg);
                return ResMap.error(msg);
            }


            // 预览
            JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new URL(URLDecoder.decode(url, "UTF-8")));
            PropertyResourceBundle viewer = (PropertyResourceBundle) ResourceBundle.getBundle("bundle/viewer", Locale.CHINA);
            JasperViewer.viewReport(DefaultJasperReportsContext.getInstance(), jasperPrint, false, Locale.CHINA, viewer);
            /*JRPrintServiceExporter exporter = new JRPrintServiceExporter();
            SimplePrintServiceExporterConfiguration cfg = new SimplePrintServiceExporterConfiguration();
            cfg.setPrintService(printServices[0]);
            cfg.setDisplayPageDialog(false);    // 页面设置
            cfg.setDisplayPrintDialog(false);    // 打印机设置

            PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
            printRequestAttributeSet.add(new Copies(1));    // 打印张数
            cfg.setPrintRequestAttributeSet(printRequestAttributeSet);

            exporter.setConfiguration(cfg);
            exporter.setExporterInput(new SimpleExporterInput(new URL(URLDecoder.decode(url, "UTF-8"))));
            exporter.exportReport();*/
            return ResMap.success();
        } catch (Exception e) {
            log.error("", e);
            AlertUtil.error(e.getClass().getName());
            return ResMap.error(e.getClass().getName());
        }

    }

    public static boolean addIgnoreUrl(String ignoreUrl) {
        return ignoreUrlSet.add(ignoreUrl);
    }
}
