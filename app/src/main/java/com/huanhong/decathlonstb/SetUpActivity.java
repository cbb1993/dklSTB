package com.huanhong.decathlonstb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.huanhong.decathlonstb.custom.MySpinner;
import com.huanhong.decathlonstb.custom.MySpinner.OnChooseListener;
import com.huanhong.decathlonstb.db.DBManager;
import com.huanhong.decathlonstb.http.callback.HttpCallback;
import com.huanhong.decathlonstb.model.BaseData;
import com.huanhong.decathlonstb.model.BaseListData;
import com.huanhong.decathlonstb.model.StoreData;
import com.huanhong.decathlonstb.model.StoreData.City;
import com.huanhong.decathlonstb.model.StoreData.City.Store;
import com.huanhong.decathlonstb.service.ClientService;
import com.huanhong.decathlonstb.util.AppJsonFileReader;
import com.huanhong.decathlonstb.util.CommentManager;
import com.huanhong.decathlonstb.util.StoreAmout;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;

import java.util.ArrayList;
import java.util.List;


public class SetUpActivity extends Activity implements OnClickListener {

    private final String TAG = "SetUpActivity";

    private MySpinner spinnerAreas, spinnerCitys, spinnerStores;
    private TextView textWelcome;
    private TextView textSave;
    private EditText editHttp;
    private EditText editIp1, editIp2, editIp3, editIp4;

    private DBManager db;
    private List<StoreData> dataList = null;
    private int areaPotion;

    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup);

        db = new DBManager(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        textWelcome = (TextView) this.findViewById(R.id.setup_welcome);
        editHttp = (EditText) findViewById(R.id.setup_http);
        spinnerAreas = (MySpinner) findViewById(R.id.spinner_areas);
        spinnerCitys = (MySpinner) findViewById(R.id.spinner_citys);
        spinnerStores = (MySpinner) findViewById(R.id.spinner_stores);
        textSave = (TextView) findViewById(R.id.setup_save);

        textSave.setOnClickListener(this);
        findViewById(R.id.setup_update_stores).setOnClickListener(this);
        findViewById(R.id.main_ip).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) v).setText(getIp());
            }
        });

        initSelect();
        String httpUrl = db.getHttpUrl();
        if (!TextUtils.isEmpty(httpUrl))
            editHttp.setText(httpUrl);
        initEditData();
        textWelcome.setText(getString(R.string.welcome) +
                getString(R.string.app_versionName) + ")");
        ((TextView) findViewById(R.id.setup_vision)).setText(getString(R.string.set_vision) +
                getString(R.string.app_versionName) + ")");
        ((TextView) findViewById(R.id.main_ip)).setText("本机Ip：" + getIp());

        showAnimation();
    }


    @Override
    public void onBackPressed() {
        MainActivity.start(this);
        finish();
    }

    private void initEditData() {
        editIp1 = (EditText) findViewById(R.id.setup_edit_ip1);
        editIp2 = (EditText) findViewById(R.id.setup_edit_ip2);
        editIp3 = (EditText) findViewById(R.id.setup_edit_ip3);
        editIp4 = (EditText) findViewById(R.id.setup_edit_ip4);
        String string = db.getIp();
        if (TextUtils.isEmpty(string)) {
            return;
        }
        String[] ips = string.split("\\.");
        if (ips.length == 4) {
            editIp1.setText(ips[0]);
            editIp2.setText(ips[1]);
            editIp3.setText(ips[2]);
            editIp4.setText(ips[3]);
        }
    }

    private void initSelect() {
        spinnerAreas.reset();
        spinnerCitys.reset();
        spinnerStores.reset();
        String stores = AppJsonFileReader.getFileStr(SetUpActivity.this, "stores.txt");
        if (TextUtils.isEmpty(stores)) {
            AppJsonFileReader.copyAssetFileToFiles(SetUpActivity.this, "stores.txt");
            stores = AppJsonFileReader.getFileStr(SetUpActivity.this, "stores.txt");
        }
        if (TextUtils.isEmpty(stores)) {
            Toast.makeText(this, R.string.read_stores_error, Toast.LENGTH_SHORT).show();
            return;
        }
        dataList = JSON.parseObject(stores, new TypeReference<List<StoreData>>() {
        });
        List<String> areasData = new ArrayList<>();
        for (StoreData storeData : dataList) {
            areasData.add(storeData.zone);
        }
        spinnerAreas.setData(areasData);
        spinnerAreas.setChooseListener(new OnChooseListener() {
            @Override
            public void click() {

            }

            @Override
            public void choose(int i, String s) {
                areaPotion = i;
                List<String> citysData = new ArrayList<>();
                for (City city : dataList.get(areaPotion).city) {
                    citysData.add(city.city_name);
                }
                spinnerCitys.setData(citysData);
                spinnerCitys.setSelect(0);
            }
        });

        spinnerCitys.setChooseListener(new OnChooseListener() {

            @Override
            public void click() {

            }

            @Override
            public void choose(int i, String s) {
                List<String> storeData = new ArrayList<>();
                for (Store store : dataList.get(areaPotion).city.get(i).shop) {
                    storeData.add(store.shop_no + "-" + store.shop_name);
                }
                spinnerStores.setData(storeData);
                spinnerStores.setSelect(0);
            }
        });
        String store = db.getStore();
        if (!TextUtils.isEmpty(store)) {
            String[] strStore = store.split("##");
            if (strStore.length == 3) {
                spinnerAreas.setSeletDefault(strStore[0]);
                spinnerCitys.setSeletDefault(strStore[1]);
                spinnerStores.setSeletDefault(strStore[2]);
            }
        }
    }

    private void updateStoreData() {
        progressDialog.setMessage(getString(R.string.stores_updating));
        progressDialog.show();
        OkGo.<BaseListData<StoreData>>post(db.getHttpUrl() + "/decathlon-store/getData.").execute(new HttpCallback<BaseListData<StoreData>>() {
            @Override
            public void success(BaseListData<StoreData> data) {
                if (data.ok && data.data != null) {
                    List<StoreData> storeData = data.data;
                    String strData = JSON.toJSONString(storeData);
                    Log.d(TAG, "updateStoreData: success-->"+strData);
                    Log.d(TAG, "updateStoreData: success-->"+strData.indexOf("测试"));
                    AppJsonFileReader.writeToFiles(SetUpActivity.this, strData, "stores.txt");
                    initSelect();
                    Toast.makeText(SetUpActivity.this, R.string.stores_update_success, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "updateStoreData: data error");
                    onError(null);
                }
            }

            @Override
            public void onError(Response<BaseListData<StoreData>> response) {
                super.onError(response);
                Toast.makeText(SetUpActivity.this, R.string.stores_update_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFinish() {
                progressDialog.dismiss();
            }
        });
    }

    public void syncData() {
        progressDialog.setMessage(getString(R.string.sync_ing));
        progressDialog.show();
        OkGo.<BaseData<StoreAmout>>post(db.getHttpUrl() + "/decathlon-store/updateData.")
                .params("shop_no", db.getStore().split("##")[2].split("-")[0])
                .params("date", System.currentTimeMillis() / 1000)
                .execute(new HttpCallback<BaseData<StoreAmout>>() {

                    @Override
                    public void success(BaseData<StoreAmout> storeAmoutDataResponse) {
                        StoreAmout storeAmout = storeAmoutDataResponse.data;
                        if (storeAmout != null) {
                            Log.d(TAG, "syncData: success");
                            db.sync(storeAmout);
                            Toast.makeText(SetUpActivity.this, R.string.sync_done, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Response<BaseData<StoreAmout>> response) {
                        super.onError(response);
                        Toast.makeText(SetUpActivity.this, R.string.sync_failed, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFinish() {
                        progressDialog.dismiss();
                    }
                });
    }

    private void showAnimation() {
        if (!TextUtils.isEmpty(db.getStore())) {
            findViewById(R.id.setup_update_data).setOnClickListener(this);
            return;
        } else {
            findViewById(R.id.setup_update_data).setVisibility(View.GONE);
            textWelcome.setVisibility(View.VISIBLE);
        }
        AlphaAnimation anima = new AlphaAnimation(1.0f, 1.0f);
        anima.setDuration(2000);
        textWelcome.startAnimation(anima);
        anima.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textWelcome.clearAnimation();
                textWelcome.setVisibility(View.GONE);
            }
        });
    }

    private String getIp() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    private void save() {
        String area = spinnerAreas.getObject();
        if (TextUtils.isEmpty(area)) {
            Toast.makeText(this, R.string.choose_regoin_please, Toast.LENGTH_SHORT).show();
            return;
        }

        String city = spinnerCitys.getObject();
        if (TextUtils.isEmpty(city)) {
            Toast.makeText(this, R.string.choose_city_please, Toast.LENGTH_SHORT).show();
            return;
        }

        String store = spinnerStores.getObject();
        if (TextUtils.isEmpty(store)) {
            Toast.makeText(this, R.string.choose_store_please, Toast.LENGTH_SHORT).show();
            return;
        }
        String storeNum = store.split("-")[0];
        if (storeNum.length() < 2) {
            Toast.makeText(this, R.string.choose_store_error, Toast.LENGTH_SHORT).show();
            return;
        }
        String saveStore = area + "##" + city + "##" + store;

        String httpUrl = editHttp.getText().toString();
        if (TextUtils.isEmpty(httpUrl)) {
            Toast.makeText(this, R.string.input_http_address_please, Toast.LENGTH_SHORT).show();
            return;
        }

        String ip1 = editIp1.getText().toString();
        String ip2 = editIp2.getText().toString();
        String ip3 = editIp3.getText().toString();
        String ip4 = editIp4.getText().toString();
        if (TextUtils.isEmpty(ip1) || TextUtils.isEmpty(ip2) || TextUtils.isEmpty(ip3) || TextUtils.isEmpty(ip4)) {
            Toast.makeText(this, R.string.input_watch_address_please, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!saveStore.equals(db.getStore())) {
            db.clearData();
            db.updateStore(saveStore);
            CommentManager.reset();
        }
        if (!httpUrl.equals(db.getHttpUrl()))
            db.updateHttp(httpUrl);
        String saveIp = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
        if (!saveIp.equals(db.getIp())) {
            db.updateIp(saveIp);
        }
        finish();
        MainActivity.start(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setup_save:
                save();
                break;
            case R.id.setup_update_data:
                syncData();
                break;
            case R.id.setup_update_stores:
                updateStoreData();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.closeDb();
    }
}