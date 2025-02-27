package com.xs.widget.view;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xs.widget.R;
import com.xs.widget.adapter.MainAdapter;
import com.xs.widget.callback.OnCitySelectListener;
import com.xs.widget.callback.OnItemClickListener;
import com.xs.widget.callback.OnLocationListener;
import com.xs.widget.item.CustomItemDecoration;
import com.xs.widget.model.DataInfoModel;
import com.xs.widget.model.DataModel;
import com.github.stuxuhai.jpinyin.ChineseHelper;
import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @date: on 2019-10-30
 * @author: a112233
 * @email: mxnzp_life@163.com
 * @desc: 选择城市的View
 */
public class CitySelectView extends ConstraintLayout {
    /**
     * views
     */
    private EditText edSearch;
    private TextView tvCancel;
    private RecyclerView mainRecyclerView;
    private FastIndexView indexSideView;
    private TextView indexView;

    //data and model
    /**
     * 主要用于展示数据的list
     */
    private List<DataInfoModel> list;
    /**
     * 第一次加载之后缓存的数据
     */
    private List<DataInfoModel> cacheList;
    /**
     * 用于存储搜索结果的list
     */
    private List<DataInfoModel> searchList;

    /**
     * 页面recyclerview的适配器
     */
    private MainAdapter mainAdapter;
    /**
     * 布局管理器
     */
    private LinearLayoutManager layoutManager;

    private Context mContext;

    /**
     * 定时器
     */
    private Timer timer;
    /**
     * 定时任务
     */
    private TimerTask timerTask;

    private OnCitySelectListener citySelectListener;

    private OnLocationListener locationListener;

    /**
     * 记录是否绑定过数据
     */
    private boolean hasBindData = false;

    public CitySelectView(Context context) {
        this(context, null, 0);
    }

    public CitySelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CitySelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
        View.inflate(context, R.layout.layout_city_select_view, this);

        //读取xml属性 todo
        initView();
        initAdapter();
        initListener();
    }

    private void initView() {
        edSearch = findViewById(R.id.ed_search);
        tvCancel = findViewById(R.id.tv_cancel);
        mainRecyclerView = findViewById(R.id.recyclerView);
        indexSideView = findViewById(R.id.fastIndexView);
        indexView = findViewById(R.id.tv_index);
    }

    private void initAdapter() {
        list = new ArrayList<>();
        cacheList = new ArrayList<>();
        searchList = new ArrayList<>();

        layoutManager = new LinearLayoutManager(mContext);
        mainRecyclerView.setLayoutManager(layoutManager);
        mainRecyclerView.addItemDecoration(new CustomItemDecoration(mContext, new CustomItemDecoration.TitleDecorationCallback() {
            @Override
            public String getGroupId(int position) {
                //这个是用来比较是否是同一组数据的
                return list.get(position).getSortId();
            }

            @Override
            public String getGroupName(int position) {
                DataInfoModel dataInfoModel = list.get(position);
                if (dataInfoModel.getType() == DataInfoModel.TYPE_CURRENT || dataInfoModel.getType() == DataInfoModel.TYPE_HOT) {
                    return dataInfoModel.getSortName();
                }
                //拼音都是小写的
                return dataInfoModel.getSortId().toUpperCase();
            }
        }));
        mainAdapter = new MainAdapter(mContext, list);
        //设置item的点击事件
        mainAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(DataInfoModel dataInfoModel) {
                if (citySelectListener != null) {
                    citySelectListener.onCitySelect(new DataModel(dataInfoModel.getCityName(), dataInfoModel.getExtra()));
                }
            }
        });
        //设置定位的点击时间
        mainAdapter.setLocationListener(new OnLocationListener() {
            @Override
            public void onLocation() {
                if (locationListener != null) {
                    locationListener.onLocation();
                }
            }
        });
        mainRecyclerView.setAdapter(mainAdapter);
    }

    private void initListener() {
        indexSideView.setListener(new FastIndexView.OnLetterUpdateListener() {
            @Override
            public void onLetterUpdate(String letter) {
                indexView.setText(letter);
                indexView.setVisibility(View.VISIBLE);

                moveToLetterPosition(letter);

                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }

                if (timerTask != null) {
                    timerTask.cancel();
                    timerTask = null;
                }
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                indexView.setVisibility(View.GONE);
                            }
                        });
                    }
                };
                timer = new Timer();
                timer.schedule(timerTask, 500);
            }
        });

        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tvCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (citySelectListener != null) {
                    citySelectListener.onSelectCancel();
                }
            }
        });
    }

    /**
     * 设置搜索输入框的提示文案
     *
     * @param tips
     */
    public void setSearchTips(String tips) {
        edSearch.setHint(tips);
    }

    /**
     * 给View绑定数据
     *
     * @param allCity     所有城市列表
     * @param hotCity     热门城市列表
     * @param currentCity 当前城市
     */
    public void bindData(List<DataModel> allCity, List<DataModel> hotCity, DataModel currentCity) {
        if (allCity != null) {
            for (DataModel dataModel : allCity) {
                try {
                    String pingYin = PinyinHelper.convertToPinyinString(dataModel.getDataName(), " ", PinyinFormat.WITHOUT_TONE);
                    cacheList.add(new DataInfoModel(DataInfoModel.TYPE_NORMAL, dataModel.getDataName(), pingYin.substring(0, 1), pingYin, dataModel.getExtra()));
                } catch (PinyinException e) {
                    e.printStackTrace();
                }
            }
            //排序
            Collections.sort(cacheList, new Comparator<DataInfoModel>() {
                @Override
                public int compare(DataInfoModel o1, DataInfoModel o2) {
                    return o1.getSortName().compareTo(o2.getSortName());
                }
            });

            if (hotCity != null) {
                List<DataInfoModel> hotList = new ArrayList<>();
                for (DataModel dataModel : hotCity) {
                    hotList.add(new DataInfoModel(0, dataModel.getDataName(), "", "", dataModel.getExtra()));
                }

                mainAdapter.bindHotCity(hotList);
                cacheList.add(0, new DataInfoModel(DataInfoModel.TYPE_HOT, "", "#", "热门城市", "hot"));
            }

            if (currentCity != null) {
                cacheList.add(0, new DataInfoModel(DataInfoModel.TYPE_CURRENT, currentCity.getDataName(), "*", "当前定位城市", currentCity.getExtra()));
            }

            this.list.clear();
            this.list.addAll(cacheList);
            mainAdapter.notifyDataSetChanged();

            hasBindData = true;
        }
    }

    /**
     * 重新绑定当前城市
     *
     * @param currentCity
     */
    public void reBindCurrentCity(DataModel currentCity) {
        if (!hasBindData) {
            throw new RuntimeException("请先绑定数据再调用重新绑定当前城市的方法");
        }
        for (DataInfoModel dataInfoModel : cacheList) {
            if (dataInfoModel.getType() == DataInfoModel.TYPE_CURRENT) {
                //有 找到了
                dataInfoModel.setCityName(currentCity.getDataName());
                dataInfoModel.setExtra(currentCity.getExtra());
                mainAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    //滚动recyclerview
    private void moveToLetterPosition(String letter) {
        //这里主要是为了跳转到最顶端
        if ("#".equals(letter)) {
            letter = "*";
        }
        for (int i = 0; i < list.size(); i++) {
            DataInfoModel dataInfoModel = list.get(i);
            if (dataInfoModel.getSortId().toUpperCase().equals(letter)) {
                layoutManager.scrollToPositionWithOffset(i, 0);
                return;
            }
        }
    }

    //执行搜索
    private void search(String key) {
        searchList.clear();
        boolean isChiness = ChineseHelper.containsChinese(key);
        if (isChiness) {
            for (DataInfoModel dataInfoModel : cacheList) {
                boolean has = true;
                HH:
                for (char c : key.toCharArray()) {
                    if (!dataInfoModel.getCityName().contains(c + "")) {
                        has = false;
                        break HH;
                    }
                }
                if (has) {
                    searchList.add(dataInfoModel);
                }
            }
        } else {
            for (DataInfoModel dataInfoModel : cacheList) {
                boolean has = true;
                HH:
                for (char c : key.toCharArray()) {
                    if (!dataInfoModel.getSortName().contains(c + "")) {
                        has = false;
                        break HH;
                    }
                }
                if (has) {
                    searchList.add(dataInfoModel);
                }
            }
        }
        list.clear();
        list.addAll(searchList);
        mainAdapter.notifyDataSetChanged();
    }

    public void setOnCitySelectListener(OnCitySelectListener listener) {
        this.citySelectListener = listener;
    }

    public void setOnLocationListener(OnLocationListener locationListener) {
        this.locationListener = locationListener;
    }
}
