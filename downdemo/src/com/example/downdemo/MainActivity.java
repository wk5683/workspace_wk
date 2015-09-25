package com.example.downdemo;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.downdemo.dao.InfoDao;
import com.example.downdemo.utils.Downloader;


public class MainActivity extends Activity {
	
	private LinearLayout rootLinearLayout;
	private LayoutInflater inflater;
	private EditText pathEditText;
	
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initView();
        
    }

    private void initView() {
		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		rootLinearLayout = (LinearLayout) findViewById(R.id.root);
		pathEditText = (EditText) findViewById(R.id.path);
		
		List<String> list = new InfoDao(this).queryUndone();
		for (String path:list) {
			createDownload(path);
		}

	}
    
    /** 
     * 下载按钮 
     * @param view 
     */  
    public void download(View view) {  
        String path = "http://192.168.1.199:8080/14_Web/" + pathEditText.getText().toString();  
        createDownload(path);  
    }  
  
    /** 
     * 动态生成新View 
     * 初始化表单数据 
     * @param path 
     */  
    private void createDownload(String path) {  
        //获取系统服务LayoutInflater，用来生成新的View  
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);  
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.download, null);  
          
        LinearLayout childLinearLayout = (LinearLayout) linearLayout.getChildAt(0);  
        ProgressBar progressBar = (ProgressBar) childLinearLayout.getChildAt(0);  
        TextView textView = (TextView) childLinearLayout.getChildAt(1);  
        Button button = (Button) linearLayout.getChildAt(1);  
  
        try {  
            button.setOnClickListener(new MyListener(progressBar, textView, path));  
            //调用当前页面中某个容器的addView，将新创建的View添加进来  
            rootLinearLayout.addView(linearLayout);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
  
    private final class MyListener implements OnClickListener {  
        private ProgressBar progressBar;  
        private TextView textView;  
        private int fileLen;  
        private Downloader downloader;  
        private String name;  
          
        /** 
         * 执行下载 
         * @param progressBar //进度条 
         * @param textView //百分比 
         * @param path  //下载文件路径 
         */  
        public MyListener(ProgressBar progressBar, TextView textView, String path) {  
            this.progressBar = progressBar;  
            this.textView = textView;  
            name = path.substring(path.lastIndexOf("/") + 1);  
  
            downloader = new Downloader(getApplicationContext(), handler);  
            try {  
                downloader.download(path, 3);  
            } catch (Exception e) {  
                e.printStackTrace();  
                Toast.makeText(getApplicationContext(), "下载过程中出现异常", 0).show();  
                throw new RuntimeException(e);  
            }  
        }  
          
        //Handler传输数据  
        private Handler handler = new Handler() {  
            @Override  
            public void handleMessage(Message msg) {  
                switch (msg.what) {  
                    case 0:  
                        //获取文件的大小  
                        fileLen = msg.getData().getInt("fileLen");  
                        //设置进度条最大刻度：setMax()  
                        progressBar.setMax(fileLen);  
                        break;  
                    case 1:  
                        //获取当前下载的总量  
                        int done = msg.getData().getInt("done");  
                        //当前进度的百分比  
                        textView.setText(name + "\t" + done * 100 / fileLen + "%");  
                        //进度条设置当前进度：setProgress()  
                        progressBar.setProgress(done);  
                        if (done == fileLen) {  
                            Toast.makeText(getApplicationContext(), name + " 下载完成", 0).show();  
                            //下载完成后退出进度条  
                            rootLinearLayout.removeView((View) progressBar.getParent().getParent());  
                        }  
                        break;  
                }  
            }  
        };  
  
        /** 
         * 暂停和继续下载 
         */  
        public void onClick(View v) {  
            Button pauseButton = (Button) v;  
            if ("||".equals(pauseButton.getText())) {  
                downloader.pause();  
                pauseButton.setText("▶");  
            } else {  
                downloader.resume();  
                pauseButton.setText("||");  
            }  
        }  
    }  

}
