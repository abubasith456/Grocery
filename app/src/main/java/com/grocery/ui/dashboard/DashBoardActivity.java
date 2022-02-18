package com.grocery.ui.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.grocery.BaseActivity;
import com.grocery.R;
import com.grocery.dialog.CustomDialogWithTwoButtons;
import com.grocery.ui.dashboard.tab_fragments.MessageChatActivity;
import com.grocery.ui.dashboard.tab_fragments.about_us.AboutUsFragment;
import com.grocery.ui.dashboard.tab_fragments.contact_us.ContactUsFragment;
import com.grocery.ui.dashboard.tab_fragments.products_and_services.ProductsAndServicesFragment;
import com.grocery.ui.login.LoginActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;

public class DashBoardActivity extends BaseActivity implements AboutUsFragment.OnAboutUsListener, ProductsAndServicesFragment.OnProductsAndServicesListener, ContactUsFragment.OnContactUsListener {

    @BindView(R.id.linear_layout_back)
    LinearLayout linearLayoutBack;

    @BindView(R.id.sideMenuClose)
    LinearLayout sideMenuClose;

    @BindView(R.id.sideMenu)
    FrameLayout sideMenu;

    @BindView(R.id.chatButton)
    ImageView chatButton;

    @BindView(R.id.filterButton)
    ImageView filterButton;

    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;

    @BindView(R.id.progress_bar)
    FrameLayout progressBar;

    @BindView(R.id.textViewUserName)
    TextView textViewUserName;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    AboutUsFragment aboutUsFragment;
    ProductsAndServicesFragment productsAndServicesFragment;
    ContactUsFragment contactUsFragment;
    MenuItem prevMenuItem = null;
    String userId;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
//        userId = getIntent().getStringExtra("userId");
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        try {
            bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
            setupViewPager(viewPager);

            if (ContextCompat.checkSelfPermission(this,
                    ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        ACCESS_FINE_LOCATION)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        ActivityCompat.requestPermissions(this, new String[]{/*ACCESS_BACKGROUND_LOCATION,*/ ACCESS_FINE_LOCATION, CALL_PHONE}, 1);
                    else
                        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, CALL_PHONE}, 1);
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{ACCESS_FINE_LOCATION, CALL_PHONE}, 1);
                }
            }
            loadUserInfo();
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadUserInfo();
    }

    private void loadUserInfo() {
        try {
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference documentReference = firebaseFirestore.collection("Users").document(userId);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            String nameResult = task.getResult().getString("userName");
                            String mobileNumberResult = task.getResult().getString("userMobileNumber");
                            String emailAddressResult = task.getResult().getString("userEmailAddress");
                            textViewUserName.setText("Hi... " + nameResult);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
                        ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {
                try {
                    switch (item.getItemId()) {
                        case R.id.navigation_products_and_services:
                            viewPager.setCurrentItem(0);
                            return true;
                        case R.id.navigation_contact_us:
                            viewPager.setCurrentItem(1);
                            return true;
                    }
                } catch (Exception exception) {
                    Log.e("Error ==> ", "" + exception);
                }
                return false;
            };

    private void setupViewPager(ViewPager viewPager) {
        try {
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
            productsAndServicesFragment = new ProductsAndServicesFragment();
            contactUsFragment = new ContactUsFragment();
            adapter.addFragment(productsAndServicesFragment, "");
            adapter.addFragment(contactUsFragment, "");
            viewPager.setAdapter(adapter);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (prevMenuItem != null)
                        prevMenuItem.setChecked(false);
                    else
                        bottomNavigation.getMenu().getItem(0).setChecked(false);

                    bottomNavigation.getMenu().getItem(position).setChecked(true);
                    prevMenuItem = bottomNavigation.getMenu().getItem(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @Override
    public void onShowAllCategories(String message) {
        try {

        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @Override
    public void onShowResults(String subCategoryName) {
        try {

        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @Override
    public void onShowDetailsView(String id) {
        try {

        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            try {
                switch (position) {
                    case 0:
                        aboutUsFragment = new AboutUsFragment();
                        break;
                    case 1:
                        productsAndServicesFragment = new ProductsAndServicesFragment();
                        break;
                    case 2:
                        contactUsFragment = new ContactUsFragment();
                        break;
                }
                return mFragmentList.get(position);
            } catch (Exception exception) {
                Log.e("Error ==> ", "" + exception);
                return null;
            }
        }

        @Override
        public int getCount() {
            try {
                return mFragmentList.size();

            } catch (Exception exception) {
                Log.e("Error ==> ", "" + exception);
                return 0;
            }
        }

        void addFragment(Fragment fragment, String title) {
            try {
                mFragmentList.add(fragment);
                mFragmentTitleList.add(title);
            } catch (Exception exception) {
                Log.e("Error ==> ", "" + exception);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            try {
                return mFragmentTitleList.get(position);
            } catch (Exception exception) {
                Log.e("Error ==> ", "" + exception);
                return "";
            }
        }
    }

    @OnClick(R.id.chatButton)
    void chatButtonClick() {
        try {
            startActivity(new Intent(DashBoardActivity.this, MessageChatActivity.class));
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.filterButton)
    void filterButtonClick() {
        try {

        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.locationButton)
    void locationButtonClick() {
        try {

        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.linear_layout_back)
    void hamburgerMenuClick() {
        try {
            sideMenu.setVisibility(View.VISIBLE);
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.sideMenuClose)
    void setSideMenuClose() {
        try {
            sideMenu.setVisibility(View.GONE);
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.textViewNotification)
    void setMenuOne() {
        try {
            sideMenu.setVisibility(View.GONE);
            startActivity(new Intent(this, NotificationActivity.class));
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.textViewChat)
    void setMenuTwo() {
        try {
            sideMenu.setVisibility(View.GONE);
            startActivity(new Intent(this, MessageChatActivity.class));
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.textViewChangePassword)
    void onChangePasswordClick() {
        try {
            sideMenu.setVisibility(View.GONE);
            startActivity(new Intent(this, ChangePasswordActivity.class));
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.textViewLogout)
    void setMenuThree() {
        try {
            showCustomDialogWithTwoButtons("", "Do you want to Logout?", getResources().getString(R.string.yes), getResources().getString(R.string.confirm), onDismissListener);
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    CustomDialogWithTwoButtons.OnDismissListener onDismissListener = () -> {
        sideMenu.setVisibility(View.GONE);
//        editor.putString("mobile_number", "").apply();
//        editor.putString("password", "").apply();
        FirebaseAuth.getInstance().signOut();
        GoogleSignIn.getClient(getApplicationContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut();
        Intent intent = new Intent(DashBoardActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    };


    @OnClick(R.id.locationButton)
    void onLocationClick() {
        try {

        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    @OnClick(R.id.filterButton)
    void onFilterClick() {
        try {

        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    public void showFragment(Fragment fragment, boolean addToBackStack) {
        try {
            FragmentManager manager = getSupportFragmentManager();
            androidx.fragment.app.FragmentTransaction fragmentTransaction = manager.beginTransaction();
            fragmentTransaction.replace(R.id.layout_fragment, fragment);
            if (addToBackStack) {
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    public void removeFragment() {
        try {
//            FragmentManager manager = getSupportFragmentManager();
//            androidx.fragment.app.FragmentTransaction fragmentTransaction = manager.beginTransaction();
//            fragmentTransaction.remove(Objects.requireNonNull(manager.findFragmentById(R.id.layout_fragment))).commit();

            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment != null) {
                    if (fragment.getClass().getSimpleName().equalsIgnoreCase("AllCategoriesFragment")
                            || fragment.getClass().getSimpleName().equalsIgnoreCase("SubCategoriesFragment")
                            || fragment.getClass().getSimpleName().equalsIgnoreCase("CategorySearchFragment")) {
                        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
                    }
                }
            }
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    public void showProgress() {
        try {
            progressBar.setVisibility(View.VISIBLE);
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }

    public void hideProgress() {
        try {
            progressBar.setVisibility(View.GONE);
        } catch (Exception exception) {
            Log.e("Error ==> ", "" + exception);
        }
    }
}
