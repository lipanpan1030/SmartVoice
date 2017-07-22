package com.meizu.smartvoice.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.meizu.common.widget.CompleteToast;
import com.meizu.smartvoice.R;
import com.meizu.smartvoice.tools.Activity_R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import flyme.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = "SmartVoice";
    @BindView(R.id.content)
    LinearLayout mContent;

    @BindView(R.id.begin_simulate)
    Button mbeginSimulate;
    public static long mLastSendTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity_R.setStatusBarDarkIcon(this, true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


    }

    @OnClick(R.id.begin_simulate)
    public void simulate(View view) {
        Log.d(TAG, "begin_simulate");
        CompleteToast.makeText(this, "开始展示", Toast.LENGTH_SHORT).show();
        startService(new Intent(this, SmartVoiceService.class));

        mLastSendTime = System.currentTimeMillis();
        View send = LayoutInflater.from(this).inflate(R.layout.send, null);
        mContent.addView(send);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mLastSendTime > 0) {
            mContent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SmartVoiceService.getInstance().showNotification("路人甲", "正在回复", 1);
                }
            }, 2000);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLastSendTime = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
