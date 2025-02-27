package com.cretin.cityselect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ResourceUtils;
import com.cretin.cityselect.utils.JSONHelper;
import com.xs.widget.callback.OnCitySelectListener;
import com.xs.widget.callback.OnLocationListener;
import com.xs.widget.model.DataModel;
import com.xs.widget.view.CitySelectView;

import java.util.ArrayList;
import java.util.List;

public class SelfSelectCityActivity extends AppCompatActivity {

    public static void start(Context context, int type) {
        Intent intent = new Intent(context, SelfSelectCityActivity.class);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    private CitySelectView citySelectView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //0 所有 1 热门城市 2 当前城市 3 仅列表
        final int type = getIntent().getIntExtra("type", 0);

        //隐藏actionBar 好看点
        getSupportActionBar().hide();

        //状态栏透明 好看点 你自己看着办哈
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }

        //状态看文字黑色好看点 你随意哈
        View decor = getWindow().getDecorView();
        //decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        setContentView(R.layout.activity_select_city);

        citySelectView = findViewById(R.id.city_view);

        //设置搜索框的文案提示
        citySelectView.setSearchTips("搜索");

        //拉取大数据还是要在子线程做的 我这是图简单 你别这样玩啊
        new Thread() {
            @Override
            public void run() {
                final List<DataModel> allCitys = new ArrayList<>();

                //这里是模仿网络请求获取城市列表 这里就放了一个json
                final String city = ResourceUtils.readAssets2String("city.json");

                try {
                    CityResponse cityResponse = JSONHelper.parseObject(city, CityResponse.class);
                    List<CityResponse.DataBean> data = cityResponse.getData();
                    for (CityResponse.DataBean item : data) {
                        if (item.getSons() == null) {
                            allCitys.add(new DataModel(item.getName(), item.getAreaId()));
                        } else {
                            for (CityResponse.DataBean.SonsBean son : item.getSons()) {
                                allCitys.add(new DataModel(son.getName(), son.getAreaId()));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //设置热门城市列表 这都是瞎写的 哈哈哈
                final List<DataModel> hotCitys = new ArrayList<>();
                hotCitys.add(new DataModel("深圳", "10000000"));
                hotCitys.add(new DataModel("广州", "10000001"));
                hotCitys.add(new DataModel("北京", "10000002"));
                hotCitys.add(new DataModel("天津", "10000003"));
                hotCitys.add(new DataModel("武汉", "10000004"));

                //设置当前城市数据
                final DataModel currentCity = new DataModel("深圳", "10000000");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //绑定数据到视图 需要 所有城市列表（必传） 热门城市列表（选填） 和 当前城市列表（选填）
                        if (type == 0) {
                            citySelectView.bindData(allCitys, hotCitys, currentCity);
                        } else if (type == 1) {
                            citySelectView.bindData(allCitys, hotCitys, null);
                        } else if (type == 2) {
                            citySelectView.bindData(allCitys, null, currentCity);
                        } else {
                            citySelectView.bindData(allCitys, null, null);
                        }
                    }
                });
            }
        }.start();


        //设置城市选择之后的事件监听
        citySelectView.setOnCitySelectListener(new OnCitySelectListener() {
            @Override
            public void onCitySelect(DataModel dataModel) {
                Toast.makeText(SelfSelectCityActivity.this, "你点击了：" + dataModel.getDataName() + ":" + dataModel.getExtra().toString(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.putExtra("model", dataModel);
                setResult(RESULT_OK, intent);

                finish();
            }

            @Override
            public void onSelectCancel() {
                Toast.makeText(SelfSelectCityActivity.this, "你取消了城市选择", Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        //设置点击重新定位之后的事件监听
        citySelectView.setOnLocationListener(new OnLocationListener() {
            @Override
            public void onLocation() {
                //这里模拟定位 两秒后给个随便的定位数据
                citySelectView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        citySelectView.reBindCurrentCity(new DataModel("广州", "10000001"));
                    }
                }, 2000);
            }
        });
    }
}
