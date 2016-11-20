package com.score.chatz.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.score.chatz.R;
import com.score.chatz.application.IntentProvider;
import com.score.chatz.db.SenzorsDbSource;
import com.score.chatz.enums.IntentType;
import com.score.chatz.pojo.SecretUser;
import com.score.chatz.utils.ActivityUtils;
import com.score.chatz.utils.PhoneUtils;
import com.score.senzc.enums.SenzTypeEnum;
import com.score.senzc.pojos.Senz;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private static final String TAG = FriendListFragment.class.getName();

    private ArrayList<SecretUser> friendsList;
    private FriendListAdapter adapter;

    private Typeface typeface;

    private BroadcastReceiver senzReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Got new user from Senz service");

            if (intent.hasExtra("SENZ")) {
                Senz senz = intent.getExtras().getParcelable("SENZ");
                if (senz.getSenzType() == SenzTypeEnum.SHARE) {
                    displayUserList();
                } else if (senz.getSenzType() == SenzTypeEnum.DATA && senz.getAttributes().containsKey("status") && senz.getAttributes().get("status").equalsIgnoreCase("701")) {
                    // New user added to list via user action after an sms
                    ActivityUtils.cancelProgressDialog();
                    displayUserList();
                }
            } else if (intent.hasExtra("UPDATE_UI_ON_NEW_ADDED_USER")) {
                displayUserList();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.recent_friend_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/GeosansLight.ttf");
        setupEmptyTextFont();
    }

    @Override
    public void onResume() {
        super.onResume();
        displayUserList();

        getActivity().registerReceiver(senzReceiver, IntentProvider.getIntentFilter(IntentType.SENZ));
    }

    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(senzReceiver);
    }

    private void setupEmptyTextFont() {
        ((TextView) getActivity().findViewById(R.id.empty_view_friend)).setTypeface(typeface);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (friendsList.get(position).isActive()) {
            Intent intent = new Intent(this.getActivity(), ChatActivity.class);
            intent.putExtra("SENDER", friendsList.get(position).getUsername());
            startActivity(intent);
        } else {
            if (friendsList.get(position).isSMSRequester()) {
                String contactName = PhoneUtils.getDisplayNameFromNumber(friendsList.get(position).getPhone(), getActivity());
                ActivityUtils.displayConfirmationMessageDialog("Confirm", "Would you like to resend friend request to " + contactName + "?", getActivity(), typeface, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Start sharing again
                    }
                });
            } else {
                ActivityUtils.displayConfirmationMessageDialog("Confirm", "Response taking too long? Would you like to retry?", getActivity(), typeface, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Start getting public key and sending confirmation sms
                    }
                });
            }
        }
    }

    /**
     * Display sensor list
     * Basically setup list adapter if have items to display otherwise display empty view
     */

    private void displayUserList() {
        // get User from db
        friendsList = new SenzorsDbSource(this.getActivity()).getSecretUserList();
        // construct list adapter
        if (friendsList.size() > 0) {
            adapter = new FriendListAdapter(getContext(), friendsList);
            adapter.notifyDataSetChanged();
            getListView().setAdapter(adapter);
        } else {
            adapter = new FriendListAdapter(getContext(), friendsList);
            getListView().setAdapter(adapter);
        }
    }
}
