package socket.hdsx.com.socketdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_connect, btn_receiver, btn_stop, btn_send;
    private ExecutorService mThreadPool;
    private Socket socket;
    private TextView tv_content;
    private EditText port;

    /*
    输入流 参数
     */
    // 输入流对象
    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    OutputStream os;
    private int intPort;//端口号


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mThreadPool = Executors.newCachedThreadPool();
        tv_content = findViewById(R.id.tv_content);
        port = findViewById(R.id.port);
        btn_connect = findViewById(R.id.btn_connect);
        btn_send = findViewById(R.id.btn_send);
        btn_receiver = findViewById(R.id.btn_receiver);
        btn_stop = findViewById(R.id.btn_stop);
        btn_connect.setOnClickListener(this);
        btn_receiver.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_send.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /*
            建立连接
             */
            case R.id.btn_connect:
                tv_content.setText("");

                String port = this.port.getText().toString();
                if (!"".equals(port)) {
                    intPort = Integer.parseInt(port);
                } else {
                    Toast.makeText(MainActivity.this, "输入端口号", Toast.LENGTH_SHORT).show();
                    return;
                }

                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            /*
                            建立请求连接
                             */
                            socket = new Socket("192.168.43.151", intPort);
                            System.out.println(socket.isConnected());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (socket.isConnected()) {
                                        tv_content.setText("建立连接成功！" + intPort);
                                    } else {
                                        tv_content.setText("建立连接失败！" + intPort);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
                /*
                获取数据
                 */
            case R.id.btn_receiver:
                tv_content.setText("");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            is = socket.getInputStream();
//                            isr = new InputStreamReader(is);
//                            br = new BufferedReader(isr);

                            DataInputStream input = new DataInputStream(is);
                            byte[] b = new byte[1024];

                            int len = 0;
                            String response = "";
                            while (true) {
                                len = input.read(b);
                                response = new String(b, 0, len);
                                Log.e("datadadata", response);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                break;
            case R.id.btn_stop:
                dissConnection();
                break;
            case R.id.btn_send:
                sendMessageToServer();
                break;
        }
    }


    /*
    断开 连接
     */
    private void dissConnection() {
        try {
            // 断开 客户端发送到服务器 的连接，即关闭输出流对象OutputStream
            os.close();
            // 断开 服务器发送到客户端 的连接，即关闭输入流读取器对象BufferedReader
            br.close();

            // 最终关闭整个Socket连接
            socket.close();

            // 判断客户端和服务器是否已经断开连接
            System.out.println(socket.isConnected());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    发送数据给服务端
     */
    private void sendMessageToServer() {
        long l = System.currentTimeMillis();
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("msgType", "infomation");
            jsonObject.put("msgValue", "status");
            jsonObject.put("msgTime", l + "");

        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    os = socket.getOutputStream();
                    os.write((jsonObject.toString() + "/n").getBytes("utf-8"));
                    // 数据的结尾加上换行符才可让服务器端的readline()停止阻塞
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}
