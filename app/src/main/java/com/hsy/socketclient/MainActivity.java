package com.hsy.socketclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hsy.socketclient.msg.HelloOuterClass;
import com.hsy.socketclient.utils.SocketUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @BindView(R.id.connect)
    Button connect;
    @BindView(R.id.send)
    Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.connect, R.id.send,R.id.disconnect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.connect:
                SocketUtils.getInstance().connect();
                break;
            case R.id.send:
                HelloOuterClass.Hello.Builder helloBuilder = HelloOuterClass.Hello.newBuilder();
                helloBuilder.setName("hsy66");
                HelloOuterClass.Hello hello = helloBuilder.build();
                SocketUtils.getInstance().send(hello.toByteArray());
                break;
            case R.id.disconnect:
                SocketUtils.getInstance().disconnect();
                break;
        }
    }
}
