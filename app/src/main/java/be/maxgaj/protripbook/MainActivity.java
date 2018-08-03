package be.maxgaj.protripbook;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_pager) ViewPager viewPager;
    @BindView(R.id.main_tabs) TabLayout tabLayout;
    @BindView(R.id.main_toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(this.toolbar);

        CustomPagerAdapter pagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new CarFragment(), "CAR");
        pagerAdapter.addFragment(new TripFragment(), "TRIPS");
        this.viewPager.setAdapter(pagerAdapter);
        this.tabLayout.setupWithViewPager(this.viewPager);
    }
}
