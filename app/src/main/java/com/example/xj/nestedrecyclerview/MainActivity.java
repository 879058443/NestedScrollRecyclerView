package com.example.xj.nestedrecyclerview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.xj.nestedrecyclerview.module.home.bean.HomeTopBean;
import com.example.xj.nestedrecyclerview.module.home.itemview.BottomItemView;
import com.example.xj.nestedrecyclerview.module.home.itemview.HeaderItemView;
import com.example.xj.nestedrecyclerview.module.home.itemview.TopItemView;
import com.example.xj.nestedrecyclerview.widget.behavior.HomeContentBehavior;
import com.example.xj.nestedrecyclerview.widget.behavior.HomeHeaderBehavior;
import com.example.xj.nestedrecyclerview.widget.recyclerview.RecyclerViewAdapter;
import com.example.xj.nestedrecyclerview.widget.recyclerview.adapter.MultiItemTypeAdapter;
import com.example.xj.nestedrecyclerview.widget.recyclerview.bean.RecyclerViewBean;
import com.example.xj.nestedrecyclerview.widget.refresh.HomeSmartRefreshFooter;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.ele.uetool.UETool;

public class MainActivity extends Activity implements View.OnClickListener {

    private RecyclerView mRecyclerViewHeader, mRecyclerViewContent;
    private RelativeLayout rl_home_header, rl_home_content;
    private SmartRefreshLayout srlHeader, srlContent;

    private HomeHeaderBehavior mHomeHeaderBehavior;
    private HomeContentBehavior mHomeContentBehavior;
    private RecyclerViewAdapter mHeaderRecyclerViewAdapter, mContentRecyclerViewAdapter;

    private List<RecyclerViewBean> headerList, contentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UETool.showUETMenu(1000);

        initView();
        initData();
    }

    @Override
    public void onBackPressed() {
        if (mHomeHeaderBehavior.isClosed()) {
            backToTop();
            return;
        }
        super.onBackPressed();
    }

    private void initView() {
        mRecyclerViewHeader = findViewById(R.id.recycler_home_header);
        mRecyclerViewContent = findViewById(R.id.recycler_home_content);
        srlHeader = findViewById(R.id.srl_home_header);
        srlContent = findViewById(R.id.srl_home_content);

        rl_home_header = findViewById(R.id.rl_home_header);
        rl_home_content = findViewById(R.id.rl_home_content);

        MaterialHeader materialHeader = new MaterialHeader(this);
        materialHeader.setColorSchemeColors(Color.parseColor("#ff98bf"));
        srlHeader.setRefreshHeader(materialHeader);
        srlHeader.setRefreshFooter(new HomeSmartRefreshFooter(this));
        //需要禁用掉顶部下拉刷新控件的加载更多
        srlContent.setEnableRefresh(false);
        srlHeader.setEnableLoadMore(false);

        srlHeader.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                requestData();
            }
        });

        headerList = new ArrayList<>();
        contentList = new ArrayList<>();

        MultiItemTypeAdapter headerAdapter = new MultiItemTypeAdapter(this, headerList);
        headerAdapter.addItemViewDelegate(new TopItemView(this));
        headerAdapter.addItemViewDelegate(new HeaderItemView(this));
        mHeaderRecyclerViewAdapter = new RecyclerViewAdapter(headerAdapter);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        mRecyclerViewHeader.setLayoutManager(layoutManager1);
        mRecyclerViewHeader.setAdapter(mHeaderRecyclerViewAdapter);

        CoordinatorLayout.LayoutParams headerParams = (CoordinatorLayout.LayoutParams) rl_home_header.getLayoutParams();
        mHomeHeaderBehavior = (HomeHeaderBehavior) headerParams.getBehavior();
        mHomeHeaderBehavior.setPagerStateListener(new HomeHeaderBehavior.OnPagerStateListener() {
            @Override
            public void onPagerClosed() {
                srlContent.setEnableRefresh(true);
            }

            @Override
            public void onPagerOpened() {
                srlContent.setEnableRefresh(false);
            }
        });


        MultiItemTypeAdapter contentAdapter = new MultiItemTypeAdapter(this, contentList);
        contentAdapter.addItemViewDelegate(new BottomItemView(this));
        mContentRecyclerViewAdapter = new RecyclerViewAdapter(contentAdapter);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        mRecyclerViewContent.setLayoutManager(layoutManager2);
        mRecyclerViewContent.setAdapter(mContentRecyclerViewAdapter);
    }

    private void initData() {
        for (int i = 0; i < 10; i++) {
            RecyclerViewBean recyclerViewBean = new RecyclerViewBean();
            recyclerViewBean.setType(RecyclerViewBean.TYPE_TOP_ITEM);
            recyclerViewBean.setData(new HomeTopBean());
            headerList.add(recyclerViewBean);
        }

        //添加一个标题
        RecyclerViewBean titleBean = new RecyclerViewBean();
        titleBean.setType(RecyclerViewBean.TYPE_HEADER_ITEM);
        headerList.add(titleBean);

        for (int i = 0; i < 20; i++) {
            RecyclerViewBean recyclerViewBean = new RecyclerViewBean();
            recyclerViewBean.setType(RecyclerViewBean.TYPE_BOTTOM_ITEM);
            recyclerViewBean.setData(new HomeTopBean());
            contentList.add(recyclerViewBean);
        }

        mHeaderRecyclerViewAdapter.notifyDataSetChanged();
        mContentRecyclerViewAdapter.notifyDataSetChanged();
    }

    @SuppressLint("CheckResult")
    private void requestData() {
        Observable.timer(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Toast.makeText(MainActivity.this, "头部数据刷新完成", Toast.LENGTH_SHORT).show();
                        srlHeader.finishRefresh();
                    }
                });
    }

    /**
     * 回到列表头部
     */
    private void backToTop() {
        //暂停加载更多
        srlContent.finishLoadMore();

        mRecyclerViewContent.stopScroll();
        mRecyclerViewContent.scrollToPosition(0);
        mHomeHeaderBehavior.openPager();
        mRecyclerViewHeader.scrollToPosition(0);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_home_locaiton_back:
                backToTop();
                break;
            default:
                break;
        }
    }
}