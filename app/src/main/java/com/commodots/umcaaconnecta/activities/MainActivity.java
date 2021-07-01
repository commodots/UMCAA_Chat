package com.commodots.umcaaconnecta.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mxn.soul.flowingdrawer_core.ElasticDrawer;
import com.mxn.soul.flowingdrawer_core.FlowingDrawer;
import com.sinch.android.rtc.calling.Call;
import com.commodots.umcaaconnecta.R;
import com.commodots.umcaaconnecta.adapters.ViewPagerAdapter;
import com.commodots.umcaaconnecta.fragments.GroupCreateDialogFragment;
import com.commodots.umcaaconnecta.fragments.MyCallsFragment;
import com.commodots.umcaaconnecta.fragments.MyGroupsFragment;
import com.commodots.umcaaconnecta.fragments.MyUsersFragment;
import com.commodots.umcaaconnecta.fragments.OptionsFragment;
import com.commodots.umcaaconnecta.fragments.UserSelectDialogFragment;
import com.commodots.umcaaconnecta.interfaces.ChatItemClickListener;
import com.commodots.umcaaconnecta.interfaces.ContextualModeInteractor;
import com.commodots.umcaaconnecta.interfaces.HomeIneractor;
import com.commodots.umcaaconnecta.interfaces.UserGroupSelectionDismissListener;
import com.commodots.umcaaconnecta.models.Contact;
import com.commodots.umcaaconnecta.models.Group;
import com.commodots.umcaaconnecta.models.Message;
import com.commodots.umcaaconnecta.models.User;
import com.commodots.umcaaconnecta.services.FetchMyUsersService;
import com.commodots.umcaaconnecta.utils.ConfirmationDialogFragment;
import com.commodots.umcaaconnecta.utils.Helper;
import com.commodots.umcaaconnecta.views.SwipeControlViewPager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.realm.RealmResults;

public class MainActivity extends BaseActivity implements HomeIneractor, ChatItemClickListener, View.OnClickListener, ContextualModeInteractor, UserGroupSelectionDismissListener {
    private static final int REQUEST_CODE_CHAT_FORWARD = 99;
    private final int CONTACTS_REQUEST_CODE = 321;
    private static String USER_SELECT_TAG = "userselectdialog";
    private static String OPTIONS_MORE = "optionsmore";
    private static String GROUP_CREATE_TAG = "groupcreatedialog";
    private static String CONFIRM_TAG = "confirmtag";

    private ImageView usersImage, dialogUserImage;
    private RecyclerView menuRecyclerView;
    private SwipeRefreshLayout swipeMenuRecyclerView;
    private DrawerLayout drawerLayout;
    private EditText searchContact;
    private TextView selectedCount;
    private RelativeLayout toolbarContainer, cabContainer;

    private HashMap<String, Contact> contactsData;

    private TabLayout tabLayout;
    private SwipeControlViewPager viewPager;

    private FloatingActionButton floatingActionButton;
    private CoordinatorLayout coordinatorLayout;

    private ArrayList<User> myUsers = new ArrayList<>();
    private ArrayList<Group> myGroups = new ArrayList<>();
    private ArrayList<Message> messageForwardList = new ArrayList<>();
    private UserSelectDialogFragment userSelectDialogFragment;
    private ViewPagerAdapter adapter;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();

        contactsData = helper.getMyUsersNameCache();

        //If its a url then load it, else Make a text drawable of user's name
        setProfileImage(usersImage);
        usersImage.setOnClickListener(this);
        //invite.setOnClickListener(this);
        findViewById(R.id.action_delete).setOnClickListener(this);
        floatingActionButton.setOnClickListener(this);
        floatingActionButton.setVisibility(View.VISIBLE);

        setupViewPager();

        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        }

        markOnline(true);
        updateFcmToken();

        loadAds();
    }

    private void initUi() {
        usersImage = findViewById(R.id.users_image);
        menuRecyclerView = findViewById(R.id.menu_recycler_view);
        swipeMenuRecyclerView = findViewById(R.id.menu_recycler_view_swipe_refresh);
        drawerLayout = findViewById(R.id.drawer_layout);
        searchContact = findViewById(R.id.searchContact);
        //invite = findViewById(R.id.invite);
        toolbarContainer = findViewById(R.id.toolbarContainer);
        cabContainer = findViewById(R.id.cabContainer);
        selectedCount = findViewById(R.id.selectedCount);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        floatingActionButton = findViewById(R.id.addConversation);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
    }

    private void updateFcmToken() {
        fcmIdRef.child(userMe.getId()).setValue(FirebaseInstanceId.getInstance().getToken());
    }

    private void loadAds() {
        AdView mAdView = findViewById(R.id.adView);

        String admobAppId = getString(R.string.admob_app_id);
        String admobBannerId = getString(R.string.admob_banner_id);
        if (TextUtils.isEmpty(admobAppId) || TextUtils.isEmpty(admobBannerId)) {
            mAdView.setVisibility(View.GONE);
        } else {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    private void setupViewPager() {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new MyUsersFragment(), getString(R.string.tab_title_chat));
        adapter.addFrag(new MyGroupsFragment(), getString(R.string.tab_title_group));
        adapter.addFrag(new MyCallsFragment(), getString(R.string.tab_title_call));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setProfileImage(ImageView imageView) {
        if (userMe != null)
            Glide.with(this).load(userMe.getImage()).apply(new RequestOptions().placeholder(R.drawable.avatar)).into(imageView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CONTACTS_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(this, Contact_activity.class));
        }

    }


    private void refreshMyContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            if (!FetchMyUsersService.STARTED) {
                if (!swipeMenuRecyclerView.isRefreshing())
                    swipeMenuRecyclerView.setRefreshing(true);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                FetchMyUsersService.startMyUsersService(MainActivity.this, userMe.getId(), idToken);
                            }
                        }
                    });
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        markOnline(false);
    }

    @Override
    public void onBackPressed() {
        if (isContextualMode()) {
            disableContextualMode();
        } else if (viewPager.getCurrentItem() != 0) {
            viewPager.post(() -> viewPager.setCurrentItem(0));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (REQUEST_CODE_CHAT_FORWARD):
                if (resultCode == Activity.RESULT_OK) {
                    //show forward dialog to choose users
                    messageForwardList.clear();
                    ArrayList<Message> temp = data.getParcelableArrayListExtra("FORWARD_LIST");
                    messageForwardList.addAll(temp);
                    userSelectDialogFragment = UserSelectDialogFragment.newInstance(this, myUsers);
                    FragmentManager manager = getSupportFragmentManager();
                    Fragment frag = manager.findFragmentByTag(USER_SELECT_TAG);
                    if (frag != null) {
                        manager.beginTransaction().remove(frag).commit();
                    }
                    userSelectDialogFragment.show(manager, USER_SELECT_TAG);
                }
                break;
        }
    }

    private void sortMyGroupsByName() {
        Collections.sort(myGroups, (group1, group2) -> group1.getName().compareToIgnoreCase(group2.getName()));
    }


    @Override
    void userAdded(User value) {
        if (value.getId().equals(userMe.getId()))
            return;
        int existingPos = myUsers.indexOf(value);
        if (existingPos != -1) {
            myUsers.remove(existingPos);
        }
        myUsers.add(0, value);
        refreshUsers(-1);
    }

    @Override
    void groupAdded(Group group) {
        if (!myGroups.contains(group)) {
            myGroups.add(group);
            sortMyGroupsByName();
        }
    }

    @Override
    void userUpdated(User value) {
        if (value.getId().equals(userMe.getId())) {
            userMe = helper.getLoggedInUser();
            setProfileImage(usersImage);
            FragmentManager manager = getSupportFragmentManager();
            Fragment frag = manager.findFragmentByTag(OPTIONS_MORE);
            if (frag != null) {
                ((OptionsFragment) frag).setUserDetails();
            }
        } else {
            int existingPos = myUsers.indexOf(value);
            if (existingPos != -1) {
                myUsers.set(existingPos, value);
                refreshUsers(existingPos);
            }
        }
    }

    @Override
    void groupUpdated(Group group) {
        int existingPos = myGroups.indexOf(group);
        if (existingPos != -1) {
            myGroups.set(existingPos, group);
        }
    }

    @Override
    void onSinchConnected() {

    }

    @Override
    void onSinchDisconnected() {

    }

    @Override
    public void onChatItemClick(String chatId, String chatName, int position, View userImage) {
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        } else {
            openChat(ChatActivity.newIntent(this, messageForwardList, chatId, chatName), userImage);
        }
    }

    @Override
    public void onChatItemClick(Group group, int position, View userImage) {
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        } else {
            openChat(ChatActivity.newIntent(this, messageForwardList, group), userImage);
        }
    }

    private void openChat(Intent intent, View userImage) {
        if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, CONTACTS_REQUEST_CODE);
        } else {
            if (userImage == null) {
                userImage = usersImage;
            }

            if (Build.VERSION.SDK_INT > 21) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, userImage, "backImage");
                startActivityForResult(intent, REQUEST_CODE_CHAT_FORWARD, options.toBundle());
            } else {
                startActivityForResult(intent, REQUEST_CODE_CHAT_FORWARD);
                overridePendingTransition(0, 0);
            }

            if (userSelectDialogFragment != null)
                userSelectDialogFragment.dismiss();
        }
    }

    private void refreshUsers(int pos) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(USER_SELECT_TAG);
        if (frag != null) {
            userSelectDialogFragment.refreshUsers(pos);
        }
    }

    private void markOnline(boolean b) {
        //Mark online boolean as b in firebase
        usersRef.child(userMe.getId()).child("online").setValue(b);
        usersRef.child(userMe.getId()).child("time").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addConversation:
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        startActivity(new Intent(this, Contact_activity.class));
                        break;
                    case 1:
                        if (myUsers.isEmpty()) {
                            try {
                                refreshMyContacts();
                            }catch (Exception ignored){}
                        }
                        GroupCreateDialogFragment.newInstance(this, userMe, myUsers).show(getSupportFragmentManager(), GROUP_CREATE_TAG);
                        break;
                    case 2:
                        startActivity(new Intent(this, Contact_activity.class));
                        break;
                }
                break;
            case R.id.users_image:
                if (userMe != null)
                    OptionsFragment.newInstance(getSinchServiceInterface()).show(getSupportFragmentManager(), OPTIONS_MORE);
                break;
            case R.id.action_delete:
                FragmentManager manager = getSupportFragmentManager();
                Fragment frag = manager.findFragmentByTag(CONFIRM_TAG);
                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit();
                }

                ConfirmationDialogFragment confirmationDialogFragment = ConfirmationDialogFragment.newInstance(getString(R.string.delete_chat_title),
                        getString(R.string.delete_chat_message),
                        view -> {
                            ((MyUsersFragment) adapter.getItem(0)).deleteSelectedChats();
                            ((MyGroupsFragment) adapter.getItem(1)).deleteSelectedChats();
                            disableContextualMode();
                        },
                        view -> disableContextualMode());
                confirmationDialogFragment.show(manager, CONFIRM_TAG);
                break;
        }
    }

    @Override
    public void placeCall(boolean callIsVideo, User user) {
        if (permissionsAvailable(permissionsSinch)) {
            try {
                Call call = callIsVideo ? getSinchServiceInterface().callUserVideo(user.getId()) : getSinchServiceInterface().callUser(user.getId());
                if (call == null) {
                    // Service failed for some reason, show a Toast and abort
                    Toast.makeText(this, "Service is not started. Try stopping the service and starting it again before placing a call.", Toast.LENGTH_LONG).show();
                    return;
                }
                String callId = call.getCallId();
                startActivity(CallScreenActivity.newIntent(this, user, callId, "OUT"));
            } catch (Exception e) {
                Log.e("CHECK", e.getMessage());
                //ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissionsSinch, 69);
        }
    }

    @Override
    public void onUserGroupSelectDialogDismiss() {
        messageForwardList.clear();
    }

    @Override
    public void selectionDismissed() {
        //do nothing..
    }

    @Override
    public void myUsersResult(ArrayList<User> myUsers) {
        this.myUsers.clear();
        this.myUsers.addAll(myUsers);

        if (!contactsData.isEmpty()) {
            HashMap<String, Contact> tempContactData = new HashMap<>(contactsData);
            for (User user : this.myUsers) {
                tempContactData.remove(Helper.getEndTrim(user.getId()));
            }
            ArrayList<User> inviteAble = new ArrayList<>();
            for (Map.Entry<String, Contact> contactEntry : tempContactData.entrySet()) {
                inviteAble.add(new User(contactEntry.getValue().getPhoneNumber(), contactEntry.getValue().getName()));
            }
            if (!inviteAble.isEmpty()) {
                inviteAble.add(0, new User("-1", "-1"));
            }
            //sortMyUsersByName(inviteAble);
            this.myUsers.addAll(inviteAble);
        }

        refreshUsers(-1);
    }

    @Override
    public void myContactsResult(HashMap<String, Contact> myContacts) {
        contactsData.clear();
        contactsData.putAll(myContacts);
        MyUsersFragment myUsersFragment = ((MyUsersFragment) adapter.getItem(0));
        if (myUsersFragment != null) myUsersFragment.setUserNamesAsInPhone();
        MyCallsFragment myCallsFragment = ((MyCallsFragment) adapter.getItem(2));
        if (myCallsFragment != null) myCallsFragment.setUserNamesAsInPhone();
    }

    public void disableContextualMode() {
        cabContainer.setVisibility(View.GONE);
        toolbarContainer.setVisibility(View.VISIBLE);
        ((MyUsersFragment) adapter.getItem(0)).disableContextualMode();
        ((MyGroupsFragment) adapter.getItem(1)).disableContextualMode();
        viewPager.setSwipeAble(true);
    }

    @Override
    public void enableContextualMode() {
        cabContainer.setVisibility(View.VISIBLE);
        toolbarContainer.setVisibility(View.GONE);
        viewPager.setSwipeAble(false);
    }

    @Override
    public boolean isContextualMode() {
        return cabContainer.getVisibility() == View.VISIBLE;
    }

    @Override
    public void updateSelectedCount(int count) {
        if (count > 0) {
            selectedCount.setText(String.format(getString(R.string.selected_count), count));
        } else {
            disableContextualMode();
        }
    }

    @Override
    public HashMap<String, Contact> getLocalContacts() {
        return null;
    }
}
