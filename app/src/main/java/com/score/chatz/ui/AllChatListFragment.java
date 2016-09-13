package com.score.chatz.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.score.chatz.R;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.exceptions.NoUserException;
import com.score.chatz.pojo.Secret;
import com.score.chatz.pojo.UserPermission;
import com.score.chatz.utils.PreferenceUtils;
import com.score.chatz.utils.SecretsUtil;
import com.score.senzc.pojos.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lakmalcaldera on 8/19/16.
 */
public class AllChatListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private static final String TAG = AllChatListFragment.class.getName();
    private ArrayList<Secret> allSecretsList;
    private AllChatListAdapter adapter;
    private User currentUser;
    SenzorsDbSource dbSource;

    public AllChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            currentUser = PreferenceUtils.getUser(this.getContext());
        } catch (NoUserException ex) {
            Log.e(TAG, "No user Found.");
        }
        dbSource = new SenzorsDbSource(getContext());
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.all_chat_messages_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getContext().registerReceiver(userSharedReceiver, new IntentFilter("com.score.chatz.USER_UPDATE"));
        getListView().setOnItemClickListener(this);
    }

    private BroadcastReceiver userSharedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got new user from Senz service");
            handleSharedUser(intent);
        }
    };

    private void handleSharedUser(Intent intent) {
        displayUserList();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayUserList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(userSharedReceiver);
    }

    /**
     * Display sensor list
     * Basically setup list adapter if have items to display otherwise display empty view
     */
    private void displayUserList() {

        allSecretsList = (ArrayList<Secret>) dbSource.getAllOtherSercets(currentUser);
        adapter = new AllChatListAdapter(getContext(), allSecretsList);
        getListView().setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //removeOldItemsFromChat();
    }

    private void removeOldItemsFromChat() {
        Iterator<Secret> iter = allSecretsList.iterator();
        while (iter.hasNext()) {
            Secret secret = iter.next();

            if (!SecretsUtil.isSecretToBeShown(secret)) {
                iter.remove();
                dbSource.deleteSecret(secret);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this.getActivity(), ChatActivity.class);
        intent.putExtra("SENDER", allSecretsList.get(position).getSender().getUsername());
        startActivity(intent);
    }


}
