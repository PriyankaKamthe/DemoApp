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
import com.cazaayan.tenzfree.Engineer.Services.AsyncGetAllTask_ServiceFragment_Engineer;
import com.cazaayan.tenzfree.Engineer.Views.DividerItemDecoration;
import com.cazaayan.tenzfree.R;
import com.cazaayan.tenzfree.Utilities.Utils;


/**
 * Created by Priyanka on 09-10-2015.
 */
public class Fragment_ServiceRequests extends Fragment {
    RecyclerView mServiceRequestRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    RecyclerViewAdapter_ServiceRequest mRecyclerAdapter;
    TextView mNoData;


    public Fragment_ServiceRequests() { }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_requests_engineer, container, false);
        mServiceRequestRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_serviceRequest);
        mNoData = (TextView) view.findViewById(R.id.noData);
        mNoData.setVisibility(View.VISIBLE);
        mServiceRequestRecyclerView.setVisibility(View.GONE);

        mServiceRequestRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mLayoutManager = new LinearLayoutManager(getActivity());
        mServiceRequestRecyclerView.setLayoutManager(mLayoutManager);

        if(!Utils.isInternetConnected()){
            Utils.showInternetConnectionDialog(getActivity());
        }
        else {
            new AsyncGetAllTask_ServiceFragment_Engineer(getActivity(), mServiceRequestRecyclerView, mNoData).execute();
        }
        return view;
    }
}
