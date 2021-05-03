package com.flytbase.madscalculator.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.flytbase.madscalculator.MainActivity;
import com.flytbase.madscalculator.R;
import com.flytbase.madscalculator.adapter.HistoryAdapter;
import com.flytbase.madscalculator.databinding.FragmentHistoryBinding;
import com.flytbase.madscalculator.model.CalculationHistory;
import com.flytbase.madscalculator.utility.SharedPrefsUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NavigationDrawerFragment extends Fragment {
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private FirebaseUser currentUser;
    private String userId;
    private FragmentHistoryBinding fragmentHistoryBinding;
    private HistoryAdapter historyAdapter;
    private ArrayList<CalculationHistory> historyList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentHistoryBinding = FragmentHistoryBinding.inflate(inflater);

        historyList = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userId = currentUser.getUid();
            fetchFirestoreHistory(historyList);
        } else {
            if (getActivity() != null) {
                fetchPrefs(SharedPrefsUtil.loadData(getActivity()));
            }
        }
        return fragmentHistoryBinding.getRoot();
    }

    private void fetchFirestoreHistory(ArrayList<CalculationHistory> list) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .collection("history")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            error.printStackTrace();
                            return;
                        }
                        list.clear();
                        if (value != null) {
                            for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                                CalculationHistory history = documentSnapshot.toObject(CalculationHistory.class);
                                list.add(history);
                            }
                            if (list.size() > 10) {
                                list.subList(0, list.size() - 10).clear();
                            }
                            fragmentHistoryBinding.rvCalculationsHistory.setLayoutManager(new LinearLayoutManager(getActivity(),
                                    LinearLayoutManager.VERTICAL, false));
                            historyAdapter = new HistoryAdapter(list, getActivity(), expression -> {
                                if (getActivity() != null) {
                                    ((MainActivity) getActivity()).setDisplayExpression(expression);
                                }
                            });
                            fragmentHistoryBinding.rvCalculationsHistory.setAdapter(historyAdapter);
                            historyAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void fetchPrefs(ArrayList<CalculationHistory> list) {
        if (getActivity() != null) {
            SharedPrefsUtil.loadData(getActivity());
            fragmentHistoryBinding.rvCalculationsHistory.setLayoutManager(new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL, false));
            historyAdapter = new HistoryAdapter(list, getActivity(), expression -> {
                ((MainActivity) getActivity()).setDisplayExpression(expression);
            });
            fragmentHistoryBinding.rvCalculationsHistory.setAdapter(historyAdapter);
            historyAdapter.notifyDataSetChanged();
        }
    }

    public void setUpDrawer(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDrawerLayout.openDrawer(GravityCompat.START);
                if (currentUser != null) {
                    userId = currentUser.getUid();
                    fetchFirestoreHistory(historyList);
                } else {
                    if (getActivity() != null) {
                        fetchPrefs(SharedPrefsUtil.loadData(getActivity()));
                    }
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // sync the state of Navigation Drawer with the help of Runnable
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }
}
