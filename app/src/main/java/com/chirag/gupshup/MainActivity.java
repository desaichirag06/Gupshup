package com.chirag.gupshup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.chirag.gupshup.chats.ChatFragment;
import com.chirag.gupshup.common.NodeNames;
import com.chirag.gupshup.databinding.ActivityMainBinding;
import com.chirag.gupshup.findFriends.FindFriendsFragment;
import com.chirag.gupshup.friendRequests.RequestsFragment;
import com.chirag.gupshup.profile.ProfileActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS).child(firebaseAuth.getCurrentUser().getUid());

        databaseReferenceUsers.child(NodeNames.ONLINE_STATUS).setValue(true);
        databaseReferenceUsers.child(NodeNames.ONLINE_STATUS).onDisconnect().setValue(false);

        setViewPager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mnuProfile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    class Adapter extends FragmentStatePagerAdapter {

        public Adapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ChatFragment();

                case 1:
                    return new RequestsFragment();
                case 2:
                    return new FindFriendsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return mBinding.tabMain.getTabCount();
        }
    }

    private void setViewPager() {
        mBinding.tabMain.addTab(mBinding.tabMain.newTab().setCustomView(R.layout.tab_chat));
        mBinding.tabMain.addTab(mBinding.tabMain.newTab().setCustomView(R.layout.tab_request));
        mBinding.tabMain.addTab(mBinding.tabMain.newTab().setCustomView(R.layout.tab_find_friends));
        mBinding.tabMain.setTabGravity(TabLayout.GRAVITY_FILL);

        Adapter adapter = new Adapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mBinding.vpMain.setAdapter(adapter);

        mBinding.tabMain.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mBinding.vpMain.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mBinding.vpMain.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mBinding.tabMain));
    }

    private boolean doubleBackPressed = false;

    @Override
    public void onBackPressed() {
        if (mBinding.tabMain.getSelectedTabPosition() > 0) {
            mBinding.tabMain.selectTab(mBinding.tabMain.getTabAt(0));
        } else {
            if (doubleBackPressed) {
                finishAffinity();
            } else {
                doubleBackPressed = true;
                Toast.makeText(this, R.string.press_back_to_exit, Toast.LENGTH_SHORT).show();
                //delay
                Handler handler = new Handler();
                handler.postDelayed(() -> doubleBackPressed = false, 2000);
            }
        }
    }
}