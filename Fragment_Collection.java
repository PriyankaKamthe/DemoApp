package com.cazaayan.tenzfree.Engineer.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cazaayan.tenzfree.Engineer.Adapter.RecyclerViewAdapter_ServiceRequest;
import com.cazaayan.tenzfree.Engineer.Model.EngineerNotificationAllData;
import com.cazaayan.tenzfree.Engineer.Services.AsyncGetAllTask_Collections_Engineer;
import com.cazaayan.tenzfree.Engineer.Views.DividerItemDecoration;
import com.cazaayan.tenzfree.R;
import com.cazaayan.tenzfree.Utilities.Utils;

import java.util.ArrayList;


/**
 * Created by Priyanka on 09-10-2015.
 */
public class Fragment_Collection extends Fragment {
    RecyclerView mCollectionsRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    RecyclerViewAdapter_ServiceRequest mRecyclerAdapter;
    TextView mNoData;
    ArrayList<String> array_CustomerName = new ArrayList<String>();

    public Fragment_Collection() {
        array_CustomerName = EngineerNotificationAllData.getInstance().getArray_CustomerName();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection_engineer, container, false);

        mCollectionsRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_collection_engineer);
        mNoData = (TextView) view.findViewById(R.id.noData_collection);
        mNoData.setVisibility(View.VISIBLE);
        mCollectionsRecyclerView.setVisibility(View.GONE);

        mCollectionsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(getActivity());
        mCollectionsRecyclerView.setLayoutManager(mLayoutManager);

        if(!Utils.isInternetConnected()){
            Utils.showInternetConnectionDialog(getActivity());
        }else {
            new AsyncGetAllTask_Collections_Engineer(getActivity(), mCollectionsRecyclerView, mNoData).execute();
        }

        return view;
    }
}
