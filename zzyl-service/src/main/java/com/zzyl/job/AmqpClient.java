package com.zzyl.job;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.zzyl.constant.Constants;
import com.zzyl.entity.Device;
import com.zzyl.entity.DeviceData;
import com.zzyl.mapper.DeviceDataMapper;
import com.zzyl.mapper.DeviceMapper;
import com.zzyl.properties.AliIoTConfigProperties;
import com.zzyl.service.AlertRuleService;
import com.zzyl.vo.DeviceVo;
import com.zzyl.websocket.WebSocketServer;
import io.reactivex.Single;
import org.apache.commons.codec.binary.Base64;
import org.apache.qpid.jms.JmsConnection;
import org.apache.qpid.jms.JmsConnectionListener;
import org.apache.qpid.jms.message.JmsInboundMessageDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;

@Component
public class AmqpClient implements ApplicationRunner {
    private final static Logger logger = LoggerFactory.getLogger(AmqpClient.class);

    @Resource
    private AliIoTConfigProperties aliIoTConfigProperties;
    @Resource
    private ExecutorService executorService;
    @Resource
    private DeviceDataMapper deviceDataMapper;
    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private AlertRuleService alertRuleService;
    @Resource
    private WebSocketServer webSocketServer;

    //控制台服务端订阅中消费组状态页客户端ID一栏将显示clientId参数。
    //建议使用机器UUID、MAC地址、IP等唯一标识等作为clientId。便于您区分识别不同的客户端。
    private static String clientId;

    static {
        try {
            clientId = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    // 指定单个进程启动的连接数
    // 单个连接消费速率有限，请参考使用限制，最大64个连接
    // 连接数和消费速率及rebalance相关，建议每500QPS增加一个连接
    private static int connectionCount = 4;

    public void start() throws Exception {
        List<Connection> connections = new ArrayList<>();

        //参数说明，请参见AMQP客户端接入说明文档。
        for (int i = 0; i < connectionCount; i++) {
            long timeStamp = System.currentTimeMillis();
            //签名方法：支持hmacmd5、hmacsha1和hmacsha256。
            String signMethod = "hmacsha1";

            //userName组装方法，请参见AMQP客户端接入说明文档。
            String userName = clientId + "-" + i + "|authMode=aksign"
                    + ",signMethod=" + signMethod
                    + ",timestamp=" + timeStamp
                    + ",authId=" + aliIoTConfigProperties.getAccessKeyId()
                    + ",iotInstanceId=" + aliIoTConfigProperties.getIotInstanceId()
                    + ",consumerGroupId=" + aliIoTConfigProperties.getConsumerGroupId()
                    + "|";
            //计算签名，password组装方法，请参见AMQP客户端接入说明文档。
            String signContent = "authId=" + aliIoTConfigProperties.getAccessKeyId() + "&timestamp=" + timeStamp;
            String password = doSign(signContent, aliIoTConfigProperties.getAccessKeySecret(), signMethod);
            String connectionUrl = "failover:(amqps://" + aliIoTConfigProperties.getHost() + ":5671?amqp.idleTimeout=80000)"
                    + "?failover.reconnectDelay=30";

            Hashtable<String, String> hashtable = new Hashtable<>();
            hashtable.put("connectionfactory.SBCF", connectionUrl);
            hashtable.put("queue.QUEUE", "default");
            hashtable.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
            Context context = new InitialContext(hashtable);
            ConnectionFactory cf = (ConnectionFactory) context.lookup("SBCF");
            Destination queue = (Destination) context.lookup("QUEUE");
            // 创建连接。
            Connection connection = cf.createConnection(userName, password);
            connections.add(connection);

            ((JmsConnection) connection).addConnectionListener(myJmsConnectionListener);
            // 创建会话。
            // Session.CLIENT_ACKNOWLEDGE: 收到消息后，需要手动调用message.acknowledge()。
            // Session.AUTO_ACKNOWLEDGE: SDK自动ACK（推荐）。
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            connection.start();
            // 创建Receiver连接。
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(messageListener);
        }

        logger.info("amqp  is started successfully, and will exit after server shutdown ");
    }

    private MessageListener messageListener = message -> {
        try {
            //异步处理收到的消息，确保onMessage函数里没有耗时逻辑
            executorService.submit(() -> processMessage(message));
        } catch (Exception e) {
            logger.error("submit task occurs exception ", e);
        }
    };

    /**
     * 在这里处理您收到消息后的具体业务逻辑。
     */
    private void processMessage(Message message) {
        try {
            byte[] body = message.getBody(byte[].class);
            String content = new String(body);
            String topic = message.getStringProperty("topic");
            String messageId = message.getStringProperty("messageId");
            logger.info("receive message"
                    + ",\n topic = " + topic
                    + ",\n messageId = " + messageId
                    + ",\n content = " + content);

            //解析JSON数据
            Content data = JSONUtil.toBean(content, Content.class);
            String deviceId = data.getIotId();
            //查到对应的属性
            List<DeviceVo> deviceVos = this.deviceMapper.selectByDeviceIds(new ArrayList<>(Collections.singletonList(deviceId)));
            List<DeviceData> list = new ArrayList<>();
            if(deviceVos.size() != 0){
                DeviceVo deviceVo = deviceVos.get(0);
                for (String i : data.getItems().keySet()) {
                    DeviceData deviceData = DeviceData.builder().build();
                    //复制属性
                    BeanUtil.copyProperties(deviceVo, deviceData, CopyOptions.create().ignoreNullValue());
                    deviceData.setFunctionName(i);
                    deviceData.setDataValue(Convert.toStr(data.getItems().get(i).getValue()));
                    Instant instant = Instant.ofEpochMilli(data.getItems().get(i).getTime());
                    LocalDateTime ldt = LocalDateTime.ofInstant(instant,ZoneId.systemDefault());
                    deviceData.setAlarmTime(ldt);
                    deviceData.setStatus("0");
                    deviceData.setIotId(deviceVo.getDeviceId());
                    deviceData.setAccessLocation(deviceVo.getRemark());
                    deviceData.setProductId(data.getProductKey());
                    list.add(deviceData);
                }
                String redisData = JSONUtil.toJsonStr(list);
                stringRedisTemplate.opsForHash().put(Constants.DEVICE_LASTDATA_CACHE_KEY,deviceVo.getDeviceId(),redisData);
            }
            //过滤异常数据
            alertRuleService.alertFilter(list);

            //检测是否存在报警状态的数据，如果存在向客户端发消息
            for (DeviceData deviceData : list) {
                if(deviceData.getStatus().equals("2")){
                    Device device = deviceMapper.selectByDeviceId(deviceData.getIotId());
                    if(device.getLocationType().equals(1)){
                        String floorId = StrUtil.subBefore(device.getDeviceDescription(), ",", false);
                        webSocketServer.sendToAllClient(floorId);
                    }
                }
            }

            //数据库插入
            this.deviceDataMapper.batchInsert(list);


        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.error("processMessage occurs error ", e);
        }
    }

    private JmsConnectionListener myJmsConnectionListener = new JmsConnectionListener() {
        /**
         * 连接成功建立。
         */
        @Override
        public void onConnectionEstablished(URI remoteURI) {
            logger.info("onConnectionEstablished, remoteUri:{}", remoteURI);
        }

        /**
         * 尝试过最大重试次数之后，最终连接失败。
         */
        @Override
        public void onConnectionFailure(Throwable error) {
            logger.error("onConnectionFailure, {}", error.getMessage());
        }

        /**
         * 连接中断。
         */
        @Override
        public void onConnectionInterrupted(URI remoteURI) {
            logger.info("onConnectionInterrupted, remoteUri:{}", remoteURI);
        }

        /**
         * 连接中断后又自动重连上。
         */
        @Override
        public void onConnectionRestored(URI remoteURI) {
            logger.info("onConnectionRestored, remoteUri:{}", remoteURI);
        }

        @Override
        public void onInboundMessage(JmsInboundMessageDispatch envelope) {
        }

        @Override
        public void onSessionClosed(Session session, Throwable cause) {
        }

        @Override
        public void onConsumerClosed(MessageConsumer consumer, Throwable cause) {
        }

        @Override
        public void onProducerClosed(MessageProducer producer, Throwable cause) {
        }
    };

    /**
     * 计算签名，password组装方法，请参见AMQP客户端接入说明文档。
     */
    private static String doSign(String toSignString, String secret, String signMethod) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), signMethod);
        Mac mac = Mac.getInstance(signMethod);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(toSignString.getBytes());
        return Base64.encodeBase64String(rawHmac);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        start();
    }
}
